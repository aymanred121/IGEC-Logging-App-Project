package com.igec.user.fragments;

import static com.igec.common.CONSTANTS.EMPLOYEE_COL;
import static com.igec.common.cryptography.RSAUtil.encrypt;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.snackbar.Snackbar;
import com.igec.user.R;
import com.igec.common.firebase.Employee;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.FirebaseFirestore;
import com.igec.user.activities.DateInaccurate;
import com.igec.user.databinding.FragmentChangePasswordBinding;

import java.util.ArrayList;
import java.util.Base64;

public class ChangePasswordFragment extends Fragment {

    //Views
    private ArrayList<Pair<TextInputLayout, TextInputEditText>> views;
    //Vars
    private Employee user;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public static ChangePasswordFragment newInstance(Employee user) {
        Bundle args = new Bundle();
        args.putSerializable("user",user);
        ChangePasswordFragment fragment = new ChangePasswordFragment();
        fragment.setArguments(args);
        return fragment;
    }
    private FragmentChangePasswordBinding binding;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentChangePasswordBinding.inflate(inflater,container,false);
        return binding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        validateDate(getActivity());
    }
    private void validateDate(Context c) {
        if (Settings.Global.getInt(c.getContentResolver(), Settings.Global.AUTO_TIME, 0) != 1) {
            Intent intent = new Intent(getActivity(), DateInaccurate.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            getActivity().finish();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        binding = null;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initialize();
        binding.changePasswordButton.setOnClickListener(oclChangePassword);
        binding.confirmPasswordEdit.addTextChangedListener(twConfirmPassword);
        binding.newPasswordEdit.addTextChangedListener(twNewPassword);
    }

    private void initialize() {
        user = (Employee) getArguments().getSerializable("user");
        views = new ArrayList<>();
        views.add(new Pair<>(binding.newPasswordLayout, binding.newPasswordEdit));
        views.add(new Pair<>(binding.confirmPasswordLayout, binding.confirmPasswordEdit));
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

    private void hideError(TextInputLayout textInputLayout) {
        textInputLayout.setErrorEnabled(textInputLayout.getError() != null);

    }
    private void clearInput() {
        binding.newPasswordEdit.setText(null);
        binding.confirmPasswordEdit.setText(null);
    }

    private String encryptedPassword() {
        try {
            return Base64.getEncoder().encodeToString(encrypt(binding.newPasswordEdit.getText().toString()));
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
                    .setPositiveButton(getString(R.string.Yes), (dialogInterface, i) -> {
                        user.setPassword(binding.newPasswordEdit.getText().toString());
                        EMPLOYEE_COL.document(user.getId()).update("password", encryptedPassword()).addOnSuccessListener(unused -> {
                            Snackbar.make(binding.getRoot(), "Password Changed Successfully", Snackbar.LENGTH_SHORT).show();
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
            if (!binding.confirmPasswordEdit.getText().toString().trim().isEmpty()) {
                if (binding.confirmPasswordLayout.getError() == "Missing") {
                    binding.confirmPasswordLayout.setError(null);
                }
                boolean bothAreNotEqualWithTrim = !binding.newPasswordEdit.getText().toString().trim().equals(binding.confirmPasswordEdit.getText().toString().trim());
                if (bothAreNotEqualWithTrim)
                    binding.confirmPasswordLayout.setError("not Matched");
                else
                    binding.confirmPasswordLayout.setError(null);
            } else {
                binding.confirmPasswordLayout.setError(null);
            }

            hideError(binding.confirmPasswordLayout);
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
            if (binding.newPasswordLayout.getError() == "Missing" && !binding.newPasswordEdit.getText().toString().trim().isEmpty()) {
                binding.newPasswordLayout.setError(null);
            }

            if (!binding.confirmPasswordEdit.getText().toString().trim().isEmpty() && !binding.newPasswordEdit.getText().toString().trim().isEmpty()) {
                boolean bothAreNotEqualWithTrim = !binding.newPasswordEdit.getText().toString().trim().equals(binding.confirmPasswordEdit.getText().toString().trim());
                if (bothAreNotEqualWithTrim)
                    binding.confirmPasswordLayout.setError("not Matched");
                else
                    binding.confirmPasswordLayout.setError(null);
            }
            hideError(binding.newPasswordLayout);
        }
    };
}