package com.example.igecuser.Dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;
import android.util.Pair;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;


import com.example.igecuser.R;
import com.example.igecuser.fireBase.Allowance;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;

public class AllowanceInfoDialog extends DialogFragment {
    private final int PROJECT = 0;
    private final int NETSALARY = 1;
    private final int ALLOWANCE = 2;
    private final int BONUS = 3;
    private final int PENALTY = 4;
    private TextInputEditText vAllowanceName, vAllowanceMount, vAllowanceNote;
    private TextInputLayout vAllowanceNameLayout, vAllowanceMountLayout, vAllowanceNoteLayout;
    private ArrayList<Pair<TextInputLayout, TextInputEditText>> views;
    private MaterialCheckBox vPenalty;
    private MaterialButton vDone;
    private int position;
    private Allowance allowance = null;
    private boolean canGivePenalty, isProject;

    public AllowanceInfoDialog(int position, boolean canGivePenalty, boolean isProject) {
        this.position = position;
        this.canGivePenalty = canGivePenalty;
        this.isProject = isProject;
    }

    public AllowanceInfoDialog(int position, Allowance allowance, boolean canGivePenalty, boolean isProject) {
        this.position = position;
        this.allowance = allowance;
        this.canGivePenalty = canGivePenalty;
        this.isProject = isProject;
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
        vAllowanceName.addTextChangedListener(twAllowanceName);
        vAllowanceMount.addTextChangedListener(twAllowanceMount);
        vAllowanceNote.addTextChangedListener(twAllowanceNote);

    }

    private void initialize(View view) {
        vAllowanceMount = view.findViewById(R.id.TextInput_AllowanceMount);
        vAllowanceName = view.findViewById(R.id.TextInput_AllowanceName);
        vAllowanceNote = view.findViewById(R.id.TextInput_AllowanceNote);
        vAllowanceMountLayout = view.findViewById(R.id.textInputLayout_AllowanceMount);
        vAllowanceNameLayout = view.findViewById(R.id.textInputLayout_AllowanceName);
        vAllowanceNoteLayout = view.findViewById(R.id.textInputLayout_AllowanceNote);

        views = new ArrayList<>();
        views.add(new Pair<>(vAllowanceNameLayout, vAllowanceName));
        views.add(new Pair<>(vAllowanceMountLayout, vAllowanceMount));
        views.add(new Pair<>(vAllowanceNoteLayout, vAllowanceNote));

        vPenalty = view.findViewById(R.id.checkBox_Penalty);
        vDone = view.findViewById(R.id.button_Done);
        vPenalty.setVisibility(canGivePenalty ? View.VISIBLE : View.GONE);

        if (allowance != null) {
            vPenalty.setChecked(allowance.getAmount() < 0);
            vAllowanceName.setText(allowance.getName());
            vAllowanceMount.setText(String.valueOf(allowance.getAmount()));
            vAllowanceNote.setText(allowance.getNote());
        }
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
        return false;
    }

    private boolean validateInput() {
        return !generateError();
    }

    private View.OnClickListener oclDone = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (validateInput()) {
                Bundle result = new Bundle();
                Allowance allowance = new Allowance();
                allowance.setName(vAllowanceName.getText().toString());
                allowance.setAmount(vPenalty.isChecked() ? (-1) * Double.parseDouble(vAllowanceMount.getText().toString()) : Double.parseDouble(vAllowanceMount.getText().toString()));
                allowance.setNote(vAllowanceNote.getText().toString());
                if (isProject)
                    allowance.setType(PROJECT);
                else {
                    if (vPenalty.isChecked())
                        allowance.setType(PENALTY);
                    else
                        allowance.setType(BONUS);
                }

                result.putSerializable("allowance", allowance);
                result.putString("note", vAllowanceNote.getText().toString());
                result.putInt("position", position);
                if (position == -1)
                    getParentFragmentManager().setFragmentResult("addAllowance", result);
                else
                    getParentFragmentManager().setFragmentResult("editAllowance", result);

                dismiss();
            }
        }

    };
    private TextWatcher twAllowanceName = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void afterTextChanged(Editable editable) {
            if (!vAllowanceName.getText().toString().trim().isEmpty())
                vAllowanceNameLayout.setError(null);
        }
    };
    private TextWatcher twAllowanceMount = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void afterTextChanged(Editable editable) {

            if (!vAllowanceMount.getText().toString().trim().isEmpty()) {
                if (Double.parseDouble(vAllowanceMount.getText().toString().trim()) == 0)
                    vAllowanceMountLayout.setError("Invalid Value");
                else
                    vAllowanceMountLayout.setError(null);
            } else {
                vAllowanceMountLayout.setError(null);
            }
        }
    };
    private TextWatcher twAllowanceNote = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void afterTextChanged(Editable editable) {

            if (!vAllowanceNote.getText().toString().trim().isEmpty())
                vAllowanceNoteLayout.setError(null);
        }
    };
}