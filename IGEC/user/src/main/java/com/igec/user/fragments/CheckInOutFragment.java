package com.igec.user.fragments;

import static com.igec.common.CONSTANTS.CAMERA_REQUEST_CODE;
import static com.igec.common.CONSTANTS.CHECK_IN_FROM_HOME;
import static com.igec.common.CONSTANTS.CHECK_IN_FROM_OFFICE;
import static com.igec.common.CONSTANTS.CHECK_IN_FROM_SITE;
import static com.igec.common.CONSTANTS.CHECK_IN_FROM_SUPPORT;
import static com.igec.common.CONSTANTS.EMPLOYEE_GROSS_SALARY_COL;
import static com.igec.common.CONSTANTS.LOCATION_REQUEST_CODE;
import static com.igec.common.CONSTANTS.MACHINE_COL;
import static com.igec.common.CONSTANTS.MACHINE_DEFECT_LOG_COL;
import static com.igec.common.CONSTANTS.MACHINE_EMPLOYEE_COL;
import static com.igec.common.CONSTANTS.OFFICE_REF;
import static com.igec.common.CONSTANTS.PROJECT_COL;
import static com.igec.common.CONSTANTS.SUMMARY_COL;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
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
import com.google.android.gms.location.LocationServices;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.igec.common.firebase.Allowance;
import com.igec.common.firebase.Client;
import com.igec.common.firebase.Employee;
import com.igec.common.firebase.EmployeesGrossSalary;
import com.igec.common.firebase.Machine;
import com.igec.common.firebase.MachineDefectsLog;
import com.igec.common.firebase.Machine_Employee;
import com.igec.common.firebase.Project;
import com.igec.common.firebase.Summary;
import com.igec.common.utilities.AllowancesEnum;
import com.igec.user.AlarmReceiver;
import com.igec.user.CacheDirectory;
import com.igec.user.R;
import com.igec.user.activities.DateInaccurate;
import com.igec.user.databinding.FragmentCheckInOutBinding;
import com.igec.user.dialogs.AccessoriesDialog;
import com.igec.user.dialogs.ClientInfoDialog;
import com.igec.user.dialogs.MachineCheckInOutDialog;

import java.lang.reflect.Type;
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
    private String lastProjectId;
    private AlarmManager alarmManager = null;
    private PendingIntent pendingIntent = null;

    private enum CheckInType {
        HOME,
        OFFICE,
        SITE,
        SUPPORT,
    }

    CheckInType checkInType = CheckInType.HOME;
    boolean canCheckFromHome = true;

    public static CheckInOutFragment newInstance(Employee user) {
        Bundle args = new Bundle();
        args.putSerializable("user", user);
        CheckInOutFragment fragment = new CheckInOutFragment();
        fragment.setArguments(args);
        return fragment;
    }


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
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
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
        updateDate();
        binding.greetingText.setText(String.format("%s\n%s", getString(R.string.good_morning), currEmployee.getFirstName()));
        LocationServices.getFusedLocationProviderClient(getActivity());
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
                .get().addOnCompleteListener((task) -> {
                    if (!task.isSuccessful() || task.getResult().getMetadata().isFromCache()) {
                        //update from offline data
                        Gson gson = new Gson();
                        Summary summary = gson.fromJson(CacheDirectory.readAllCachedText(getActivity(), "summary.json"), Summary.class);
                        isHere = summary != null && summary.getCheckOut() == null;
                        updateCheckInOutBtn(summary);
                    } else {
                        DocumentSnapshot value = task.getResult();
                        isHere = value.exists() && value.getData().size() != 0 && value.getData().get("checkOut") == null;
                        Summary summary = value.toObject(Summary.class);
                        updateCheckInOutBtn(summary);
                    }
                });
    }

    private void updateCheckInOutBtn(Summary summary) {
        lastProjectId = summary != null ? summary.getLastProjectId() : null;
        /*
         * summary == null --> canCheckFromHome = true
         * summary != null --> summary.getProjectIds().size() == 0 ==> canCheckFromHome
         * */
        canCheckFromHome = summary == null || summary.getProjectIds().size() == 0;
        if (lastProjectId != null) {
            PROJECT_COL.document(lastProjectId).get().addOnSuccessListener((value1) -> {
                Project project = value1.toObject(Project.class);
                if (project != null) {
                    binding.greetingText.setText(String.format("you are currently \n In %s", project.getName()));
                }
            });
        }
        //need project id
        if (summary != null && summary.getProjectIds().containsKey("HOME")) {
            //disable check-In btn
            binding.checkInOutFab.setEnabled(false);
            binding.checkInOutFab.setText("HOME");
            binding.checkInOutFab.setBackgroundColor(Color.GRAY);
        } else {
            binding.checkInOutFab.setBackgroundColor((isHere) ? Color.rgb(153, 0, 0) : Color.rgb(0, 153, 0));
            binding.checkInOutFab.setText(isHere ? "Out" : "In");
            binding.addMachineFab.setClickable(isHere);
            if (isHere)
                binding.addMachineFab.startAnimation(show);
        }
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


    private List<Project> getProjectsFromCache() {
        List<Project> projects = new ArrayList<>();
        String projectsJson = CacheDirectory.readAllCachedText(getActivity(), "projects.json");
        if (projectsJson != null) {
            Type type = new TypeToken<List<Project>>() {
            }.getType();
            projects = new Gson().fromJson(projectsJson, type);
        }
        return projects;
    }

    private void notifyLocation() {
        switch (checkInType) {
            case SITE:
                Snackbar.make(binding.getRoot(), "You are in the site", Snackbar.LENGTH_SHORT).show();
                break;
            case OFFICE:
                Snackbar.make(binding.getRoot(), "You are at the office", Snackbar.LENGTH_SHORT).show();
                break;
            case SUPPORT:
                Snackbar.make(binding.getRoot(), "You are now a support in this project", Snackbar.LENGTH_SHORT).show();
                break;
            case HOME:
                if (canCheckFromHome)
                    updateEmployeeSummary(latitude, longitude, new Project());
                else {
                    if (lastProjectId != null)
                        Snackbar.make(binding.getRoot(), "You can't check-out from outside the project", Snackbar.LENGTH_SHORT).show();
                    else
                        Snackbar.make(binding.getRoot(), "You can't recheck-in from home", Snackbar.LENGTH_SHORT).show();
                    binding.checkInOutFab.setEnabled(true);
                }
                break;
        }
    }

    private MaterialAlertDialogBuilder showAlertOfTheCheckingInLocation(String message) {
        MaterialAlertDialogBuilder alert = new MaterialAlertDialogBuilder(getActivity());
        alert.setTitle(message)
                .setNegativeButton("No", (dialogInterface12, i12) -> {
                    binding.checkInOutFab.setEnabled(true);
                })
                .setOnDismissListener(
                        dialogInterface -> binding.checkInOutFab.setEnabled(true)
                );
        return alert;
    }


    private void updateCheckInOutBtn() {
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
    }

    private void updateEmployeeSummary(double latitude, double longitude, Project project) {
        updateDate();
        Summary summary = new Summary(latitude, longitude);
        HashMap<String, Object> checkOutDetails = new HashMap<>(summary.getGeoMap());
        checkOutDetails.put("Time", Timestamp.now());
        SUMMARY_COL.document(id)
                .collection(year + "-" + month).document(day)
                .get().addOnCompleteListener(task -> {
                    if (!task.isSuccessful() || task.getResult().getMetadata().isFromCache()) {
                        //offline
                        Summary summaryCache = getSummaryCache();
                        if (summaryCache == null) {
                            //checkIn
                            checkInAction(project, summary, checkOutDetails);
                        } else {
                            if (summaryCache.getCheckOut() == null) {
                                //checkOut
                                checkOutAction(project, checkOutDetails, summaryCache);
                            } else {
                                reCheckInAction(project, summaryCache);
                            }
                        }
                    } else {
                        //get the result
                        DocumentSnapshot documentSnapshot = task.getResult();
                        if (!documentSnapshot.exists() || documentSnapshot.getData().size() == 0) {
                            //check in
                            checkInAction(project, summary, checkOutDetails);
                        } else {
                            Summary summary1 = documentSnapshot.toObject(Summary.class);
                            if (summary1.getCheckOut() == null) {
                                //check out
                                checkOutAction(project, checkOutDetails, summary1);
                            } else {
                                //re check in
                                reCheckInAction(project, summary1);
                            }
                        }
                    }
                });
    }

    private void createNotificationChannel(String CHANNEL_ID, int channelName, int channelDesc) {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        String name = getString(channelName);
        String descriptionText = getString(channelDesc);
        int importance = NotificationManager.IMPORTANCE_DEFAULT;
        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
        channel.setDescription(descriptionText);
        channel.enableLights(true);
        channel.setLightColor(Color.GREEN);
        channel.setSound(alarmSound, null);

        // Register the channel with the system
        NotificationManager notificationManager =
                (NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.createNotificationChannel(channel);
    }

    private void setUpAlarm(int hours, int minutes) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        // set to 4:30 pm
        calendar.set(Calendar.HOUR_OF_DAY, hours + 12);
        calendar.set(Calendar.MINUTE, minutes);
        calendar.set(Calendar.SECOND, 0);
        if (calendar.get(Calendar.DAY_OF_WEEK) != Calendar.FRIDAY) {
            createNotificationChannel(
                    "shift-notification",
                    R.string.shift_notification,
                    R.string.shift_notification_channel_description
            );
            alarmManager = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);
            Intent intent = new Intent(getActivity(), AlarmReceiver.class);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                pendingIntent = PendingIntent.getBroadcast(
                        getActivity(),
                        0,
                        intent,
                        PendingIntent.FLAG_MUTABLE
                );
            } else {
                pendingIntent = PendingIntent.getBroadcast(
                        getActivity(),
                        0,
                        intent,
                        0
                );
            }
            alarmManager.setInexactRepeating(
                    AlarmManager.RTC_WAKEUP,
                    calendar.getTimeInMillis(),
                    AlarmManager.INTERVAL_DAY,
                    pendingIntent
            );
        }
    }

    private void reCheckInAction(Project project, Summary summary1) {
        showAlertOfTheCheckingInLocation(getString(R.string.you_are_rechecking_in_to_that_do_you_want_to_confirm_this_action, project.getName())).setPositiveButton("Yes", (dialogInterface1, i1) -> {
                    employeeReCheckIn(summary1, project);
                    binding.greetingText.setText(String.format("you are currently \n In %s", project.getName()));
                    updateCheckInOutBtn();
                    notifyLocation();
                }
        ).show();
    }


    private void checkOutAction(Project project, HashMap<String, Object> checkOutDetails, Summary summary1) {
        showAlertOfTheCheckingInLocation(getString(R.string.you_are_checking_out_to_that_do_you_want_to_confirm_this_action, project.getName())).setPositiveButton("Yes", ((dialogInterface, i) -> {
            employeeCheckOut(summary1, checkOutDetails, project.getId());
            binding.greetingText.setText(String.format("%s\n%s", getString(R.string.good_morning), currEmployee.getFirstName()));
            updateCheckInOutBtn();
        })).show();
    }

    private void checkInAction(Project project, Summary summary, HashMap<String, Object> checkOutDetails) {
        showAlertOfTheCheckingInLocation(getString(R.string.you_are_checking_in_to_that_do_you_want_to_confirm_this_action, checkInType == CheckInType.HOME ? "Home" : project.getName())).setPositiveButton("Yes", (dialogInterface1, i1) -> {
            canCheckFromHome = false;
            // if possible set time based on when he checked in
            setUpAlarm(4, 30);
            employeeCheckIn(summary, project);
            if (checkInType == CheckInType.HOME) {
                employeeCheckOut(summary, checkOutDetails, "HOME");
                binding.checkInOutFab.setEnabled(false);
                binding.checkInOutFab.setText("HOME");
                binding.checkInOutFab.setBackgroundColor(Color.GRAY);
                Snackbar.make(binding.getRoot(), "You are at home", Snackbar.LENGTH_SHORT).show();
            } else
                binding.greetingText.setText(project.getId() != null ? String.format("you are currently \n In %s", project.getName()) : binding.greetingText.getText());
            updateCheckInOutBtn();
            notifyLocation();
        }).show();
    }

    private void employeeReCheckIn(Summary summary, Project project) {
        summary.setLastCheckInTime(Timestamp.now());
        if (!summary.getProjectIds().keySet().contains(project.getId())) {
            switch (checkInType) {
                case SITE:
                    summary.getProjectIds().put(project.getId(), CHECK_IN_FROM_SITE);
                    if (currEmployee.isManager()) {
                        ArrayList<Allowance> projectAllowances = new ArrayList<>();
                        projectAllowances.addAll(project.getAllowancesList());
                        projectAllowances.forEach(allowance -> {
                            allowance.setNote(day);
                        });
                        EMPLOYEE_GROSS_SALARY_COL.document(currEmployee.getId()).get().addOnSuccessListener(doc -> {
                            if (!doc.exists()) return;
                            EmployeesGrossSalary employeesGrossSalary = doc.toObject(EmployeesGrossSalary.class);
                            ArrayList<Allowance> allTypes = new ArrayList<>();
                            ArrayList<Allowance> baseAllowances = new ArrayList<>();
                            allTypes.addAll(projectAllowances);
                            employeesGrossSalary.getAllTypes().forEach(al -> {
                                if (al.getType() == AllowancesEnum.NETSALARY.ordinal())
                                    allTypes.add(al);
                                else {
                                    baseAllowances.add(al);
                                }
                            });
                            EMPLOYEE_GROSS_SALARY_COL.document(currEmployee.getId())
                                    .collection(year).document(month).get().addOnSuccessListener(doc1 -> {
                                        EmployeesGrossSalary employeesGrossSalary1 = new EmployeesGrossSalary();
                                        if (!doc1.exists()) {
                                            //new month
                                            employeesGrossSalary1.setEmployeeId(employeesGrossSalary.getEmployeeId());
                                            employeesGrossSalary1.setBaseAllowances(baseAllowances);
                                            employeesGrossSalary1.setAllTypes(allTypes);
                                        } else {
                                            employeesGrossSalary1 = doc1.toObject(EmployeesGrossSalary.class);
                                            employeesGrossSalary1.getAllTypes().addAll(projectAllowances);
                                        }
                                        EMPLOYEE_GROSS_SALARY_COL.document(currEmployee.getId())
                                                .collection(year).document(month).set(employeesGrossSalary1, SetOptions.merge());
                                    });
                        });
                    }
                    break;
                case SUPPORT:
                    summary.getProjectIds().put(project.getId(), CHECK_IN_FROM_SUPPORT);
                    if (currEmployee.isManager()) {
                        ArrayList<Allowance> projectAllowances = new ArrayList<>();
                        projectAllowances.addAll(project.getAllowancesList());
                        projectAllowances.forEach(allowance -> {
                            allowance.setNote(day);
                        });
                        EMPLOYEE_GROSS_SALARY_COL.document(currEmployee.getId()).get().addOnSuccessListener(doc -> {
                            if (!doc.exists()) return;
                            EmployeesGrossSalary employeesGrossSalary = doc.toObject(EmployeesGrossSalary.class);
                            ArrayList<Allowance> allTypes = new ArrayList<>();
                            ArrayList<Allowance> baseAllowances = new ArrayList<>();
                            allTypes.addAll(projectAllowances);
                            employeesGrossSalary.getAllTypes().forEach(al -> {
                                if (al.getType() == AllowancesEnum.NETSALARY.ordinal())
                                    allTypes.add(al);
                                else {
                                    baseAllowances.add(al);
                                }
                            });
                            EMPLOYEE_GROSS_SALARY_COL.document(currEmployee.getId())
                                    .collection(year).document(month).get().addOnSuccessListener(doc1 -> {
                                        EmployeesGrossSalary employeesGrossSalary1 = new EmployeesGrossSalary();
                                        if (!doc1.exists()) {
                                            //new month
                                            employeesGrossSalary1.setEmployeeId(employeesGrossSalary.getEmployeeId());
                                            employeesGrossSalary1.setBaseAllowances(baseAllowances);
                                            employeesGrossSalary1.setAllTypes(allTypes);
                                        } else {
                                            employeesGrossSalary1 = doc1.toObject(EmployeesGrossSalary.class);
                                            employeesGrossSalary1.getAllTypes().addAll(projectAllowances);
                                        }
                                        EMPLOYEE_GROSS_SALARY_COL.document(currEmployee.getId())
                                                .collection(year).document(month).set(employeesGrossSalary1, SetOptions.merge());
                                    });
                        });
                    }
                    break;
                case HOME:
                    summary.getProjectIds().put("HOME", CHECK_IN_FROM_HOME);
                    break;
                case OFFICE:
                    summary.getProjectIds().put(project.getId(), CHECK_IN_FROM_OFFICE);
                    break;
            }
        }
        lastProjectId = project.getId();
        summary.setCheckOut(null);
        summary.setLastProjectId(project.getId());
        SUMMARY_COL.document(id).collection(year + "-" + month).document(day).update("lastCheckInTime", summary.getLastCheckInTime(),
                "checkOut", null,
                "projectIds", summary.getProjectIds(),
                "lastProjectId", project.getId());
        //delete checkout shared pref
        setSummaryCache(summary);
        Snackbar.make(binding.getRoot(), "Checked In successfully!", Toast.LENGTH_SHORT).show();
        binding.checkInOutFab.setEnabled(true);
    }

    private void employeeCheckOut(Summary summary, HashMap<String, Object> checkOut, String projectId) {
        updateDate();
        long checkInTime = (summary.getLastCheckInTime()).getSeconds();
        long checkOutTime = Timestamp.now().getSeconds();
        long workingTime = (checkOutTime - checkInTime);
        //check if working time is greater than 8 hrs
        summary.setCheckOut(checkOut);
        if (summary.getWorkingTime().containsKey(projectId))
            summary.getWorkingTime().put(projectId, (long) summary.getWorkingTime().get(projectId) + workingTime);
        else summary.getWorkingTime().put(projectId, workingTime);
        summary.setLastProjectId(null);
        lastProjectId = null;
        SUMMARY_COL.document(id).collection(year + "-" + month).document(day)
                .update("lastProjectId", null, "checkOut", checkOut, "workingTime." + projectId, FieldValue.increment(workingTime))
                .addOnSuccessListener(unused -> {
                    //todo uncomment those lines when needed to calculate overTime
//                    SUMMARY_COL
//                            .document(id)
//                            .collection(year + "-" + month)
//                            .document(day)
//                            .get().addOnSuccessListener(doc -> {
//                                long workingTime1 = (long) doc.getData().get("workingTime");
//                                long overTime =(workingTime1 - 28800)<0?0:(workingTime1 - 28800)/3600;
//                                updateOverTime(overTime,doc.getReference().getPath(),(Timestamp) (summary.getCheckIn().get("Time")));
//                            });

                    if (checkInType != CheckInType.HOME)
                        binding.checkInOutFab.setEnabled(true);
                    if (checkInType == CheckInType.SITE) {
                        for (String pid : currEmployee.getProjectIds()) {
                            PROJECT_COL.document(pid).update("employeeWorkedTime." + currEmployee.getId(), FieldValue.increment(workingTime));
                        }
                    }
                });
        //save summary into shared preferences
        setSummaryCache(summary);
        Snackbar.make(binding.getRoot(), "Checked Out successfully!", Toast.LENGTH_SHORT).show();
    }


    private void employeeCheckIn(Summary summary, Project project) {
        updateDate();
        summary.setLastCheckInTime(Timestamp.now());
        summary.setLastProjectId(project.getId());
        HashMap<String, Object> checkInDetails = new HashMap<>(summary.getGeoMap());
        checkInDetails.put("Time", Timestamp.now());
        summary.setCheckIn(checkInDetails);
        HashMap<String, Object> checkIn = new HashMap<>();
        checkIn.put("checkIn", checkInDetails);
        checkIn.put("lastCheckInTime", summary.getLastCheckInTime());
        checkIn.put("lastProjectId", project.getId());
        lastProjectId = project.getId();
        switch (checkInType) {
            case HOME:
                checkIn.put("projectIds", new HashMap<String, String>() {{
                    put("HOME", CHECK_IN_FROM_HOME);
                }});
                SUMMARY_COL.document(id).collection(year + "-" + month).document(day).set(checkIn);
                return;
            case SITE:
                checkIn.put("projectIds", new HashMap<String, String>() {{
                    put(project.getId(), CHECK_IN_FROM_SITE);
                }});
                break;
            case OFFICE:
                checkIn.put("projectIds", new HashMap<String, String>() {{
                    put(project.getId(), CHECK_IN_FROM_OFFICE);
                }});
                SUMMARY_COL.document(id).collection(year + "-" + month).document(day).set(checkIn);
                return;
            case SUPPORT:
                checkIn.put("projectIds", new HashMap<String, String>() {{
                    put(project.getId(), CHECK_IN_FROM_SUPPORT);
                }});
                break;
        }
        SUMMARY_COL.document(id).collection(year + "-" + month).document(day).set(checkIn, SetOptions.merge());
        EMPLOYEE_GROSS_SALARY_COL.document(currEmployee.getId()).get().addOnCompleteListener(task -> {
            if (!task.isSuccessful() || task.getResult().getMetadata().isFromCache()) {
                //offline
                EmployeesGrossSalary employeesGrossSalary = getGrossSalaryFromCache();
                updateMonthGrossSalary(summary, project, employeesGrossSalary);
            } else {
                //online
                EmployeesGrossSalary employeesGrossSalary = task.getResult().toObject(EmployeesGrossSalary.class);
                updateMonthGrossSalary(summary, project, employeesGrossSalary);
            }
        });
    }


    private EmployeesGrossSalary getGrossSalaryFromCache() {
        Gson gson = new Gson();
        String json = CacheDirectory.readAllCachedText(getActivity(), "baseAllowances.json");
        return gson.fromJson(json, EmployeesGrossSalary.class);
    }

    private void updateMonthGrossSalary(Summary summary, Project project, EmployeesGrossSalary employeesGrossSalary) {
        ArrayList<Allowance> allowances = employeesGrossSalary.getAllTypes().stream().filter(allowance -> allowance.getType() != AllowancesEnum.NETSALARY.ordinal()).collect(Collectors.toCollection(ArrayList::new));
        allowances.removeIf(allowance -> allowance.getType() == AllowancesEnum.PROJECT.ordinal() && !allowance.getProjectId().equals(project.getId()));
        employeesGrossSalary.getAllTypes().removeIf(allowance -> allowance.getType() != AllowancesEnum.NETSALARY.ordinal());
        employeesGrossSalary.setBaseAllowances(allowances);
        if (checkInType == CheckInType.SITE) {
            for (Allowance allowance : employeesGrossSalary.getBaseAllowances()) {
                allowance.setNote(day);
                employeesGrossSalary.getAllTypes().add(allowance);
            }
        } else { //support
            for (Allowance allowance : employeesGrossSalary.getBaseAllowances()) {
                if (currEmployee.getProjectIds().contains(allowance.getProjectId()))
                    continue;
                allowance.setNote(day);
                employeesGrossSalary.getAllTypes().add(allowance);
            }
            for (Allowance allowance1 : project.getAllowancesList()) {
                allowance1.setNote(day);
                employeesGrossSalary.getAllTypes().add(allowance1);
            }
        }
        EMPLOYEE_GROSS_SALARY_COL.document(currEmployee.getId()).collection(year).document(month).set(employeesGrossSalary, SetOptions.merge());
        setSummaryCache(summary, employeesGrossSalary);
    }


    private void updateOverTime(long overTime, String path, Timestamp time) {
//        WriteBatch batch = db.batch();
//        batch.set(db.document(path), new HashMap<String, Object>() {{
//            put("overTime", overTime);
//        }}, SetOptions.merge());
//        batch.commit();
//        Allowance overTimeAllowance = new Allowance();
//        overTimeAllowance.setAmount(overTime * currEmployee.getOverTime());
//        overTimeAllowance.setName("overTime");
//        Date checkInDate = (time).toDate();
//        Calendar cal = Calendar.getInstance();
//        cal.setTime(checkInDate);
//        String prevDay = String.format("%02d", cal.get(Calendar.DAY_OF_MONTH));
//        overTimeAllowance.setNote(prevDay);
//        overTimeAllowance.setType(AllowancesEnum.OVERTIME.ordinal());
//        overTimeAllowance.setCurrency(currEmployee.getCurrency());
//        overTimeAllowance.setProjectId(currEmployee.getProjectID());
//        EMPLOYEE_GROSS_SALARY_COL.document(id).collection(year).document(month).get().addOnSuccessListener(doc1 -> {
//            EmployeesGrossSalary emp = doc1.toObject(EmployeesGrossSalary.class);
//            ArrayList<Allowance> allowanceArrayList = emp.getAllTypes();
//            if (allowanceArrayList != null) {
//                allowanceArrayList.removeIf(x -> x.getType() == AllowancesEnum.OVERTIME.ordinal() && x.getNote().trim().equals(day));
//            }
//            allowanceArrayList.add(overTimeAllowance);
//            EMPLOYEE_GROSS_SALARY_COL.document(id).collection(year).document(month).update("allTypes", allowanceArrayList);
//        });
    }


    private void setSummaryCache(Summary summary, EmployeesGrossSalary employeesGrossSalary) {
        Gson gson = new Gson();
        String summaryJson = gson.toJson(summary);
        CacheDirectory.writeAllCachedText(getActivity(), "summary.json", summaryJson);
        String grossSalaryJson = gson.toJson(employeesGrossSalary);
        CacheDirectory.writeAllCachedText(getActivity(), "grossSalary.json", grossSalaryJson);

    }

    private void setSummaryCache(Summary summary) {
        Gson gson = new Gson();
        String summaryJson = gson.toJson(summary);
        CacheDirectory.writeAllCachedText(getActivity(), "summary.json", summaryJson);
    }


    private Summary getSummaryCache() {
        Gson gson = new Gson();
        String summaryJson = CacheDirectory.readAllCachedText(getActivity(), "summary.json");
        if (summaryJson == null) return null;
        return gson.fromJson(summaryJson, Summary.class);
    }


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
        for (String pid : currEmployee.getProjectIds()) {
            PROJECT_COL.document(pid)
                    .update("machineWorkedTime." + currMachine.getReference(), FieldValue.increment(workingTime));
        }
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
                                AccessoriesDialog accessoriesDialog = new AccessoriesDialog(isItAUser, currMachine);
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

    // Listeners
    @SuppressLint("MissingPermission")
    private final View.OnClickListener oclCheckInOut = v -> {
        binding.checkInOutFab.setEnabled(false);
        PROJECT_COL.get().addOnSuccessListener(docs -> {
            final List<Project> projects = new ArrayList<>();
            projects.clear();
            if (docs.size() == 0) {
                projects.addAll(getProjectsFromCache());
            } else {
                projects.addAll(docs.toObjects(Project.class));
            }
            Locus.INSTANCE.getCurrentLocation(getActivity(), result -> {
                        if (result.getError() != null) {
                            Snackbar.make(binding.getRoot(), "can't complete the operation.", Snackbar.LENGTH_SHORT).show();
                            binding.checkInOutFab.setEnabled(true);
                            return null;
                        }
                        Location location = result.getLocation();
                        longitude = location.getLongitude();
                        latitude = location.getLatitude();
                        checkInType = CheckInType.HOME;
                        /*
                         *
                         * check-in given lastProjectId == null
                         * employee -> home / project / office
                         * manager -> home/ project / office / support
                         * check-out given lastProjectId != null && canCheckFromHome == false
                         * employee or manager -> same Project only
                         * recheck in given lastProjectId == null && canCheckFromHome == false
                         * employee -> project/ office
                         * manager --> project /office /support
                         * */
                        for (Project project : projects) {
                            if ((lastProjectId != null && !project.getId().equals(lastProjectId))) {
                                // if the user is not in the last project
                                // to prevent user from checking out from another project
                                continue;
                            }
                            double distance;
                            float[] results = new float[3];
                            Location.distanceBetween(latitude, longitude, project.getLat(), project.getLng(), results);
                            distance = results[0];
                            if (distance > project.getArea()) {
                                continue;
                            } else if (currEmployee.getProjectIds().contains(project.getId())) {
                                checkInType = CheckInType.SITE;
                            } else if (currEmployee.isManager()) {
                                if (project.getReference().equals(OFFICE_REF))
                                    checkInType = CheckInType.OFFICE;
                                else
                                    checkInType = CheckInType.SUPPORT;
                            } else {
                                // inside a project but not in the employee's project list
                                continue;
                            }
                            updateEmployeeSummary(latitude, longitude, project);
                            break;
                        }
                        if (checkInType == CheckInType.HOME) {
                            notifyLocation();
                        }
                        return null;
                    }
            );
        });
    };
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
    private final View.OnClickListener oclMachine = view -> {
        animationFab();
    };
}