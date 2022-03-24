package com.example.igecuser.Fragments;

import static android.content.Context.LOCATION_SERVICE;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentResultListener;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import android.widget.Toast;

import com.example.igecuser.Dialogs.CheckingInOutDialog;
import com.example.igecuser.Dialogs.ClientInfoFragmentDialog;
import com.example.igecuser.Dialogs.MachineCheckInOutFragmentDialog;
import com.example.igecuser.R;
import com.example.igecuser.fireBase.Employee;
import com.example.igecuser.fireBase.Machine;
import com.example.igecuser.fireBase.Machine_Employee;
import com.example.igecuser.fireBase.Summary;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CheckInOutFragment extends Fragment {

    private MaterialButton vCheckInOut;
    private FloatingActionButton vAddMachine, vAddMachineInside, vAddMachineOutside;
    private TextView vInsideText, vOutsideText;
    // Vars
    private boolean isOpen = false;
    private Animation fabClose, fabOpen, rotateForward, rotateBackward, show, hide, rotateBackwardHide;
    private boolean isIn = false;
    private final Employee currEmployee;
    private String id;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private double latitude, longitude;
    private Machine currMachine;
    private View view;

    public CheckInOutFragment(Employee currEmployee) {
        this.currEmployee = currEmployee;
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getParentFragmentManager().setFragmentResultListener("employee", this, new FragmentResultListener() {
            @Override
            public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle bundle) {
                // We use a String here, but any type that can be put in a Bundle is supported
                String result = bundle.getString("response");
                // Do something with the result
                if (result.equals("Yes")) {
                    Location location = getLocation();
                    if (location == null) {
                        Toast.makeText(getContext(), "Please enable GPS!", Toast.LENGTH_SHORT).show();
                    } else {
                        longitude = location.getLongitude();
                        latitude = location.getLatitude();

                        HashMap<String, Object> checkOut = new HashMap<>();
                        Summary summary = new Summary(latitude, longitude);
                        HashMap<String, Object> checkOutDetails = new HashMap<>(summary.getGeoMap());
                        checkOutDetails.put("Time", Timestamp.now());
                        checkOut.put("checkOut", checkOutDetails);
                        checkOut.put("employee", currEmployee);
                        db.collection("summary").document(id).get().addOnSuccessListener(documentSnapshot -> {
                            if (!documentSnapshot.exists()) {
                                HashMap<String, Object> checkInDetails = new HashMap<>(summary.getGeoMap());
                                checkInDetails.put("Time", Timestamp.now());
                                HashMap<String, Object> checkIn = new HashMap<>();
                                checkIn.put("checkIn", checkInDetails);
                                checkIn.put("employee", currEmployee);
                                db.collection("summary").document(id).set(checkIn);
                                Toast.makeText(getContext(), "Checked In successfully!", Toast.LENGTH_SHORT).show();
                            } else {

                                Summary currEmpSummary = documentSnapshot.toObject(Summary.class);
                                if (currEmpSummary.getWorkedTime() == null) {
                                    db.collection("summary").document(id).update(checkOut)
                                            .addOnSuccessListener(unused -> {
                                                Toast.makeText(getContext(), "Checked Out successfully!", Toast.LENGTH_SHORT).show();
                                                long checkInTime = ((Timestamp) currEmpSummary.getCheckIn().get("Time")).getSeconds();
                                                long checkOutTime = Timestamp.now().getSeconds();
                                                long workingTime = (checkOutTime - checkInTime);
                                                currEmpSummary.setWorkedTime(workingTime);
                                                db.collection("summary").document(id).set(currEmpSummary);
                                                db.collection("projects").document(currEmployee.getProjectID())
                                                        .update("employeeWorkedTime." + currEmployee.getId(), FieldValue.increment(workingTime));

                                            });
                                } else {
                                    Toast.makeText(getActivity(), "You've been checked Out already!", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                        isIn = !isIn;
                        vCheckInOut.setBackgroundColor((isIn) ? Color.rgb(153, 0, 0) : Color.rgb(0, 153, 0));
                        vCheckInOut.setText(isIn ? "Out" : "In");
                        vAddMachine.setClickable(isIn);
                        if (isOpen) {
                            Toast.makeText(getActivity(), "closed", Toast.LENGTH_SHORT).show();
                            vAddMachine.startAnimation(rotateBackwardHide);
                            vAddMachineInside.startAnimation(fabClose);
                            vAddMachineOutside.startAnimation(fabClose);
                            vInsideText.startAnimation(hide);
                            vOutsideText.startAnimation(hide);
                            vAddMachineInside.setClickable(false);
                            vAddMachineOutside.setClickable(false);
                            isOpen = false;
                        } else {
                            vAddMachine.startAnimation(isIn ? show : hide);
                        }

                    }
                }
            }
        });

        getParentFragmentManager().setFragmentResultListener("machine", this, new FragmentResultListener() {
            @Override
            public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle bundle) {
                // We use a String here, but any type that can be put in a Bundle is supported
                String result = bundle.getString("response");
                Toast.makeText(getActivity(), result, Toast.LENGTH_SHORT).show();
                // Do something with the result
                String machineID = result;
                try {
                    Location location = getLocation();
                    if (location == null) {
                        Toast.makeText(getContext(), "Please enable GPS!", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    longitude = location.getLongitude();
                    latitude = location.getLatitude();
                    db.collection("machine").document(machineID).addSnapshotListener((value, error) -> {
                        currMachine = value.toObject(Machine.class);
                        String machineEmpId = id + machineID;
                        db.collection("Machine_Employee").document(machineEmpId).get().addOnSuccessListener(documentSnapshot -> {
                            if (!documentSnapshot.exists()) {
                                HashMap<String, Object> checkInDetails = new HashMap<>((new Summary(latitude, longitude)).getGeoMap());
                                checkInDetails.put("Time", Timestamp.now());
                                Map<String, Object> machineEmployee1 = new HashMap();
                                machineEmployee1.put("machine", currMachine);
                                machineEmployee1.put("employee", currEmployee);
                                machineEmployee1.put("checkIn", checkInDetails);
                                db.collection("Machine_Employee").document(machineEmpId).set(machineEmployee1);
                                Toast.makeText(getContext(), "Machine: " + currMachine.getCodeName() + " checked In successfully", Toast.LENGTH_SHORT).show();

                            } else {
                                Machine_Employee currMachineEmployee = documentSnapshot.toObject(Machine_Employee.class);
                                HashMap<String, Object> checkOutDetails = new HashMap<>((new Summary(latitude, longitude)).getGeoMap());
                                checkOutDetails.put("Time", Timestamp.now());
                                if (currMachineEmployee.getWorkedTime() == null) {
                                    long checkInTime = ((Timestamp) currMachineEmployee.getCheckIn().get("Time")).getSeconds();
                                    long checkOutTime = Timestamp.now().getSeconds();
                                    long workingTime = (checkOutTime - checkInTime);
                                    currMachineEmployee.setWorkedTime(workingTime);
                                    currMachineEmployee.setCheckOut(checkOutDetails);
                                    db.collection("Machine_Employee").document(machineEmpId).set(currMachineEmployee)
                                            .addOnSuccessListener(unused -> Toast.makeText(getContext(), "Machine: " + currMachine.getCodeName() + " checked Out successfully", Toast.LENGTH_SHORT).show());

                                } else {
                                    Toast.makeText(getActivity(), "this Machine Already checked Out", Toast.LENGTH_SHORT).show();
                                }

                            }
                        });
                    });

                } catch (Exception e) {
                    Toast.makeText(getContext(), "invalid Machine ID", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_check_in_out, container, false);
        initialize();


        // Listeners
        vCheckInOut.setOnClickListener(oclCheckInOut);
        vAddMachine.setOnClickListener(oclMachine);
        vAddMachineInside.setOnClickListener(oclInside);
        vAddMachineOutside.setOnClickListener(oclOutside);
        return view;
    }

    private void initialize() {
        //Views
        TextView vGreeting = view.findViewById(R.id.TextView_Greeting);
        vCheckInOut = view.findViewById(R.id.Button_CheckInOut);
        vAddMachine = view.findViewById(R.id.Button_AddMachine);
        vAddMachineInside = view.findViewById(R.id.Button_AddMachineInside);
        vAddMachineOutside = view.findViewById(R.id.Button_AddMachineOutside);
        vOutsideText = view.findViewById(R.id.textView_non_user);
        vInsideText = view.findViewById(R.id.textView_user);

        fabClose = AnimationUtils.loadAnimation(getActivity(), R.anim.fab_close);
        fabOpen = AnimationUtils.loadAnimation(getActivity(), R.anim.fab_open);
        rotateForward = AnimationUtils.loadAnimation(getActivity(), R.anim.rotate_forward);
        rotateBackward = AnimationUtils.loadAnimation(getActivity(), R.anim.rotate_backward);
        rotateBackwardHide = AnimationUtils.loadAnimation(getActivity(), R.anim.rotate_backward_hide);
        show = AnimationUtils.loadAnimation(getActivity(), R.anim.show);
        hide = AnimationUtils.loadAnimation(getActivity(), R.anim.hide);
        id = LocalDate.now().toString() + currEmployee.getId();

        vGreeting.setText(String.format("%s\n%s", getString(R.string.good_morning), currEmployee.getFirstName()));
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
        if (requestCode == 55) {
            if (resultCode == Activity.RESULT_OK) {
                String machineID = data.getStringExtra("qrCamera");
                try {
                    Location location = getLocation();
                    if (location == null) {
                        Toast.makeText(getContext(), "Please enable GPS!", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    longitude = location.getLongitude();
                    latitude = location.getLatitude();
                    db.collection("machine").document(machineID).addSnapshotListener((value, error) -> {
                        currMachine = value.toObject(Machine.class);
                        String machineEmpId = id + machineID;
                        db.collection("Machine_Employee").document(machineEmpId).get().addOnSuccessListener(documentSnapshot -> {
                            if (!documentSnapshot.exists()) {
                                HashMap<String, Object> checkInDetails = new HashMap<>((new Summary(latitude, longitude)).getGeoMap());
                                checkInDetails.put("Time", Timestamp.now());
                                Map<String, Object> machineEmployee1 = new HashMap();
                                machineEmployee1.put("machine", currMachine);
                                machineEmployee1.put("employee", currEmployee);
                                machineEmployee1.put("checkIn", checkInDetails);
                                db.collection("Machine_Employee").document(machineEmpId).set(machineEmployee1);
                                Toast.makeText(getContext(), "Machine: " + currMachine.getCodeName() + " checked In successfully", Toast.LENGTH_SHORT).show();

                            } else {
                                Machine_Employee currMachineEmployee = documentSnapshot.toObject(Machine_Employee.class);
                                HashMap<String, Object> checkOutDetails = new HashMap<>((new Summary(latitude, longitude)).getGeoMap());
                                checkOutDetails.put("Time", Timestamp.now());
                                if (currMachineEmployee.getWorkedTime() == null) {
                                    long checkInTime = ((Timestamp) currMachineEmployee.getCheckIn().get("Time")).getSeconds();
                                    long checkOutTime = Timestamp.now().getSeconds();
                                    long workingTime = (checkOutTime - checkInTime);
                                    currMachineEmployee.setWorkedTime(workingTime);
                                    currMachineEmployee.setCheckOut(checkOutDetails);
                                    db.collection("Machine_Employee").document(machineEmpId).set(currMachineEmployee)
                                            .addOnSuccessListener(unused -> Toast.makeText(getContext(), "Machine: " + currMachine.getCodeName() + " checked Out successfully", Toast.LENGTH_SHORT).show());

                                } else {
                                    Toast.makeText(getActivity(), "this Machine Already checked Out", Toast.LENGTH_SHORT).show();
                                }

                            }
                        });
                    });

                } catch (Exception e) {
                    Toast.makeText(getContext(), "invalid Machine ID", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void animationFab() {
        if (isOpen) {
            vAddMachine.startAnimation(rotateBackward);
            vAddMachineInside.startAnimation(fabClose);
            vAddMachineOutside.startAnimation(fabClose);
            vInsideText.startAnimation(hide);
            vOutsideText.startAnimation(hide);
            vAddMachineInside.setClickable(false);
            vAddMachineOutside.setClickable(false);
        } else {

            vAddMachine.startAnimation(rotateForward);
            vAddMachineInside.startAnimation(fabOpen);
            vAddMachineOutside.startAnimation(fabOpen);
            vInsideText.startAnimation(show);
            vOutsideText.startAnimation(show);
            vAddMachineInside.setClickable(true);
            vAddMachineOutside.setClickable(true);
        }
        isOpen = !isOpen;
    }

    // Listeners
    private final View.OnClickListener oclCheckInOut = v -> {
        CheckingInOutDialog checkingInOutDialog = new CheckingInOutDialog();
        checkingInOutDialog.show(getParentFragmentManager(), "");
    };
    private View.OnClickListener oclMachine = view -> {
        animationFab();
    };
    private View.OnClickListener oclInside = view -> {

    };
    private View.OnClickListener oclOutside = view -> {
        ClientInfoFragmentDialog clientInfoFragmentDialog = new ClientInfoFragmentDialog();
        clientInfoFragmentDialog.show(getParentFragmentManager(), "");
    };


}