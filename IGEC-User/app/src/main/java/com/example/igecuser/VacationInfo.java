package com.example.igecuser;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.igecuser.Fragments.VacationsLog;
import com.example.igecuser.R;
import com.example.igecuser.fireBase.VacationRequest;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Locale;

public class VacationInfo extends AppCompatActivity {

    // Views
    private TextInputEditText vVacationDate, vVacationNote, vVacationDays,vEmployeeName;
    private MaterialButton vAcceptRequest, vDeclineRequest;

    //var
    private VacationRequest currVacation;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();


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
        vEmployeeName = findViewById(R.id.TextInput_EmployeeName);
        vVacationDays = findViewById(R.id.TextInput_VacationDays);
        vAcceptRequest = findViewById(R.id.Button_AcceptRequest);
        vDeclineRequest = findViewById(R.id.Button_DeclineRequest);
        currVacation = (VacationRequest) getIntent().getSerializableExtra("request");
        vVacationDate.setText(currVacation.getStartDate().toString());
        vVacationNote.setText(currVacation.getVacationNote());
        vVacationDays.setText(getDays(currVacation));
        vEmployeeName.setText(currVacation.getEmployee().getFirstName()+" "+currVacation.getEmployee().getLastName());



    }
    private String getDays(VacationRequest vacation) {
        long days= vacation.getEndDate().getTime()-vacation.getStartDate().getTime();
        days /=(24*3600*1000);
        return String.valueOf(days);
    }
    // Listeners
    private View.OnClickListener clAcceptRequest = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            db.collection("Vacation")
                    .document(currVacation.getId())
                    .update("vacationStatus",1).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void unused) {
                    finish();
                }
            });

        }
    };
    private View.OnClickListener clDeclineRequest = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            db.collection("Vacation")
                    .document(currVacation.getId())
                    .update("vacationStatus",-1).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void unused) {
                    finish();
                }
            });
        }
    };

    @Override
    protected void onStop() {
        super.onStop();
        VacationsLog.loadVacations();
    }
}