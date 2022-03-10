package com.example.igecuser.Activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Toast;

import com.example.igecuser.R;
import com.example.igecuser.cryptography.RSAUtil;
import com.example.igecuser.fireBase.Employee;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.normal.TedPermission;

import java.util.List;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    // Views
    private TextInputEditText vEmail;
    private TextInputLayout vEmailLayout;
    private TextInputEditText vPassword;
    private MaterialButton vSignIn;

    // Vars
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        setContentView(R.layout.activity_main);
        askPermission();
        initialize();

        // Listeners
        vEmail.addTextChangedListener(twEmail);
        vSignIn.setOnClickListener(clSignIn);

    }

    private void askPermission() {
        String[] PERMISSIONS={Manifest.permission.ACCESS_BACKGROUND_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.CAMERA};
        ActivityCompat.requestPermissions((Activity) this, PERMISSIONS, 112);

        TedPermission.create()
                .setPermissionListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted() {
                        Toast.makeText(MainActivity.this, "Granted", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onPermissionDenied(List<String> deniedPermissions) {

                    }
                })
                .setDeniedMessage("If you reject permission,you can not use this service\n\nPlease turn on permissions at [Setting] > [Permission]")
                .setPermissions(PERMISSIONS)
                .check();
    }


    // Functions
    private void initialize() {
        vEmail = findViewById(R.id.TextInput_email);
        vPassword = findViewById(R.id.TextInput_password);
        vEmailLayout = findViewById(R.id.textInputLayout_Email);
        vSignIn = findViewById(R.id.Button_SignIn);

    }


    private boolean isPasswordRight(String password) {
        try {
            String decryptedPassword = RSAUtil.decrypt(password, RSAUtil.privateKey);
            if (!vPassword.getText().toString().equals(decryptedPassword)) {
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
    private TextWatcher twEmail = new TextWatcher() {
        private Pattern mPattern = Pattern.compile("(?:[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*|\"(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\\[(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?|[a-z0-9-]*[a-z0-9]:(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)\\])");

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
    private View.OnClickListener clSignIn = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            db.collection("employees")
                    .whereEqualTo("email", vEmail.getText().toString())
                    .limit(1)
                    .get().addOnSuccessListener(queryDocumentSnapshots -> {
                        if (queryDocumentSnapshots.size() == 0) {
                            Toast.makeText(MainActivity.this, "please enter a valid email or password", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        DocumentSnapshot d = queryDocumentSnapshots.getDocuments().get(0);
                        if (d.exists()) {
                            Employee currEmployee = d.toObject(Employee.class);
                            if( !isPasswordRight(currEmployee.getPassword())){
                                return;
                            }
                            Intent intent;
                            if(currEmployee.getManagerID()==null){
                                Toast.makeText(MainActivity.this, "you are not assigned to any project", Toast.LENGTH_SHORT).show();
                                return;
                            }
                           else if (currEmployee.getManagerID().equals("adminID")) {
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