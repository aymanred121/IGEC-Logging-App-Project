package com.example.igecuser.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.igecuser.R;
import com.example.igecuser.fireBase.Employee;
import com.example.igecuser.fireBase.EmployeeOverview;
import com.example.igecuser.fireBase.Project;
import com.example.igecuser.fireBase.TransferRequests;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class SendTransferRequest extends Fragment {

    //TODO to be used in the transfer request sending process
    private final Employee manager;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private String note;
    private final ArrayList<Project> projects = new ArrayList<>();


    public SendTransferRequest(Employee manager) {
        this.manager = manager;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_send_transfer_request, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    private void initialize(View view) {
    }
    //TODO: Use sendRequest() to send a transfer request

    /**
     * You have to provide
     * employeeOverview of the transferred employee which you can get from the project object
     * old "current" project name,id,ref
     * new "target" project name,id,ref
     * note
     */
    private Task<Void> sendRequest(EmployeeOverview employee) {
        String transferId = db.collection("TransferRequests").getId();
        TransferRequests request = new TransferRequests();
        request.setTransferId(transferId);
        request.setEmployee(employee);
        request.setNewProjectId("");
        request.setNewProjectName("");
        request.setNewProjectReference("");
        request.setOldProjectId("");
        request.setOldProjectName("");
        request.setOldProjectReference("");
        request.setNote(note);
        return db.collection("TransferRequests").document(transferId).set(request);
    }

    private Task<QuerySnapshot> getAllProjects(String projectId) {
        return db.collection("Project").whereNotEqualTo("id", projectId).get().addOnSuccessListener(queryDocumentSnapshots -> {
            if (queryDocumentSnapshots.size() == 0)
                return;
            projects.addAll((queryDocumentSnapshots.toObjects(Project.class)));
        });
    }
}