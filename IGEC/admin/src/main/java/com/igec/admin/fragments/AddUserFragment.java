package com.igec.admin.fragments;

import static com.igec.common.CONSTANTS.EMPLOYEE_COL;
import static com.igec.common.CONSTANTS.EMPLOYEE_GROSS_SALARY_COL;
import static com.igec.common.CONSTANTS.EMPLOYEE_OVERVIEW_REF;
import static com.igec.common.cryptography.RSAUtil.encrypt;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.SetOptions;
import com.igec.admin.R;
import com.igec.admin.databinding.FragmentAddUserBinding;
import com.igec.common.firebase.Allowance;
import com.igec.common.firebase.Employee;
import com.igec.common.firebase.EmployeesGrossSalary;
import com.igec.common.utilities.AllowancesEnum;
import com.github.javafaker.Faker;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputLayout;
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
    private MaterialDatePicker.Builder<Long> vDatePickerBuilder = MaterialDatePicker.Builder.datePicker();
    private MaterialDatePicker vDatePicker;
    private ArrayList<Pair<TextInputLayout, EditText>> views;

    // Vars
    long hireDate;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    private WriteBatch batch = FirebaseFirestore.getInstance().batch();

    private FragmentAddUserBinding binding;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentAddUserBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        binding = null;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initialize();
        // Listeners
        binding.emailEdit.addTextChangedListener(twEmail);
        binding.hireDateLayout.setEndIconOnClickListener(oclHireDate);
        binding.hireDateLayout.setErrorIconOnClickListener(oclHireDate);
        binding.registerButton.setOnClickListener(clRegister);
        vDatePicker.addOnPositiveButtonClickListener(pclDatePicker);
        binding.adminCheckbox.setOnClickListener(oclAdmin);
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


        //TODO: remove fakeData() when all testing is finished
//        fakeData();
    }

    private String convertDateToString(long selection) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(selection);
        return simpleDateFormat.format(calendar.getTime());
    }

    void addEmployee() {
        EMPLOYEE_COL.whereEqualTo("email", binding.emailEdit.getText().toString().trim()).get().addOnSuccessListener(documents -> {
            if (documents.getDocuments().size() != 0) {
                Snackbar.make(binding.getRoot(), "This Email already exist", Snackbar.LENGTH_SHORT).show();
                binding.registerButton.setEnabled(true);
                return;
            }

            // employeeOverview
            String id = db.collection("EmployeeOverview").document().getId().substring(0, 5);
            ArrayList<Object> empInfo = new ArrayList<>();
            empInfo.add((binding.firstNameEdit.getText()).toString());
            empInfo.add((binding.secondNameEdit.getText()).toString());
            empInfo.add((binding.titleEdit.getText()).toString());
            empInfo.add(null); // ManagerID
            empInfo.add(new HashMap<String,Object>(){{
                put("pids",new ArrayList<String>());
            }}); // ProjectID
            empInfo.add(false); // isSelected
            empInfo.add(binding.managerCheckbox.isChecked()); // isManager
            Map<String, Object> empInfoMap = new HashMap<>();
            empInfoMap.put(id, empInfo);
            batch.set(EMPLOYEE_OVERVIEW_REF, empInfoMap, SetOptions.merge());

            // employees
            Employee newEmployee = fillEmployeeData();
            newEmployee.setId(id);
            batch.set(EMPLOYEE_COL.document(id), newEmployee);


            // grossSalary
            EmployeesGrossSalary employeesGrossSalary = new EmployeesGrossSalary();
            ArrayList<Allowance> allTypes = new ArrayList<>();
            allTypes.add(new Allowance("Net salary", Double.parseDouble(binding.salaryEdit.getText().toString()), AllowancesEnum.NETSALARY.ordinal(), binding.currencyAuto.getText().toString()));
            employeesGrossSalary.setEmployeeId(id);
            employeesGrossSalary.setAllTypes(allTypes);
            batch.set(EMPLOYEE_GROSS_SALARY_COL.document(id), employeesGrossSalary);


            batch.commit().addOnSuccessListener(unused -> {
                clearInputs();
//                fakeData();
                Snackbar.make(binding.getRoot(), "Registered", Snackbar.LENGTH_SHORT).show();
                batch = FirebaseFirestore.getInstance().batch();
            });
        });
    }

    private Employee fillEmployeeData() {
        double overTime = (Double.parseDouble(binding.salaryEdit.getText().toString()) / 30.0 / 10.0) * 1.5;
        return new Employee(
                (binding.firstNameEdit.getText()).toString(),
                (binding.secondNameEdit.getText()).toString(),
                (binding.titleEdit.getText()).toString(),
                (binding.areaEdit.getText()).toString(),
                (binding.cityEdit.getText()).toString(),
                (binding.streetEdit.getText()).toString(),
                Double.parseDouble(binding.salaryEdit.getText().toString()),
                binding.currencyAuto.getText().toString(),
                overTime,
                ((binding.nationalIdEdit.getText()).toString()),
                new Date(hireDate),
                binding.emailEdit.getText().toString().trim(),
                encryptedPassword(),
                binding.phoneEdit.getText().toString(),
                binding.insuranceNumberEdit.getText().toString(),
                binding.temporaryCheckbox.isChecked(),
                Double.parseDouble(binding.insuranceAmountEdit.getText().toString()),
                binding.adminCheckbox.isChecked(),
                binding.managerCheckbox.isChecked());
    }

    private String encryptedPassword() {
        try {
            return Base64.getEncoder().encodeToString(encrypt(binding.passwordEdit.getText().toString()));
        } catch (Exception e) {
            Log.e("error in encryption", e.toString());
            return null;
        }
    }

    void clearInputs() {
        binding.registerButton.setEnabled(true);
        binding.firstNameEdit.setText(null);
        binding.secondNameEdit.setText(null);
        binding.emailEdit.setText(null);
        binding.passwordEdit.setText(null);
        binding.phoneEdit.setText(null);
        binding.titleEdit.setText(null);
        binding.salaryEdit.setText(null);
        binding.areaEdit.setText(null);
        binding.cityEdit.setText(null);
        binding.streetEdit.setText(null);
        binding.hireDateEdit.setText(null);
        binding.insuranceAmountEdit.setText(null);
        binding.insuranceNumberEdit.setText(null);
        binding.nationalIdEdit.setText(null);
        vDatePickerBuilder = MaterialDatePicker.Builder.datePicker();
        vDatePicker = vDatePickerBuilder.build();
        vDatePicker.addOnPositiveButtonClickListener(pclDatePicker);
    }

    void fakeData() {
        Faker faker = new Faker();
        binding.firstNameEdit.setText(faker.name().firstName());
        binding.secondNameEdit.setText(faker.name().lastName());
        binding.emailEdit.setText(faker.bothify("????##@gmail.com"));
        binding.passwordEdit.setText("1");
        binding.phoneEdit.setText(faker.phoneNumber().phoneNumber());
        binding.titleEdit.setText("eng");
        binding.adminCheckbox.setChecked(faker.bool().bool());
        binding.salaryEdit.setText(faker.numerify("####"));
        binding.currencyAuto.setText("EGP");
        binding.insuranceNumberEdit.setText(faker.numerify("#####"));
        binding.insuranceAmountEdit.setText(faker.numerify("#####"));
        binding.temporaryCheckbox.setChecked(faker.bool().bool());
        binding.areaEdit.setText(faker.address().cityName());
        binding.cityEdit.setText(faker.address().cityName());
        binding.streetEdit.setText(faker.address().streetName());
        hireDate = faker.date().birthday().getTime();
        binding.hireDateEdit.setText(convertDateToString(hireDate));
        binding.nationalIdEdit.setText(faker.numerify("##############"));
        vDatePickerBuilder = MaterialDatePicker.Builder.datePicker();
        vDatePicker = vDatePickerBuilder.build();
        vDatePicker.addOnPositiveButtonClickListener(pclDatePicker);

    }

    private boolean generateError() {
        for (Pair<TextInputLayout, EditText> view : views) {
            // if field is empty
            if (view.second.getText().toString().trim().isEmpty()) {
                view.first.setError("Missing");
                return true;
            }
            // if field has invalid data
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

    // Listeners
    View.OnClickListener clRegister = v -> {
        if (validateInputs()) {
            binding.registerButton.setEnabled(false);
            if (!binding.adminCheckbox.isChecked())
                addEmployee();
            else {
                MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(getActivity());
                builder.setTitle(getString(R.string.admin_register_title))
                        .setMessage(getString(R.string.AreUSure))
                        .setNegativeButton(getString(R.string.no), (dialogInterface, i) -> {
                            binding.registerButton.setEnabled(true);
                        })
                        .setPositiveButton(getString(R.string.yes), (dialogInterface, i) -> {
                            addEmployee();
                        })
                        .show();
            }
        }
    };
    MaterialPickerOnPositiveButtonClickListener pclDatePicker = selection -> {
        binding.hireDateEdit.setText(convertDateToString((long) selection));
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
                binding.emailLayout.setError("Wrong E-mail form");
            } else {
                binding.emailLayout.setError(null);
            }
        }
    };
    View.OnClickListener oclAdmin = v -> {
        binding.temporaryCheckbox.setEnabled(!binding.adminCheckbox.isChecked());
        binding.temporaryCheckbox.setChecked(!binding.adminCheckbox.isChecked() && binding.temporaryCheckbox.isChecked());
    };


}