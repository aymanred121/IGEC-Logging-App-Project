package com.example.igecuser;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;
import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class VacationRequest extends Fragment {

    // Views
    private TextInputEditText vVacationDate, vVacationNote;
    private MaterialButton vSendRequest;
    MaterialDatePicker.Builder<Long> vDatePickerBuilder = MaterialDatePicker.Builder.datePicker();
    MaterialDatePicker vDatePicker;

    // Vars
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_vacation_request, container, false);
        Initialize(view);

        vVacationDate.setOnClickListener(clVacationDate);
        vVacationDate.setOnFocusChangeListener(fclVacationDate);
        vDatePicker.addOnPositiveButtonClickListener(pclDatePicker);
        vSendRequest.setOnClickListener(clSendRequest);

        // Inflate the layout for this fragment
        return view;
    }

    // Functions
    private void Initialize(View view) {
        vVacationDate = view.findViewById(R.id.TextInput_VacationDate);
        vVacationNote = view.findViewById(R.id.TextInput_VacationNote);
        vSendRequest = view.findViewById(R.id.Button_SendRequest);
        vDatePickerBuilder.setTitleText("Vacation Date");
        vDatePicker = vDatePickerBuilder.build();
    }

    // Listeners
    private View.OnClickListener clSendRequest = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

        }
    };
    View.OnClickListener clVacationDate = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (!vDatePicker.isVisible())
                vDatePicker.show(getFragmentManager(), "DATE_PICKER");
        }
    };
    View.OnFocusChangeListener fclVacationDate = new View.OnFocusChangeListener() {
        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            if (hasFocus) {
                vDatePicker.show(getFragmentManager(), "DATE_PICKER");
            }
        }
    };
    MaterialPickerOnPositiveButtonClickListener pclDatePicker = new MaterialPickerOnPositiveButtonClickListener() {
        @Override
        public void onPositiveButtonClick(Object selection) {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy");
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis((long) selection);
            vVacationDate.setText(simpleDateFormat.format(calendar.getTime()));
        }
    };
}