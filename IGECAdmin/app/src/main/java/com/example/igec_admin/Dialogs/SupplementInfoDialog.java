package com.example.igec_admin.Dialogs;

import static android.app.Activity.RESULT_OK;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
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
import androidx.fragment.app.DialogFragment;

import com.example.igec_admin.R;
import com.example.igec_admin.fireBase.Supplement;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;

public class SupplementInfoDialog extends DialogFragment {

    private ImageView vSupplementImg;
    private TextInputEditText vSupplementName;
    private MaterialButton vDone;
    private final Supplement supplement;
    private ActivityResultLauncher<Intent> activityResultLauncher;
    private final int position;
    public static ArrayList<String> oldName = new ArrayList<>();
    private final ArrayList<Supplement> supplementNames;

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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_supplement_info_dialog, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initialize(view);
        vSupplementName.addTextChangedListener(twNames);
        vDone.setOnClickListener(oclDone);
        vSupplementImg.setOnClickListener(oclImg);
        activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Bundle bundle = result.getData().getExtras();
                    Bitmap bitmap = (Bitmap) bundle.get("data");
                    vSupplementImg.setImageBitmap(bitmap);
                }
            }
        });

    }

    private void initialize(View view) {
        vSupplementImg = view.findViewById(R.id.ImageView_Supplement);
        vSupplementName = view.findViewById(R.id.TextInput_SupplementName);
        vDone = view.findViewById(R.id.button_Done);
        vSupplementImg.setImageBitmap(supplement.getPhoto());
        vSupplementName.setText(supplement.getName());
    }

    private final View.OnClickListener oclDone = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (supplementNames != null) {
                for (int i = 0; i < supplementNames.size(); i++) {
                    if (supplementNames.get(i).getName().equals(vSupplementName.getText().toString())) {
                        Toast.makeText(getContext(), "name is taken , please try another name", Toast.LENGTH_SHORT).show();
                        oldName.remove(oldName.size() - 1);
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
    TextWatcher twNames = new TextWatcher() {


        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            oldName.add(s.toString());
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };

    private final View.OnClickListener oclImg = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent takePicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (takePicture.resolveActivity(getContext().getPackageManager()) != null) {
                activityResultLauncher.launch(takePicture);
            } else {
                Toast.makeText(getActivity(), "there's no activity that supports that action", Toast.LENGTH_SHORT).show();
            }
        }
    };

}