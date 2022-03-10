package com.example.igec_admin.Dialogs;

import static android.content.ContentValues.TAG;

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
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.igec_admin.Adatpers.EmployeeAdapter;
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
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ProjectFragmentDialog extends DialogFragment {
    // Views
    private TextInputEditText vName, vLocation, vStartTime, vEndTime, vManagerName;
    private MaterialButton vRegister, vUpdate, vDelete;
    private AutoCompleteTextView vManagerID;
    private TextInputLayout vManagerIDLayout, vStartTimeLayout, vEndTimeLayout;
    private RecyclerView recyclerView;
    private EmployeeAdapter adapter;
    private RecyclerView.LayoutManager layoutManager;

    // Vars
    MaterialDatePicker.Builder<Long> vStartDatePickerBuilder = MaterialDatePicker.Builder.datePicker();
    MaterialDatePicker vStartDatePicker;
    MaterialDatePicker.Builder<Long> vEndDatePickerBuilder = MaterialDatePicker.Builder.datePicker();
    MaterialDatePicker vEndDatePicker;
    ArrayList<EmployeeOverview> employees = new ArrayList<>();
    ArrayList<String> TeamID = new ArrayList<>();
    ArrayList<EmployeeOverview> Team = new ArrayList<>();
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    DocumentReference employeeOverviewRef = db.collection("EmployeeOverview")
            .document("emp");
    CollectionReference employeeCol = db.collection("employees");
    Project project;
    private Boolean isDeleted = false;
    private String currProjectID;

    public ProjectFragmentDialog(Project project) {
        this.project = project;
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
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL,R.style.FullscreenDialogTheme);
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_project, container, false);
        Initialize(view);

        // listeners
        vUpdate.setOnClickListener(clUpdate);
        vDelete.setOnClickListener(clDelete);
        vManagerID.addTextChangedListener(twManagerID);
        vStartDatePicker.addOnPositiveButtonClickListener(pclStartDatePicker);
        vEndDatePicker.addOnPositiveButtonClickListener(pclEndDatePicker);
        vStartTimeLayout.setEndIconOnClickListener(oclStartDate);
        vEndTimeLayout.setEndIconOnClickListener(oclEndDate);


        // Inflate the layout for this fragment
        return view;
    }

    // Functions
    private void Initialize(View view) {
        vName = view.findViewById(R.id.TextInput_ProjectName);
        vLocation = view.findViewById(R.id.TextInput_Location);
        vStartTime = view.findViewById(R.id.TextInput_StartTime);
        vEndTime = view.findViewById(R.id.TextInput_EndTime);
        vStartTimeLayout = view.findViewById(R.id.textInputLayout_StartTime);
        vEndTimeLayout = view.findViewById(R.id.textInputLayout_EndTime);
        vManagerID = view.findViewById(R.id.TextInput_ManagerID);
        vManagerIDLayout = view.findViewById(R.id.textInputLayout_ManagerID);
        vManagerName = view.findViewById(R.id.TextInput_ManagerName);
        vRegister = view.findViewById(R.id.button_register);
        vUpdate = view.findViewById(R.id.button_update);
        vDelete = view.findViewById(R.id.button_delete);

        vRegister.setVisibility(View.GONE);
        vDelete.setVisibility(View.VISIBLE);
        vUpdate.setVisibility(View.VISIBLE);

        vStartDatePickerBuilder.setTitleText("Start Date");
        vStartDatePicker = vStartDatePickerBuilder.build();
        vEndDatePickerBuilder.setTitleText("End Date");
        vEndDatePicker = vEndDatePickerBuilder.build();
        recyclerView = view.findViewById(R.id.recyclerview);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(getActivity());
        adapter = new EmployeeAdapter(employees, true);
        adapter.setOnItemClickListener(itclEmployeeAdapter);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);


        vName.setText(project.getName());
        vLocation.setText(project.getLocation());
        vManagerID.setText(project.getManagerID());
        vManagerName.setText(project.getManagerName());
        vStartTime.setText(convertDateToString(project.getStartDate().getTime()));
        vEndTime.setText(convertDateToString(project.getEstimatedEndDate().getTime()));

        vManagerID.setEnabled(false);

        getEmployees();
    }

    String convertDateToString(long selection)
    {
        @SuppressLint("SimpleDateFormat") SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy");
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(selection);
        return simpleDateFormat.format(calendar.getTime());
    }

    void ChangeSelectedTeam(int position) {
        //TODO modify to add to the already employeeList for the project

        employees.get(position).setSelected(!employees.get(position).getSelected());
        if (employees.get(position).getSelected()) {
            Team.add(employees.get(position));
            TeamID.add(String.valueOf(employees.get(position).getId()));
        } else {
            Team.remove(employees.get(position));
            TeamID.remove(String.valueOf(employees.get(position).getId()));
        }
        adapter.notifyItemChanged(position);
    }

    void getEmployees() {
        employees = project.getEmployees();
        employeeOverviewRef
                .addSnapshotListener((documentSnapshot, e) -> {
                    if (e != null) {
                        Log.w(TAG, "Listen failed.", e);
                        return;
                    }
                    if (documentSnapshot.exists()) {
                        Map<String, ArrayList<String>> empMap;
                        empMap = (HashMap) documentSnapshot.getData();
                            retrieveEmployees(empMap);
                    }
                });
    }
    private void updateEmployeesDetails(String projectID) {
       currProjectID=projectID;
        for (EmployeeOverview empOverview : employees) {
                ArrayList<String> empInfo = new ArrayList<>();
                if(isDeleted || !empOverview.getSelected()){
                    empOverview.setManagerID(null);
                    currProjectID=null;
                    Team.remove(empOverview);
                }
                if (empOverview.getId().equals(vManagerID.getText().toString())) {
                    empOverview.setManagerID("adminID");
                } else {
                    empOverview.setManagerID(vManagerID.getText().toString());
                }
                empInfo.add(empOverview.getFirstName());
                empInfo.add(empOverview.getLastName());
                empInfo.add(empOverview.getTitle());
                empInfo.add(empOverview.getManagerID());
                Map<String, Object> empInfoMap = new HashMap<>();
                empInfoMap.put(empOverview.getId(), empInfo);
                employeeOverviewRef.update( empInfoMap);
                employeeCol.document(empOverview.getId()).update("managerID", empOverview.getManagerID(), "projectID", currProjectID);
        }
    }


    private void updateTeam() {
        for (EmployeeOverview emp : Team) {
            if (emp.getId().equals(vManagerID.getText().toString())) {
                emp.setManagerID("adminID");
            } else
                emp.setManagerID(vManagerID.getText().toString());
        }
    }


    Date convertStringDate(String sDate) {
        @SuppressLint("SimpleDateFormat") SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
        try {
            return format.parse(sDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    @SuppressLint("NotifyDataSetChanged")
    void retrieveEmployees(Map<String, ArrayList<String>> empMap) {

        for (String key : empMap.keySet()) {
            String firstName = empMap.get(key).get(0);
            String lastName = empMap.get(key).get(1);
            String title = empMap.get(key).get(2);
            String managerID = empMap.get(key).get(3);
            String id = (key);
            if(managerID == null){
                EmployeeOverview newEmp = new EmployeeOverview(firstName, lastName, title, id);
                newEmp.setManagerID(managerID);
                employees.add(newEmp);
            }
        }
        adapter.setEmployeeOverviewsList(employees);
        adapter.notifyDataSetChanged();

    }

    void ClearInputs() {
        vName.setText(null);
        vLocation.setText(null);
        vManagerID.setText(null);
        vManagerName.setText(null);
        vStartTime.setText(null);
        vEndTime.setText(null);
    }

    boolean ValidateInputs() {
        return
                !(vName.getText().toString().isEmpty() ||
                        vLocation.getText().toString().isEmpty() ||
                        vManagerID.toString().isEmpty() ||
                        vManagerName.toString().isEmpty() ||
                        vStartTime.toString().isEmpty() ||
                        vEndTime.toString().isEmpty());
    }
    void updateProject(){
        Date startDate = convertStringDate(vStartTime.getText().toString());
        Date endDate = convertStringDate(vEndTime.getText().toString());
        updateTeam();
        HashMap<String,Object>updatedProjectData= new HashMap<>();
        updatedProjectData.put("estimatedEndDate",endDate);
        updatedProjectData.put("startDate",startDate);
        updatedProjectData.put("name",vName.getText().toString());
        updatedProjectData.put("manager",vManagerName.getText().toString());
        updatedProjectData.put("managerID",vManagerID.getText().toString());
        updatedProjectData.put("location",vLocation.getText().toString());
        updateEmployeesDetails(project.getId());
        updatedProjectData.put("employees",Team);
        db.collection("projects").document(project.getId()).update(updatedProjectData);

    }

    void deleteProject(){
        currProjectID = project.getId();
        db.collection("projects").document(project.getId()).delete().addOnSuccessListener(unused -> {
            isDeleted=true;
            updateEmployeesDetails(currProjectID);
        });

    }

    // Listeners
    TextWatcher twManagerID = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            if (vManagerID.getText().length() > 0) {
                int position = 0;
                for (int i = 0; i < Team.size(); i++) {
                    if (String.valueOf(Team.get(i).getId()).equals(s.toString())) {
                        position = i;

                    }
                }
                vManagerName.setText(Team.get(position).getFirstName() + " " + Team.get(position).getLastName());
            } else
                vManagerName.setText(null);
        }
    };
    View.OnClickListener oclStartDate = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            vStartDatePicker.show(getParentFragmentManager(), "DATE_PICKER");
        }
    };
    View.OnClickListener oclEndDate = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            vEndDatePicker.show(getParentFragmentManager(), "DATE_PICKER");
        }
    };
    MaterialPickerOnPositiveButtonClickListener pclStartDatePicker = new MaterialPickerOnPositiveButtonClickListener() {
        @Override
        public void onPositiveButtonClick(Object selection) {
            @SuppressLint("SimpleDateFormat") SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy");
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis((long) selection);
            vStartTime.setText(simpleDateFormat.format(calendar.getTime()));
        }
    };
    MaterialPickerOnPositiveButtonClickListener pclEndDatePicker = new MaterialPickerOnPositiveButtonClickListener() {
        @Override
        public void onPositiveButtonClick(Object selection) {
            @SuppressLint("SimpleDateFormat") SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy");
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis((long) selection);
            vEndTime.setText(simpleDateFormat.format(calendar.getTime()));
        }
    };
    View.OnClickListener clUpdate = v -> {
        if (ValidateInputs()) {
            updateProject();
        } else {
            Toast.makeText(getActivity(), "please, fill the project data", Toast.LENGTH_SHORT).show();
        }
    };
    View.OnClickListener clDelete = v -> deleteProject();

    EmployeeAdapter.OnItemClickListener itclEmployeeAdapter = new EmployeeAdapter.OnItemClickListener() {
        @Override
        public void onItemClick(int position) {
            ChangeSelectedTeam(position);
        }

        @Override
        public void onCheckboxClick(int position) {
            ChangeSelectedTeam(position);
        }
    };

}
