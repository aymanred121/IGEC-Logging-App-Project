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
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.igec.admin.R;
import com.igec.admin.databinding.DialogAllowanceInfoBinding;
import com.igec.common.firebase.Allowance;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;

public class AllowanceInfoDialog extends DialogFragment {
    private ArrayList<Pair<TextInputLayout, EditText>> views;
    private int position;
    private Allowance allowance = null;
    private ArrayList<String> allowancesList = new ArrayList<>();
    private ArrayList<String> types = new ArrayList<>();

    // edit
    public AllowanceInfoDialog(int position) {
        this.position = position;
    }

    // add
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
                binding.typeAuto.setText(types.get(index));
            } else {
                binding.nameLayout.setVisibility(View.VISIBLE);
                binding.typeAuto.setText(types.get(types.size() - 1));
                binding.nameEdit.setText(allowance.getName());
            }
            binding.currencyAuto.setText(allowance.getCurrency());
            binding.mountEdit.setText(String.format("%.2f", Math.abs(allowance.getAmount())));
        }
        ArrayAdapter<String> allowancesAdapter = new ArrayAdapter<>(getActivity(), R.layout.item_dropdown, allowancesList);
        binding.typeAuto.setAdapter(allowancesAdapter);
        ArrayList<String> currencies = new ArrayList<>();
        currencies.add("EGP");
        currencies.add("SAR");
        ArrayAdapter<String> currenciesAdapter = new ArrayAdapter<>(getActivity(), R.layout.item_dropdown, currencies);
        binding.currencyAuto.setAdapter(currenciesAdapter);
    }

    private DialogAllowanceInfoBinding binding;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = DialogAllowanceInfoBinding.inflate(inflater,container,false);
        return binding.getRoot();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        binding = null;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initialize();
        binding.doneButton.setOnClickListener(oclDone);
        binding.typeAuto.addTextChangedListener(twType);
        binding.nameEdit.addTextChangedListener(twName);
        binding.mountEdit.addTextChangedListener(twMount);
        binding.currencyAuto.addTextChangedListener(twCurrency);

    }

    private void initialize() {
        views = new ArrayList<>();
        views.add(new Pair<>(binding.nameLayout, binding.typeAuto));
        views.add(new Pair<>(binding.mountLayout, binding.mountEdit));
        views.add(new Pair<>(binding.currencyLayout, binding.currencyAuto));
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
        boolean otherNoName = binding.nameEdit.getText().toString().trim().isEmpty() && binding.typeAuto.getText().toString().equals("Other");
        binding.nameLayout.setError(otherNoName ? "Missing" : null);
        return otherNoName;
    }

    private boolean validateInput() {
        return !generateError();
    }

    private View.OnClickListener oclDone = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (!validateInput()) return;
            binding.doneButton.setEnabled(false);
            Bundle result = new Bundle();
            Allowance allowance = new Allowance();
            boolean isOther = binding.typeAuto.getText().toString().equals("Other");
            allowance.setName(isOther ? binding.nameEdit.getText().toString() : binding.typeAuto.getText().toString().trim());
            allowance.setAmount(Double.parseDouble(binding.mountEdit.getText().toString()));
            allowance.setCurrency(binding.currencyAuto.getText().toString());
            result.putSerializable("allowance", allowance);
            result.putInt("position", position);
            if (position == -1)
                getParentFragmentManager().setFragmentResult("addAllowance", result);
            else
                getParentFragmentManager().setFragmentResult("editAllowance", result);
            binding.doneButton.setEnabled(true);
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
            boolean isOther = binding.typeAuto.getText().toString().equals("Other");
            binding.nameLayout.setVisibility(isOther ? View.VISIBLE : View.GONE);
            binding.nameEdit.setText("");
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
    private TextWatcher twMount = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void afterTextChanged(Editable editable) {
            if (!binding.mountEdit.getText().toString().trim().isEmpty()) {
                if (binding.mountEdit.getText().toString().equals(".") || Double.parseDouble(binding.mountEdit.getText().toString().trim()) == 0)
                    binding.mountLayout.setError("Invalid Value");
                else
                    binding.mountLayout.setError(null);
            } else {
                binding.mountLayout.setError(null);
            }
            binding.mountLayout.setErrorEnabled(binding.mountLayout.getError() != null);
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
            binding.currencyLayout.setError(null);
            binding.currencyLayout.setErrorEnabled(false);
        }
    };


}