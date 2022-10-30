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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

public class VacationDialog extends DialogFragment {
    private final VacationRequest vacationRequest;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private boolean displayOnly;

    public VacationDialog(VacationRequest vacationRequest, boolean displayOnly) {
        this.vacationRequest = vacationRequest;
        this.displayOnly = displayOnly;
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
        binding.totalRequestedDaysEdit.setText(vacationRequest.getRequestedDaysString());
        binding.vacationNoteText.setText(String.format("Note: %s", vacationRequest.getVacationNote()));
        binding.vacationStartDateText.setText(String.format("Starts on: %s", vacationRequest.formattedStartDate()));
        binding.vacationEndDateText.setText(String.format("Ends on :%s", vacationRequest.formattedEndDate()));
        if (displayOnly) {
            binding.rejectButton.setVisibility(View.GONE);
            binding.acceptButton.setVisibility(View.GONE);
            binding.okButton.setVisibility(View.VISIBLE);
            if (vacationRequest.getVacationStatus() == ACCEPTED) {
                binding.okButton.setBackgroundColor(getResources().getColor(R.color.green));
            } else if (vacationRequest.getVacationStatus() == REJECTED) {
                binding.okButton.setBackgroundColor(getResources().getColor(R.color.red));
            }
            binding.okButton.setOnClickListener(v -> dismiss());

            // disable all fields
            binding.totalRequestedDaysEdit.setEnabled(false);
            binding.vacationDaysEdit.setEnabled(false);
            binding.sickLeaveDaysEdit.setEnabled(false);
            binding.unpaidDaysEdit.setEnabled(false);
            binding.noteEdit.setEnabled(false);
            binding.vacationDaysEdit.setText(vacationRequest.getVacationDays() != 0 ? String.format(Locale.getDefault(), "%d", vacationRequest.getVacationDays()) : "Can't be edited");
            binding.sickLeaveDaysEdit.setText(vacationRequest.getSickDays() != 0 ? String.format(Locale.getDefault(), "%d", vacationRequest.getSickDays()) : "Can't be edited");
            binding.unpaidDaysEdit.setText(vacationRequest.getUnpaidDays() != 0 ? String.format(Locale.getDefault(), "%d", vacationRequest.getUnpaidDays()) : "Can't be edited");
            binding.noteEdit.setText(vacationRequest.getFeedback());
            return;
        }
        binding.vacationDaysLayout.setSuffixText(String.format(Locale.getDefault(), " of %d", vacationRequest.getRemainingDays()));
        distributeVacation(vacationRequest.getRequestedDays());
        binding.totalRequestedDaysEdit.addTextChangedListener(twAcceptedAmount);
        binding.vacationDaysEdit.addTextChangedListener(twVacationDays);
        binding.unpaidDaysEdit.addTextChangedListener(twUnpaid);
        binding.sickLeaveDaysEdit.addTextChangedListener(twSickLeave);
        binding.acceptButton.setOnClickListener(oclAccept);
        binding.rejectButton.setOnClickListener(oclReject);
    }

    private void distributeVacation(long requested) {
        if (requested <= vacationRequest.getRemainingDays()) {
            binding.vacationDaysEdit.setText(String.format(Locale.getDefault(), "%d", requested));
        } else {
            binding.vacationDaysEdit.setText(String.format(Locale.getDefault(), "%d", vacationRequest.getRemainingDays()));
            binding.unpaidDaysEdit.setText(String.format(Locale.getDefault(), "%d", requested - vacationRequest.getRemainingDays()));
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private boolean isInputValid() {

        if (Objects.requireNonNull(binding.totalRequestedDaysEdit.getText()).toString().trim().isEmpty()) {
            binding.totalRequestedDaysLayout.setError("Missing");
            return false;
        }
        int acceptedDays = Objects.requireNonNull(binding.totalRequestedDaysEdit.getText()).toString().trim().isEmpty() ? 0 : Integer.parseInt(binding.totalRequestedDaysEdit.getText().toString());
        int unPaidDays = Objects.requireNonNull(binding.unpaidDaysEdit.getText()).toString().trim().isEmpty() ? 0 : Integer.parseInt(binding.unpaidDaysEdit.getText().toString().trim());
        int vacationDays = Objects.requireNonNull(binding.vacationDaysEdit.getText()).toString().trim().isEmpty() ? 0 : Integer.parseInt(binding.vacationDaysEdit.getText().toString().trim());
        int sickLeaveDays = Objects.requireNonNull(binding.sickLeaveDaysEdit.getText()).toString().trim().isEmpty() ? 0 : Integer.parseInt(binding.sickLeaveDaysEdit.getText().toString().trim());
        boolean valid = acceptedDays == vacationDays + sickLeaveDays + unPaidDays;
        if (!valid)
            binding.totalRequestedDaysLayout.setError("Total accepted days must be equal to the sum of vacation days, sick leave days and unpaid days");
        else
            binding.totalRequestedDaysLayout.setError(null);
        return valid;
    }

    @SuppressLint("DefaultLocale")
    private void updateVacationEndDate(VacationRequest vacationRequest, int totalAcceptedDays, int unPaidDays, int sickLeaveDays, int vacationDays) {
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
        calendar.add(Calendar.DATE, totalAcceptedDays);
        Date newEndDate = calendar.getTime();
        vacationRequest.setEndDate(newEndDate);
        vacationRequest.setVacationDays(vacationDays);
        vacationRequest.setSickDays(sickLeaveDays);
        vacationRequest.setUnpaidDays(unPaidDays);
        vacationRequest.setFeedback(binding.noteEdit.getText().toString());
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
            boolean isEmpty = Objects.requireNonNull(binding.totalRequestedDaysEdit.getText()).toString().trim().isEmpty();
            binding.unpaidDaysEdit.setEnabled(!isEmpty);
            binding.vacationNoteText.setEnabled(!isEmpty);
            binding.sickLeaveDaysEdit.setEnabled(!isEmpty);
            if (!isEmpty) {
                int accepted = Integer.parseInt(binding.totalRequestedDaysEdit.getText().toString());
                if (accepted > vacationRequest.getRequestedDays()) {
                    binding.totalRequestedDaysLayout.setErrorEnabled(false);
                    binding.totalRequestedDaysLayout.setError(String.format("Can't accept more than %d", vacationRequest.getRequestedDays()));
                } else if (accepted == 0) {
                    binding.totalRequestedDaysLayout.setErrorEnabled(false);
                    binding.totalRequestedDaysLayout.setError("Invalid value");
                } else {
                    binding.totalRequestedDaysLayout.setError(null);
                    binding.totalRequestedDaysLayout.setErrorEnabled(false);
                    // choose lesser or equal to the requested days
                    if (accepted != vacationRequest.getRequestedDays()) {
                        distributeVacation(accepted);
                    }
                }

            } else {
                binding.totalRequestedDaysLayout.setErrorEnabled(false);
                binding.totalRequestedDaysLayout.setError(null);
            }
        }
    };
    private final TextWatcher twVacationDays = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void afterTextChanged(Editable editable) {
            boolean isEmpty = Objects.requireNonNull(binding.vacationDaysEdit.getText()).toString().trim().isEmpty();
            if (!isEmpty) {
                int vacationDays = Integer.parseInt(binding.vacationDaysEdit.getText().toString());
                if (vacationDays > vacationRequest.getRequestedDays()) {
                    binding.vacationDaysLayout.setErrorEnabled(false);
                    binding.vacationDaysLayout.setError(String.format(Locale.getDefault(), "Can't be more than %d", vacationRequest.getRequestedDays()));
                } else {
                    binding.vacationDaysLayout.setError(null);
                    binding.vacationDaysLayout.setErrorEnabled(false);
                }
            } else {
                binding.vacationDaysLayout.setErrorEnabled(false);
                binding.vacationDaysLayout.setError(null);
            }
            isInputValid();

        }
    };
    private final TextWatcher twSickLeave = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void afterTextChanged(Editable editable) {
            isInputValid();
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
            isInputValid();
        }
    };
    private final View.OnClickListener oclAccept = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            if (isInputValid()) {
                int acceptedDays = Objects.requireNonNull(binding.totalRequestedDaysEdit.getText()).toString().trim().isEmpty() ? 0 : Integer.parseInt(binding.totalRequestedDaysEdit.getText().toString());
                int unPaidDays = Objects.requireNonNull(binding.unpaidDaysEdit.getText()).toString().trim().isEmpty() ? 0 : Integer.parseInt(binding.unpaidDaysEdit.getText().toString().trim());
                int vacationDays = Objects.requireNonNull(binding.vacationDaysEdit.getText()).toString().trim().isEmpty() ? 0 : Integer.parseInt(binding.vacationDaysEdit.getText().toString().trim());
                int sickLeaveDays = Objects.requireNonNull(binding.sickLeaveDaysEdit.getText()).toString().trim().isEmpty() ? 0 : Integer.parseInt(binding.sickLeaveDaysEdit.getText().toString().trim());
                updateVacationEndDate(vacationRequest, acceptedDays, unPaidDays, sickLeaveDays, vacationDays);
                dismiss();
            }
        }
    };
    private final View.OnClickListener oclReject = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            VACATION_COL
                    .document(vacationRequest.getId())
                    .update("vacationStatus", REJECTED, "feedback", binding.noteEdit.getText().toString()).addOnSuccessListener(v -> dismiss());
        }
    };
}
