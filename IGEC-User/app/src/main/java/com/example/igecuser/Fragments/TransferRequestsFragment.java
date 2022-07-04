package com.example.igecuser.Fragments;

import android.annotation.SuppressLint;
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
import com.example.igecuser.R;
import com.example.igecuser.fireBase.Allowance;
import com.example.igecuser.fireBase.Employee;
import com.example.igecuser.fireBase.EmployeeOverview;
import com.example.igecuser.fireBase.EmployeesGrossSalary;
import com.example.igecuser.fireBase.Project;
import com.example.igecuser.fireBase.TransferRequests;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class TransferRequestsFragment extends Fragment {

    private int transferRequestStatus;
    private TransferAdapter adapter;
    private RecyclerView.LayoutManager layoutManager;
    private RecyclerView recyclerView;
    private final Employee manager;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private ArrayList<TransferRequests> requests;
    private WriteBatch batch = FirebaseFirestore.getInstance().batch();
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
        batch.update( db.collection("employees").document(request.getEmployee().getId()),"projectID" , request.getNewProjectId()
                , "managerID" , manager.getId());

        ArrayList<String> empInfo = new ArrayList<>();
        empInfo.add(request.getEmployee().getFirstName());
        empInfo.add(request.getEmployee().getLastName());
        empInfo.add(request.getEmployee().getTitle());
        empInfo.add(manager.getId());
        empInfo.add(manager.getProjectID());
        empInfo.add("1"); // by default because already in project
        Map<String, Object> empInfoMap = new HashMap<>();
        empInfoMap.put(request.getEmployee().getId(), empInfo);
        batch.update( db.collection("EmployeeOverview").document("emp"),empInfoMap);
    }
    private void updateOldProjectData(TransferRequests request) {
        db.collection("projects").document(request.getOldProjectId()).get().addOnSuccessListener((value) ->
        {
            if (!value.exists()) return;
            Project projectTemp = value.toObject(Project.class);
            for (EmployeeOverview e: projectTemp.getEmployees()) {
                if(e.getId().equals(request.getEmployee().getId())) {
                    projectTemp.getEmployees().remove(e);
                    break;
                }

            }
            db.collection("projects").document(request.getOldProjectId()).update("employees" ,  projectTemp.getEmployees());
        });
    }
    private void updateNewProjectData(TransferRequests request) {
        db.collection("projects").document(request.getNewProjectId()).get().addOnSuccessListener((value) ->
        {
            if (!value.exists()) return;
            Project projectTemp = value.toObject(Project.class);
            request.getEmployee().setProjectId(request.getNewProjectId());
            request.getEmployee().setManagerID(manager.getId());
            projectTemp.getEmployees().add(request.getEmployee());
            db.collection("projects").document(request.getNewProjectId()).update("employees", projectTemp.getEmployees());
            updateAllowancesData(request, projectTemp.getAllowancesList());

        });
    }

    private void updateAllowancesData(TransferRequests request, ArrayList<Allowance> projectAllowances) {
        @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
        String currentDateAndTime = sdf.format(new Date());
        String day = currentDateAndTime.substring(0,2);
        String month = currentDateAndTime.substring(3,5);
        String year = currentDateAndTime.substring(6,10);
        db.collection("EmployeesGrossSalary").document(request.getEmployee().getId()).get().addOnSuccessListener(value -> {
            if (!value.exists())
                return;
            EmployeesGrossSalary temp = value.toObject(EmployeesGrossSalary.class);
            temp.getAllTypes().removeIf(x->x.getProjectId().equals(request.getOldProjectId()));
            temp.getAllTypes().addAll(projectAllowances);
            db.collection("EmployeesGrossSalary").document(request.getEmployee().getId()).update("allTypes", temp.getAllTypes());
            db.collection("EmployeesGrossSalary").document(request.getEmployee().getId()).collection(year).document(month).get().addOnSuccessListener(doc->{
                if (!doc.exists())
                    return;
                EmployeesGrossSalary employeesGrossSalary = doc.toObject(EmployeesGrossSalary.class);
                employeesGrossSalary.getBaseAllowances().removeIf(a->a.getProjectId().trim().equals(request.getOldProjectId()));
                employeesGrossSalary.getBaseAllowances().addAll(projectAllowances);
                db.document(doc.getReference().getPath()).update("baseAllowances", employeesGrossSalary.getBaseAllowances());
            });
        });


    }

    private void updateRequestStatus(TransferRequests request, int status) {
        batch = FirebaseFirestore.getInstance().batch();
        db.collection("TransferRequests").document(request.getTransferId()).update("transferStatus", status).addOnSuccessListener(unused -> {
            Toast.makeText(getActivity(), "complete", Toast.LENGTH_SHORT).show();
            if (status == 1) {
                updateEmployeeData(request);
                updateOldProjectData(request);
                updateNewProjectData(request);
                batch.commit();
            }
            else if(status == 0) {/*do nothing*/}
        });
    }

    private final TransferAdapter.OnItemClickListener oclRequest = new TransferAdapter.OnItemClickListener() {
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