package com.example.igec_admin.Dialogs;

import static com.example.igec_admin.cryptography.RSAUtil.encrypt;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.igec_admin.R;
import com.example.igec_admin.fireBase.Employee;
import com.example.igec_admin.fireBase.EmployeeOverview;
import com.example.igec_admin.fireBase.Project;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class UserFragmentDialog  extends DialogFragment {

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL,R.style.FullscreenDialogTheme);
    }
    //Var
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    CollectionReference machineEmployeeCol =  db.collection("Machine_Employee");
    CollectionReference vacationCol = db.collection("Vacation");
    CollectionReference projectCol = db.collection("projects");
    CollectionReference summaryCOl = db.collection("summary");
    DocumentReference employeeOverviewRef = db.collection("EmployeeOverview").document("emp");
    Employee employee;
    ArrayList<EmployeeOverview> employeeOverviewArrayList;
    int currEmpOverviewPos;

    // Views
    MaterialButton vUpdate,vDelete,vRegister;
    TextInputEditText vFirstName;
    TextInputEditText vSecondName;
    TextInputEditText vEmail;
    TextInputLayout vEmailLayout;
    TextInputEditText vPassword;
    TextInputEditText vTitle;
    TextInputEditText vSalary;
    TextInputEditText vSSN;
    TextInputEditText vArea;
    TextInputEditText vCity;
    TextInputEditText vStreet;
    TextInputEditText vHireDate;
    TextInputLayout vHireDateLayout;
    MaterialDatePicker.Builder<Long> vDatePickerBuilder = MaterialDatePicker.Builder.datePicker();
    MaterialDatePicker vDatePicker;
    public UserFragmentDialog(Employee employee, ArrayList<EmployeeOverview> employeeOverviewArrayList,int currEmpOverviewPos) {
        this.employee = employee;
        this.employeeOverviewArrayList = employeeOverviewArrayList;
        this.currEmpOverviewPos = currEmpOverviewPos;
    }
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {


        Dialog dialog = super.onCreateDialog(savedInstanceState);
        Window window = dialog.getWindow();

        if (window != null) {
            dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);
        }

        return dialog;
    }
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_user, container, false);
        Initialize(view);
        // Listeners
        vEmail.addTextChangedListener(twEmail);
        vDatePicker.addOnPositiveButtonClickListener(pclDatePicker);
        vHireDateLayout.setEndIconOnClickListener(oclHireDate);
        vUpdate.setOnClickListener(clUpdate);
        vDelete.setOnClickListener(clDelete);
        return view;
    }

    // Functions
    private void Initialize(View view)
    {
        vFirstName = view.findViewById(R.id.TextInput_FirstName);
        vSecondName = view.findViewById(R.id.TextInput_SecondName);
        vEmail = view.findViewById(R.id.TextInput_Email);
        vEmailLayout = view.findViewById(R.id.textInputLayout_Email);
        vPassword = view.findViewById(R.id.TextInput_Password);
        vTitle = view.findViewById(R.id.TextInput_Title);
        vSalary = view.findViewById(R.id.TextInput_Salary);
        vSSN = view.findViewById(R.id.TextInput_SNN);
        vArea = view.findViewById(R.id.TextInput_Area);
        vCity = view.findViewById(R.id.TextInput_City);
        vStreet = view.findViewById(R.id.TextInput_Street);
        vHireDate = view.findViewById(R.id.TextInput_HireDate);
        vHireDateLayout = view.findViewById(R.id.textInputLayout_HireDate);
        vDatePickerBuilder.setTitleText("Hire Date");
        vDatePicker = vDatePickerBuilder.build();
        vRegister = view.findViewById(R.id.button_register);
        vDelete = view.findViewById(R.id.button_delete);
        vUpdate = view.findViewById(R.id.button_update);

        vRegister.setVisibility(View.GONE);
        vDelete.setVisibility(View.VISIBLE);
        vUpdate.setVisibility(View.VISIBLE);

        vFirstName.setText(employee.getFirstName());
        vSecondName.setText(employee.getLastName());
        vTitle.setText(employee.getTitle());
        vArea.setText(employee.getArea());
        vCity.setText(employee.getCity());
        vStreet.setText(employee.getStreet());
        vEmail.setText(employee.getEmail());
        vPassword.setText(employee.getDecryptedPassword());
        vSalary.setText(String.valueOf(employee.getSalary()));
        vSSN.setText(employee.getSSN());
        vHireDate.setText(convertDateToString(employee.getHireDate().getTime()));

    }


    void ClearInputs() {
        vFirstName.setText(null);
        vSecondName.setText(null);
        vEmail.setText(null);
        vPassword.setText(null);
        vTitle.setText(null);
        vSalary.setText(null);
        vArea.setText(null);
        vCity.setText(null);
        vStreet.setText(null);
        vHireDate.setText(null);
        vSSN.setText(null);
    }

    boolean ValidateInputs() {
        return
                !(vFirstName.getText().toString().isEmpty() ||
                        vSecondName.getText().toString().isEmpty() ||
                        vEmail.toString().isEmpty() ||
                        vPassword.toString().isEmpty() ||
                        vTitle.toString().isEmpty() ||
                        vSalary.getText().toString().isEmpty() ||
                        vArea.getText().toString().isEmpty() ||
                        vCity.toString().isEmpty() ||
                        vStreet.toString().isEmpty() ||
                        vHireDate.toString().isEmpty() ||
                        vSSN.toString().isEmpty());
    }
    private void updateEmployeeOverview(Map<String, Object> updatedEmpOverviewMap) {
       employeeOverviewRef.update(updatedEmpOverviewMap);
    }
    private void updateMachineEmployee(HashMap<String,Object>updatedEmployeeMap) {
        machineEmployeeCol.whereEqualTo("Employee.id",employee.getId()).get().addOnSuccessListener(queryDocumentSnapshots -> {
            for (QueryDocumentSnapshot d :  queryDocumentSnapshots){
                machineEmployeeCol.document(d.getId()).update("Employee",updatedEmployeeMap);
            }
        });
    }
    private void updateVacation(HashMap<String, Object> updatedEmployeeMap) {
        vacationCol.whereEqualTo("employee.id",employee.getId()).get().addOnSuccessListener(queryDocumentSnapshots ->{
            for (QueryDocumentSnapshot d :  queryDocumentSnapshots){
                vacationCol.document(d.getId()).update("employee",updatedEmployeeMap);
            }
        } );
        vacationCol.whereEqualTo("manager.id",employee.getId()).get().addOnSuccessListener(queryDocumentSnapshots ->{
            for (QueryDocumentSnapshot d :  queryDocumentSnapshots){
                vacationCol.document(d.getId()).update("manager",updatedEmployeeMap);
            }
        } );
    }
    private void updateSummary(HashMap<String,Object> updatedEmployeeMap) {
        summaryCOl.whereEqualTo("Employee.id",employee.getId()).get().addOnSuccessListener(queryDocumentSnapshots ->{
            for (QueryDocumentSnapshot d :  queryDocumentSnapshots){
                summaryCOl.document(d.getId()).update("Employee",updatedEmployeeMap);
            }
        } );
    }
    private void updateProjects() {
        EmployeeOverview tempEmp = new EmployeeOverview(vFirstName.getText().toString(),vSecondName.getText().toString(),vTitle.getText().toString(),employee.getId());
        tempEmp.setManagerID(employee.getManagerID());
        employeeOverviewArrayList.set(currEmpOverviewPos,tempEmp);
        projectCol.document(employee.getProjectID()).get().addOnSuccessListener(documentSnapshot -> {
            Project currProject = documentSnapshot.toObject(Project.class);
            ArrayList<EmployeeOverview> temp = currProject.getEmployees();
            temp.remove(new EmployeeOverview(employee.getFirstName(),employee.getLastName(),employee.getTitle(),employee.getId()));
            temp.add(tempEmp);
            currProject.setEmployees(temp);
            projectCol.document(employee.getProjectID()).update("employees",temp);
        });
    }

    private HashMap<String, Object> fillEmployeeData() {
        HashMap<String,Object>empMap = new HashMap<>();
        empMap.put("area",vArea.getText().toString());
        empMap.put("street",vStreet.getText().toString());
        empMap.put("city",vCity.getText().toString());
        empMap.put("email",vEmail.getText().toString());
        empMap.put("firstName",vFirstName.getText().toString());
        empMap.put("lastName",vSecondName.getText().toString());
        empMap.put("password",encryptedPassword());
        empMap.put("title",vTitle.getText().toString());
        empMap.put("salary",Double.parseDouble(vSalary.getText().toString()));
        empMap.put("SSN",vSSN.getText().toString());
        empMap.put("hireDate",convertStringDate(vHireDate.getText().toString()));
        empMap.put("id",employee.getId());
        return empMap;
    }
    private String encryptedPassword() {
        try {
            return Base64.getEncoder().encodeToString(encrypt(vPassword.getText().toString()));
        } catch (Exception e) {
            Log.e("error in encryption",e.toString());
            return null;
        }
    }
    Date convertStringDate(String sDate){
        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
        try {
            Date date = format.parse(sDate);
            return date;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    String convertDateToString(long selection)
    {
        @SuppressLint("SimpleDateFormat") SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy");
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(selection);
        return simpleDateFormat.format(calendar.getTime());
    }
    private void deleteEmployee() {
        db.collection("employees").document(employee.getId()).delete().addOnSuccessListener(unused -> {
           employeeOverviewRef.update(employee.getId(),null);
           projectCol.document(employee.getProjectID()).get().addOnSuccessListener(documentSnapshot -> {
               Project currProject = documentSnapshot.toObject(Project.class);
               ArrayList<EmployeeOverview> temp = currProject.getEmployees();
               temp.remove(new EmployeeOverview(employee.getFirstName(),employee.getLastName(),employee.getTitle(),employee.getId()));
               currProject.setEmployees(temp);
               projectCol.document(employee.getProjectID()).update("employees",temp);
           });
           vacationCol.whereEqualTo("employee.id",employee.getId()).whereEqualTo("vacationStatus",0).get().addOnSuccessListener(documentQuerey->{
               for(QueryDocumentSnapshot d : documentQuerey){
                   vacationCol.document(d.getId()).delete();
               }
           });
        });
    }

    // Listeners
    View.OnClickListener clUpdate = v -> {
        String id = employee.getId();
        Map<String, Object> updatedEmpOverviewMap = new HashMap<>();
        ArrayList<String> empInfo = new ArrayList<>();
        empInfo.add((vFirstName.getText()).toString());
        empInfo.add((vSecondName.getText()).toString());
        empInfo.add((vTitle.getText()).toString());
        empInfo.add((employee.getId()));
        updatedEmpOverviewMap.put(id,empInfo);
        HashMap<String,Object> updatedEmployeeMap = fillEmployeeData();
        db.collection("employees").document(id).update(updatedEmployeeMap).addOnSuccessListener(unused->{
           updateEmployeeOverview(updatedEmpOverviewMap);
           updateMachineEmployee(updatedEmployeeMap);
           updateVacation(updatedEmployeeMap);
           updateSummary(updatedEmployeeMap);
           updateProjects();
        });
    };
    View.OnClickListener clDelete = v -> {
        deleteEmployee();
    };



    View.OnClickListener oclHireDate =  new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(!vDatePicker.isVisible())
                vDatePicker.show(getFragmentManager(),"DATE_PICKER");
        }
    };
    MaterialPickerOnPositiveButtonClickListener pclDatePicker =  new MaterialPickerOnPositiveButtonClickListener() {
        @Override
        public void onPositiveButtonClick(Object selection) {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy");
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis((long) selection);
            vHireDate.setText(simpleDateFormat.format(calendar.getTime()));
        }
    };
    TextWatcher twEmail = new TextWatcher() {
        private final Pattern mPattern = Pattern.compile("(?:[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*|\"(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\\[(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?|[a-z0-9-]*[a-z0-9]:(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)\\])");

        private boolean isValid(CharSequence s)
        {
            return s.toString().equals("") || mPattern.matcher(s).matches();
        }
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            if(!isValid(s))
            {
                vEmailLayout.setError("Wrong E-mail form");
            }
            else
            {
                vEmailLayout.setError(null);
            }
        }
    };


}