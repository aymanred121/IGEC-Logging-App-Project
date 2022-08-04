package com.example.igec_admin.Dialogs;

import static com.example.igec_admin.cryptography.RSAUtil.encrypt;
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
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.igec_admin.R;
import com.example.igec_admin.fireBase.Allowance;
import com.example.igec_admin.fireBase.Employee;
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
    private MaterialCheckBox vAdmin, vTemporary;
    private MaterialButton vUpdate;
    private MaterialButton vDelete;
    private MaterialButton vUnlock;
    private TextInputEditText vFirstName, vSecondName, vEmail, vPassword, vPhone, vTitle, vSalary, vNationalID, vArea, vCity, vStreet, vHireDate, vInsuranceNumber, vInsuranceAmount;
    private TextInputLayout vFirstNameLayout, vSecondNameLayout, vEmailLayout, vPasswordLayout, vPhoneLayout, vTitleLayout, vSalaryLayout, vNationalIDLayout, vAreaLayout, vCityLayout, vStreetLayout, vHireDateLayout, vInsuranceNumberLayout, vInsuranceAmountLayout;
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

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_user, container, false);
        initialize(view);
        // Listeners
        vEmail.addTextChangedListener(twEmail);
        vDatePicker.addOnPositiveButtonClickListener(pclDatePicker);
        vHireDateLayout.setEndIconOnClickListener(oclHireDate);
        vHireDateLayout.setErrorIconOnClickListener(oclHireDate);
        vUpdate.setOnClickListener(clUpdate);
        vDelete.setOnClickListener(clDelete);
        vUnlock.setOnClickListener(clUnlock);
        vPasswordLayout.setEndIconOnClickListener(oclPasswordGenerate);
        for (Pair<TextInputLayout, EditText> v : views) {
            if(v.first != vEmailLayout)
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

        return view;
    }

    // Functions
    private void initialize(View view) {
        vFirstName = view.findViewById(R.id.TextInput_FirstName);
        vFirstNameLayout = view.findViewById(R.id.textInputLayout_FirstName);
        vSecondName = view.findViewById(R.id.TextInput_SecondName);
        vSecondNameLayout = view.findViewById(R.id.textInputLayout_SecondName);
        vEmail = view.findViewById(R.id.TextInput_Email);
        vEmailLayout = view.findViewById(R.id.textInputLayout_Email);
        vPassword = view.findViewById(R.id.TextInput_Password);
        vPasswordLayout = view.findViewById(R.id.textInputLayout_Password);
        vPhone = view.findViewById(R.id.TextInput_Phone);
        vPhoneLayout = view.findViewById(R.id.textInputLayout_Phone);
        vTitle = view.findViewById(R.id.TextInput_Title);
        vTitleLayout = view.findViewById(R.id.textInputLayout_Title);
        vAdmin = view.findViewById(R.id.CheckBox_Admin);
        vTemporary = view.findViewById(R.id.CheckBox_Temporary);
        vSalary = view.findViewById(R.id.TextInput_Salary);
        vSalaryLayout = view.findViewById(R.id.textInputLayout_Salary);
        vInsuranceNumber = view.findViewById(R.id.TextInput_InsuranceNumber);
        vInsuranceNumberLayout = view.findViewById(R.id.textInputLayout_InsuranceNumber);
        vInsuranceAmount = view.findViewById(R.id.TextInput_InsuranceAmount);
        vInsuranceAmountLayout = view.findViewById(R.id.textInputLayout_InsuranceAmount);
        vNationalID = view.findViewById(R.id.TextInput_NationalID);
        vNationalIDLayout = view.findViewById(R.id.textInputLayout_NationalID);
        vArea = view.findViewById(R.id.TextInput_Area);
        vAreaLayout = view.findViewById(R.id.textInputLayout_Area);
        vCity = view.findViewById(R.id.TextInput_City);
        vCityLayout = view.findViewById(R.id.textInputLayout_City);
        vStreet = view.findViewById(R.id.TextInput_Street);
        vStreetLayout = view.findViewById(R.id.textInputLayout_Street);
        vHireDate = view.findViewById(R.id.TextInput_HireDate);
        vHireDateLayout = view.findViewById(R.id.textInputLayout_HireDate);
        vDatePickerBuilder.setTitleText("Hire Date");
        vDatePicker = vDatePickerBuilder.build();
//        vRegister = view.findViewById(R.id.button_register);
        MaterialButton vRegister = view.findViewById(R.id.button_register);
        vDelete = view.findViewById(R.id.button_delete);
        vUpdate = view.findViewById(R.id.button_update);
        vUnlock = view.findViewById(R.id.button_unlock);
        vRegister.setVisibility(View.GONE);
        vDelete.setVisibility(View.VISIBLE);
        vUpdate.setVisibility(View.VISIBLE);
        vUnlock.setVisibility(View.VISIBLE);


        views = new ArrayList<>();
        views.add(new Pair<>(vFirstNameLayout, vFirstName));
        views.add(new Pair<>(vSecondNameLayout, vSecondName));
        views.add(new Pair<>(vEmailLayout, vEmail));
        views.add(new Pair<>(vPasswordLayout, vPassword));
        views.add(new Pair<>(vPhoneLayout, vPhone));
        views.add(new Pair<>(vTitleLayout, vTitle));
        views.add(new Pair<>(vSalaryLayout, vSalary));
        views.add(new Pair<>(vInsuranceNumberLayout, vInsuranceNumber));
        views.add(new Pair<>(vInsuranceAmountLayout, vInsuranceAmount));
        views.add(new Pair<>(vAreaLayout, vArea));
        views.add(new Pair<>(vCityLayout, vCity));
        views.add(new Pair<>(vStreetLayout, vStreet));
        views.add(new Pair<>(vHireDateLayout, vHireDate));
        views.add(new Pair<>(vNationalIDLayout, vNationalID));


        vFirstName.setText(employee.getFirstName());
        vSecondName.setText(employee.getLastName());
        vTitle.setText(employee.getTitle());
        vArea.setText(employee.getArea());
        vCity.setText(employee.getCity());
        vStreet.setText(employee.getStreet());
        vEmail.setText(employee.getEmail());
        vSalary.setText(String.valueOf(employee.getSalary()));
        vNationalID.setText(employee.getSSN());
        vPassword.setText(employee.getDecryptedPassword());
        vPhone.setText(employee.getPhoneNumber());
        vAdmin.setChecked(employee.isAdmin());
        vTemporary.setChecked(employee.isTemporary());
        vInsuranceNumber.setText(employee.getInsuranceNumber());
        vInsuranceAmount.setText(String.valueOf(employee.getInsuranceAmount()));
        vHireDate.setText(convertDateToString(employee.getHireDate().getTime()));
        hireDate = employee.getHireDate().getTime();
        vDatePickerBuilder.setTitleText("Hire Date");
        vDatePicker = vDatePickerBuilder.setSelection(hireDate).build();
        vPasswordLayout.setEndIconMode(END_ICON_CUSTOM);
        vPasswordLayout.setEndIconDrawable(R.drawable.ic_baseline_autorenew_24);
        vPassword.setEnabled(false);
        vDelete.setEnabled(employee.getManagerID() == null || !employee.getManagerID().equals("adminID"));
        db.collection("Machine_Employee").whereEqualTo("employee.id", employee.getId()).addSnapshotListener((docs, e) -> {
            // no machines found = enabled X
            // a machine without a check-out = disabled
            // all machines have been checked-out = enabled
            for (QueryDocumentSnapshot doc : docs) {
                if (doc.get("checkOut") == null || ((HashMap) doc.get("checkOut")).size() == 0) {
                    vDelete.setEnabled(false);
                    return;
                }
                vDelete.setEnabled(true);
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
        boolean isNationalIdValid = vNationalID.getText().toString().length() == 14;
        if (!isNationalIdValid) {
            vNationalIDLayout.setError("Must be 14 digits");
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
            vUpdate.setEnabled(true);
            Toast.makeText(getActivity(), "Updated", Toast.LENGTH_SHORT).show();
            dismiss();
        });
    }

    private void updateProjects(HashMap<String, Object> updatedEmployeeMap) {
        EmployeeOverview tempEmp = new EmployeeOverview(vFirstName.getText().toString(), vSecondName.getText().toString(), vTitle.getText().toString(), employee.getId(), employee.getProjectID(), employee.getProjectID() != null);
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
        empMap.put("firstName", vFirstName.getText().toString());
        empMap.put("lastName", vSecondName.getText().toString());
        empMap.put("title", vTitle.getText().toString());
        empMap.put("email", vEmail.getText().toString());
        empMap.put("password", encryptedPassword());
        empMap.put("salary", Double.parseDouble(vSalary.getText().toString()));
        empMap.put("ssn", vNationalID.getText().toString());
        empMap.put("hireDate", new Date(hireDate));
        empMap.put("phoneNumber", vPhone.getText().toString());
        empMap.put("area", vArea.getText().toString());
        empMap.put("street", vStreet.getText().toString());
        empMap.put("city", vCity.getText().toString());
        empMap.put("overTime", (Double.parseDouble(vSalary.getText().toString()) / 30.0 / 10.0) * 1.5);
        empMap.put("admin",vAdmin.isChecked());
//        if(vAdmin.isChecked()){
//            empMap.put("managerID", null);
//            empMap.put("projectID", null);
//        }
        return empMap;
    }

    private String encryptedPassword() {
        try {
            return Base64.getEncoder().encodeToString(encrypt(vPassword.getText().toString()));
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
                    vDelete.setEnabled(true);
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
                                vDelete.setEnabled(true);
                                dismiss();
                            });
                        });

            });

        });
    }

    private void updateEmployee() {
        db.collection("employees").whereEqualTo("email", vEmail.getText().toString().trim())
                .whereNotEqualTo("id", employee.getId())
                .get().addOnSuccessListener(documents -> {
                    if (documents.getDocuments().size() != 0) {
                        vEmailLayout.setError("This Email already exists");
                        vUpdate.setEnabled(true);
                        return;
                    }
                    String id = employee.getId();
                    Map<String, Object> updatedEmpOverviewMap = new HashMap<>();
                    ArrayList<String> empInfo = new ArrayList<>();
                    empInfo.add((vFirstName.getText()).toString());
                    empInfo.add((vSecondName.getText()).toString());
                    empInfo.add((vTitle.getText()).toString());
                    empInfo.add((employee.getManagerID()));
                    empInfo.add((employee.getProjectID()));
                    empInfo.add((employee.getManagerID() == null) ? "0" : "1");
                    updatedEmpOverviewMap.put(id, empInfo);
                    HashMap<String, Object> updatedEmployee = fillEmployeeData();
                    db.collection("employees").document(id).update(updatedEmployee).addOnSuccessListener(unused -> {

                        if (employee.getSalary() != Double.parseDouble(vSalary.getText().toString())) {
                            ArrayList<Allowance> allTypes = new ArrayList<>();
                            db.collection("EmployeesGrossSalary").document(employee.getId()).get().addOnSuccessListener((value) -> {
                                if (!value.exists())
                                    return;
                                EmployeesGrossSalary employeesGrossSalary;
                                employeesGrossSalary = value.toObject(EmployeesGrossSalary.class);
                                allTypes.addAll(employeesGrossSalary.getAllTypes());
                                allTypes.removeIf(allowance -> allowance.getType() == NETSALARY);
                                allTypes.add(new Allowance("Net salary", Double.parseDouble(vSalary.getText().toString()), NETSALARY));
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
        vUpdate.setEnabled(false);
        updateEmployee();

    };
    private View.OnClickListener clDelete = v -> {
        vDelete.setEnabled(false);
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(getActivity());
        builder.setTitle(getString(R.string.Delete))
                .setMessage(getString(R.string.AreUSure))
                .setNegativeButton(getString(R.string.cancel), (dialogInterface, i) -> {
                    vDelete.setEnabled(true);
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
            vHireDate.setText(convertDateToString((long) selection));
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
                vEmailLayout.setError("Wrong E-mail form");
            } else {
                vEmailLayout.setError(null);
            }
        }
    };
    private View.OnClickListener oclPasswordGenerate = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            vPassword.setText("1234");
            vPassword.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
        }
    };


}