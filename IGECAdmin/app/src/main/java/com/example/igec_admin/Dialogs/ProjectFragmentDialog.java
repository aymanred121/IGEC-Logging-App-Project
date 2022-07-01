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
import androidx.fragment.app.FragmentResultListener;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.igec_admin.Adatpers.EmployeeAdapter;
import com.example.igec_admin.R;
import com.example.igec_admin.fireBase.Allowance;
import com.example.igec_admin.fireBase.Client;
import com.example.igec_admin.fireBase.EmployeeOverview;
import com.example.igec_admin.fireBase.EmployeesGrossSalary;
import com.example.igec_admin.fireBase.Project;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class ProjectFragmentDialog extends DialogFragment {
    private final int PROJECT = 0;
    private final int NETSALARY = 1;
    private final int ALLOWANCE = 2;
    private final int BONUS = 3;
    private final int PENALTY = 4;


    // Views
    private TextInputEditText vName, vArea, vStreet, vCity, vTime, vManagerName, vProjectReference;
    private MaterialButton vRegister, vUpdate, vDelete, vAddClient, vAddAllowance;
    private AutoCompleteTextView vManagerID, vContractType;
    private TextInputLayout vManagerIDLayout, vTimeLayout, vProjectReferenceLayout, vContractTypeLayout;
    private RecyclerView recyclerView;
    private EmployeeAdapter adapter;
    private MaterialCheckBox vOfficeWork;
    private RecyclerView.LayoutManager layoutManager;

    // Vars
    private ArrayList<Allowance> allowances;
    long startDate;
    private Client client;
    private final MaterialDatePicker.Builder<Long> vTimeDatePickerBuilder = MaterialDatePicker.Builder.datePicker();
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
    private WriteBatch batch = FirebaseFirestore.getInstance().batch();
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
        getParentFragmentManager().setFragmentResultListener("client", this, new FragmentResultListener() {
            @Override
            public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle bundle) {
                // We use a String here, but any type that can be put in a Bundle is supported
                client = (Client) bundle.getSerializable("client");
                // Do something with the result

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
        View view = inflater.inflate(R.layout.fragment_add_project, container, false);
        Initialize(view);

        // listeners
        vUpdate.setOnClickListener(clUpdate);
        vDelete.setOnClickListener(clDelete);
        vManagerID.addTextChangedListener(twManagerID);
        vTimeDatePicker.addOnPositiveButtonClickListener(pclTimeDatePicker);
        vTimeLayout.setEndIconOnClickListener(oclStartDate);
        vProjectReference.addTextChangedListener(twProjectReference);
        vOfficeWork.setOnClickListener(oclOfficeWork);
        vAddClient.setOnClickListener(oclAddClient);
        vAddAllowance.setOnClickListener(oclAddAllowance);


        // Inflate the layout for this fragment
        return view;
    }

    // Functions
    private void Initialize(View view) {
        allowances = new ArrayList<>();
        vName = view.findViewById(R.id.TextInput_ProjectName);
        vProjectReference = view.findViewById(R.id.TextInput_ProjectReference);
        vProjectReferenceLayout = view.findViewById(R.id.textInputLayout_ProjectReference);
        vOfficeWork = view.findViewById(R.id.checkbox_officeWork);
        vCity = view.findViewById(R.id.TextInput_City);
        vStreet = view.findViewById(R.id.TextInput_Street);
        vArea = view.findViewById(R.id.TextInput_Area);
        vContractType = view.findViewById(R.id.TextInput_ContractType);
        vContractTypeLayout = view.findViewById(R.id.textInputLayout_ContractType);
        vTime = view.findViewById(R.id.TextInput_Time);
        vTimeLayout = view.findViewById(R.id.textInputLayout_Time);
        vManagerID = view.findViewById(R.id.TextInput_ManagerID);
        vManagerIDLayout = view.findViewById(R.id.textInputLayout_ManagerID);
        vManagerName = view.findViewById(R.id.TextInput_ManagerName);
        vRegister = view.findViewById(R.id.button_register);
        vAddClient = view.findViewById(R.id.button_AddClient);
        vAddAllowance = view.findViewById(R.id.button_AddAllowances);
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
        allowances.addAll(project.getAllowancesList());
        client = project.getClient();
        vName.setText(project.getName());
        vManagerID.setText(project.getManagerID());
        vManagerName.setText(project.getManagerName());
        vContractType.setText(project.getContractType());
        vContractTypeLayout.setEnabled(true);
        vContractType.setEnabled(false);
        vProjectReference.setText(project.getReference());
        vOfficeWork.setChecked(project.getReference().equals("-99999"));
        vAddClient.setEnabled(!project.getReference().equals("-99999"));
        vArea.setText(project.getLocationArea());
        vCity.setText(project.getLocationCity());
        vStreet.setText(project.getLocationStreet());
        vManagerIDLayout.setEnabled(true);
        vManagerID.setEnabled(false);
        startDate = project.getStartDate().getTime();
        vTimeDatePickerBuilder.setTitleText("Time");
        vTimeDatePicker = vTimeDatePickerBuilder.setSelection(startDate).build();
        vTime.setText(String.format("%s", convertDateToString(startDate)));
        getEmployees();

        ArrayAdapter<String> idAdapter = new ArrayAdapter<>(getActivity(), R.layout.item_dropdown, TeamID);
        vManagerID.setAdapter(idAdapter);
        ArrayList<String> contract = new ArrayList<>();
        contract.add("lump sum");
        contract.add("timesheet");
        ArrayAdapter<String> ContractAdapter = new ArrayAdapter<>(getActivity(), R.layout.item_dropdown, contract);
        vContractType.setAdapter(ContractAdapter);
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

            if (!vManagerID.getText().toString().isEmpty() && vManagerID.getText().toString().equals(employees.get(position).getId()))
                vManagerID.setText("");
            Team.remove(employees.get(position));
            TeamID.remove(String.valueOf(employees.get(position).getId()));
            employees.get(position).setProjectId(null);
        }
        vManagerIDLayout.setEnabled(Team.size() >= 1);
        vManagerID.setEnabled(false);
        if (!vManagerIDLayout.isEnabled())
            vManagerID.setText("");
        if (TeamID.size() > 0) {
            ArrayAdapter<String> idAdapter = new ArrayAdapter<>(getActivity(), R.layout.item_dropdown, TeamID);
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
                empOverview.setProjectId(null);
                Team.remove(empOverview);
            }
            if (empOverview.getId().equals(vManagerID.getText().toString()) && !isDeleted) {
                empOverview.setManagerID("adminID");
            } else if (empOverview.getProjectId() != null && empOverview.getProjectId().equals(projectID) && !isDeleted) {
                empOverview.setManagerID(vManagerID.getText().toString());
            }
            empInfo.add(empOverview.getFirstName());
            empInfo.add(empOverview.getLastName());
            empInfo.add(empOverview.getTitle());
            empInfo.add(empOverview.getManagerID());
            empInfo.add(empOverview.getProjectId());
            Map<String, Object> empInfoMap = new HashMap<>();
            empInfoMap.put(empOverview.getId(), empInfo);
            batch.update(employeeOverviewRef, empInfoMap);
            //employeeOverviewRef.update(empInfoMap);
            // employeeCol.document(empOverview.getId()).update("managerID", empOverview.getManagerID(), "projectID", empOverview.getProjectId());
            batch.update(employeeCol.document(empOverview.getId()), "managerID", empOverview.getManagerID(), "projectID",empOverview.getProjectId());
            //   employeeCol.document(empOverview.getId()).update("managerID", empOverview.getManagerID(), "projectID", currProjectID);


//            employeeCol.document(empOverview.getId()).update("managerID", empOverview.getManagerID(), "projectID", currProjectID);
        }

    }


    @SuppressLint("NotifyDataSetChanged")
    void retrieveEmployees(Map<String, ArrayList<String>> empMap) {
        employees.clear();
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
//        HashMap<String, Object> updatedProjectData = new HashMap<>();
//        updatedProjectData.put("estimatedEndDate", new Date(endDate));
//        updatedProjectData.put("startDate", new Date(startDate));
//        updatedProjectData.put("name", vName.getText().toString());
//        updatedProjectData.put("managerName", vManagerName.getText().toString());
//        updatedProjectData.put("managerID", vManagerID.getText().toString());
//        updatedProjectData.put("location", "");
//        updatedProjectData.put("employees", Team);
        updateEmployeesDetails(project.getId());
        Project newProject = new Project(vManagerName.getText().toString()
                , vManagerID.getText().toString()
                , vName.getText().toString()
                , new Date(startDate)
                , Team
                , vProjectReference.getText().toString()
                , vCity.getText().toString()
                , vArea.getText().toString()
                , vStreet.getText().toString()
                , vContractType.getText().toString());
        newProject.setId(project.getId());
        newProject.setClient(client);
        //Added projectId to each allowance that is coming from project
        allowances.stream().flatMap(allowance -> {
            allowance.setProjectId(project.getId());
            return null;
        }).collect(Collectors.toList());
        newProject.getAllowancesList().addAll(allowances);
        allowances = newProject.getAllowancesList();
        batch.set(db.collection("projects").document(project.getId()), newProject);
        final int[] counter = {0};
        newProject.getEmployees().forEach(emp -> {
            ArrayList<Allowance> allTypes = new ArrayList<>();
            db.collection("EmployeesGrossSalary").document(emp.getId()).get().addOnSuccessListener((value) -> {
                String year = String.valueOf(LocalDate.now().getYear());
                String month = String.valueOf(LocalDate.now().getMonthValue());
                if(month.length()==1)
                    month= "0"+month;
                if (!value.exists())
                    return;
                EmployeesGrossSalary employeesGrossSalary = value.toObject(EmployeesGrossSalary.class);
                allTypes.addAll(employeesGrossSalary.getAllTypes());
                if (allowances.size() != 0) {
                    allTypes.removeIf(allowance -> allowance.getType() == PROJECT);
                    allTypes.addAll(allowances);
                }
                batch.update(db.collection("EmployeesGrossSalary").document(emp.getId()), "allTypes", allTypes);
                db.collection("EmployeesGrossSalary").document(emp.getId()).collection(year).document(month)
                        .get().addOnSuccessListener(documentSnapshot -> {
                                    if(!documentSnapshot.exists()){
                                        //new month
                                        batch.set(db.collection("EmployeesGrossSalary").document(documentSnapshot.getReference().getPath()), employeesGrossSalary);
                                        if (counter[0] == newProject.getEmployees().size() - 1) {
                                            batch.commit().addOnSuccessListener(unused1 -> {
                                                Toast.makeText(getActivity(), "Updated", Toast.LENGTH_SHORT).show();
                                                vUpdate.setEnabled(true);
                                                batch = FirebaseFirestore.getInstance().batch();
                                                project.setEmployees(null);
                                                employees.clear();
                                                Team.clear();
                                                TeamID.clear();
                                                dismiss();
                                            });
                                        }
                                        counter[0]++;
                                        return;
                                    }
                            allTypes.clear();
                            EmployeesGrossSalary employeesGrossSalary1 = documentSnapshot.toObject(EmployeesGrossSalary.class);
                            allTypes.addAll(employeesGrossSalary1.getAllTypes());
                            if (allowances.size() != 0) {
                                allTypes.removeIf(allowance -> allowance.getType() == PROJECT);
                                allTypes.addAll(allowances);
                            }
                            batch.update(db.collection("EmployeesGrossSalary").document(documentSnapshot.getReference().getPath()), "allTypes", allTypes);
                            if (counter[0] == newProject.getEmployees().size() - 1) {
                                batch.commit().addOnSuccessListener(unused1 -> {
                                    Toast.makeText(getActivity(), "Updated", Toast.LENGTH_SHORT).show();
                                    vUpdate.setEnabled(true);
                                    batch = FirebaseFirestore.getInstance().batch();
                                    project.setEmployees(null);
                                    employees.clear();
                                    Team.clear();
                                    TeamID.clear();
                                    dismiss();
                                });
                            }
                            counter[0]++;
                        });
                //  db.collection("EmployeesGrossSalary").document(emp.getId()).update("allTypes", allTypes);
            });
        });

    }

    void deleteProject() {
        currProjectID = project.getId();
        batch.delete(db.collection("projects").document(project.getId()));
        isDeleted = true;
        updateEmployeesDetails(currProjectID);
        batch.commit().addOnSuccessListener(unused -> {
            batch = FirebaseFirestore.getInstance().batch();
            Toast.makeText(getActivity(), "Deleted", Toast.LENGTH_SHORT).show();
            vDelete.setEnabled(true);
            dismiss();
        });
//        db.collection("projects").document(project.getId()).delete().addOnSuccessListener(unused -> {
//            isDeleted = true;
//            updateEmployeesDetails(currProjectID);
//            Toast.makeText(getActivity(), "Deleted", Toast.LENGTH_SHORT).show();
//            dismiss();
//        });

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
    private final TextWatcher twProjectReference = new TextWatcher() {
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
    MaterialPickerOnPositiveButtonClickListener pclTimeDatePicker = new MaterialPickerOnPositiveButtonClickListener() {
        @Override
        public void onPositiveButtonClick(Object selection) {
            startDate = (long) selection;
            vTime.setText(String.format("%s", convertDateToString(startDate)));
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
            vUpdate.setEnabled(true);
            updateProject();
        } else {
            Toast.makeText(getActivity(), "please, fill the project data", Toast.LENGTH_SHORT).show();
        }
    };
    View.OnClickListener clDelete = v -> {
        vDelete.setEnabled(false);
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(getActivity());
        builder.setTitle(getString(R.string.Delete))
                .setMessage(getString(R.string.AreUSure))
                .setNegativeButton(getString(R.string.cancel), (dialogInterface, i) -> {
                    vDelete.setEnabled(true);
                })
                .setPositiveButton(getString(R.string.accept), (dialogInterface, i) -> {
                    deleteProject();
                    dialogInterface.dismiss();
                })
                .show();

    };
    private final View.OnClickListener oclAddClient = new View.OnClickListener() {
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
    private final View.OnClickListener oclOfficeWork = new View.OnClickListener() {
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
    private final View.OnClickListener oclAddAllowance = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            AddAllowanceDialog addAllowanceDialog = new AddAllowanceDialog(allowances);
            addAllowanceDialog.show(getParentFragmentManager(), "");
        }
    };

}
