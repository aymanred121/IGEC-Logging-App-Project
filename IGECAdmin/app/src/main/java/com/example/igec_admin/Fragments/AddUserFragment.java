package com.example.igec_admin.Fragments;

import static com.example.igec_admin.cryptography.RSAUtil.encrypt;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.igec_admin.R;
import com.example.igec_admin.fireBase.Allowance;
import com.example.igec_admin.fireBase.Employee;
import com.example.igec_admin.fireBase.EmployeesGrossSalary;
import com.github.javafaker.Faker;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

public class AddUserFragment extends Fragment {


    // Views
    private MaterialCheckBox vAdmin,vTemporary;
    private MaterialButton vRegister;
    private TextInputEditText vFirstName, vSecondName, vEmail, vPassword, vPhone, vTitle, vSalary, vNationalID, vArea, vCity, vStreet, vHireDate,vInsuranceNumber,vInsuranceAmount;
    private TextInputLayout vHireDateLayout, vEmailLayout;
    private MaterialDatePicker.Builder<Long> vDatePickerBuilder = MaterialDatePicker.Builder.datePicker();
    private MaterialDatePicker vDatePicker;

    // Vars
    long hireDate;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final int PROJECT = 0;
    private final int NETSALARY = 1;
    private final int ALLOWANCE = 2;
    private final int BONUS = 3;
    private final int PENALTY = 4;
    private final DocumentReference employeeOverviewRef = db.collection("EmployeeOverview").document("emp");
    private WriteBatch batch = FirebaseFirestore.getInstance().batch();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_add_user, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initialize(view);
        // Listeners
        vEmail.addTextChangedListener(twEmail);
        vHireDateLayout.setEndIconOnClickListener(oclHireDate);
        vRegister.setOnClickListener(clRegister);
        vDatePicker.addOnPositiveButtonClickListener(pclDatePicker);
        vAdmin.setOnClickListener(oclAdmin);
    }

    // Functions
    private void initialize(View view) {
        vFirstName = view.findViewById(R.id.TextInput_FirstName);
        vSecondName = view.findViewById(R.id.TextInput_SecondName);
        vEmail = view.findViewById(R.id.TextInput_Email);
        vEmailLayout = view.findViewById(R.id.textInputLayout_Email);
        vPassword = view.findViewById(R.id.TextInput_Password);
        vPhone = view.findViewById(R.id.TextInput_Phone);
        vTitle = view.findViewById(R.id.TextInput_Title);
        vAdmin = view.findViewById(R.id.CheckBox_Admin);
        vTemporary = view.findViewById(R.id.CheckBox_Temporary);
        vSalary = view.findViewById(R.id.TextInput_Salary);
        vInsuranceNumber = view.findViewById(R.id.TextInput_InsuranceNumber);
        vInsuranceAmount = view.findViewById(R.id.TextInput_InsuranceAmount);
        vNationalID = view.findViewById(R.id.TextInput_NationalID);
        vArea = view.findViewById(R.id.TextInput_Area);
        vCity = view.findViewById(R.id.TextInput_City);
        vStreet = view.findViewById(R.id.TextInput_Street);
        vHireDate = view.findViewById(R.id.TextInput_HireDate);
        vHireDateLayout = view.findViewById(R.id.textInputLayout_HireDate);
        vDatePickerBuilder.setTitleText("Hire Date");
        vDatePicker = vDatePickerBuilder.build();
        vRegister = view.findViewById(R.id.button_register);
        //TODO: remove fakeData() when all testing is finished
        fakeData();

//        {
//            vFirstName.setText("test");
//            vSecondName.setText("1");
//            vEmail.setText("t@gmail.com");
//            vPassword.setText("1");
//            vTitle.setText("a");
//            vSalary.setText("1");
//            vNationalID.setText("11111111111111");
//            vPhone.setText("11111111111");
//            vArea.setText("a");
//            vCity.setText("a");
//            vStreet.setText("a");
//            vHireDate.setText(convertDateToString(1000000000));
//        }
//

    }

    private String convertDateToString(long selection) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(selection);
        return simpleDateFormat.format(calendar.getTime());
    }

    void addEmployee() {
        db.collection("employees").whereEqualTo("email", vEmail.getText().toString().trim()).get().addOnSuccessListener(documents -> {
            if (documents.getDocuments().size() != 0) {
                Toast.makeText(getActivity(), "this Email is already exist", Toast.LENGTH_SHORT).show();
                return;
            }

            String id = db.collection("EmployeeOverview").document().getId().substring(0, 5);
            EmployeesGrossSalary employeesGrossSalary = new EmployeesGrossSalary();
            employeesGrossSalary.setEmployeeId(id);
            ArrayList<Allowance> allTypes = new ArrayList<>();
            allTypes.add(new Allowance("Net salary", Double.parseDouble(vSalary.getText().toString()), NETSALARY));
            employeesGrossSalary.setAllTypes(allTypes);
            ArrayList<String> empInfo = new ArrayList<>();
            empInfo.add((vFirstName.getText()).toString());
            empInfo.add((vSecondName.getText()).toString());
            empInfo.add((vTitle.getText()).toString());
            if(vAdmin.isChecked())
                empInfo.add("IGEC"); // ManagerID
            else
            empInfo.add(null); // ManagerID
            empInfo.add(null); // ProjectID
            Map<String, Object> empInfoMap = new HashMap<>();
            empInfoMap.put(id, empInfo);
            employeeOverviewRef.update(empInfoMap).addOnFailureListener(e -> employeeOverviewRef.set(empInfoMap));
            Employee newEmployee = fillEmployeeData();
            newEmployee.setId(id);
            batch.set(db.collection("employees").document(id), newEmployee);
            batch.set(db.collection("EmployeesGrossSalary").document(id), employeesGrossSalary);
            batch.commit().addOnSuccessListener(unused -> {
                clearInputs();
                fakeData();
                Toast.makeText(getActivity(), "Registered", Toast.LENGTH_SHORT).show();
                batch = FirebaseFirestore.getInstance().batch();
            });
//            db.collection("employees").document(id).set(newEmployee).addOnSuccessListener(unused -> {
//                db.collection("EmployeesGrossSalary").document(id).set(employeesGrossSalary).addOnSuccessListener(unused1 -> {
//                    clearInputs();
//                    fakeData();
//                    Toast.makeText(getActivity(), "Registered", Toast.LENGTH_SHORT).show();
//
//                });
//            });
        });
    }

    private Employee fillEmployeeData() {
        return new Employee(
                (vFirstName.getText()).toString(),
                (vSecondName.getText()).toString(),
                (vTitle.getText()).toString(),
                (vArea.getText()).toString(),
                (vCity.getText()).toString(),
                (vStreet.getText()).toString(),
                Double.parseDouble(vSalary.getText().toString()),
                ((vNationalID.getText()).toString()),
                new Date(hireDate),
                vEmail.getText().toString().trim(),
                encryptedPassword(),
                vPhone.getText().toString()).setManagerID(vAdmin.isChecked() ? "IGEC" : null);
    }

    private String encryptedPassword() {
        try {
            return Base64.getEncoder().encodeToString(encrypt(vPassword.getText().toString()));
        } catch (Exception e) {
            Log.e("error in encryption", e.toString());
            return null;
        }
    }

    void clearInputs() {
        vFirstName.setText(null);
        vSecondName.setText(null);
        vEmail.setText(null);
        vPassword.setText(null);
        vPhone.setText(null);
        vTitle.setText(null);
        vSalary.setText(null);
        vArea.setText(null);
        vCity.setText(null);
        vStreet.setText(null);
        vHireDate.setText(null);
        vNationalID.setText(null);
        vDatePickerBuilder = MaterialDatePicker.Builder.datePicker();
        vDatePicker = vDatePickerBuilder.build();
        vDatePicker.addOnPositiveButtonClickListener(pclDatePicker);
    }

    void fakeData() {
        Faker faker = new Faker();
        vFirstName.setText(faker.name().firstName());
        vSecondName.setText(faker.name().lastName());
        vEmail.setText(faker.bothify("????##@gmail.com"));
        vPassword.setText("1");
        vPhone.setText(faker.phoneNumber().phoneNumber());
        vTitle.setText("eng");
        vSalary.setText(faker.numerify("#####"));
        vInsuranceNumber.setText(faker.numerify("#####"));
        vInsuranceAmount.setText(faker.numerify("#####"));
        vTemporary.setChecked(faker.bool().bool());
        vArea.setText(faker.address().cityName());
        vCity.setText(faker.address().cityName());
        vStreet.setText(faker.address().streetName());
        hireDate = faker.date().birthday().getTime();
        vHireDate.setText(convertDateToString(hireDate));
        vNationalID.setText(faker.numerify("##############"));
        vDatePickerBuilder = MaterialDatePicker.Builder.datePicker();
        vDatePicker = vDatePickerBuilder.build();
        vDatePicker.addOnPositiveButtonClickListener(pclDatePicker);

    }

    boolean validateInputs() {
        return
                !(vFirstName.getText().toString().isEmpty() ||
                        vSecondName.getText().toString().isEmpty() ||
                        vEmail.getText().toString().isEmpty() ||
                        vEmailLayout.getError() != null ||
                        vPassword.getText().toString().isEmpty() ||
                        vPhone.getText().toString().isEmpty() ||
                        vTitle.getText().toString().isEmpty() ||
                        vSalary.getText().toString().isEmpty() ||
                        vArea.getText().toString().isEmpty() ||
                        vCity.getText().toString().isEmpty() ||
                        vStreet.getText().toString().isEmpty() ||
                        vHireDate.getText().toString().isEmpty() ||
                        vNationalID.getText().toString().isEmpty() ||
                        vNationalID.getText().toString().length() != 14);
    }

    // Listeners
    View.OnClickListener clRegister = v -> {
        if (validateInputs()) {
            if(!vAdmin.isChecked())
            addEmployee();
            else
            {
                MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(getActivity());
                builder.setTitle(getString(R.string.admin_register_title))
                        .setMessage(getString(R.string.AreUSure))
                        .setNegativeButton(getString(R.string.cancel), (dialogInterface, i) -> {
                        })
                        .setPositiveButton(getString(R.string.accept), (dialogInterface, i) -> {
                            addEmployee();
                        })
                        .show();
            }
        } else {
            Toast.makeText(getActivity(), "please, fill the user data", Toast.LENGTH_SHORT).show();
        }
    };
    MaterialPickerOnPositiveButtonClickListener pclDatePicker = selection -> {
        vHireDate.setText(convertDateToString((long) selection));
        hireDate = (long) selection;
    };
    View.OnClickListener oclHireDate = v -> {

        vDatePicker.show(getFragmentManager(), "DATE_PICKER");
    };
    TextWatcher twEmail = new TextWatcher() {
        private final Pattern mPattern = Pattern.compile("(?:[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*|\"(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\\[(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?|[a-z0-9-]*[a-z0-9]:(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)])");

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
            if (!isValid(s.toString().trim())) {
                vEmailLayout.setError("Wrong E-mail form");
            } else {
                vEmailLayout.setError(null);
            }
        }
    };
    View.OnClickListener oclAdmin = v -> {
        vTemporary.setEnabled(!vAdmin.isChecked());
        vTemporary.setChecked(!vAdmin.isChecked() && vTemporary.isChecked());
    };


}