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
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class AllowanceInfoDialog extends DialogFragment {
    private TextInputEditText vAllowanceMount, vAllowanceName, vAllowanceNote;
    private AutoCompleteTextView vAllowanceType;
    private TextInputLayout vAllowanceNameLayout, vAllowanceMountLayout, vAllowanceTypeLayout, vAllowanceNoteLayout;
    private ArrayList<Pair<TextInputLayout, EditText>> views;
    private MaterialButton vDone;
    private int position;
    private Allowance allowance = null;
    private ArrayList<String> types = new ArrayList<>();
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
        vAllowanceType.addTextChangedListener(twType);
        vAllowanceName.addTextChangedListener(twName);
        vAllowanceMount.addTextChangedListener(twMount);
        vAllowanceNote.addTextChangedListener(twNote);

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
                vAllowanceType.setText(types.get(index));
            } else {
                vAllowanceNameLayout.setVisibility(View.VISIBLE);
                vAllowanceType.setText(types.get(types.size() - 1));
                vAllowanceName.setText(allowance.getName());
            }

            vAllowanceMount.setText(String.format("%.2f", Math.abs(allowance.getAmount())));
            vAllowanceNote.setText(allowance.getNote());
        }
        ArrayAdapter<String> allowancesAdapter = new ArrayAdapter<>(getActivity(), R.layout.item_dropdown, allowancesList);
        vAllowanceType.setAdapter(allowancesAdapter);
    }

    private void initialize(View view) {
        vAllowanceMount = view.findViewById(R.id.TextInput_AllowanceMount);
        vAllowanceName = view.findViewById(R.id.TextInput_AllowanceName);
        vAllowanceNote = view.findViewById(R.id.TextInput_AllowanceNote);
        vAllowanceType = view.findViewById(R.id.TextInput_AllowanceType);
        vAllowanceMountLayout = view.findViewById(R.id.textInputLayout_AllowanceMount);
        vAllowanceNameLayout = view.findViewById(R.id.textInputLayout_AllowanceName);
        vAllowanceNoteLayout = view.findViewById(R.id.textInputLayout_AllowanceNote);
        vAllowanceTypeLayout = view.findViewById(R.id.textInputLayout_AllowanceType);
        views = new ArrayList<>();
        views.add(new Pair<>(vAllowanceTypeLayout, vAllowanceType));
        views.add(new Pair<>(vAllowanceMountLayout, vAllowanceMount));
        views.add(new Pair<>(vAllowanceNoteLayout, vAllowanceNote));
        vDone = view.findViewById(R.id.button_Done);
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

    private void hideError(TextInputLayout textInputLayout) {
        textInputLayout.setErrorEnabled(textInputLayout.getError() != null);
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
                boolean isOther = vAllowanceType.getText().toString().equals("Other");
                allowance.setName(isOther ? vAllowanceName.getText().toString() : vAllowanceType.getText().toString().trim());
                // TODO override amount value if it was days or retention with a negative value
                allowance.setAmount(Double.parseDouble(vAllowanceMount.getText().toString()));
                allowance.setNote(vAllowanceNote.getText().toString());

                //TODO two properties to be set next Type and projectID
                // allowance.setProjectId();
                // allowance.setType();
                if (isProject) {
                    result.putSerializable("allowance", allowance);
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
                        //TODO use type instead of modes
//                        if (vMode.isChecked() && vPenalty.isChecked())
//                            amount.updateAndGet(v1 -> new Double((double) (v1 * (baseSalary[0] / 30))));
//                        allowance.setNote(vAllowanceNote.getText().toString());
//                        if (vPenalty.isChecked()) {
//                            allowance.setAmount(-1 * amount.get());
//                            allowance.setType(allowancesEnum.RETENTION.ordinal());
//                        } else {
//                            allowance.setAmount(amount.get());
//                            allowance.setType(vMode.isChecked()?allowancesEnum.BONUS.ordinal(): allowancesEnum.valueOf(vAllowanceName.getText().toString().toUpperCase(Locale.ROOT)).ordinal());
//
//                        }

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
            if (!vAllowanceName.getText().toString().trim().isEmpty())
                vAllowanceNameLayout.setError(null);
            hideError(vAllowanceNameLayout);

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
            hideError(vAllowanceMountLayout);
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

            if (!vAllowanceNote.getText().toString().trim().isEmpty())
                vAllowanceNoteLayout.setError(null);
            hideError(vAllowanceNoteLayout);
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
            boolean isRetentionByDays = vAllowanceType.getText().toString().equals("Retention by Days");
            vAllowanceMountLayout.setSuffixText(isRetentionByDays ? "Day(s)" : "£");
            vAllowanceNameLayout.setVisibility(isOther ? View.VISIBLE : View.GONE);
            vAllowanceName.setText("");
//            ConstraintSet constraintSet = new ConstraintSet();
//            constraintSet.clone(constraintLayout);
//            constraintSet.connect(
//                    R.id.textInputLayout_AllowanceMount,
//                    ConstraintSet.TOP,
//                    isOther ? R.id.textInputLayout_AllowanceName : R.id.textInputLayout_AllowanceType,
//                    ConstraintSet.BOTTOM, 32);
        }
    };
}