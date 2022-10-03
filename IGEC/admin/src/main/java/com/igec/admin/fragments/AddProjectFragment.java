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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.javafaker.Team;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.FieldValue;
import com.igec.admin.adapters.EmployeeAdapter;
import com.igec.admin.databinding.FragmentAddProjectBinding;
import com.igec.admin.dialogs.AddAllowanceDialog;
import com.igec.admin.dialogs.AddClientDialog;
import com.igec.admin.dialogs.LocationDialog;
import com.igec.admin.R;
import com.igec.admin.dialogs.ProjectEmployeesDialog;
import com.igec.admin.dialogs.ProjectManagerDialog;
import com.igec.common.firebase.Allowance;
import com.igec.common.firebase.Client;
import com.igec.common.firebase.EmployeeOverview;
import com.igec.common.firebase.EmployeesGrossSalary;
import com.igec.common.firebase.Project;
import com.igec.common.utilities.AllowancesEnum;
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
    private String lat, lng;
    private ArrayList<String> contract = new ArrayList<>();
    private ArrayList<Pair<TextInputLayout, EditText>> views;
    private ArrayList<Allowance> allowances = new ArrayList<>();
    private Client client;
    private String PID;
    private long startDate;
    private MaterialDatePicker.Builder<Long> vTimeDatePickerBuilder = MaterialDatePicker.Builder.datePicker();
    private MaterialDatePicker vTimeDatePicker;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private ArrayList<EmployeeOverview> team;
    private EmployeeOverview projectManager;
    private WriteBatch batch = FirebaseFirestore.getInstance().batch();

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
        getParentFragmentManager().setFragmentResultListener("teamMembers", this, (requestKey, bundle) -> {
            team = bundle.getParcelableArrayList("teamMembers");
            Toast.makeText(getActivity(), String.format("%d", team.size()), Toast.LENGTH_SHORT).show();
        });
        getParentFragmentManager().setFragmentResultListener("manager", this, (requestKey, bundle) -> {
            projectManager = bundle.getParcelable("manager");
            if (projectManager != null)
                binding.managerNameEdit.setText(String.format("%s - %s %s", projectManager.getId(), projectManager.getFirstName(), projectManager.getLastName()));
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
        binding.employeesButton.setOnClickListener(oclEmployees);
        binding.managerButton.setOnClickListener(oclManager);


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
        team = new ArrayList<>();
        views = new ArrayList<>();
        views.add(new Pair<>(binding.nameLayout, binding.nameEdit));
        views.add(new Pair<>(binding.referenceLayout, binding.referenceEdit));
        views.add(new Pair<>(binding.areaLayout, binding.areaEdit));
        views.add(new Pair<>(binding.cityLayout, binding.cityEdit));
        views.add(new Pair<>(binding.streetLayout, binding.streetEdit));
        views.add(new Pair<>(binding.projectAreaLayout, binding.projectAreaEdit));
        views.add(new Pair<>(binding.dateLayout, binding.dateEdit));
        views.add(new Pair<>(binding.hoursLayout, binding.hoursEdit));
        views.add(new Pair<>(binding.contractTypeLayout, binding.contractTypeAuto));
        views.add(new Pair<>(binding.managerNameLayout, binding.managerNameEdit));
        vTimeDatePickerBuilder.setTitleText("Time");
        vTimeDatePicker = vTimeDatePickerBuilder.build();


        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
        PID = PROJECT_COL.document().getId().substring(0, 5);
        binding.clientButton.setEnabled(!binding.officeWorkCheckbox.isChecked());
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
            allowance.setType(AllowancesEnum.PROJECT.ordinal());
            allowance.setProjectId(PID);
        });
        team.forEach(employeeOverview -> {
            employeeOverview.setManagerID(projectManager.getId());
            employeeOverview.getProjectIds().add(PID);
        });
        projectManager.setManagerID(ADMIN);
        projectManager.getProjectIds().add(PID);
        team.add(projectManager);

        Project newProject = new Project(projectManager.getFirstName() + projectManager.getLastName()
                , projectManager.getId()
                , binding.nameEdit.getText().toString()
                , new Date(startDate)
                , team
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

    }

    private void updateEmployeesDetails(String projectID) {

        EmployeeOverview temp = null;
        try {
            temp = projectManager.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        temp.setProjectIds(projectManager.getProjectIds());
        temp.getProjectIds().remove(PID);
        for (String pid : projectManager.getProjectIds()) {
            if (pid.equals(PID))
                continue;
            batch.update(PROJECT_COL.document(pid), "employees", FieldValue.arrayRemove(temp));
            batch.update(PROJECT_COL.document(pid), "employees", FieldValue.arrayUnion(projectManager));
        }
        final int[] counter = {0};
        team.forEach(emp -> {
            ArrayList<Object> empInfo = new ArrayList<>();
            empInfo.add(emp.getFirstName());
            empInfo.add(emp.getLastName());
            empInfo.add(emp.getTitle());
            empInfo.add(emp.getManagerID());
            empInfo.add(new HashMap<String, Object>() {{
                put("pids", emp.getProjectIds());
            }});
            empInfo.add(true);
            empInfo.add(emp.isManager);
            Map<String, Object> empInfoMap = new HashMap<>();
            empInfoMap.put(emp.getId(), empInfo);

            batch.update(EMPLOYEE_OVERVIEW_REF, empInfoMap);

            batch.update(EMPLOYEE_COL.document(emp.getId()), "managerID", emp.getManagerID(), "projectIds", FieldValue.arrayUnion(projectID));

            EMPLOYEE_GROSS_SALARY_COL.document(emp.getId()).get().addOnSuccessListener((value) -> {
                if (!value.exists())
                    return;
                EmployeesGrossSalary employeesGrossSalary = value.toObject(EmployeesGrossSalary.class);
                employeesGrossSalary.getAllTypes().addAll(allowances);
                batch.update(EMPLOYEE_GROSS_SALARY_COL.document(emp.getId()), "allTypes", employeesGrossSalary.getAllTypes());

                updateDate();

                EMPLOYEE_GROSS_SALARY_COL.document(emp.getId()).collection(year).document(month).get().addOnSuccessListener(documentSnapshot -> {
                    if (!documentSnapshot.exists()) {
                        if (counter[0] == team.size() - 1) {
                            batch.commit().addOnSuccessListener(unused -> {
                                clearInputs();
                                PID = PROJECT_COL.document().getId().substring(0, 5);
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
                        employeesGrossSalary1.getBaseAllowances().removeIf(allowance -> allowance.getType() == AllowancesEnum.PROJECT.ordinal());
                        employeesGrossSalary1.getBaseAllowances().addAll(allowances);
                    }
                    batch.set(db.document(documentSnapshot.getReference().getPath()), employeesGrossSalary1, SetOptions.mergeFields("baseAllowances"));
                    if (counter[0] == team.size() - 1) {
                        batch.commit().addOnSuccessListener(unused -> {
                            clearInputs();
                            fakeData();
                            PID = PROJECT_COL.document().getId().substring(0, 5);
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

    void clearInputs() {
        binding.registerButton.setEnabled(true);
        for (Pair<TextInputLayout, EditText> v : views) {
            v.second.setText(null);
        }
        binding.officeWorkCheckbox.setChecked(false);
        binding.referenceLayout.setEnabled(true);
        binding.referenceEdit.setEnabled(true);
        client = null;
        vTimeDatePickerBuilder = MaterialDatePicker.Builder.datePicker();
        vTimeDatePicker = vTimeDatePickerBuilder.build();
        vTimeDatePicker.addOnPositiveButtonClickListener(pclTimeDatePicker);
        allowances.clear();
        team.clear();
        projectManager = null;
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
    private final View.OnClickListener oclEmployees = v -> {
        ProjectEmployeesDialog projectEmployeesDialog;
        projectEmployeesDialog = ProjectEmployeesDialog.newInstance(team);
        projectEmployeesDialog.show(getParentFragmentManager(), "");
    };
    private final View.OnClickListener oclManager = v -> {
        ProjectManagerDialog projectManagerDialog = ProjectManagerDialog.newInstance(projectManager);
        projectManagerDialog.show(getParentFragmentManager(), "");
    };
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
            MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(getActivity());
            builder.setTitle(getString(R.string.hours))
                    .setMessage(getString(R.string.hours_limit, binding.hoursEdit.getText().toString()))
                    .setNegativeButton(getString(R.string.no), (dialogInterface, i) -> {
                        binding.registerButton.setEnabled(true);
                    })
                    .setPositiveButton(getString(R.string.yes), (dialogInterface, i) -> {
                        addProject();
                    })
                    .show();
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