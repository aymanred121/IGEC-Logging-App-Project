package com.example.igecuser.Fragments;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.igecuser.MachineAdapter;
import com.example.igecuser.R;
import com.example.igecuser.VacationAdapter;
import com.example.igecuser.fireBase.Employee;
import com.example.igecuser.fireBase.Machine;
import com.example.igecuser.fireBase.Summary;
import com.example.igecuser.qrCameraActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class CheckInOut extends Fragment {

    TextView vGreeting;
    MaterialButton vCheckInOut;
    FloatingActionButton vAddMachine;



    boolean isIn = false;
    String id;
    Employee currEmployee;
    String machineID;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    double latitude,longitude;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_check_in_out, container, false);
        Initialize(view);

        vCheckInOut.setOnClickListener(oclCheckInOut);
        vAddMachine.setOnClickListener(oclMachine);
        return view;
    }

    private void Initialize(View view) {
        vGreeting = view.findViewById(R.id.TextView_Greeting);
        vCheckInOut = view.findViewById(R.id.Button_CheckInOut);
        vAddMachine = view.findViewById(R.id.Button_AddMachine);
        currEmployee = (Employee) getArguments().getSerializable("emp");
        id=db.collection("summary").document().getId();
    }

    View.OnClickListener oclCheckInOut = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            LocationManager lm = (LocationManager) getContext().getSystemService(Context.LOCATION_SERVICE);
            if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
             longitude = location.getLongitude();
             latitude = location.getLatitude();
            Summary summary = new Summary(latitude,longitude);
            Map<String,Object> checkInOut = new HashMap<>();
            if (!isIn) {
                checkInOut.put("Employee",currEmployee);
                checkInOut.put("Check In",summary);
                db.collection("summary").document(id).update(checkInOut).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        db.collection("summary").document(id).set(checkInOut);
                    }
                });

            }
            else{
                checkInOut.put("Employee",currEmployee);
                checkInOut.put("Check Out",summary);
                db.collection("summary").document(id).update(checkInOut).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getActivity(), "loooooooooool", Toast.LENGTH_SHORT).show();
                    }
                });
            }
            isIn = !isIn;
            vCheckInOut.setBackgroundColor((isIn) ? Color.rgb(153, 0, 0) : Color.rgb(0, 153, 0));
            vCheckInOut.setText(isIn ? "Out" : "In");

        }
    };
    MachineAdapter.OnItemClickListener iclMachine = new MachineAdapter.OnItemClickListener() {
        @Override
        public void onItemClick(int position) {

        }

        @Override
        public void onCheckInOutClick(int position) {

        }
    };
    View.OnClickListener oclMachine = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Intent intent = new Intent(getActivity(), qrCameraActivity.class);
            startActivityForResult(intent,55);

        }
    };
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode) {
            case (55) : {
                if (resultCode == Activity.RESULT_OK) {
                    machineID = data.getStringExtra("qrCamera");
                    Toast.makeText(getContext(), machineID, Toast.LENGTH_SHORT).show();
                    db.collection("machine").document(machineID).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            Machine currMachine = documentSnapshot.toObject(Machine.class);
                            Summary machineCheckIn = new Summary(latitude,longitude);
                            Map<String,Object>machineEmployee = new HashMap();
                            machineEmployee.put("Machine",currMachine);
                            machineEmployee.put("Employee",currEmployee);
                            machineEmployee.put("check In",machineCheckIn);
                            db.collection("Machine_Employee").add(machineEmployee);
                            //TODO handle check out
                        }
                    });
                }
                break;
            }
        }
    }
}