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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
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
    private MaterialCheckBox vAdmin,vTemporary;
    private MaterialButton vUpdate;
    private MaterialButton vDelete;
    private TextInputEditText vFirstName;
    private TextInputEditText vSecondName;
    private TextInputEditText vEmail;
    private TextInputLayout vEmailLayout, vPasswordLayout;
    private TextInputEditText vPassword;
    private TextInputEditText vTitle;
    private TextInputEditText vSalary;
    private TextInputEditText vSSN;
    private TextInputEditText vArea;
    private TextInputEditText vCity;
    private TextInputEditText vStreet;
    private TextInputEditText vHireDate;
    private TextInputEditText vPhone;
    private TextInputEditText vInsuranceNumber,vInsuranceAmount;
    private TextInputLayout vHireDateLayout;
    private final MaterialDatePicker.Builder<Long> vDatePickerBuilder = MaterialDatePicker.Builder.datePicker();
    private MaterialDatePicker vDatePicker;


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
            dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);
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
        vUpdate.setOnClickListener(clUpdate);
        vDelete.setOnClickListener(clDelete);
        vPasswordLayout.setEndIconOnClickListener(oclPasswordGenerate);
        return view;
    }

    // Functions
    private void initialize(View view) {
        vFirstName = view.findViewById(R.id.TextInput_FirstName);
        vSecondName = view.findViewById(R.id.TextInput_SecondName);
        vEmail = view.findViewById(R.id.TextInput_Email);
        vEmailLayout = view.findViewById(R.id.textInputLayout_Email);
        vPassword = view.findViewById(R.id.TextInput_Password);
        vPasswordLayout = view.findViewById(R.id.textInputLayout_Password);
        vTitle = view.findViewById(R.id.TextInput_Title);
        vSalary = view.findViewById(R.id.TextInput_Salary);
        vSSN = view.findViewById(R.id.TextInput_NationalID);
        vArea = view.findViewById(R.id.TextInput_Area);
        vCity = view.findViewById(R.id.TextInput_City);
        vStreet = view.findViewById(R.id.TextInput_Street);
        vHireDate = view.findViewById(R.id.TextInput_HireDate);
        vAdmin = view.findViewById(R.id.CheckBox_Admin);
        vInsuranceNumber = view.findViewById(R.id.TextInput_InsuranceNumber);
        vInsuranceAmount = view.findViewById(R.id.TextInput_InsuranceAmount);
        vTemporary = view.findViewById(R.id.CheckBox_Temporary);
        vPhone = view.findViewById(R.id.TextInput_Phone);
        vHireDateLayout = view.findViewById(R.id.textInputLayout_HireDate);
        MaterialButton vRegister = view.findViewById(R.id.button_register);
        vDelete = view.findViewById(R.id.button_delete);
        vUpdate = view.findViewById(R.id.button_update);

        vRegister.setVisibility(View.GONE);
        vDelete.setVisibility(View.VISIBLE);
        vUpdate.setVisibility(View.VISIBLE);

        vFirstName.setText(employee.getFirstName());
        vSecondName.setText(employee.getLastName());
        vTitle.setText(employee.getTitle());
        vArea.setText(employee.getArea());
        vCity.setText(employee.getCity());
        vStreet.setText(employee.getStreet());
        vEmail.setText(employee.getEmail());
        vSalary.setText(String.valueOf(employee.getSalary()));
        vSSN.setText(employee.getSSN());
        vPassword.setText(employee.getDecryptedPassword());
        vPhone.setText(employee.getPhoneNumber());
        vAdmin.setChecked(employee.getManagerID().equals("IGEC"));
        //TODO vTemporary.setChecked(employee.isTemporary());
        //TODO vInsuranceNumber.setText(employee.getInsuranceNumber());
        //TODO vInsuranceAmount.setText(String.valueOf(employee.getInsuranceAmount()));
        vHireDate.setText(convertDateToString(employee.getHireDate().getTime()));
        hireDate = employee.getHireDate().getTime();
        vDatePickerBuilder.setTitleText("Hire Date");
        vDatePicker = vDatePickerBuilder.setSelection(hireDate).build();
        vPasswordLayout.setEndIconMode(END_ICON_CUSTOM);
        vPasswordLayout.setEndIconDrawable(R.drawable.ic_baseline_autorenew_24);
        vPassword.setInputType(InputType.TYPE_CLASS_TEXT);


    }

    boolean validateInputs() {
        return
                !(vFirstName.getText().toString().isEmpty() ||
                        vSecondName.getText().toString().isEmpty() ||
                        vEmail.getText().toString().isEmpty() ||
                        vEmailLayout.getError() != null ||
                        vPassword.getText().toString().isEmpty() ||
                        vTitle.getText().toString().isEmpty() ||
                        vSalary.getText().toString().isEmpty() ||
                        vArea.getText().toString().isEmpty() ||
                        vCity.getText().toString().isEmpty() ||
                        vStreet.getText().toString().isEmpty() ||
                        vHireDate.getText().toString().isEmpty() ||
                        vSSN.getText().toString().isEmpty() ||
                        vInsuranceAmount.getText().toString().isEmpty() ||
                        vInsuranceNumber.getText().toString().isEmpty() ||
                        vSSN.getText().toString().length() != 14);
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
            Toast.makeText(getActivity(), "Updated", Toast.LENGTH_SHORT).show();
            dismiss();
        });
    }

    private void updateProjects(HashMap<String, Object> updatedEmployeeMap) {
        EmployeeOverview tempEmp = new EmployeeOverview(vFirstName.getText().toString(), vSecondName.getText().toString(), vTitle.getText().toString(), employee.getId(), employee.getProjectID());
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
        empMap.put("area", vArea.getText().toString());
        empMap.put("street", vStreet.getText().toString());
        empMap.put("city", vCity.getText().toString());
        empMap.put("email", vEmail.getText().toString());
        empMap.put("firstName", vFirstName.getText().toString());
        empMap.put("lastName", vSecondName.getText().toString());
        empMap.put("password", encryptedPassword());
        empMap.put("title", vTitle.getText().toString());
        empMap.put("salary", Double.parseDouble(vSalary.getText().toString()));
        empMap.put("SSN", vSSN.getText().toString());
        empMap.put("phoneNumber", vPhone.getText().toString());
        empMap.put("hireDate", new Date(hireDate));
        empMap.put("id", employee.getId());
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
                                dismiss();
                            });
                        });

            });

        });
    }

    private void updateEmployee() {
        String id = employee.getId();
        Map<String, Object> updatedEmpOverviewMap = new HashMap<>();
        ArrayList<String> empInfo = new ArrayList<>();
        empInfo.add((vFirstName.getText()).toString());
        empInfo.add((vSecondName.getText()).toString());
        empInfo.add((vTitle.getText()).toString());
        empInfo.add((employee.getManagerID()));
        empInfo.add((employee.getProjectID()));
        updatedEmpOverviewMap.put(id, empInfo);
        HashMap<String, Object> updatedEmployeeMap = fillEmployeeData();
        db.collection("employees").document(id).update(updatedEmployeeMap).addOnSuccessListener(unused -> {
            if (employee.getSalary() != Double.parseDouble(vSalary.getText().toString())) {
                ArrayList<Allowance> allTypes = new ArrayList<>();
                db.collection("EmployeesGrossSalary").document(employee.getId()).get().addOnSuccessListener((value) -> {
                    if (!value.exists())
                        return;
                    EmployeesGrossSalary employeesGrossSalary;
                    employeesGrossSalary = value.toObject(EmployeesGrossSalary.class);
                    allTypes.addAll(employeesGrossSalary.getAllTypes());
                    allTypes.removeIf(allowance -> allowance.getType() == NETSALARY);
                    allTypes.add(new Allowance("Net salary" ,Double.parseDouble(vSalary.getText().toString()) , NETSALARY ));
                    db.collection("EmployeesGrossSalary").document(employee.getId()).update("allTypes", allTypes);
                });
            }
            updateEmployeeOverview(updatedEmpOverviewMap, updatedEmployeeMap);

        });
    }

    // Listeners
    private View.OnClickListener clUpdate = v -> {
        if (!validateInputs()) {
            Toast.makeText(getActivity(), "please, fill the project data", Toast.LENGTH_SHORT).show();
            return;
        }
        updateEmployee();

    };
    private View.OnClickListener clDelete = v -> {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(getActivity());
        builder.setTitle(getString(R.string.Delete))
                .setMessage(getString(R.string.AreUSure))
                .setNegativeButton(getString(R.string.cancel), (dialogInterface, i) -> {
                })
                .setPositiveButton(getString(R.string.accept), (dialogInterface, i) -> {
                    deleteEmployee();
                    dialogInterface.dismiss();
                })
                .show();
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
        }
    };


}