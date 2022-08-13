package com.igec.user.Activities;


import static com.igec.common.CONSTANTS.ADMIN;
import static com.igec.common.CONSTANTS.CAMERA_REQUEST_CODE;
import static com.igec.common.CONSTANTS.ID;
import static com.igec.common.CONSTANTS.IGEC;
import static com.igec.common.CONSTANTS.LOCATION_REQUEST_CODE;
import static com.igec.common.CONSTANTS.LOGGED;
import static com.igec.common.cryptography.RSAUtil.decrypt;
import static com.igec.common.cryptography.RSAUtil.privateKey;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.igec.user.R;
import com.igec.common.firebase.Employee;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.igec.user.databinding.ActivityMainBinding;

import java.util.regex.Pattern;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class MainActivity extends AppCompatActivity {

    private final int PROJECT = 0;
    private final int NETSALARY = 1;
    private final int ALLOWANCE = 2;
    private final int BONUS = 3;
    private final int PENALTY = 4;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    // Overrides
    private ActivityMainBinding binding;
    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        setContentView(binding.getRoot());
        validateDate(this);
        initialize();
        // Listeners
        binding.emailEdit.addTextChangedListener(twEmail);
        binding.signInButton.setOnClickListener(clSignIn);

    }

    @Override
    protected void onResume() {
        super.onResume();
        validateDate(this);

    }

    public void validateDate(Context c) {
        if (Settings.Global.getInt(c.getContentResolver(), Settings.Global.AUTO_TIME, 0) != 1) {
            Intent intent = new Intent(MainActivity.this, SplashScreen_DateInaccurate.class);
            startActivity(intent);
            finish();
        }
    }

    // Functions
    private void initialize() {
        getLocationPermissions();
        getCameraPermission();
    }

    @AfterPermissionGranted(LOCATION_REQUEST_CODE)
    private boolean getLocationPermissions() {
        String[] perms = {Manifest.permission.ACCESS_FINE_LOCATION};

        if (EasyPermissions.hasPermissions(this, perms)) {
            return true;
        } else {
            EasyPermissions.requestPermissions(
                    this,
                    "We need location permissions in order to the app to functional correctly",
                    LOCATION_REQUEST_CODE,
                    Manifest.permission.ACCESS_FINE_LOCATION);
            return false;
        }
    }

    @AfterPermissionGranted(CAMERA_REQUEST_CODE)
    private boolean getCameraPermission() {
        String[] perms = {Manifest.permission.CAMERA};
        if (EasyPermissions.hasPermissions(this, perms)) {
            return true;
        } else {
            EasyPermissions.requestPermissions(this, "We need camera permission in order to be able to scan the qr code",
                    CAMERA_REQUEST_CODE, perms);
            return false;
        }
    }

    private boolean isPasswordRight(String password) {
        try {
            String decryptedPassword = decrypt(password, privateKey);
            if (binding.passwordEdit.getText() != null && !binding.passwordEdit.getText().toString().equals(decryptedPassword)) {
                Toast.makeText(MainActivity.this, "please enter a valid email or password", Toast.LENGTH_SHORT).show();
                return false;
            }
        } catch (Exception e) {
            Toast.makeText(MainActivity.this, "please enter a valid email or password", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void hideError(TextInputLayout textInputLayout) {
        textInputLayout.setErrorEnabled(textInputLayout.getError() != null);

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
                binding.emailLayout.setError("Wrong E-mail form");
            } else {
                binding.emailLayout.setError(null);
            }
            hideError(binding.emailLayout);
        }
    };
    private final View.OnClickListener clSignIn = new View.OnClickListener() {
        @RequiresApi(api = Build.VERSION_CODES.Q)
        @Override
        public void onClick(View v) {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("employees")
                    .whereEqualTo("email", (binding.emailEdit.getText() != null) ? binding.emailEdit.getText().toString() : "")
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
                            if (currEmployee.isLocked() == true) {
                                Toast.makeText(MainActivity.this, "This email is already in use", Toast.LENGTH_SHORT).show();
                                return;
                            }
                            currEmployee.setLocked(true);
                            db.collection("employees").document(currEmployee.getId()).set(currEmployee, SetOptions.merge()).addOnSuccessListener(unused -> {
                                Intent intent;
                                if (currEmployee != null && currEmployee.getManagerID() == null) {
                                    Toast.makeText(MainActivity.this, "you are not assigned to any project", Toast.LENGTH_SHORT).show();
                                    return;
                                } else if (currEmployee != null && currEmployee.getManagerID().equals(ADMIN)) {
                                    intent = new Intent(MainActivity.this, MDashboard.class);
                                } else {
                                    intent = new Intent(MainActivity.this, EDashboard.class);
                                }
                                intent.putExtra("emp", currEmployee);
                                SharedPreferences sharedPreferences = getSharedPreferences(IGEC, MODE_PRIVATE);
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.putBoolean(LOGGED, true);
                                editor.putString(ID, currEmployee.getId());
                                editor.apply();
                                startActivity(intent);
                                finish();
                            });

                        }
                    });
        }


    };
}