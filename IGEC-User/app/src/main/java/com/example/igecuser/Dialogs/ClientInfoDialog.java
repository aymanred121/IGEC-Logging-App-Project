package com.example.igecuser.Dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.igecuser.R;
import com.example.igecuser.fireBase.Client;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.regex.Pattern;

public class ClientInfoDialog extends DialogFragment {


    //Views
    private TextInputEditText vCompanyName, vCompanyEmail, vCompanyPhoneNumber;
    private TextInputLayout vCompanyEmailLayout;
    private MaterialButton vDone;
    //Vars
    private float startDate;
    private String note;
    public ClientInfoDialog(String note) {
        this.note = note;
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

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_client_info, container, false);
        initialize(view);
        vDone.setOnClickListener(oclDone);
        vCompanyEmail.addTextChangedListener(twEmail);
        return view;
    }

    // Functions
    private void initialize(View view) {
        vCompanyName = view.findViewById(R.id.TextInput_CompanyName);
        vCompanyEmail = view.findViewById(R.id.TextInput_CompanyEmail);
        vCompanyEmailLayout = view.findViewById(R.id.textInputLayout_CompanyEmail);
        vCompanyPhoneNumber = view.findViewById(R.id.TextInput_CompanyPhoneNumber);
        vDone = view.findViewById(R.id.Button_Done);
    }

    private boolean validateInput() {
        return !(vCompanyName.getText().toString().isEmpty() ||
                vCompanyEmail.getText().toString().isEmpty() ||
                vCompanyPhoneNumber.getText().toString().isEmpty() ||
                vCompanyEmailLayout.getError() != null);
    }


    private final View.OnClickListener oclDone = v -> {

        if (!validateInput()) {
            Toast.makeText(getActivity(), "please, fill client info correctly", Toast.LENGTH_SHORT).show();
            return;
        }
        Bundle bundle = new Bundle();
        Client client = new Client(vCompanyName.getText().toString(), vCompanyEmail.getText().toString(), vCompanyPhoneNumber.getText().toString());
        bundle.putSerializable("client", client);
        bundle.putString("note",note);
        getParentFragmentManager().setFragmentResult("clientInfo", bundle);
        dismiss();

    };


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
                vCompanyEmailLayout.setError("Wrong E-mail form");
            } else {
                vCompanyEmailLayout.setError(null);
            }
        }
    };

}
