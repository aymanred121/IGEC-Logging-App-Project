package com.igec.common.Dialogs;

import static com.igec.common.CONSTANTS.EMPLOYEE_COL;
import static com.igec.common.CONSTANTS.EMPLOYEE_GROSS_SALARY_COL;
import static com.igec.common.CONSTANTS.VACATION_COL;

import android.annotation.SuppressLint;
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

import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.igec.common.firebase.Allowance;
import com.igec.common.firebase.EmployeesGrossSalary;
import com.igec.common.firebase.VacationRequest;
import com.igec.common.utilities.allowancesEnum;
import com.igec.common.R;
import com.igec.common.databinding.DialogVacationBinding;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

public class VacationDialog extends DialogFragment {
    private final VacationRequest vacationRequest;
    private final String vacationNote;
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
    public void onDestroy() {
        super.onDestroy();
        binding = null;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        int accepted = Integer.parseInt(vacationRequest.getDays());
        int remaining = vacationRequest.getEmployee().getTotalNumberOfVacationDays();
        binding.TextInputAcceptedAmount.setText(String.format("%s", vacationRequest.getDays()));
        binding.textInputLayoutAcceptedAmount.setSuffixText(String.format("of %s", vacationRequest.getDays()));
        binding.textInputLayoutUnpaid.setSuffixText(String.format("of %s", vacationRequest.getDays()));
        binding.TextViewVacationStatus.setText(String.format("%s\nNote: %s", vacationNote, vacationRequest.getVacationNote()));
        if (remaining < accepted)
            binding.TextInputUnpaid.setText(String.format(Locale.getDefault(),"%d", accepted - remaining));
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

    private String formatDate(Date Date) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(Date.getTime());
        return simpleDateFormat.format(calendar.getTime());
    }

    private boolean isInputValid() {

        if (Objects.requireNonNull(binding.TextInputAcceptedAmount.getText()).toString().trim().isEmpty()) {
            binding.textInputLayoutAcceptedAmount.setError("Missing");
            return false;
        }

        return binding.textInputLayoutAcceptedAmount.getError() == null && binding.textInputLayoutUnpaid.getError() == null;
    }

    @SuppressLint("DefaultLocale")
    private void updateVacationEndDate(VacationRequest vacationRequest, int vacationDays, int unPaidDays) {
        String month, year;
        Calendar calendar = Calendar.getInstance();
        Date requestStartDate = vacationRequest.getStartDate();
        calendar.setTime(requestStartDate);
        month = String.format(Locale.getDefault(),"%02d", calendar.get(Calendar.MONTH) + 1);
        year = String.valueOf(calendar.get(Calendar.YEAR));
        if (calendar.get(Calendar.DAY_OF_MONTH) > 25) {
            if (calendar.get(Calendar.MONTH) == Calendar.DECEMBER) {
                month = "01";
                year = String.format(Locale.getDefault(),"%02d", calendar.get(Calendar.YEAR) + 1);
            } else {
                month = String.format(Locale.getDefault(),"%02d", calendar.get(Calendar.MONTH) + 2);
            }
        }
        calendar.add(Calendar.DATE, vacationDays);
        Date newEndDate = calendar.getTime();
        vacationRequest.setEndDate(newEndDate);
        vacationRequest.setVacationStatus(1);
        VACATION_COL.document(vacationRequest.getId())
                .set(vacationRequest, SetOptions.merge());
        EMPLOYEE_COL.document(vacationRequest.getEmployee().getId())
                .update("totalNumberOfVacationDays", FieldValue.increment(-vacationDays));
        if(unPaidDays==0)
            return;
        EMPLOYEE_GROSS_SALARY_COL.document(vacationRequest.getEmployee().getId()).collection(year).document(month).get().addOnSuccessListener(documentSnapshot -> {
            if (!documentSnapshot.exists()) {
                EMPLOYEE_GROSS_SALARY_COL.document(vacationRequest.getEmployee().getId()).get().addOnSuccessListener(doc->{
                    if(!doc.exists())
                        return;
                    EmployeesGrossSalary employeesGrossSalary = doc.toObject(EmployeesGrossSalary.class);
                    assert employeesGrossSalary != null;
                    ArrayList<Allowance> allTypes = employeesGrossSalary.getAllTypes();
                    for (Allowance allowance:allTypes) {
                        if (allowance.getType() != allowancesEnum.NETSALARY.ordinal()) {
                            employeesGrossSalary.getBaseAllowances().add(allowance);
                            employeesGrossSalary.getAllTypes().remove(allowance);
                        }
                    }
                    Allowance unPaidAllowance = new Allowance();
                    unPaidAllowance.setAmount(unPaidDays * (vacationRequest.getEmployee().getSalary() / 30));
                    unPaidAllowance.setType(allowancesEnum.RETENTION.ordinal());
                    unPaidAllowance.setName("unpaid");
                    unPaidAllowance.setNote(String.format("%d", unPaidDays));
                    employeesGrossSalary.getAllTypes().add(unPaidAllowance);
                    db.document(documentSnapshot.getReference().getPath()).set(employeesGrossSalary,SetOptions.merge());
                });

            } else {
                Allowance unPaidAllowance = new Allowance();
                unPaidAllowance.setAmount(-unPaidDays * (vacationRequest.getEmployee().getSalary() / 30));
                unPaidAllowance.setType(allowancesEnum.RETENTION.ordinal());
                unPaidAllowance.setName("unpaid");
                unPaidAllowance.setNote(String.format("%d", unPaidDays));
                db.document(documentSnapshot.getReference().getPath()).update("allTypes",FieldValue.arrayUnion(unPaidAllowance));
            }
        });
    }


    private final TextWatcher twAcceptedAmount = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @SuppressLint("DefaultLocale")
        @Override
        public void afterTextChanged(Editable editable) {
            boolean isEmpty = Objects.requireNonNull(binding.TextInputAcceptedAmount.getText()).toString().trim().isEmpty();
            binding.TextInputUnpaid.setEnabled(isEmpty);
            if (!isEmpty) {
                int requested = Integer.parseInt(vacationRequest.getDays());
                int accepted = Integer.parseInt(binding.TextInputAcceptedAmount.getText().toString());
                int remaining = vacationRequest.getEmployee().getTotalNumberOfVacationDays();

                Date newEndDate = new Date((long) ((long) accepted * 24 * 3600 * 1000) + vacationRequest.getStartDate().getTime());

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
    private final TextWatcher twUnpaid = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @SuppressLint("DefaultLocale")
        @Override
        public void afterTextChanged(Editable editable) {
            boolean isEmpty = Objects.requireNonNull(binding.TextInputUnpaid.getText()).toString().trim().isEmpty();
            if (!isEmpty) {
                int unpaid = Integer.parseInt(binding.TextInputUnpaid.getText().toString());
                int accepted = Integer.parseInt(Objects.requireNonNull(binding.TextInputAcceptedAmount.getText()).toString());
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
    private final View.OnClickListener oclAccept = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            if (isInputValid()) {
                int acceptedDays = Objects.requireNonNull(binding.TextInputAcceptedAmount.getText()).toString().trim().isEmpty() ? 0 : Integer.parseInt(binding.TextInputAcceptedAmount.getText().toString());
                int unPaidDays = Objects.requireNonNull(binding.TextInputUnpaid.getText()).toString().trim().isEmpty() ? 0 : Integer.parseInt(binding.TextInputUnpaid.getText().toString().trim());
                updateVacationEndDate(vacationRequest, acceptedDays, unPaidDays);
                dismiss();
            }
        }
    };
    private final View.OnClickListener oclReject = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            VACATION_COL
                    .document(vacationRequest.getId())
                    .update("vacationStatus", -1).addOnSuccessListener(v -> dismiss());
        }
    };
}
