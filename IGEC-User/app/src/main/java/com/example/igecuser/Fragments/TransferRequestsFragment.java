package com.example.igecuser.Fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.igecuser.Adapters.TransferAdapter;
import com.example.igecuser.Adapters.VacationAdapter;
import com.example.igecuser.R;
import com.example.igecuser.fireBase.Employee;
import com.example.igecuser.fireBase.EmployeeOverview;
import com.example.igecuser.fireBase.Project;
import com.example.igecuser.fireBase.TransferRequests;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class TransferRequestsFragment extends Fragment {

    private int transferRequestStatus;
    private TransferAdapter adapter;
    private RecyclerView.LayoutManager layoutManager;
    private RecyclerView recyclerView;
    private final Employee manager;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private ArrayList<TransferRequests> requests;
    public TransferRequestsFragment(Employee manager) {
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
        initialize(view);
        adapter.setOnItemClickListener(oclRequest);
    }

    private void initialize(View view) {
        requests = new ArrayList<>();
        recyclerView = view.findViewById(R.id.recyclerview);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(getActivity());
        adapter = new TransferAdapter(requests);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
        getRequests();
    }

    private void getRequests() {
        db.collection("TransferRequests").whereEqualTo("newProjectId", manager.getProjectID()).whereEqualTo("transferStatus",-1).addSnapshotListener((values, error) -> {
            if (values.size() == 0)
                return;
            requests.clear();
            adapter.notifyDataSetChanged();
            requests.addAll(values.toObjects(com.example.igecuser.fireBase.TransferRequests.class));
            adapter.setTransfers(requests);
            adapter.notifyDataSetChanged();
        });
    }
    private void updateEmployeeData (TransferRequests request) {
        db.collection("employees").document(request.getEmployee().getId())
                .update("projectID" , request.getNewProjectId()
                        , "managerID" , manager.getId());

        EmployeeOverview temp = new EmployeeOverview(request.getEmployee());
        temp.setManagerID(manager.getId());
        temp.setProjectId(request.getNewProjectId());
        db.collection("EmployeeOverview").document("emp")
                .update(request.getEmployee().getId() , temp );
    }
    private void updateOldProjectData(TransferRequests request) {
        db.collection("projects").document(request.getOldProjectId()).get().addOnSuccessListener((value) ->
        {
            if(value !=null)
            {
                Project projectTemp = value.toObject(Project.class);
                for (EmployeeOverview e: projectTemp.getEmployees()) {
                    if(e.getId().equals(request.getEmployee().getId())) {
                        projectTemp.getEmployees().remove(e);
                        break;
                    }

                }

                db.collection("projects").document(request.getOldProjectId()).update("employees" ,  projectTemp.getEmployees());
            }
        });
    }
    private void updateNewProjectData(TransferRequests request) {
        db.collection("projects").document(request.getNewProjectId()).get().addOnSuccessListener((value) ->
        {
            if(value !=null)
            {
                Project projectTemp = value.toObject(Project.class);
                request.getEmployee().setProjectId(request.getNewProjectId());
                request.getEmployee().setManagerID(manager.getId());
                projectTemp.getEmployees().add(request.getEmployee());
                db.collection("projects").document(request.getNewProjectId()).update("employees" ,  projectTemp.getEmployees());
            }
        });
    }
    private void updateAllowancesData(TransferRequests request) {
        //TODO CHANGE ALLOWANCES AFTER DISCUSSION
    }
    private void updateRequestStatus(TransferRequests request, int status) {
        db.collection("TransferRequests").document(request.getTransferId()).update("transferStatus", status).addOnSuccessListener(unused -> {
            Toast.makeText(getActivity(), "complete", Toast.LENGTH_SHORT).show();
            if(status == 1)
            {
                updateEmployeeData(request);
                updateOldProjectData(request);
                updateNewProjectData(request);
                updateAllowancesData(request);
            }
            else if(status == 0) {/*do nothing*/}
        });
    }

    private TransferAdapter.OnItemClickListener oclRequest = new TransferAdapter.OnItemClickListener() {
        @Override
        public void onItemClick(int position) {
            TransferRequests request = requests.get(position);
            EmployeeOverview employee = request.getEmployee();
            String content = "Employee ID: " + request.getEmployee().getId() + '\n'
                    + "Employee Name: " + employee.getFirstName() + " " + employee.getLastName() + '\n'
                    + "Project reference: IGEC" + request.getOldProjectReference() + '\n'
                    + "Project name: " + request.getOldProjectName() + '\n'
                    + "Comment: " + request.getNote();
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage(content);
            builder.setTitle("Content");
            builder.setPositiveButton("Accept", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    transferRequestStatus = 1;
                    requests.remove(request);
                    adapter.notifyItemRemoved(position);
                    updateRequestStatus(request,transferRequestStatus);
                }
            });
            builder.setNegativeButton("Reject", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    transferRequestStatus = 0;
                    requests.remove(request);
                    adapter.notifyItemRemoved(position);
                    updateRequestStatus(request,transferRequestStatus);
                }
            });
            builder.setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

                }
            });
            builder.show();


        }
    };

}