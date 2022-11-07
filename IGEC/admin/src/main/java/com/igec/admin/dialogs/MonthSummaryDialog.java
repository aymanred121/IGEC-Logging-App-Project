package com.igec.admin.dialogs;

import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.igec.admin.adapters.WorkingDayAdapter;
import com.igec.admin.databinding.FragmentMonthSummaryBinding;
import com.igec.admin.fragments.SummaryFragment;
import com.igec.admin.R;
import com.igec.common.utilities.CsvWriter;
import com.igec.common.utilities.WorkingDay;

import java.io.IOException;
import java.util.ArrayList;
import java.util.StringJoiner;
import java.util.stream.IntStream;

public class MonthSummaryDialog extends DialogFragment {


    private WorkingDayAdapter adapter;
    private ArrayList<WorkingDay> workingDays;

    public MonthSummaryDialog(ArrayList<WorkingDay> workingDays) {
        this.workingDays = workingDays;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.FullscreenDialogTheme);
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

    private FragmentMonthSummaryBinding binding;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        binding = FragmentMonthSummaryBinding.inflate(inflater,container,false);
        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        int parent =getParentFragmentManager().getFragments().size()-1;
        ((SummaryFragment)getParentFragmentManager().getFragments().get(parent)).setOpened(false);
        binding = null;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initialize();
        adapter.setOnItemClickListener(oclWorkingDay);
    }

    void initialize() {
        binding.recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
        adapter = new WorkingDayAdapter(workingDays);
        binding.recyclerView.setLayoutManager(layoutManager);
        binding.recyclerView.setAdapter(adapter);

    }

    private final WorkingDayAdapter.OnItemClickListener oclWorkingDay = new WorkingDayAdapter.OnItemClickListener() {
        @Override
        public void onItemClick(int position) {
            // Create a Uri from an intent string. Use the result to create an Intent.
            WorkingDay current = workingDays.get(position);
            String location = String.format("geo:<%s>,<%s>?q=<%s>,<%s>(%s)", current.getCheckIn().getLat(), current.getCheckIn().getLng(), current.getCheckIn().getLat(), current.getCheckIn().getLng(), current.getProjectName() + "\n" + current.getProjectLocation());
            Uri gmmIntentUri = Uri.parse(location);

            // Create an Intent from gmmIntentUri. Set the action to ACTION_VIEW
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
            // Make the Intent explicit by setting the Google Maps package
            mapIntent.setPackage("com.google.android.apps.maps");

            // Attempt to start an activity that can handle the Intent
            startActivity(mapIntent);
        }
    };
}
