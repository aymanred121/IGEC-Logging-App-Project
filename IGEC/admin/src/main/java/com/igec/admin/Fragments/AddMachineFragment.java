package com.igec.admin.Fragments;

import static android.app.Activity.RESULT_OK;

import static com.igec.common.CONSTANTS.CAMERA_REQUEST_CODE;

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

import com.igec.admin.Dialogs.AddAccessoriesDialog;
import com.igec.admin.Dialogs.MachineSerialNumberDialog;
import com.igec.admin.R;
import com.igec.admin.databinding.FragmentAddMachineBinding;
import com.igec.common.firebase.Machine;
import com.igec.common.firebase.Supplement;
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
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;

public class AddMachineFragment extends Fragment implements EasyPermissions.PermissionCallbacks {

    private final int PROJECT = 0;
    private final int NETSALARY = 1;
    private final int ALLOWANCE = 2;
    private final int BONUS = 3;
    private final int PENALTY = 4;
    // Vars
    private String currentPhotoPath;
    private ActivityResultLauncher<Intent> activityResultLauncher;
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
        if (new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + binding.serialNumberEdit.getText().toString() + ".jpg").exists())
            return;
        Bitmap bitmapImage = ((BitmapDrawable) binding.idImageView.getDrawable()).getBitmap();
        if (bitmapImage == null)
            return;
        File path = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), binding.serialNumberEdit.getText().toString() + ".jpg");
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
                if (mPattern.matcher(serialNumber).matches())
                    binding.serialNumberEdit.setText(serialNumber);
                else
                    Toast.makeText(getActivity(), "Invalid Serial Number", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private FragmentAddMachineBinding binding;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentAddMachineBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        binding = null;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initialize();
        getCameraPermission();
        binding.registerButton.setOnClickListener(oclRegister);
        binding.idLayout.setEndIconOnClickListener(oclMachineID);
        binding.purchaseDateLayout.setEndIconOnClickListener(oclDate);
        binding.serialNumberLayout.setEndIconOnClickListener(oclSerialNumber);
        binding.idLayout.setErrorIconOnClickListener(oclMachineID);
        binding.purchaseDateLayout.setErrorIconOnClickListener(oclDate);
        binding.serialNumberLayout.setErrorIconOnClickListener(oclSerialNumber);
        vDatePicker.addOnPositiveButtonClickListener(pclDatePicker);
        binding.addButton.setOnClickListener(oclAddSupplement);

        binding.idEdit.addTextChangedListener(twId);
        binding.coverImageView.setOnClickListener(oclMachineImg);
        binding.serialNumberEdit.addTextChangedListener(twSerialNumber);
        binding.purchaseDateEdit.addTextChangedListener(twPurchaseDate);
        binding.dayEdit.addTextChangedListener(twByDay);
        binding.weekEdit.addTextChangedListener(twByWeek);
        binding.monthEdit.addTextChangedListener(twByMonth);
    }

    // Functions

    private void initialize() {
        vDatePickerBuilder.setTitleText("Purchase Date");
        vDatePicker = vDatePickerBuilder.build();
        views = new ArrayList<>();
        views.add(new Pair<>(binding.idLayout, binding.idEdit));
        views.add(new Pair<>(binding.serialNumberLayout, binding.serialNumberEdit));
        views.add(new Pair<>(binding.purchaseDateLayout, binding.purchaseDateEdit));
        views.add(new Pair<>(binding.dayLayout, binding.dayEdit));
        views.add(new Pair<>(binding.weekLayout, binding.weekEdit));
        views.add(new Pair<>(binding.monthLayout, binding.monthEdit));
        activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                if (result.getResultCode() == RESULT_OK) {
                    //Bundle bundle = result.getData().getExtras();
                    Bitmap bitmap = BitmapFactory.decodeFile(currentPhotoPath);
                    binding.coverImageView.setImageBitmap(bitmap);
                    binding.coverImageView.setBorderWidth(2);
                    machineCover = new Supplement();
                    machineCover.setName("cover");
                    machineCover.setPhoto(bitmap);
                }
            }
        });
    }

    private void clearInput() {
        binding.registerButton.setEnabled(true);
        binding.idEdit.setText(null);
        binding.serialNumberEdit.setText(null);
        binding.purchaseDateEdit.setText(null);
        binding.idImageView.setImageResource(R.drawable.ic_baseline_image_24);
        binding.dayEdit.setText(null);
        binding.monthEdit.setText(null);
        binding.weekEdit.setText(null);
        machineCover = null;
        binding.coverImageView.setImageResource(R.drawable.ic_baseline_image_200);
        binding.coverImageView.setBorderWidth(0);
        supplements.clear();
        vDatePickerBuilder = MaterialDatePicker.Builder.datePicker();
        vDatePicker = vDatePickerBuilder.build();
        vDatePicker.addOnPositiveButtonClickListener(pclDatePicker);
    }

    private boolean generateError() {
        for (Pair<TextInputLayout, TextInputEditText> view : views) {
            if (view.second.getText().toString().trim().isEmpty()) {
                if (view.first == binding.idLayout)
                    view.first.setErrorIconDrawable(R.drawable.ic_baseline_autorenew_24);
                else if (view.first == binding.serialNumberLayout)
                    view.first.setErrorIconDrawable(R.drawable.ic_barcode);
                else if (view.first == binding.purchaseDateLayout)
                    view.first.setErrorIconDrawable(R.drawable.ic_baseline_calendar_month_24);


                view.first.setError("Missing");
                return true;
            }
            if (view.first.getError() != null) {
                return true;
            }
        }
        if (machineCover == null) {
            Toast.makeText(getActivity(), "Machine Image Missing", Toast.LENGTH_SHORT).show();
            return true;
        }
        return false;
    }

    private boolean validateInput() {
        if (generateError())
            return false;
        boolean noSupplements = supplements.size() == 0;
        if (noSupplements)
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
                binding.registerButton.setEnabled(false);
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
                    supplements.get(i).saveToCloudStorage(storageRef, binding.idEdit.getText().toString()).addOnSuccessListener(taskSnapshot -> {
                        if (finalI[0] == size - 1) {
                            alertDialog.dismiss();
                        }

                    }).addOnFailureListener(e -> {
                        alertDialog.dismiss();
                        Toast.makeText(getActivity(), "Failed to upload, check your internet", Toast.LENGTH_SHORT).show();
                    });
                }
                saveToInternalStorage();
                Machine newMachine = new Machine(binding.idEdit.getText().toString(), binding.serialNumberEdit.getText().toString(), new Date(purchaseDate));
                newMachine.setDailyRentPrice(Double.parseDouble(binding.dayEdit.getText().toString()));
                newMachine.setWeeklyRentPrice(Double.parseDouble(binding.weekEdit.getText().toString()));
                newMachine.setMonthlyRentPrice(Double.parseDouble(binding.monthEdit.getText().toString()));
                newMachine.setSupplementsNames(new ArrayList<>());
                IntStream.range(0, supplements.size()).forEach(i -> newMachine.getSupplementsNames().add(supplements.get(i).getName()));
                machineCover.saveToCloudStorage(FirebaseStorage.getInstance().getReference(), binding.idEdit.getText().toString()).addOnSuccessListener(unused -> {
                    machineCol.document(binding.idEdit.getText().toString()).set(newMachine).addOnSuccessListener(unused1 -> {
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
            binding.idEdit.setText(machineCol.document().getId().substring(0, 5));
        }
    };
    private final View.OnClickListener oclAddSupplement = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            AddAccessoriesDialog addAccessoriesDialog = new AddAccessoriesDialog(supplements);
            addAccessoriesDialog.show(getParentFragmentManager(), "");
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

                Uri imageUri = FileProvider.getUriForFile(getActivity(), "com.igec.admin.fileprovider", imageFile);

                Intent takePicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                takePicture.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                activityResultLauncher.launch(takePicture);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    };
    private final MaterialPickerOnPositiveButtonClickListener pclDatePicker = new MaterialPickerOnPositiveButtonClickListener() {
        @Override
        public void onPositiveButtonClick(Object selection) {
            binding.purchaseDateEdit.setText(convertDateToString((long) selection));
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
            qrgEncoder = new QRGEncoder(binding.idEdit.getText().toString(), null, QRGContents.Type.TEXT, 25 * 25);
            try {
                binding.idImageView.setImageBitmap(qrgEncoder.encodeAsBitmap());
            } catch (WriterException e) {
                e.printStackTrace();
            }
            binding.idLayout.setError(null);
            binding.idLayout.setErrorEnabled(false);
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
            binding.serialNumberLayout.setError(null);
            binding.serialNumberLayout.setErrorEnabled(false);
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
            binding.purchaseDateLayout.setError(null);
            binding.purchaseDateLayout.setErrorEnabled(false);
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
            binding.dayLayout.setError(null);
            binding.dayLayout.setErrorEnabled(false);
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
            binding.weekLayout.setError(null);
            binding.weekLayout.setErrorEnabled(false);
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
            binding.monthLayout.setError(null);
            binding.monthLayout.setErrorEnabled(false);
        }
    };


}