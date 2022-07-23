package com.example.igec_admin.Dialogs;

import static android.app.Activity.RESULT_OK;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.FileProvider;
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

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.stream.IntStream;

import androidmads.library.qrgenearator.QRGContents;
import androidmads.library.qrgenearator.QRGEncoder;
import de.hdodenhof.circleimageview.CircleImageView;

@RequiresApi(api = Build.VERSION_CODES.R)
public class MachineFragmentDialog extends DialogFragment {


    // Views
    private TextInputLayout vIDLayout, vPurchaseDateLayout, vSerialNumberLayout, vByDayLayout, vByWeekLayout, vByMonthLayout;
    private TextInputEditText vID, vPurchaseDate, vSerialNumber, vMachineByDay, vMachineByWeek, vMachineByMonth;
    private MaterialButton vRegister, vDelete, vUpdate, vAddSupplements;
    private ImageView vQRImg;
    private CircleImageView vMachineImg;

    // Vars
    private String currentPhotoPath;
    private ActivityResultLauncher<Intent> activityResultLauncher;
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
    private Bitmap machineCover;
    private ArrayList<String> oldNames;
    private ArrayList<Pair<TextInputLayout, TextInputEditText>> views;

    public MachineFragmentDialog(Machine machine,Bitmap machineCover) {
        this.machine = machine;
        this.machineCover = machineCover;
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


        vMachineImg.setOnClickListener(oclMachineImg);
        vUpdate.setOnClickListener(oclUpdate);
        vDelete.setOnClickListener(oclDelete);
        vPurchaseDateLayout.setEndIconOnClickListener(oclDate);
        vDatePicker.addOnPositiveButtonClickListener(pclDatePicker);
        vSerialNumberLayout.setEndIconOnClickListener(oclSerialNumber);
        vPurchaseDateLayout.setErrorIconOnClickListener(oclDate);
        vSerialNumberLayout.setErrorIconOnClickListener(oclSerialNumber);
        vAddSupplements.setOnClickListener(oclAddSupplement);
        vSerialNumber.addTextChangedListener(twSerialNumber);
        vPurchaseDate.addTextChangedListener(twPurchaseDate);
        vMachineByDay.addTextChangedListener(twByDay);
        vMachineByWeek.addTextChangedListener(twByWeek);
        vMachineByMonth.addTextChangedListener(twByMonth);
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
        vMachineImg = view.findViewById(R.id.ImageView_MachineIMG);
        vByDayLayout = view.findViewById(R.id.textInputLayout_MachineByDay);
        vByWeekLayout = view.findViewById(R.id.textInputLayout_MachineByWeek);
        vByMonthLayout = view.findViewById(R.id.textInputLayout_MachineByMonth);

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
        vMachineImg.setImageBitmap(machineCover);
        //TODO load the image of machine from database
        vDatePickerBuilder.setTitleText("Purchase Date");
        vDatePicker = vDatePickerBuilder.setSelection(purchaseDate).build();

        views = new ArrayList<>();
        views.add(new Pair<>(vSerialNumberLayout, vSerialNumber));
        views.add(new Pair<>(vPurchaseDateLayout, vPurchaseDate));
        views.add(new Pair<>(vByDayLayout, vMachineByDay));
        views.add(new Pair<>(vByWeekLayout, vMachineByWeek));
        views.add(new Pair<>(vByMonthLayout, vMachineByMonth));
        qrgEncoder = new QRGEncoder(vID.getText().toString(), null, QRGContents.Type.TEXT, 25 * 25);
        try {
            vQRImg.setImageBitmap(qrgEncoder.encodeAsBitmap());
        } catch (WriterException e) {
            e.printStackTrace();
        }
        activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                if (result.getResultCode() == RESULT_OK) {
                    //Bundle bundle = result.getData().getExtras();
                    Bitmap bitmap = BitmapFactory.decodeFile(currentPhotoPath);
                    vMachineImg.setImageBitmap(bitmap);
                    vMachineImg.setBorderWidth(2);
                }
            }
        });
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
            vDelete.setEnabled(true);
            dismiss();
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.R)
    private void updateMachine() throws ParseException {

        HashMap<String, Object> modifiedMachine = new HashMap<>();
        if (supplements != null) {
            int size = supplements.size();
            MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(getActivity());
            builder.setTitle("Uploading...")
                    .setMessage("Uploading Data")
                    .setCancelable(false);
            AlertDialog alertDialog = builder.create();
            alertDialog.show();
            if (supplements.size() > 0) {
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
        machine.setReference(vSerialNumber.getText().toString());
        machine.setPurchaseDate(new SimpleDateFormat("dd/MM/yyyy").parse(vPurchaseDate.getText().toString()));
        machine.setDailyRentPrice(Double.parseDouble(vMachineByDay.getText().toString()));
        machine.setWeeklyRentPrice(Double.parseDouble(vMachineByWeek.getText().toString()));
        machine.setMonthlyRentPrice(Double.parseDouble(vMachineByMonth.getText().toString()));
        machineCol.document(machine.getId()).set(machine).addOnSuccessListener(unused -> {
            db.collection("Machine_Employee").whereEqualTo("machine.id", machine.getId()).get().addOnSuccessListener(queryDocumentSnapshots -> {
                for (DocumentSnapshot d : queryDocumentSnapshots) {
                    db.collection("Machine_Employee")
                            .document(d.getId())
                            .update("machine", machine);
                }
            });
            Toast.makeText(getActivity(), "Updated", Toast.LENGTH_SHORT).show();
            vUpdate.setEnabled(true);
            dismiss();
        });

    }


    private boolean generateError() {
        //TODO add a validation for Machine Image
        for (Pair<TextInputLayout, TextInputEditText> view : views) {
            if (view.second.getText().toString().trim().isEmpty()) {
                if (view.first == vSerialNumberLayout)
                    view.first.setErrorIconDrawable(R.drawable.ic_barcode);
                else if (view.first == vPurchaseDateLayout)
                    view.first.setErrorIconDrawable(R.drawable.ic_baseline_calendar_month_24);


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
        if (generateError())
            return false;

        if (supplements != null) {
            boolean noSupplements = supplements.size() == 0;
            if (noSupplements)
                Toast.makeText(getActivity(), "No accessories were added", Toast.LENGTH_SHORT).show();
            return !noSupplements;
        }
        return true;
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
    private final View.OnClickListener oclUpdate = v -> {
        if (validateInput()) {
            vUpdate.setEnabled(false);
            try {
                updateMachine();
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
    };
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
    private View.OnClickListener oclMachineImg = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            String fileName = "photo";
            File storageDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);

            try {
                File imageFile = File.createTempFile(fileName, ".jpg", storageDirectory);
                currentPhotoPath = imageFile.getAbsolutePath();

                Uri imageUri = FileProvider.getUriForFile(getActivity(), "com.example.igec_admin.fileprovider", imageFile);

                Intent takePicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                takePicture.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                activityResultLauncher.launch(takePicture);
            } catch (IOException e) {
                e.printStackTrace();
            }
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

        vDelete.setEnabled(false);
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(getActivity());
        builder.setTitle(getString(R.string.Delete))
                .setMessage(getString(R.string.AreUSure))
                .setNegativeButton(getString(R.string.cancel), (dialogInterface, i) -> {
                    vDelete.setEnabled(true);
                })
                .setPositiveButton(getString(R.string.accept), (dialogInterface, i) -> {
                    deleteMachine();
                    dialogInterface.dismiss();
                })
                .show();

    };
    private final TextWatcher twSerialNumber = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void afterTextChanged(Editable editable) {
            vSerialNumberLayout.setError(null);
            vSerialNumberLayout.setErrorEnabled(false);
        }
    };
    private final TextWatcher twPurchaseDate = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void afterTextChanged(Editable editable) {
            vPurchaseDateLayout.setError(null);
            vPurchaseDateLayout.setErrorEnabled(false);
        }
    };
    private final TextWatcher twByDay = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void afterTextChanged(Editable editable) {
            vByDayLayout.setError(null);
            vByDayLayout.setErrorEnabled(false);
        }
    };
    private final TextWatcher twByWeek = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void afterTextChanged(Editable editable) {
            vByWeekLayout.setError(null);
            vByWeekLayout.setErrorEnabled(false);
        }
    };
    private final TextWatcher twByMonth = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void afterTextChanged(Editable editable) {
            vByMonthLayout.setError(null);
            vByMonthLayout.setErrorEnabled(false);
        }
    };
    private final MaterialPickerOnPositiveButtonClickListener pclDatePicker = new MaterialPickerOnPositiveButtonClickListener() {
        @Override
        public void onPositiveButtonClick(Object selection) {
            vPurchaseDate.setText(convertDateToString((long) selection));
            purchaseDate = (long) selection;
        }
    };
}
