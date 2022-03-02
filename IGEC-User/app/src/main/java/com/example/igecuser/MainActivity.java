package com.example.igecuser;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.igecuser.cryptography.RSAUtil;
import com.example.igecuser.fireBase.Employee;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.Serializable;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.regex.Pattern;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public class MainActivity extends AppCompatActivity {

    // Views
    TextInputEditText vEmail;
    TextInputLayout vEmailLayout;
    TextInputEditText vPassword;
    MaterialButton vSignIn;

    // Vars
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Initialize();

        // Listeners
        vEmail.addTextChangedListener(twEmail);
        vSignIn.setOnClickListener(clSignIn);
    }

    // Functions
    private void Initialize() {
        vEmail = findViewById(R.id.TextInput_email);
        vPassword = findViewById(R.id.TextInput_password);
        vEmailLayout = findViewById(R.id.textInputLayout_Email);
        vSignIn = findViewById(R.id.Button_SignIn);
    }

    // Listeners
    TextWatcher twEmail = new TextWatcher() {
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
    View.OnClickListener clSignIn = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            // TODO: Add authentication
            // if employee
            db.collection("employees")
                    .whereEqualTo("email", vEmail.getText().toString())
                    .limit(1)
                    .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                @Override
                public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                    if (queryDocumentSnapshots.size() == 0) {
                        Toast.makeText(MainActivity.this, "please enter a valid email or password", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    DocumentSnapshot d = queryDocumentSnapshots.getDocuments().get(0);
                    if (d.exists()) {
                        Employee emp = d.toObject(Employee.class);
                        try {
                            String decryptedPassword = RSAUtil.decrypt(emp.getPassword(), RSAUtil.privateKey);
                            if (!vPassword.getText().toString().equals(decryptedPassword)) {
                                Toast.makeText(MainActivity.this, "please enter a valid email or password", Toast.LENGTH_SHORT).show();
                                return;
                            }
                        } catch (IllegalBlockSizeException | InvalidKeyException | BadPaddingException | NoSuchAlgorithmException | NoSuchPaddingException e) {
                            e.printStackTrace();
                        }
                        Intent intent;
                        if (emp.getManagerID() == null) {
                            intent = new Intent(MainActivity.this, ManagerDashboard.class);
                        } else {
                            intent = new Intent(MainActivity.this, EmployeeDashboard.class);
                        }
                        intent.putExtra("emp", emp);
                        startActivity(intent);
                    }
                }
            });

            // if Manager
            {

            }
            // else
            {
                // notify user that either e-mail or password are wrong
            }
        }
    };
}