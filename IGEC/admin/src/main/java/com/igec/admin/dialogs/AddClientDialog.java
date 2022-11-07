package com.igec.admin.dialogs;

import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import com.igec.admin.R;
import com.igec.admin.databinding.DialogAddClientBinding;
import com.igec.common.firebase.Client;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.Objects;
import java.util.regex.Pattern;

public class AddClientDialog extends DialogFragment {

    //Views
    private Client client;

    public AddClientDialog(Client client) {
        this.client = client;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {


        Dialog dialog = super.onCreateDialog(savedInstanceState);
        Window window = dialog.getWindow();

        if (window != null) {
            dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);
        }

        return dialog;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.FullscreenDialogTheme);
    }

    private DialogAddClientBinding binding;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = DialogAddClientBinding.inflate(inflater, container, false);
        return binding.getRoot();
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
        binding.doneFab.setOnClickListener(oclDone);
        binding.nameEdit.addTextChangedListener(twName);
        binding.emailEdit.addTextChangedListener(twEmail);
        binding.phoneEdit.addTextChangedListener(twPhone);
        binding.noteEdit.addTextChangedListener(twNote);
        binding.perHourEdit.addTextChangedListener(twPerHour);
        binding.overTimeEdit.addTextChangedListener(twOverTime);
        binding.perFridayEdit.addTextChangedListener(twPerFriday);
    }

    private void initialize() {

        if (client != null) {
            binding.nameEdit.setText(client.getName());
            binding.emailEdit.setText(client.getEmail());
            binding.phoneEdit.setText(client.getPhoneNumber());
            binding.noteEdit.setText(client.getNote());
            binding.perHourEdit.setText(String.valueOf(client.getDefaultPrice()));
            binding.overTimeEdit.setText(String.valueOf(client.getOverTimePrice()));
            binding.perFridayEdit.setText(String.valueOf(client.getFridaysPrice()));
        }

    }




    private View.OnClickListener oclDone = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

                binding.doneFab.setEnabled(false);
                Client client = new Client(
                        binding.nameEdit.getText().toString(),
                        binding.emailEdit.getText().toString(),
                        binding.phoneEdit.getText().toString(),
                        binding.noteEdit.getText().toString(),
                        Objects.requireNonNull(binding.perHourEdit.getText()).toString().trim().isEmpty() ? 0.0 : Double.parseDouble(binding.perHourEdit.getText().toString()),
                        Objects.requireNonNull(binding.overTimeEdit.getText()).toString().trim().isEmpty() ? 0.0 : Double.parseDouble(binding.overTimeEdit.getText().toString()),
                        Objects.requireNonNull(binding.perFridayEdit.getText()).toString().trim().isEmpty() ? 0.0 : Double.parseDouble(binding.perFridayEdit.getText().toString())
                        );
                Bundle result = new Bundle();
                result.putSerializable("client", client);
                getParentFragmentManager().setFragmentResult("client", result);
                binding.doneFab.setEnabled(true);
                dismiss();

        }
    };
    private TextWatcher twEmail = new TextWatcher() {
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
            if (!isValid(s.toString().trim())) {
                binding.emailLayout.setError("Wrong E-mail form");
            } else {
                binding.emailLayout.setError(null);
                binding.emailLayout.setErrorEnabled(false);
            }
        }
    };
    private TextWatcher twName = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            binding.nameLayout.setError(null);
            binding.nameLayout.setErrorEnabled(false);
        }
    };
    private TextWatcher twNote = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            binding.noteLayout.setError(null);
            binding.noteLayout.setErrorEnabled(false);
        }
    };
    private TextWatcher twPhone = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            binding.phoneLayout.setError(null);
            binding.phoneLayout.setErrorEnabled(false);
        }
    };
    private TextWatcher twPerHour = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            binding.perHourLayout.setError(null);
            binding.perHourLayout.setErrorEnabled(false);
        }
    };
    private TextWatcher twOverTime = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            binding.overTimeLayout.setError(null);
            binding.overTimeLayout.setErrorEnabled(false);
        }
    };
    private TextWatcher twPerFriday = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            binding.perFridayLayout.setError(null);
            binding.perFridayLayout.setErrorEnabled(false);
        }
    };
}