package com.example.igec_admin.Dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.igec_admin.R;
import com.example.igec_admin.fireBase.Allowance;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;

public class AllowanceInfoDialog extends DialogFragment {

    private TextInputEditText vAllowanceMount;
    private AutoCompleteTextView vAllowanceName;
    private TextInputLayout vAllowanceNameLayout, vAllowanceMountLayout;
    private ArrayList<Pair<TextInputLayout, EditText>> views;
    private MaterialButton vDone;
    private int position;
    private Allowance allowance = null;
    private ArrayList<String> allowancesList = new ArrayList<>();
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
        return inflater.inflate(R.layout.dialog_allowance_info, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initialize(view);
        vDone.setOnClickListener(oclDone);
        //vAllowanceName.addTextChangedListener(twName);
        vAllowanceMount.addTextChangedListener(twMount);

    }

    private void initialize(View view) {
        vAllowanceMount = view.findViewById(R.id.TextInput_AllowanceMount);
        vAllowanceName = view.findViewById(R.id.TextInput_allowanceName);
        vAllowanceMountLayout = view.findViewById(R.id.textInputLayout_AllowanceMount);
        vAllowanceNameLayout = view.findViewById(R.id.textInputLayout_AllowanceName);
        vDone = view.findViewById(R.id.button_Done);

        views = new ArrayList<>();
        views.add(new Pair<>(vAllowanceNameLayout, vAllowanceName));
        views.add(new Pair<>(vAllowanceMountLayout, vAllowanceMount));
        if (allowance != null) {
            vAllowanceName.setText(allowance.getName());
            vAllowanceMount.setText(String.valueOf(allowance.getAmount()));
        }
        vAllowanceName.addTextChangedListener(twName);
        allowancesList.clear();
        allowancesList.add("Transportation");
        allowancesList.add("accommodation");
        allowancesList.add("site");
        allowancesList.add("remote");
        allowancesList.add("food");
        allowancesList.add("Other");
        ArrayAdapter<String> allowancesAdapter = new ArrayAdapter<>(getActivity(), R.layout.item_dropdown, allowancesList);
        vAllowanceName.setAdapter(allowancesAdapter);
    }

    private boolean generateError() {
        for (Pair<TextInputLayout, EditText> view : views) {
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
            if (!validateInput()) return;
            vDone.setEnabled(false);
            Bundle result = new Bundle();
            Allowance allowance = new Allowance();
            allowance.setName(vAllowanceName.getText().toString().trim());
            allowance.setAmount(Double.parseDouble(vAllowanceMount.getText().toString()));
            result.putSerializable("allowance", allowance);
            result.putInt("position", position);
            if (position == -1)
                getParentFragmentManager().setFragmentResult("addAllowance", result);
            else
                getParentFragmentManager().setFragmentResult("editAllowance", result);
            vDone.setEnabled(true);
            dismiss();
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
            vAllowanceNameLayout.setError(null);
            vAllowanceNameLayout.setErrorEnabled(false);
        }
    };
    private TextWatcher twMount = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void afterTextChanged(Editable editable) {
            if (!vAllowanceMount.getText().toString().trim().isEmpty()) {
                if (vAllowanceMount.getText().toString().equals(".") || Double.parseDouble(vAllowanceMount.getText().toString().trim()) == 0)
                    vAllowanceMountLayout.setError("Invalid Value");
                else
                    vAllowanceMountLayout.setError(null);
            } else {
                vAllowanceMountLayout.setError(null);
            }
            vAllowanceMountLayout.setErrorEnabled(vAllowanceMountLayout.getError() != null);
        }
    };

}