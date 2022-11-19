package com.igec.user.fragments;

import static com.igec.common.CONSTANTS.ADMIN;
import static com.igec.common.CONSTANTS.EMPLOYEE_COL;
import static com.igec.common.CONSTANTS.VACATION_COL;
import static com.igec.common.CONSTANTS.convertDateToString;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.util.Pair;
import androidx.fragment.app.Fragment;

import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.datepicker.MaterialDatePicker;
import com.igec.user.R;
import com.igec.common.firebase.Employee;
import com.igec.common.firebase.VacationRequest;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.DateValidatorPointForward;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.FirebaseFirestore;
import com.igec.user.activities.DateInaccurate;
import com.igec.user.databinding.FragmentSendVacationRequestBinding;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;


public class SendVacationRequestFragment extends Fragment {

    //Views
    private ArrayList<Pair<TextInputLayout, TextInputEditText>> views;
    private MaterialDatePicker.Builder<Pair<Long, Long>> vDatePickerBuilder = MaterialDatePicker.Builder.dateRangePicker();
    private MaterialDatePicker vDatePicker;
    //Vars
    private long endDate, startDate;
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
    public void onResume() {
        super.onResume();
        validateDate(getActivity());
    }

    private void validateDate(Context c) {
        if (Settings.Global.getInt(c.getContentResolver(), Settings.Global.AUTO_TIME, 0) != 1) {
            Intent intent = new Intent(getActivity(), DateInaccurate.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            getActivity().finish();
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Initialize();

        binding.dateLayout.setEndIconOnClickListener(oclVacationDate);
        binding.dateLayout.setErrorIconOnClickListener(oclVacationDate);
        binding.sendButton.setOnClickListener(oclSendRequest);
        binding.dateEdit.addTextChangedListener(twVacationDate);
        binding.noteEdit.addTextChangedListener(twVacationNote);
    }

    public static SendVacationRequestFragment newInstance(Employee currEmployee) {

        Bundle args = new Bundle();
        args.putSerializable("currEmployee", currEmployee);
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
        views.add(new Pair<>(binding.noteLayout, binding.noteEdit));

        CalendarConstraints.Builder builder = new CalendarConstraints.Builder();
        builder.setValidator(DateValidatorPointForward.now());
        datePickerSetup();
    }

    private void datePickerSetup() {
        vDatePickerBuilder.setTitleText("Vacation Days");
        vDatePicker = vDatePickerBuilder.build();
        vDatePicker.addOnPositiveButtonClickListener(selection -> {
            Pair<Long, Long> selectedDates = (Pair<Long, Long>) selection;
            startDate = selectedDates.first;
            endDate = selectedDates.second;
            binding.dateEdit.setText(convertDateToString(selectedDates.first) + " - " + convertDateToString(selectedDates.second));
        });
    }

    private boolean isDateValid(long start, long end) {
        return start > System.currentTimeMillis() && !isFriday(start, end);
    }

    private boolean isFriday(long start, long end) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date(start));
        return calendar.get(Calendar.DAY_OF_WEEK) == Calendar.FRIDAY && start == end;
    }
    private void uploadVacationRequest() {
        String vacationID = VACATION_COL.document().getId().substring(0, 5);
        EMPLOYEE_COL
                .document(currEmployee.getManagerID() == null ? ADMIN : currEmployee.getManagerID())
                .addSnapshotListener((value, error) -> {
                    EMPLOYEE_COL.document(currEmployee.getId()).get().addOnSuccessListener(d -> {
                        currEmployee = d.toObject(Employee.class);

                        vacationRequest = new VacationRequest(
                                new Date(startDate),
                                new Date(endDate),
                                (new Date()),
                                value.toObject(Employee.class),
                                currEmployee,
                                binding.noteEdit.getText().toString()
                        );
                        vacationRequest.setId(vacationID);
                        VACATION_COL.document(vacationID).set(vacationRequest).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                binding.sendButton.setEnabled(true);
                                clearInputs();
                            }
                        });
                    });
                });
    }

    private void clearInputs() {
        binding.dateEdit.setText(null);
        binding.noteEdit.setText(null);
    }

    private boolean generateError() {

        for (Pair<TextInputLayout, TextInputEditText> view : views) {
            if (view.second.getText().toString().trim().isEmpty()) {
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
        if (!isDateValid(startDate, endDate))
            binding.dateLayout.setError("Invalid Date");
        else
            hideError(binding.dateLayout);
        return !generateError() && isDateValid(startDate, endDate);
    }


    // Listeners
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
            binding.sendButton.setEnabled(false);
            uploadVacationRequest();
        }

    };
    private View.OnClickListener oclVacationDate = v -> {
        vDatePicker.show(getParentFragmentManager(), "DATE_PICKER");
    };

}