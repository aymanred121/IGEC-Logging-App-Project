package com.igec.admin.fragments;

import static android.content.ContentValues.TAG;

import static com.igec.common.CONSTANTS.ADMIN;
import static com.igec.common.CONSTANTS.EMPLOYEE_GROSS_SALARY_COL;
import static com.igec.common.CONSTANTS.EMPLOYEE_OVERVIEW_REF;
import static com.igec.common.CONSTANTS.EMPLOYEE_COL;
import static com.igec.common.CONSTANTS.PROJECT_COL;

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
import android.widget.CompoundButton;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.igec.admin.adapters.EmployeeAdapter;
import com.igec.admin.databinding.FragmentAddProjectBinding;
import com.igec.admin.dialogs.AddAllowanceDialog;
import com.igec.admin.dialogs.AddClientDialog;
import com.igec.admin.dialogs.LocationDialog;
import com.igec.admin.R;
import com.igec.common.firebase.Allowance;
import com.igec.common.firebase.Client;
import com.igec.common.firebase.EmployeeOverview;
import com.igec.common.firebase.EmployeesGrossSalary;
import com.igec.common.firebase.Project;
import com.igec.common.utilities.allowancesEnum;
import com.github.javafaker.Faker;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.WriteBatch;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class AddProjectFragment extends Fragment {

    // Vars
    private String day, month, year;
    private EmployeeAdapter adapter;
    private String lat, lng;
    private ArrayList<String> contract = new ArrayList<>();
    private ArrayList<Pair<TextInputLayout, EditText>> views;
    private ArrayList<Allowance> allowances = new ArrayList<>();
    private Client client;
    private String MID = null;
    private String PID;
    private long startDate;
    private MaterialDatePicker.Builder<Long> vTimeDatePickerBuilder = MaterialDatePicker.Builder.datePicker();
    private MaterialDatePicker vTimeDatePicker;
    private ArrayList<EmployeeOverview> employees;
    private static ArrayList<String> TeamID = new ArrayList<>();
    private static ArrayList<EmployeeOverview> Team = new ArrayList<>();
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private WriteBatch batch = FirebaseFirestore.getInstance().batch();

    public static void clearTeam() {
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
            batch.update(EMPLOYEE_OVERVIEW_REF, empInfoMap);
        }
        Team.clear();
        TeamID.clear();
        batch.commit();
    }

    @Override
    public void onResume() {
        super.onResume();
        setUpContractType();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getParentFragmentManager().setFragmentResultListener("client", this, (requestKey, bundle) -> {
            client = (Client) bundle.getSerializable("client");

        });
        getParentFragmentManager().setFragmentResultListener("allowances", this, (requestKey, bundle) -> {
            allowances = bundle.getParcelableArrayList("allowances");
        });
        getParentFragmentManager().setFragmentResultListener("location", this, (requestKey, result) -> {
            lat = result.getString("lat");
            lng = result.getString("lng");
        });
    }

    private FragmentAddProjectBinding binding;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentAddProjectBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initialize();
        setUpListeners();
    }

    // Functions
    private void setUpListeners() {
        // listeners
        binding.managerNameLayout.setEndIconOnClickListener(view -> {
            binding.managerNameEdit.setText(null);
            MID = null;
            adapter.setMID(null);
            adapter.notifyDataSetChanged();
        });
        binding.managerNameLayout.setErrorIconOnClickListener(view -> {
            binding.managerNameEdit.setText(null);
            MID = null;
            adapter.setMID(null);
            adapter.notifyDataSetChanged();
        });
        binding.referenceEdit.addTextChangedListener(twProjectReference);
        binding.projectAreaEdit.addTextChangedListener(twArea);
        binding.officeWorkCheckbox.setOnCheckedChangeListener(oclOfficeWork);
        binding.clientButton.setOnClickListener(oclAddClient);
        binding.registerButton.setOnClickListener(clRegister);
        binding.locateButton.setOnClickListener(clLocate);
        binding.dateLayout.setEndIconOnClickListener(oclTimeDate);
        binding.dateLayout.setErrorIconOnClickListener(oclTimeDate);
        vTimeDatePicker.addOnPositiveButtonClickListener(pclTimeDatePicker);
        binding.allowancesButton.setOnClickListener(oclAddAllowance);


        for (Pair<TextInputLayout, EditText> v : views) {
            if (v.first != binding.referenceLayout && v.first != binding.projectAreaLayout)
                v.second.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                    }

                    @Override
                    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                    }

                    @Override
                    public void afterTextChanged(Editable editable) {
                        hideError(binding.dateLayout);
                    }
                });
        }
    }

    private void initialize() {
        employees = new ArrayList<>();
        views = new ArrayList<>();
        views.add(new Pair<>(binding.nameLayout, binding.nameEdit));
        views.add(new Pair<>(binding.referenceLayout, binding.referenceEdit));
        views.add(new Pair<>(binding.areaLayout, binding.areaEdit));
        views.add(new Pair<>(binding.cityLayout, binding.cityEdit));
        views.add(new Pair<>(binding.streetLayout, binding.streetEdit));
        views.add(new Pair<>(binding.projectAreaLayout, binding.projectAreaEdit));
        views.add(new Pair<>(binding.dateLayout, binding.dateEdit));
        views.add(new Pair<>(binding.contractTypeLayout, binding.contractTypeAuto));
        views.add(new Pair<>(binding.managerNameLayout, binding.managerNameEdit));
        vTimeDatePickerBuilder.setTitleText("Time");
        vTimeDatePicker = vTimeDatePickerBuilder.build();


        binding.recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
        adapter = new EmployeeAdapter(employees, true);
        adapter.setOnItemClickListener(itclEmployeeAdapter);
        binding.recyclerView.setLayoutManager(layoutManager);
        binding.recyclerView.setAdapter(adapter);
        PID = PROJECT_COL.document().getId().substring(0, 5);
        binding.clientButton.setEnabled(!binding.officeWorkCheckbox.isChecked());
        getEmployees();
        //TODO: remove fakeData() when all testing is finished
        fakeData();

    }

    private void setUpContractType() {
        contract.clear();
        contract.add("lump sum");
        contract.add("timesheet");
        ArrayAdapter<String> ContractAdapter = new ArrayAdapter<>(getActivity(), R.layout.item_dropdown, contract);
        binding.contractTypeAuto.setAdapter(ContractAdapter);
    }

    private void changeSelectedTeam(int position) {

        batch = db.batch();
        employees.get(position).isSelected = !employees.get(position).isSelected;
        ArrayList<String> empInfo = new ArrayList<>();
        empInfo.add(employees.get(position).getFirstName());
        empInfo.add(employees.get(position).getLastName());
        empInfo.add(employees.get(position).getTitle());
        empInfo.add(null);
        empInfo.add(null);
        if (employees.get(position).isSelected) {
            Team.add(employees.get(position));
            TeamID.add(String.valueOf(employees.get(position).getId()));
            empInfo.add("1");
            Map<String, Object> empInfoMap = new HashMap<>();
            empInfoMap.put(employees.get(position).getId(), empInfo);
            batch.update(EMPLOYEE_OVERVIEW_REF, empInfoMap);
        } else {
            Team.removeIf(employeeOverview -> employeeOverview.getId().equals(employees.get(position).getId()));
            TeamID.remove(String.valueOf(employees.get(position).getId()));
            empInfo.add("0");
            Map<String, Object> empInfoMap = new HashMap<>();
            empInfoMap.put(employees.get(position).getId(), empInfo);
            batch.update(EMPLOYEE_OVERVIEW_REF, empInfoMap);
        }
        batch.commit();
        adapter.notifyItemChanged(position);
    }

    void getEmployees() {
        EMPLOYEE_OVERVIEW_REF
                .addSnapshotListener((documentSnapshot, e) -> {
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

            batch.update(EMPLOYEE_OVERVIEW_REF, empInfoMap);

            batch.update(EMPLOYEE_COL.document(emp.getId()), "managerID", emp.getManagerID(), "projectID", projectID);

            EMPLOYEE_GROSS_SALARY_COL.document(emp.getId()).get().addOnSuccessListener((value) -> {
                if (!value.exists())
                    return;
                EmployeesGrossSalary employeesGrossSalary = value.toObject(EmployeesGrossSalary.class);
                employeesGrossSalary.getAllTypes().addAll(allowances);
                batch.update(EMPLOYEE_GROSS_SALARY_COL.document(emp.getId()), "allTypes", employeesGrossSalary.getAllTypes());

                updateDate();
                EMPLOYEE_GROSS_SALARY_COL.document(emp.getId()).collection(year).document(month).get().addOnSuccessListener(documentSnapshot -> {
                    if (!documentSnapshot.exists()) {
                        if (counter[0] == Team.size() - 1) {
                            batch.commit().addOnSuccessListener(unused -> {
                                clearInputs();
                                fakeData();
                                Snackbar.make(binding.getRoot(), "Registered", Snackbar.LENGTH_SHORT).show();
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
                            Snackbar.make(binding.getRoot(), "Registered", Snackbar.LENGTH_SHORT).show();
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

    private void updateDate() {
        day = binding.dateEdit.getText().toString().substring(0, 2);
        year = binding.dateEdit.getText().toString().substring(6, 10);
        month = binding.dateEdit.getText().toString().substring(3, 5);
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
    }

    private void addProject() {

        allowances.forEach(allowance -> {
            allowance.setType(allowancesEnum.PROJECT.ordinal());
            allowance.setProjectId(PID);
        });
        Team.forEach(employeeOverview -> {
            if (employeeOverview.getId().equals(MID))
                employeeOverview.setManagerID(ADMIN);
            else
                employeeOverview.setManagerID(MID);
            employeeOverview.setProjectId(PID);
        });
        Project newProject = new Project(binding.managerNameEdit.getText().toString()
                , MID
                , binding.nameEdit.getText().toString()
                , new Date(startDate)
                , Team
                , binding.referenceEdit.getText().toString()
                , binding.cityEdit.getText().toString()
                , binding.areaEdit.getText().toString()
                , binding.streetEdit.getText().toString()
                , Double.parseDouble(lat)
                , Double.parseDouble(lng)
                , binding.contractTypeAuto.getText().toString()
                , Double.parseDouble(binding.projectAreaEdit.getText().toString()));

        newProject.setId(PID);
        newProject.setClient(binding.officeWorkCheckbox.isChecked() ? null : client);
        newProject.getAllowancesList().addAll(allowances);
        batch = db.batch();
        batch.set(PROJECT_COL.document(PID), newProject);
        updateEmployeesDetails(PID);
        PID = PROJECT_COL.document().getId().substring(0, 5);


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
            boolean matchDb = TeamID.contains(id) == isSelected;
            boolean hasNoManager = managerID == null;
            if (matchDb && hasNoManager)
                employees.add(new EmployeeOverview(firstName, lastName, title, id, projectID, isSelected));

        }
        employees.sort(Comparator.comparing(EmployeeOverview::getId));
        adapter.setEmployeeOverviewsList(employees);
        adapter.notifyDataSetChanged();

    }

    void clearInputs() {
        binding.registerButton.setEnabled(true);
        for (Pair<TextInputLayout, EditText> v : views) {
            v.second.setText(null);
        }
        TeamID.clear();
        Team.clear();
        client = null;
        vTimeDatePickerBuilder = MaterialDatePicker.Builder.datePicker();
        vTimeDatePicker = vTimeDatePickerBuilder.build();
        vTimeDatePicker.addOnPositiveButtonClickListener(pclTimeDatePicker);
        allowances.clear();
    }

    void fakeData() {
        Faker faker = new Faker();
        binding.nameEdit.setText(faker.bothify("??????"));
        binding.cityEdit.setText(faker.address().cityName());
        binding.areaEdit.setText(faker.address().cityName());
        binding.streetEdit.setText(faker.address().streetName());
        startDate = faker.date().birthday().getTime();
//        binding.dateEdit.setText(convertDateToString(startDate));

    }

    private boolean generateError() {
        for (Pair<TextInputLayout, EditText> view : views) {
            if (view.second.getText().toString().trim().isEmpty()) {
                view.first.setError("Missing");
                return true;
            }
            if (view.first.getError() != null) {
                return true;
            }
        }
        boolean isClientMissing = (!binding.officeWorkCheckbox.isChecked() && client == null);
        boolean isLocationMissing = (lat == null && lng == null);
        if (isClientMissing) {
            Snackbar.make(binding.getRoot(), "client Info Missing", Snackbar.LENGTH_SHORT).show();
            return true;
        }
        if (isLocationMissing) {
            Snackbar.make(binding.getRoot(), "Location is Missing", Snackbar.LENGTH_SHORT).show();
            return true;
        }
        return false;
    }

    boolean validateInputs() {
        return !generateError();
    }

    // Listeners
    private final View.OnClickListener oclTimeDate = v -> {
        vTimeDatePicker.show(getFragmentManager(), "DATE_PICKER");
    };
    private final MaterialPickerOnPositiveButtonClickListener pclTimeDatePicker = selection -> {
        startDate = (long) selection;
        binding.dateEdit.setText(String.format("%s", convertDateToString(startDate)));
    };
    private final View.OnClickListener clRegister = v -> {
        if (validateInputs()) {
            binding.registerButton.setEnabled(false);
            addProject();
        }
    };
    private final EmployeeAdapter.OnItemClickListener itclEmployeeAdapter = new EmployeeAdapter.OnItemClickListener() {
        @Override
        public void onItemClick(int position) {
        }

        @Override
        public void onCheckboxClick(int position) {
            changeSelectedTeam(position);
        }

        @Override
        public void onRadioClick(int position) {
            MID = employees.get(position).getId();
            binding.managerNameEdit.setText(String.format("%s %s", employees.get(position).getFirstName(), employees.get(position).getLastName()));
            adapter.setMID(MID);
            adapter.notifyDataSetChanged();
        }
    };
    private final View.OnClickListener oclAddClient = v -> {
        AddClientDialog addClientDialog;
        addClientDialog = new AddClientDialog(client);
        addClientDialog.show(getParentFragmentManager(), "");
    };
    private final CompoundButton.OnCheckedChangeListener oclOfficeWork = (v, office) -> {
        binding.clientButton.setEnabled(!office);
        binding.referenceEdit.setEnabled(!office);
        binding.referenceLayout.setEnabled(!office);
        if (office) {
            binding.referenceEdit.setText("-99999");
        } else {
            binding.referenceEdit.setText(null);
        }
    };
    private final View.OnClickListener oclAddAllowance = v -> {
        AddAllowanceDialog addAllowanceDialog = new AddAllowanceDialog((ArrayList<Allowance>) allowances.clone());
        addAllowanceDialog.show(getParentFragmentManager(), "");
    };
    private final View.OnClickListener clLocate = v -> {
        LocationDialog locationDialog;
        if (lat == null && lng == null)
            locationDialog = LocationDialog.newInstance();
        else
            locationDialog = LocationDialog.newInstance(lat, lng);
        locationDialog.show(getParentFragmentManager(), "");
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
            hideError(binding.referenceLayout);
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
            if (!binding.projectAreaEdit.getText().toString().trim().isEmpty()) {
                double value = Double.parseDouble(binding.projectAreaEdit.getText().toString());
                if (value == 0)
                    binding.projectAreaLayout.setError("Invalid value");
                else
                    hideError(binding.projectAreaLayout);
            } else
                hideError(binding.projectAreaLayout);
        }
    };
}