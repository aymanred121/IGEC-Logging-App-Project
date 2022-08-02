package com.example.igec_admin.Dialogs;

import static android.content.ContentValues.TAG;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
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
import com.example.igec_admin.utilites.allowancesEnum;
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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class ProjectFragmentDialog extends DialogFragment {


    // Views
    private TextInputEditText vName, vArea, vStreet, vCity, vTime, vManagerName, vProjectReference;
    private MaterialButton vRegister, vUpdate, vDelete, vAddClient, vAddAllowance, vLocate;
    private AutoCompleteTextView vManagerID, vContractType;
    private TextInputLayout vNameLayout, vAreaLayout, vStreetLayout, vCityLayout, vManagerIDLayout, vTimeLayout, vProjectReferenceLayout, vContractTypeLayout;
    private RecyclerView recyclerView;
    private EmployeeAdapter adapter;
    private MaterialCheckBox vOfficeWork;
    private RecyclerView.LayoutManager layoutManager;

    // Vars
    private String lat, lng;
    private ArrayList<Allowance> allowances;
    long startDate;
    private Client client;
    private final MaterialDatePicker.Builder<Long> vTimeDatePickerBuilder = MaterialDatePicker.Builder.datePicker();
    private MaterialDatePicker vTimeDatePicker;
    ArrayList<EmployeeOverview> employees = new ArrayList<>();
    private static ArrayList<String> TeamID = new ArrayList<>();
    private static ArrayList<EmployeeOverview> Team = new ArrayList<>();
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    DocumentReference employeeOverviewRef = db.collection("EmployeeOverview")
            .document("emp");
    CollectionReference employeeCol = db.collection("employees");
    Project project;
    private EmployeeOverview selectedManager;
    private Boolean isDeleted = false;
    private String currProjectID;
    private WriteBatch batch = FirebaseFirestore.getInstance().batch();
    private final SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
    private ArrayList<Pair<TextInputLayout, EditText>> views;

    public ProjectFragmentDialog(Project project) {
        this.project = project;
        currProjectID = project.getId();
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        clearTeam();

    }

    @Override
    public void onPause() {
        super.onPause();
        dismiss();
    }

    public static void clearTeam() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        WriteBatch batch = FirebaseFirestore.getInstance().batch();
        for (EmployeeOverview emp : Team) {
            if (emp.getManagerID() == null) {
                ArrayList<String> empInfo = new ArrayList<>();
                empInfo.add(emp.getFirstName());
                empInfo.add(emp.getLastName());
                empInfo.add(emp.getTitle());
                empInfo.add(null);
                empInfo.add(null);
                empInfo.add("0");
                Map<String, Object> empInfoMap = new HashMap<>();
                empInfoMap.put(emp.getId(), empInfo);
                batch.update(db.collection("EmployeeOverview")
                        .document("emp"), empInfoMap);
            }
        }
        Team.clear();
        TeamID.clear();
        batch.commit();
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
        getParentFragmentManager().setFragmentResultListener("location", this, new FragmentResultListener() {
            @Override
            public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle result) {

                lat = result.getString("lat");
                lng = result.getString("lng");
                Toast.makeText(getActivity(), String.format(
                                "lat: %s, lang: %s",
                                result.getString("lat"),
                                result.getString("lng")),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_project, container, false);
        Initialize(view);

        // listeners
        vName.addTextChangedListener(twName);
        vProjectReference.addTextChangedListener(twProjectReference);
        vArea.addTextChangedListener(twArea);
        vCity.addTextChangedListener(twCity);
        vStreet.addTextChangedListener(twStreet);
        vTime.addTextChangedListener(twTime);
        vContractType.addTextChangedListener(twContractType);
        vManagerID.addTextChangedListener(twManagerID);
        vUpdate.setOnClickListener(clUpdate);
        vDelete.setOnClickListener(clDelete);
        vLocate.setOnClickListener(clLocate);
        vTimeDatePicker.addOnPositiveButtonClickListener(pclTimeDatePicker);
        vTimeLayout.setEndIconOnClickListener(oclStartDate);
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
        vNameLayout = view.findViewById(R.id.textInputLayout_ProjectName);
        vProjectReference = view.findViewById(R.id.TextInput_ProjectReference);
        vProjectReferenceLayout = view.findViewById(R.id.textInputLayout_ProjectReference);
        vOfficeWork = view.findViewById(R.id.checkbox_officeWork);
        vCity = view.findViewById(R.id.TextInput_City);
        vCityLayout = view.findViewById(R.id.textInputLayout_City);
        vStreet = view.findViewById(R.id.TextInput_Street);
        vStreetLayout = view.findViewById(R.id.textInputLayout_Street);
        vArea = view.findViewById(R.id.TextInput_Area);
        vAreaLayout = view.findViewById(R.id.textInputLayout_Area);
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
        vLocate = view.findViewById(R.id.button_Locate);

        views = new ArrayList<>();
        views.add(new Pair<>(vNameLayout, vName));
        views.add(new Pair<>(vProjectReferenceLayout, vProjectReference));
        views.add(new Pair<>(vAreaLayout, vArea));
        views.add(new Pair<>(vCityLayout, vCity));
        views.add(new Pair<>(vStreetLayout, vStreet));
        views.add(new Pair<>(vTimeLayout, vTime));
        views.add(new Pair<>(vContractTypeLayout, vContractType));
        views.add(new Pair<>(vManagerIDLayout, vManagerID));

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
        startDate = project.getStartDate().getTime();
        vTimeDatePickerBuilder.setTitleText("Time");
        vTimeDatePicker = vTimeDatePickerBuilder.setSelection(startDate).build();
        vTime.setText(String.format("%s", convertDateToString(startDate)));
        lat = String.valueOf(project.getLat());
        lng = String.valueOf(project.getLng());
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
        ArrayList<String> empInfo = new ArrayList<>();
        empInfo.add(employees.get(position).getFirstName());
        empInfo.add(employees.get(position).getLastName());
        empInfo.add(employees.get(position).getTitle());
        empInfo.add(employees.get(position).getManagerID());
        batch = FirebaseFirestore.getInstance().batch();
        if (employees.get(position).isSelected) {
            employees.get(position).setProjectId(project.getId());
            Team.add(employees.get(position));
            TeamID.add(String.valueOf(employees.get(position).getId()));
            empInfo.add(project.getId());
            empInfo.add("1");
            Map<String, Object> empInfoMap = new HashMap<>();
            empInfoMap.put(employees.get(position).getId(), empInfo);
            if (employees.get(position).getManagerID() == null)
                batch.update(db.collection("EmployeeOverview")
                        .document("emp"), empInfoMap);

        } else {

            if (!vManagerID.getText().toString().isEmpty() && vManagerID.getText().toString().equals(employees.get(position).getId()))
                vManagerID.setText("");
            employees.get(position).setProjectId(null);
            Team.removeIf(employeeOverview -> employeeOverview.getId().equals(employees.get(position).getId()));
            TeamID.remove(String.valueOf(employees.get(position).getId()));
            empInfo.add(null);
            empInfo.add("0");
            Map<String, Object> empInfoMap = new HashMap<>();
            empInfoMap.put(employees.get(position).getId(), empInfo);
            if (employees.get(position).getManagerID() == null) {
                batch.update(db.collection("EmployeeOverview")
                        .document("emp"), empInfoMap);
            }
        }
        batch.commit();
        if (!vManagerIDLayout.isEnabled())
            vManagerID.setText("");
        if (TeamID.size() > 0) {
            ArrayAdapter<String> idAdapter = new ArrayAdapter<>(getActivity(), R.layout.item_dropdown, TeamID);
            vManagerID.setAdapter(idAdapter);

        }
        adapter.notifyItemChanged(position);
    }

    void getEmployees() {
        db.collection("EmployeeOverview")
                .document("emp")
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
        String currentDateAndTime = sdf.format(new Date());
        String month = currentDateAndTime.substring(3, 5);
        String year = currentDateAndTime.substring(6, 10);
        batch = FirebaseFirestore.getInstance().batch();
        for (EmployeeOverview empOverview : employees) {
            currProjectID = projectID;
            ArrayList<String> empInfo = new ArrayList<>();
            if (isDeleted || empOverview.getProjectId() == null) {
                empOverview.setManagerID(null);
                empOverview.setProjectId(null);
                empOverview.isSelected = false;
                db.collection("EmployeesGrossSalary").document(empOverview.getId()).get().addOnSuccessListener(doc -> {
                    if (!doc.exists())
                        return;
                    EmployeesGrossSalary employeesGrossSalary = doc.toObject(EmployeesGrossSalary.class);
                    employeesGrossSalary.getAllTypes().removeIf(x -> x.getProjectId().equals(currProjectID));
                    db.document(doc.getReference().getPath()).update("allTypes", employeesGrossSalary.getAllTypes());
                });
                db.collection("EmployeesGrossSalary").document(empOverview.getId()).collection(year).document(month).update("baseAllowances", null);
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
            empInfo.add(empOverview.isSelected ? "1" : "0");
            Map<String, Object> empInfoMap = new HashMap<>();
            empInfoMap.put(empOverview.getId(), empInfo);
            batch.update(db.collection("EmployeeOverview")
                    .document("emp"), empInfoMap);
            batch.update(employeeCol.document(empOverview.getId()), "managerID", empOverview.getManagerID(), "projectID", empOverview.getProjectId());
        }

    }


    @SuppressLint("NotifyDataSetChanged")
    void retrieveEmployees(Map<String, ArrayList<String>> empMap) {
        employees.clear();
        Team.clear();
        TeamID.clear();
        for (String key : empMap.keySet()) {
            String firstName = empMap.get(key).get(0);
            String lastName = empMap.get(key).get(1);
            String title = empMap.get(key).get(2);
            String managerID = empMap.get(key).get(3);
            String projectID = empMap.get(key).get(4);
            boolean isSelected = empMap.get(key).get(5).equals("1");
            String id = (key);
            EmployeeOverview newEmp = new EmployeeOverview(firstName, lastName, title, id, projectID, isSelected);
            if (currProjectID.equals(projectID)) {
                newEmp.isSelected = true;
                if (id.equals(currProjectID))
                    selectedManager = newEmp;
                if (managerID != null)
                    newEmp.setManagerID(id.equals(project.getManagerID()) ? "adminID" : vManagerID.getText().toString());
                Team.add(newEmp);
                TeamID.add(newEmp.getId());
            }
            if (TeamID.contains(id) == isSelected && (managerID == null || currProjectID.equals(projectID)))
                employees.add(newEmp);


            /*
             *
             *
             * show if d == l && (manager == null || projectID = emp.project)
             *
             * d l m
             * 1 0 0
             * 1 0 1
             * 1 1 0
             * 1 1 1 = show to me
             * */
            adapter.setEmployeeOverviewsList(employees);
            adapter.notifyDataSetChanged();

        }
    }

    private boolean generateError() {
        for (Pair<TextInputLayout, EditText> view : views) {
            if (view.second.getText().toString().trim().isEmpty()) {
                if (view.first == vTimeLayout)
                    view.first.setErrorIconDrawable(R.drawable.ic_baseline_calendar_month_24);
                view.first.setError("Missing");
                return true;
            }
            if (view.first.getError() != null) {
                return true;
            }
        }
        boolean isClientMissing = (!vOfficeWork.isChecked() && client == null);
        boolean isLocationMissing = (lat == null && lng == null);
        if (isClientMissing) {
            Toast.makeText(getActivity(), "client Info Missing", Toast.LENGTH_SHORT).show();
        }
        if (isLocationMissing) {
            Toast.makeText(getActivity(), "Location is Missing", Toast.LENGTH_SHORT).show();
        }
        return isClientMissing || isLocationMissing;
    }

    boolean validateInputs() {
        return !generateError();
    }

    void updateProject() {
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
                , Double.parseDouble(lat)
                , Double.parseDouble(lng)
                , vContractType.getText().toString());
        newProject.setId(project.getId());
        newProject.setClient(vOfficeWork.isChecked() ? null : client);
        newProject.setMachineWorkedTime(project.getMachineWorkedTime());
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
            String currentDateAndTime = sdf.format(new Date());
            String day = currentDateAndTime.substring(0, 2);
            String month = currentDateAndTime.substring(3, 5);
            String year = currentDateAndTime.substring(6, 10);
            if (Integer.parseInt(day) > 25) {
                if (Integer.parseInt(month) + 1 == 13) {
                    month = "01";
                    year = Integer.parseInt(year) + 1 + "";
                } else {
                    month = Integer.parseInt(month) + 1 + "";
                    if (month.length() == 1) {
                        month = "0" + month;
                    }
                }

            }
            final String finalMonth = month;
            final String finalYear = year;
            db.collection("EmployeesGrossSalary").document(emp.getId()).get().addOnSuccessListener((value) -> {
                if (!value.exists())
                    return;
                EmployeesGrossSalary employeesGrossSalary = value.toObject(EmployeesGrossSalary.class);
                if (allowances.size() != 0) {
                    employeesGrossSalary.getAllTypes().removeIf(allowance -> allowance.getType() == allowancesEnum.PROJECT.ordinal());
                    employeesGrossSalary.getAllTypes().addAll(allowances);
                }
                batch.update(db.collection("EmployeesGrossSalary").document(emp.getId()), "allTypes", employeesGrossSalary.getAllTypes());
                db.collection("EmployeesGrossSalary").document(emp.getId()).collection(finalYear).document(finalMonth)
                        .get().addOnSuccessListener(documentSnapshot -> {
                            if (!documentSnapshot.exists()) {
                                //new month
//                                        employeesGrossSalary.getAllTypes().removeIf(allowance -> allowance.getType() == allowancesEnum.PROJECT.ordinal());
//                                        employeesGrossSalary.setBaseAllowances(allowances);
//                                        batch.set(db.document(documentSnapshot.getReference().getPath()), employeesGrossSalary);
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
                            EmployeesGrossSalary employeesGrossSalary1 = documentSnapshot.toObject(EmployeesGrossSalary.class);
                            if (employeesGrossSalary1.getBaseAllowances() == null)
                                employeesGrossSalary1.setBaseAllowances(allowances);
                            else {
                                employeesGrossSalary1.getBaseAllowances().removeIf(allowance -> allowance.getType() == allowancesEnum.PROJECT.ordinal());
                                employeesGrossSalary1.getBaseAllowances().addAll(allowances);
                            }
                            batch.update(db.document(documentSnapshot.getReference().getPath()), "baseAllowances", employeesGrossSalary1.getBaseAllowances());
                            if (counter[0] == newProject.getEmployees().size() - 1) {
                                batch.commit().addOnSuccessListener(unused1 -> {
                                    Toast.makeText(getActivity(), "Updated", Toast.LENGTH_SHORT).show();
                                    vUpdate.setEnabled(true);
                                    batch = FirebaseFirestore.getInstance().batch();
                                    project.setEmployees(null);
                                    employees.clear();
                                    Team.clear();
                                    TeamID.clear();
                                    batch = FirebaseFirestore.getInstance().batch();
                                    dismiss();
                                }).addOnFailureListener(e -> {
                                    Log.d("batchError", e.toString());
                                    batch = FirebaseFirestore.getInstance().batch();
                                });
                            }
                            counter[0]++;
                        });
            });
        });

    }

    void deleteProject() {
        currProjectID = project.getId();
        batch.delete(db.collection("projects").document(project.getId()));
        isDeleted = true;
        batch.commit().addOnSuccessListener(unused -> {
            updateEmployeesDetails(currProjectID);
            batch.commit().addOnSuccessListener(unused2 -> {
                batch = FirebaseFirestore.getInstance().batch();
                Toast.makeText(getActivity(), "Deleted", Toast.LENGTH_SHORT).show();
                vDelete.setEnabled(true);
                dismiss();
            });
        });


    }

    private void hideError(TextInputLayout layout) {
        layout.setError(null);
        layout.setErrorEnabled(false);
    }

    // Listeners
    private final TextWatcher twName = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void afterTextChanged(Editable editable) {
            hideError(vNameLayout);
        }
    };
    private final TextWatcher twArea = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void afterTextChanged(Editable editable) {
            hideError(vAreaLayout);
        }
    };
    private final TextWatcher twCity = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void afterTextChanged(Editable editable) {
            hideError(vCityLayout);
        }
    };
    private final TextWatcher twStreet = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void afterTextChanged(Editable editable) {
            hideError(vStreetLayout);
        }
    };
    private final TextWatcher twTime = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void afterTextChanged(Editable editable) {
            hideError(vTimeLayout);
        }
    };
    private final TextWatcher twContractType = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void afterTextChanged(Editable editable) {
            hideError(vContractTypeLayout);
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
            hideError(vProjectReferenceLayout);
        }
    };
    private final TextWatcher twManagerID = new TextWatcher() {
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
            hideError(vManagerIDLayout);
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
            vUpdate.setEnabled(false);
            updateProject();
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
    private final View.OnClickListener clLocate = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            // first time
            if (lat == null && lng == null)
                LocationDialog.newInstance().show(getParentFragmentManager(), "");
            else
                LocationDialog.newInstance(lat, lng).show(getParentFragmentManager(), "");
        }
    };

    private final View.OnClickListener oclAddClient = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            AddClientDialog addClientDialog;
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
