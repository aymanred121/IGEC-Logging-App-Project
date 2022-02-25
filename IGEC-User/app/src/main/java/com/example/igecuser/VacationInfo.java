package com.example.igecuser;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;

import com.example.igecuser.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.textfield.TextInputEditText;

public class VacationInfo extends AppCompatActivity {

    // Views
    private TextInputEditText vVacationDate, vVacationNote;
    private MaterialButton vSendRequest;
    private MaterialButton vAcceptRequest, vDeclineRequest;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vacation_info);
        Initialize();

        vAcceptRequest.setOnClickListener(clAcceptRequest);
        vDeclineRequest.setOnClickListener(clDeclineRequest);
    }
    // Functions
    private void Initialize() {
        vVacationDate = findViewById(R.id.TextInput_VacationDate);
        vVacationNote = findViewById(R.id.TextInput_VacationNote);
        vAcceptRequest = findViewById(R.id.Button_AcceptRequest);
        vDeclineRequest = findViewById(R.id.Button_DeclineRequest);
    }

    // Listeners
    private View.OnClickListener clAcceptRequest = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

        }
    };
    private View.OnClickListener clDeclineRequest = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

        }
    };
}