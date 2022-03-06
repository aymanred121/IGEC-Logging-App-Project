package com.example.igecuser.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.igecuser.R;
import com.example.igecuser.fireBase.VacationRequest;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class VacationRequestFragmentDialog extends BottomSheetDialogFragment {


    // Views
    private TextInputEditText vVacationDate, vVacationNote, vVacationDays, vEmployeeName, vEmployeeID;
    private MaterialButton vAcceptRequest, vDeclineRequest;

    //var
    private VacationRequest currVacation;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    public VacationRequestFragmentDialog(VacationRequest currVacation) {
        this.currVacation = currVacation;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_vacation_info, container, false);
        getDialog().getWindow().setLayout(600,300);
        initialize(view);

        vAcceptRequest.setOnClickListener(oclAcceptRequest);
        vDeclineRequest.setOnClickListener(oclDeclineRequest);
        return view;
    }

    // Functions
    private void initialize(View view) {
        vEmployeeID = view.findViewById(R.id.TextInput_EmployeeId);
        vVacationDate = view.findViewById(R.id.TextInput_VacationDate);
        vVacationNote = view.findViewById(R.id.TextInput_VacationNote);
        vEmployeeName = view.findViewById(R.id.TextInput_EmployeeName);
        vVacationDays = view.findViewById(R.id.TextInput_VacationDays);
        vAcceptRequest = view.findViewById(R.id.Button_AcceptRequest);
        vDeclineRequest = view.findViewById(R.id.Button_DeclineRequest);
        vEmployeeID.setText(currVacation.getEmployee().getId());
        vVacationDate.setText(dateToString(currVacation.getStartDate().getTime()));
        vVacationNote.setText(currVacation.getVacationNote());
        vVacationDays.setText(getDays(currVacation));
        vEmployeeName.setText(currVacation.getEmployee().getFirstName() + " " + currVacation.getEmployee().getLastName());


    }

    private String getDays(VacationRequest vacation) {
        long days = vacation.getEndDate().getTime() - vacation.getStartDate().getTime();
        days /= (24 * 3600 * 1000);
        return String.valueOf(days);
    }
    private String dateToString(long selection)
    {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy");
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(selection);
        return simpleDateFormat.format(calendar.getTime());
    }
    // Listeners
    private View.OnClickListener oclAcceptRequest = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            db.collection("Vacation")
                    .document(currVacation.getId())
                    .update("vacationStatus", 1).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void unused) {
                    getDialog().dismiss();
                }
            });

        }
    };
    private View.OnClickListener oclDeclineRequest = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            db.collection("Vacation")
                    .document(currVacation.getId())
                    .update("vacationStatus", -1).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void unused) {
                    getDialog().dismiss();
                }
            });
        }
    };
}
