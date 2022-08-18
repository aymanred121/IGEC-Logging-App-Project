package com.igec.admin.dialogs;

import static android.app.Activity.RESULT_OK;

import static com.igec.common.CONSTANTS.MACHINE_COL;
import static com.igec.common.CONSTANTS.MACHINE_EMPLOYEE_COL;

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

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.StorageTask;
import com.igec.admin.databinding.FragmentAddMachineBinding;
import com.igec.admin.fragments.MachinesFragment;
import com.igec.admin.R;
import com.igec.common.firebase.Machine;
import com.igec.common.firebase.Accessory;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.DocumentSnapshot;
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
import java.util.stream.IntStream;

import androidmads.library.qrgenearator.QRGContents;
import androidmads.library.qrgenearator.QRGEncoder;

@RequiresApi(api = Build.VERSION_CODES.R)
public class MachineFragmentDialog extends DialogFragment {
    // Vars
    private String currentPhotoPath;
    private ActivityResultLauncher<Intent> activityResultLauncher;
    private long purchaseDate;
    private QRGEncoder qrgEncoder;
    private final MaterialDatePicker.Builder<Long> vDatePickerBuilder = MaterialDatePicker.Builder.datePicker();
    private MaterialDatePicker vDatePicker;
    private final Machine machine;
    private ArrayList<Accessory> accessories;
    private Accessory machineCover;
    private ArrayList<StorageTask<FileDownloadTask.TaskSnapshot>> downloadTasks = new ArrayList<>();
    private final FirebaseStorage storage = FirebaseStorage.getInstance();
    private final StorageReference storageRef = storage.getReference();
    private ArrayList<String> oldNames;
    private ArrayList<Pair<TextInputLayout, TextInputEditText>> views;
    private boolean loaded = false;

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
                accessories = bundle.getParcelableArrayList("supplements");
                oldNames = bundle.getStringArrayList("oldNames");
                loaded = bundle.getBoolean("loaded");
            }
        });
        getParentFragmentManager().setFragmentResultListener("machine", this, new FragmentResultListener() {
            @Override
            public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle result) {
                binding.serialNumberEdit.setText(result.getString("SerialNumber"));
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
        downloadTasks.forEach(StorageTask::cancel);
        int parent =getParentFragmentManager().getFragments().size()-1;
        ((MachinesFragment) getParentFragmentManager().getFragments().get(parent)).setOpened(false);
        binding = null;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initialize();
        binding.coverImageView.setOnClickListener(oclMachineImg);
        binding.updateButton.setOnClickListener(oclUpdate);
        binding.deleteButton.setOnClickListener(oclDelete);
        binding.purchaseDateLayout.setEndIconOnClickListener(oclDate);
        vDatePicker.addOnPositiveButtonClickListener(pclDatePicker);
        binding.serialNumberLayout.setEndIconOnClickListener(oclSerialNumber);
        binding.purchaseDateLayout.setErrorIconOnClickListener(oclDate);
        binding.serialNumberLayout.setErrorIconOnClickListener(oclSerialNumber);
        binding.addButton.setOnClickListener(oclAddSupplement);
        binding.serialNumberEdit.addTextChangedListener(twSerialNumber);
        binding.purchaseDateEdit.addTextChangedListener(twPurchaseDate);
        binding.dayEdit.addTextChangedListener(twByDay);
        binding.weekEdit.addTextChangedListener(twByWeek);
        binding.monthEdit.addTextChangedListener(twByMonth);
    }

    // Functions
    private void initialize() {
        oldNames = new ArrayList<>();
        binding.registerButton.setVisibility(View.GONE);
        binding.deleteButton.setVisibility(View.VISIBLE);
        binding.updateButton.setVisibility(View.VISIBLE);

        binding.idEdit.setEnabled(false);

        binding.serialNumberEdit.setText(machine.getReference());
        binding.idEdit.setText(machine.getId());
        purchaseDate = machine.getPurchaseDate().getTime();
        binding.purchaseDateEdit.setText(convertDateToString(machine.getPurchaseDate().getTime()));
        binding.dayEdit.setText(String.valueOf(machine.getDailyRentPrice()));
        binding.weekEdit.setText(String.valueOf(machine.getWeeklyRentPrice()));
        binding.monthEdit.setText(String.valueOf(machine.getMonthlyRentPrice()));
        getMachineCover();
        vDatePickerBuilder.setTitleText("Purchase Date");
        vDatePicker = vDatePickerBuilder.setSelection(purchaseDate).build();

        views = new ArrayList<>();
        views.add(new Pair<>(binding.serialNumberLayout, binding.serialNumberEdit));
        views.add(new Pair<>(binding.purchaseDateLayout, binding.purchaseDateEdit));
        views.add(new Pair<>(binding.dayLayout, binding.dayEdit));
        views.add(new Pair<>(binding.weekLayout, binding.weekEdit));
        views.add(new Pair<>(binding.monthLayout, binding.monthEdit));
        qrgEncoder = new QRGEncoder(binding.idEdit.getText().toString(), null, QRGContents.Type.TEXT, 25 * 25);
        try {
            binding.idImageView.setImageBitmap(qrgEncoder.encodeAsBitmap());
        } catch (WriterException e) {
            e.printStackTrace();
        }
        activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                if (result.getResultCode() == RESULT_OK) {
                    //Bundle bundle = result.getData().getExtras();
                    Bitmap bitmap = BitmapFactory.decodeFile(currentPhotoPath);
                    binding.coverImageView.setImageBitmap(bitmap);
                    binding.coverImageView.setBorderWidth(2);
                    machineCover.setPhoto(bitmap);
                    machineCover.setName("cover");
                }
            }
        });
    }

    private void getMachineCover() {
        String cover = "cover";
        StorageReference ref = FirebaseStorage.getInstance().getReference().child("/imgs/" + machine.getId() + String.format("/%s.jpg", cover));
        try {
            final File localFile = File.createTempFile(cover, "jpg");
            downloadTasks.add(ref.getFile(localFile).addOnSuccessListener(taskSnapshot -> {
                Bitmap bitmap = BitmapFactory.decodeFile(localFile.getAbsolutePath());
                machineCover = new Accessory();
                machineCover.setPhoto(bitmap);
                machineCover.setName("cover");
                binding.coverImageView.setImageBitmap(bitmap);
            }).addOnFailureListener(e -> {
                machineCover = new Accessory();
                machineCover.setName("bad_cover");
            }));
        } catch (IOException e) {
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
            storageRef.child("imgs/" + binding.idEdit.getText().toString() + "/" + name + ".jpg").delete().addOnSuccessListener(new OnSuccessListener<Void>() {
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
        storageRef.child("imgs/" + binding.idEdit.getText().toString() + "/" + "cover" + ".jpg").delete();
        MACHINE_COL.document(machine.getId()).delete().addOnSuccessListener(unused -> {
            Snackbar.make(binding.getRoot(), "Deleted", Snackbar.LENGTH_SHORT).show();
            binding.deleteButton.setEnabled(true);
            dismiss();
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.R)
    private void updateMachine() throws ParseException {

        if (accessories != null) {
            int size = accessories.size();
            MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(getActivity());
            builder.setTitle("Uploading...")
                    .setMessage("Uploading Data")
                    .setCancelable(false);
            AlertDialog alertDialog = builder.create();
            alertDialog.show();
            if (accessories.size() > 0) {
                machine.getSupplementsNames().clear();
                IntStream.range(0, accessories.size()).forEach(i -> machine.getSupplementsNames().add(accessories.get(i).getName()));
            }
            for (String name : oldNames) {
                storageRef.child("imgs/" + binding.idEdit.getText().toString() + "/" + name + ".jpg").delete().addOnSuccessListener(new OnSuccessListener<Void>() {
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
                accessories.get(i).saveToCloudStorage(storageRef, binding.idEdit.getText().toString()).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        if (finalI[0] == size - 1) {
                            alertDialog.dismiss();
                        }
                    }
                });
            }
        }
        machine.setReference(binding.serialNumberEdit.getText().toString());
        machine.setPurchaseDate(new SimpleDateFormat("dd/MM/yyyy").parse(binding.purchaseDateEdit.getText().toString()));
        machine.setDailyRentPrice(Double.parseDouble(binding.dayEdit.getText().toString()));
        machine.setWeeklyRentPrice(Double.parseDouble(binding.weekEdit.getText().toString()));
        machine.setMonthlyRentPrice(Double.parseDouble(binding.monthEdit.getText().toString()));

        if (machineCover != null)
            machineCover.saveToCloudStorage(storageRef, machine.getId()).addOnSuccessListener(uv -> {
                MACHINE_COL.document(machine.getId()).set(machine).addOnSuccessListener(unused -> {
                    MACHINE_EMPLOYEE_COL.whereEqualTo("machine.id", machine.getId()).get().addOnSuccessListener(queryDocumentSnapshots -> {
                        for (DocumentSnapshot d : queryDocumentSnapshots) {
                            MACHINE_EMPLOYEE_COL
                                    .document(d.getId())
                                    .update("machine", machine);
                        }
                    });
                    Snackbar snackbar = Snackbar.make(binding.getRoot(), "Updated", Snackbar.LENGTH_SHORT);
                    snackbar.show();
                    binding.updateButton.setEnabled(true);
                    dismiss();
                });
            });
        else
            MACHINE_COL.document(machine.getId()).set(machine).addOnSuccessListener(unused -> {
                MACHINE_EMPLOYEE_COL.whereEqualTo("machine.id", machine.getId()).get().addOnSuccessListener(queryDocumentSnapshots -> {
                    for (DocumentSnapshot d : queryDocumentSnapshots) {
                        MACHINE_EMPLOYEE_COL
                                .document(d.getId())
                                .update("machine", machine);
                    }
                });
                Snackbar snackbar = Snackbar.make(binding.getRoot(), "Updated", Snackbar.LENGTH_SHORT);
                snackbar.show();
                binding.updateButton.setEnabled(true);
                dismiss();
            });


    }


    private boolean generateError() {
        for (Pair<TextInputLayout, TextInputEditText> view : views) {
            if (view.second.getText().toString().trim().isEmpty()) {
                view.first.setError("Missing");
                return true;
            }
            if (view.first.getError() != null) {
                return true;
            }
        }
        if (machineCover == null) {
            Snackbar.make(binding.getRoot(), "Machine Image Missing", Snackbar.LENGTH_SHORT).show();
            return true;
        }
        return false;
    }

    private boolean validateInput() {
        if (generateError())
            return false;

        if (accessories != null) {
            boolean noSupplements = accessories.size() == 0;
            if (noSupplements) {
                Snackbar.make(binding.getRoot(), "Accessories Missing", Snackbar.LENGTH_SHORT).show();
                return false;
            }
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
            binding.updateButton.setEnabled(false);
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
            AddAccessoriesDialog addAccessoriesDialog;
            if (accessories == null || !loaded)
                addAccessoriesDialog = new AddAccessoriesDialog(machine);
            else
                addAccessoriesDialog = new AddAccessoriesDialog((ArrayList<Accessory>) accessories.clone(), loaded);
            addAccessoriesDialog.show(getParentFragmentManager(), "");
        }
    };
    private View.OnClickListener oclMachineImg = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            String fileName = "cover";
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
    private View.OnClickListener oclSerialNumber = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            MachineSerialNumberDialog machineSerialNumberDialog = new MachineSerialNumberDialog();
            machineSerialNumberDialog.show(getParentFragmentManager(), "");
        }
    };
    private final View.OnClickListener oclDelete = v -> {

        binding.deleteButton.setEnabled(false);
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(getActivity());
        builder.setTitle(getString(R.string.Delete))
                .setMessage(getString(R.string.AreUSure))
                .setNegativeButton(getString(R.string.cancel), (dialogInterface, i) -> {
                    binding.deleteButton.setEnabled(true);
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
    private final MaterialPickerOnPositiveButtonClickListener pclDatePicker = new MaterialPickerOnPositiveButtonClickListener() {
        @Override
        public void onPositiveButtonClick(Object selection) {
            binding.purchaseDateEdit.setText(convertDateToString((long) selection));
            purchaseDate = (long) selection;
        }
    };
}
