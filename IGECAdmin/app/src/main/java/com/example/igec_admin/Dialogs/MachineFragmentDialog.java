package com.example.igec_admin.Dialogs;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.os.Build;
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
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
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
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.zxing.WriterException;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.stream.IntStream;

import androidmads.library.qrgenearator.QRGContents;
import androidmads.library.qrgenearator.QRGEncoder;

@RequiresApi(api = Build.VERSION_CODES.R)
public class MachineFragmentDialog extends DialogFragment {


    // Views
    private TextInputLayout vIDLayout, vPurchaseDateLayout,vSerialNumberLayout;
    private TextInputEditText vID, vPurchaseDate, vSerialNumber, vMachineByDay, vMachineByWeek, vMachineByMonth;
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
    private final FirebaseStorage storage = FirebaseStorage.getInstance();
    private final StorageReference storageRef = storage.getReference();
    private ArrayList<String> oldNames;

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
                oldNames = bundle.getStringArrayList("oldNames");
            }
        });
        getParentFragmentManager().setFragmentResultListener("machine", this, new FragmentResultListener() {
            @Override
            public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle result) {
                vSerialNumber.setText(result.getString("SerialNumber"));
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
        vSerialNumberLayout.setEndIconOnClickListener(oclSerialNumber);
        vAddSupplements.setOnClickListener(oclAddSupplement);
        return view;
    }

    // Functions
    private void initialize(View view) {
        oldNames = new ArrayList<>();
        vRegister = view.findViewById(R.id.button_register);
        vUpdate = view.findViewById(R.id.button_update);
        vDelete = view.findViewById(R.id.button_delete);
        vSerialNumberLayout = view.findViewById(R.id.textInputLayout_MachineSerialNumber);
        vSerialNumber = view.findViewById(R.id.TextInput_MachineSerialNumber);
        vPurchaseDate = view.findViewById(R.id.TextInput_MachinePurchaseDate);
        vPurchaseDateLayout = view.findViewById(R.id.textInputLayout_MachinePurchaseDate);
        vID = view.findViewById(R.id.TextInput_MachineID);
        vIDLayout = view.findViewById(R.id.textInputLayout_MachineID);
        vQRImg = view.findViewById(R.id.ImageView_MachineIDIMG);
        vAddSupplements = view.findViewById(R.id.button_addSupplements);
        vMachineByDay = view.findViewById(R.id.TextInput_MachineByDay);
        vMachineByWeek = view.findViewById(R.id.TextInput_MachineByWeek);
        vMachineByMonth = view.findViewById(R.id.TextInput_MachineByMonth);


        vRegister.setVisibility(View.GONE);
        vDelete.setVisibility(View.VISIBLE);
        vUpdate.setVisibility(View.VISIBLE);

        vID.setEnabled(false);

        vSerialNumber.setText(machine.getReference());
        vID.setText(machine.getId());
        purchaseDate = machine.getPurchaseDate().getTime();
        vPurchaseDate.setText(convertDateToString(machine.getPurchaseDate().getTime()));
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
    }


    private void deleteMachine() {
        /*
          we can't delete machine from Machine_Employee
          since this would imply that this machine didn't exist
          in the first place
          */
        for (String name : machine.getSupplementsNames()) {
            storageRef.child("imgs/" + vID.getText().toString() + "/" + name + ".jpg").delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void unused) {
                    //do nothing
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                }
            });
        }
        machineCol.document(machine.getId()).delete().addOnSuccessListener(unused -> {
            Toast.makeText(getActivity(), "Deleted", Toast.LENGTH_SHORT).show();
            dismiss();
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.R)
    private void updateMachine() {

        if (!validateInput()) {
            Toast.makeText(getActivity(), "please, fill the machine data", Toast.LENGTH_SHORT).show();
            return;
        }

        HashMap<String, Object> modifiedMachine = new HashMap<>();
        if (supplements != null) {
            int size = supplements.size();
            MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(getActivity());
            builder.setTitle("Uploading...")
                    .setMessage("Uploading Data")
                    .setCancelable(false);
            AlertDialog alertDialog = builder.create();
            alertDialog.show();
            if(supplements.size()>0) {
                machine.getSupplementsNames().clear();
                IntStream.range(0, supplements.size()).forEach(i -> machine.getSupplementsNames().add(supplements.get(i).getName()));
            }
            for (String name : oldNames) {
                storageRef.child("imgs/" + vID.getText().toString() + "/" + name + ".jpg").delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        //do nothing
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                    }
                });
            }
            oldNames.clear();
            for (int i = 0; i < size; i++) {
                Integer[] finalI = new Integer[1];
                finalI[0] = i;
                supplements.get(i).saveToCloudStorage(storageRef, vID.getText().toString()).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        if (finalI[0] == size - 1) {
                            alertDialog.dismiss();
                        }
                    }
                });
            }
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
                vSerialNumber.getText().toString().isEmpty() ||
                vMachineByDay.getText().toString().isEmpty() ||
                vMachineByWeek.getText().toString().isEmpty() ||
                vMachineByMonth.getText().toString().isEmpty());
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
            AddSupplementsDialog addSupplementsDialog;
            if (supplements == null)
                addSupplementsDialog = new AddSupplementsDialog(machine);
            else
                addSupplementsDialog = new AddSupplementsDialog(supplements);
            addSupplementsDialog.show(getParentFragmentManager(), "");
        }
    };
    private View.OnClickListener oclSerialNumber = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            MachineSerialNumberDialog machineSerialNumberDialog = new MachineSerialNumberDialog();
            machineSerialNumberDialog.show(getParentFragmentManager(), "");
        }
    };
    private final View.OnClickListener oclDelete = v -> {

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(getActivity());
        builder.setTitle(getString(R.string.Delete))
                .setMessage(getString(R.string.AreUSure))
                .setNegativeButton(getString(R.string.cancel), (dialogInterface, i) -> {
                })
                .setPositiveButton(getString(R.string.accept), (dialogInterface, i) -> {
                    deleteMachine();
                    dialogInterface.dismiss();
                })
                .show();

    };

    private final MaterialPickerOnPositiveButtonClickListener pclDatePicker = new MaterialPickerOnPositiveButtonClickListener() {
        @Override
        public void onPositiveButtonClick(Object selection) {
            vPurchaseDate.setText(convertDateToString((long) selection));
            purchaseDate = (long) selection;
        }
    };
}
