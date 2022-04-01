package com.example.igec_admin.Dialogs;

import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.TextWatcher;
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

import java.util.regex.Pattern;

public class AddClientDialog extends DialogFragment {

    //Views
    private TextInputEditText vCompanyName, vCompanyEmail, vCompanyPhoneNumber, vNote;
    private MaterialButton vDone;
    private TextInputLayout vEmailLayout;
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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_add_client_dialog, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initialize(view);
        vDone.setOnClickListener(oclDone);
        vCompanyEmail.addTextChangedListener(twEmail);
    }

    private void initialize(View view) {
        vCompanyName = view.findViewById(R.id.TextInput_CompanyName);
        vCompanyEmail = view.findViewById(R.id.TextInput_CompanyEmail);
        vEmailLayout = view.findViewById(R.id.textInputLayout_CompanyEmail);
        vCompanyPhoneNumber = view.findViewById(R.id.TextInput_CompanyPhoneNumber);
        vNote = view.findViewById(R.id.TextInput_Note);
        vDone = view.findViewById(R.id.Button_Done);

        if (client != null) {
            vCompanyName.setText(client.getName());
            vCompanyEmail.setText(client.getEmail());
            vCompanyPhoneNumber.setText(client.getPhoneNumber());
            vNote.setText(client.getNote());
        }

    }

    private boolean validateInput() {
        return
                !(vCompanyName.getText().toString().isEmpty() ||
                        vCompanyEmail.getText().toString().isEmpty() ||
                        vEmailLayout.getError() != null ||
                        vCompanyPhoneNumber.getText().toString().isEmpty() ||
                        vNote.getText().toString().isEmpty());

    }

    private View.OnClickListener oclDone = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(validateInput()) {
                Client client = new Client();
                client.setName(vCompanyName.getText().toString());
                client.setEmail(vCompanyEmail.getText().toString());
                client.setPhoneNumber(vCompanyPhoneNumber.getText().toString());
                client.setNote(vNote.getText().toString());
                Bundle result = new Bundle();
                result.putSerializable("client", client);
                getParentFragmentManager().setFragmentResult("client", result);
                dismiss();
            }
            else
            {
                Toast.makeText(getActivity(), "please, fill the client data", Toast.LENGTH_SHORT).show();
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
                vEmailLayout.setError("Wrong E-mail form");
            } else {
                vEmailLayout.setError(null);
            }
        }
    };
}