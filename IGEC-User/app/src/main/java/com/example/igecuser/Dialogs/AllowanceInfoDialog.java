package com.example.igecuser.Dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.text.InputType;
import android.text.method.TransformationMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;


import com.example.igecuser.R;
import com.example.igecuser.fireBase.Allowance;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.lang.reflect.Type;

public class AllowanceInfoDialog extends DialogFragment {

    private TextInputEditText vAllowanceName, vAllowanceMount, vAllowanceNote;
    private MaterialButton vDone;
    private int position;
    private Allowance allowance = null;
    private boolean canGivePenalty;

    public AllowanceInfoDialog(int position, boolean canGivePenalty) {
        this.position = position;
        this.canGivePenalty = canGivePenalty;
    }

    public AllowanceInfoDialog(int position, Allowance allowance, boolean canGivePenalty) {
        this.position = position;
        this.allowance = allowance;
        this.canGivePenalty = canGivePenalty;
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
        vAllowanceNote = view.findViewById(R.id.TextInput_AllowanceNote);
        vDone = view.findViewById(R.id.button_Done);
        if (allowance != null) {
            vAllowanceName.setText(allowance.getName());
            vAllowanceMount.setText(String.valueOf(allowance.getAmount()));
        }
    }

    private boolean validateInput() {
        return
                !(vAllowanceName.getText().toString().isEmpty() ||
                        vAllowanceMount.getText().toString().isEmpty() ||
                        (!canGivePenalty && Integer.parseInt(vAllowanceMount.getText().toString()) < 0) ||
                        vAllowanceNote.getText().toString().isEmpty());

    }

    private View.OnClickListener oclDone = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (!validateInput()) {
                if (!canGivePenalty)
                    Toast.makeText(getActivity(), "can't give penalty to whole project", Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(getActivity(), "please fill allowance data", Toast.LENGTH_SHORT).show();
                return;
            }
            Bundle result = new Bundle();
            Allowance allowance = new Allowance();
            allowance.setName(vAllowanceName.getText().toString());
            allowance.setAmount(Integer.parseInt(vAllowanceMount.getText().toString()));
            result.putSerializable("allowance", allowance);
            result.putString("note",vAllowanceNote.getText().toString());
            result.putInt("position", position);
            if (position == -1)
                getParentFragmentManager().setFragmentResult("addAllowance", result);
            else
                getParentFragmentManager().setFragmentResult("editAllowance", result);

            dismiss();
        }
    };

}