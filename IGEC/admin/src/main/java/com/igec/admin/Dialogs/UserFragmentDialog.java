package com.igec.admin.Dialogs;

import static com.igec.common.cryptography.RSAUtil.encrypt;
import static com.google.android.material.textfield.TextInputLayout.END_ICON_CUSTOM;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
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

import com.igec.admin.R;
import com.igec.admin.databinding.FragmentAddUserBinding;
import com.igec.common.firebase.Allowance;
import com.igec.common.firebase.Employee;
import com.igec.common.firebase.EmployeeOverview;
import com.igec.common.firebase.EmployeesGrossSalary;
import com.igec.common.firebase.Project;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.SetOptions;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class UserFragmentDialog extends DialogFragment {

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.FullscreenDialogTheme);
    }

    private final int PROJECT = 0;
    private final int NETSALARY = 1;
    private final int ALLOWANCE = 2;
    private final int BONUS = 3;
    private final int PENALTY = 4;
    // Views
    private final MaterialDatePicker.Builder<Long> vDatePickerBuilder = MaterialDatePicker.Builder.datePicker();
    private MaterialDatePicker vDatePicker;
    private ArrayList<Pair<TextInputLayout, EditText>> views;


    //Var
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference machineEmployeeCol = db.collection("Machine_Employee");
    private CollectionReference vacationCol = db.collection("Vacation");
    private CollectionReference projectCol = db.collection("projects");
    private CollectionReference summaryCOl = db.collection("summary");
    private DocumentReference employeeOverviewRef = db.collection("EmployeeOverview").document("emp");
    private Employee employee;
    private ArrayList<EmployeeOverview> employeeOverviewArrayList;
    private int currEmpOverviewPos;
    private long hireDate;

    public UserFragmentDialog(Employee employee, ArrayList<EmployeeOverview> employeeOverviewArrayList, int currEmpOverviewPos) {
        this.employee = employee;
        this.employeeOverviewArrayList = employeeOverviewArrayList;
        this.currEmpOverviewPos = currEmpOverviewPos;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {


        Dialog dialog = super.onCreateDialog(savedInstanceState);
        Window window = dialog.getWindow();

        if (window != null) {
            dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_MODE_CHANGED);
        }

        return dialog;
    }

    private FragmentAddUserBinding binding;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentAddUserBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initialize();
        // Listeners
        binding.emailEdit.addTextChangedListener(twEmail);
        vDatePicker.addOnPositiveButtonClickListener(pclDatePicker);
        binding.hireDateLayout.setEndIconOnClickListener(oclHireDate);
        binding.hireDateLayout.setErrorIconOnClickListener(oclHireDate);
        binding.updateButton.setOnClickListener(clUpdate);
        binding.deleteButton.setOnClickListener(clDelete);
        binding.unlockButton.setOnClickListener(clUnlock);
        binding.passwordLayout.setEndIconOnClickListener(oclPasswordGenerate);
        for (Pair<TextInputLayout, EditText> v : views) {
            if (v.first != binding.emailLayout)
                v.second.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                    }

                    @Override
                    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                    }

                    @Override
                    public void afterTextChanged(Editable editable) {
                        v.first.setError(null);
                        v.first.setErrorEnabled(false);
                    }
                });
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onResume() {
        super.onResume();
        ArrayList<String> currencies = new ArrayList<>();
        currencies.add("EGP");
        currencies.add("SAR");
        ArrayAdapter<String> currenciesAdapter = new ArrayAdapter<>(getActivity(), R.layout.item_dropdown, currencies);
        binding.currencyAuto.setAdapter(currenciesAdapter);
    }

    // Functions
    private void initialize() {
        vDatePickerBuilder.setTitleText("Hire Date");
        vDatePicker = vDatePickerBuilder.build();
        binding.registerButton.setVisibility(View.GONE);
        binding.deleteButton.setVisibility(View.VISIBLE);
        binding.updateButton.setVisibility(View.VISIBLE);
        binding.unlockButton.setVisibility(View.VISIBLE);


        views = new ArrayList<>();
        views.add(new Pair<>(binding.firstNameLayout, binding.firstNameEdit));
        views.add(new Pair<>(binding.secondNameLayout, binding.secondNameEdit));
        views.add(new Pair<>(binding.emailLayout, binding.emailEdit));
        views.add(new Pair<>(binding.passwordLayout, binding.passwordEdit));
        views.add(new Pair<>(binding.phoneLayout, binding.phoneEdit));
        views.add(new Pair<>(binding.titleLayout, binding.titleEdit));
        views.add(new Pair<>(binding.salaryLayout, binding.salaryEdit));
        views.add(new Pair<>(binding.currencyLayout, binding.currencyAuto));
        views.add(new Pair<>(binding.insuranceNumberLayout, binding.insuranceNumberEdit));
        views.add(new Pair<>(binding.insuranceAmountLayout, binding.insuranceAmountEdit));
        views.add(new Pair<>(binding.areaLayout, binding.areaEdit));
        views.add(new Pair<>(binding.cityLayout, binding.cityEdit));
        views.add(new Pair<>(binding.streetLayout, binding.streetEdit));
        views.add(new Pair<>(binding.hireDateLayout, binding.hireDateEdit));
        views.add(new Pair<>(binding.nationalIdLayout, binding.nationalIdEdit));


        binding.firstNameEdit.setText(employee.getFirstName());
        binding.secondNameEdit.setText(employee.getLastName());
        binding.titleEdit.setText(employee.getTitle());
        binding.areaEdit.setText(employee.getArea());
        binding.cityEdit.setText(employee.getCity());
        binding.streetEdit.setText(employee.getStreet());
        binding.emailEdit.setText(employee.getEmail());
        binding.salaryEdit.setText(String.valueOf(employee.getSalary()));
        binding.currencyAuto.setText(employee.getCurrency());
        binding.nationalIdEdit.setText(employee.getSSN());
        binding.passwordEdit.setText(employee.getDecryptedPassword());
        binding.phoneEdit.setText(employee.getPhoneNumber());
        binding.adminCheckbox.setChecked(employee.isAdmin());
        binding.temporaryCheckbox.setChecked(employee.isTemporary());
        binding.insuranceNumberEdit.setText(employee.getInsuranceNumber());
        binding.insuranceAmountEdit.setText(String.valueOf(employee.getInsuranceAmount()));
        binding.hireDateEdit.setText(convertDateToString(employee.getHireDate().getTime()));
        hireDate = employee.getHireDate().getTime();
        vDatePickerBuilder.setTitleText("Hire Date");
        vDatePicker = vDatePickerBuilder.setSelection(hireDate).build();
        binding.passwordLayout.setEndIconMode(END_ICON_CUSTOM);
        binding.passwordLayout.setEndIconDrawable(R.drawable.ic_baseline_autorenew_24);
        binding.passwordEdit.setEnabled(false);
        binding.deleteButton.setEnabled(employee.getManagerID() == null || !employee.getManagerID().equals("adminID"));
        db.collection("Machine_Employee").whereEqualTo("employee.id", employee.getId()).addSnapshotListener((docs, e) -> {
            // no machines found = enabled X
            // a machine without a check-out = disabled
            // all machines have been checked-out = enabled
            for (QueryDocumentSnapshot doc : docs) {
                if (doc.get("checkOut") == null || ((HashMap) doc.get("checkOut")).size() == 0) {
                    binding.deleteButton.setEnabled(false);
                    return;
                }
                binding.deleteButton.setEnabled(true);
            }
        });
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
        boolean isNationalIdValid = binding.nationalIdEdit.getText().toString().length() == 14;
        if (!isNationalIdValid) {
            binding.nationalIdLayout.setError("Must be 14 digits");
            return true;
        }
        return false;
    }

    boolean validateInputs() {
        return !generateError();
    }

    private void updateEmployeeOverview(Map<String, Object> updatedEmpOverviewMap, HashMap<String, Object> updatedEmployeeMap) {
        employeeOverviewRef.update(updatedEmpOverviewMap).addOnSuccessListener(unused -> updateMachineEmployee(updatedEmployeeMap));
    }

    private void updateMachineEmployee(HashMap<String, Object> updatedEmployeeMap) {
        machineEmployeeCol.whereEqualTo("Employee.id", employee.getId()).get().addOnSuccessListener(queryDocumentSnapshots -> {
            for (QueryDocumentSnapshot d : queryDocumentSnapshots) {
                machineEmployeeCol.document(d.getId()).update("Employee", updatedEmployeeMap);
            }
            //updateVacation(updatedEmployeeMap);
            updateProjects(updatedEmployeeMap);
        });
    }

    private void updateVacation(HashMap<String, Object> updatedEmployeeMap) {
        vacationCol.whereEqualTo("employee.id", employee.getId()).get().addOnSuccessListener(queryDocumentSnapshots -> {
            for (QueryDocumentSnapshot d : queryDocumentSnapshots) {
                vacationCol.document(d.getId()).update("employee", updatedEmployeeMap);
            }
            vacationCol.whereEqualTo("manager.id", employee.getId()).get().addOnSuccessListener(queryDocumentSnapshot -> {
                for (QueryDocumentSnapshot d : queryDocumentSnapshot) {
                    vacationCol.document(d.getId()).update("manager", updatedEmployeeMap);
                }
                updateSummary(updatedEmployeeMap);
            });
        });

    }

    private void updateSummary(HashMap<String, Object> updatedEmployeeMap) {
        summaryCOl.whereEqualTo("Employee.id", employee.getId()).get().addOnSuccessListener(queryDocumentSnapshots -> {
            for (QueryDocumentSnapshot d : queryDocumentSnapshots) {
                summaryCOl.document(d.getId()).update("Employee", updatedEmployeeMap);
            }
            binding.updateButton.setEnabled(true);
            Toast.makeText(getActivity(), "Updated", Toast.LENGTH_SHORT).show();
            dismiss();
        });
    }

    private void updateProjects(HashMap<String, Object> updatedEmployeeMap) {
        EmployeeOverview tempEmp = new EmployeeOverview(binding.firstNameEdit.getText().toString(), binding.secondNameEdit.getText().toString(), binding.titleEdit.getText().toString(), employee.getId(), employee.getProjectID(), employee.getProjectID() != null);
        tempEmp.setManagerID(employee.getManagerID());
        employeeOverviewArrayList.set(currEmpOverviewPos, tempEmp);
        if (employee.getProjectID() == null) {
            updateVacation(updatedEmployeeMap);
            return;
        }
        projectCol.document(employee.getProjectID()).get().addOnSuccessListener(documentSnapshot ->
        {
            Project currProject = documentSnapshot.toObject(Project.class);
            ArrayList<EmployeeOverview> temp = currProject.getEmployees();
            EmployeeOverview tempemp = null;
            for (int i = 0; i < temp.size(); i++) {
                if (employee.getId().equals(temp.get(i).getId())) {
                    tempemp = temp.get(i);
                }
            }
            temp.remove(tempemp);
            temp.add(tempEmp);
            currProject.setEmployees(temp);
            if (tempEmp.getManagerID().equals("adminID")) {
                projectCol.document(employee.getProjectID()).update("managerName", tempEmp.getFirstName() + " " + tempEmp.getLastName(), "employees", temp);
            } else {
                projectCol.document(employee.getProjectID()).update("employees", temp);
            }
            updateVacation(updatedEmployeeMap);
        });
    }

    private HashMap<String, Object> fillEmployeeData() {
        HashMap<String, Object> empMap = new HashMap<>();
        empMap.put("id", employee.getId());
        empMap.put("firstName", binding.firstNameEdit.getText().toString());
        empMap.put("lastName", binding.secondNameEdit.getText().toString());
        empMap.put("title", binding.titleEdit.getText().toString());
        empMap.put("email", binding.emailEdit.getText().toString());
        empMap.put("password", encryptedPassword());
        empMap.put("salary", Double.parseDouble(binding.salaryEdit.getText().toString()));
        empMap.put("currency", binding.currencyAuto.getText().toString());
        empMap.put("ssn", binding.nationalIdEdit.getText().toString());
        empMap.put("hireDate", new Date(hireDate));
        empMap.put("phoneNumber", binding.phoneEdit.getText().toString());
        empMap.put("area", binding.areaEdit.getText().toString());
        empMap.put("street", binding.streetEdit.getText().toString());
        empMap.put("city", binding.cityEdit.getText().toString());
        empMap.put("overTime", (Double.parseDouble(binding.salaryEdit.getText().toString()) / 30.0 / 10.0) * 1.5);
        empMap.put("admin", binding.adminCheckbox.isChecked());
//        if(binding.adminCheckbox.isChecked()){
//            empMap.put("managerID", null);
//            empMap.put("projectID", null);
//        }
        return empMap;
    }

    private String encryptedPassword() {
        try {
            return Base64.getEncoder().encodeToString(encrypt(binding.passwordEdit.getText().toString()));
        } catch (Exception e) {
            Log.e("error in encryption", e.toString());
            return null;
        }
    }

    private String convertDateToString(long selection) {
        @SuppressLint("SimpleDateFormat") SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy");
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(selection);
        return simpleDateFormat.format(calendar.getTime());
    }

    private void deleteEmployee() {
        db.collection("employees").document(employee.getId()).delete().addOnSuccessListener(unused -> {
            employeeOverviewRef.update(employee.getId(), FieldValue.delete()).addOnSuccessListener(unused1 -> {
                if (employee.getProjectID() == null) {
                    Toast.makeText(getActivity(), "Deleted", Toast.LENGTH_SHORT).show();
                    binding.deleteButton.setEnabled(true);
                    dismiss();
                    return;
                }
                EmployeeOverview tEmp = new EmployeeOverview(employee.getFirstName(), employee.getLastName(), employee.getTitle(), employee.getId());
                tEmp.setManagerID(employee.getManagerID());
                tEmp.setProjectId(employee.getProjectID());
                projectCol.document(employee.getProjectID()).update("employees", FieldValue.arrayRemove(tEmp))
                        .addOnSuccessListener(documentSnapshot -> {
                            vacationCol.whereEqualTo("employee.id", employee.getId()).whereEqualTo("vacationStatus", 0).get().addOnSuccessListener(documentQuery -> {
                                for (QueryDocumentSnapshot d : documentQuery) {
                                    vacationCol.document(d.getId()).delete();
                                }
                                Toast.makeText(getActivity(), "Deleted", Toast.LENGTH_SHORT).show();
                                binding.deleteButton.setEnabled(true);
                                dismiss();
                            });
                        });

            });

        });
    }

    private void updateEmployee() {
        db.collection("employees").whereEqualTo("email", binding.emailEdit.getText().toString().trim())
                .whereNotEqualTo("id", employee.getId())
                .get().addOnSuccessListener(documents -> {
                    if (documents.getDocuments().size() != 0) {
                        binding.emailLayout.setError("This Email already exists");
                        binding.updateButton.setEnabled(true);
                        return;
                    }
                    String id = employee.getId();
                    Map<String, Object> updatedEmpOverviewMap = new HashMap<>();
                    ArrayList<String> empInfo = new ArrayList<>();
                    empInfo.add((binding.firstNameEdit.getText()).toString());
                    empInfo.add((binding.secondNameEdit.getText()).toString());
                    empInfo.add((binding.titleEdit.getText()).toString());
                    empInfo.add((employee.getManagerID()));
                    empInfo.add((employee.getProjectID()));
                    empInfo.add((employee.getManagerID() == null) ? "0" : "1");
                    updatedEmpOverviewMap.put(id, empInfo);
                    HashMap<String, Object> updatedEmployee = fillEmployeeData();
                    db.collection("employees").document(id).update(updatedEmployee).addOnSuccessListener(unused -> {

                        if (employee.getSalary() != Double.parseDouble(binding.salaryEdit.getText().toString()) || !employee.getCurrency().equals(binding.currencyAuto.getText().toString())) {
                            ArrayList<Allowance> allTypes = new ArrayList<>();
                            db.collection("EmployeesGrossSalary").document(employee.getId()).get().addOnSuccessListener((value) -> {
                                if (!value.exists())
                                    return;
                                EmployeesGrossSalary employeesGrossSalary;
                                employeesGrossSalary = value.toObject(EmployeesGrossSalary.class);
                                allTypes.addAll(employeesGrossSalary.getAllTypes());
                                allTypes.removeIf(allowance -> allowance.getType() == NETSALARY);
                                allTypes.add(new Allowance("Net salary", Double.parseDouble(binding.salaryEdit.getText().toString()), NETSALARY, binding.currencyAuto.getText().toString()));
                                db.collection("EmployeesGrossSalary").document(employee.getId()).update("allTypes", allTypes);
                            });
                        }
                        updateEmployeeOverview(updatedEmpOverviewMap, updatedEmployee);

                    }).addOnFailureListener(e -> {
                        Toast.makeText(getActivity(), "Failed to update due to corrupted data ", Toast.LENGTH_SHORT).show();
                        dismiss();

                    });
                });
    }

    // Listeners
    private View.OnClickListener clUpdate = v -> {
        if (!validateInputs()) {
            return;
        }
        binding.updateButton.setEnabled(false);
        updateEmployee();

    };
    private View.OnClickListener clDelete = v -> {
        binding.deleteButton.setEnabled(false);
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(getActivity());
        builder.setTitle(getString(R.string.Delete))
                .setMessage(getString(R.string.AreUSure))
                .setNegativeButton(getString(R.string.cancel), (dialogInterface, i) -> {
                    binding.deleteButton.setEnabled(true);
                })
                .setPositiveButton(getString(R.string.accept), (dialogInterface, i) -> {
                    deleteEmployee();
                    dialogInterface.dismiss();
                })
                .show();

    };
    private View.OnClickListener clUnlock = v -> {
        employee.setLocked(false);
        db.collection("employees").document(employee.getId()).set(employee, SetOptions.mergeFields("locked"));
    };

    private View.OnClickListener oclHireDate = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (!vDatePicker.isVisible())
                vDatePicker.show(getFragmentManager(), "DATE_PICKER");
        }
    };
    private MaterialPickerOnPositiveButtonClickListener pclDatePicker = new MaterialPickerOnPositiveButtonClickListener() {
        @Override
        public void onPositiveButtonClick(Object selection) {
            binding.hireDateEdit.setText(convertDateToString((long) selection));
            hireDate = (long) selection;
        }
    };
    private TextWatcher twEmail = new TextWatcher() {
        private final Pattern mPattern = Pattern.compile("(?:[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*|\"(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\\[(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?|[a-z0-9-]*[a-z0-9]:(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)\\])");

        private boolean isValid(CharSequence s) {
            return s.toString().equals("") || mPattern.matcher(s).matches();
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            if (!isValid(s)) {
                binding.emailLayout.setError("Wrong E-mail form");
            } else {
                binding.emailLayout.setError(null);
            }
        }
    };
    private View.OnClickListener oclPasswordGenerate = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            binding.passwordEdit.setText("1234");
            binding.passwordEdit.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
        }
    };


}