package com.igec.admin.Fragments;

import static android.app.Activity.RESULT_OK;

import android.Manifest;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
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
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentResultListener;

import com.igec.admin.Dialogs.AddSupplementsDialog;
import com.igec.admin.Dialogs.MachineSerialNumberDialog;
import com.igec.admin.Dialogs.SupplementInfoDialog;
import com.igec.admin.R;
import com.igec.common.firebase.Allowance;
import com.igec.common.firebase.Machine;
import com.igec.common.firebase.Supplement;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.zxing.WriterException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

import androidmads.library.qrgenearator.QRGContents;
import androidmads.library.qrgenearator.QRGEncoder;
import de.hdodenhof.circleimageview.CircleImageView;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;

public class AddMachineFragment extends Fragment implements EasyPermissions.PermissionCallbacks {

    private final int PROJECT = 0;
    private final int NETSALARY = 1;
    private final int ALLOWANCE = 2;
    private final int BONUS = 3;
    private final int PENALTY = 4;
    // Views
    private TextInputLayout vIdLayout, vPurchaseDateLayout, vSerialNumberLayout, vByDayLayout, vByWeekLayout, vByMonthLayout;
    private TextInputEditText vId, vPurchaseDate, vSerialNumber, vByDay, vByWeek, vByMonth;
    private ImageView vQRImg;
    private CircleImageView vMachineImg;
    private MaterialButton vRegister, vAddSupplement;
    // Vars
    private String currentPhotoPath;
    private ActivityResultLauncher<Intent> activityResultLauncher;
    private static final int CAMERA_REQUEST_CODE = 100;
    private long purchaseDate;
    private QRGEncoder qrgEncoder;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final CollectionReference machineCol = db.collection("machine");
    private MaterialDatePicker.Builder<Long> vDatePickerBuilder = MaterialDatePicker.Builder.datePicker();
    private MaterialDatePicker vDatePicker;
    private final FirebaseStorage storage = FirebaseStorage.getInstance();
    private final StorageReference storageRef = storage.getReference();
    private ArrayList<Supplement> supplements = new ArrayList<>();
    private Supplement machineCover;
    private ArrayList<Pair<TextInputLayout, TextInputEditText>> views;


    public void saveToInternalStorage() {
        if (new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + vSerialNumber.getText().toString() + ".jpg").exists())
            return;
        Bitmap bitmapImage = ((BitmapDrawable) vQRImg.getDrawable()).getBitmap();
        if (bitmapImage == null)
            return;
        File path = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), vSerialNumber.getText().toString() + ".jpg");
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

    @AfterPermissionGranted(CAMERA_REQUEST_CODE)
    private boolean getCameraPermission() {
        String[] perms = {Manifest.permission.CAMERA};
        if (EasyPermissions.hasPermissions(getContext(), perms)) {
            return true;
        } else {
            EasyPermissions.requestPermissions(this, "We need camera permission in order to be able to scan the serial number",
                    CAMERA_REQUEST_CODE, perms);
            return false;
        }
    }

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {
    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            new AppSettingsDialog.Builder(this).build().show();
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getParentFragmentManager().setFragmentResultListener("supplements", this, new FragmentResultListener() {
            @Override
            public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle bundle) {
                supplements = bundle.getParcelableArrayList("supplements");
            }
        });
        getParentFragmentManager().setFragmentResultListener("machine", this, new FragmentResultListener() {
            @Override
            public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle result) {
                String serialNumber = result.getString("SerialNumber");
                Pattern mPattern = Pattern.compile("[0-9]+");
                if(mPattern.matcher(serialNumber).matches())
                    vSerialNumber.setText(serialNumber);
                else
                    Toast.makeText(getActivity(), "Invalid Serial Number", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_machine, container, false);
        Initialize(view);
        getCameraPermission();
        vRegister.setOnClickListener(oclRegister);
        vIdLayout.setEndIconOnClickListener(oclMachineID);
        vPurchaseDateLayout.setEndIconOnClickListener(oclDate);
        vSerialNumberLayout.setEndIconOnClickListener(oclSerialNumber);
        vIdLayout.setErrorIconOnClickListener(oclMachineID);
        vPurchaseDateLayout.setErrorIconOnClickListener(oclDate);
        vSerialNumberLayout.setErrorIconOnClickListener(oclSerialNumber);
        vDatePicker.addOnPositiveButtonClickListener(pclDatePicker);
        vAddSupplement.setOnClickListener(oclAddSupplement);

        vId.addTextChangedListener(twId);
        vMachineImg.setOnClickListener(oclMachineImg);
        vSerialNumber.addTextChangedListener(twSerialNumber);
        vPurchaseDate.addTextChangedListener(twPurchaseDate);
        vByDay.addTextChangedListener(twByDay);
        vByWeek.addTextChangedListener(twByWeek);
        vByMonth.addTextChangedListener(twByMonth);

        return view;
    }
    // Functions

    private void Initialize(View view) {

        vId = view.findViewById(R.id.TextInput_MachineID);
        vQRImg = view.findViewById(R.id.ImageView_MachineIDIMG);
        vMachineImg = view.findViewById(R.id.ImageView_MachineIMG);
        vRegister = view.findViewById(R.id.button_register);
        vAddSupplement = view.findViewById(R.id.button_addSupplements);
        vSerialNumber = view.findViewById(R.id.TextInput_MachineSerialNumber);
        vPurchaseDate = view.findViewById(R.id.TextInput_MachinePurchaseDate);
        vByDay = view.findViewById(R.id.TextInput_MachineByDay);
        vByWeek = view.findViewById(R.id.TextInput_MachineByWeek);
        vByMonth = view.findViewById(R.id.TextInput_MachineByMonth);
        vDatePickerBuilder.setTitleText("Purchase Date");
        vDatePicker = vDatePickerBuilder.build();

        vIdLayout = view.findViewById(R.id.textInputLayout_MachineID);
        vSerialNumberLayout = view.findViewById(R.id.textInputLayout_MachineSerialNumber);
        vPurchaseDateLayout = view.findViewById(R.id.textInputLayout_MachinePurchaseDate);
        vByDayLayout = view.findViewById(R.id.textInputLayout_MachineByDay);
        vByWeekLayout = view.findViewById(R.id.textInputLayout_MachineByWeek);
        vByMonthLayout = view.findViewById(R.id.textInputLayout_MachineByMonth);

        views = new ArrayList<>();
        views.add(new Pair<>(vIdLayout, vId));
        views.add(new Pair<>(vSerialNumberLayout, vSerialNumber));
        views.add(new Pair<>(vPurchaseDateLayout, vPurchaseDate));
        views.add(new Pair<>(vByDayLayout, vByDay));
        views.add(new Pair<>(vByWeekLayout, vByWeek));
        views.add(new Pair<>(vByMonthLayout, vByMonth));
        activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                if (result.getResultCode() == RESULT_OK) {
                    //Bundle bundle = result.getData().getExtras();
                    Bitmap bitmap = BitmapFactory.decodeFile(currentPhotoPath);
                    vMachineImg.setImageBitmap(bitmap);
                    vMachineImg.setBorderWidth(2);
                    machineCover = new Supplement();
                    machineCover.setName("cover");
                    machineCover.setPhoto(bitmap);
                }
            }
        });
    }

    private void clearInput() {
        vRegister.setEnabled(true);
        vId.setText(null);
        vSerialNumber.setText(null);
        vPurchaseDate.setText(null);
        vQRImg.setImageResource(R.drawable.ic_baseline_image_24);
        vByDay.setText(null);
        vByMonth.setText(null);
        vByWeek.setText(null);
        machineCover = null;
        vMachineImg.setImageResource(R.drawable.ic_baseline_image_200);
        vMachineImg.setBorderWidth(0);
        supplements.clear();
        vDatePickerBuilder = MaterialDatePicker.Builder.datePicker();
        vDatePicker = vDatePickerBuilder.build();
        vDatePicker.addOnPositiveButtonClickListener(pclDatePicker);
    }

    private boolean generateError() {
        for (Pair<TextInputLayout, TextInputEditText> view : views) {
            if (view.second.getText().toString().trim().isEmpty()) {
                if (view.first == vIdLayout)
                    view.first.setErrorIconDrawable(R.drawable.ic_baseline_autorenew_24);
                else if (view.first == vSerialNumberLayout)
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
        if(machineCover == null)
        {
            Toast.makeText(getActivity(), "Machine Image Missing", Toast.LENGTH_SHORT).show();
            return true;
        }
        return false;
    }

    private boolean validateInput() {
        if (generateError())
            return false;
        boolean noSupplements = supplements.size() == 0;
        if(noSupplements)
            Toast.makeText(getActivity(), "Accessories Missing", Toast.LENGTH_SHORT).show();
        return !noSupplements;
    }

    private String convertDateToString(long selection) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
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
    private final View.OnClickListener oclSerialNumber = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (getCameraPermission()) {
                MachineSerialNumberDialog machineSerialNumberDialog = new MachineSerialNumberDialog();
                machineSerialNumberDialog.show(getParentFragmentManager(), "");
            }

        }
    };
    private final View.OnClickListener oclRegister = new View.OnClickListener() {
        @RequiresApi(api = Build.VERSION_CODES.R)
        @Override
        public void onClick(View v) {
            if (validateInput()) {
                vRegister.setEnabled(false);
                int size = supplements.size();
                MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(getActivity());
                builder.setTitle("Uploading...")
                        .setMessage("Uploading Data")
                        .setCancelable(false);
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
                for (int i = 0; i < size; i++) {
                    Integer[] finalI = new Integer[1];
                    finalI[0] = i;
                    supplements.get(i).saveToCloudStorage(storageRef, vId.getText().toString()).addOnSuccessListener(taskSnapshot -> {
                        if (finalI[0] == size - 1) {
                            alertDialog.dismiss();
                        }

                    }).addOnFailureListener(e -> {
                        alertDialog.dismiss();
                        Toast.makeText(getActivity(), "Failed to upload, check your internet", Toast.LENGTH_SHORT).show();
                    });
                }
                saveToInternalStorage();
                Machine newMachine = new Machine(vId.getText().toString(), vSerialNumber.getText().toString(), new Date(purchaseDate));
                newMachine.setDailyRentPrice(Double.parseDouble(vByDay.getText().toString()));
                newMachine.setWeeklyRentPrice(Double.parseDouble(vByWeek.getText().toString()));
                newMachine.setMonthlyRentPrice(Double.parseDouble(vByMonth.getText().toString()));
                newMachine.setSupplementsNames(new ArrayList<>());
                IntStream.range(0, supplements.size()).forEach(i -> newMachine.getSupplementsNames().add(supplements.get(i).getName()));
                machineCover.saveToCloudStorage( FirebaseStorage.getInstance().getReference(),vId.getText().toString()).addOnSuccessListener(unused->{
                    machineCol.document(vId.getText().toString()).set(newMachine).addOnSuccessListener(unused1 -> {
                        Toast.makeText(getActivity(), "Registered", Toast.LENGTH_SHORT).show();
                        clearInput();
                    });
                });
            }
        }
    };
    private final View.OnClickListener oclMachineID = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            vId.setText(machineCol.document().getId().substring(0, 5));
        }
    };
    private final View.OnClickListener oclAddSupplement = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            AddSupplementsDialog addSupplementsDialog = new AddSupplementsDialog(supplements);
            addSupplementsDialog.show(getParentFragmentManager(), "");
        }
    };
    private View.OnClickListener oclMachineImg = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            String fileName = "photo";
            File storageDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);

            try {
                File imageFile = File.createTempFile(fileName,".jpg", storageDirectory);
                currentPhotoPath = imageFile.getAbsolutePath();

                Uri imageUri =  FileProvider.getUriForFile(getActivity(),"com.igec.admin.fileprovider",imageFile);

                Intent takePicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                takePicture. putExtra(MediaStore.EXTRA_OUTPUT,imageUri);
                activityResultLauncher.launch(takePicture);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    };
    private final MaterialPickerOnPositiveButtonClickListener pclDatePicker = new MaterialPickerOnPositiveButtonClickListener() {
        @Override
        public void onPositiveButtonClick(Object selection) {
            vPurchaseDate.setText(convertDateToString((long) selection));
            purchaseDate = (long) selection;
        }
    };
    private final TextWatcher twId = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void afterTextChanged(Editable editable) {
            qrgEncoder = new QRGEncoder(vId.getText().toString(), null, QRGContents.Type.TEXT, 25 * 25);
            try {
                vQRImg.setImageBitmap(qrgEncoder.encodeAsBitmap());
            } catch (WriterException e) {
                e.printStackTrace();
            }
            vIdLayout.setError(null);
            vIdLayout.setErrorEnabled(false);
        }
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


}