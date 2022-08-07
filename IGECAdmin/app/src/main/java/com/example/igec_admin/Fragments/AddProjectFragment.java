package com.example.igec_admin.Fragments;

import static android.content.ContentValues.TAG;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentResultListener;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.igec_admin.Adatpers.EmployeeAdapter;
import com.example.igec_admin.Dialogs.AddAllowanceDialog;
import com.example.igec_admin.Dialogs.AddClientDialog;
import com.example.igec_admin.Dialogs.LocationDialog;
import com.example.igec_admin.R;
import com.example.igec_admin.fireBase.Allowance;
import com.example.igec_admin.fireBase.Client;
import com.example.igec_admin.fireBase.EmployeeOverview;
import com.example.igec_admin.fireBase.EmployeesGrossSalary;
import com.example.igec_admin.fireBase.Project;
import com.example.igec_admin.utilites.allowancesEnum;
import com.github.javafaker.Faker;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.WriteBatch;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class AddProjectFragment extends Fragment {

    // Views
    private TextInputEditText vName, vTime, vManagerName, vArea, vStreet, vCity, vProjectReference, vAreaM;
    private TextInputLayout vNameLayout, vTimeLayout, vManagerNameLayout, vAreaLayout, vAreaMLayout, vStreetLayout, vCityLayout, vProjectReferenceLayout, vManagerIDLayout, vContractTypeLayout;
    private MaterialButton vRegister, vAddClient, vAddAllowance, vLocate;
    private AutoCompleteTextView vManagerID, vContractType;
    private RecyclerView recyclerView;
    private MaterialCheckBox vOfficeWork;
    private EmployeeAdapter adapter;

    // Vars
    private String lat, lng;
    private ArrayList<String> contract = new ArrayList<>();
    private ArrayList<Pair<TextInputLayout, EditText>> views;
    private ArrayList<Allowance> allowances = new ArrayList<>();
    private Client client;
    private EmployeeOverview selectedManager;
    private String projectID;
    private long startDate;
    private MaterialDatePicker.Builder<Long> vTimeDatePickerBuilder = MaterialDatePicker.Builder.datePicker();
    private MaterialDatePicker vTimeDatePicker;
    private ArrayList<EmployeeOverview> employees;
    private static ArrayList<String> TeamID = new ArrayList<>();
    private static ArrayList<EmployeeOverview> Team = new ArrayList<>();
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final DocumentReference employeeOverviewRef = db.collection("EmployeeOverview")
            .document("emp");
    private final CollectionReference employeeCol = db.collection("employees");
    private WriteBatch batch = FirebaseFirestore.getInstance().batch();
    private View view;

    public static void clearTeam() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        WriteBatch batch = FirebaseFirestore.getInstance().batch();
        for (EmployeeOverview emp : Team) {
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
        Team.clear();
        TeamID.clear();
        batch.commit();
    }

    @Override
    public void onResume() {
        super.onResume();
        vManagerID.setText(null);
        ArrayAdapter<String> idAdapter = new ArrayAdapter<>(getActivity(), R.layout.item_dropdown, TeamID);
        vManagerID.setAdapter(idAdapter);
        setUpContractType();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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

                //Added projectId to each allowance that is coming from project and set type to project
                // Do something with the result
            }
        });

        getParentFragmentManager().setFragmentResultListener("location", this, new FragmentResultListener() {
            @Override
            public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle result) {

                lat = result.getString("lat");
                lng = result.getString("lng");
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
        this.view = view;
        initialize();
        setUpListeners();

    }

    // Functions
    private void setUpListeners() {
        // listeners

        vProjectReference.addTextChangedListener(twProjectReference);
        vManagerID.addTextChangedListener(twManagerID);
        vAreaM.addTextChangedListener(twArea);
        vOfficeWork.setOnClickListener(oclOfficeWork);
        vAddClient.setOnClickListener(oclAddClient);
        vRegister.setOnClickListener(clRegister);
        vLocate.setOnClickListener(clLocate);
        vTimeLayout.setEndIconOnClickListener(oclTimeDate);
        vTimeLayout.setErrorIconOnClickListener(oclTimeDate);
        vTimeDatePicker.addOnPositiveButtonClickListener(pclTimeDatePicker);
        vAddAllowance.setOnClickListener(oclAddAllowance);


        for (Pair<TextInputLayout, EditText> v : views) {
            if (v.first != vManagerIDLayout && v.first != vProjectReferenceLayout && v.first != vAreaMLayout)
                v.second.addTextChangedListener(new TextWatcher() {
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
                });
        }
    }

    private void initialize() {
        employees = new ArrayList<>();
        vName = view.findViewById(R.id.TextInput_ProjectName);
        vProjectReference = view.findViewById(R.id.TextInput_ProjectReference);
        vArea = view.findViewById(R.id.TextInput_Area);
        vCity = view.findViewById(R.id.TextInput_City);
        vStreet = view.findViewById(R.id.TextInput_Street);
        vTime = view.findViewById(R.id.TextInput_Time);
        vContractType = view.findViewById(R.id.TextInput_ContractType);
        vManagerID = view.findViewById(R.id.TextInput_ManagerID);
        vManagerName = view.findViewById(R.id.TextInput_ManagerName);
        vAreaM = view.findViewById(R.id.TextInput_ProjectArea);

        vNameLayout = view.findViewById(R.id.textInputLayout_ProjectName);
        vProjectReferenceLayout = view.findViewById(R.id.textInputLayout_ProjectReference);
        vAreaLayout = view.findViewById(R.id.textInputLayout_Area);
        vCityLayout = view.findViewById(R.id.textInputLayout_City);
        vStreetLayout = view.findViewById(R.id.textInputLayout_Street);
        vTimeLayout = view.findViewById(R.id.textInputLayout_Time);
        vContractTypeLayout = view.findViewById(R.id.textInputLayout_ContractType);
        vManagerIDLayout = view.findViewById(R.id.textInputLayout_ManagerID);
        vManagerNameLayout = view.findViewById(R.id.textInputLayout_ManagerName);
        vAreaMLayout = view.findViewById(R.id.textInputLayout_ProjectArea);

        views = new ArrayList<>();
        views.add(new Pair<>(vNameLayout, vName));
        views.add(new Pair<>(vProjectReferenceLayout, vProjectReference));
        views.add(new Pair<>(vAreaLayout, vArea));
        views.add(new Pair<>(vCityLayout, vCity));
        views.add(new Pair<>(vStreetLayout, vStreet));
        views.add(new Pair<>(vAreaMLayout, vAreaM));
        views.add(new Pair<>(vTimeLayout, vTime));
        views.add(new Pair<>(vContractTypeLayout, vContractType));
        views.add(new Pair<>(vManagerIDLayout, vManagerID));

        vOfficeWork = view.findViewById(R.id.checkbox_officeWork);
        vRegister = view.findViewById(R.id.button_register);
        vAddAllowance = view.findViewById(R.id.button_AddAllowances);
        vAddClient = view.findViewById(R.id.button_AddClient);
        vLocate = view.findViewById(R.id.button_Locate);
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
        vAddClient.setEnabled(!vOfficeWork.isChecked());
        getEmployees();
        setUpContractType();
        //TODO: remove fakeData() when all testing is finished
        fakeData();

    }

    private void setUpContractType() {
        contract.clear();
        contract.add("lump sum");
        contract.add("timesheet");
        ArrayAdapter<String> ContractAdapter = new ArrayAdapter<>(getActivity(), R.layout.item_dropdown, contract);
        vContractType.setAdapter(ContractAdapter);
    }

    private void ChangeSelectedTeam(int position) {

        employees.get(position).isSelected = !employees.get(position).isSelected;
        ArrayList<String> empInfo = new ArrayList<>();
        empInfo.add(employees.get(position).getFirstName());
        empInfo.add(employees.get(position).getLastName());
        empInfo.add(employees.get(position).getTitle());
        empInfo.add(null);
        empInfo.add(null);
        batch = FirebaseFirestore.getInstance().batch();
        if (employees.get(position).isSelected) {
            employees.get(position).setProjectId(projectID);
            Team.add(employees.get(position));
            TeamID.add(String.valueOf(employees.get(position).getId()));
            empInfo.add("1");
            Map<String, Object> empInfoMap = new HashMap<>();
            empInfoMap.put(employees.get(position).getId(), empInfo);
            batch.update(db.collection("EmployeeOverview")
                    .document("emp"), empInfoMap);

        } else {

            if (!vManagerID.getText().toString().isEmpty() && vManagerID.getText().toString().equals(employees.get(position).getId()))
                vManagerID.setText("");
//            Team.remove(employees.get(position));
            Team.removeIf(employeeOverview -> employeeOverview.getId().equals(employees.get(position).getId()));
            TeamID.remove(String.valueOf(employees.get(position).getId()));
            employees.get(position).setProjectId(null);
            empInfo.add("0");
            Map<String, Object> empInfoMap = new HashMap<>();
            empInfoMap.put(employees.get(position).getId(), empInfo);
            batch.update(db.collection("EmployeeOverview")
                    .document("emp"), empInfoMap);
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
//        TeamID.clear();
//        Team.clear();
        employeeOverviewRef
                .addSnapshotListener((documentSnapshot, e) -> {
                    vManagerID.setText(null);
                    vManagerName.setText(null);
                    if (e != null) {
                        Log.w(TAG, "Listen failed.", e);
                        return;
                    }

                    if (!documentSnapshot.exists())
                        return;
                    HashMap empMap = (HashMap) documentSnapshot.getData();
                    retrieveEmployees(empMap);
                });
    }


    private void updateEmployeesDetails(String projectID) {
        String day = vTime.getText().toString().substring(0, 2);
        String year = vTime.getText().toString().substring(6, 10);
        String month = vTime.getText().toString().substring(3, 5);
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
        final int[] counter = {0};
        Team.forEach(emp -> {
            ArrayList<String> empInfo = new ArrayList<>();
            empInfo.add(emp.getFirstName());
            empInfo.add(emp.getLastName());
            empInfo.add(emp.getTitle());
            empInfo.add(emp.getManagerID());
            empInfo.add(projectID);
            empInfo.add(emp.isSelected ? "1" : "0");
            Map<String, Object> empInfoMap = new HashMap<>();
            empInfoMap.put(emp.getId(), empInfo);
            batch.update(employeeOverviewRef, empInfoMap);
            batch.update(employeeCol.document(emp.getId()), "managerID", emp.getManagerID(), "projectID", projectID);
            db.collection("EmployeesGrossSalary").document(emp.getId()).get().addOnSuccessListener((value) -> {
                if (!value.exists())
                    return;
                EmployeesGrossSalary employeesGrossSalary = value.toObject(EmployeesGrossSalary.class);
                if (allowances.size() != 0) {
                    employeesGrossSalary.getAllTypes().addAll(allowances);
                }
                batch.update(db.collection("EmployeesGrossSalary").document(emp.getId()), "allTypes", employeesGrossSalary.getAllTypes());

                db.collection("EmployeesGrossSalary").document(emp.getId()).collection(finalYear).document(finalMonth).get().addOnSuccessListener(documentSnapshot -> {
                    if (!documentSnapshot.exists()) {
//                        employeesGrossSalary.getAllTypes().removeIf(allowance -> allowance.getType() == allowancesEnum.PROJECT.ordinal());
//                        employeesGrossSalary.setBaseAllowances(allowances);
//                        batch.set(db.document(documentSnapshot.getReference().getPath()),employeesGrossSalary);
                        if (counter[0] == Team.size() - 1) {
                            batch.commit().addOnSuccessListener(unused -> {
                                clearInputs();
                                fakeData();
                                Toast.makeText(getActivity(), "Registered", Toast.LENGTH_SHORT).show();
                                batch = FirebaseFirestore.getInstance().batch();
                            }).addOnFailureListener(unused -> {
                                batch = FirebaseFirestore.getInstance().batch();
                            });
                        }
                        counter[0]++;
                        return;
                    }
                    EmployeesGrossSalary employeesGrossSalary1 = documentSnapshot.toObject(EmployeesGrossSalary.class);
                    if (employeesGrossSalary1.getBaseAllowances() != null) {
                        employeesGrossSalary1.getBaseAllowances().removeIf(allowance -> allowance.getType() == allowancesEnum.PROJECT.ordinal());
                        employeesGrossSalary1.getBaseAllowances().addAll(allowances);
                    }
                    batch.set(db.document(documentSnapshot.getReference().getPath()), employeesGrossSalary1, SetOptions.mergeFields("baseAllowances"));
                    if (counter[0] == Team.size() - 1) {
                        batch.commit().addOnSuccessListener(unused -> {
                            clearInputs();
                            fakeData();
                            Toast.makeText(getActivity(), "Registered", Toast.LENGTH_SHORT).show();
                            batch = FirebaseFirestore.getInstance().batch();
                        }).addOnFailureListener(unused -> {
                            batch = FirebaseFirestore.getInstance().batch();
                        });
                    }
                    counter[0]++;
                });
            });
        });

    }

    private void addProject() {

        allowances.forEach(allowance -> {
            allowance.setType(allowancesEnum.PROJECT.ordinal());
            allowance.setProjectId(projectID);
        });
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
                , vContractType.getText().toString()
                , Double.parseDouble(vAreaM.getText().toString()));
        newProject.setId(projectID);
        newProject.setClient(vOfficeWork.isChecked() ? null : client);
        newProject.getAllowancesList().addAll(allowances);
        allowances = newProject.getAllowancesList();
        batch = FirebaseFirestore.getInstance().batch();
        batch.set(db.collection("projects").document(projectID), newProject);
        updateEmployeesDetails(projectID);
        projectID = db.collection("projects").document().getId().substring(0, 5);


    }

    private void hideError(TextInputLayout layout) {
        layout.setError(null);
        layout.setErrorEnabled(false);
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
            boolean isSelected = empMap.get(key).get(5).equals("1");
            String id = (key);
            /*
            *
                d l
                00 => show
                10 => hide
                01 => doesn't exist
                11 => show
            * */
            if (TeamID.contains(id) == isSelected && managerID == null)
                employees.add(new EmployeeOverview(firstName, lastName, title, id, projectID, isSelected));

        }
        adapter.setEmployeeOverviewsList(employees);
        adapter.notifyDataSetChanged();

    }

    void clearInputs() {
        vRegister.setEnabled(true);
        for (Pair<TextInputLayout, EditText> v : views) {
            v.second.setText(null);
        }
        TeamID.clear();
        Team.clear();
        vManagerID.setAdapter(null);
        client = null;
        vTimeDatePickerBuilder = MaterialDatePicker.Builder.datePicker();
        vTimeDatePicker = vTimeDatePickerBuilder.build();
        vTimeDatePicker.addOnPositiveButtonClickListener(pclTimeDatePicker);
        allowances.clear();
    }

    void fakeData() {
        Faker faker = new Faker();
        vName.setText(faker.bothify("??????"));
        vCity.setText(faker.address().cityName());
        vArea.setText(faker.address().cityName());
        vStreet.setText(faker.address().streetName());
        startDate = faker.date().birthday().getTime();
//        vTime.setText(convertDateToString(startDate));

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

    // Listeners
    private final View.OnClickListener oclTimeDate = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            vTimeDatePicker.show(getFragmentManager(), "DATE_PICKER");
        }
    };
    private final MaterialPickerOnPositiveButtonClickListener pclTimeDatePicker = new MaterialPickerOnPositiveButtonClickListener() {
        @Override
        public void onPositiveButtonClick(Object selection) {
            startDate = (long) selection;
            vTime.setText(String.format("%s", convertDateToString(startDate)));
        }
    };
    private final View.OnClickListener clRegister = v -> {
        if (validateInputs()) {
            vRegister.setEnabled(false);
            addProject();
        }
    };
    private final EmployeeAdapter.OnItemClickListener itclEmployeeAdapter = new EmployeeAdapter.OnItemClickListener() {
        @Override
        public void onItemClick(int position) {
        }

        @Override
        public void onCheckboxClick(int position) {
            ChangeSelectedTeam(position);
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
    private final View.OnClickListener oclAddAllowance = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            AddAllowanceDialog addAllowanceDialog = new AddAllowanceDialog((ArrayList<Allowance>) allowances.clone());
            addAllowanceDialog.show(getParentFragmentManager(), "");
        }
    };
    private final View.OnClickListener clLocate = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            // first time
            LocationDialog locationDialog;

            if (lat == null && lng == null)
                locationDialog = LocationDialog.newInstance();
            else
                locationDialog = LocationDialog.newInstance(lat, lng);
            locationDialog.show(getParentFragmentManager(), "");
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
    private final TextWatcher twArea = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void afterTextChanged(Editable editable) {
            if (!vAreaM.getText().toString().trim().isEmpty()) {
                double value = Double.parseDouble(vAreaM.getText().toString());
                if (value == 0)
                    vAreaMLayout.setError("Invalid value");
                else
                    hideError(vAreaMLayout);
            } else
                hideError(vAreaMLayout);
        }
    };
}