package com.example.igecuser.Fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.igecuser.R;
import com.example.igecuser.VacationAdapter;
import com.example.igecuser.VacationInfo;
import com.example.igecuser.dummyVacation;

import java.util.ArrayList;


public class VacationRequests extends Fragment {

    private RecyclerView recyclerView;
    private VacationAdapter adapter;
    private RecyclerView.LayoutManager layoutManager;
    private ArrayList<dummyVacation> vacations = new ArrayList<>();
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_vacation_requests, container, false);
        Initialize(view);

        // Inflate the layout for this fragment
        return view;
    }

    // Functions
    private void Initialize(View view) {

        for(int i = 0 ; i < 10; i++)
        {
            vacations.add(new dummyVacation());
        }
        recyclerView = view.findViewById(R.id.recyclerview);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(getActivity());
        adapter = new VacationAdapter(vacations);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);

        adapter.setOnItemClickListener(new VacationAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                Intent intent = new Intent(getActivity(), VacationInfo.class);
                startActivity(intent);
            }
        });
    }

}