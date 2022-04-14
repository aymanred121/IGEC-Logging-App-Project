package com.example.igecuser.Fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.igecuser.R;
import com.example.igecuser.fireBase.Employee;
import com.example.igecuser.fireBase.VacationRequest;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.DateValidatorPointForward;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;


public class SendVacationRequestFragment extends Fragment {

    //Views
    private TextInputEditText vVacationDate, vVacationNote, vVacationDays;
    private TextInputLayout vVacationDateLayout, vVacationDaysLayout;
    private MaterialButton vSendRequest;
    private MaterialDatePicker.Builder<Long> vDatePickerBuilder = MaterialDatePicker.Builder.datePicker();
    private MaterialDatePicker vDatePicker;
    //Vars
    private int remainingDays, daysAfterVacationIsTaken;
    private long days, startDate;
    private Employee currEmployee;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private VacationRequest vacationRequest;

    //Overrides
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_vacation_request, container, false);

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Initialize(view);

        vVacationDateLayout.setEndIconOnClickListener(clVacationDate);
        vDatePicker.addOnPositiveButtonClickListener(pclDatePicker);
        vSendRequest.setOnClickListener(clSendRequest);
        vVacationDays.addTextChangedListener(twVacationDays);
    }

    //Functions
    public SendVacationRequestFragment(Employee currEmployee) {
        this.currEmployee = currEmployee;
    }

    private void Initialize(View view) {
        vVacationDate = view.findViewById(R.id.TextInput_VacationDate);
        vVacationNote = view.findViewById(R.id.TextInput_VacationNote);
        vVacationDays = view.findViewById(R.id.TextInput_VacationDays);
        vVacationDateLayout = view.findViewById(R.id.textInputLayout_VacationDate);
        vVacationDaysLayout = view.findViewById(R.id.textInputLayout_VacationDays);
        vSendRequest = view.findViewById(R.id.Button_SendRequest);
        vDatePickerBuilder.setTitleText("Vacation Date");
        CalendarConstraints.Builder builder = new CalendarConstraints.Builder();
        builder.setValidator(DateValidatorPointForward.now());
        vDatePickerBuilder.setCalendarConstraints(builder.build());
        vDatePicker = vDatePickerBuilder.build();
        remainingDays = currEmployee.getTotalNumberOfVacationDays();
        vVacationDaysLayout.setHelperText(String.format("%d days Remaining", remainingDays));

    }

    private void uploadVacationRequest() {
        String vacationID = db.collection("Vacation").document().getId().substring(0, 5);
        db.collection("employees")
                .document(currEmployee.getManagerID())
                .addSnapshotListener((value, error) -> {
                    days = ((long) Integer.parseInt(vVacationDays.getText().toString()) * 24 * 3600 * 1000) + startDate;
                    vacationRequest = new VacationRequest(
                            new Date(startDate),
                            new Date(days),
                            (new Date()),
                            value.toObject(Employee.class),
                            currEmployee,
                            vVacationNote.getText().toString()
                    );
                    vacationRequest.setId(vacationID);
                    db.collection("Vacation").document(vacationID).set(vacationRequest).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            db.collection("employees").document(currEmployee.getId()).update("totalNumberOfVacationDays", daysAfterVacationIsTaken).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    remainingDays = daysAfterVacationIsTaken;
                                    vVacationDaysLayout.setHelperText(String.format("%d days Remaining", daysAfterVacationIsTaken));
                                    ClearInputs();
                                }
                            });
                        }
                    });

                });
    }

    private void ClearInputs() {
        vVacationDate.setText(null);
        vVacationDays.setText(null);
        vVacationNote.setText(null);
    }

    private boolean ValidateInputs() {
        return !(vVacationNote.getText().toString().isEmpty()
                ||
                vVacationDate.getText().toString().isEmpty()
                ||
                (vVacationDays.getText().toString().isEmpty() || Integer.parseInt(vVacationDays.getText().toString()) == 0)
                ||
                vVacationDaysLayout.getError() != null
        );
    }

    private String convertDateToString(Object selection) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy");
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis((long) selection);
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

        @Override
        public void afterTextChanged(Editable editable) {
            daysAfterVacationIsTaken = vVacationDays.getText().toString().trim().equals("") ? remainingDays : remainingDays - Integer.parseInt(vVacationDays.getText().toString());
            if (daysAfterVacationIsTaken < 0) {
                vVacationDaysLayout.setError("Exceeds remaining");
            } else {
                vVacationDaysLayout.setHelperText(String.format("%d days Remaining", daysAfterVacationIsTaken));
                vVacationDaysLayout.setError(null);
            }

        }
    };
    private View.OnClickListener clSendRequest = v -> {
        if (ValidateInputs()) {
            uploadVacationRequest();

        } else {
            Toast.makeText(getActivity(), "please, fill vacation request data", Toast.LENGTH_SHORT).show();
        }
    };
    private View.OnClickListener clVacationDate = v -> {
        vDatePicker.show(getParentFragmentManager(), "DATE_PICKER");
    };
    private MaterialPickerOnPositiveButtonClickListener pclDatePicker = selection -> {
        vVacationDate.setText(convertDateToString(selection));
        startDate = (long) selection;
    };

}