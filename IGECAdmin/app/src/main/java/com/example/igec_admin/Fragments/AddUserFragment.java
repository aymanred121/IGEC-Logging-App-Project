package com.example.igec_admin.Fragments;

import static com.example.igec_admin.cryptography.RSAUtil.encrypt;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.igec_admin.R;
import com.example.igec_admin.fireBase.Employee;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class AddUserFragment extends Fragment {


    // Views
    MaterialButton vRegister;
    TextInputEditText vFirstName;
    TextInputEditText vSecondName;
    TextInputEditText vEmail;
    TextInputLayout vEmailLayout;
    TextInputEditText vPassword;
    TextInputEditText vTitle;
    TextInputEditText vSalary;
    TextInputEditText vSSN;
    TextInputEditText vArea;
    TextInputEditText vCity;
    TextInputEditText vStreet;
    TextInputEditText vHireDate;
    TextInputLayout vHireDateLayout;
    MaterialDatePicker.Builder<Long> vDatePickerBuilder = MaterialDatePicker.Builder.datePicker();
    MaterialDatePicker vDatePicker;

    // Vars
    long hireDate;
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_user, container, false);

        initialize(view);
        // Listeners
        vEmail.addTextChangedListener(twEmail);
        vDatePicker.addOnPositiveButtonClickListener(pclDatePicker);
        vHireDateLayout.setEndIconOnClickListener(oclHireDate);
        vRegister.setOnClickListener(clRegister);

        return view;
    }


    // Functions
    private void initialize(View view) {
        vFirstName = view.findViewById(R.id.TextInput_FirstName);
        vSecondName = view.findViewById(R.id.TextInput_SecondName);
        vEmail = view.findViewById(R.id.TextInput_Email);
        vEmailLayout = view.findViewById(R.id.textInputLayout_Email);
        vPassword = view.findViewById(R.id.TextInput_Password);
        vTitle = view.findViewById(R.id.TextInput_Title);
        vSalary = view.findViewById(R.id.TextInput_Salary);
        vSSN = view.findViewById(R.id.TextInput_SNN);
        vArea = view.findViewById(R.id.TextInput_Area);
        vCity = view.findViewById(R.id.TextInput_City);
        vStreet = view.findViewById(R.id.TextInput_Street);
        vHireDate = view.findViewById(R.id.TextInput_HireDate);
        vHireDateLayout = view.findViewById(R.id.textInputLayout_HireDate);
        vDatePickerBuilder.setTitleText("Hire Date");
        vDatePicker = vDatePickerBuilder.build();
        vRegister = view.findViewById(R.id.button_register);


    }

    String convertDateToString(long selection) {
        @SuppressLint("SimpleDateFormat") SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy");
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(selection);
        return simpleDateFormat.format(calendar.getTime());
    }

    // Listeners
    View.OnClickListener clRegister = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (validateInputs()) {
                addEmployee();
            } else {
                Toast.makeText(getActivity(), "please, fill the user data", Toast.LENGTH_SHORT).show();
            }
        }
    };
    View.OnClickListener oclHireDate = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (!vDatePicker.isVisible())
                vDatePicker.show(getFragmentManager(), "DATE_PICKER");
        }
    };
    MaterialPickerOnPositiveButtonClickListener pclDatePicker = new MaterialPickerOnPositiveButtonClickListener() {
        @Override
        public void onPositiveButtonClick(Object selection) {
            vHireDate.setText(convertDateToString((long) selection));
            hireDate = (long) selection;
        }
    };
    TextWatcher twEmail = new TextWatcher() {
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


    void addEmployee() {
        DocumentReference employeeOverviewRef = db.collection("EmployeeOverview").document("emp");
        String id = db.collection("EmployeeOverview").document().getId().substring(0, 5);
        ArrayList<String> empInfo = new ArrayList<>();
        empInfo.add((vFirstName.getText()).toString());
        empInfo.add((vSecondName.getText()).toString());
        empInfo.add((vTitle.getText()).toString());
        empInfo.add(null);
        empInfo.add(null);
        Map<String, Object> empInfoMap = new HashMap<>();
        empInfoMap.put(id, empInfo);
        employeeOverviewRef.update(empInfoMap).addOnFailureListener(e -> employeeOverviewRef.set(empInfoMap));
        Employee newEmployee = fillEmployeeData();
        newEmployee.setId(id);
        db.collection("employees").document(id).set(newEmployee).addOnSuccessListener(unused -> {
            clearInputs();
            Toast.makeText(getActivity(), "Registered", Toast.LENGTH_SHORT).show();
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
                ((vSSN.getText()).toString()),
                new Date(hireDate),
                vEmail.getText().toString(),
                encryptedPassword());
    }

    private String encryptedPassword() {
        try {
            return Base64.getEncoder().encodeToString(encrypt(vPassword.getText().toString()));
        } catch (Exception e) {
            Log.e("error in encryption", e.toString());
            return null;
        }
    }

    void deleteRecord(String collection, String ID) {
        db.collection(collection).whereEqualTo("id", ID).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                for (DocumentSnapshot ds : queryDocumentSnapshots) {
                    db.collection("employees").document(ds.getId()).delete();

                }
            }
        });

    }

    void updateRecord(String Collection, String id, String field, Object value) {
        Task updateDB = db.collection(Collection).document(id).update(field, value).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getActivity(), "Error while updating record", Toast.LENGTH_SHORT).show();
            }
        });
    }

    void clearInputs() {
        vFirstName.setText(null);
        vSecondName.setText(null);
        vEmail.setText(null);
        vPassword.setText(null);
        vTitle.setText(null);
        vSalary.setText(null);
        vArea.setText(null);
        vCity.setText(null);
        vStreet.setText(null);
        vHireDate.setText(null);
        vSSN.setText(null);
    }

    boolean validateInputs() {
        return
                !(vFirstName.getText().toString().isEmpty() ||
                        vSecondName.getText().toString().isEmpty() ||
                        vEmail.getText().toString().isEmpty() ||
                        vPassword.getText().toString().isEmpty() ||
                        vTitle.getText().toString().isEmpty() ||
                        vSalary.getText().toString().isEmpty() ||
                        vArea.getText().toString().isEmpty() ||
                        vCity.getText().toString().isEmpty() ||
                        vStreet.getText().toString().isEmpty() ||
                        vHireDate.getText().toString().isEmpty() ||
                        vSSN.getText().toString().isEmpty() ||
                        vSSN.getText().toString().length() != 14);
    }


}