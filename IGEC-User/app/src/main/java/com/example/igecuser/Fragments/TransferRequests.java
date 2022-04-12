package com.example.igecuser.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.example.igecuser.R;
import com.example.igecuser.fireBase.Employee;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class TransferRequests extends Fragment {

    private RecyclerView recyclerView;
    private final Employee manager;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private ArrayList<com.example.igecuser.fireBase.TransferRequests> requests;
//    private ArrayList<Transfer> transfers;

    public TransferRequests(Employee manager) {
        this.manager = manager;
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

    private void initialize(View view) {
        recyclerView = view.findViewById(R.id.recyclerview);
    }

    private void getRequests() {
        db.collection("TransferRequests").whereEqualTo("newProjectId", manager.getProjectID()).addSnapshotListener((values, error) -> {
            if (values.size() == 0)
                return;
            requests.addAll(values.toObjects(com.example.igecuser.fireBase.TransferRequests.class));
        });
    }

    //TODO use updateRequestStatus() to update request status
    private void updateRequestStatus(String requestTransferId, int status) {
        db.collection("TransferRequests").document(requestTransferId).update("transferStatus", status).addOnSuccessListener(unused -> {
            Toast.makeText(getActivity(), "complete", Toast.LENGTH_SHORT).show();
        });
    }
}