package com.example.igecuser.Dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.igecuser.R;
import com.example.igecuser.fireBase.VacationRequest;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Objects;

public class ClientInfoFragmentDialog extends DialogFragment {


    //Views
    private TextInputEditText vCompanyName,vCompanyEmail,vCompanyPhoneNumber,vStartDateOfUse;
    private MaterialButton vScan;
    private MaterialDatePicker.Builder<Long> vDatePickerBuilder = MaterialDatePicker.Builder.datePicker();
    private MaterialDatePicker vDatePicker;

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


        vScan.setOnClickListener(oclScan);
        return view;
    }

    // Functions
    private void initialize(View view)
    {
        vCompanyName =view.findViewById(R.id.TextInput_CompanyName);
        vCompanyEmail = view.findViewById(R.id.TextInput_CompanyEmail);
        vCompanyPhoneNumber = view.findViewById(R.id.TextInput_CompanyPhoneNumber);
        vStartDateOfUse = view.findViewById(R.id.TextInput_UseStartDate);
        vScan = view.findViewById(R.id.Button_Scan);
    }

    private View.OnClickListener oclScan = v -> {

    };

}
