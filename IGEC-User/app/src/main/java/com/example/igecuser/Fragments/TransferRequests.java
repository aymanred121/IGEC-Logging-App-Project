package com.example.igecuser.Fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.igecuser.R;
import com.example.igecuser.fireBase.Employee;
import com.example.igecuser.fireBase.EmployeeOverview;

import java.util.ArrayList;

public class TransferRequests extends Fragment {

    private RecyclerView recyclerView;
    private Employee Manager;
    //TODO: fill this List with the Transfer Requests sent to the current Manager
//    private ArrayList<Transfer> transfers;

    public TransferRequests(Employee manager) {
        Manager = manager;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_transfer_requests, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }
    private void initialize(View view)
    {
        recyclerView = view.findViewById(R.id.recyclerview);
    }
}