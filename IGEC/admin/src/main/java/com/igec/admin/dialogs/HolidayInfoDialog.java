package com.igec.admin.dialogs;


import static com.igec.common.CONSTANTS.HOLIDAYS;
import static com.igec.common.CONSTANTS.HOLIDAYS_COL;

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
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.android.gms.tasks.Task;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.SetOptions;
import com.igec.admin.R;
import com.igec.admin.databinding.HolidayInfoDialogBinding;
import com.igec.common.firebase.Holiday;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

public class HolidayInfoDialog extends DialogFragment {

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

    public static HolidayInfoDialog newInstance(Holiday holiday) {

        Bundle args = new Bundle();
        args.putParcelable("holiday", holiday);
        HolidayInfoDialog fragment = new HolidayInfoDialog();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.FullscreenDialogTheme);
    }

    private MaterialDatePicker vStartDatePicker, vEndDatePicker;
    private HolidayInfoDialogBinding binding;
    private ArrayList<Pair<TextInputLayout, EditText>> views;
    private Date startDate, endDate;
    private Holiday holiday;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = HolidayInfoDialogBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        holiday = getArguments().getParcelable("holiday");

        binding.nameEdit.setText(holiday.getName());
        binding.startDateEdit.setText(holiday.convertDateToString(holiday.getStart()));
        binding.endDateEdit.setText(holiday.convertDateToString(holiday.getEnd()));
        startDate = holiday.getStart();
        endDate = holiday.getEnd();

        views = new ArrayList<>();
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
        binding.updateButton.setOnClickListener(v -> {
            if (!validate()) return;
            updateHoliday();
        });
        binding.deleteButton.setOnClickListener(v -> {
            removeHoliday().addOnSuccessListener(aVoid -> dismiss());
        });
    }

    private Task<Void> addHoliday(String startYear, Holiday holiday) {
        return HOLIDAYS_COL.document(HOLIDAYS).update(startYear, FieldValue.arrayUnion(holiday))
                .addOnFailureListener(e -> {
                    HOLIDAYS_COL.document(HOLIDAYS).set(new HashMap<String, Object>() {
                        {
                            put(startYear, new ArrayList<Holiday>() {
                                {
                                    add(holiday);
                                }
                            });
                        }
                    }, SetOptions.merge()).addOnSuccessListener(aVoid -> {
                    });
                });
    }

    private Task<Void> removeHoliday() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(this.holiday.getStart());
        String startYear = String.valueOf(calendar.get(Calendar.YEAR));
        return HOLIDAYS_COL.document(HOLIDAYS).update(startYear, FieldValue.arrayRemove(this.holiday));
    }

    private void updateHoliday() {
        Calendar yearCalendar = Calendar.getInstance();
        yearCalendar.setTime(this.holiday.getStart());
        String year = String.valueOf(yearCalendar.get(Calendar.YEAR));
        HOLIDAYS_COL.document(HOLIDAYS).update(year, FieldValue.arrayRemove(this.holiday))
                .addOnSuccessListener(aVoid -> {
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
                                endDate))
                                .addOnSuccessListener(aVoid1 -> dismiss());
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
                                endDate)).addOnSuccessListener(
                                aVoid1 -> {
                                    dismiss();
                                });
                    }
                });
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
