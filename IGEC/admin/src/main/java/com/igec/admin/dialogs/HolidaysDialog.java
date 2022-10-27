package com.igec.admin.dialogs;

import static com.igec.common.CONSTANTS.HOLIDAYS;
import static com.igec.common.CONSTANTS.HOLIDAYS_COL;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.firebase.firestore.ListenerRegistration;
import com.igec.admin.R;
import com.igec.admin.adapters.HolidayAdapter;
import com.igec.admin.databinding.HolidaysListBinding;
import com.igec.common.firebase.Holiday;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Objects;

public class HolidaysDialog extends DialogFragment {

    private ListenerRegistration task;

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

    private HolidaysListBinding binding;
    private MaterialDatePicker vDatePicker;
    private ArrayList<Holiday> holidays;
    private String selectedYear;
    private HolidayAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = HolidaysListBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        if (task != null) task.remove();

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        holidays = new ArrayList<>();
        adapter = new HolidayAdapter(holidays);
        vDatePicker = MaterialDatePicker.Builder.datePicker().build();
        vDatePicker.addOnPositiveButtonClickListener(selection -> {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(new Date((long) selection));
            selectedYear = String.valueOf(calendar.get(Calendar.YEAR));
            binding.yearEdit.setText(selectedYear);
            adapter.notifyDataSetChanged();
            task = HOLIDAYS_COL.document(HOLIDAYS).addSnapshotListener((documentSnapshot, e) -> {
                holidays.clear();
                if (!documentSnapshot.exists())
                    return;
                if (documentSnapshot.contains(selectedYear)) {
                    if (documentSnapshot.getData().get(selectedYear) == null)
                        return;
                    // loop over hashmap
                    for (Object o : ((ArrayList<Object>) documentSnapshot.getData().get(selectedYear))) {
                        holidays.add(new Holiday((HashMap) o));
                    }
                    binding.holidaysRecycler.setAdapter(adapter);
                    adapter.notifyDataSetChanged();
                }
            });
        });
        binding.yearLayout.setEndIconOnClickListener(v -> vDatePicker.show(getChildFragmentManager(), "date_picker"));
        adapter.setOnItemClickListener(position -> {
            HolidayInfoDialog dialog = HolidayInfoDialog.newInstance(holidays.get(position));
            dialog.show(getChildFragmentManager(), "holiday_info");
        });
    }
}
