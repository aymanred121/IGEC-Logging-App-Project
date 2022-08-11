package com.igec.user.Fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.igec.user.R;
import com.igec.common.firebase.Employee;
import com.igec.common.firebase.VacationRequest;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.DateValidatorPointForward;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.FirebaseFirestore;
import com.igec.user.databinding.FragmentSendVacationRequestBinding;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;


public class SendVacationRequestFragment extends Fragment implements DatePickerDialog.OnDateSetListener {

    //Views
    private ArrayList<Pair<TextInputLayout, TextInputEditText>> views;
    private DatePickerDialog dpd;
    //Vars
    private int remainingDays, daysAfterVacationIsTaken;
    private long days, startDate;
    private Employee currEmployee;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private VacationRequest vacationRequest;

    //Overrides
    private FragmentSendVacationRequestBinding binding;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentSendVacationRequestBinding.inflate(inflater, container, false);
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
        Initialize();

        binding.dateLayout.setEndIconOnClickListener(oclVacationDate);
        binding.dateLayout.setErrorIconOnClickListener(oclVacationDate);
        binding.sendButton.setOnClickListener(oclSendRequest);
        binding.daysEdit.addTextChangedListener(twVacationDays);
        binding.dateEdit.addTextChangedListener(twVacationDate);
        binding.noteEdit.addTextChangedListener(twVacationNote);
    }

    public static SendVacationRequestFragment newInstance(Employee currEmployee) {

        Bundle args = new Bundle();
        args.putSerializable("currEmployee",currEmployee);
        SendVacationRequestFragment fragment = new SendVacationRequestFragment();
        fragment.setArguments(args);
        return fragment;
    }
    //Functions
//    public SendVacationRequestFragment(Employee currEmployee) {
//        this.currEmployee = currEmployee;
//    }

    @SuppressLint("DefaultLocale")
    private void Initialize() {
        currEmployee = (Employee) getArguments().getSerializable("currEmployee");

        views = new ArrayList<>();
        views.add(new Pair<>(binding.dateLayout, binding.dateEdit));
        views.add(new Pair<>(binding.daysLayout, binding.daysEdit));
        views.add(new Pair<>(binding.noteLayout, binding.noteEdit));

        CalendarConstraints.Builder builder = new CalendarConstraints.Builder();
        builder.setValidator(DateValidatorPointForward.now());
        remainingDays = currEmployee.getTotalNumberOfVacationDays();
        binding.daysLayout.setHelperText(String.format("%d days Remaining", remainingDays));
        datePickerSetup();


    }

    private void datePickerSetup() {
        Calendar now = Calendar.getInstance();
        dpd = DatePickerDialog.newInstance(
                this,
                now.get(Calendar.YEAR),
                now.get(Calendar.MONTH),
                now.get(Calendar.DAY_OF_MONTH)
        );
        dpd.setTitle("Vacation Date");
        dpd.setOkText("Set");
        dpd.setOkColor(getResources().getColor(R.color.green));
        dpd.setCancelColor(getResources().getColor(R.color.Red));
        //dpd.setMinDate(Calendar.getInstance());
        Calendar nextYear = Calendar.getInstance();
        nextYear.add(Calendar.YEAR, 2);
        nextYear.add(Calendar.MONTH, -nextYear.get(Calendar.MONTH));
        nextYear.add(Calendar.DAY_OF_YEAR, -nextYear.get(Calendar.DAY_OF_YEAR));
        dpd.setMaxDate(nextYear);
        // Calendar friday;
        Calendar friday;
        List<Calendar> weekends = new ArrayList<>();
        int weeks = 104;
        for (int i = 0; i < (weeks * 7); i += 7) {
            friday = Calendar.getInstance();
            friday.add(Calendar.DAY_OF_YEAR, (Calendar.FRIDAY - friday.get(Calendar.DAY_OF_WEEK) + i));
            weekends.add(friday);
        }
        Calendar[] disabledDays = weekends.toArray(new Calendar[weekends.size()]);
        dpd.setDisabledDays(disabledDays);
    }

    private void uploadVacationRequest() {
        String vacationID = db.collection("Vacation").document().getId().substring(0, 5);
        db.collection("employees")
                .document(currEmployee.getManagerID())
                .addSnapshotListener((value, error) -> {
                    days = ((long) Integer.parseInt(binding.daysEdit.getText().toString()) * 24 * 3600 * 1000) + startDate;
                    vacationRequest = new VacationRequest(
                            new Date(startDate),
                            new Date(days),
                            (new Date()),
                            value.toObject(Employee.class),
                            currEmployee,
                            binding.noteEdit.getText().toString()
                    );
                    vacationRequest.setId(vacationID);
                    db.collection("Vacation").document(vacationID).set(vacationRequest).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            clearInputs();
                        }
                    });

                });
    }

    private void clearInputs() {
        binding.dateEdit.setText(null);
        binding.daysEdit.setText(null);
        binding.noteEdit.setText(null);
    }

    private boolean generateError() {

        for (Pair<TextInputLayout, TextInputEditText> view : views) {
            if (view.second.getText().toString().trim().isEmpty()) {
                if (view.first == binding.dateLayout)
                    view.first.setErrorIconDrawable(R.drawable.ic_baseline_calendar_month_24);

                view.first.setError("Missing");
                return true;
            }
            if (view.first.getError() != null) {
                return true;
            }
        }
        return false;
    }

    private void hideError(TextInputLayout textInputLayout) {
        textInputLayout.setErrorEnabled(textInputLayout.getError() != null);

    }


    private boolean validateInputs() {
        return !generateError();
    }

    private String convertDateToString(long selection) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy");
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(selection);
        return simpleDateFormat.format(calendar.getTime());
    }

    // Listeners
    private TextWatcher twVacationDays = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @SuppressLint("DefaultLocale")
        @Override
        public void afterTextChanged(Editable editable) {
            daysAfterVacationIsTaken = binding.daysEdit.getText().toString().trim().equals("") ? remainingDays : remainingDays - Integer.parseInt(binding.daysEdit.getText().toString());
            if (daysAfterVacationIsTaken < 0) {
                binding.daysLayout.setHelperText(String.format("Exceeds remaining by %d", -daysAfterVacationIsTaken));
            } else if (daysAfterVacationIsTaken == remainingDays && !binding.daysEdit.getText().toString().trim().isEmpty()) {
                binding.daysLayout.setError("Invalid Value");
            } else {
                binding.daysLayout.setHelperText(String.format("%d days Remaining", daysAfterVacationIsTaken));
                binding.daysLayout.setError(null);
            }

            hideError(binding.daysLayout);
        }
    };
    private TextWatcher twVacationDate = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void afterTextChanged(Editable editable) {
            if (!binding.dateEdit.getText().toString().trim().isEmpty())
                binding.dateLayout.setError(null);

            hideError(binding.dateLayout);
        }
    };
    private TextWatcher twVacationNote = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void afterTextChanged(Editable editable) {
            if (!binding.noteEdit.getText().toString().trim().isEmpty())
                binding.noteLayout.setError(null);

            hideError(binding.noteLayout);
        }
    };
    private View.OnClickListener oclSendRequest = v -> {
        if (validateInputs()) {
            if (daysAfterVacationIsTaken < 0) {
                MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(getActivity());
                builder.setTitle("Days exceeded by " + daysAfterVacationIsTaken * -1 + " ...")
                        .setMessage(daysAfterVacationIsTaken * -1 + " Days at least will be considered as Unpaid")
                        .setCancelable(true)
                        .setPositiveButton("ok, send", (dialogInterface, i) -> {
                            uploadVacationRequest();
                            dialogInterface.dismiss();
                        })
                        .setNegativeButton("No", (dialogInterface, i) -> {
                            dialogInterface.dismiss();
                        });
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            } else {
                uploadVacationRequest();
            }

        }

    };
    private View.OnClickListener oclVacationDate = v -> {
        dpd.show(getParentFragmentManager(), "DATE_PICKER");
    };

    @Override
    public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
        Calendar vacation = Calendar.getInstance();
        vacation.set(year, monthOfYear, dayOfMonth);
        binding.dateEdit.setText(convertDateToString(vacation.getTime().getTime()));
        startDate = vacation.getTime().getTime();
    }
}