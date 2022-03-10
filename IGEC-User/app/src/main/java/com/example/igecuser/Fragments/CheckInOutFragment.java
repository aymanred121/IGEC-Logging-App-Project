package com.example.igecuser.Fragments;

import static android.content.ContentValues.TAG;
import static android.content.Context.LOCATION_SERVICE;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.igecuser.Adapters.MachineAdapter;
import com.example.igecuser.R;
import com.example.igecuser.fireBase.Employee;
import com.example.igecuser.fireBase.Machine;
import com.example.igecuser.fireBase.Summary;
import com.example.igecuser.Activities.qrCameraActivity;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CheckInOutFragment extends Fragment {

    //Views
    private TextView vGreeting;
    private MaterialButton vCheckInOut;
    private FloatingActionButton vAddMachine;

    // Vars
    private boolean isIn = false;
    private Employee currEmployee;
    private String machineID;
    private String id;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private double latitude, longitude;
    private Machine currMachine;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_check_in_out, container, false);
        initialize(view);


        // Listeners
        vCheckInOut.setOnClickListener(oclCheckInOut);
        vAddMachine.setOnClickListener(oclMachine);
        return view;
    }

    private void initialize(View view) {
        vGreeting = view.findViewById(R.id.TextView_Greeting);
        vCheckInOut = view.findViewById(R.id.Button_CheckInOut);
        vAddMachine = view.findViewById(R.id.Button_AddMachine);
        currEmployee = (Employee) getArguments().getSerializable("emp");
        id = LocalDate.now().toString()+currEmployee.getId();
    }
    private Location getLocation() {
        LocationManager lm = (LocationManager) getContext().getSystemService(LOCATION_SERVICE);
        List<String> providers = lm.getProviders(true);
        Location bestLocation = null;
        for (String provider : providers) {
            if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return null;
            }
            Location l = lm.getLastKnownLocation(provider);
            if (l == null) {
                continue;
            }
            if (bestLocation == null || l.getAccuracy() < bestLocation.getAccuracy()) {
                bestLocation = l;
            }
        }
        return bestLocation;

    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode) {
            case (55) : {
                if (resultCode == Activity.RESULT_OK) {
                    machineID = data.getStringExtra("qrCamera");
                    try{
                        Location location = getLocation();
                        if(location ==null){
                            Toast.makeText(getContext(), "Please enable GPS!", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        longitude = location.getLongitude();
                        latitude = location.getLatitude();

                        db.collection("machine").document(machineID).addSnapshotListener((value, error) -> {
                            currMachine = value.toObject(Machine.class);
                            Summary machineCheckOut = new Summary(latitude,longitude);
                            Map<String,Object>machineEmployee = new HashMap();
                            machineEmployee.put("Machine",currMachine);
                            machineEmployee.put("Employee",currEmployee);
                            machineEmployee.put("check Out",machineCheckOut);
                            db.collection("Machine_Employee").document( LocalDate.now().toString() +currEmployee.getId()+currMachine.getId()).update(machineEmployee)
                                    .addOnSuccessListener(unused -> Toast.makeText(getContext(), "Machine: "+currMachine.getCodeName()+" checked In successfully", Toast.LENGTH_SHORT).show())
                                    .addOnFailureListener(e -> {
                                        Summary machineCheckIn = new Summary(latitude,longitude);
                                        Map<String,Object> machineEmployee1 = new HashMap();
                                        machineEmployee1.put("Machine",currMachine);
                                        machineEmployee1.put("Employee",currEmployee);
                                        machineEmployee1.put("check In",machineCheckIn);
                                        db.collection("Machine_Employee").document( LocalDate.now().toString() +currEmployee.getId()+currMachine.getId()).set(machineEmployee1);
                                        Toast.makeText(getContext(), "Machine: "+currMachine.getCodeName()+" checked Out successfully", Toast.LENGTH_SHORT).show();
                                    });
                        });

                    }catch (Exception e){
                        Toast.makeText(getContext(), "invalid Machine ID", Toast.LENGTH_SHORT).show();
                    }
                }
                break;
            }

        }
    }



    // Listeners
    private View.OnClickListener oclCheckInOut = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Location location = getLocation();
            if(location ==null){
                Toast.makeText(getContext(), "Please enable GPS!", Toast.LENGTH_SHORT).show();
                return;
            }
            longitude = location.getLongitude();
            latitude = location.getLatitude();

            Map<String, Object> checkOut = new HashMap<>();
                checkOut.put("Employee", currEmployee);
                checkOut.put("Check Out",new Summary(latitude, longitude));
                db.collection("summary").document(id).update(checkOut)
                        .addOnSuccessListener(unused -> {
                            Toast.makeText(getContext(), "Checked Out successfully!", Toast.LENGTH_SHORT).show();
                        })
                        .addOnFailureListener(e -> {
                            Map<String, Object> checkIn = new HashMap<>();
                            checkIn.put("Employee", currEmployee);
                            checkIn.put("Check In", new Summary(latitude, longitude));
                            db.collection("summary").document(id).set(checkIn);
                            Toast.makeText(getContext(), "Checked In successfully!", Toast.LENGTH_SHORT).show();
                        });
            isIn = !isIn;
            vCheckInOut.setBackgroundColor((isIn) ? Color.rgb(153, 0, 0) : Color.rgb(0, 153, 0));
            vCheckInOut.setText(isIn ? "Out" : "In");
            vAddMachine.setVisibility(isIn? View.VISIBLE:View.GONE);

        }


    };
    private MachineAdapter.OnItemClickListener iclMachine = new MachineAdapter.OnItemClickListener() {
        @Override
        public void onItemClick(int position) {

        }

        @Override
        public void onCheckInOutClick(int position) {

        }
    };
    private View.OnClickListener oclMachine = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Intent intent = new Intent(getActivity(), qrCameraActivity.class);
            startActivityForResult(intent,55);

        }
    };


}