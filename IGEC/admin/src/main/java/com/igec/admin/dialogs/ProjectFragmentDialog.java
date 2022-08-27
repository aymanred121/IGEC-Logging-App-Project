package com.igec.admin.dialogs;

import static android.content.ContentValues.TAG;

import static com.igec.common.CONSTANTS.ADMIN;
import static com.igec.common.CONSTANTS.EMPLOYEE_COL;
import static com.igec.common.CONSTANTS.EMPLOYEE_GROSS_SALARY_COL;
import static com.igec.common.CONSTANTS.EMPLOYEE_OVERVIEW_REF;
import static com.igec.common.CONSTANTS.PROJECT_COL;

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
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.collection.ArraySet;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentResultListener;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.igec.admin.adapters.EmployeeAdapter;
import com.igec.admin.databinding.FragmentAddProjectBinding;
import com.igec.admin.fragments.ProjectsFragment;
import com.igec.admin.R;
import com.igec.common.firebase.Allowance;
import com.igec.common.firebase.Client;
import com.igec.common.firebase.EmployeeOverview;
import com.igec.common.firebase.EmployeesGrossSalary;
import com.igec.common.firebase.Project;
import com.igec.common.utilities.AllowancesEnum;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class ProjectFragmentDialog extends DialogFragment {


    // Views
    private EmployeeAdapter adapter;

    // Vars
    private String MID;
    private String lat, lng;
    private ArrayList<Allowance> allowances;
    long startDate;
    private Client client;
    private final MaterialDatePicker.Builder<Long> vTimeDatePickerBuilder = MaterialDatePicker.Builder.datePicker();
    private MaterialDatePicker vTimeDatePicker;
    ArrayList<EmployeeOverview> employees = new ArrayList<>();
    private static ArraySet<String> TeamID = new ArraySet<>();
    private static ArrayList<EmployeeOverview> Team = new ArrayList<>();
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    Project project;
    private WriteBatch batch = FirebaseFirestore.getInstance().batch();
    private final SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
    private ArrayList<Pair<TextInputLayout, EditText>> views;

    public ProjectFragmentDialog(Project project) {
        this.project = project;
        MID = project.getManagerID();
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
                batch.update(EMPLOYEE_OVERVIEW_REF, empInfoMap);
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
            }
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
    public void onDestroy() {
        super.onDestroy();
        int parent = getParentFragmentManager().getFragments().size() - 1;
        ((ProjectsFragment) getParentFragmentManager().getFragments().get(parent)).setOpened(false);
        binding = null;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initialize();
        // listeners
        binding.referenceEdit.addTextChangedListener(twProjectReference);
        binding.projectAreaEdit.addTextChangedListener(twArea);
        binding.updateButton.setOnClickListener(clUpdate);
        binding.deleteButton.setOnClickListener(clDelete);
        binding.locateButton.setOnClickListener(clLocate);
        vTimeDatePicker.addOnPositiveButtonClickListener(pclTimeDatePicker);
        binding.dateLayout.setEndIconOnClickListener(oclStartDate);
        binding.officeWorkCheckbox.setOnClickListener(oclOfficeWork);
        binding.clientButton.setOnClickListener(oclAddClient);
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
                        hideError(v.first);
                    }
                });
        }

        // Inflate the layout for this fragment
    }

    // Functions
    private void initialize() {
        binding.managerNameEdit.setText(project.getManagerName());
        binding.managerNameLayout.setEndIconDrawable(null);
        binding.managerNameLayout.setErrorIconDrawable(null);
        allowances = new ArrayList<>();
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

        binding.registerButton.setVisibility(View.GONE);
        binding.deleteButton.setVisibility(View.VISIBLE);
        binding.updateButton.setVisibility(View.VISIBLE);
        binding.recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
        adapter = new EmployeeAdapter(employees, true);
        adapter.setMID(MID);
        adapter.setOnItemClickListener(itclEmployeeAdapter);
        binding.recyclerView.setLayoutManager(layoutManager);
        binding.recyclerView.setAdapter(adapter);
        allowances.addAll(project.getAllowancesList());
        client = project.getClient();
        binding.nameEdit.setText(project.getName());
        binding.managerNameEdit.setText(project.getManagerName());
        binding.projectAreaEdit.setText(String.valueOf((long) project.getArea()));
        binding.contractTypeAuto.setText(project.getContractType());
        binding.contractTypeLayout.setEnabled(true);
        binding.contractTypeAuto.setEnabled(false);
        boolean isOfficeWork = project.getReference().equals("-99999");
        binding.referenceEdit.setText(project.getReference());
        binding.officeWorkCheckbox.setChecked(isOfficeWork);
        binding.clientButton.setEnabled(!isOfficeWork);
        binding.referenceLayout.setEnabled(!isOfficeWork);
        binding.referenceEdit.setEnabled(!isOfficeWork);
        binding.areaEdit.setText(project.getLocationArea());
        binding.cityEdit.setText(project.getLocationCity());
        binding.streetEdit.setText(project.getLocationStreet());
        startDate = project.getStartDate().getTime();
        vTimeDatePickerBuilder.setTitleText("Time");
        vTimeDatePicker = vTimeDatePickerBuilder.setSelection(startDate).build();
        binding.dateEdit.setText(String.format("%s", convertDateToString(startDate)));
        lat = String.valueOf(project.getLat());
        lng = String.valueOf(project.getLng());
        getEmployees();
        ArrayList<String> contract = new ArrayList<>();
        contract.add("lump sum");
        contract.add("timesheet");
        ArrayAdapter<String> ContractAdapter = new ArrayAdapter<>(getActivity(), R.layout.item_dropdown, contract);
        binding.contractTypeAuto.setAdapter(ContractAdapter);
    }

    String convertDateToString(long selection) {
        @SuppressLint("SimpleDateFormat") SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy");
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(selection);
        return simpleDateFormat.format(calendar.getTime());
    }

    private void ChangeSelectedTeam(int position) {

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
            if (employees.get(position).getManagerID() == null && employees.get(position).getProjectId() == null)
                batch.update(EMPLOYEE_OVERVIEW_REF, empInfoMap);
        } else {
            Team.removeIf(employeeOverview -> employeeOverview.getId().equals(employees.get(position).getId()));
            TeamID.remove(String.valueOf(employees.get(position).getId()));
            empInfo.add("0");
            Map<String, Object> empInfoMap = new HashMap<>();
            empInfoMap.put(employees.get(position).getId(), empInfo);
            if (employees.get(position).getManagerID() == null && employees.get(position).getProjectId() == null)
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
                    if (documentSnapshot.exists()) {
                        Map<String, ArrayList<String>> empMap;
                        empMap = (HashMap) documentSnapshot.getData();
                        retrieveEmployees(empMap);
                    }
                });
    }

    private void updateEmployeesDetails() {
        String currentDateAndTime = sdf.format(new Date());
        String month = currentDateAndTime.substring(3, 5);
        String year = currentDateAndTime.substring(6, 10);
        batch = FirebaseFirestore.getInstance().batch();
        employees.forEach(emp -> {
            if (emp.getManagerID() != null && emp.getProjectId() != null && !emp.isSelected) {
                // removed
                emp.setProjectId(null);
                emp.setManagerID(null);
                EMPLOYEE_GROSS_SALARY_COL.document(emp.getId()).get().addOnSuccessListener(doc -> {
                    if (!doc.exists())
                        return;
                    EmployeesGrossSalary employeesGrossSalary = doc.toObject(EmployeesGrossSalary.class);
                    employeesGrossSalary.getAllTypes().removeIf(x -> x.getProjectId().equals(project.getId()));
                    db.document(doc.getReference().getPath()).update("allTypes", employeesGrossSalary.getAllTypes());
                    employeesGrossSalary.getAllTypes().removeIf(x -> x.getType() == AllowancesEnum.NETSALARY.ordinal());
                    EMPLOYEE_GROSS_SALARY_COL.document(emp.getId()).collection(year).document(month).update("baseAllowances", employeesGrossSalary.getAllTypes());
                });
            } else if (TeamID.contains(emp.getId())) {
                // newly added
                emp.setProjectId(project.getId());
                emp.setManagerID(emp.getId().equals(MID) ? ADMIN : MID);
            }
            ArrayList<String> empInfo = new ArrayList<>();
            empInfo.add(emp.getFirstName());
            empInfo.add(emp.getLastName());
            empInfo.add(emp.getTitle());
            empInfo.add(emp.getManagerID());
            empInfo.add(emp.getProjectId());
            empInfo.add(emp.isSelected ? "1" : "0");
            Map<String, Object> empInfoMap = new HashMap<>();
            empInfoMap.put(emp.getId(), empInfo);
            batch.update(EMPLOYEE_OVERVIEW_REF, empInfoMap);
            batch.update(EMPLOYEE_COL.document(emp.getId()), "managerID", emp.getManagerID(), "projectID", emp.getProjectId());
        });
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
            boolean isSelected = empMap.get(key).get(5).equals("1");
            String id = (key);
            EmployeeOverview newEmp = new EmployeeOverview(firstName, lastName, title, id, projectID, isSelected);
            if (project.getId().equals(projectID)) {
                newEmp.setManagerID(id.equals(project.getManagerID()) ? ADMIN : MID);
                newEmp.isSelected = true;
                if (!TeamID.contains(id)) {
                    TeamID.add(newEmp.getId());
                    Team.add(newEmp);
                }
            }
            boolean matchWithDb = TeamID.contains(id) == isSelected;
            boolean hasNoManager = managerID == null;
            if (matchWithDb && (hasNoManager || project.getId().equals(projectID)))
                employees.add(newEmp);


        }
        employees.sort(Comparator.comparing(EmployeeOverview::getId));
        adapter.setEmployeeOverviewsList(employees);
        adapter.notifyDataSetChanged();
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
        }
        if (isLocationMissing) {
            Snackbar.make(binding.getRoot(), "Location is Missing", Snackbar.LENGTH_SHORT).show();
        }
        return isClientMissing || isLocationMissing;
    }

    boolean validateInputs() {
        return !generateError();
    }

    void updateProject() {
        updateEmployeesDetails();
        Team.forEach(employeeOverview -> {
            if (employeeOverview.getId().equals(MID))
                employeeOverview.setManagerID(ADMIN);
            else
                employeeOverview.setManagerID(MID);
            employeeOverview.setProjectId(project.getId());
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
        newProject.setId(project.getId());
        newProject.setClient(binding.officeWorkCheckbox.isChecked() ? null : client);
        newProject.setMachineWorkedTime(project.getMachineWorkedTime());
        //Added projectId to each allowance that is coming from project
        allowances.stream().flatMap(allowance -> {
            allowance.setProjectId(project.getId());
            return null;
        }).collect(Collectors.toList());
        newProject.getAllowancesList().addAll(allowances);
        allowances = newProject.getAllowancesList();
        batch.set(PROJECT_COL.document(project.getId()), newProject);
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
            EMPLOYEE_GROSS_SALARY_COL.document(emp.getId()).get().addOnSuccessListener((value) -> {
                if (!value.exists())
                    return;
                EmployeesGrossSalary employeesGrossSalary = value.toObject(EmployeesGrossSalary.class);
                if (allowances.size() != 0) {
                    employeesGrossSalary.getAllTypes().removeIf(allowance -> allowance.getType() == AllowancesEnum.PROJECT.ordinal());
                    employeesGrossSalary.getAllTypes().addAll(allowances);
                }
                batch.update(EMPLOYEE_GROSS_SALARY_COL.document(emp.getId()), "allTypes", employeesGrossSalary.getAllTypes());
                EMPLOYEE_GROSS_SALARY_COL.document(emp.getId()).collection(finalYear).document(finalMonth)
                        .get().addOnSuccessListener(documentSnapshot -> {
                            if (!documentSnapshot.exists()) {
                                //new month
//                                        employeesGrossSalary.getAllTypes().removeIf(allowance -> allowance.getType() == allowancesEnum.PROJECT.ordinal());
//                                        employeesGrossSalary.setBaseAllowances(allowances);
//                                        batch.set(db.document(documentSnapshot.getReference().getPath()), employeesGrossSalary);
                                if (counter[0] == newProject.getEmployees().size() - 1) {
                                    batch.commit().addOnSuccessListener(unused1 -> {
                                        Snackbar snackbar = Snackbar.make(binding.getRoot(), "Updated", Snackbar.LENGTH_SHORT);

                                        snackbar.show();
                                        dismiss();
                                        binding.updateButton.setEnabled(true);
                                        batch = FirebaseFirestore.getInstance().batch();
                                        project.setEmployees(null);
                                        employees.clear();
                                        Team.clear();
                                        TeamID.clear();
                                    });
                                }
                                counter[0]++;
                                return;
                            }
                            EmployeesGrossSalary employeesGrossSalary1 = documentSnapshot.toObject(EmployeesGrossSalary.class);
                            if (employeesGrossSalary1.getBaseAllowances() == null)
                                employeesGrossSalary1.setBaseAllowances(allowances);
                            else {
                                employeesGrossSalary1.getBaseAllowances().removeIf(allowance -> allowance.getType() == AllowancesEnum.PROJECT.ordinal());
                                employeesGrossSalary1.getBaseAllowances().addAll(allowances);
                            }
                            batch.update(db.document(documentSnapshot.getReference().getPath()), "baseAllowances", employeesGrossSalary1.getBaseAllowances());
                            if (counter[0] == newProject.getEmployees().size() - 1) {
                                batch.commit().addOnSuccessListener(unused1 -> {
                                    Snackbar snackbar = Snackbar.make(binding.getRoot(), "Updated", Snackbar.LENGTH_SHORT);

                                    snackbar.show();
                                    dismiss();
                                    binding.updateButton.setEnabled(true);
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
        String currentDateAndTime = sdf.format(new Date());
        String month = currentDateAndTime.substring(3, 5);
        String year = currentDateAndTime.substring(6, 10);
        batch.delete(PROJECT_COL.document(project.getId()));
        project.getEmployees().forEach(member -> {
            member.setManagerID(null);
            member.setProjectId(null);
            member.isSelected = false;
            EMPLOYEE_GROSS_SALARY_COL.document(member.getId()).get().addOnSuccessListener(doc -> {
                if (!doc.exists())
                    return;
                EmployeesGrossSalary employeesGrossSalary = doc.toObject(EmployeesGrossSalary.class);
                employeesGrossSalary.getAllTypes().removeIf(x -> x.getProjectId().equals(project.getId()));
                db.document(doc.getReference().getPath()).update("allTypes", employeesGrossSalary.getAllTypes());
                employeesGrossSalary.getAllTypes().removeIf(x -> x.getType() == AllowancesEnum.NETSALARY.ordinal());
                EMPLOYEE_GROSS_SALARY_COL.document(member.getId()).collection(year).document(month).update("baseAllowances", employeesGrossSalary.getAllTypes());
            });
            ArrayList<String> empInfo = new ArrayList<>();
            empInfo.add(member.getFirstName());
            empInfo.add(member.getLastName());
            empInfo.add(member.getTitle());
            empInfo.add(null);
            empInfo.add(null);
            empInfo.add("0");
            Map<String, Object> empInfoMap = new HashMap<>();
            empInfoMap.put(member.getId(), empInfo);
            batch.update(EMPLOYEE_OVERVIEW_REF, empInfoMap);
            batch.update(EMPLOYEE_COL.document(member.getId()), "managerID", null, "projectID", null);
        });
        batch.commit().addOnSuccessListener(unused2 -> {
            batch = FirebaseFirestore.getInstance().batch();
            Snackbar.make(binding.getRoot(), "Deleted", Snackbar.LENGTH_SHORT).show();
            binding.deleteButton.setEnabled(true);
            dismiss();
        });
    }

    private void hideError(TextInputLayout layout) {
        layout.setError(null);
        layout.setErrorEnabled(false);
    }

    // Listeners
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
    private final MaterialPickerOnPositiveButtonClickListener pclTimeDatePicker = new MaterialPickerOnPositiveButtonClickListener() {
        @Override
        public void onPositiveButtonClick(Object selection) {
            startDate = (long) selection;
            binding.dateEdit.setText(String.format("%s", convertDateToString(startDate)));
        }
    };
    private final View.OnClickListener oclStartDate = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            vTimeDatePicker.show(getFragmentManager(), "DATE_PICKER");
        }
    };
    private final View.OnClickListener clUpdate = v -> {
        if (validateInputs()) {
            binding.updateButton.setEnabled(false);
            updateProject();
        }
    };
    private final View.OnClickListener clDelete = v -> {
        binding.deleteButton.setEnabled(false);
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(getActivity());
        builder.setTitle(getString(R.string.Delete))
                .setMessage(getString(R.string.AreUSure))
                .setNegativeButton(getString(R.string.no), (dialogInterface, i) -> {
                    binding.deleteButton.setEnabled(true);
                })
                .setPositiveButton(getString(R.string.yes), (dialogInterface, i) -> {
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
            binding.clientButton.setEnabled(!binding.officeWorkCheckbox.isChecked());
            binding.referenceEdit.setEnabled(!binding.officeWorkCheckbox.isChecked());
            binding.referenceLayout.setEnabled(!binding.officeWorkCheckbox.isChecked());
            if (binding.officeWorkCheckbox.isChecked()) {
                binding.referenceEdit.setText("-99999");
            } else {
                binding.referenceEdit.setText(null);
            }

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

        @Override
        public void onRadioClick(int position) {
            MID = employees.get(position).getId();
            binding.managerNameEdit.setText(String.format("%s %s", employees.get(position).getFirstName(), employees.get(position).getLastName()));
            adapter.setMID(MID);
            adapter.notifyDataSetChanged();
        }
    };
    private final View.OnClickListener oclAddAllowance = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            AddAllowanceDialog addAllowanceDialog = new AddAllowanceDialog((ArrayList<Allowance>) allowances.clone());
            addAllowanceDialog.show(getParentFragmentManager(), "");
        }
    };

}
