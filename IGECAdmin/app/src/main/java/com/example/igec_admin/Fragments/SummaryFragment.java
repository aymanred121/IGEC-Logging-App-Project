package com.example.igec_admin.Fragments;

import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.igec_admin.Adatpers.ProjectAdapter;
import com.example.igec_admin.Dialogs.EmployeeFragmentDialog;
import com.example.igec_admin.R;
import com.example.igec_admin.fireBase.Allowance;
import com.example.igec_admin.fireBase.Employee;
import com.example.igec_admin.fireBase.EmployeesGrossSalary;
import com.example.igec_admin.fireBase.Project;
import com.example.igec_admin.utilites.CsvWriter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.whiteelephant.monthpicker.MonthPickerDialog;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;

public class SummaryFragment extends Fragment {


    // Views
    private RecyclerView recyclerView;
    private TextInputLayout selectedMonthLayout;
    private TextInputEditText selectedMonthEdit;
    private FloatingActionButton createCSV;
    // Vars
    private ProjectAdapter adapter;
    private RecyclerView.LayoutManager layoutManager;
    private String year, month;
    ArrayList<Project> projects = new ArrayList();
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    CollectionReference projectRef = db.collection("projects");

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_summary, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Initialize(view);
        selectedMonthLayout.setEndIconOnClickListener(oclMonthPicker);
        selectedMonthLayout.setErrorIconOnClickListener(oclMonthPicker);
        selectedMonthLayout.setErrorIconDrawable(R.drawable.ic_baseline_calendar_month_24);
        adapter.setOnItemClickListener(oclEmployees);
        createCSV.setOnClickListener(oclCSV);
    }

    // Functions
    private void Initialize(View view) {
        recyclerView = view.findViewById(R.id.recyclerview);
        selectedMonthEdit = view.findViewById(R.id.TextInput_SelectedMonth);
        selectedMonthLayout = view.findViewById(R.id.textInputLayout_SelectedMonth);
        createCSV = view.findViewById(R.id.fab_createCSV);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(getActivity());
        adapter = new ProjectAdapter(projects);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
        getProjects();

    }

    void getProjects() {
        projectRef.addSnapshotListener((queryDocumentSnapshots, e) -> {
            projects.clear();
            for (DocumentSnapshot d : queryDocumentSnapshots) {
                projects.add(d.toObject(Project.class));
            }
            adapter.setProjectsList(projects);
            adapter.notifyDataSetChanged();
        });
    }

    private final View.OnClickListener oclMonthPicker = v -> {
        final Calendar today = Calendar.getInstance();
        MonthPickerDialog.Builder builder = new MonthPickerDialog.Builder(getActivity(),
                new MonthPickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(int selectedMonth, int selectedYear) {
                        selectedMonthLayout.setError(null);
                        selectedMonthLayout.setErrorEnabled(false);
                        selectedMonthEdit.setText(String.format("%d/%d", selectedMonth + 1, selectedYear));
                        String[] selectedDate = selectedMonthEdit.getText().toString().split("/");
                        year = selectedDate[1];
                        month = selectedDate[0];
                        if (month.length() == 1) {
                            month = "0" + month;
                        }
                    }
                }, today.get(Calendar.YEAR), today.get(Calendar.MONTH));
        builder.setActivatedMonth(today.get(Calendar.MONTH))
                .setMinYear(today.get(Calendar.YEAR) - 1)
                .setActivatedYear(today.get(Calendar.YEAR))
                .setMaxYear(today.get(Calendar.YEAR) + 1)
                .setTitle("Select Month")
                .build().show();

    };

    private final View.OnClickListener oclCSV = v -> {
        if (selectedMonthEdit.getText().toString().isEmpty()) {
            selectedMonthLayout.setError("Please select a month");
        } else {
            String[] selectedDate = selectedMonthEdit.getText().toString().split("/");
            db.collection("employees")
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        String[] header = {"Name", "Basic","over time", "Cuts", "Transportation", "other", "personal" , "Next month" , "current month" , "previous month"};
                        CsvWriter csvWriter = new CsvWriter(header);
                        final int[] counter = new int[1];
                        for (QueryDocumentSnapshot queryDocumentSnapshot : queryDocumentSnapshots) {
                            year = selectedDate[1];
                            month = selectedDate[0];
                            if (month.length() == 1)
                                month = "0" + month;
                            db.collection("EmployeesGrossSalary").document(queryDocumentSnapshot.getId()).collection(year).document(month).get().addOnSuccessListener(documentSnapshot1 -> {
                                if (!documentSnapshot1.exists()) {
                                    if (counter[0] == queryDocumentSnapshots.size()-1) {
                                        try {
                                            csvWriter.build(year+"-"+month);
                                            Toast.makeText(getActivity(), "CSV file created", Toast.LENGTH_SHORT).show();
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                    counter[0]++;
                                    return;
                                }
                                Employee emp = queryDocumentSnapshot.toObject(Employee.class);
                                double cuts  = 0 ;
                                double transportation = 0 ;
                                double other = 0 ;
                                double overTime = 0;
                                double personal = 0;
                                double nextMonth = 0;
                                double currentMonth = 0;
                                double previousMonth = 0;
                                for (Allowance allowance : documentSnapshot1.toObject(EmployeesGrossSalary.class).getAllTypes()) {
                                    if (allowance.getName().trim().equalsIgnoreCase("Transportation"))
                                        transportation +=allowance.getAmount();
                                    else
                                        other += allowance.getAmount();
                                    if (allowance.getName().trim().equalsIgnoreCase("overTime"))
                                        overTime += allowance.getAmount();
                                    //todo use allowancesEnum instead of magic numbers
                                    switch (allowance.getType()) {
                                        case 4:
                                            cuts += allowance.getAmount();
                                            break;
                                        case 2:
                                        case 3:
                                            personal += allowance.getAmount();
                                            break;
                                        default:
                                            nextMonth += allowance.getAmount();
                                    }
                                }
                                nextMonth = other + personal;
                                currentMonth = transportation+emp.getSalary()+cuts+overTime;
                                previousMonth = 10000;
                                csvWriter.addDataRow(emp.getFirstName() + " " + emp.getLastName(), String.valueOf(emp.getSalary()), String.valueOf(overTime), String.valueOf(cuts), String.valueOf(transportation) , String.valueOf(other), String.valueOf(personal), String.valueOf(nextMonth) , String.valueOf(currentMonth) , String.valueOf(previousMonth));
                                if (counter[0] == queryDocumentSnapshots.size()-1) {
                                    try {
                                        csvWriter.build(year+"-"+month);
                                        Toast.makeText(getActivity(), "CSV file created", Toast.LENGTH_SHORT).show();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                                counter[0]++;
                            });

                        }
                    });
        }
    };
    private ProjectAdapter.OnItemClickListener oclEmployees = new ProjectAdapter.OnItemClickListener() {
        @Override
        public void onItemClick(int position) {
            final Calendar today = Calendar.getInstance();
            MonthPickerDialog.Builder builder = new MonthPickerDialog.Builder(getActivity(),
                    new MonthPickerDialog.OnDateSetListener() {
                        @Override
                        public void onDateSet(int selectedMonth, int selectedYear) {
                            selectedMonthLayout.setError(null);
                            selectedMonthLayout.setErrorEnabled(false);
                            selectedMonthEdit.setText(String.format("%d/%d", selectedMonth + 1, selectedYear));
                            String[] selectedDate = selectedMonthEdit.getText().toString().split("/");
                            year = selectedDate[1];
                            month = selectedDate[0];
                            if (month.length() == 1) {
                                month = "0" + month;
                            }
                            EmployeeFragmentDialog employeeFragmentDialog = new EmployeeFragmentDialog(projects.get(position), year, month);
                            employeeFragmentDialog.show(getParentFragmentManager(), "");

                        }
                    }, today.get(Calendar.YEAR), today.get(Calendar.MONTH));
            MonthPickerDialog monthPickerDialog = builder.setActivatedMonth(today.get(Calendar.MONTH))
                    .setMinYear(today.get(Calendar.YEAR) - 1)
                    .setActivatedYear(today.get(Calendar.YEAR))
                    .setMaxYear(today.get(Calendar.YEAR) + 1)
                    .setTitle("Select Month")
                    .build();
            if (selectedMonthEdit.getText().toString().isEmpty())
                monthPickerDialog.show();
            else
            {
                String[] selectedDate = selectedMonthEdit.getText().toString().split("/");
                year = selectedDate[1];
                month = selectedDate[0];
                if (month.length() == 1) {
                    month = "0" + month;
                }
                EmployeeFragmentDialog employeeFragmentDialog = new EmployeeFragmentDialog(projects.get(position), year, month);
                employeeFragmentDialog.show(getParentFragmentManager(), "");
            }

        }
    };
}
