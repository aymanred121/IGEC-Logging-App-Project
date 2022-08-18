package com.igec.user.fragments;

import static com.igec.common.CONSTANTS.CAMERA_REQUEST_CODE;
import static com.igec.common.CONSTANTS.EMPLOYEE_GROSS_SALARY_COL;
import static com.igec.common.CONSTANTS.LOCATION_REQUEST_CODE;
import static com.igec.common.CONSTANTS.MACHINE_COL;
import static com.igec.common.CONSTANTS.MACHINE_DEFECT_LOG_COL;
import static com.igec.common.CONSTANTS.MACHINE_EMPLOYEE_COL;
import static com.igec.common.CONSTANTS.PROJECT_COL;
import static com.igec.common.CONSTANTS.SUMMARY_COL;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentResultListener;

import com.birjuvachhani.locus.Locus;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.material.snackbar.Snackbar;
import com.igec.user.activities.DateInaccurate;
import com.igec.user.databinding.FragmentCheckInOutBinding;
import com.igec.user.dialogs.ClientInfoDialog;
import com.igec.user.dialogs.MachineCheckInOutDialog;
import com.igec.user.dialogs.AccessoriesDialog;
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
import com.google.android.gms.location.LocationServices;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.Timestamp;
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

    private boolean isOpen = false;
    private Animation fabClose, fabOpen, rotateForward, rotateBackward, show, hide, rotateBackwardHide;
    private Boolean isHere = Boolean.FALSE;
    private Employee currEmployee = null;
    private String id;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private double latitude, longitude;
    private Machine currMachine;
    private String machineEmpId;
    private String year, month, day;
    private FusedLocationProviderClient fusedLocationClient;
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

    private final View.OnClickListener oclMachine = view -> {
        animationFab();
    };

    private FragmentCheckInOutBinding binding;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentCheckInOutBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        binding = null;
    }
    @Override
    public void onResume() {
        super.onResume();
        validateDate(getActivity());
    }
    private void validateDate(Context c) {
        if (Settings.Global.getInt(c.getContentResolver(), Settings.Global.AUTO_TIME, 0) != 1) {
            Intent intent = new Intent(getActivity(), DateInaccurate.class);
            startActivity(intent);
            getActivity().finish();
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initialize();
        getLocationPermissions();
        getCameraPermission();
        // Listeners

        binding.checkInOutFab.setOnClickListener(oclCheckInOut);
        binding.addMachineFab.setOnClickListener(oclMachine);
        binding.insideFab.setOnClickListener(oclInside);
        binding.outsideFab.setOnClickListener(oclOutside);
    }

    private void initialize() {
        //Views
        currEmployee = (Employee) getArguments().getSerializable("user");
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


        binding.greetingText.setText(String.format("%s\n%s", getString(R.string.good_morning), currEmployee.getFirstName()));
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());
    }

    private void updateDate() {
        Calendar calendar = Calendar.getInstance();
        year = String.valueOf(calendar.get(Calendar.YEAR));
        month = String.format("%02d", calendar.get(Calendar.MONTH) + 1);
        day = String.format("%02d", calendar.get(Calendar.DAY_OF_MONTH));
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
        SUMMARY_COL.document(id)
                .collection(year + "-" + month)
                .document(day)
                .get().addOnSuccessListener((value) -> {
                    if (!value.exists() || value.getData().size() == 0)
                        isHere = false;
                    else {
                        isHere = value.getData().get("checkOut") == null;
                    }
                    binding.checkInOutFab.setBackgroundColor((isHere) ? Color.rgb(153, 0, 0) : Color.rgb(0, 153, 0));
                    binding.checkInOutFab.setText(isHere ? "Out" : "In");
                    binding.addMachineFab.setClickable(isHere);
                    if (isHere)
                        binding.addMachineFab.startAnimation(show);
                });
    }


    private void animationFab() {
        if (isOpen) {
            binding.addMachineFab.startAnimation(rotateBackward);
            binding.insideFab.startAnimation(fabClose);
            binding.outsideFab.startAnimation(fabClose);
            binding.insideText.startAnimation(hide);
            binding.outsideText.startAnimation(hide);
            binding.insideFab.setClickable(false);
            binding.outsideFab.setClickable(false);
        } else {

            binding.addMachineFab.startAnimation(rotateForward);
            binding.insideFab.startAnimation(fabOpen);
            binding.outsideFab.startAnimation(fabOpen);
            binding.insideText.startAnimation(show);
            binding.outsideText.startAnimation(show);
            binding.insideFab.setClickable(true);
            binding.outsideFab.setClickable(true);
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
        binding.checkInOutFab.setEnabled(false);
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(getActivity());
        builder.setTitle(getString(R.string.do_you_want_to_confirm_this_action))
                .setNegativeButton(getString(R.string.No), (dialogInterface, i) -> {
                    binding.checkInOutFab.setEnabled(true);
                }).setOnDismissListener(unused -> {
                    binding.checkInOutFab.setEnabled(true);
                })
                .setPositiveButton(getString(R.string.Yes), (dialogInterface, i) -> {
                    PROJECT_COL.document(currEmployee.getProjectID()).get().addOnSuccessListener(doc -> {
                        if (!doc.exists())
                            return;
                        Project project = doc.toObject(Project.class);
                        Locus.INSTANCE.getCurrentLocation(getActivity(), result -> {
                            if (result.getError() != null) {
                                Snackbar.make(binding.getRoot(), "can't complete the operation.", Snackbar.LENGTH_SHORT).show();
                                binding.checkInOutFab.setEnabled(true);
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
                                if (distance < 200 /*TODO Help placeholder for office area*/) {
                                    inProjectArea = false;
                                    Snackbar.make(binding.getRoot(), "You're in the office", Snackbar.LENGTH_SHORT).show();
                                } else {
                                    Snackbar.make(binding.getRoot(), "You're not in the project area", Snackbar.LENGTH_SHORT).show();
                                    return null;
                                }
                            }
                            Summary summary = new Summary(latitude, longitude);
                            HashMap<String, Object> checkOutDetails = new HashMap<>(summary.getGeoMap());
                            checkOutDetails.put("Time", Timestamp.now());
                            SUMMARY_COL.document(id)
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
                                                Snackbar.make(binding.getRoot(), "Checked In successfully!", Toast.LENGTH_SHORT).show();
                                                binding.checkInOutFab.setEnabled(true);
                                            }
                                        }
                                    });
                            isHere = !isHere;
                            binding.checkInOutFab.setBackgroundColor((isHere) ? Color.rgb(153, 0, 0) : Color.rgb(0, 153, 0));
                            binding.checkInOutFab.setText(isHere ? "Out" : "In");
                            binding.addMachineFab.setClickable(isHere);
                            if (isOpen) {
                                binding.addMachineFab.startAnimation(rotateBackwardHide);
                                binding.insideFab.startAnimation(fabClose);
                                binding.outsideFab.startAnimation(fabClose);
                                binding.insideText.startAnimation(hide);
                                binding.outsideText.startAnimation(hide);
                                binding.insideFab.setClickable(false);
                                binding.outsideFab.setClickable(false);
                                isOpen = false;
                            } else {
                                binding.addMachineFab.startAnimation(isHere ? show : hide);
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
        SUMMARY_COL.document(id).collection(year + "-" + month).document(day)
                .update("checkOut", checkOut, "workingTime", FieldValue.increment(workingTime))
                .addOnSuccessListener(unused -> {
                    SUMMARY_COL
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
                                EMPLOYEE_GROSS_SALARY_COL.document(id).collection(year).document(month).get().addOnSuccessListener(doc1 -> {
                                    EmployeesGrossSalary emp = doc1.toObject(EmployeesGrossSalary.class);
                                    ArrayList<Allowance> allowanceArrayList = emp.getAllTypes();
                                    if (allowanceArrayList != null) {
                                        allowanceArrayList.removeIf(x -> x.getName().equals("overTime") && x.getNote().trim().equals(day));
                                    }
                                    allowanceArrayList.add(overTimeAllowance);
                                    EMPLOYEE_GROSS_SALARY_COL.document(id).collection(year).document(month).update("allTypes", allowanceArrayList);
                                });
                            });

                    binding.checkInOutFab.setEnabled(true);
                    PROJECT_COL.document(currEmployee.getProjectID())
                            .update("employeeWorkedTime." + currEmployee.getId(), FieldValue.increment(workingTime));

                });
        Snackbar.make(binding.getRoot(), "Checked Out successfully!", Toast.LENGTH_SHORT).show();
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
        SUMMARY_COL.document(id).collection(year + "-" + month).document(day).set(checkIn);
        EMPLOYEE_GROSS_SALARY_COL.document(currEmployee.getId()).collection(year).document(month).get().addOnSuccessListener(doc -> {
            if (!doc.exists()) {
                //new month
                EMPLOYEE_GROSS_SALARY_COL.document(currEmployee.getId()).get().addOnSuccessListener(documentSnapshot -> {
                    if (!documentSnapshot.exists()) return;
                    EmployeesGrossSalary employeesGrossSalary = documentSnapshot.toObject(EmployeesGrossSalary.class);
                    ArrayList<Allowance> allowances = employeesGrossSalary.getAllTypes().stream().filter(allowance -> allowance.getType() != allowancesEnum.NETSALARY.ordinal()).collect(Collectors.toCollection(ArrayList::new));
                    employeesGrossSalary.getAllTypes().removeIf(allowance -> allowance.getType() != allowancesEnum.NETSALARY.ordinal());
                    employeesGrossSalary.setBaseAllowances(allowances);
                    if (inProjectArea) {
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
            if (inProjectArea) {
                for (Allowance allowance : employeesGrossSalary.getBaseAllowances()) {
                    allowance.setNote(day);
                    employeesGrossSalary.getAllTypes().add(allowance);
                }
            }
            EMPLOYEE_GROSS_SALARY_COL.document(currEmployee.getId()).collection(year).document(month).set(employeesGrossSalary, SetOptions.merge());

        });
    }


    private final View.OnClickListener oclInside = view -> {
        if (!getCameraPermission()) {
            Snackbar.make(binding.getRoot(), "Please, Enable camera permission !", Snackbar.LENGTH_SHORT).show();
            return;
        }
        MachineCheckInOutDialog machineCheckInOutDialog = new MachineCheckInOutDialog(true);
        machineCheckInOutDialog.show(getParentFragmentManager(), "");
    };
    private final View.OnClickListener oclOutside = view -> {
        if (!getCameraPermission()) {
            Snackbar.make(binding.getRoot(), "Please, Enable camera permission !", Snackbar.LENGTH_SHORT).show();
            return;
        }
        MachineCheckInOutDialog machineCheckInOutDialog = new MachineCheckInOutDialog(false);
        machineCheckInOutDialog.show(getParentFragmentManager(), "");
    };

    private void machineCheckInOut(Client client, String note) {
        MACHINE_EMPLOYEE_COL.document(machineEmpId).get().addOnSuccessListener(documentSnapshot -> {
            if (!documentSnapshot.exists()) {
                machineCheckIn(client);
            } else {
                Machine_Employee currMachineEmployee = documentSnapshot.toObject(Machine_Employee.class);
                machineCheckOut(currMachineEmployee);
            }
            if (!note.trim().isEmpty()) {
                MachineDefectsLog machineDefectsLog = new MachineDefectsLog(note.trim(), currMachine.getReference(), currMachine.getId(), currEmployee.getId(), currEmployee.getFirstName(), new Date());
                MACHINE_DEFECT_LOG_COL.add(machineDefectsLog).addOnFailureListener(unused -> {
                    Snackbar.make(binding.getRoot(), "error uploading comment", Snackbar.LENGTH_SHORT).show();
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
        PROJECT_COL.document(currEmployee.getProjectID())
                .update("machineWorkedTime." + currMachine.getReference(), FieldValue.increment(workingTime));
        MACHINE_EMPLOYEE_COL.document(machineEmpId).set(currMachineEmployee)
                .addOnSuccessListener(unused -> {
                    currMachine.removeEmployeeDependency();
                    MACHINE_COL.document(currMachine.getId()).update("isUsed", false, "employeeFirstName", "", "employeeId", "", "machineEmployeeID", "").addOnSuccessListener(vu -> {

                        Snackbar.make(binding.getRoot(), "Machine: " + currMachine.getReference() + " checked Out successfully", Toast.LENGTH_SHORT).show();

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
        MACHINE_COL.document(currMachine.getId()).update("isUsed", true, "employeeFirstName", currEmployee.getFirstName(), "employeeId", currEmployee.getId(), "machineEmployeeID", machineEmpId)
                .addOnSuccessListener(unused1 -> {
                    MACHINE_EMPLOYEE_COL.document(machineEmpId).set(machine_employee).addOnSuccessListener(unused -> Snackbar.make(binding.getRoot(), "Machine: " + currMachine.getReference() + " checked In successfully", Toast.LENGTH_SHORT).show());
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
                    MACHINE_COL.document(machineID).get().addOnSuccessListener((value) -> {
                        if (!value.exists()) {
                            Snackbar.make(binding.getRoot(), "Invalid Machine ID", Snackbar.LENGTH_SHORT).show();
                            return;
                        }
                        currMachine = value.toObject(Machine.class);
                        // Generate machineEmpID if not found
                        machineEmpId = !currMachine.getEmployeeId().equals(currEmployee.getId()) ? MACHINE_EMPLOYEE_COL.document().getId() : currMachine.getMachineEmployeeID();
                        // if machineEmpID found check if the check-in agent and checkout agent are the same (user or company)
                        MACHINE_EMPLOYEE_COL.document(machineEmpId).get().addOnSuccessListener(documentSnapshot -> {
                            if (documentSnapshot.exists()) {
                                // check-out
                                if (isItAUser != ((documentSnapshot.toObject(Machine_Employee.class)).getClient() == null)) {
                                    Snackbar.make(binding.getRoot(), "Checkout agent doesn't match", Snackbar.LENGTH_SHORT).show();
                                    return;
                                }
                            }
                            Locus.INSTANCE.getCurrentLocation(getActivity(), locusResult -> {
                                Location location = locusResult.getLocation();
                                if (locusResult.getError() != null) {
                                    Snackbar.make(binding.getRoot(), "can't complete the operation.", Snackbar.LENGTH_SHORT).show();
                                    binding.checkInOutFab.setEnabled(true);
                                    return null;
                                }
                                longitude = location.getLongitude();
                                latitude = location.getLatitude();
                                // abort if machine is used already by another user
                                if (currMachine.getIsUsed() && !currMachine.getEmployeeId().equals(currEmployee.getId())) {
                                    Snackbar.make(binding.getRoot(), "this Machine already being used by" + currMachine.getEmployeeFirstName(), Toast.LENGTH_SHORT).show();
                                    return null;
                                }
                                // open supplements dialog if not
                                AccessoriesDialog accessoriesDialog = new AccessoriesDialog(isItAUser, currMachine, currEmployee);
                                accessoriesDialog.show(getParentFragmentManager(), "");
                                return null;
                            });

                        });
                    });
                } catch (Exception e) {
                    Snackbar.make(binding.getRoot(), "Invalid Machine ID", Snackbar.LENGTH_SHORT).show();
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
                    MACHINE_EMPLOYEE_COL.document(machineEmpId).get().addOnSuccessListener(documentSnapshot -> {
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