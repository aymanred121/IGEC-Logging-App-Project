package com.example.igecuser.Dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.igecuser.R;
import com.example.igecuser.databinding.DialogVacationBinding;
import com.example.igecuser.fireBase.VacationRequest;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class VacationDialog extends DialogFragment {
    private VacationRequest vacationRequest;
    private String vacationNote;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();


    public VacationDialog(VacationRequest vacationRequest) {
        this.vacationRequest = vacationRequest;
        vacationNote = String.format("Starts: %s\nEnds: %s", formatDate(vacationRequest.getStartDate()), formatDate(vacationRequest.getEndDate()));
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        Window window = dialog.getWindow();

        if (window != null) {
            dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);
        }

        return dialog;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.FullscreenDialogTheme);
    }

    private DialogVacationBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DialogVacationBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        int accepted = Integer.parseInt(getDays(vacationRequest));
        int remaining = vacationRequest.getEmployee().getTotalNumberOfVacationDays();
        String days = getDays(vacationRequest);
        binding.TextInputAcceptedAmount.setText(days);
        binding.textInputLayoutAcceptedAmount.setSuffixText("of " + days);
        binding.textInputLayoutUnpaid.setSuffixText("of " + days);
        binding.TextViewVacationStatus.setText(String.format("%s\nNote: %s", vacationNote, vacationRequest.getVacationNote()));
        if (remaining < accepted)
            binding.TextInputUnpaid.setText(String.format("%d", accepted - remaining));
        binding.TextInputAcceptedAmount.addTextChangedListener(twAcceptedAmount);
        binding.TextInputUnpaid.addTextChangedListener(twUnpaid);
        binding.ButtonAccept.setOnClickListener(oclAccept);
        binding.ButtonReject.setOnClickListener(oclReject);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private String getDays(VacationRequest vacation) {
        long days = vacation.getEndDate().getTime() - vacation.getStartDate().getTime();
        days /= (24 * 3600 * 1000);
        return String.valueOf(days);
    }

    private String formatDate(Date Date) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(Date.getTime());
        return simpleDateFormat.format(calendar.getTime());
    }

    private boolean isInputValid() {

        if (binding.TextInputAcceptedAmount.getText().toString().trim().isEmpty()) {
            binding.textInputLayoutAcceptedAmount.setError("Missing");
            return false;
        }

        return binding.textInputLayoutAcceptedAmount.getError() == null && binding.textInputLayoutUnpaid.getError() == null;
    }

    private void updateVacationEndDate(VacationRequest vacationRequest, int vacationDays, int unPaidDays) {
        String day, month, year;
        Calendar calendar = Calendar.getInstance();
        Date requestStartDate = vacationRequest.getStartDate();
        calendar.setTime(requestStartDate);
        if (calendar.get(Calendar.DAY_OF_MONTH) > 25) {
            if (calendar.get(Calendar.MONTH) == 13) {
                month = "1";
                year = String.format("%02d", calendar.get(Calendar.YEAR) + 1);
            } else {
                month = String.format("%02d", calendar.get(Calendar.MONTH) - 1);
            }
        } else {
            day = String.format("%02d", calendar.get(Calendar.DAY_OF_MONTH));
            month = String.format("%02d", calendar.get(Calendar.MONTH) - 1);
            year = String.valueOf(calendar.get(Calendar.YEAR));
        }

        calendar.add(Calendar.DATE, vacationDays);
        Date newEndDate = calendar.getTime();
        vacationRequest.setEndDate(newEndDate);
        vacationRequest.setVacationStatus(1);
        db.collection("Vacation").document(vacationRequest.getId())
                .set(vacationRequest, SetOptions.merge());
        db.collection("employees").document(vacationRequest.getEmployee().getId())
                .update("totalNumberOfVacationDays", FieldValue.increment(-vacationDays));
        //todo add the unpaid days to the employeeGrossSalary when the allowances implementation is done
    }


    private TextWatcher twAcceptedAmount = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void afterTextChanged(Editable editable) {
            boolean isEmpty = binding.TextInputAcceptedAmount.getText().toString().trim().isEmpty();
            binding.TextInputUnpaid.setEnabled(isEmpty);
            if (!isEmpty) {
                int requested = Integer.parseInt(getDays(vacationRequest));
                int accepted = Integer.parseInt(binding.TextInputAcceptedAmount.getText().toString());
                int remaining = vacationRequest.getEmployee().getTotalNumberOfVacationDays();

                Date newEndDate = new Date((long) (accepted * 24 * 3600 * 1000) + vacationRequest.getStartDate().getTime());

                if (accepted > requested) {
                    binding.textInputLayoutAcceptedAmount.setErrorEnabled(false);
                    binding.textInputLayoutAcceptedAmount.setError(String.format("Can't accept more than %d", requested));
                    binding.TextInputUnpaid.setText("");
                    binding.TextViewVacationStatus.setText(String.format("%s\nNote: %s", vacationNote, vacationRequest.getVacationNote()));
                } else if (accepted == 0) {
                    binding.textInputLayoutAcceptedAmount.setErrorEnabled(false);
                    binding.textInputLayoutAcceptedAmount.setError("Invalid value");
                    binding.TextInputUnpaid.setText("");
                    binding.TextViewVacationStatus.setText(String.format("%s\nNote: %s", vacationNote, vacationRequest.getVacationNote()));
                } else {
                    binding.textInputLayoutUnpaid.setSuffixText(String.format("of %d", accepted));
                    binding.TextViewVacationStatus.setText(String.format("%s %s\nNote: %s", vacationNote, requested != accepted ? "changed to " + formatDate(newEndDate) : "", vacationRequest.getVacationNote()));
                    binding.textInputLayoutUnpaid.setEnabled(true);
                    binding.textInputLayoutAcceptedAmount.setError(null);
                    binding.textInputLayoutAcceptedAmount.setErrorEnabled(false);
                    if (remaining < accepted)
                        binding.TextInputUnpaid.setText(String.format("%d", accepted - remaining));
                    else
                        binding.TextInputUnpaid.setText("");

                }

            } else {
                binding.TextInputUnpaid.setText("");
                binding.textInputLayoutUnpaid.setSuffixText("");
                binding.TextViewVacationStatus.setText(String.format("%s\nNote: %s", vacationNote, vacationRequest.getVacationNote()));
                binding.textInputLayoutUnpaid.setEnabled(false);
                binding.textInputLayoutAcceptedAmount.setErrorEnabled(false);
                binding.textInputLayoutAcceptedAmount.setError(null);
            }
        }
    };
    private TextWatcher twUnpaid = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void afterTextChanged(Editable editable) {
            boolean isEmpty = binding.TextInputUnpaid.getText().toString().trim().isEmpty();
            if (!isEmpty) {
                int unpaid = Integer.parseInt(binding.TextInputUnpaid.getText().toString());
                int accepted = Integer.parseInt(binding.TextInputAcceptedAmount.getText().toString());
                int remaining = vacationRequest.getEmployee().getTotalNumberOfVacationDays();
                if (unpaid > accepted) {
                    binding.textInputLayoutUnpaid.setError(String.format("Can't be more than %d", accepted));
                } else if (remaining < accepted && unpaid < accepted - remaining) {
                    binding.textInputLayoutUnpaid.setError("Has to be at least " + (accepted - remaining) + " Days");
                } else {
                    binding.textInputLayoutUnpaid.setError(null);
                    binding.textInputLayoutUnpaid.setErrorEnabled(false);
                }
            } else {
                binding.textInputLayoutUnpaid.setErrorEnabled(false);
                binding.textInputLayoutUnpaid.setError(null);
            }
        }
    };
    private View.OnClickListener oclAccept = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            if (isInputValid()) {
                int acceptedDays = binding.TextInputAcceptedAmount.getText().toString().trim().isEmpty() ? 0 : Integer.parseInt(binding.TextInputAcceptedAmount.getText().toString());
                int unPaidDays = binding.TextInputUnpaid.getText().toString().trim().isEmpty() ? 0 : Integer.parseInt(binding.TextInputUnpaid.getText().toString().trim());
                updateVacationEndDate(vacationRequest, acceptedDays, unPaidDays);
                dismiss();
            }
        }
    };
    private View.OnClickListener oclReject = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            db.collection("Vacation")
                    .document(vacationRequest.getId())
                    .update("vacationStatus", -1).addOnSuccessListener(v -> dismiss());
        }
    };
}
