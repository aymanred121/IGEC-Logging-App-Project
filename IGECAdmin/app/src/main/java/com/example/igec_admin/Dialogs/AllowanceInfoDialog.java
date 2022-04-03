package com.example.igec_admin.Dialogs;

import static android.app.Activity.RESULT_OK;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.igec_admin.R;
import com.example.igec_admin.fireBase.Allowance;
import com.example.igec_admin.fireBase.Supplement;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class AllowanceInfoDialog extends DialogFragment {

    private TextInputEditText vAllowanceName, vAllowanceMount;
    private MaterialButton vDone;
    private int position;
    private Allowance allowance = null;

    public AllowanceInfoDialog(int position) {
        this.position = position;
    }
    public AllowanceInfoDialog(int position, Allowance allowance) {
        this.position = position;
        this.allowance = allowance;
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
        return inflater.inflate(R.layout.fragment_allowance_info_dialog, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initialize(view);
        vDone.setOnClickListener(oclDone);

    }

    private void initialize(View view) {
        vAllowanceMount = view.findViewById(R.id.TextInput_AllowanceMount);
        vAllowanceName = view.findViewById(R.id.TextInput_AllowanceName);
        vDone = view.findViewById(R.id.button_Done);
        if(allowance != null)
        {
            vAllowanceName.setText(allowance.getName());
            vAllowanceMount.setText(String.valueOf(allowance.getAmount()));
        }
    }

    private boolean validateInput() {
        return
                !(vAllowanceName.getText().toString().isEmpty() ||
                        vAllowanceMount.getText().toString().isEmpty());

    }

    private View.OnClickListener oclDone = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(!validateInput()){
                Toast.makeText(getActivity(), "please fill allowance data", Toast.LENGTH_SHORT).show();
                return;
            }
            Bundle result = new Bundle();
            Allowance allowance = new Allowance();
            allowance.setName(vAllowanceName.getText().toString());
            allowance.setAmount(Integer.parseInt(vAllowanceMount.getText().toString()));
            result.putSerializable("allowance", allowance);
            result.putInt("position", position);
            if (position == -1)
                getParentFragmentManager().setFragmentResult("addAllowance", result);
            else
                getParentFragmentManager().setFragmentResult("editAllowance", result);

            dismiss();
        }
    };

}