package com.example.igec_admin.Fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.igec_admin.Adatpers.ProjectAdapter;
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
        }
        else
        {
            //TODO: Add condition to retrieve only the selected month data
           db.collection("EmployeesGrossSalary").get().addOnSuccessListener(queryDocumentSnapshots -> {
               CsvWriter csvWriter = new CsvWriter();
               String[] header = {"Name","Basic","Cuts","Transportation","personal","others"};
               csvWriter.addHeader(header);
               final int[] counter = new int[1];
               for(QueryDocumentSnapshot q : queryDocumentSnapshots){
                   db.collection("employees").document(q.getId()).get().addOnSuccessListener(documentSnapshot -> {
                       Employee emp = documentSnapshot.toObject(Employee.class);
                       String cuts = " ";
                       String transportation = " ";
                       String personal = " ";
                       String others = "[";
                       for(Allowance allowance : q.toObject(EmployeesGrossSalary.class).getAllTypes()) {
                            if (allowance.getName().equalsIgnoreCase("Transportation")) {
                                transportation = String.valueOf(allowance.getAmount());
                            }
                            switch (allowance.getType()) {
                                case 4:
                                    cuts = String.valueOf(allowance.getAmount());
                                    break;
                                case 3:
                                    personal = String.valueOf(allowance.getAmount());
                                    break;
                                default:
                                    others += allowance.getAmount() + "-";
                            }
                       }
                       others = others.length() != 1 ? others.substring(0, others.length() - 1) + "]" : " ";
                       csvWriter.addDataRow(emp.getFirstName()+" "+emp.getLastName(),String.valueOf(emp.getSalary()),cuts,transportation,personal,others,"\n");
                       counter[0]++;
                       if(counter[0]==queryDocumentSnapshots.size()) {
                           try {
                               csvWriter.build("test123");
                               Toast.makeText(getActivity(), "CSV file created", Toast.LENGTH_SHORT).show();
                           } catch (IOException e) {
                               e.printStackTrace();
                           }
                       }
                   });
               }
           });
        }
    };
}