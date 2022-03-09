package com.example.igec_admin.Dialogs;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.igec_admin.R;
import com.example.igec_admin.fireBase.Machine;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.zxing.WriterException;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

import androidmads.library.qrgenearator.QRGContents;
import androidmads.library.qrgenearator.QRGEncoder;

public class MachineFragmentDialog extends DialogFragment {


    // Views
    TextInputLayout vIDLayout, vPurchaseDateLayout;
    TextInputEditText vID, vPurchaseDate, vCodeName;
    MaterialButton vRegister, vDelete, vUpdate;
    ImageView vQRImg;

    // Vars
    QRGEncoder qrgEncoder;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    CollectionReference machineCol = db.collection("machine");
    MaterialDatePicker.Builder<Long> vDatePickerBuilder = MaterialDatePicker.Builder.datePicker();
    MaterialDatePicker vDatePicker;
    Machine machine;


    public MachineFragmentDialog(Machine machine) {
        this.machine = machine;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.FullscreenDialogTheme);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_machine, container, false);
        Initialize(view);

        vUpdate.setOnClickListener(oclUpdate);
        vDelete.setOnClickListener(oclDelete);
        vPurchaseDateLayout.setEndIconOnClickListener(oclDate);
        vDatePicker.addOnPositiveButtonClickListener(pclDatePicker);
        return view;
    }

    // Functions
    private void Initialize(View view) {

        vRegister = view.findViewById(R.id.button_register);
        vUpdate = view.findViewById(R.id.button_update);
        vDelete = view.findViewById(R.id.button_delete);
        vCodeName = view.findViewById(R.id.TextInput_MachineCodeName);
        vPurchaseDate = view.findViewById(R.id.TextInput_MachinePurchaseDate);
        vPurchaseDateLayout = view.findViewById(R.id.textInputLayout_MachinePurchaseDate);
        vID = view.findViewById(R.id.TextInput_MachineID);
        vIDLayout = view.findViewById(R.id.textInputLayout_MachineID);
        vQRImg = view.findViewById(R.id.ImageView_MachineIDIMG);
        vDatePickerBuilder.setTitleText("Purchase Date");
        vDatePicker = vDatePickerBuilder.build();


        vRegister.setVisibility(View.GONE);
        vDelete.setVisibility(View.VISIBLE);
        vUpdate.setVisibility(View.VISIBLE);

        vID.setEnabled(false);

        vCodeName.setText(machine.getCodeName());
        vID.setText(machine.getId());
        vPurchaseDate.setText(machine.getPurchaseDate());
        qrgEncoder = new QRGEncoder(vID.getText().toString(), null, QRGContents.Type.TEXT, 25 * 25);
        try {
            vQRImg.setImageBitmap(qrgEncoder.encodeAsBitmap());
        } catch (WriterException e) {
            e.printStackTrace();
        }

    }
    private void deleteMachine() {
        /*
          we can't delete machine from Machine_Employee
          since this would imply that this machine didn't exist
          in the first place
          */
        machineCol.document(machine.getId()).delete().addOnSuccessListener(unused ->Toast.makeText(getActivity(), "Deleted", Toast.LENGTH_SHORT).show());
    }

    private void updateMachine() {
        HashMap<String,Object> modifiedMachine = new HashMap<>();
        modifiedMachine.put("codeName",vCodeName.getText().toString());
        modifiedMachine.put("purchaseDate",vPurchaseDate.getText().toString());
        modifiedMachine.put("id",machine.getId());
        machineCol.document(machine.getId()).update(modifiedMachine).addOnSuccessListener(unused -> {
            db.collection("Machine_Employee").whereEqualTo("Machine",machine).get().addOnSuccessListener(queryDocumentSnapshots -> {
                for (DocumentSnapshot d : queryDocumentSnapshots){
                    db.collection("Machine_Employee")
                            .document(d.getId())
                            .update("Machine",modifiedMachine);
                }
            });
            Toast.makeText(getActivity(), "Updated", Toast.LENGTH_SHORT).show();
        });
    }

    // Listeners
    View.OnClickListener oclDate = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            vDatePicker.show(getFragmentManager(), "DATE_PICKER");
        }
    };
    View.OnClickListener oclUpdate = v -> updateMachine();

    View.OnClickListener oclDelete = v -> deleteMachine();
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
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy");
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis((long) selection);
            vPurchaseDate.setText(simpleDateFormat.format(calendar.getTime()));
        }
    };
}
