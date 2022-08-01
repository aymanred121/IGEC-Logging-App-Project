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
import android.util.Pair;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;


import com.example.igecuser.R;
import com.example.igecuser.fireBase.Allowance;
import com.example.igecuser.fireBase.Employee;
import com.example.igecuser.utilites.allowancesEnum;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicReference;

public class AllowanceInfoDialog extends DialogFragment {
    private TextInputEditText vAllowanceMount, vAllowanceNote;
    private TextInputLayout vAllowanceNameLayout, vAllowanceMountLayout, vAllowanceNoteLayout;
    private ArrayList<Pair<TextInputLayout, EditText>> views;
    private MaterialCheckBox vPenalty, vMode;
    private MaterialButton vDone;
    private AutoCompleteTextView vAllowanceName;
    private int position;
    private Allowance allowance = null;
    private ArrayList<String> allowancesList = new ArrayList<>();
    private boolean canGivePenalty, isProject;
    private String EmployeeID;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public AllowanceInfoDialog(int position, boolean canGivePenalty, boolean isProject, String id) {
        this.position = position;
        this.canGivePenalty = canGivePenalty;
        this.isProject = isProject;
        this.EmployeeID = id;
    }

    public AllowanceInfoDialog(int position, Allowance allowance, boolean canGivePenalty, boolean isProject, String id) {
        this.position = position;
        this.allowance = allowance;
        this.canGivePenalty = canGivePenalty;
        this.isProject = isProject;
        this.EmployeeID = id;
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
        vPenalty.setOnClickListener(oclPenalty);
        vMode.setOnClickListener(oclPerDay);
        vAllowanceName.addTextChangedListener(twAllowanceName);
        vAllowanceMount.addTextChangedListener(twAllowanceMount);
        vAllowanceNote.addTextChangedListener(twAllowanceNote);

    }

    private void initialize(View view) {
        vAllowanceMount = view.findViewById(R.id.TextInput_AllowanceMount);
        vAllowanceName = view.findViewById(R.id.TextInput_allowanceName);
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
        vMode = view.findViewById(R.id.checkBox_PerDay);
        vPenalty.setVisibility(canGivePenalty ? View.VISIBLE : View.GONE);
        vMode.setVisibility(canGivePenalty ? View.VISIBLE : View.GONE);
        if (allowance != null) {
            vPenalty.setChecked(allowance.getAmount() < 0);
            vMode.setText(vPenalty.isChecked() ? "Days(s)" : "Bonus");
            vAllowanceName.setText(allowance.getName());
            vAllowanceMount.setText(String.format("%.2f",Math.abs(allowance.getAmount())));
            vAllowanceNote.setText(allowance.getNote());
        }
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

    private void hideError(TextInputLayout textInputLayout) {
        textInputLayout.setErrorEnabled(textInputLayout.getError() != null);
    }


    private boolean validateInput() {
        return !generateError();
    }

    private View.OnClickListener oclPerDay = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (vPenalty.isChecked()) {
                vAllowanceMountLayout.setSuffixText(vMode.isChecked() ? "Days(s)" : "£");
            } else {
                vAllowanceMountLayout.setSuffixText("£");
            }
        }
    };
    private View.OnClickListener oclPenalty = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (vPenalty.isChecked()) {
                vMode.setChecked(false);
                vMode.setText("Days");
            } else {
                vMode.setChecked(false);
                vMode.setText("Bonus");
            }

        }
    };
    private final View.OnClickListener oclDone = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (validateInput()) {
                Bundle result = new Bundle();
                Allowance allowance = new Allowance();
                allowance.setName(vAllowanceName.getText().toString());
                AtomicReference<Double> amount = new AtomicReference<>(Double.parseDouble(vAllowanceMount.getText().toString()));
                if (isProject) {
                    allowance.setName(vAllowanceName.getText().toString());
                    allowance.setAmount(Double.parseDouble(vAllowanceMount.getText().toString()));
                    allowance.setNote(vAllowanceNote.getText().toString());

                    result.putSerializable("allowance", allowance);
                    result.putString("note", vAllowanceNote.getText().toString());
                    result.putInt("position", position);
                    if (position == -1)
                        getParentFragmentManager().setFragmentResult("addAllowance", result);
                    else
                        getParentFragmentManager().setFragmentResult("editAllowance", result);
                    dismiss();

                } else {
                    Employee[] temp = new Employee[1];
                    double[] baseSalary = new double[1];
                    db.collection("employees").document(EmployeeID).get().addOnSuccessListener(value -> {
                        if (!value.exists()) return;
                        temp[0] = value.toObject(Employee.class);
                        baseSalary[0] = temp[0].getSalary();
                        if (vMode.isChecked() && vPenalty.isChecked())
                            amount.updateAndGet(v1 -> new Double((double) (v1 * (baseSalary[0] / 30))));
                        allowance.setNote(vAllowanceNote.getText().toString());
                        if (vPenalty.isChecked()) {
                            allowance.setAmount(-1 * amount.get());
                            allowance.setType(allowancesEnum.RETENTION.ordinal());
                        } else {
                            allowance.setAmount(amount.get());
                            allowance.setType(vMode.isChecked()?allowancesEnum.BONUS.ordinal(): allowancesEnum.valueOf(vAllowanceName.getText().toString().toUpperCase(Locale.ROOT)).ordinal());

                        }

                        result.putSerializable("allowance", allowance);
                        result.putString("note", vAllowanceNote.getText().toString());
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
            hideError(vAllowanceNameLayout);

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
                if (vAllowanceMount.getText().toString().equals(".") || Double.parseDouble(vAllowanceMount.getText().toString().trim()) == 0)
                    vAllowanceMountLayout.setError("Invalid Value");
                else
                    vAllowanceMountLayout.setError(null);
            } else {
                vAllowanceMountLayout.setError(null);
            }
            hideError(vAllowanceMountLayout);
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
            hideError(vAllowanceNoteLayout);
        }
    };

    public String getEmployeeID() {
        return EmployeeID;
    }

    public void setEmployeeID(String employeeID) {
        EmployeeID = employeeID;
    }
}