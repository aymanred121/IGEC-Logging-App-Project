package com.example.igec_admin;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.igec_admin.fireBase.Machine;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.zxing.WriterException;
import androidmads.library.qrgenearator.QRGContents;
import androidmads.library.qrgenearator.QRGEncoder;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class Add_Machine extends Fragment {


    // Views
    TextInputLayout vMachineIDLayout;
    TextInputEditText vMachineID;
    ImageView vMachineIDIMG;
    MaterialButton vRegister;
    // Vars
    QRGEncoder qrgEncoder;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    CollectionReference machineCol = db.collection("machine");
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
       View view = inflater.inflate(R.layout.fragment_add_machine, container, false);
       Initialize(view);

        vMachineIDLayout.setEndIconOnClickListener(oclMachineID);
        vRegister.setOnClickListener(oclRegister);
        vMachineID.addTextChangedListener(atlMachineID);
        return view;
    }
    // Functions
    private void Initialize(View view) {

        vMachineID = view.findViewById(R.id.TextInput_MachineID);
        vMachineIDLayout = view.findViewById(R.id.textInputLayout_MachineID);
        vMachineIDIMG = view.findViewById(R.id.ImageView_MachineIDIMG);
        vRegister = view.findViewById(R.id.button_register);

    }


    // Listeners
    View.OnClickListener oclRegister = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Machine newMachine = new Machine(vMachineID.getText().toString(),"cool","yesterday");
            machineCol.document(vMachineID.getText().toString()).set(newMachine).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void unused) {
                    Toast.makeText(getActivity(), "Registered", Toast.LENGTH_SHORT).show();

                }
            });
        }
    };
    View.OnClickListener oclMachineID = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            vMachineID.setText(machineCol.document().getId().substring(0,5));
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
            qrgEncoder = new QRGEncoder(vMachineID.getText().toString(),null,QRGContents.Type.TEXT,25*25);
            try {
                vMachineIDIMG.setImageBitmap(qrgEncoder.encodeAsBitmap());
            } catch (WriterException e) {
                e.printStackTrace();
            }
        }
    };




}