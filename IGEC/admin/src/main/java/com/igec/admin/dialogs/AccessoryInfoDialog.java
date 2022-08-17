package com.igec.admin.dialogs;

import static android.app.Activity.RESULT_OK;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.DialogFragment;

import com.igec.admin.R;
import com.igec.admin.databinding.DialogAccessoryInfoBinding;
import com.igec.common.firebase.Accessory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class AccessoryInfoDialog extends DialogFragment {
    private final Accessory accessory;
    private ActivityResultLauncher<Intent> activityResultLauncher;
    private int position;
    private final ArrayList<Accessory> accessoryNames;
    private String currentPhotoPath;

    public AccessoryInfoDialog(int position, Accessory accessory, ArrayList<Accessory> accessoryNames) {
        this.accessory = accessory;
        this.position = position;
        this.accessoryNames = accessoryNames;
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
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
    }

    private DialogAccessoryInfoBinding binding;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = DialogAccessoryInfoBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initialize();
        binding.doneButton.setOnClickListener(oclDone);
        binding.accessoryImageView.setOnClickListener(oclImg);
        activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK) {
                //Bundle bundle = result.getData().getExtras();
                Bitmap bitmap = BitmapFactory.decodeFile(currentPhotoPath);
                binding.accessoryImageView.setImageBitmap(bitmap);
            }
        });
        binding.nameEdit.addTextChangedListener(twName);
    }

    private void initialize() {
        binding.accessoryImageView.setImageBitmap(accessory.getPhoto());
        binding.nameEdit.setText(accessory.getName());
    }

    private boolean validateInput() {
        boolean isEmpty = binding.nameEdit.getText().toString().trim().isEmpty();
        if (isEmpty) {
            binding.nameLayout.setError("Missing");
            return false;
        }
        if (accessoryNames != null) {
            for (int i = 0; i < accessoryNames.size(); i++) {
                if (position != i && accessoryNames.get(i).getName().equals(binding.nameEdit.getText().toString().trim())) {
                    binding.nameLayout.setError("Name is Taken");
                    binding.doneButton.setEnabled(true);
                    return false;
                }
            }
        }
        return true;
    }

    private final View.OnClickListener oclDone = v -> {
        if (!validateInput()) return;
        binding.doneButton.setEnabled(false);
        Bundle result = new Bundle();
        Accessory accessory = new Accessory();
        binding.accessoryImageView.setDrawingCacheEnabled(true);
        binding.accessoryImageView.buildDrawingCache();
        accessory.setName(binding.nameEdit.getText().toString());
        accessory.setPhoto(((BitmapDrawable) binding.accessoryImageView.getDrawable()).getBitmap());
        result.putSerializable("supplement", accessory);
        result.putInt("position", position);
        if (position == -1)
            getParentFragmentManager().setFragmentResult("addSupplement", result);
        else
            getParentFragmentManager().setFragmentResult("editSupplement", result);

        dismiss();
    };
    private final View.OnClickListener oclImg = v -> {
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

    };
    private TextWatcher twName = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void afterTextChanged(Editable editable) {
            binding.nameLayout.setError(null);
            binding.nameLayout.setErrorEnabled(false);
        }
    };

}