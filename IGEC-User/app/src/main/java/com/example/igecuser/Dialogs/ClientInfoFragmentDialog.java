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
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Objects;

public class ClientInfoFragmentDialog extends DialogFragment {


    //Views
    private TextInputEditText vCompanyName,vCompanyEmail,vCompanyPhoneNumber,vStartDateOfUse;
    private TextInputLayout vStartDateOfUseLayout;
    private MaterialButton vScan;
    private MaterialDatePicker.Builder<Long> vDatePickerBuilder = MaterialDatePicker.Builder.datePicker();
    private MaterialDatePicker vDatePicker;
    private SupplementsFragmentDialog supplementsFragmentDialog;
    //Vars
    private float startDate;

    public ClientInfoFragmentDialog(SupplementsFragmentDialog supplementsFragmentDialog) {
        this.supplementsFragmentDialog = supplementsFragmentDialog;
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
        supplementsFragmentDialog.dismiss();
        initialize(view);
        vScan.setOnClickListener(oclScan);
        vStartDateOfUseLayout.setEndIconOnClickListener(oclStartDateOfUse);
        vDatePicker.addOnPositiveButtonClickListener(pclDatePicker);
        return view;
    }

    // Functions
    private void initialize(View view)
    {
        vCompanyName =view.findViewById(R.id.TextInput_CompanyName);
        vCompanyEmail = view.findViewById(R.id.TextInput_CompanyEmail);
        vCompanyPhoneNumber = view.findViewById(R.id.TextInput_CompanyPhoneNumber);
        vStartDateOfUse = view.findViewById(R.id.TextInput_UseStartDate);
        vStartDateOfUseLayout = view.findViewById(R.id.textInputLayout_UseStartDate);

        vScan = view.findViewById(R.id.Button_Scan);
        vDatePickerBuilder.setTitleText("Start Date of Use");
        vDatePicker = vDatePickerBuilder.build();
    }
    private String convertDateToString(Object selection) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy");
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis((long) selection);
        return simpleDateFormat.format(calendar.getTime());
    }


    private View.OnClickListener oclScan = v -> {
        dismiss();

    };
    private MaterialPickerOnPositiveButtonClickListener pclDatePicker = selection -> {
        vStartDateOfUse.setText(convertDateToString(selection));
        startDate = (long) selection;
    };
    private View.OnClickListener oclStartDateOfUse = v -> {
        vDatePicker.show(getParentFragmentManager(), "DATE_PICKER");
    };

}
