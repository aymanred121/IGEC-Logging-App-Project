package com.example.igec_admin.Activities;

import static com.example.igec_admin.cryptography.RSAUtil.decrypt;
import static com.example.igec_admin.cryptography.RSAUtil.privateKey;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Toast;

import com.example.igec_admin.R;
import com.example.igec_admin.fireBase.Employee;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.regex.Pattern;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText vEmail;
    private TextInputLayout vEmailLayout;
    private TextInputEditText vPassword;
    private MaterialButton vSignIn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        initialize();
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
        vEmail.setText("admin@gmail.com");
        vPassword.setText("1");

    }
    private boolean isPasswordRight(String password) {
        try {
            String decryptedPassword = decrypt(password, privateKey);
            if (vPassword.getText() != null && !vPassword.getText().toString().equals(decryptedPassword)) {
                Toast.makeText(LoginActivity.this, "please enter a valid email or password", Toast.LENGTH_SHORT).show();
                return false;
            }
        } catch (Exception e) {
            Toast.makeText(LoginActivity.this, "please enter a valid email or password", Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(LoginActivity.this, "please enter a valid email or password", Toast.LENGTH_SHORT).show();
                    return;
                }
                DocumentSnapshot d = queryDocumentSnapshots.getDocuments().get(0);
                if (d.exists()) {
                    Employee currEmployee = d.toObject(Employee.class);
                    if (currEmployee != null && !isPasswordRight(currEmployee.getPassword())) {
                        return;
                    }
                    Intent intent;
                    if (currEmployee != null && currEmployee.isAdmin()) {
                        intent = new Intent(LoginActivity.this, MainActivity.class);
                        startActivity(intent);
                    }
                }
            });
        }


    };
}