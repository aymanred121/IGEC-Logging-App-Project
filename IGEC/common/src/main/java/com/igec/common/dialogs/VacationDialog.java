package com.igec.common.dialogs;

import static com.igec.common.CONSTANTS.ACCEPTED;
import static com.igec.common.CONSTANTS.EMPLOYEE_COL;
import static com.igec.common.CONSTANTS.EMPLOYEE_GROSS_SALARY_COL;
import static com.igec.common.CONSTANTS.REJECTED;
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
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.igec.common.R;
import com.igec.common.databinding.DialogVacationBinding;
import com.igec.common.firebase.Allowance;
import com.igec.common.firebase.EmployeesGrossSalary;
import com.igec.common.firebase.VacationRequest;
import com.igec.common.utilities.AllowancesEnum;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class VacationDialog extends DialogFragment {
    private final VacationRequest vacationRequest;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();


    public VacationDialog(VacationRequest vacationRequest) {
        this.vacationRequest = vacationRequest;
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

        binding.totalAcceptedDaysEdit.setText(String.format("%s", vacationRequest.getDays()));
        binding.vacationNoteText.setText(String.format("Note: %s", vacationRequest.getVacationNote()));
        binding.vacationStartDateText.setText(String.format("Starts on: %s", vacationRequest.formattedStartDate()));
        binding.vacationEndDateText.setText(String.format("Ends on :%s", vacationRequest.formattedEndDate()));
        if (vacationRequest.getRemainingDays() < vacationRequest.getRequestedDays())
            binding.unpaidDaysEdit.setText(String.format(Locale.getDefault(), "%d", vacationRequest.getRequestedDays() - vacationRequest.getRemainingDays()));
        binding.totalAcceptedDaysEdit.addTextChangedListener(twAcceptedAmount);
        binding.unpaidDaysEdit.addTextChangedListener(twUnpaid);
        binding.acceptButton.setOnClickListener(oclAccept);
        binding.rejectButton.setOnClickListener(oclReject);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
    private boolean isInputValid() {

        if (Objects.requireNonNull(binding.totalAcceptedDaysEdit.getText()).toString().trim().isEmpty()) {
            binding.totalAcceptedDaysLayout.setError("Missing");
            return false;
        }

        return binding.totalAcceptedDaysLayout.getError() == null && binding.unpaidDaysLayout.getError() == null;
    }
    @SuppressLint("DefaultLocale")
    private void updateVacationEndDate(VacationRequest vacationRequest, int vacationDays, int unPaidDays) {
        String month, year;
        Calendar calendar = Calendar.getInstance();
        Date requestStartDate = vacationRequest.getStartDate();
        calendar.setTime(requestStartDate);
        month = String.format(Locale.getDefault(), "%02d", calendar.get(Calendar.MONTH) + 1);
        year = String.valueOf(calendar.get(Calendar.YEAR));
        if (calendar.get(Calendar.DAY_OF_MONTH) > 25) {
            if (calendar.get(Calendar.MONTH) == Calendar.DECEMBER) {
                month = "01";
                year = String.format(Locale.getDefault(), "%02d", calendar.get(Calendar.YEAR) + 1);
            } else {
                month = String.format(Locale.getDefault(), "%02d", calendar.get(Calendar.MONTH) + 2);
            }
        }
        calendar.add(Calendar.DATE, vacationDays);
        Date newEndDate = calendar.getTime();
        vacationRequest.setEndDate(newEndDate);
        vacationRequest.setVacationStatus(ACCEPTED);
        VACATION_COL.document(vacationRequest.getId())
                .set(vacationRequest, SetOptions.merge());
        EMPLOYEE_COL.document(vacationRequest.getEmployee().getId())
                .update("totalNumberOfVacationDays", FieldValue.increment(-vacationDays));
        if (unPaidDays == 0)
            return;
        EMPLOYEE_GROSS_SALARY_COL.document(vacationRequest.getEmployee().getId()).collection(year).document(month).get().addOnSuccessListener(documentSnapshot -> {
            if (!documentSnapshot.exists()) {
                EMPLOYEE_GROSS_SALARY_COL.document(vacationRequest.getEmployee().getId()).get().addOnSuccessListener(doc -> {
                    if (!doc.exists())
                        return;
                    EmployeesGrossSalary employeesGrossSalary = doc.toObject(EmployeesGrossSalary.class);
                    assert employeesGrossSalary != null;
                    ArrayList<Allowance> allTypes = employeesGrossSalary.getAllTypes();
                    for (Allowance allowance : allTypes) {
                        if (allowance.getType() != AllowancesEnum.NETSALARY.ordinal()) {
                            employeesGrossSalary.getBaseAllowances().add(allowance);
                            employeesGrossSalary.getAllTypes().remove(allowance);
                        }
                    }
                    Allowance unPaidAllowance = new Allowance();
                    unPaidAllowance.setAmount(unPaidDays * (vacationRequest.getEmployee().getSalary() / 30));
                    unPaidAllowance.setType(AllowancesEnum.RETENTION.ordinal());
                    unPaidAllowance.setName("unpaid");
                    unPaidAllowance.setNote(String.format("%d", unPaidDays));
                    employeesGrossSalary.getAllTypes().add(unPaidAllowance);
                    db.document(documentSnapshot.getReference().getPath()).set(employeesGrossSalary, SetOptions.merge());
                });

            } else {
                Allowance unPaidAllowance = new Allowance();
                unPaidAllowance.setAmount(-unPaidDays * (vacationRequest.getEmployee().getSalary() / 30));
                unPaidAllowance.setType(AllowancesEnum.RETENTION.ordinal());
                unPaidAllowance.setName("unpaid");
                unPaidAllowance.setNote(String.format("%d", unPaidDays));
                db.document(documentSnapshot.getReference().getPath()).update("allTypes", FieldValue.arrayUnion(unPaidAllowance));
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
            boolean isEmpty = Objects.requireNonNull(binding.totalAcceptedDaysEdit.getText()).toString().trim().isEmpty();
            binding.unpaidDaysEdit.setEnabled(isEmpty);
            if (!isEmpty) {
                int requested = Integer.parseInt(vacationRequest.getDays());
                int accepted = Integer.parseInt(binding.totalAcceptedDaysEdit.getText().toString());
                if (accepted > requested) {
                    binding.totalAcceptedDaysLayout.setErrorEnabled(false);
                    binding.totalAcceptedDaysLayout.setError(String.format("Can't accept more than %d", requested));
                } else if (accepted == 0) {
                    binding.totalAcceptedDaysLayout.setErrorEnabled(false);
                    binding.totalAcceptedDaysLayout.setError("Invalid value");
                } else {
                    binding.totalAcceptedDaysLayout.setError(null);
                    binding.totalAcceptedDaysLayout.setErrorEnabled(false);
                }

            } else {
                binding.totalAcceptedDaysLayout.setErrorEnabled(false);
                binding.totalAcceptedDaysLayout.setError(null);
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
            boolean isEmpty = Objects.requireNonNull(binding.unpaidDaysEdit.getText()).toString().trim().isEmpty();
            int unpaid = 0;
            int accepted = 0;
            if (binding.totalAcceptedDaysEdit.getText().toString().trim().length() != 0)
                accepted = Integer.parseInt(binding.totalAcceptedDaysEdit.getText().toString());
            int remaining = vacationRequest.getEmployee().getTotalNumberOfVacationDays();
            boolean errorFlag = false;
            if (!isEmpty) {
                unpaid = Integer.parseInt(binding.unpaidDaysEdit.getText().toString());
                if (unpaid > accepted) {
                    binding.unpaidDaysLayout.setError(String.format("Can't be more than %d", accepted));
                    errorFlag = true;
                } else if (remaining < accepted && unpaid < accepted - remaining) {
                    binding.unpaidDaysLayout.setError("Has to be at least " + (accepted - remaining) + " Days");
                    errorFlag = true;
                } else {
                    binding.unpaidDaysLayout.setError(null);
                    binding.unpaidDaysLayout.setErrorEnabled(false);
                }
            } else if (isEmpty) {
                if (remaining < accepted) {
                    binding.unpaidDaysLayout.setError("Has to be at least " + (accepted - remaining) + " Days");
                    errorFlag = true;
                }
            }
            if (!errorFlag) {
                binding.unpaidDaysLayout.setErrorEnabled(false);
                binding.unpaidDaysLayout.setError(null);
            }
        }
    };
    private final View.OnClickListener oclAccept = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            if (isInputValid()) {
                int acceptedDays = Objects.requireNonNull(binding.totalAcceptedDaysEdit.getText()).toString().trim().isEmpty() ? 0 : Integer.parseInt(binding.totalAcceptedDaysEdit.getText().toString());
                int unPaidDays = Objects.requireNonNull(binding.unpaidDaysEdit.getText()).toString().trim().isEmpty() ? 0 : Integer.parseInt(binding.unpaidDaysEdit.getText().toString().trim());
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
                    .update("vacationStatus", REJECTED).addOnSuccessListener(v -> dismiss());
        }
    };
}
