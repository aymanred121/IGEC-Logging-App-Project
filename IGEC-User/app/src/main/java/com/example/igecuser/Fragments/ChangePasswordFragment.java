package com.example.igecuser.Fragments;

import static com.example.igecuser.cryptography.RSAUtil.encrypt;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.igecuser.R;
import com.example.igecuser.fireBase.Employee;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Base64;

public class ChangePasswordFragment extends Fragment {

    //Views
    private TextInputEditText vNewPassword, vConfirmPassword;
    private TextInputLayout vConfirmPasswordLayout, vNewPasswordLayout;
    private MaterialButton vChangePassword;
    private ArrayList<Pair<TextInputLayout, TextInputEditText>> views;
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
        vChangePassword = v.findViewById(R.id.Button_ChangePassword);


        vConfirmPasswordLayout = v.findViewById(R.id.textInputLayout_ConfirmPassword);
        vNewPasswordLayout = v.findViewById(R.id.textInputLayout_NewPassword);

        views = new ArrayList<>();
        views.add(new Pair<>(vNewPasswordLayout, vNewPassword));
        views.add(new Pair<>(vConfirmPasswordLayout, vConfirmPassword));
    }

    private boolean validateInput() {

        return !generateError();
    }

    private boolean generateError() {

        for (Pair<TextInputLayout, TextInputEditText> view : views) {
            if (view.second.getText().toString().trim().isEmpty()) {
                view.first.setError("Missing");
                return true;
            }
            if (view.first.getError() != null) {
                return true;
            }
        }
        return false;
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
            MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(getActivity());
            builder.setTitle(getString(R.string.ChangePassword))
                    .setMessage(getString(R.string.AreUSure))
                    .setNegativeButton(getString(R.string.cancel), (dialogInterface, i) -> {
                    })
                    .setPositiveButton(getString(R.string.accept), (dialogInterface, i) -> {
                        user.setPassword(vNewPassword.getText().toString());
                        db.collection("employees").document(user.getId()).update("password", encryptedPassword()).addOnSuccessListener(unused -> {
                            Toast.makeText(getActivity(), "Password Changed Successfully", Toast.LENGTH_SHORT).show();
                            clearInput();
                        });
                    })
                    .show();

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
            if (!vConfirmPassword.getText().toString().trim().isEmpty()) {
                if (vConfirmPasswordLayout.getError() == "Missing") {
                    vConfirmPasswordLayout.setError(null);
                }
                boolean bothAreNotEqualWithTrim = !vNewPassword.getText().toString().trim().equals(vConfirmPassword.getText().toString().trim());
                if (bothAreNotEqualWithTrim)
                    vConfirmPasswordLayout.setError("not Matched");
                else
                    vConfirmPasswordLayout.setError(null);
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
            if (vNewPasswordLayout.getError() == "Missing" && !vNewPassword.getText().toString().trim().isEmpty()) {
                vNewPasswordLayout.setError(null);
            }

            if (!vConfirmPassword.getText().toString().trim().isEmpty() && !vNewPassword.getText().toString().trim().isEmpty()) {
                boolean bothAreNotEqualWithTrim = !vNewPassword.getText().toString().trim().equals(vConfirmPassword.getText().toString().trim());
                if (bothAreNotEqualWithTrim)
                    vConfirmPasswordLayout.setError("not Matched");
                else
                    vConfirmPasswordLayout.setError(null);
            }
        }
    };
}