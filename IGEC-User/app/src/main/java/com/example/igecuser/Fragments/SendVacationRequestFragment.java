package com.example.igecuser.Fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.igecuser.R;
import com.example.igecuser.fireBase.Employee;
import com.example.igecuser.fireBase.VacationRequest;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class SendVacationRequestFragment extends Fragment {

    //TODO Add view to show number of available days which is accessed via  -->  currEmployee.getTotalNumberOfVacationDays();
    //Views
    private TextInputEditText vVacationDate, vVacationNote, vVacationDays;
    private TextInputLayout vVacationDateLayout;
    private MaterialButton vSendRequest;
    private MaterialDatePicker.Builder<Long> vDatePickerBuilder = MaterialDatePicker.Builder.datePicker();
    private MaterialDatePicker vDatePicker;
    //Vars
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
        vSendRequest = view.findViewById(R.id.Button_SendRequest);
        vDatePickerBuilder.setTitleText("Vacation Date");
        vDatePicker = vDatePickerBuilder.build();
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
                    db.collection("Vacation").document(vacationID).set(vacationRequest);
                    ClearInputs();
                });
    }

    private void ClearInputs() {
        vVacationDate.setText(null);
        vVacationDays.setText(null);
        vVacationNote.setText(null);
    }

    private boolean ValidateInputs() {
        boolean accepted = true ;
        if(currEmployee.getTotalNumberOfVacationDays() - Integer.parseInt(vVacationDays.getText().toString()) < 0)
        {
            accepted = false;
            Toast.makeText(getActivity(), "No available days", Toast.LENGTH_SHORT).show();
        }
        return !(vVacationNote.getText().toString().isEmpty()
                ||
                vVacationDate.getText().toString().isEmpty()
                ||
                vVacationDays.getText().toString().isEmpty()
                ||
                !accepted
        );
    }

    private String convertDateToString(Object selection) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy");
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis((long) selection);
        return simpleDateFormat.format(calendar.getTime());
    }

    // Listeners
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