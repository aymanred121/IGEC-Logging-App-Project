package com.example.igecuser.Fragments;

import static com.example.igecuser.cryptography.RSAUtil.encrypt;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.igecuser.R;
import com.example.igecuser.fireBase.Employee;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Base64;

public class ChangePasswordFragment extends Fragment {

    //Views
    private TextInputEditText vNewPassword, vConfirmPassword;
    private TextInputLayout vConfirmPasswordLayout;
    private MaterialButton vChangePassword;

    //Vars
    private Employee user;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public ChangePasswordFragment(Employee user) {
        this.user = user;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_change_password, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initialize(view);
        vChangePassword.setOnClickListener(oclChangePassword);
        vConfirmPassword.addTextChangedListener(twConfirmPassword);
        vNewPassword.addTextChangedListener(twNewPassword);
    }

    private void initialize(View v) {
        vNewPassword = v.findViewById(R.id.TextInput_NewPassword);
        vConfirmPassword = v.findViewById(R.id.TextInput_ConfirmPassword);
        vConfirmPasswordLayout = v.findViewById(R.id.textInputLayout_ConfirmPassword);
        vChangePassword = v.findViewById(R.id.Button_ChangePassword);
    }

    private boolean validateInput() {
        return (
                !vNewPassword.getText().toString().equals("")
                        &&
                        vNewPassword.getText().toString().equals(vConfirmPassword.getText().toString())
        );
    }

    private void clearInput() {
        vNewPassword.setText(null);
        vConfirmPassword.setText(null);
    }
    private String encryptedPassword() {
        try {
            return Base64.getEncoder().encodeToString(encrypt(vNewPassword.getText().toString()));
        } catch (Exception e) {
            Log.e("error in encryption", e.toString());
            return null;
        }
    }

    private View.OnClickListener oclChangePassword = v -> {
        if (validateInput()) {
            // update password
            user.setPassword(vNewPassword.getText().toString());
            db.collection("employees").document(user.getId()).update("password", encryptedPassword()).addOnSuccessListener(unused -> {
                clearInput();
            });

        }
    };
    private TextWatcher twConfirmPassword = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            if (!s.toString().equals(vNewPassword.getText().toString()) && s.toString().length() > 0) {

                vConfirmPasswordLayout.setError("Not matched");

            } else {
                vConfirmPasswordLayout.setError(null);
            }
        }
    };
    private TextWatcher twNewPassword = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            if (vConfirmPassword.getText().toString().length() > 0 && !vNewPassword.getText().toString().equals(vConfirmPassword.getText().toString())) {
                vConfirmPasswordLayout.setError("Not matched");
            } else {
                vConfirmPasswordLayout.setError(null);
            }
        }
    };
}