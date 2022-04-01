package com.example.igec_admin.Fragments;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentResultListener;

import com.example.igec_admin.Dialogs.AddSupplementsDialog;
import com.example.igec_admin.Dialogs.SupplementInfoDialog;
import com.example.igec_admin.R;
import com.example.igec_admin.fireBase.Allowance;
import com.example.igec_admin.fireBase.Machine;
import com.example.igec_admin.fireBase.Supplement;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.zxing.WriterException;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import androidmads.library.qrgenearator.QRGContents;
import androidmads.library.qrgenearator.QRGEncoder;

public class AddMachineFragment extends Fragment {


    // Views
    private TextInputLayout vIDLayout, vPurchaseDateLayout;
    private TextInputEditText vID, vPurchaseDate, vReference, vAllowance, vMachineByDay, vMachineByWeek, vMachineByMonth;
    private ImageView vQRImg;
    private MaterialButton vRegister, vAddSupplement;
    // Vars
    private long purchaseDate;
    private QRGEncoder qrgEncoder;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference machineCol = db.collection("machine");
    private MaterialDatePicker.Builder<Long> vDatePickerBuilder = MaterialDatePicker.Builder.datePicker();
    private MaterialDatePicker vDatePicker;
    private FirebaseStorage storage = FirebaseStorage.getInstance("gs://test1-c253b.appspot.com");
    private StorageReference storageRef = storage.getReference();
    private ArrayList<Supplement> supplements;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getParentFragmentManager().setFragmentResultListener("supplements", this, new FragmentResultListener() {
            @Override
            public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle bundle) {
                supplements = bundle.getParcelableArrayList("supplements");
                SupplementInfoDialog supplementInfoDialog = new SupplementInfoDialog(-1,supplements.get(0));
                supplementInfoDialog.show(getParentFragmentManager(),"");
            }
        });

    }

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
        vAddSupplement.setOnClickListener(oclAddSupplement);
        return view;
    }

    // Functions
    private void Initialize(View view) {
        supplements = new ArrayList<>();
        vID = view.findViewById(R.id.TextInput_MachineID);
        vIDLayout = view.findViewById(R.id.textInputLayout_MachineID);
        vQRImg = view.findViewById(R.id.ImageView_MachineIDIMG);
        vRegister = view.findViewById(R.id.button_register);
        vAddSupplement = view.findViewById(R.id.button_addSupplements);
        vReference = view.findViewById(R.id.TextInput_MachineCodeName);
        vPurchaseDate = view.findViewById(R.id.TextInput_MachinePurchaseDate);
        vPurchaseDateLayout = view.findViewById(R.id.textInputLayout_MachinePurchaseDate);
        vAllowance = view.findViewById(R.id.TextInput_MachineAllowance);
        vMachineByDay = view.findViewById(R.id.TextInput_MachineByDay);
        vMachineByWeek = view.findViewById(R.id.TextInput_MachineByWeek);
        vMachineByMonth = view.findViewById(R.id.TextInput_MachineByMonth);
        vDatePickerBuilder.setTitleText("Purchase Date");
        vDatePicker = vDatePickerBuilder.build();

    }

    private void clearInput() {
        vID.setText(null);
        vReference.setText(null);
        vPurchaseDate.setText(null);
        vQRImg.setImageResource(R.drawable.ic_baseline_image_24);
    }


    private boolean validateInput() {
        return !(vID.getText().toString().isEmpty() || vPurchaseDate.getText().toString().isEmpty() || vReference.getText().toString().isEmpty());
    }

    private String convertDateToString(long selection) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
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
                saveToInternalStorage();
                saveToCloudStorage();
                Machine newMachine = new Machine(vID.getText().toString(), vReference.getText().toString(), new Date(purchaseDate), new Allowance());
                machineCol.document(vID.getText().toString()).set(newMachine).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(getActivity(), "Registered", Toast.LENGTH_SHORT).show();
                        clearInput();
                    }
                });
            } else {
                Toast.makeText(getActivity(), "please, fill the machine data", Toast.LENGTH_SHORT).show();
            }
        }
    };

    private void saveToCloudStorage() {
        vQRImg.setDrawingCacheEnabled(true);
        vQRImg.buildDrawingCache();
        Bitmap bitmap = ((BitmapDrawable) vQRImg.getDrawable()).getBitmap();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 20, baos);
        byte[] data = baos.toByteArray();
        StorageReference mountainsRef = storageRef.child("imgs/" + vID.getText().toString() + "/" + vReference.getText().toString() + ".jpg");

        UploadTask uploadTask = mountainsRef.putBytes(data);
        uploadTask.addOnSuccessListener(unsed -> {
            Toast.makeText(getActivity(), "uploaded", Toast.LENGTH_SHORT).show();
        }).addOnFailureListener(e -> {
            Toast.makeText(getActivity(), e.toString(), Toast.LENGTH_SHORT).show();
        });
    }


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
    private View.OnClickListener oclAddSupplement = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            AddSupplementsDialog addSupplementsDialog = new AddSupplementsDialog(supplements);
            addSupplementsDialog.show(getParentFragmentManager(), "");
        }
    };


    MaterialPickerOnPositiveButtonClickListener pclDatePicker = new MaterialPickerOnPositiveButtonClickListener() {
        @Override
        public void onPositiveButtonClick(Object selection) {
            vPurchaseDate.setText(convertDateToString((long) selection));
            purchaseDate = (long) selection;
        }
    };

    public void saveToInternalStorage() {
        if (new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + vReference.getText().toString() + ".jpg").exists())
            return;
        Bitmap bitmapImage = ((BitmapDrawable) vQRImg.getDrawable()).getBitmap();
        if (bitmapImage == null)
            return;
        File path = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), vReference.getText().toString() + ".jpg");
        FileOutputStream fos = null;
        try {
            path.createNewFile();
            fos = new FileOutputStream(path.getAbsoluteFile());
            bitmapImage.compress(Bitmap.CompressFormat.JPEG, 10, fos);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return;
    }

}