package com.example.igec_admin.Fragments;

import static android.content.ContentValues.TAG;

import android.annotation.SuppressLint;
import android.os.Bundle;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.util.Pair;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentResultListener;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import com.example.igec_admin.Adatpers.EmployeeAdapter;
import com.example.igec_admin.Dialogs.AddAllowanceDialog;
import com.example.igec_admin.Dialogs.AddClientDialog;
import com.example.igec_admin.R;
import com.example.igec_admin.fireBase.Allowance;
import com.example.igec_admin.fireBase.Client;
import com.example.igec_admin.fireBase.EmployeeOverview;
import com.example.igec_admin.fireBase.Project;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class AddProjectFragment extends Fragment {

    // Views
    private TextInputEditText vName, vTime, vManagerName, vArea, vStreet, vCity, vProjectReference;
    private MaterialButton vRegister, vAddClient, vAddAllowance;
    private AutoCompleteTextView vManagerID, vContractType;
    private TextInputLayout vManagerIDLayout, vTimeLayout, vProjectReferenceLayout;
    private RecyclerView recyclerView;
    private MaterialCheckBox vOfficeWork;
    private EmployeeAdapter adapter;

    // Vars
    private ArrayList<Allowance> allowances;
    private Client client;
    private EmployeeOverview selectedManager;
    private String projectID;
    private long startDate, endDate;
    private MaterialDatePicker.Builder<Long> vTimeDatePickerBuilder = MaterialDatePicker.Builder.datePicker();
    private MaterialDatePicker vTimeDatePicker;
    private ArrayList<EmployeeOverview> employees = new ArrayList();
    private ArrayList<String> TeamID = new ArrayList<>();
    private ArrayList<EmployeeOverview> Team = new ArrayList<>();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private DocumentReference employeeOverviewRef = db.collection("EmployeeOverview")
            .document("emp");
    private CollectionReference employeeCol = db.collection("employees");
    private WriteBatch batch = db.batch();


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getParentFragmentManager().setFragmentResultListener("client", this, new FragmentResultListener() {
            @Override
            public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle bundle) {
                // We use a String here, but any type that can be put in a Bundle is supported
                client = (Client) bundle.getSerializable("client");
                // Do something with the result
                Toast.makeText(getActivity(), client.getName(), Toast.LENGTH_SHORT).show();

            }
        });
        getParentFragmentManager().setFragmentResultListener("allowances", this, new FragmentResultListener() {
            @Override
            public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle bundle) {
                // We use a String here, but any type that can be put in a Bundle is supported
                allowances = bundle.getParcelableArrayList("allowances");
                // Do something with the result
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_add_project, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Initialize(view);

        // listeners
        vProjectReference.addTextChangedListener(twProjectReference);
        vOfficeWork.setOnClickListener(oclOfficeWork);
        vAddClient.setOnClickListener(oclAddClient);
        vRegister.setOnClickListener(clRegister);
        vManagerID.addTextChangedListener(twManagerID);
        vTimeLayout.setEndIconOnClickListener(oclTimeDate);
        vTimeDatePicker.addOnPositiveButtonClickListener(pclTimeDatePicker);
        vAddAllowance.setOnClickListener(oclAddAllowance);
    }

    // Functions
    private void Initialize(View view) {
        vName = view.findViewById(R.id.TextInput_ProjectName);
        vProjectReference = view.findViewById(R.id.TextInput_ProjectReference);
        vProjectReferenceLayout = view.findViewById(R.id.textInputLayout_ProjectReference);
        vOfficeWork = view.findViewById(R.id.checkbox_officeWork);
        vCity = view.findViewById(R.id.TextInput_City);
        vStreet = view.findViewById(R.id.TextInput_Street);
        vArea = view.findViewById(R.id.TextInput_Area);
        vContractType = view.findViewById(R.id.TextInput_ContractType);
        vTime = view.findViewById(R.id.TextInput_Time);
        vTimeLayout = view.findViewById(R.id.textInputLayout_Time);
        vManagerID = view.findViewById(R.id.TextInput_ManagerID);
        vManagerIDLayout = view.findViewById(R.id.textInputLayout_ManagerID);
        vManagerName = view.findViewById(R.id.TextInput_ManagerName);
        vRegister = view.findViewById(R.id.button_register);
        vAddAllowance = view.findViewById(R.id.button_AddAllowances);
        vAddClient = view.findViewById(R.id.button_AddClient);
        vTimeDatePickerBuilder.setTitleText("Time");
        vTimeDatePicker = vTimeDatePickerBuilder.build();
        recyclerView = view.findViewById(R.id.recyclerview);
        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
        adapter = new EmployeeAdapter(employees, true);
        adapter.setOnItemClickListener(itclEmployeeAdapter);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
        projectID = db.collection("projects").document().getId().substring(0, 5);
        getEmployees();

        ArrayList<String> contract = new ArrayList<>();
        contract.add("lump sum");
        contract.add("timesheet");
        ArrayAdapter<String> ContractAdapter = new ArrayAdapter<>(getActivity(), R.layout.dropdown_item, contract);
        vContractType.setAdapter(ContractAdapter);
        allowances = new ArrayList<>();
    }

    private void ChangeSelectedTeam(int position) {

        employees.get(position).isSelected = !employees.get(position).isSelected;
        if (employees.get(position).isSelected) {
            employees.get(position).setProjectId(projectID);
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

                    String source = documentSnapshot != null && documentSnapshot.getMetadata().hasPendingWrites()
                            ? "Local" : "Server";

                    if (documentSnapshot.exists()) {
                        Map<String, ArrayList<String>> empMap;
                        empMap = (HashMap) documentSnapshot.getData();
                        retrieveEmployees(empMap);
                    }
                });
    }


    private void updateEmployeesDetails(String projectID) {
        for (EmployeeOverview emp : Team) {
            ArrayList<String> empInfo = new ArrayList<>();
            empInfo.add(emp.getFirstName());
            empInfo.add(emp.getLastName());
            empInfo.add(emp.getTitle());
            empInfo.add(emp.getManagerID());
            empInfo.add(projectID);
            Map<String, Object> empInfoMap = new HashMap<>();
            empInfoMap.put(emp.getId(), empInfo);
            employeeOverviewRef.update(empInfoMap);
            batch.update(employeeCol.document(emp.getId()), "managerID", empInfo.get(3), "projectID", projectID);
        }
        batch.commit().addOnSuccessListener(unused -> {
            ClearInputs();
            Toast.makeText(getActivity(), "Registered", Toast.LENGTH_SHORT).show();
        });
    }

    private void addProject() {
        Project newProject = new Project(vManagerName.getText().toString()
                , vManagerID.getText().toString()
                , vName.getText().toString()
                , new Date(startDate)
                , Team
                , vProjectReference.getText().toString()
                , vCity.getText().toString()
                , vArea.getText().toString()
                , vStreet.getText().toString()
                ,vContractType.getText().toString());
        newProject.setId(projectID);
        newProject.setClient(client);
        newProject.getAllowancesList().addAll(allowances);
        db.collection("projects").document(projectID).set(newProject).addOnSuccessListener(unused -> updateEmployeesDetails(projectID));
    }


    private String convertDateToString(long selection) {
        @SuppressLint("SimpleDateFormat") SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy");
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(selection);
        return simpleDateFormat.format(calendar.getTime());
    }

    void retrieveEmployees(Map<String, ArrayList<String>> empMap) {
        employees.clear();
        for (String key : empMap.keySet()) {
            String firstName = empMap.get(key).get(0);
            String lastName = empMap.get(key).get(1);
            String title = empMap.get(key).get(2);
            String managerID = empMap.get(key).get(3);
            String projectID = empMap.get(key).get(4);
            String id = (key);
            if ((managerID == null))
                employees.add(new EmployeeOverview(firstName, lastName, title, id, projectID));
        }
        adapter.setEmployeeOverviewsList(employees);
        adapter.notifyDataSetChanged();

    }

    void ClearInputs() {
        vName.setText(null);
        vCity.setText(null);
        vArea.setText(null);
        vStreet.setText(null);
        vContractType.setText(null);
        vManagerID.setText(null);
        vManagerName.setText(null);
        vTime.setText(null);
        client = null;
    }

    boolean validateInputs() {
        return
                !(vName.getText().toString().isEmpty() ||
                        vArea.getText().toString().isEmpty() ||
                        vCity.getText().toString().isEmpty() ||
                        vStreet.getText().toString().isEmpty() ||
                        vContractType.getText().toString().isEmpty() ||
                        vManagerID.getText().toString().isEmpty() ||
                        vManagerName.getText().toString().isEmpty() ||
                        client == null ||
                        vTime.getText().toString().isEmpty());
    }

    // Listeners
    private TextWatcher twManagerID = new TextWatcher() {
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
    private TextWatcher twProjectReference = new TextWatcher() {
        boolean removeDash = false;

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            removeDash = s.toString().contains("-");

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            if (s.toString().length() == 2 && !removeDash) {
                s.append('-');
            }
        }
    };
    private View.OnClickListener oclTimeDate = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            vTimeDatePicker.show(getFragmentManager(), "DATE_PICKER");
        }
    };
    private MaterialPickerOnPositiveButtonClickListener pclTimeDatePicker = new MaterialPickerOnPositiveButtonClickListener() {
        @Override
        public void onPositiveButtonClick(Object selection) {
            startDate = (long) selection;
            vTime.setText(String.format("%s", convertDateToString(startDate)));
        }
    };
    private View.OnClickListener clRegister = v -> {
        if (validateInputs()) {
            addProject();
        } else {
            Toast.makeText(getActivity(), "please, fill the project data", Toast.LENGTH_SHORT).show();
        }
    };
    private EmployeeAdapter.OnItemClickListener itclEmployeeAdapter = new EmployeeAdapter.OnItemClickListener() {
        @Override
        public void onItemClick(int position) {
            ChangeSelectedTeam(position);
        }

        @Override
        public void onCheckboxClick(int position) {
            ChangeSelectedTeam(position);
        }
    };
    private View.OnClickListener oclAddClient = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            AddClientDialog addClientDialog;
            if (client != null && client.getName().equals(""))
                addClientDialog = new AddClientDialog(null);
            else
                addClientDialog = new AddClientDialog(client);

            addClientDialog.show(getParentFragmentManager(), "");
        }
    };
    private View.OnClickListener oclOfficeWork = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            vAddClient.setEnabled(!vOfficeWork.isChecked());
            vProjectReference.setEnabled(!vOfficeWork.isChecked());
            vProjectReferenceLayout.setEnabled(!vOfficeWork.isChecked());
            if (vOfficeWork.isChecked()) {
                vProjectReference.setText("-99999");
            } else {
                vProjectReference.setText(null);
            }

        }
    };
    private View.OnClickListener oclAddAllowance = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            AddAllowanceDialog addAllowanceDialog = new AddAllowanceDialog(allowances);
            addAllowanceDialog.show(getParentFragmentManager(),"");
        }
    };
}