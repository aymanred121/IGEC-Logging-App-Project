package com.igec.user.Fragments;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
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
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentResultListener;

import com.birjuvachhani.locus.Locus;
import com.igec.user.Dialogs.ClientInfoDialog;
import com.igec.user.Dialogs.MachineCheckInOutDialog;
import com.igec.user.Dialogs.SupplementsDialog;
import com.igec.user.R;
import com.igec.common.firebase.Allowance;
import com.igec.common.firebase.Client;
import com.igec.common.firebase.Employee;
import com.igec.common.firebase.EmployeesGrossSalary;
import com.igec.common.firebase.Machine;
import com.igec.common.firebase.MachineDefectsLog;
import com.igec.common.firebase.Machine_Employee;
import com.igec.common.firebase.Project;
import com.igec.common.firebase.Summary;
import com.igec.common.utilities.allowancesEnum;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.WriteBatch;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;

public class CheckInOutFragment extends Fragment implements EasyPermissions.PermissionCallbacks {

    private ExtendedFloatingActionButton vCheckInOut;
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
    private String year,month,day;
    private FusedLocationProviderClient fusedLocationClient;
    private final CollectionReference machineEmployee = db.collection("Machine_Employee");
    private final CollectionReference machineCol = db.collection("machine");
    @SuppressLint("SimpleDateFormat")
    private final SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
    private final double LAT = 30.103168;
    private final double LNG = 31.373099;
    private boolean inProjectArea;

    public static CheckInOutFragment newInstance(Employee user) {
        Bundle args = new Bundle();
        args.putSerializable("user", user);
        CheckInOutFragment fragment = new CheckInOutFragment();
        fragment.setArguments(args);
        return fragment;
    }
//    public CheckInOutFragment(Employee currEmployee) {
//        this.currEmployee = currEmployee;
//    }


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
        currEmployee = (Employee) getArguments().getSerializable("user");
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
        updateDate();
        setCheckInOutBtn();
        //check if the gps permission is granted
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //ask for permission
            getLocationPermissions();
        }


        vGreeting.setText(String.format("%s\n%s", getString(R.string.good_morning), currEmployee.getFirstName()));
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());
    }

    private void updateDate() {
        Calendar calendar = Calendar.getInstance();
        year = String.valueOf(calendar.get(Calendar.YEAR));
        month = String.format("%02d",calendar.get(Calendar.MONTH) + 1);
        day = String.format("%02d",calendar.get(Calendar.DAY_OF_MONTH));
        if (Integer.parseInt(day) > 25) {
            if (Integer.parseInt(month) + 1 == 13) {
                month = "01";
                year = String.format("%d", Integer.parseInt(year) + 1);
            } else {
                month = String.format("%02d", Integer.parseInt(month) + 1);
            }
        }
    }

    private void setCheckInOutBtn() {
        updateDate();
        db.collection("summary").document(id)
                .collection(year + "-" + month)
                .document(day)
                .get().addOnSuccessListener((value) -> {
                    if (!value.exists() || value.getData().size() == 0)
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
        vCheckInOut.setEnabled(false);
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(getActivity());
        builder.setTitle(getString(R.string.do_you_want_to_confirm_this_action))
                .setNegativeButton(getString(R.string.No), (dialogInterface, i) -> {
                    vCheckInOut.setEnabled(true);
                }).setOnDismissListener(unused -> {
                    vCheckInOut.setEnabled(true);
                })
                .setPositiveButton(getString(R.string.Yes), (dialogInterface, i) -> {
                    db.collection("projects").document(currEmployee.getProjectID()).get().addOnSuccessListener(doc -> {
                        if (!doc.exists())
                            return;
                        Project project = doc.toObject(Project.class);
                        Locus.INSTANCE.getCurrentLocation(getActivity(), result -> {
                            if (result.getError() != null) {
                                Toast.makeText(getActivity(), "can't complete the operation.", Toast.LENGTH_SHORT).show();
                                vCheckInOut.setEnabled(true);
                                return null;
                            }
                            Location location = result.getLocation();
                            updateDate();
                            longitude = location.getLongitude();
                            latitude = location.getLatitude();

                            // check if his location inside the project radius
                            double distance;
                            float results[] = new float[3];
                            Location.distanceBetween(latitude, longitude, project.getLat(), project.getLng(), results);
                            distance = results[0];
                            if (distance < project.getArea()) // he's in the project area
                            {
                                inProjectArea = true;
                            } else // he's not, or he's on office work
                            {
                                Location.distanceBetween(latitude, longitude, LAT, LNG, results);
                                distance = results[0];
                                if (distance < 200 /*TODO Help placeholder for office area*/){
                                    inProjectArea = false;
                                    Toast.makeText(getActivity(), "You're in the office", Toast.LENGTH_SHORT).show();
                                }
                                else{
                                    Toast.makeText(getActivity(), "You're not in the project area", Toast.LENGTH_SHORT).show();
                                    return null;
                                }
                            }
                            Summary summary = new Summary(latitude, longitude);
                            HashMap<String, Object> checkOutDetails = new HashMap<>(summary.getGeoMap());
                            checkOutDetails.put("Time", Timestamp.now());
                            db.collection("summary").document(id)
                                    .collection(year + "-" + month).document(day)
                                    .get().addOnSuccessListener(documentSnapshot -> {
                                        if (!documentSnapshot.exists() || documentSnapshot.getData().size() == 0) {
                                            employeeCheckIn(summary);
                                        } else {
                                            Summary summary1 = documentSnapshot.toObject(Summary.class);
                                            if (summary1.getCheckOut() == null) {
                                                employeeCheckOut(summary1, checkOutDetails);
                                            } else {
                                                summary1.setLastCheckInTime(Timestamp.now());
                                                db.document(documentSnapshot.getReference().getPath()).update("lastCheckInTime", summary1.getLastCheckInTime(), "checkOut", null);
                                                Toast.makeText(getContext(), "Checked In successfully!", Toast.LENGTH_SHORT).show();
                                                vCheckInOut.setEnabled(true);
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
                            return null;

                        });
                    });

                })
                .show();
    };

    private void employeeCheckOut(Summary summary, HashMap<String, Object> checkOut) {
        updateDate();
        long checkInTime = (summary.getLastCheckInTime()).getSeconds();
        long checkOutTime = Timestamp.now().getSeconds();
        long workingTime = (checkOutTime - checkInTime);
        //check if working time is greater than 8 hrs
        summary.setCheckOut(checkOut);
        summary.setWorkedTime(FieldValue.increment(workingTime));
        db.collection("summary").document(id).collection(year + "-" + month).document(day)
                .update("checkOut", checkOut, "workingTime", FieldValue.increment(workingTime))
                .addOnSuccessListener(unused -> {
                    db.collection("summary")
                            .document(id)
                            .collection(year + "-" + month)
                            .document(day)
                            .get().addOnSuccessListener(doc -> {
                                long workingTime1 = (long) doc.getData().get("workingTime");
                                long overTime = (workingTime1 - 28800);
                                if (overTime < 0) {
                                    overTime = 0;
                                }
                                overTime = overTime / 3600;
                                long finalOverTime = overTime;
                                WriteBatch batch = db.batch();
                                batch.set(db.document(doc.getReference().getPath()), new HashMap<String, Object>() {{
                                    put("overTime", finalOverTime);
                                }}, SetOptions.merge());
                                batch.commit();
                                Allowance overTimeAllowance = new Allowance();
                                overTimeAllowance.setAmount(finalOverTime * currEmployee.getOverTime());
                                overTimeAllowance.setName("overTime");
                                Date checkInDate = ((Timestamp) (summary.getCheckIn().get("Time"))).toDate();
                                Calendar cal = Calendar.getInstance();
                                cal.setTime(checkInDate);
                                String prevDay = String.format("%02d", cal.get(Calendar.DAY_OF_MONTH));
                                overTimeAllowance.setNote(prevDay);
                                overTimeAllowance.setType(allowancesEnum.OVERTIME.ordinal());
                                overTimeAllowance.setProjectId(currEmployee.getProjectID());
                                db.collection("EmployeesGrossSalary").document(id).collection(year).document(month).get().addOnSuccessListener(doc1 -> {
                                    EmployeesGrossSalary emp = doc1.toObject(EmployeesGrossSalary.class);
                                    ArrayList<Allowance> allowanceArrayList = emp.getAllTypes();
                                    if (allowanceArrayList != null) {
                                        allowanceArrayList.removeIf(x -> x.getName().equals("overTime") && x.getNote().trim().equals(day));
                                    }
                                    allowanceArrayList.add(overTimeAllowance);
                                    db.collection("EmployeesGrossSalary").document(id).collection(year).document(month).update("allTypes", allowanceArrayList);
                                });
                            });

                    vCheckInOut.setEnabled(true);
                    db.collection("projects").document(currEmployee.getProjectID())
                            .update("employeeWorkedTime." + currEmployee.getId(), FieldValue.increment(workingTime));

                });
        Toast.makeText(getContext(), "Checked Out successfully!", Toast.LENGTH_SHORT).show();
    }

    private void employeeCheckIn(Summary summary) {
        updateDate();
        summary.setLastCheckInTime(Timestamp.now());
        HashMap<String, Object> checkInDetails = new HashMap<>(summary.getGeoMap());
        checkInDetails.put("Time", Timestamp.now());
        HashMap<String, Object> checkIn = new HashMap<>();
        checkIn.put("checkIn", checkInDetails);
        checkIn.put("projectId", currEmployee.getProjectID());
        checkIn.put("lastCheckInTime", summary.getLastCheckInTime());
        db.collection("summary").document(id).collection(year + "-" + month).document(day).set(checkIn);
        db.collection("EmployeesGrossSalary").document(currEmployee.getId()).collection(year).document(month).get().addOnSuccessListener(doc -> {
            if (!doc.exists()) {
                //new month
                db.collection("EmployeesGrossSalary").document(currEmployee.getId()).get().addOnSuccessListener(documentSnapshot -> {
                    if (!documentSnapshot.exists()) return;
                    EmployeesGrossSalary employeesGrossSalary = documentSnapshot.toObject(EmployeesGrossSalary.class);
                    ArrayList<Allowance> allowances = employeesGrossSalary.getAllTypes().stream().filter(allowance -> allowance.getType() != allowancesEnum.NETSALARY.ordinal()).collect(Collectors.toCollection(ArrayList::new));
                    employeesGrossSalary.getAllTypes().removeIf(allowance -> allowance.getType()!= allowancesEnum.NETSALARY.ordinal());
                    employeesGrossSalary.setBaseAllowances(allowances);
                    if(inProjectArea) {
                    for (Allowance allowance : employeesGrossSalary.getBaseAllowances()) {
                            allowance.setNote(day);
                            employeesGrossSalary.getAllTypes().add(allowance);
                        }
                    }
                    db.document(doc.getReference().getPath()).set(employeesGrossSalary, SetOptions.merge());
                });
                return;
            }
            EmployeesGrossSalary employeesGrossSalary = doc.toObject(EmployeesGrossSalary.class);
            employeesGrossSalary.setEmployeeId(currEmployee.getId());
            if(inProjectArea) {
            for (Allowance allowance : employeesGrossSalary.getBaseAllowances()) {
                        allowance.setNote(day);
                        employeesGrossSalary.getAllTypes().add(allowance);
                }
            }
            db.collection("EmployeesGrossSalary").document(currEmployee.getId()).collection(year).document(month).set(employeesGrossSalary, SetOptions.merge());

        });
    }


    private final View.OnClickListener oclInside = view -> {
        if (!getCameraPermission()) {
            Toast.makeText(getActivity(), "Please, Enable camera permission !", Toast.LENGTH_SHORT).show();
            return;
        }
        MachineCheckInOutDialog machineCheckInOutDialog = new MachineCheckInOutDialog(true);
        machineCheckInOutDialog.show(getParentFragmentManager(), "");
    };
    private final View.OnClickListener oclOutside = view -> {
        if (!getCameraPermission()) {
            Toast.makeText(getActivity(), "Please, Enable camera permission !", Toast.LENGTH_SHORT).show();
            return;
        }
        MachineCheckInOutDialog machineCheckInOutDialog = new MachineCheckInOutDialog(false);
        machineCheckInOutDialog.show(getParentFragmentManager(), "");
    };

    private void machineCheckInOut(Client client, String note) {
        machineEmployee.document(machineEmpId).get().addOnSuccessListener(documentSnapshot -> {
            if (!documentSnapshot.exists()) {
                machineCheckIn(client);
            } else {
                Machine_Employee currMachineEmployee = documentSnapshot.toObject(Machine_Employee.class);
                machineCheckOut(currMachineEmployee);
            }
            if (!note.trim().isEmpty()) {
                MachineDefectsLog machineDefectsLog = new MachineDefectsLog(note.trim(), currMachine.getReference(), currMachine.getId(), currEmployee.getId(), currEmployee.getFirstName(), new Date());
                db.collection("MachineDefectsLog").add(machineDefectsLog).addOnFailureListener(unused -> {
                    Toast.makeText(getActivity(), "error uploading comment", Toast.LENGTH_SHORT).show();
                });
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
        Machine_Employee machine_employee = new Machine_Employee();
        machine_employee.setMachine(currMachine);
        machine_employee.setEmployee(currEmployee);
        machine_employee.setClient(client);
        machine_employee.setCheckIn(checkInDetails);
        currMachine.setIsUsed(true);
        currMachine.setEmployeeFirstName(currEmployee.getFirstName());
        currMachine.setMachineEmployeeID(machineEmpId);
        currMachine.setEmployeeId(currEmployee.getId());
        //NOTE don't use set()
        machineCol.document(currMachine.getId()).update("isUsed", true, "employeeFirstName", currEmployee.getFirstName(), "employeeId", currEmployee.getId(), "machineEmployeeID", machineEmpId)
                .addOnSuccessListener(unused1 -> {
                    machineEmployee.document(machineEmpId).set(machine_employee).addOnSuccessListener(unused -> Toast.makeText(getContext(), "Machine: " + currMachine.getReference() + " checked In successfully", Toast.LENGTH_SHORT).show());
                });
//        ArrayList<Allowance> allTypes = new ArrayList<>();
//        db.collection("EmployeesGrossSalary").document(currEmployee.getId()).get().addOnSuccessListener((value) -> {
//            if (!value.exists())
//                return;
//            EmployeesGrossSalary employeesGrossSalary;
//            employeesGrossSalary = value.toObject(EmployeesGrossSalary.class);
//            allTypes.addAll(employeesGrossSalary.getAllTypes());
//            allTypes.add(currMachine.getAllowance());
//            db.collection("EmployeesGrossSalary").document(currEmployee.getId()).update("allTypes", allTypes);
//        });


    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getParentFragmentManager().setFragmentResultListener("machine", this, new FragmentResultListener() {
            @SuppressLint("MissingPermission")
            @Override
            public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle bundle) {
                String machineID = bundle.getString("machineID");
                boolean isItAUser = bundle.getBoolean("isItAUser");

                // validate machine ID
                try {
                    machineCol.document(machineID).get().addOnSuccessListener((value) -> {
                        if (!value.exists()) {
                            Toast.makeText(getActivity(), "Invalid Machine ID", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        currMachine = value.toObject(Machine.class);
                        // Generate machineEmpID if not found
                        machineEmpId = !currMachine.getEmployeeId().equals(currEmployee.getId()) ? machineEmployee.document().getId() : currMachine.getMachineEmployeeID();
                        // if machineEmpID found check if the check-in agent and checkout agent are the same (user or company)
                        machineEmployee.document(machineEmpId).get().addOnSuccessListener(documentSnapshot -> {
                            if (documentSnapshot.exists()) {
                                // check-out
                                if (isItAUser != ((documentSnapshot.toObject(Machine_Employee.class)).getClient() == null)) {
                                    Toast.makeText(getActivity(), "Checkout agent doesn't match", Toast.LENGTH_SHORT).show();
                                    return;
                                }
                            }
                            Locus.INSTANCE.getCurrentLocation(getActivity(), locusResult -> {
                                Location location = locusResult.getLocation();
                                if (locusResult.getError() != null) {
                                    Toast.makeText(getActivity(), "can't complete the operation.", Toast.LENGTH_SHORT).show();
                                    vCheckInOut.setEnabled(true);
                                    return null;
                                }
                                longitude = location.getLongitude();
                                latitude = location.getLatitude();
                                // abort if machine is used already by another user
                                if (currMachine.getIsUsed() && !currMachine.getEmployeeId().equals(currEmployee.getId())) {
                                    Toast.makeText(getContext(), "this Machine already being used by" + currMachine.getEmployeeFirstName(), Toast.LENGTH_SHORT).show();
                                    return null;
                                }
                                // open supplements dialog if not
                                SupplementsDialog supplementsDialog = new SupplementsDialog(isItAUser, currMachine, currEmployee);
                                supplementsDialog.show(getParentFragmentManager(), "");
                                return null;
                            });

                        });
                    });
                } catch (Exception e) {
                    Toast.makeText(getActivity(), "invalid Machine ID", Toast.LENGTH_SHORT).show();
                }
            }
        });
        getParentFragmentManager().setFragmentResultListener("supplements", this, new FragmentResultListener() {
            @Override
            public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle bundle) {
                String note = bundle.getString("supplementState");
                boolean isItAUser = bundle.getBoolean("isItAUser");

                // if isItAUser check-in
                if (isItAUser) {
                    machineCheckInOut(null, note);

                }
                // if not
                else {
                    // only open when client is null in Machine_Employee ie when checking in only
                    machineEmployee.document(machineEmpId).get().addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            Machine_Employee machine_employee = documentSnapshot.toObject(Machine_Employee.class);
                            machineCheckInOut(machine_employee.getClient(), note);
                        } else {
                            ClientInfoDialog clientInfoDialog = new ClientInfoDialog(note);
                            clientInfoDialog.show(getParentFragmentManager(), "");
                        }
                    });

                }
            }
        });
        getParentFragmentManager().setFragmentResultListener("clientInfo", this, new FragmentResultListener() {
            @Override
            public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle result) {
                Client client = (Client) result.getSerializable("client");
                String note = result.getString("note");
                machineCheckInOut(client, note);
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