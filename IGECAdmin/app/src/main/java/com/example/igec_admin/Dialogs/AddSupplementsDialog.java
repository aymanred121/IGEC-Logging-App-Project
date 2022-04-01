package com.example.igec_admin.Dialogs;

import static android.app.Activity.RESULT_OK;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Icon;
import android.os.Bundle;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.example.igec_admin.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class AddSupplementsDialog extends DialogFragment {

    private FloatingActionButton vAddPhoto, vDone;
    private ActivityResultLauncher<Intent> activityResultLauncher;

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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_add_supplements_dialog, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initialize(view);
        vAddPhoto.setOnClickListener(oclAddPhoto);
    }

    private void initialize(View view) {
        vAddPhoto = view.findViewById(R.id.Button_AddPhoto);
        vDone = view.findViewById(R.id.Button_Done);
        activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Bundle bundle = result.getData().getExtras();
                    Bitmap bitmap = (Bitmap) bundle.get("data");
                    SupplementInfoDialog supplementInfoDialog = new SupplementInfoDialog(bitmap);
                    supplementInfoDialog.show(getParentFragmentManager(),"");
                }
            }
        });
    }

    private View.OnClickListener oclAddPhoto = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent takePicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if(takePicture.resolveActivity(getContext().getPackageManager())!= null) {
                activityResultLauncher.launch(takePicture);
            }
            else
            {
                Toast.makeText(getActivity(), "there's no activity that supports that action", Toast.LENGTH_SHORT).show();
            }

        }
    };
}