package com.example.igec_admin.Dialogs;

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
import android.widget.Toast;

import com.example.igec_admin.R;
import com.example.igec_admin.fireBase.Client;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

public class AddClientDialog extends DialogFragment {

    //Views
    private TextInputEditText vCompanyName, vCompanyEmail, vCompanyPhoneNumber, vNote,vPerHour,vOverTime,vPerFriday;
    private TextInputLayout vCompanyNameLayout, vCompanyEmailLayout, vCompanyPhoneNumberLayout, vNoteLayout,vPerHourLayout,vOverTimeLayout,vPerFridayLayout;
    private MaterialButton vDone;
    private Client client;
    private ArrayList<Pair<TextInputLayout, TextInputEditText>> views;
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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.dialog_add_client, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initialize(view);
        vDone.setOnClickListener(oclDone);
        vCompanyName.addTextChangedListener(twName);
        vCompanyEmail.addTextChangedListener(twEmail);
        vCompanyPhoneNumber.addTextChangedListener(twPhone);
        vNote.addTextChangedListener(twNote);
        vPerHour.addTextChangedListener(twPerHour);
        vOverTime.addTextChangedListener(twOverTime);
        vPerFriday.addTextChangedListener(twPerFriday);
    }

    private void initialize(View view) {
        vNote = view.findViewById(R.id.TextInput_Note);
        vCompanyName = view.findViewById(R.id.TextInput_CompanyName);
        vCompanyEmail = view.findViewById(R.id.TextInput_CompanyEmail);
        vCompanyPhoneNumber = view.findViewById(R.id.TextInput_CompanyPhoneNumber);
        vCompanyNameLayout = view.findViewById(R.id.textInputLayout_CompanyName);
        vCompanyEmailLayout = view.findViewById(R.id.textInputLayout_CompanyEmail);
        vCompanyPhoneNumberLayout = view.findViewById(R.id.textInputLayout_CompanyPhoneNumber);
        vPerHour = view.findViewById(R.id.TextInput_PerHour);
        vOverTime = view.findViewById(R.id.TextInput_OverTime);
        vPerFriday = view.findViewById(R.id.TextInput_PerFriday);
        vPerHourLayout = view.findViewById(R.id.textInputLayout_PerHour);
        vOverTimeLayout  = view.findViewById(R.id.textInputLayout_OverTime);
        vPerFridayLayout  = view.findViewById(R.id.textInputLayout_PerFriday);
        vNoteLayout = view.findViewById(R.id.textInputLayout_Note);
        vDone = view.findViewById(R.id.Button_Done);


        views = new ArrayList<>();
        views.add(new Pair<>(vCompanyNameLayout, vCompanyName));
        views.add(new Pair<>(vCompanyEmailLayout, vCompanyEmail));
        views.add(new Pair<>(vCompanyPhoneNumberLayout, vCompanyPhoneNumber));
        views.add(new Pair<>(vNoteLayout, vNote));
        views.add(new Pair<>(vPerHourLayout, vPerHour));
        views.add(new Pair<>(vOverTimeLayout, vOverTime));
        views.add(new Pair<>(vPerFridayLayout, vPerFriday));
        if (client != null) {
            vCompanyName.setText(client.getName());
            vCompanyEmail.setText(client.getEmail());
            vCompanyPhoneNumber.setText(client.getPhoneNumber());
            vNote.setText(client.getNote());
            vPerHour.setText(String.valueOf(client.getDefaultPrice()));
            vOverTime.setText(String.valueOf(client.getOverTimePrice()));
            vPerFriday.setText(String.valueOf(client.getFridaysPrice()));
        }

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

    private boolean validateInput() {
        return !generateError();
    }

    private View.OnClickListener oclDone = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (validateInput()) {
                vDone.setEnabled(false);
                Client client = new Client(
                        vCompanyName.getText().toString(),
                        vCompanyEmail.getText().toString(),
                        vCompanyPhoneNumber.getText().toString(),
                        vNote.getText().toString(),
                        Double.parseDouble(vPerHour.getText().toString()),
                        Double.parseDouble(vOverTime.getText().toString()),
                        Double.parseDouble(vPerFriday.getText().toString()));
                Bundle result = new Bundle();
                result.putSerializable("client", client);
                getParentFragmentManager().setFragmentResult("client", result);
                vDone.setEnabled(true);
                dismiss();
            }
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
                vCompanyEmailLayout.setError("Wrong E-mail form");
            } else {
                vCompanyEmailLayout.setError(null);
                vCompanyEmailLayout.setErrorEnabled(false);
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
            vCompanyNameLayout.setError(null);
            vCompanyNameLayout.setErrorEnabled(false);
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
            vNoteLayout.setError(null);
            vNoteLayout.setErrorEnabled(false);
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
            vCompanyPhoneNumberLayout.setError(null);
            vCompanyPhoneNumberLayout.setErrorEnabled(false);
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
            vPerHourLayout.setError(null);
            vPerHourLayout.setErrorEnabled(false);
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
            vOverTimeLayout.setError(null);
            vOverTimeLayout.setErrorEnabled(false);
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
            vPerFridayLayout.setError(null);
            vPerFridayLayout.setErrorEnabled(false);
        }
    };
}