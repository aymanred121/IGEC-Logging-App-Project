package com.example.igec_admin.Fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.example.igec_admin.R;
import com.example.igec_admin.fireBase.Machine;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.zxing.WriterException;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import androidmads.library.qrgenearator.QRGContents;
import androidmads.library.qrgenearator.QRGEncoder;

public class AddMachineFragment extends Fragment {


    // Views
    TextInputLayout vIDLayout, vPurchaseDateLayout;
    TextInputEditText vID, vPurchaseDate, vCodeName;
    ImageView vQRImg;
    MaterialButton vRegister;
    // Vars
    long purchaseDate;
    QRGEncoder qrgEncoder;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    CollectionReference machineCol = db.collection("machine");
    MaterialDatePicker.Builder<Long> vDatePickerBuilder = MaterialDatePicker.Builder.datePicker();
    MaterialDatePicker vDatePicker;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_machine, container, false);
        Initialize(view);

        vIDLayout.setEndIconOnClickListener(oclMachineID);
        vRegister.setOnClickListener(oclRegister);
        vID.addTextChangedListener(atlMachineID);
        vPurchaseDateLayout.setEndIconOnClickListener(oclDate);
        vDatePicker.addOnPositiveButtonClickListener(pclDatePicker);
        return view;
    }

    // Functions
    private void Initialize(View view) {

        vID = view.findViewById(R.id.TextInput_MachineID);
        vIDLayout = view.findViewById(R.id.textInputLayout_MachineID);
        vQRImg = view.findViewById(R.id.ImageView_MachineIDIMG);
        vRegister = view.findViewById(R.id.button_register);
        vCodeName = view.findViewById(R.id.TextInput_MachineCodeName);
        vPurchaseDate = view.findViewById(R.id.TextInput_MachinePurchaseDate);
        vPurchaseDateLayout = view.findViewById(R.id.textInputLayout_MachinePurchaseDate);
        vDatePickerBuilder.setTitleText("Purchase Date");
        vDatePicker = vDatePickerBuilder.build();

    }

    private void clearInput() {
        vID.setText(null);
        vCodeName.setText(null);
        vPurchaseDate.setText(null);
        vQRImg.setImageResource(R.drawable.ic_baseline_image_24);
    }


    private boolean validateInput() {
        return !(vID.getText().toString().isEmpty() || vPurchaseDate.getText().toString().isEmpty() || vCodeName.getText().toString().isEmpty());
    }

    private String convertDateToString(long selection) {
        @SuppressLint("SimpleDateFormat") SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy");
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(selection);
        return simpleDateFormat.format(calendar.getTime());
    }

    // Listeners
    View.OnClickListener oclDate = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            vDatePicker.show(getFragmentManager(), "DATE_PICKER");
        }
    };
    View.OnClickListener oclRegister = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (validateInput()) {
                Machine newMachine = new Machine(vID.getText().toString(), vCodeName.getText().toString(), new Date(purchaseDate));
                machineCol.document(vID.getText().toString()).set(newMachine).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(getActivity(), "Registered", Toast.LENGTH_SHORT).show();
                        clearInput();
                    }
                });
            }
            else
            {
                Toast.makeText(getActivity(), "please, fill the machine data", Toast.LENGTH_SHORT).show();
            }
        }
    };


    View.OnClickListener oclMachineID = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            vID.setText(machineCol.document().getId().substring(0, 5));
            Toast.makeText(getActivity(), "Generated", Toast.LENGTH_SHORT).show();
        }
    };
    TextWatcher atlMachineID = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void afterTextChanged(Editable editable) {
            qrgEncoder = new QRGEncoder(vID.getText().toString(), null, QRGContents.Type.TEXT, 25 * 25);
            try {
                vQRImg.setImageBitmap(qrgEncoder.encodeAsBitmap());
            } catch (WriterException e) {
                e.printStackTrace();
            }
        }
    };


    MaterialPickerOnPositiveButtonClickListener pclDatePicker = new MaterialPickerOnPositiveButtonClickListener() {
        @Override
        public void onPositiveButtonClick(Object selection) {
            vPurchaseDate.setText(convertDateToString((long) selection));
            purchaseDate = (long) selection;
        }
    };


}