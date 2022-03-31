package com.example.igecuser.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.igecuser.Adapters.EmployeeAdapter;
import com.example.igecuser.Dialogs.SalarySummaryDialog;
import com.example.igecuser.R;
import com.example.igecuser.dummyEmployee;
import com.example.igecuser.dummySalarySummary;
import com.example.igecuser.dummySalarySummary.SalaryType;

import java.util.ArrayList;

public class ProjectSummaryFragment extends Fragment {
    private final EmployeeAdapter.OnItemClickListener itemClickListener = new EmployeeAdapter.OnItemClickListener() {
        @Override
        public void onItemClick(int position) {
            ArrayList<dummySalarySummary> salarySummaries = new ArrayList<>();
            // TODO: to be updated when firebase is done
            salarySummaries.add(new dummySalarySummary("Machine", 50.0f, SalaryType.allowance));
            salarySummaries.add(new dummySalarySummary("Base Salary", 1500.0f, SalaryType.base));
            salarySummaries.add(new dummySalarySummary("Late attendance", 50.0f, SalaryType.penalty));
            salarySummaries.add(new dummySalarySummary("Over Time", 50.0f, SalaryType.overtime));
            SalarySummaryDialog employeeSummaryDialog = new SalarySummaryDialog(salarySummaries);
            employeeSummaryDialog.show(getParentFragmentManager(), "");
        }
    };
    private RecyclerView recyclerView;
    private EmployeeAdapter adapter;
    private RecyclerView.LayoutManager layoutManager;
    private ArrayList<dummyEmployee> dummyEmployees;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_project_summary, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initialize(view);
        adapter.setOnItemClickListener(itemClickListener);
    }

    // Functions
    private void initialize(View view) {

        dummyEmployees = new ArrayList<>();
        // TODO: to be updated when firebase is done
        dummyEmployees.add(new dummyEmployee("A", 0, 1, 1));
        dummyEmployees.add(new dummyEmployee("B", 1, 1, 1));
        dummyEmployees.add(new dummyEmployee("C", 2, 1, 1));
        dummyEmployees.add(new dummyEmployee("D", 3, 1, 1));


        recyclerView = view.findViewById(R.id.recyclerview);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(getActivity());
        adapter = new EmployeeAdapter(dummyEmployees);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);

    }
}