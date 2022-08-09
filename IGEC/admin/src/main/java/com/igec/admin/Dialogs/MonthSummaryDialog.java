package com.igec.admin.Dialogs;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.igec.admin.Adatpers.WorkingDayAdapter;
import com.igec.admin.R;
import com.igec.common.utilities.CsvWriter;
import com.igec.common.utilities.WorkingDay;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.StringJoiner;
import java.util.stream.IntStream;

public class MonthSummaryDialog extends DialogFragment {


    private WorkingDayAdapter adapter;
    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private ArrayList<WorkingDay> workingDays;
    private FloatingActionButton createCSV;
    private final int MONTH31DAYS = 0xA55;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

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

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_month_summary, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initialize(view);
        adapter.setOnItemClickListener(oclWorkingDay);
        createCSV.setOnClickListener(oclCSV);
    }

    void initialize(View view) {
        createCSV = view.findViewById(R.id.fab_createCSV);
        recyclerView = view.findViewById(R.id.recyclerview);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(getActivity());
        adapter = new WorkingDayAdapter(workingDays);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);

    }

    private final View.OnClickListener oclCSV = v -> {
        @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
        String currentDateAndTime = sdf.format(new Date());
        String month = workingDays.get(0).getMonth();
        String year = workingDays.get(0).getYear();
        String empName = workingDays.get(0).getEmpName();
        int yearNumber = Integer.parseInt(year);
        int monthNumber = Integer.parseInt(month);
        StringJoiner header = new StringJoiner(",");
        String[] dataRow;
        header.add("Name");
        /*
        we know that months with 31 days are 1 3 5 7 8 10 12
        and when we use those numbers to shift left 1 we get
        2,8,32,128,256,1024,4096
        each of them represent a single 1 at different locations when transform into binary
        0b0000000000010
        0b0000000001000
        0b0000000100000
        0b0000010000000
        0b0000100000000
        0b0010000000000
        0b1000000000000
        so we could bitwise and with
        0b0101001010101
        we will always get 0 and otherwise with other numbers
         */
        if ((1 << (monthNumber) & MONTH31DAYS) == 0) {
            //create header with 31 days
            for (int i = 1; i <= 31; i++) {
                header.add(String.valueOf(i));
            }
            dataRow = new String[32];
        } else if (monthNumber == 2) {
            if (yearNumber % 400 == 0 || (yearNumber % 100 != 0) && (yearNumber % 4 == 0)) {
                //create header with 29 days
                for (int i = 1; i <= 29; i++) {
                    header.add(String.valueOf(i));
                }
                dataRow = new String[30];
            } else {
                //create header with 28 days
                for (int i = 1; i <= 28; i++) {
                    header.add(String.valueOf(i));
                }
                dataRow = new String[29];
            }

        } else {
            //create header with 30 days
            for (int i = 1; i <= 30; i++) {
                header.add(String.valueOf(i));
            }
            dataRow = new String[31];
        }
        dataRow[0] = empName;
        CsvWriter csvWriter = new CsvWriter(header.toString().split(","));
        for (WorkingDay w : workingDays) {
            dataRow[Integer.parseInt(w.getDay())] = String.valueOf(w.getHours());
        }
        IntStream.range(1, dataRow.length).filter(i -> dataRow[i] == null).forEach(i -> dataRow[i] = "0");
        csvWriter.addDataRow(dataRow);
        dataRow[0] = "project name";
        for (WorkingDay w : workingDays) {
            dataRow[Integer.parseInt(w.getDay())] = String.valueOf(w.getProjectName());
        }
        IntStream.range(1, dataRow.length).filter(i -> dataRow[i] == null).forEach(i -> dataRow[i] = "0");
        csvWriter.addDataRow(dataRow);

        try {
            csvWriter.build(empName + "-" + year + "-" + month);
            Toast.makeText(getActivity(), "csv Saved!", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
        }

    };
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
