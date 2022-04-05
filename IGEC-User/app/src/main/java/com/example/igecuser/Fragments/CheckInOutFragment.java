package com.example.igecuser.Fragments;

import static android.content.Context.LOCATION_SERVICE;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentResultListener;

import com.example.igecuser.Dialogs.MachineCheckInOutDialog;
import com.example.igecuser.Dialogs.SupplementsDialog;
import com.example.igecuser.R;
import com.example.igecuser.fireBase.Employee;
import com.example.igecuser.fireBase.Machine;
import com.example.igecuser.fireBase.Machine_Employee;
import com.example.igecuser.fireBase.Summary;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
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
    private Boolean isHere = Boolean.FALSE;
    private Employee currEmployee = null;
    private String id;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private double latitude, longitude;
    private Machine currMachine;
    private String machineEmpId;
    private FusedLocationProviderClient fusedLocationClient;
    private final CollectionReference machineEmployee = db.collection("Machine_Employee");
    private final CollectionReference machineCol = db.collection("machine");

    public CheckInOutFragment(Employee currEmployee) {
        this.currEmployee = currEmployee;
    }


    private final View.OnClickListener oclMachine = view -> {
        animationFab();
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_check_in_out, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initialize(view);

        // Listeners
        vCheckInOut.setOnClickListener(oclCheckInOut);
        vAddMachine.setOnClickListener(oclMachine);
        vAddMachineInside.setOnClickListener(oclInside);
        vAddMachineOutside.setOnClickListener(oclOutside);
    }

    private void initialize(View view) {
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
        vCheckInOut.setBackgroundColor((isHere) ? Color.rgb(153, 0, 0) : Color.rgb(0, 153, 0));
        vCheckInOut.setText(isHere ? "Out" : "In");
        vAddMachine.setClickable(isHere);
        vAddMachine.startAnimation(isHere ? show : hide);

        vGreeting.setText(String.format("%s\n%s", getString(R.string.good_morning), currEmployee.getFirstName()));
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());
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
    @SuppressLint("MissingPermission")
    private final View.OnClickListener oclCheckInOut = v -> {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(getActivity());
        builder.setTitle(getString(R.string.do_you_want_to_confirm_this_action))
                .setNegativeButton(getString(R.string.No), (dialogInterface, i) -> {
                })
                .setPositiveButton(getString(R.string.Yes), (dialogInterface, i) -> {

                    fusedLocationClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
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
                                    employeeCheckIn(summary);
                                } else {

                                    Summary summary1 = documentSnapshot.toObject(Summary.class);
                                    if (summary1.getWorkedTime() == null) {
                                      employeeCheckOut(summary1,checkOut);
                                    } else {
                                        Toast.makeText(getActivity(), "You've been checked Out already!", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                            isHere = !isHere;
                            vCheckInOut.setBackgroundColor((isHere) ? Color.rgb(153, 0, 0) : Color.rgb(0, 153, 0));
                            vCheckInOut.setText(isHere ? "Out" : "In");
                            vAddMachine.setClickable(isHere);
                            if (isOpen) {
                                vAddMachine.startAnimation(rotateBackwardHide);
                                vAddMachineInside.startAnimation(fabClose);
                                vAddMachineOutside.startAnimation(fabClose);
                                vInsideText.startAnimation(hide);
                                vOutsideText.startAnimation(hide);
                                vAddMachineInside.setClickable(false);
                                vAddMachineOutside.setClickable(false);
                                isOpen = false;
                            } else {
                                vAddMachine.startAnimation(isHere ? show : hide);
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(getContext(), "Please enable GPS!", Toast.LENGTH_SHORT).show();

                        }
                    });
                })
                .show();

    };

    private void employeeCheckOut(Summary summary1, HashMap<String, Object> checkOut) {
        db.collection("summary").document(id).update(checkOut)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(getContext(), "Checked Out successfully!", Toast.LENGTH_SHORT).show();
                    long checkInTime = ((Timestamp) summary1.getCheckIn().get("Time")).getSeconds();
                    long checkOutTime = Timestamp.now().getSeconds();
                    long workingTime = (checkOutTime - checkInTime);
                    summary1.setWorkedTime(workingTime);
                    db.collection("summary").document(id).set(summary1);
                    db.collection("projects").document(currEmployee.getProjectID())
                            .update("employeeWorkedTime." + currEmployee.getId(), FieldValue.increment(workingTime));

                });
    }

    private void employeeCheckIn(Summary summary) {
        HashMap<String, Object> checkInDetails = new HashMap<>(summary.getGeoMap());
        checkInDetails.put("Time", Timestamp.now());
        HashMap<String, Object> checkIn = new HashMap<>();
        checkIn.put("checkIn", checkInDetails);
        checkIn.put("employee", currEmployee);
        db.collection("summary").document(id).set(checkIn);
        Toast.makeText(getContext(), "Checked In successfully!", Toast.LENGTH_SHORT).show();
    }

    private final View.OnClickListener oclInside = view -> {
        MachineCheckInOutDialog machineCheckInOutDialog = new MachineCheckInOutDialog(true);
        machineCheckInOutDialog.show(getParentFragmentManager(), "");
    };
    private final View.OnClickListener oclOutside = view -> {
        MachineCheckInOutDialog machineCheckInOutDialog = new MachineCheckInOutDialog(false);
        machineCheckInOutDialog.show(getParentFragmentManager(), "");
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getParentFragmentManager().setFragmentResultListener("machine", this, new FragmentResultListener() {
            @SuppressLint("MissingPermission")
            @Override
            public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle bundle) {
                // We use a String here, but any type that can be put in a Bundle is supported
                String machineID = bundle.getString("machineID");
                boolean isItAUser = bundle.getBoolean("isItAUser");
                Toast.makeText(getActivity(), machineID, Toast.LENGTH_SHORT).show();
                // Do something with the result
                    fusedLocationClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            try {
                            longitude = location.getLongitude();
                            latitude = location.getLatitude();

                            machineCol.document(machineID).get().addOnSuccessListener((value) -> {
                                if (!value.exists()) {
                                    Toast.makeText(getActivity(), "Invalid Machine ID", Toast.LENGTH_SHORT).show();
                                    return;
                                }
                                currMachine = value.toObject(Machine.class);

                                if (currMachine.getUsed() && !currMachine.getEmployeeId().equals(currEmployee.getId())) {
                                    Toast.makeText(getContext(), "this Machine already being used by" + currMachine.getEmployeeFirstName(), Toast.LENGTH_SHORT).show();
                                    return;
                                }
                                machineEmpId = !currMachine.getEmployeeId().equals(currEmployee.getId()) ? machineEmployee.document().getId() : currMachine.getMachineEmployeeID();

                                machineEmployee.document(machineEmpId).get().addOnSuccessListener(documentSnapshot -> {
                                    if (!documentSnapshot.exists()) {
                                        checkInMachine();
                                    } else {
                                        Machine_Employee currMachineEmployee = documentSnapshot.toObject(Machine_Employee.class);
                                        checkOutMachine(currMachineEmployee);
                                    }

                                });
                            });
                        }  catch (Exception e) {
                                Toast.makeText(getContext(), "invalid Machine ID", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }).addOnFailureListener(unused ->{
                        Toast.makeText(getContext(), "Please enable GPS!", Toast.LENGTH_SHORT).show();
                    });



                // TODO: put it back to onSuccess
                SupplementsDialog supplementsDialog = new SupplementsDialog(isItAUser);
                supplementsDialog.show(getParentFragmentManager(), "");
            }
        });
        getParentFragmentManager().setFragmentResultListener("supplements", this, new FragmentResultListener() {
            @Override
            public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle bundle) {
                String note = bundle.getString("supplementState");
                Toast.makeText(getActivity(), note, Toast.LENGTH_SHORT).show();
            }
        });


    }

    private void checkOutMachine(Machine_Employee currMachineEmployee) {
        HashMap<String, Object> checkOutDetails = new HashMap<>((new Summary(latitude, longitude)).getGeoMap());
        checkOutDetails.put("Time", Timestamp.now());
        long checkInTime = ((Timestamp) currMachineEmployee.getCheckIn().get("Time")).getSeconds();
        long checkOutTime = Timestamp.now().getSeconds();
        long workingTime = (checkOutTime - checkInTime);
        currMachineEmployee.setWorkedTime(workingTime);
        currMachineEmployee.setCheckOut(checkOutDetails);
        machineEmployee.document(machineEmpId).set(currMachineEmployee)
                .addOnSuccessListener(unused -> {
                    db.collection("Machine_Employee").document(currMachine.getMachineEmployeeID()).set(currMachineEmployee).addOnSuccessListener(unused1 -> {
                        currMachine.removeEmployeeDependency();
                        machineCol.document(currMachine.getId()).update("isUsed", false, "employeeFirstName", "", "employeeId", "", "machineEmployeeID", "").addOnSuccessListener(vu -> {

                            Toast.makeText(getContext(), "Machine: " + currMachine.getReference() + " checked Out successfully", Toast.LENGTH_SHORT).show();

                        });
                    });
                });
    }

    private void checkInMachine() {
        HashMap<String, Object> checkInDetails = new HashMap<>((new Summary(latitude, longitude)).getGeoMap());
        checkInDetails.put("Time", Timestamp.now());
        Map<String, Object> machineEmployee1 = new HashMap();
        machineEmployee1.put("machine", currMachine);
        machineEmployee1.put("employee", currEmployee);
        machineEmployee1.put("checkIn", checkInDetails);
        currMachine.setUsed(true);
        currMachine.setEmployeeFirstName(currEmployee.getFirstName());
        currMachine.setMachineEmployeeID(machineEmpId);
        //NOTE don't use set()
        machineCol.document(currMachine.getId()).update("isUsed", true, "employeeFirstName", currEmployee.getFirstName(), "employeeId", currEmployee.getId(), "machineEmployeeID", machineEmpId)
                .addOnSuccessListener(unused1 -> {
                    machineEmployee.document(machineEmpId).set(machineEmployee1).addOnSuccessListener(unused -> Toast.makeText(getContext(), "Machine: " + currMachine.getReference() + " checked In successfully", Toast.LENGTH_SHORT).show());
                });

    }


}