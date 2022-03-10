package com.example.igecuser.Fragments;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.igecuser.R;
import com.example.igecuser.fireBase.Employee;
import com.example.igecuser.fireBase.VacationRequest;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class VacationRequestFragment extends Fragment {

    // Views
    private TextInputEditText vVacationDate, vVacationNote, vVacationDays;
    private TextInputLayout vVacationDateLayout;
    private MaterialButton vSendRequest;
    private MaterialDatePicker.Builder<Long> vDatePickerBuilder = MaterialDatePicker.Builder.datePicker();
    private MaterialDatePicker vDatePicker;

    // Vars
    private long duration, startDateMileSecond;
    private Employee currEmployee;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private VacationRequest vacationRequest;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_vacation_request, container, false);
        Initialize(view);

        vVacationDateLayout.setEndIconOnClickListener(clVacationDate);
        vDatePicker.addOnPositiveButtonClickListener(pclDatePicker);
        vSendRequest.setOnClickListener(clSendRequest);

        // Inflate the layout for this fragment
        return view;
    }

    // Functions
    private void Initialize(View view) {
        vVacationDate = view.findViewById(R.id.TextInput_VacationDate);
        vVacationNote = view.findViewById(R.id.TextInput_VacationNote);
        vVacationDays = view.findViewById(R.id.TextInput_VacationDays);
        vVacationDateLayout = view.findViewById(R.id.textInputLayout_VacationDate);
        vSendRequest = view.findViewById(R.id.Button_SendRequest);
        vDatePickerBuilder.setTitleText("Vacation Date");
        vDatePicker = vDatePickerBuilder.build();
        currEmployee = (Employee) getArguments().getSerializable("emp");
    }

    private void uploadVacationRequest() {
        String vacationID = db.collection("Vacation").document().getId().substring(0, 5);
        db.collection("employees")
                .document(currEmployee.getManagerID())
                .addSnapshotListener((value, error) -> {
                    Date startDate = convertStringDate(vVacationDate.getText().toString());
                    duration = ((long) Integer.parseInt(vVacationDays.getText().toString()) * 24 * 3600 * 1000) + startDateMileSecond;
                    Date endDate = convertStringDate(convertLongDate(duration));
                    vacationRequest = new VacationRequest(
                            startDate,
                            endDate,
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
        return !(vVacationNote.getText().toString().isEmpty()
                        ||
                vVacationDate.getText().toString().isEmpty()
                        ||
                vVacationDays.getText().toString().isEmpty());
    }

    private String convertLongDate(Object selection) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy");
        Calendar calendar = Calendar.getInstance();
        startDateMileSecond = (long) selection;
        calendar.setTimeInMillis((long) selection);
        return simpleDateFormat.format(calendar.getTime());
    }
    private Date convertStringDate(String sDate) {
        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
        try {
            Date date = format.parse(sDate);
            return date;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    // Listeners
    private View.OnClickListener clSendRequest = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(ValidateInputs()) {
                uploadVacationRequest();

            }
            else
            {
                Toast.makeText(getActivity(), "please, fill vacation request data", Toast.LENGTH_SHORT).show();
            }
        }


    };
    private View.OnClickListener clVacationDate = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (!vDatePicker.isVisible())
                vDatePicker.show(getParentFragmentManager(), "DATE_PICKER");
        }
    };
    private MaterialPickerOnPositiveButtonClickListener pclDatePicker = new MaterialPickerOnPositiveButtonClickListener() {
        @Override
        public void onPositiveButtonClick(Object selection) {

            vVacationDate.setText(convertLongDate(selection));
        }

    };

}