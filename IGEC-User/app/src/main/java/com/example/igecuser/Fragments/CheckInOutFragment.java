package com.example.igecuser.Fragments;

import android.Manifest;
import android.annotation.SuppressLint;
import android.graphics.Color;
import android.location.Location;
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
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentResultListener;

import com.example.igecuser.Dialogs.ClientInfoDialog;
import com.example.igecuser.Dialogs.MachineCheckInOutDialog;
import com.example.igecuser.Dialogs.SupplementsDialog;
import com.example.igecuser.R;
import com.example.igecuser.fireBase.Allowance;
import com.example.igecuser.fireBase.Client;
import com.example.igecuser.fireBase.Employee;
import com.example.igecuser.fireBase.EmployeesGrossSalary;
import com.example.igecuser.fireBase.Machine;
import com.example.igecuser.fireBase.MachineDefectsLog;
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
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;

public class CheckInOutFragment extends Fragment implements EasyPermissions.PermissionCallbacks {

    private MaterialButton vCheckInOut;
    private FloatingActionButton vAddMachine, vAddMachineInside, vAddMachineOutside;
    private TextView vInsideText, vOutsideText;
    // Vars
    private boolean isOpen = false;
    private final int CAMERA_REQUEST_CODE = 123;
    private final int LOCATION_REQUEST_CODE = 155;
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
    @SuppressLint("SimpleDateFormat") private final  SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");


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
        getLocationPermissions();
        getCameraPermission();
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
        id = currEmployee.getId();
        setCheckInOutBtn();


        vGreeting.setText(String.format("%s\n%s", getString(R.string.good_morning), currEmployee.getFirstName()));
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());
    }

    private void setCheckInOutBtn() {
        @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
        String currentDateAndTime = sdf.format(new Date());
        String day = currentDateAndTime.substring(0,2);
        String month = currentDateAndTime.substring(3,5);
        String year = currentDateAndTime.substring(6,10);
        db.collection("summary").document(id).collection(year+"-"+month).document(day).get().addOnSuccessListener((value) -> {
            if (!value.exists())
                isHere = false;
            else {
                isHere = value.getData().get("checkOut") == null;
            }
            vCheckInOut.setBackgroundColor((isHere) ? Color.rgb(153, 0, 0) : Color.rgb(0, 153, 0));
            vCheckInOut.setText(isHere ? "Out" : "In");
            vAddMachine.setClickable(isHere);
            if (isHere)
                vAddMachine.startAnimation(show);
        });
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

    @AfterPermissionGranted(LOCATION_REQUEST_CODE)
    private boolean getLocationPermissions() {
        String[] perms = {Manifest.permission.ACCESS_FINE_LOCATION};

        if (EasyPermissions.hasPermissions(getContext(), perms)) {
            return true;
        } else {
            EasyPermissions.requestPermissions(
                    this,
                    "We need location permissions in order to the app to functional correctly",
                    LOCATION_REQUEST_CODE,
                    Manifest.permission.ACCESS_FINE_LOCATION);
            return false;
        }
    }

    @AfterPermissionGranted(CAMERA_REQUEST_CODE)
    private boolean getCameraPermission() {
        String[] perms = {Manifest.permission.CAMERA};
        if (EasyPermissions.hasPermissions(getContext(), perms)) {
            return true;
        } else {
            EasyPermissions.requestPermissions(this, "We need camera permission in order to be able to scan the qr code",
                    CAMERA_REQUEST_CODE, perms);
            return false;
        }
    }

    // Listeners
    @SuppressLint("MissingPermission")
    private final View.OnClickListener oclCheckInOut = v -> {
        if (getLocationPermissions() && getCameraPermission()) {
            MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(getActivity());
            builder.setTitle(getString(R.string.do_you_want_to_confirm_this_action))
                    .setNegativeButton(getString(R.string.No), (dialogInterface, i) -> {
                    })
                    .setPositiveButton(getString(R.string.Yes), (dialogInterface, i) -> {

                        fusedLocationClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
                            @Override
                            public void onSuccess(Location location) {
                                @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
                                String currentDateAndTime = sdf.format(new Date());
                                String day = currentDateAndTime.substring(0,2);
                                String month = currentDateAndTime.substring(3,5);
                                String year = currentDateAndTime.substring(6,10);

                                longitude = location.getLongitude();
                                latitude = location.getLatitude();

                                Summary summary = new Summary(latitude, longitude);
                                HashMap<String, Object> checkOutDetails = new HashMap<>(summary.getGeoMap());
                                checkOutDetails.put("Time", Timestamp.now());
                                db.collection("summary").document(id).collection(year+"-"+month).document(day).get().addOnSuccessListener(documentSnapshot -> {
                                    if (!documentSnapshot.exists()) {
                                        employeeCheckIn(summary);
                                    } else {
                                        Summary summary1 = documentSnapshot.toObject(Summary.class);
                                        if (summary1.getCheckOut() == null) {
                                            employeeCheckOut(summary1, checkOutDetails);
                                        } else {
                                            summary1.setLastCheckInTime(Timestamp.now());
                                            db.collection("summary").document(id).collection(year+"-"+month).document(day).update("lastCheckInTime", summary1.getLastCheckInTime(), "checkOut", null);
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
        }


    };

    private void employeeCheckOut(Summary summary1, HashMap<String, Object> checkOut) {
        //get current year and month from date
        @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
        String currentDateAndTime = sdf.format(new Date());
        String day = currentDateAndTime.substring(0,2);
        String month = currentDateAndTime.substring(3,5);
        String year = currentDateAndTime.substring(6,10);

        long checkInTime = (summary1.getLastCheckInTime()).getSeconds();
        long checkOutTime = Timestamp.now().getSeconds();
        long workingTime = (checkOutTime - checkInTime);
        summary1.setCheckOut(checkOut);
        summary1.setWorkedTime(FieldValue.increment(workingTime));
        db.collection("summary").document(id).collection(year+"-"+month).document(day).update("checkOut", checkOut, "workingTime", FieldValue.increment(workingTime))
                .addOnSuccessListener(unused -> {
                    Toast.makeText(getContext(), "Checked Out successfully!", Toast.LENGTH_SHORT).show();
                    db.collection("projects").document(currEmployee.getProjectID())
                            .update("employeeWorkedTime." + currEmployee.getId(), FieldValue.increment(workingTime));

                });
    }

    private void employeeCheckIn(Summary summary) {
        //get current year and month from date
        String currentDateAndTime = sdf.format(new Date());
        String day = currentDateAndTime.substring(0,2);
        String month = currentDateAndTime.substring(3,5);
        String year = currentDateAndTime.substring(6,10);

        summary.setLastCheckInTime(Timestamp.now());
        HashMap<String, Object> checkInDetails = new HashMap<>(summary.getGeoMap());
        checkInDetails.put("Time", Timestamp.now());
        HashMap<String, Object> checkIn = new HashMap<>();
        checkIn.put("checkIn", checkInDetails);
        checkIn.put("projectId", currEmployee.getProjectID());
        checkIn.put("lastCheckInTime", summary.getLastCheckInTime());
        db.collection("summary").document(id).collection(year+"-"+month).document(day).set(checkIn);
        db.collection("EmployeesGrossSalary").document(currEmployee.getId()).collection(year).document(month).get().addOnSuccessListener(doc -> {
           if(!doc.exists()){
               //new month
               db.collection("EmployeesGrossSalary").document(currEmployee.getId()).get().addOnSuccessListener(documentSnapshot -> {
                   if (!documentSnapshot.exists()) return;
                   EmployeesGrossSalary employeeGrossSalary = documentSnapshot.toObject(EmployeesGrossSalary.class);
                   updateEmployeeGrossSalary(employeeGrossSalary);
               });
               return;
           }
           EmployeesGrossSalary employeesGrossSalary = doc.toObject(EmployeesGrossSalary.class);
            updateEmployeeGrossSalary(employeesGrossSalary);


        });

        Toast.makeText(getContext(), "Checked In successfully!", Toast.LENGTH_SHORT).show();
    }

    private void updateEmployeeGrossSalary(EmployeesGrossSalary employeesGrossSalary) {
        String currentDateAndTime = sdf.format(new Date());
        String day = currentDateAndTime.substring(0,2);
        String month = currentDateAndTime.substring(3,5);
        String year = currentDateAndTime.substring(6,10);
        ArrayList<Allowance> allTypes = employeesGrossSalary.getAllTypes();
        //return the first appearance of transportation type in allType array
        for(Allowance i :allTypes){
            if(i.getName().equalsIgnoreCase("Transportation")){
                Allowance transportation = new Allowance("Transportation",i.getAmount());
                transportation.setNote(day);
                allTypes.add(transportation);
                break;
            }
        }
        employeesGrossSalary.setAllTypes(allTypes);
        db.collection("EmployeesGrossSalary").document(currEmployee.getId()).collection(year).document(month).set(employeesGrossSalary, SetOptions.merge());

    }

    private final View.OnClickListener oclInside = view -> {
        MachineCheckInOutDialog machineCheckInOutDialog = new MachineCheckInOutDialog(true);
        machineCheckInOutDialog.show(getParentFragmentManager(), "");
    };
    private final View.OnClickListener oclOutside = view -> {
        MachineCheckInOutDialog machineCheckInOutDialog = new MachineCheckInOutDialog(false);
        machineCheckInOutDialog.show(getParentFragmentManager(), "");
    };

    private void checkMachineInOut(Client client) {
        machineEmployee.document(machineEmpId).get().addOnSuccessListener(documentSnapshot -> {
            if (!documentSnapshot.exists()) {
                machineCheckIn(client);
            } else {
                Machine_Employee currMachineEmployee = documentSnapshot.toObject(Machine_Employee.class);
                machineCheckOut(currMachineEmployee);
            }

        });
    }

    private void machineCheckOut(Machine_Employee currMachineEmployee) {
        HashMap<String, Object> checkOutDetails = new HashMap<>((new Summary(latitude, longitude)).getGeoMap());
        double workingCost, monthlyCost = 0, weeklyCost = 0, dailyCost = 0;
        checkOutDetails.put("Time", Timestamp.now());
        long checkInTime = ((Timestamp) currMachineEmployee.getCheckIn().get("Time")).getSeconds();
        long checkOutTime = Timestamp.now().getSeconds();
        long workingTime = (checkOutTime - checkInTime);
        long rem = workingTime;
        monthlyCost += (int) (workingTime / 2.628e+6);
        rem %= 2.628e+6;
        weeklyCost += (int) (rem / 604800);
        rem %= 604800;
        dailyCost += (int) (rem / 86400);
        rem %= 86400;
        if (rem > 0)
            dailyCost++;
        monthlyCost *= currMachine.getMonthlyRentPrice();
        weeklyCost *= currMachine.getWeeklyRentPrice();
        dailyCost *= currMachine.getDailyRentPrice();
        currMachineEmployee.setWorkedTime(workingTime);
        currMachineEmployee.setCheckOut(checkOutDetails);
        currMachineEmployee.setCost(monthlyCost + weeklyCost + dailyCost);
        db.collection("projects").document(currEmployee.getProjectID())
                .update("machineWorkedTime." + currMachine.getReference(), FieldValue.increment(workingTime));
        machineEmployee.document(machineEmpId).set(currMachineEmployee)
                .addOnSuccessListener(unused -> {
                    currMachine.removeEmployeeDependency();
                    machineCol.document(currMachine.getId()).update("isUsed", false, "employeeFirstName", "", "employeeId", "", "machineEmployeeID", "").addOnSuccessListener(vu -> {

                        Toast.makeText(getContext(), "Machine: " + currMachine.getReference() + " checked Out successfully", Toast.LENGTH_SHORT).show();

                    });
                });
    }

    private void machineCheckIn(Client client) {
        HashMap<String, Object> checkInDetails = new HashMap<>((new Summary(latitude, longitude)).getGeoMap());
        checkInDetails.put("Time", Timestamp.now());
        Map<String, Object> machineEmployee1 = new HashMap();
        machineEmployee1.put("machine", currMachine);
        machineEmployee1.put("employee", currEmployee);
        if (client != null)
            machineEmployee1.put("client", client);

        machineEmployee1.put("checkIn", checkInDetails);
        currMachine.setUsed(true);
        currMachine.setEmployeeFirstName(currEmployee.getFirstName());
        currMachine.setMachineEmployeeID(machineEmpId);
        //NOTE don't use set()
        machineCol.document(currMachine.getId()).update("isUsed", true, "employeeFirstName", currEmployee.getFirstName(), "employeeId", currEmployee.getId(), "machineEmployeeID", machineEmpId)
                .addOnSuccessListener(unused1 -> {
                    machineEmployee.document(machineEmpId).set(machineEmployee1).addOnSuccessListener(unused -> Toast.makeText(getContext(), "Machine: " + currMachine.getReference() + " checked In successfully", Toast.LENGTH_SHORT).show());
                });
        //todo: to be removed
        ArrayList<Allowance> allTypes = new ArrayList<>();
        db.collection("EmployeesGrossSalary").document(currEmployee.getId()).get().addOnSuccessListener((value) -> {
            if (!value.exists())
                return;
            EmployeesGrossSalary employeesGrossSalary;
            employeesGrossSalary = value.toObject(EmployeesGrossSalary.class);
            allTypes.addAll(employeesGrossSalary.getAllTypes());
            allTypes.add(currMachine.getAllowance());
            db.collection("EmployeesGrossSalary").document(currEmployee.getId()).update("allTypes", allTypes);
        });


    }

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
                                SupplementsDialog supplementsDialog = new SupplementsDialog(isItAUser, currMachine, currEmployee);
                                supplementsDialog.show(getParentFragmentManager(), "");
                                if (currMachine.getUsed() && !currMachine.getEmployeeId().equals(currEmployee.getId())) {
                                    Toast.makeText(getContext(), "this Machine already being used by" + currMachine.getEmployeeFirstName(), Toast.LENGTH_SHORT).show();
                                    return;
                                }
                                machineEmpId = !currMachine.getEmployeeId().equals(currEmployee.getId()) ? machineEmployee.document().getId() : currMachine.getMachineEmployeeID();

                            });
                        } catch (Exception e) {
                            Toast.makeText(getContext(), "invalid Machine ID", Toast.LENGTH_SHORT).show();
                        }
                    }
                }).addOnFailureListener(unused -> {
                    Toast.makeText(getContext(), "Please enable GPS!", Toast.LENGTH_SHORT).show();
                });
            }
        });
        getParentFragmentManager().setFragmentResultListener("supplements", this, new FragmentResultListener() {
            @Override
            public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle bundle) {
                String note = bundle.getString("supplementState");
                boolean isItAUser = bundle.getBoolean("isItAUser");

                if (isItAUser) {
                    checkMachineInOut(null);
                    if (!note.trim().isEmpty()) {
                        MachineDefectsLog machineDefectsLog = new MachineDefectsLog(note.trim(), currMachine.getReference(), currMachine.getId(), currEmployee.getId(), currEmployee.getFirstName(), new Date());
                        db.collection("MachineDefectsLog").add(machineDefectsLog).addOnSuccessListener(unused -> {
                            Toast.makeText(getActivity(), "comment has been uploaded", Toast.LENGTH_SHORT).show();
                        });
                    }
                } else {
                    ClientInfoDialog clientInfoDialog = new ClientInfoDialog(note);
                    clientInfoDialog.show(getParentFragmentManager(), "");
                }
            }
        });
        getParentFragmentManager().setFragmentResultListener("clientInfo", this, new FragmentResultListener() {
            @Override
            public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle result) {
                Client client = (Client) result.getSerializable("client");
                String note = result.getString("note");
                checkMachineInOut(client);
                if (!note.trim().isEmpty()) {
                    MachineDefectsLog machineDefectsLog = new MachineDefectsLog(note.trim(), currMachine.getReference(), currMachine.getId(), currEmployee.getId(), currEmployee.getFirstName(), new Date());
                    db.collection("MachineDefectsLog").add(machineDefectsLog).addOnSuccessListener(unused -> {
                        Toast.makeText(getActivity(), "comment has been uploaded", Toast.LENGTH_SHORT).show();
                    });
                }
            }
        });


    }


    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {

    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            new AppSettingsDialog.Builder(this).build().show();
        }
    }
}