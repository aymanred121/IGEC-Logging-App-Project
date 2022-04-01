package com.example.igec_admin.Dialogs;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentResultListener;

import com.example.igec_admin.R;
import com.example.igec_admin.fireBase.Machine;
import com.example.igec_admin.fireBase.Supplement;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.zxing.WriterException;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.stream.IntStream;

import androidmads.library.qrgenearator.QRGContents;
import androidmads.library.qrgenearator.QRGEncoder;

public class MachineFragmentDialog extends DialogFragment {


    // Views
    private TextInputLayout vIDLayout, vPurchaseDateLayout;
    private TextInputEditText vID, vPurchaseDate, vReference, vAllowance, vMachineByDay, vMachineByWeek, vMachineByMonth;
    private MaterialButton vRegister, vDelete, vUpdate, vAddSupplements;
    private ImageView vQRImg;

    // Vars
    private long purchaseDate;
    private QRGEncoder qrgEncoder;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final CollectionReference machineCol = db.collection("machine");
    private final MaterialDatePicker.Builder<Long> vDatePickerBuilder = MaterialDatePicker.Builder.datePicker();
    private MaterialDatePicker vDatePicker;
    private final Machine machine;
    private ArrayList<Supplement> supplements;
    private final FirebaseStorage storage = FirebaseStorage.getInstance("gs://igec-dab77.appspot.com");
    private final StorageReference storageRef = storage.getReference();


    public MachineFragmentDialog(Machine machine) {
        this.machine = machine;
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
        getParentFragmentManager().setFragmentResultListener("supplements", this, new FragmentResultListener() {
            @Override
            public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle bundle) {
                supplements = bundle.getParcelableArrayList("supplements");
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_machine, container, false);
        initialize(view);

        vUpdate.setOnClickListener(oclUpdate);
        vDelete.setOnClickListener(oclDelete);
        vPurchaseDateLayout.setEndIconOnClickListener(oclDate);
        vDatePicker.addOnPositiveButtonClickListener(pclDatePicker);
        vAddSupplements.setOnClickListener(oclAddSupplement);
        return view;
    }

    // Functions
    private void initialize(View view) {
        vRegister = view.findViewById(R.id.button_register);
        vUpdate = view.findViewById(R.id.button_update);
        vDelete = view.findViewById(R.id.button_delete);
        vReference = view.findViewById(R.id.TextInput_MachineCodeName);
        vPurchaseDate = view.findViewById(R.id.TextInput_MachinePurchaseDate);
        vPurchaseDateLayout = view.findViewById(R.id.textInputLayout_MachinePurchaseDate);
        vID = view.findViewById(R.id.TextInput_MachineID);
        vIDLayout = view.findViewById(R.id.textInputLayout_MachineID);
        vQRImg = view.findViewById(R.id.ImageView_MachineIDIMG);
        vAddSupplements = view.findViewById(R.id.button_addSupplements);
        vAllowance = view.findViewById(R.id.TextInput_MachineAllowance);
        vMachineByDay = view.findViewById(R.id.TextInput_MachineByDay);
        vMachineByWeek = view.findViewById(R.id.TextInput_MachineByWeek);
        vMachineByMonth = view.findViewById(R.id.TextInput_MachineByMonth);
        supplements = new ArrayList<>();


        vRegister.setVisibility(View.GONE);
        vDelete.setVisibility(View.VISIBLE);
        vUpdate.setVisibility(View.VISIBLE);

        vID.setEnabled(false);

        vReference.setText(machine.getReference());
        vID.setText(machine.getId());
        purchaseDate = machine.getPurchaseDate().getTime();
        vPurchaseDate.setText(convertDateToString(machine.getPurchaseDate().getTime()));
        vAllowance.setText(String.valueOf(machine.getAllowance().getAmount()));
        vMachineByDay.setText(String.valueOf(machine.getDailyRentPrice()));
        vMachineByWeek.setText(String.valueOf(machine.getWeeklyRentPrice()));
        vMachineByMonth.setText(String.valueOf(machine.getMonthlyRentPrice()));
        vDatePickerBuilder.setTitleText("Purchase Date");
        vDatePicker = vDatePickerBuilder.setSelection(purchaseDate).build();
        qrgEncoder = new QRGEncoder(vID.getText().toString(), null, QRGContents.Type.TEXT, 25 * 25);
        try {
            vQRImg.setImageBitmap(qrgEncoder.encodeAsBitmap());
        } catch (WriterException e) {
            e.printStackTrace();
        }
        getAllSupplements();
    }

    private void getAllSupplements() {
        StorageReference ref;
        for (String name : machine.getSupplementsNames()) {
            ref = FirebaseStorage.getInstance().getReference().child("/imgs/" + vID.getText().toString() + String.format("/%s.jpg", name));
            try {
                final File localFile = File.createTempFile(name, "jpg");
                ref.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                        Bitmap bitmap = BitmapFactory.decodeFile(localFile.getAbsolutePath());
                        supplements.add(new Supplement(name, bitmap));
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void deleteMachine() {
        /*
          we can't delete machine from Machine_Employee
          since this would imply that this machine didn't exist
          in the first place
          */
        machineCol.document(machine.getId()).delete().addOnSuccessListener(unused -> {
            Toast.makeText(getActivity(), "Deleted", Toast.LENGTH_SHORT).show();
            dismiss();
        });
    }

    private void updateMachine() {

        if (!validateInput()) {
            Toast.makeText(getActivity(), "please, fill the machine data", Toast.LENGTH_SHORT).show();
            return;
        }
        HashMap<String, Object> modifiedMachine = new HashMap<>();
        machine.getSupplementsNames().clear();
        //TODO update supplements images
        IntStream.range(0, supplements.size()).forEach(i -> machine.getSupplementsNames().add(supplements.get(i).getName()));
        for (Supplement supplement : supplements) {
            supplement.saveToCloudStorage(storageRef, vID.getText().toString());
        }
        machineCol.document(machine.getId()).set(machine).addOnSuccessListener(unused -> {
            db.collection("Machine_Employee").whereEqualTo("Machine", machine).get().addOnSuccessListener(queryDocumentSnapshots -> {
                for (DocumentSnapshot d : queryDocumentSnapshots) {
                    db.collection("Machine_Employee")
                            .document(d.getId())
                            .update("Machine", modifiedMachine);
                }
            });
            Toast.makeText(getActivity(), "Updated", Toast.LENGTH_SHORT).show();
            dismiss();
        });

    }


    private boolean validateInput() {
        return !(vID.getText().toString().isEmpty() ||
                vPurchaseDate.getText().toString().isEmpty() ||
                vReference.getText().toString().isEmpty() ||
                vMachineByDay.getText().toString().isEmpty() ||
                vMachineByWeek.getText().toString().isEmpty() ||
                vMachineByMonth.getText().toString().isEmpty() ||
                vAllowance.getText().toString().isEmpty() ||
                supplements.size() == 0
        );
    }

    private String convertDateToString(long selection) {
        @SuppressLint("SimpleDateFormat") SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy");
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(selection);
        return simpleDateFormat.format(calendar.getTime());
    }

    // Listeners
    private final View.OnClickListener oclDate = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            vDatePicker.show(getFragmentManager(), "DATE_PICKER");
        }
    };
    private final View.OnClickListener oclUpdate = v -> updateMachine();
    private final View.OnClickListener oclAddSupplement = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            AddSupplementsDialog addSupplementsDialog = new AddSupplementsDialog(supplements);
            addSupplementsDialog.show(getParentFragmentManager(), "");
        }
    };

    private final View.OnClickListener oclDelete = v -> deleteMachine();

    private final MaterialPickerOnPositiveButtonClickListener pclDatePicker = new MaterialPickerOnPositiveButtonClickListener() {
        @Override
        public void onPositiveButtonClick(Object selection) {
            vPurchaseDate.setText(convertDateToString((long) selection));
            purchaseDate = (long) selection;
        }
    };
}
