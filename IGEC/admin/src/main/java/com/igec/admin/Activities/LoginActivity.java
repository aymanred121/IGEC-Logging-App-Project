package com.igec.admin.Activities;


import static com.igec.common.cryptography.RSAUtil.decrypt;
import static com.igec.common.cryptography.RSAUtil.privateKey;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Pair;
import android.view.View;
import android.widget.Toast;

import com.igec.admin.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.igec.common.firebase.Employee;

import java.util.ArrayList;
import java.util.regex.Pattern;

public class LoginActivity extends AppCompatActivity {
    public static final String IGEC = "IGEC";
    public static final String ID = "ID";
    private TextInputEditText vEmail;
    private TextInputLayout vEmailLayout, vPasswordLayout;
    private TextInputEditText vPassword;
    private MaterialButton vSignIn;
    private ArrayList<Pair<TextInputLayout, TextInputEditText>> views;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        isNetworkAvailable();
        initialize();
        // Listeners
        vEmail.addTextChangedListener(twEmail);
        vPassword.addTextChangedListener(twPassword);
        vSignIn.setOnClickListener(clSignIn);
    }

    @Override
    protected void onResume() {
        super.onResume();
        isNetworkAvailable();
    }

    // Functions
    private void initialize() {
        vEmail = findViewById(R.id.TextInput_email);
        vPassword = findViewById(R.id.TextInput_password);
        vEmailLayout = findViewById(R.id.textInputLayout_Email);
        vPasswordLayout = findViewById(R.id.textInputLayout_Password);
        vSignIn = findViewById(R.id.Button_SignIn);
        vEmail.setText("admin@gmail.com");
        vPassword.setText("1");

        views = new ArrayList<>();
        views.add(new Pair<>(vEmailLayout, vEmail));
        views.add(new Pair<>(vPasswordLayout, vPassword));

    }

    private boolean generateError() {
        for (Pair<TextInputLayout, TextInputEditText> view : views) {
            // check if its missing error
            if (view.second.getText().toString().trim().isEmpty())
                view.first.setError("Missing");
            // check for other errors generated via text watchers
            if (view.first.getError() != null) {
                return true;
            }
        }
        return false;
    }

    private boolean validateInput() {
        return !generateError();
    }

    private void isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        boolean connected = activeNetworkInfo != null && activeNetworkInfo.isConnected();
        if (!connected) {
            Intent intent = new Intent(LoginActivity.this, SplashScreen_InternetConnection.class);
            startActivity(intent);
            finish();
        }
    }

    private boolean isPasswordRight(String password) {
        try {
            String decryptedPassword = decrypt(password, privateKey);
            if (vPassword.getText() != null && !vPassword.getText().toString().equals(decryptedPassword)) {
                vEmailLayout.setError(" ");
                vPasswordLayout.setError("Wrong E-mail or password");
                return false;
            }
        } catch (Exception e) {
            vEmailLayout.setError(" ");
            vPasswordLayout.setError("Wrong E-mail or password");
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
                vEmailLayout.setError("Wrong E-mail form");
            } else {
                vEmailLayout.setError(null);
            }
            hideError(vEmailLayout);
        }
    };
    private final TextWatcher twPassword = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void afterTextChanged(Editable editable) {
            // remove any error when changed
            vPasswordLayout.setError(null);
            vPasswordLayout.setErrorEnabled(false);
        }
    };
    private final View.OnClickListener clSignIn = new View.OnClickListener() {
        @RequiresApi(api = Build.VERSION_CODES.Q)
        @Override
        public void onClick(View v) {
            if (!validateInput()) return;
            vSignIn.setEnabled(false);
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("employees")
                    .whereEqualTo("email", (vEmail.getText() != null) ? vEmail.getText().toString() : "")
                    .limit(1)
                    .get().addOnSuccessListener(queryDocumentSnapshots -> {
                        if (queryDocumentSnapshots.size() == 0) {
                            vEmailLayout.setError(" ");
                            vPasswordLayout.setError("Wrong E-mail or password");
                            vSignIn.setEnabled(true);
                            return;
                        }
                        DocumentSnapshot d = queryDocumentSnapshots.getDocuments().get(0);
                        if (d.exists()) {
                            Employee currEmployee = d.toObject(Employee.class);
                            if (currEmployee != null && !isPasswordRight(currEmployee.getPassword())) {
                                vSignIn.setEnabled(true);
                                return;
                            }
                            Intent intent;
                            if (currEmployee != null && currEmployee.isAdmin()) {
                                intent = new Intent(LoginActivity.this, MainActivity.class);
                                SharedPreferences sharedPreferences = getSharedPreferences(IGEC, MODE_PRIVATE);
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.putString(ID, currEmployee.getId());
                                editor.apply();
                                startActivity(intent);
                                finish();
                            }
                        }
                    });
        }


    };
}