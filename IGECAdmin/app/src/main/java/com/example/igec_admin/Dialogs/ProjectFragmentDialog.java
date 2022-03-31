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
import androidx.core.util.Pair;
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
    private TextInputEditText vName, vArea, vStreet, vCity, vTime, vManagerName;
    private MaterialButton vRegister, vUpdate, vDelete;
    private AutoCompleteTextView vManagerID;
    private TextInputLayout vManagerIDLayout, vTimeLayout;
    private RecyclerView recyclerView;
    private EmployeeAdapter adapter;
    private RecyclerView.LayoutManager layoutManager;

    // Vars
    long startDate, endDate;
    private MaterialDatePicker.Builder<Pair<Long, Long>> vTimeDatePickerBuilder = MaterialDatePicker.Builder.dateRangePicker();
    private MaterialDatePicker vTimeDatePicker;
    ArrayList<EmployeeOverview> employees = new ArrayList<>();
    ArrayList<String> TeamID = new ArrayList<>();
    ArrayList<EmployeeOverview> Team = new ArrayList<>();
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    DocumentReference employeeOverviewRef = db.collection("EmployeeOverview")
            .document("emp");
    CollectionReference employeeCol = db.collection("employees");
    Project project;
    private EmployeeOverview selectedManager;
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
        vTimeDatePicker.addOnPositiveButtonClickListener(pclTimeDatePicker);
        vTimeLayout.setEndIconOnClickListener(oclStartDate);


        // Inflate the layout for this fragment
        return view;
    }

    // Functions
    private void Initialize(View view) {
        vName = view.findViewById(R.id.TextInput_ProjectName);
        vCity = view.findViewById(R.id.TextInput_City);
        vStreet = view.findViewById(R.id.TextInput_Street);
        vArea = view.findViewById(R.id.TextInput_Area);
        vTime = view.findViewById(R.id.TextInput_Time);
        vTimeLayout = view.findViewById(R.id.textInputLayout_Time);
        vManagerID = view.findViewById(R.id.TextInput_ManagerID);
        vManagerIDLayout = view.findViewById(R.id.textInputLayout_ManagerID);
        vManagerName = view.findViewById(R.id.TextInput_ManagerName);
        vRegister = view.findViewById(R.id.button_register);
        vUpdate = view.findViewById(R.id.button_update);
        vDelete = view.findViewById(R.id.button_delete);

        vRegister.setVisibility(View.GONE);
        vDelete.setVisibility(View.VISIBLE);
        vUpdate.setVisibility(View.VISIBLE);


        recyclerView = view.findViewById(R.id.recyclerview);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(getActivity());
        adapter = new EmployeeAdapter(employees, true);
        adapter.setOnItemClickListener(itclEmployeeAdapter);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);


        vName.setText(project.getName());
        vManagerID.setText(project.getManagerID());
        vManagerName.setText(project.getManagerName());
        vManagerIDLayout.setEnabled(true);
        vManagerID.setEnabled(false);
        startDate = project.getStartDate().getTime();
        endDate = project.getEstimatedEndDate().getTime();
        vTimeDatePickerBuilder.setTitleText("Time");
        vTimeDatePicker = vTimeDatePickerBuilder.setSelection(new Pair<>(startDate, endDate)).build();
        vTime.setText(String.format("%s to %s", convertDateToString(startDate), convertDateToString(endDate)));


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

    private void ChangeSelectedTeam(int position) {

        employees.get(position).isSelected = !employees.get(position).isSelected;
        if (employees.get(position).isSelected) {
            employees.get(position).setProjectId(project.getId());
            Team.add(employees.get(position));
            TeamID.add(String.valueOf(employees.get(position).getId()));

        } else {

            if (!vManagerID.getText().toString().isEmpty() && vManagerID.getText().toString().equals(employees.get(position).getId().toString()))
                vManagerID.setText("");
            Team.remove(employees.get(position));
            TeamID.remove(String.valueOf(employees.get(position).getId()));
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
        adapter.notifyItemChanged(position);
    }

    void getEmployees() {
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

        for (EmployeeOverview empOverview : employees) {
            currProjectID = projectID;
            ArrayList<String> empInfo = new ArrayList<>();
            if (isDeleted || empOverview.getProjectId() == null) {
                empOverview.setManagerID(null);
                currProjectID = null;
                Team.remove(empOverview);
            }
            if (empOverview.getId().equals(vManagerID.getText().toString())) {
                empOverview.setManagerID("adminID");
            } else if (empOverview.getProjectId() != null && empOverview.getProjectId().equals(projectID)) {
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
            if (projectID == null) {
                employees.add(newEmp);
            } else if (managerID.equals(project.getManagerID()) || id.equals(project.getManagerID())) {
                newEmp.setManagerID(id.equals(project.getManagerID()) ? "adminID" : vManagerID.getText().toString());
                newEmp.isSelected = true;
                if (id.equals(project.getManagerID()))
                    selectedManager = newEmp;
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
                        vArea.getText().toString().isEmpty() ||
                        vCity.getText().toString().isEmpty() ||
                        vStreet.getText().toString().isEmpty() ||
                        vManagerID.getText().toString().isEmpty() ||
                        vManagerName.getText().toString().isEmpty() ||
                        vTime.getText().toString().isEmpty());
    }

    void updateProject() {

        HashMap<String, Object> updatedProjectData = new HashMap<>();
        updatedProjectData.put("estimatedEndDate", new Date(endDate));
        updatedProjectData.put("startDate", new Date(startDate));
        updatedProjectData.put("name", vName.getText().toString());
        updatedProjectData.put("managerName", vManagerName.getText().toString());
        updatedProjectData.put("managerID", vManagerID.getText().toString());
        updatedProjectData.put("location", "");
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
                if (selectedManager == null || !selectedManager.getId().equals(vManagerID.getText().toString())) {
                    for (int i = 0; i < Team.size(); i++) {
                        if (String.valueOf(Team.get(i).getId()).equals(s.toString())) {
                            selectedManager = Team.get(i);
                        }
                    }
                }
                vManagerName.setText(String.format("%s %s", selectedManager.getFirstName(), selectedManager.getLastName()));
            } else {
                vManagerName.setText(null);
                selectedManager = null;
            }

            for (EmployeeOverview emp : Team) {
                if (!emp.getId().equals(vManagerID.getText().toString())) {
                    emp.setManagerID(!vManagerID.getText().toString().equals("") ? vManagerID.getText().toString() : null);
                } else {
                    emp.setManagerID("adminID");
                }
            }
        }
    };
    MaterialPickerOnPositiveButtonClickListener pclTimeDatePicker = new MaterialPickerOnPositiveButtonClickListener() {
        @Override
        public void onPositiveButtonClick(Object selection) {
            Pair<Long, Long> time = (Pair<Long, Long>) selection;
            vTime.setText(String.format("%s to %s", convertDateToString(time.first), convertDateToString(time.second)));
            startDate = time.first;
            endDate = time.second;
        }
    };
    View.OnClickListener oclStartDate = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            vTimeDatePicker.show(getFragmentManager(), "DATE_PICKER");
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
