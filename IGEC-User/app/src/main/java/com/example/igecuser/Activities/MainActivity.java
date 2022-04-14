package com.example.igecuser.Activities;



import static com.example.igecuser.cryptography.RSAUtil.decrypt;
import static com.example.igecuser.cryptography.RSAUtil.privateKey;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.example.igecuser.R;
import com.example.igecuser.fireBase.Allowance;
import com.example.igecuser.fireBase.Employee;
import com.example.igecuser.fireBase.EmployeeOverview;
import com.example.igecuser.fireBase.EmployeesGrossSalary;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    private final int PROJECT = 0;
    private final int NETSALARY = 1;
    private final int ALLOWANCE = 2;
    private final int BONUS = 3;
    private final int PENALTY = 4;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    // Views
    private TextInputEditText vEmail;
    private TextInputLayout vEmailLayout;
    private TextInputEditText vPassword;
    private MaterialButton vSignIn;
    // Vars


    // Overrides
    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        setContentView(R.layout.activity_main);
        initialize();
        monthStart();
        // Listeners
        vEmail.addTextChangedListener(twEmail);
        vSignIn.setOnClickListener(clSignIn);

    }

    // Functions
    private void initialize() {
        vEmail = findViewById(R.id.TextInput_email);
        vPassword = findViewById(R.id.TextInput_password);
        vEmailLayout = findViewById(R.id.textInputLayout_Email);
        vSignIn = findViewById(R.id.Button_SignIn);

        vEmail.setText("t@gmail.com");
        vPassword.setText("1");

    }
    private void monthStart()
    {
        @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
        String currentDateAndTime = sdf.format(new Date());
        String day = currentDateAndTime.substring(0,2);
        String month = currentDateAndTime.substring(3,5);
        String Year = currentDateAndTime.substring(6,10);
        db.collection("Cloud").document("ifFirstDay").get().addOnSuccessListener((value) -> {
            int op = -1;

            if(!value.exists()) op = 0;

            else if (Integer.parseInt(value.get("ifFirstDay").toString()) == 2 &&
                    (value.get("IfDoneFor "+month+'-'+Year)== null || Integer.parseInt(value.get("IfDoneFor "+month+'-'+Year).toString()) == 0)) op = 1;

            else if(Integer.parseInt(value.get("ifFirstDay").toString()) == 1) op = 2;

            switch (op)
            {
                case 0:
                    if(Integer.parseInt(day) == 1)
                    {
                        HashMap<String, Integer> temp = new HashMap<>();
                        temp.put("ifFirstDay",1);
                        temp.put("IfDoneFor "+month+'-'+Year,0);
                        db.collection("Cloud").document("ifFirstDay").set(temp);
                    }
                    // No break
                case 2:
                    // a function to be discussed or deleted duo to change of logic  // TODO remove hard coded employee delete and fill allEmp with allEmployee data to reset all the old month data and leave only NETSALARY (to be discussed)
                    ArrayList<Allowance> allTypes = new ArrayList<>();
                    ArrayList<EmployeeOverview> allEmp = new ArrayList<>();
                    db.collection("EmployeesGrossSalary").document("1Yfa6").get().addOnSuccessListener((value1) -> {
                        if (!value1.exists())
                            return;
                        EmployeesGrossSalary employeesGrossSalary;
                        employeesGrossSalary = value1.toObject(EmployeesGrossSalary.class);
                        allTypes.addAll(employeesGrossSalary.getAllTypes());
                        allTypes.removeIf(allowance -> allowance.getType() != NETSALARY);
                        db.collection("EmployeesGrossSalary").document("1Yfa6").update("allTypes", allTypes);
                    });
                    db.collection("Cloud").document("ifFirstDay").update("ifFirstDay",2 ,"IfDoneFor "+month+'-'+Year , 1 );
                    break;
                case 1:
                    if(Integer.parseInt(day) == 1)
                    {
                        db.collection("Cloud").document("ifFirstDay").update("ifFirstDay",1);
                    }
                    break;
                case -1 :
                    break;
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
//    private void askPermission() {
//        String[] PERMISSIONS = {Manifest.permission.ACCESS_BACKGROUND_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.CAMERA};
//        ActivityCompat.requestPermissions(this, PERMISSIONS, 112);
//
//        TedPermission.create()
//                .setPermissionListener(new PermissionListener() {
//                    @Override
//                    public void onPermissionGranted() {
//                        Toast.makeText(MainActivity.this, "Granted", Toast.LENGTH_SHORT).show();
//                    }
//
//                    @Override
//                    public void onPermissionDenied(List<String> deniedPermissions) {
//
//                    }
//                })
//                .setDeniedMessage("If you reject permission,you can not use this service\n\nPlease turn on permissions at [Setting] > [Permission]")
//                .setPermissions(PERMISSIONS)
//                .check();
//    }

    private boolean isPasswordRight(String password) {
        try {
            String decryptedPassword = decrypt(password, privateKey);
            if (vPassword.getText() != null && !vPassword.getText().toString().equals(decryptedPassword)) {
                Toast.makeText(MainActivity.this, "please enter a valid email or password", Toast.LENGTH_SHORT).show();
                return false;
            }
        } catch (Exception e) {
            Toast.makeText(MainActivity.this, "please enter a valid email or password", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    // Listeners
    private final TextWatcher twEmail = new TextWatcher() {
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
            if (!isValid(s)) {
                vEmailLayout.setError("Wrong E-mail form");
            } else {
                vEmailLayout.setError(null);
            }
        }
    };
    private final View.OnClickListener clSignIn = new View.OnClickListener() {
        @RequiresApi(api = Build.VERSION_CODES.Q)
        @Override
        public void onClick(View v) {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("employees")
                    .whereEqualTo("email", (vEmail.getText() != null) ? vEmail.getText().toString() : "")
                    .limit(1)
                    .get().addOnSuccessListener(queryDocumentSnapshots -> {
                if (queryDocumentSnapshots.size() == 0) {
                    Toast.makeText(MainActivity.this, "please enter a valid email or password", Toast.LENGTH_SHORT).show();
                    return;
                }
                DocumentSnapshot d = queryDocumentSnapshots.getDocuments().get(0);
                if (d.exists()) {
                    Employee currEmployee = d.toObject(Employee.class);
                    if (currEmployee != null && !isPasswordRight(currEmployee.getPassword())) {
                        return;
                    }
                    Intent intent;
                    if (currEmployee != null && currEmployee.getManagerID() == null) {
                        Toast.makeText(MainActivity.this, "you are not assigned to any project", Toast.LENGTH_SHORT).show();
                        return;
                    } else if (currEmployee != null && currEmployee.getManagerID().equals("adminID")) {
                        intent = new Intent(MainActivity.this, ManagerDashboard.class);
                    } else {
                        intent = new Intent(MainActivity.this, EmployeeDashboard.class);
                    }
                    intent.putExtra("emp", currEmployee);
                    startActivity(intent);
                }
            });
        }


    };
}