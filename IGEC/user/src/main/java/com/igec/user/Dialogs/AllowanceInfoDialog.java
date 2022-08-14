package com.igec.user.Dialogs;

import static com.igec.common.CONSTANTS.EMPLOYEE_COL;

import android.app.Dialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.util.Pair;
import android.widget.ArrayAdapter;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;


import com.igec.user.R;
import com.igec.common.firebase.Allowance;
import com.igec.common.firebase.Employee;
import com.igec.common.utilities.allowancesEnum;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.FirebaseFirestore;
import com.igec.user.databinding.DialogAllowanceInfoBinding;

import java.util.ArrayList;
import java.util.Locale;

public class AllowanceInfoDialog extends DialogFragment {
    private ArrayList<Pair<TextInputLayout, EditText>> views;
    private int position;
    private Allowance allowance = null;
    private ArrayList<String> types = new ArrayList<>();
    private ArrayList<String> allowancesList = new ArrayList<>();
    private boolean canGivePenalty, isProject;
    private String EmployeeID;
    private double baseSalary;
    private String baseCurrency;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public AllowanceInfoDialog(int position, boolean canGivePenalty, boolean isProject, String id) {
        this.position = position;
        this.canGivePenalty = canGivePenalty;
        this.isProject = isProject;
        this.EmployeeID = id;
    }

    public AllowanceInfoDialog(int position, Allowance allowance, boolean canGivePenalty, boolean isProject, String id, double baseSalary, String baseCurrency) {
        this.position = position;
        this.allowance = allowance;
        this.canGivePenalty = canGivePenalty;
        this.isProject = isProject;
        this.EmployeeID = id;
        this.baseSalary = baseSalary;
        this.baseCurrency = baseCurrency;
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

    private DialogAllowanceInfoBinding binding;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = DialogAllowanceInfoBinding.inflate(inflater, container, false);
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
        binding.noteEdit.addTextChangedListener(twNote);
        binding.currencyAuto.addTextChangedListener(twCurrency);

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
        if (canGivePenalty) {
            types.add("Retention by Days");
            types.add("Retention by Amount");
            types.add("Bonus");
        }
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
            boolean isRetentionByDays = allowance.getName().equals("Retention by Days");
            binding.currencyAuto.setText(isRetentionByDays ? baseCurrency : allowance.getCurrency());
            binding.mountEdit.setText(isRetentionByDays ? String.format("%d", (int) (allowance.getAmount() * 30 / baseSalary)) : String.format("%.2f", Math.abs(allowance.getAmount())));
            binding.noteEdit.setText(allowance.getNote());
        }
        ArrayAdapter<String> allowancesAdapter = new ArrayAdapter<>(getActivity(), R.layout.item_dropdown, allowancesList);
        binding.typeAuto.setAdapter(allowancesAdapter);

        ArrayList<String> currencies = new ArrayList<>();
        currencies.add("EGP");
        currencies.add("SAR");
        ArrayAdapter<String> currenciesAdapter = new ArrayAdapter<>(getActivity(), R.layout.item_dropdown, currencies);
        binding.currencyAuto.setAdapter(currenciesAdapter);
    }

    private void initialize() {
        views = new ArrayList<>();
        views.add(new Pair<>(binding.typeLayout, binding.typeAuto));
        views.add(new Pair<>(binding.mountLayout, binding.mountEdit));
        views.add(new Pair<>(binding.currencyLayout, binding.currencyAuto));
        views.add(new Pair<>(binding.noteLayout, binding.noteEdit));
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

    private void hideError(TextInputLayout textInputLayout) {
        textInputLayout.setError(null);
        textInputLayout.setErrorEnabled(false);
    }


    private boolean validateInput() {
        return !generateError();
    }

    private final View.OnClickListener oclDone = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (validateInput()) {
                Bundle result = new Bundle();
                Allowance allowance = new Allowance();
                boolean isRetentionByDays = binding.typeAuto.getText().toString().equals("Retention by Days");
                boolean isOther = binding.typeAuto.getText().toString().equals("Other");
                allowance.setName(isOther ? binding.nameEdit.getText().toString() : binding.typeAuto.getText().toString().trim());
                allowance.setAmount(Double.parseDouble(binding.mountEdit.getText().toString()));
                allowance.setNote(binding.noteEdit.getText().toString());
                allowance.setCurrency(isRetentionByDays ? baseCurrency : binding.currencyAuto.getText().toString());
                if (isProject) {
                    result.putSerializable("allowance", allowance);
                    result.putInt("position", position);
                    if (position == -1)
                        getParentFragmentManager().setFragmentResult("addAllowance", result);
                    else
                        getParentFragmentManager().setFragmentResult("editAllowance", result);
                    dismiss();

                } else {
                    EMPLOYEE_COL.document(EmployeeID).get().addOnSuccessListener(value -> {
                        if (!value.exists()) return;
                        Employee employee = value.toObject(Employee.class);
                        if (binding.typeAuto.getText().toString().equals("Retention by Days") || binding.typeAuto.getText().toString().equals("Retention by Amount")) {
                            allowance.setType(allowancesEnum.RETENTION.ordinal());
                            if (binding.typeAuto.getText().toString().equals("Retention by Days")) {
                                allowance.setAmount(allowance.getAmount() * (employee.getSalary() / 30.0));
                            }
                        } else {
                            allowance.setType(allowancesEnum.valueOf(binding.typeAuto.getText().toString().toUpperCase(Locale.ROOT)).ordinal());
                        }
                        result.putSerializable("allowance", allowance);
                        result.putInt("position", position);
                        if (position == -1)
                            getParentFragmentManager().setFragmentResult("addAllowance", result);
                        else
                            getParentFragmentManager().setFragmentResult("editAllowance", result);

                        dismiss();
                    });
                }
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
            hideError(binding.nameLayout);
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
                else {
                    binding.mountLayout.setError(null);
                    binding.mountLayout.setErrorEnabled(false);
                }
            } else {
                binding.mountLayout.setError(null);
                binding.mountLayout.setErrorEnabled(false);
            }
        }
    };
    private TextWatcher twNote = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void afterTextChanged(Editable editable) {

            hideError(binding.noteLayout);
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
            boolean isRetentionByDays = binding.typeAuto.getText().toString().equals("Retention by Days");
            binding.mountLayout.setSuffixText(isRetentionByDays ? "Day(s)" : binding.currencyAuto.getText().toString());
            binding.nameLayout.setVisibility(isOther ? View.VISIBLE : View.GONE);
            binding.currencyLayout.setVisibility(isRetentionByDays ? View.GONE : View.VISIBLE);
            binding.nameEdit.setText("");
            hideError(binding.typeLayout);
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
            hideError(binding.currencyLayout);
        }
    };
}