package com.igec.admin.fragments;

import static com.igec.common.CONSTANTS.HOLIDAYS;
import static com.igec.common.CONSTANTS.HOLIDAYS_COL;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.internal.TextWatcherAdapter;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.SetOptions;
import com.igec.admin.databinding.FragmentAccountantBinding;
import com.igec.admin.dialogs.HolidaysDialog;
import com.igec.common.firebase.Holiday;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;


public class AccountantFragment extends Fragment {
    public static AccountantFragment newInstance() {
        Bundle args = new Bundle();
        AccountantFragment fragment = new AccountantFragment();
        fragment.setArguments(args);
        return fragment;
    }

    private MaterialDatePicker vStartDatePicker, vEndDatePicker;
    private FragmentAccountantBinding binding;
    private ArrayList<Pair<TextInputLayout, EditText>> views;
    private Date startDate, endDate;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentAccountantBinding.inflate(inflater, container, false);
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

        vStartDatePicker = MaterialDatePicker.Builder.datePicker().build();
        vEndDatePicker = MaterialDatePicker.Builder.datePicker().build();
        views = new ArrayList<>();
        views.add(new Pair<>(binding.nameLayout, binding.nameEdit));
        views.add(new Pair<>(binding.startDateLayout, binding.startDateEdit));
        views.add(new Pair<>(binding.endDateLayout, binding.endDateEdit));
        for (Pair<TextInputLayout, EditText> pair : views) {
            pair.second.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void afterTextChanged(Editable editable) {
                    pair.first.setError(null);
                    pair.first.setErrorEnabled(false);
                }
            });
        }
        vStartDatePicker.addOnPositiveButtonClickListener(selection -> {
            binding.startDateEdit.setText(DateFormat.getDateInstance().format(selection));
            startDate = new Date((long) selection);
        });
        vEndDatePicker.addOnPositiveButtonClickListener(selection -> {
            binding.endDateEdit.setText(DateFormat.getDateInstance().format(selection));
            endDate = new Date((long) selection);
        });
        binding.startDateLayout.setEndIconOnClickListener(v -> vStartDatePicker.show(getChildFragmentManager(), "start_date_picker"));
        binding.endDateLayout.setEndIconOnClickListener(v -> vEndDatePicker.show(getChildFragmentManager(), "end_date_picker"));
        binding.startDateLayout.setErrorIconOnClickListener(v -> vStartDatePicker.show(getChildFragmentManager(), "start_date_picker"));
        binding.endDateLayout.setErrorIconOnClickListener(v -> vEndDatePicker.show(getChildFragmentManager(), "end_date_picker"));
        binding.registerButton.setOnClickListener(v -> {
            if (!validate()) return;
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(startDate);
            String startYear = String.valueOf(calendar.get(Calendar.YEAR));
            calendar.setTime(endDate);
            String endYear = String.valueOf(calendar.get(Calendar.YEAR));

            if (startYear.equals(endYear)) // don't split
            {
                addHoliday(startYear, new Holiday(
                        binding.nameEdit.getText().toString(),
                        startDate,
                        endDate));
            } else {

                String name = binding.nameEdit.getText().toString();
                Calendar newEndDate = Calendar.getInstance();
                Calendar newStartDate = Calendar.getInstance();


                newEndDate.setTime(startDate);
                newEndDate.set(Calendar.MONTH, Calendar.DECEMBER);
                newEndDate.set(Calendar.DAY_OF_MONTH, 31);

                newStartDate.setTime(endDate);
                newStartDate.set(Calendar.MONTH, Calendar.JANUARY);
                newStartDate.set(Calendar.DAY_OF_MONTH, 1);

                addHoliday(startYear, new Holiday(
                        name,
                        startDate,
                        newEndDate.getTime()));
                addHoliday(endYear, new Holiday(
                        name,
                        newStartDate.getTime(),
                        endDate));

            }


        });
        binding.viewHolidaysButton.setOnClickListener(v -> {
            HolidaysDialog dialog = new HolidaysDialog();
            dialog.show(getChildFragmentManager(), "holidays_dialog");
        });
    }

    private void addHoliday(String startYear, Holiday holiday) {
        HOLIDAYS_COL.document(HOLIDAYS).update(startYear, FieldValue.arrayUnion(holiday))
                .addOnSuccessListener(aVoid -> {
                    clearData();
                }).addOnFailureListener(e -> {
                    HOLIDAYS_COL.document(HOLIDAYS).set(new HashMap<String, Object>() {
                        {
                            put(startYear, new ArrayList<Holiday>() {
                                {
                                    add(holiday);
                                }
                            });
                        }
                    }, SetOptions.merge()).addOnSuccessListener(aVoid -> {
                        clearData();
                    });
                });
    }

    private void clearData() {
        binding.nameEdit.setText("");
        binding.startDateEdit.setText("");
        binding.endDateEdit.setText("");
    }


    boolean validate() {
        boolean valid = true;
        for (Pair<TextInputLayout, EditText> pair : views) {
            if (pair.second.getText().toString().trim().isEmpty()) {
                pair.first.setError("Required");
                valid = false;
            } else {
                pair.first.setError(null);
            }
        }
        return valid;
    }
}