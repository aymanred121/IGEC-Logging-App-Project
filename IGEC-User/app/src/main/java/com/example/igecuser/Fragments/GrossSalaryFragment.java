package com.example.igecuser.Fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.igecuser.Adapters.SalarySummaryAdapter;
import com.example.igecuser.R;
import com.example.igecuser.dummySalarySummary;
import com.example.igecuser.dummySalarySummary.SalaryType;


import java.util.ArrayList;

public class GrossSalaryFragment extends Fragment {


    private RecyclerView recyclerView;
    private SalarySummaryAdapter adapter;
    private RecyclerView.LayoutManager layoutManager;
    private ArrayList<dummySalarySummary> salarySummaries = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_gross_salary, container, false);
        initialize(view);
        return view;
    }

    // Functions
    private void initialize(View view) {

        // TODO: to be updated when firebase is down
        salarySummaries.add(new dummySalarySummary("Machine",50.0f, SalaryType.allowance));
        salarySummaries.add(new dummySalarySummary("Base Salary",1500.0f,SalaryType.base));
        salarySummaries.add(new dummySalarySummary("Late attendance",50.0f,SalaryType.penalty));
        salarySummaries.add(new dummySalarySummary("Over Time",50.0f,SalaryType.overtime));


        recyclerView = view.findViewById(R.id.recyclerview);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(getActivity());
        adapter = new SalarySummaryAdapter(salarySummaries);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);

    }


}