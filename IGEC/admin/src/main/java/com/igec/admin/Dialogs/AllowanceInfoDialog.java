package com.igec.admin.Dialogs;

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
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.fragment.app.DialogFragment;

import com.igec.admin.R;
import com.igec.common.firebase.Allowance;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.Currency;
import java.util.Locale;

public class AllowanceInfoDialog extends DialogFragment {

    private TextInputEditText vAllowanceMount, vAllowanceName;
    private AutoCompleteTextView vAllowanceType, vAllowanceCurrency;
    private TextInputLayout vAllowanceNameLayout, vAllowanceMountLayout, vAllowanceTypeLayout, vAllowanceCurrencyLayout;
    private ArrayList<Pair<TextInputLayout, EditText>> views;
    private MaterialButton vDone;
    private int position;
    private Allowance allowance = null;
    private ArrayList<String> allowancesList = new ArrayList<>();
    private ArrayList<String> types = new ArrayList<>();
    ConstraintLayout constraintLayout;

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
    public void onResume() {
        super.onResume();
        allowancesList.clear();
        types.clear();
        types.add("Transportation");
        types.add("Accommodation");
        types.add("Site");
        types.add("Remote");
        types.add("Food");
        types.add("Other");
        allowancesList.addAll(types);
        if (allowance != null) {
            int index = types.indexOf(allowance.getName());
            // if -1 meaning its other else its a valid type
            if (index != -1) {
                vAllowanceType.setText(types.get(index));
            } else {
                vAllowanceNameLayout.setVisibility(View.VISIBLE);
                vAllowanceType.setText(types.get(types.size() - 1));
                vAllowanceName.setText(allowance.getName());
            }
            vAllowanceCurrency.setText(allowance.getCurrency());
            vAllowanceMount.setText(String.format("%.2f", Math.abs(allowance.getAmount())));
        }
        ArrayAdapter<String> allowancesAdapter = new ArrayAdapter<>(getActivity(), R.layout.item_dropdown, allowancesList);
        vAllowanceType.setAdapter(allowancesAdapter);
        ArrayList<String> currencies = new ArrayList<>();
        currencies.add("EGP");
        currencies.add("SAR");
        ArrayAdapter<String> currenciesAdapter = new ArrayAdapter<>(getActivity(), R.layout.item_dropdown, currencies);
        vAllowanceCurrency.setAdapter(currenciesAdapter);
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
        vAllowanceType.addTextChangedListener(twType);
        vAllowanceName.addTextChangedListener(twName);
        vAllowanceMount.addTextChangedListener(twMount);
        vAllowanceCurrency.addTextChangedListener(twCurrency);

    }

    private void initialize(View view) {
        constraintLayout = view.findViewById(R.id.parent_layout);
        vAllowanceMount = view.findViewById(R.id.TextInput_AllowanceMount);
        vAllowanceType = view.findViewById(R.id.TextInput_AllowanceType);
        vAllowanceName = view.findViewById(R.id.TextInput_AllowanceName);
        vAllowanceCurrency = view.findViewById(R.id.TextInput_AllowanceCurrency);
        vAllowanceMountLayout = view.findViewById(R.id.textInputLayout_AllowanceMount);
        vAllowanceTypeLayout = view.findViewById(R.id.textInputLayout_AllowanceType);
        vAllowanceNameLayout = view.findViewById(R.id.textInputLayout_AllowanceName);
        vAllowanceCurrencyLayout = view.findViewById(R.id.textInputLayout_AllowanceCurrency);
        vDone = view.findViewById(R.id.button_Done);
        views = new ArrayList<>();
        views.add(new Pair<>(vAllowanceNameLayout, vAllowanceType));
        views.add(new Pair<>(vAllowanceMountLayout, vAllowanceMount));
        views.add(new Pair<>(vAllowanceCurrencyLayout, vAllowanceCurrency));
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
        boolean otherNoName = vAllowanceName.getText().toString().trim().isEmpty() && vAllowanceType.getText().toString().equals("Other");
        vAllowanceNameLayout.setError(otherNoName ? "Missing" : null);
        return otherNoName;
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
            boolean isOther = vAllowanceType.getText().toString().equals("Other");
            allowance.setName(isOther ? vAllowanceName.getText().toString() : vAllowanceType.getText().toString().trim());
            allowance.setAmount(Double.parseDouble(vAllowanceMount.getText().toString()));
            allowance.setCurrency(vAllowanceCurrency.getText().toString());
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
    private TextWatcher twType = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void afterTextChanged(Editable editable) {
            boolean isOther = vAllowanceType.getText().toString().equals("Other");
            vAllowanceNameLayout.setVisibility(isOther ? View.VISIBLE : View.GONE);
            vAllowanceName.setText("");
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
    private TextWatcher twCurrency = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void afterTextChanged(Editable editable) {
            vAllowanceCurrencyLayout.setError(null);
            vAllowanceCurrencyLayout.setErrorEnabled(false);
        }
    };


}