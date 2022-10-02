package com.igec.admin.dialogs;

import static com.igec.common.CONSTANTS.ADMIN;
import static com.igec.common.CONSTANTS.EMPLOYEE_COL;
import static com.igec.common.CONSTANTS.EMPLOYEE_GROSS_SALARY_COL;
import static com.igec.common.CONSTANTS.EMPLOYEE_OVERVIEW_REF;
import static com.igec.common.CONSTANTS.MACHINE_EMPLOYEE_COL;
import static com.igec.common.CONSTANTS.PROJECT_COL;
import static com.igec.common.CONSTANTS.VACATION_COL;
import static com.igec.common.cryptography.RSAUtil.encrypt;
import static com.google.android.material.textfield.TextInputLayout.END_ICON_CUSTOM;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.graphics.drawable.Drawable;
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
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.WriteBatch;
import com.igec.admin.fragments.UsersFragment;
import com.igec.admin.R;
import com.igec.admin.databinding.FragmentAddUserBinding;
import com.igec.common.firebase.Allowance;
import com.igec.common.firebase.Employee;
import com.igec.common.firebase.EmployeeOverview;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.SetOptions;
import com.igec.common.utilities.AllowancesEnum;

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

    // Views
    private final MaterialDatePicker.Builder<Long> vDatePickerBuilder = MaterialDatePicker.Builder.datePicker();
    private MaterialDatePicker vDatePicker;
    private ArrayList<Pair<TextInputLayout, EditText>> views;


    //Var
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private Employee employee;
    private EmployeeOverview oldEmployeeOverviewData;
    private Allowance oldNetSalary = new Allowance();
    private long hireDate;
    private String year, month, day;
    private WriteBatch batch = db.batch();

    public static UserFragmentDialog newInstance(Employee employee) {
        Bundle args = new Bundle();
        args.putSerializable("employee", employee);
        UserFragmentDialog fragment = new UserFragmentDialog();
        fragment.setArguments(args);
        return fragment;
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
        binding.adminCheckbox.setOnCheckedChangeListener((compoundButton, b) -> binding.deleteButton.setEnabled(!b));
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
        int parent =getParentFragmentManager().getFragments().size()-1;
        ((UsersFragment)getParentFragmentManager().getFragments().get(parent)).setOpened(false);
        binding = null;
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
        employee = (Employee) getArguments().getSerializable("employee");
        vDatePickerBuilder.setTitleText("Hire Date");
        vDatePicker = vDatePickerBuilder.build();
        binding.registerButton.setVisibility(View.GONE);
        binding.deleteButton.setVisibility(View.VISIBLE);
        binding.updateButton.setVisibility(View.VISIBLE);
        binding.unlockButton.setVisibility(View.VISIBLE);
        Drawable state = employee.isLocked() ? getActivity().getDrawable(R.drawable.ic_outline_lock_24) : getActivity().getDrawable(R.drawable.ic_round_lock_open_24);
        binding.unlockButton.setIcon(state);
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
        //TODO
        // binding.managerCheckbox.setChecked(employee.isManager());
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
        binding.deleteButton.setEnabled((employee.getManagerID() == null || !employee.getManagerID().equals(ADMIN)) && !employee.isAdmin());
        // can't remove employee without having checkout all his machines
        MACHINE_EMPLOYEE_COL.whereEqualTo("employee.id", employee.getId()).addSnapshotListener((docs, e) -> {
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

    // update data
    private void updateEmployee() {
        updateDate();
        // check if the new e-mail is taken
        EMPLOYEE_COL.whereEqualTo("email", binding.emailEdit.getText().toString().trim())
                .whereNotEqualTo("id", employee.getId())
                .get().addOnSuccessListener(documents -> {

                    if (documents.getDocuments().size() != 0) {
                        binding.emailLayout.setError("This Email already exists");
                        binding.updateButton.setEnabled(true);
                        return;
                    }
                    // employeeOverview
                    updateEmployeeOverview();
                    // employees
                    updateEmployeeData();
                    // grossSalary

                    if (oldNetSalary.getAmount() != Double.parseDouble(binding.salaryEdit.getText().toString())
                            || oldNetSalary.getCurrency() == null //for current data as some employees don't have currency
                            || !oldNetSalary.getCurrency().equals(binding.currencyAuto.getText().toString())) {
                        // remove old grossSalary
                        EMPLOYEE_GROSS_SALARY_COL.document(employee.getId()).collection(year).document(month).get().addOnSuccessListener(un -> {
                            if (!un.exists()) {
                                batch.update(EMPLOYEE_GROSS_SALARY_COL.document(employee.getId()), "allTypes", FieldValue.arrayRemove(oldNetSalary));
                                // update netSalary
                                oldNetSalary.setCurrency(binding.currencyAuto.getText().toString());
                                oldNetSalary.setAmount(Double.parseDouble(binding.salaryEdit.getText().toString()));
                                // update grossSalary
                                batch.update(EMPLOYEE_GROSS_SALARY_COL.document(employee.getId()), "allTypes", FieldValue.arrayUnion(oldNetSalary));
                            } else {
                                batch.update(EMPLOYEE_GROSS_SALARY_COL.document(employee.getId()), "allTypes", FieldValue.arrayRemove(oldNetSalary));
                                batch.update(EMPLOYEE_GROSS_SALARY_COL.document(employee.getId()).collection(year).document(month), "allTypes", FieldValue.arrayRemove(oldNetSalary));

                                // update netSalary
                                oldNetSalary.setCurrency(binding.currencyAuto.getText().toString());
                                oldNetSalary.setAmount(Double.parseDouble(binding.salaryEdit.getText().toString()));

                                batch.update(EMPLOYEE_GROSS_SALARY_COL.document(employee.getId()), "allTypes", FieldValue.arrayUnion(oldNetSalary));
                                batch.update(EMPLOYEE_GROSS_SALARY_COL.document(employee.getId()).collection(year).document(month), "allTypes", FieldValue.arrayUnion(oldNetSalary));
                            }
                            // machine employee
                            updateMachineEmployee();

                        });
                    } else
                        updateMachineEmployee();
                });
    }

    private void updateDate() {
        Calendar calendar = Calendar.getInstance();
        year = String.valueOf(calendar.get(Calendar.YEAR));
        month = String.format("%02d", calendar.get(Calendar.MONTH) + 1);
        day = String.format("%02d", calendar.get(Calendar.DAY_OF_MONTH));
        if (Integer.parseInt(day) > 25) {
            if (Integer.parseInt(month) + 1 == 13) {
                month = "01";
                year = String.format("%d", Integer.parseInt(year) + 1);
            } else {
                month = String.format("%02d", Integer.parseInt(month) + 1);
            }
        }
    }

    private void updateEmployeeOverview() {
        Map<String, Object> updatedEmpOverviewMap = new HashMap<>();
        ArrayList<String> empInfo = new ArrayList<>();
        empInfo.add((binding.firstNameEdit.getText()).toString());
        empInfo.add((binding.secondNameEdit.getText()).toString());
        empInfo.add((binding.titleEdit.getText()).toString());
        empInfo.add((employee.getManagerID()));
        empInfo.add((employee.getProjectID()));
        empInfo.add((employee.getManagerID() == null) ? "0" : "1");
        updatedEmpOverviewMap.put(employee.getId(), empInfo);
        batch.update(EMPLOYEE_OVERVIEW_REF, updatedEmpOverviewMap);
    }

    private void updateMachineEmployee() {
        MACHINE_EMPLOYEE_COL.whereEqualTo("employee.id", employee.getId()).get().addOnSuccessListener(queryDocumentSnapshots -> {
            for (QueryDocumentSnapshot d : queryDocumentSnapshots) {
                batch.update(MACHINE_EMPLOYEE_COL.document(d.getId()), "employee", employee);
            }
            updateProjects();
        });
    }

    private void updateVacation() {
        VACATION_COL.whereEqualTo("employee.id", employee.getId()).get().addOnSuccessListener(queryDocumentSnapshots -> {
            for (QueryDocumentSnapshot d : queryDocumentSnapshots) {
                batch.update(VACATION_COL.document(d.getId()), "employee", employee);
            }
            VACATION_COL.whereEqualTo("manager.id", employee.getId()).get().addOnSuccessListener(queryDocumentSnapshot -> {
                for (QueryDocumentSnapshot d : queryDocumentSnapshot) {
                    batch.update(VACATION_COL.document(d.getId()), "manager", employee);
                }
                batch.commit().addOnSuccessListener(unused1 -> {
                    binding.updateButton.setEnabled(true);
                    Snackbar snackbar = Snackbar.make(binding.getRoot(), "Updated", Snackbar.LENGTH_SHORT);

                    snackbar.show();
                    dismiss();
                }).addOnFailureListener(e -> {
                    Toast.makeText(getActivity(), e.toString(), Toast.LENGTH_SHORT).show();
                }).addOnCompleteListener(un -> batch = db.batch());
            });
        });

    }

    private void updateProjects() {
        EmployeeOverview tempEmp = new EmployeeOverview(binding.firstNameEdit.getText().toString(), binding.secondNameEdit.getText().toString(), binding.titleEdit.getText().toString(), employee.getId(), employee.getProjectID(), employee.getProjectID() != null);
        tempEmp.setManagerID(employee.getManagerID());
        if (employee.getProjectID() == null) {
            updateVacation();
            return;
        }
        // update employee data in project
        batch.update(PROJECT_COL.document(employee.getProjectID()), "employees", FieldValue.arrayRemove(oldEmployeeOverviewData));
        if (tempEmp.getManagerID().equals(ADMIN)) {
            batch.update(PROJECT_COL.document(employee.getProjectID()), "managerName", tempEmp.getFirstName() + " " + tempEmp.getLastName(), "employees", FieldValue.arrayUnion(tempEmp));
        } else {
            batch.update(PROJECT_COL.document(employee.getProjectID()), "employees", FieldValue.arrayUnion(tempEmp));
        }
        updateVacation();

    }

    private void updateEmployeeData() {
        oldNetSalary.setAmount(employee.getSalary());
        oldNetSalary.setType(AllowancesEnum.NETSALARY.ordinal());
        oldNetSalary.setCurrency(employee.getCurrency());
        oldNetSalary.setName("Net salary");
        oldEmployeeOverviewData = new EmployeeOverview(employee.getFirstName(), employee.getLastName(), employee.getTitle(), employee.getId(), employee.getProjectID(), employee.getProjectID() != null);
        oldEmployeeOverviewData.setManagerID(employee.getManagerID());
        employee.setAdmin(binding.adminCheckbox.isChecked());
        employee.setArea(binding.areaEdit.getText().toString());
        employee.setCity(binding.cityEdit.getText().toString());
        employee.setCurrency(binding.currencyAuto.getText().toString());
        employee.setEmail(binding.emailEdit.getText().toString());
        employee.setFirstName(binding.firstNameEdit.getText().toString());
        employee.setHireDate(new Date(hireDate));
        employee.setInsuranceAmount(Double.parseDouble(binding.insuranceAmountEdit.getText().toString()));
        employee.setInsuranceNumber(binding.insuranceNumberEdit.getText().toString());
        employee.setLastName(binding.secondNameEdit.getText().toString());
        employee.setOverTime(Double.parseDouble(binding.salaryEdit.getText().toString()) / 30.0 / 10.0 * 1.5);
        employee.setPassword(encryptedPassword());
        employee.setPhoneNumber(binding.phoneEdit.getText().toString());
        employee.setSalary(Double.parseDouble(binding.salaryEdit.getText().toString()));
        employee.setSSN(binding.nationalIdEdit.getText().toString());
        employee.setStreet(binding.streetEdit.getText().toString());
        employee.setTemporary(binding.temporaryCheckbox.isChecked());
        employee.setTitle(binding.titleEdit.getText().toString());
        batch.set(EMPLOYEE_COL.document(employee.getId()), employee, SetOptions.merge());
    }

    // delete
    private void deleteEmployee() {
        batch = db.batch();
        // employee
        batch.delete(EMPLOYEE_COL.document(employee.getId()));

        // employeeOverview
        batch.update(EMPLOYEE_OVERVIEW_REF, employee.getId(), FieldValue.delete());

        if (employee.getProjectID() != null)
            batch.update(PROJECT_COL.document(employee.getProjectID()), "employees", FieldValue.arrayRemove(oldEmployeeOverviewData));

        VACATION_COL.whereEqualTo("employee.id", employee.getId()).whereEqualTo("vacationStatus", 0).get().addOnSuccessListener(documentQuery -> {
            for (QueryDocumentSnapshot d : documentQuery) {
                batch.delete(VACATION_COL.document(d.getId()));
            }
            batch.commit().addOnSuccessListener(unused -> {
                Snackbar.make(binding.getRoot(), "Deleted", Snackbar.LENGTH_SHORT).show();
                binding.deleteButton.setEnabled(true);
                dismiss();
            }).addOnFailureListener(e -> {
                Toast.makeText(getActivity(), e.toString(), Toast.LENGTH_SHORT).show();
            });
        });


    }


    // helper functions
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

    // Listeners
    private View.OnClickListener clUpdate = v -> {
        if (!validateInputs()) return;
        binding.updateButton.setEnabled(false);
        updateEmployee();

    };
    private View.OnClickListener clDelete = v -> {
        binding.deleteButton.setEnabled(false);
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(getActivity());
        builder.setTitle(getString(R.string.Delete))
                .setMessage(getString(R.string.AreUSure))
                .setNegativeButton(getString(R.string.no), (dialogInterface, i) -> {
                    binding.deleteButton.setEnabled(true);
                })
                .setPositiveButton(getString(R.string.yes), (dialogInterface, i) -> {
                    deleteEmployee();
                    dialogInterface.dismiss();
                })
                .show();

    };
    private View.OnClickListener clUnlock = v -> {
        if (!employee.isLocked()) {
            Snackbar.make(binding.getRoot(), "E-mail is already unlocked", Snackbar.LENGTH_SHORT).show();
        } else {
            employee.setLocked(false);
            binding.unlockButton.setIcon(getActivity().getDrawable(R.drawable.ic_round_lock_open_24));
            EMPLOYEE_COL.document(employee.getId()).set(employee, SetOptions.mergeFields("locked")).addOnSuccessListener(unused -> {
                Snackbar.make(binding.getRoot(), "E-mail unlocked", Snackbar.LENGTH_SHORT).show();
            });
        }
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