package com.igec.admin.Dialogs;

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
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.DialogFragment;

import com.igec.admin.R;
import com.igec.admin.fireBase.Supplement;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class SupplementInfoDialog extends DialogFragment {

    private ImageView vSupplementImg;
    private TextInputEditText vSupplementName;
    private TextInputLayout vSupplementNameLayout;
    private MaterialButton vDone;
    private final Supplement supplement;
    private ActivityResultLauncher<Intent> activityResultLauncher;
    private final int position;
    private final ArrayList<Supplement> supplementNames;
    private String currentPhotoPath;

    public SupplementInfoDialog(int position, Supplement supplement, ArrayList<Supplement> supplementNames) {
        this.supplement = supplement;
        this.position = position;
        this.supplementNames = supplementNames;
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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.dialog_supplement_info, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initialize(view);
        vDone.setOnClickListener(oclDone);
        vSupplementImg.setOnClickListener(oclImg);
        activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                if (result.getResultCode() == RESULT_OK) {
                    //Bundle bundle = result.getData().getExtras();
                    Bitmap bitmap = BitmapFactory.decodeFile(currentPhotoPath);
                    vSupplementImg.setImageBitmap(bitmap);
                }
            }
        });
        vSupplementName.addTextChangedListener(twName);
    }

    private void initialize(View view) {
        vSupplementImg = view.findViewById(R.id.ImageView_Supplement);
        vSupplementName = view.findViewById(R.id.TextInput_SupplementName);
        vSupplementNameLayout = view.findViewById(R.id.textInputLayout_SupplementName);
        vDone = view.findViewById(R.id.button_Done);
        vSupplementImg.setImageBitmap(supplement.getPhoto());
        vSupplementName.setText(supplement.getName());
    }

    private boolean validateInput() {
        boolean isEmpty = vSupplementName.getText().toString().trim().isEmpty();
        vSupplementNameLayout.setError(isEmpty ? "Missing" : null);
        return !isEmpty;
    }

    private final View.OnClickListener oclDone = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (!validateInput()) return;
            vDone.setEnabled(false);
            if (supplementNames != null) {
                for (int i = 0; i < supplementNames.size(); i++) {
                    if (position != i && supplementNames.get(i).getName().equals(vSupplementName.getText().toString())) {
                        Toast.makeText(getContext(), "name is taken , please try another name", Toast.LENGTH_SHORT).show();
                        vDone.setEnabled(true);
                        return;
                    }
                }
            }
            Bundle result = new Bundle();
            Supplement supplement = new Supplement();
            vSupplementImg.setDrawingCacheEnabled(true);
            vSupplementImg.buildDrawingCache();
            supplement.setName(vSupplementName.getText().toString());
            supplement.setPhoto(((BitmapDrawable) vSupplementImg.getDrawable()).getBitmap());
            result.putSerializable("supplement", supplement);
            result.putInt("position", position);
            if (position == -1)
                getParentFragmentManager().setFragmentResult("addSupplement", result);
            else
                getParentFragmentManager().setFragmentResult("editSupplement", result);

            dismiss();
        }
    };
    private final View.OnClickListener oclImg = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

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
    private TextWatcher twName = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void afterTextChanged(Editable editable) {
            vSupplementNameLayout.setError(null);
            vSupplementNameLayout.setErrorEnabled(false);
        }
    };

}