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
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.igec_admin.Adatpers.EmployeeAdapter;
import com.example.igec_admin.R;
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
    long startDate, endDate;
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
        setStyle(DialogFragment.STYLE_NORMAL, R.style.FullscreenDialogTheme);
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

        vManagerIDLayout.setEnabled(true);
        vManagerID.setEnabled(false);

        getEmployees();

        ArrayAdapter<String> idAdapter = new ArrayAdapter<>(getActivity(), R.layout.dropdown_item, TeamID);
        vManagerID.setAdapter(idAdapter);
    }

    String convertDateToString(long selection) {
        @SuppressLint("SimpleDateFormat") SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy");
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(selection);
        return simpleDateFormat.format(calendar.getTime());
    }

    void ChangeSelectedTeam(int position) {


        if (!employees.get(position).isSelected) {
            employees.get(position).setManagerID(vManagerID.getText().toString());
            employees.get(position).setProjectId(project.getId());
            Team.add(employees.get(position));
            TeamID.add(String.valueOf(employees.get(position).getId()));

        } else {

            if (!vManagerID.getText().toString().isEmpty() && vManagerID.getText().toString().equals(employees.get(position).getId().toString()))
                vManagerID.setText("");
            Team.remove(employees.get(position));
            TeamID.remove(String.valueOf(employees.get(position).getId()));
            employees.get(position).setManagerID(null);
            employees.get(position).setProjectId(null);
        }
        vManagerIDLayout.setEnabled(Team.size() >= 2);
        vManagerID.setEnabled(false);
        if (!vManagerIDLayout.isEnabled())
            vManagerID.setText("");
        if (TeamID.size() > 0) {
            ArrayAdapter<String> idAdapter = new ArrayAdapter<>(getActivity(), R.layout.dropdown_item, TeamID);
            vManagerID.setAdapter(idAdapter);

        }

        employees.get(position).isSelected = !employees.get(position).isSelected;
        adapter.notifyItemChanged(position);
    }

    void getEmployees() {
//        employees.addAll(project.getEmployees());
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
        currProjectID = projectID;
        for (EmployeeOverview empOverview : employees) {
            ArrayList<String> empInfo = new ArrayList<>();
            if (isDeleted || empOverview.getProjectId() == null) {
                empOverview.setManagerID(null);
                currProjectID = null;
                Team.remove(empOverview);
            }
            if (empOverview.getId().equals(vManagerID.getText().toString())) {
                empOverview.setManagerID("adminID");
            } else if (empOverview.getProjectId()!= null && empOverview.getProjectId().equals(projectID)){
                empOverview.setManagerID(vManagerID.getText().toString());
            }
            empInfo.add(empOverview.getFirstName());
            empInfo.add(empOverview.getLastName());
            empInfo.add(empOverview.getTitle());
            empInfo.add(empOverview.getManagerID());
            empInfo.add(empOverview.getProjectId());
            Map<String, Object> empInfoMap = new HashMap<>();
            empInfoMap.put(empOverview.getId(), empInfo);
            employeeOverviewRef.update(empInfoMap);
            employeeCol.document(empOverview.getId()).update("managerID", empOverview.getManagerID(), "projectID", currProjectID);
        }
    }


    private void updateTeam() {
        for (EmployeeOverview emp : Team) {
            if (emp.getId().equals(vManagerID.getText().toString())) {
                emp.setManagerID("adminID");
            } else
                emp.setManagerID(vManagerID.getText().toString());
            emp.setProjectId(project.getId());
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    void retrieveEmployees(Map<String, ArrayList<String>> empMap) {

        for (String key : empMap.keySet()) {
            String firstName = empMap.get(key).get(0);
            String lastName = empMap.get(key).get(1);
            String title = empMap.get(key).get(2);
            String managerID = empMap.get(key).get(3);
            String projectID = empMap.get(key).get(4);
            String id = (key);
            EmployeeOverview newEmp = new EmployeeOverview(firstName, lastName, title, id, projectID);
            if (managerID == null) {
                employees.add(newEmp);
            } else if (managerID.equals(vManagerID.getText().toString()) || id.equals(vManagerID.getText().toString())) {
                newEmp.setManagerID(vManagerID.getText().toString());
                newEmp.isSelected = true;
                Team.add(newEmp);
                employees.add(newEmp);
                TeamID.add(newEmp.getId());
            }
        }
        adapter.setEmployeeOverviewsList(employees);
        adapter.notifyDataSetChanged();

    }


    boolean validateInputs() {
        return
                !(vName.getText().toString().isEmpty() ||
                        vLocation.getText().toString().isEmpty() ||
                        vManagerID.getText().toString().isEmpty() ||
                        vManagerName.getText().toString().isEmpty() ||
                        vStartTime.getText().toString().isEmpty() ||
                        vEndTime.getText().toString().isEmpty());
    }

    void updateProject() {

        updateTeam();
        HashMap<String, Object> updatedProjectData = new HashMap<>();
        updatedProjectData.put("estimatedEndDate", new Date(endDate));
        updatedProjectData.put("startDate", new Date(startDate));
        updatedProjectData.put("name", vName.getText().toString());
        updatedProjectData.put("manager", vManagerName.getText().toString());
        updatedProjectData.put("managerID", vManagerID.getText().toString());
        updatedProjectData.put("location", vLocation.getText().toString());
        updateEmployeesDetails(project.getId());
        updatedProjectData.put("employees", Team);
        db.collection("projects").document(project.getId()).update(updatedProjectData).addOnSuccessListener(unused -> {
            Toast.makeText(getActivity(), "Updated", Toast.LENGTH_SHORT).show();
            project.setEmployees(null);
            employees.clear();
            Team.clear();
            TeamID.clear();
            dismiss();
        });

    }

    void deleteProject() {
        currProjectID = project.getId();
        db.collection("projects").document(project.getId()).delete().addOnSuccessListener(unused -> {
            isDeleted = true;
            updateEmployeesDetails(currProjectID);
            Toast.makeText(getActivity(), "Deleted", Toast.LENGTH_SHORT).show();
            dismiss();
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
            vStartTime.setText(convertDateToString((long) selection));
            startDate = (long) selection;
        }
    };
    MaterialPickerOnPositiveButtonClickListener pclEndDatePicker = new MaterialPickerOnPositiveButtonClickListener() {
        @Override
        public void onPositiveButtonClick(Object selection) {
            vEndTime.setText(convertDateToString((long) selection));
            endDate = (long) selection;
        }
    };
    View.OnClickListener clUpdate = v -> {
        if (validateInputs()) {
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
