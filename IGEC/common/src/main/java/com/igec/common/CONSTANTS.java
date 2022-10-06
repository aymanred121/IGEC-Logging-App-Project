package com.igec.common;

import android.content.Context;
import android.content.Intent;
import android.provider.Settings;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class CONSTANTS {
    private static final FirebaseFirestore db = FirebaseFirestore.getInstance();
    public static final int CAMERA_REQUEST_CODE = 123;
    public static final int LOCATION_REQUEST_CODE = 155;
    public static final String IGEC = "IGEC";
    public static final String ID = "ID";
    public static final String LOGGED = "LOGGED";
    public static final String ADMIN = "adminID";
    public static final String VACATION_STATUS_CHANNEL_ID = "VACATION_STATUS";
    public static final String TRANSFER_STATUS_CHANNEL_ID = "TRANSFER_STATUS";
    public static final String VACATION_REQUEST_CHANNEL_ID = "VACATION_REQUEST";
    public static final String TRANSFER_REQUEST_CHANNEL_ID = "TRANSFER_REQUEST";
    public static final String CHECK_IN_FROM_OFFICE = "office";
    public static final String CHECK_IN_FROM_SITE = "site";
    public static final String CHECK_IN_FROM_HOME = "from_home";
    public static final String CHECK_IN_FROM_SUPPORT = "support";
    public static final DocumentReference EMPLOYEE_OVERVIEW_REF = db.collection("EmployeeOverview").document("emp");
    public static final CollectionReference MACHINE_EMPLOYEE_COL = db.collection("Machine_Employee");
    public static final CollectionReference VACATION_COL = db.collection("Vacation");
    public static final CollectionReference MACHINE_COL = db.collection("machine");
    public static final CollectionReference PROJECT_COL = db.collection("projects");
    public static final CollectionReference EMPLOYEE_COL = db.collection("employees");
    public static final CollectionReference EMPLOYEE_GROSS_SALARY_COL = db.collection("EmployeesGrossSalary");
    public static final CollectionReference TRANSFER_REQUESTS_COL = db.collection("TransferRequests");
    public static final CollectionReference MACHINE_DEFECT_LOG_COL = db.collection("MachineDefectsLog");
    public static final CollectionReference SUMMARY_COL = db.collection("summary");
    }
