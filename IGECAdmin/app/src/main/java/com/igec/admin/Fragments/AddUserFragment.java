package com.igec.admin.Fragments;

import static com.igec.admin.cryptography.RSAUtil.encrypt;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.igec.admin.R;
import com.igec.admin.fireBase.Allowance;
import com.igec.admin.fireBase.Employee;
import com.igec.admin.fireBase.EmployeesGrossSalary;
import com.igec.admin.utilites.allowancesEnum;
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
    private MaterialCheckBox vAdmin, vTemporary;
    private MaterialButton vRegister;
    private AutoCompleteTextView vSalaryCurrency;
    private TextInputEditText vFirstName, vSecondName, vEmail, vPassword, vPhone, vTitle, vSalary, vNationalID, vArea, vCity, vStreet, vHireDate, vInsuranceNumber, vInsuranceAmount;
    private TextInputLayout vFirstNameLayout, vSecondNameLayout, vEmailLayout, vPasswordLayout, vPhoneLayout, vTitleLayout, vSalaryLayout, vNationalIDLayout, vAreaLayout, vCityLayout, vStreetLayout, vHireDateLayout, vInsuranceNumberLayout, vInsuranceAmountLayout, vSalaryCurrencyLayout;
    private MaterialDatePicker.Builder<Long> vDatePickerBuilder = MaterialDatePicker.Builder.datePicker();
    private MaterialDatePicker vDatePicker;
    private ArrayList<Pair<TextInputLayout, EditText>> views;

    // Vars
    long hireDate;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
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
        vHireDateLayout.setErrorIconOnClickListener(oclHireDate);
        vRegister.setOnClickListener(clRegister);
        vDatePicker.addOnPositiveButtonClickListener(pclDatePicker);
        vAdmin.setOnClickListener(oclAdmin);
        vHireDateLayout.setErrorIconDrawable(R.drawable.ic_baseline_calendar_month_24);
        for (Pair<TextInputLayout, EditText> v : views) {
            if (v.first != vEmailLayout)
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
    public void onResume() {
        super.onResume();
        ArrayList<String> currencies = new ArrayList<>();
        currencies.add("EGP");
        currencies.add("SAR");
        ArrayAdapter<String> currenciesAdapter = new ArrayAdapter<>(getActivity(), R.layout.item_dropdown, currencies);
        vSalaryCurrency.setAdapter(currenciesAdapter);
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
        vSalaryCurrency = view.findViewById(R.id.TextInput_SalaryCurrency);
        vSalaryCurrencyLayout = view.findViewById(R.id.textInputLayout_SalaryCurrency);
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
        vRegister = view.findViewById(R.id.button_register);

        views = new ArrayList<>();
        views.add(new Pair<>(vFirstNameLayout, vFirstName));
        views.add(new Pair<>(vSecondNameLayout, vSecondName));
        views.add(new Pair<>(vEmailLayout, vEmail));
        views.add(new Pair<>(vPasswordLayout, vPassword));
        views.add(new Pair<>(vPhoneLayout, vPhone));
        views.add(new Pair<>(vTitleLayout, vTitle));
        views.add(new Pair<>(vSalaryLayout, vSalary));
        views.add(new Pair<>(vSalaryCurrencyLayout, vSalaryCurrency));
        views.add(new Pair<>(vInsuranceNumberLayout, vInsuranceNumber));
        views.add(new Pair<>(vInsuranceAmountLayout, vInsuranceAmount));
        views.add(new Pair<>(vAreaLayout, vArea));
        views.add(new Pair<>(vCityLayout, vCity));
        views.add(new Pair<>(vStreetLayout, vStreet));
        views.add(new Pair<>(vHireDateLayout, vHireDate));
        views.add(new Pair<>(vNationalIDLayout, vNationalID));


        //TODO: remove fakeData() when all testing is finished
        fakeData();
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
                Toast.makeText(getActivity(), "this Email already exist", Toast.LENGTH_SHORT).show();
                vRegister.setEnabled(true);
                return;
            }

            String id = db.collection("EmployeeOverview").document().getId().substring(0, 5);
            EmployeesGrossSalary employeesGrossSalary = new EmployeesGrossSalary();
            ArrayList<Allowance> allTypes = new ArrayList<>();
            allTypes.add(new Allowance("Net salary", Double.parseDouble(vSalary.getText().toString()), allowancesEnum.NETSALARY.ordinal(), vSalaryCurrency.getText().toString()));
            employeesGrossSalary.setEmployeeId(id);
            employeesGrossSalary.setAllTypes(allTypes);
            ArrayList<String> empInfo = new ArrayList<>();
            empInfo.add((vFirstName.getText()).toString());
            empInfo.add((vSecondName.getText()).toString());
            empInfo.add((vTitle.getText()).toString());
            empInfo.add(null); // ManagerID
            empInfo.add(null); // ProjectID
            empInfo.add("0"); // isSelected
            Map<String, Object> empInfoMap = new HashMap<>();
            empInfoMap.put(id, empInfo);
            employeeOverviewRef.update(empInfoMap).addOnFailureListener(e -> employeeOverviewRef.set(empInfoMap));
            Employee newEmployee = fillEmployeeData();
            newEmployee.setId(id);
            //get year from hire date
            String year = vHireDate.getText().toString().substring(6, 10);
            String month = vHireDate.getText().toString().substring(3, 5);
            batch.set(db.collection("employees").document(id), newEmployee);
            //batch.set(db.collection("EmployeesGrossSalary").document(id).collection(year).document(month), employeesGrossSalary);
            batch.set(db.collection("EmployeesGrossSalary").document(id), employeesGrossSalary);
            batch.commit().addOnSuccessListener(unused -> {
                clearInputs();
                fakeData();
                Toast.makeText(getActivity(), "Registered", Toast.LENGTH_SHORT).show();
                batch = FirebaseFirestore.getInstance().batch();
            });
        });
    }

    private Employee fillEmployeeData() {
        double overTime = (Double.parseDouble(vSalary.getText().toString()) / 30.0 / 10.0) * 1.5;
        return new Employee(
                (vFirstName.getText()).toString(),
                (vSecondName.getText()).toString(),
                (vTitle.getText()).toString(),
                (vArea.getText()).toString(),
                (vCity.getText()).toString(),
                (vStreet.getText()).toString(),
                Double.parseDouble(vSalary.getText().toString()),
                vSalaryCurrency.getText().toString(),
                overTime,
                ((vNationalID.getText()).toString()),
                new Date(hireDate),
                vEmail.getText().toString().trim(),
                encryptedPassword(),
                vPhone.getText().toString(),
                vInsuranceNumber.getText().toString(),
                vTemporary.isChecked(),
                Double.parseDouble(vInsuranceAmount.getText().toString()),
                vAdmin.isChecked());
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
        vRegister.setEnabled(true);
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
        vInsuranceAmount.setText(null);
        vInsuranceNumber.setText(null);
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

    // Listeners
    View.OnClickListener clRegister = v -> {
        if (validateInputs()) {
            vRegister.setEnabled(false);
            if (!vAdmin.isChecked())
                addEmployee();
            else {
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