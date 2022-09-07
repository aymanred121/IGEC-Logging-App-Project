package com.igec.user.fragments;

import static com.igec.common.CONSTANTS.EMPLOYEE_COL;
import static com.igec.common.CONSTANTS.EMPLOYEE_GROSS_SALARY_COL;
import static com.igec.common.CONSTANTS.EMPLOYEE_OVERVIEW_REF;
import static com.igec.common.CONSTANTS.PROJECT_COL;
import static com.igec.common.CONSTANTS.TRANSFER_REQUESTS_COL;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.igec.user.activities.DateInaccurate;
import com.igec.user.adapters.TransferAdapter;
import com.igec.common.firebase.Allowance;
import com.igec.common.firebase.Employee;
import com.igec.common.firebase.EmployeeOverview;
import com.igec.common.firebase.EmployeesGrossSalary;
import com.igec.common.firebase.Project;
import com.igec.common.firebase.TransferRequests;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;
import com.igec.user.databinding.FragmentTransferRequestsBinding;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class TransferRequestsFragment extends Fragment {

    private int transferRequestStatus;
    private TransferAdapter adapter;
    private RecyclerView.LayoutManager layoutManager;
    private Employee manager;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private ArrayList<TransferRequests> requests;
    private String day, year, month;
    private Project newProject;
    private WriteBatch batch = FirebaseFirestore.getInstance().batch();

    public static TransferRequestsFragment newInstance(Employee manager) {

        Bundle args = new Bundle();
        args.putSerializable("manager", manager);
        TransferRequestsFragment fragment = new TransferRequestsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    private FragmentTransferRequestsBinding binding;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentTransferRequestsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        binding = null;
    }

    @Override
    public void onResume() {
        super.onResume();
        validateDate(getActivity());
    }

    private void validateDate(Context c) {
        if (Settings.Global.getInt(c.getContentResolver(), Settings.Global.AUTO_TIME, 0) != 1) {
            Intent intent = new Intent(getActivity(), DateInaccurate.class);
            startActivity(intent);
            getActivity().finish();
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initialize();
        adapter.setOnItemClickListener(oclRequest);
    }

    private void initialize() {
        manager = (Employee) getArguments().getSerializable("manager");
        requests = new ArrayList<>();
        binding.recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(getActivity());
        adapter = new TransferAdapter(requests);
        binding.recyclerView.setLayoutManager(layoutManager);
        binding.recyclerView.setAdapter(adapter);
        getRequests();
    }

    private void getRequests() {
        TRANSFER_REQUESTS_COL.whereEqualTo("oldProjectId", manager.getProjectID()).whereEqualTo("transferStatus", -1).addSnapshotListener((values, error) -> {
            requests.clear();
            adapter.notifyDataSetChanged();
            if (values.size() == 0)
                return;
            requests.addAll(values.toObjects(com.igec.common.firebase.TransferRequests.class));
            adapter.setTransfers(requests);
            adapter.notifyDataSetChanged();
        });
    }

    private void updateDate() {
        Calendar calendar = Calendar.getInstance();
        year = String.valueOf(calendar.get(Calendar.YEAR));
        month = String.format("%02d", calendar.get(Calendar.MONTH) + 1);
        day = String.format("%02d", calendar.get(Calendar.DAY_OF_MONTH));
        if (Integer.parseInt(day) > 25) {
            if (Integer.parseInt(month) + 1 == 13) {
                month = "01";
                year = String.format("%d", Integer.parseInt(year) + 1);
            } else {
                month = String.format("%02d", Integer.parseInt(month) + 1);
            }
        }
    }

    private void updateEmployeeData(TransferRequests request) {
        batch.update(EMPLOYEE_COL.document(request.getEmployee().getId()), "projectID", newProject.getId()
                , "managerID", newProject.getManagerID());

        ArrayList<String> empInfo = new ArrayList<>();
        empInfo.add(request.getEmployee().getFirstName());
        empInfo.add(request.getEmployee().getLastName());
        empInfo.add(request.getEmployee().getTitle());
        empInfo.add(newProject.getManagerID());
        empInfo.add(newProject.getId());
        empInfo.add("1"); // by default because already in project
        Map<String, Object> empInfoMap = new HashMap<>();
        empInfoMap.put(request.getEmployee().getId(), empInfo);
        batch.update(EMPLOYEE_OVERVIEW_REF, empInfoMap);
    }


    private void updateProjectData(TransferRequests request) {
        //remove emp from old project
        batch.update(PROJECT_COL.document(request.getOldProjectId()), "employees", FieldValue.arrayRemove(request.getEmployee()));
        //add emp to new project
        request.getEmployee().setProjectId(request.getNewProjectId());
        request.getEmployee().setManagerID(newProject.getManagerID());
        batch.update(PROJECT_COL.document(request.getNewProjectId()), "employees", FieldValue.arrayUnion(request.getEmployee()));
    }

    private void updateAllowancesData(TransferRequests request, ArrayList<Allowance> projectAllowances) {
        updateDate();
        EMPLOYEE_GROSS_SALARY_COL.document(request.getEmployee().getId()).get().addOnSuccessListener(value -> {
            if (!value.exists())
                return;
            EmployeesGrossSalary employeesGrossSalary1 = value.toObject(EmployeesGrossSalary.class);
            employeesGrossSalary1.getAllTypes().removeIf(x -> x.getProjectId().trim().equals(request.getOldProjectId()));
            employeesGrossSalary1.getAllTypes().addAll(projectAllowances);
            batch.update(EMPLOYEE_GROSS_SALARY_COL.document(request.getEmployee().getId()), "allTypes", employeesGrossSalary1.getAllTypes());

            EMPLOYEE_GROSS_SALARY_COL.document(request.getEmployee().getId()).collection(year).document(month).get().addOnSuccessListener(doc -> {
                if (!doc.exists()) {
                    batch.commit();
                    return;
                }
                EmployeesGrossSalary employeesGrossSalary = doc.toObject(EmployeesGrossSalary.class);
                employeesGrossSalary.getBaseAllowances().removeIf(a -> a.getProjectId().trim().equals(request.getOldProjectId()));
                employeesGrossSalary.getBaseAllowances().addAll(projectAllowances);
                batch.update(db.document(doc.getReference().getPath()), "baseAllowances", employeesGrossSalary.getBaseAllowances());
                batch.commit();
            });
        });

    }

    private void updateTransferRequests(TransferRequests request) {
        // change transfer status to 0 (rejected) for all requests on this employee
        TRANSFER_REQUESTS_COL.whereEqualTo("employee.id", request.getEmployee().getId()).get().addOnSuccessListener(values -> {
            for (DocumentSnapshot doc : values) {
                if (doc.getId().equals(request.getTransferId()))
                    continue;
                batch.update(db.document(doc.getReference().getPath()), "transferStatus", 0);
            }
            updateAllowancesData(request, newProject.getAllowancesList());
        });
    }

    private void updateRequestStatus(TransferRequests request, int status) {
        batch = FirebaseFirestore.getInstance().batch();
        if (status == 1) {
            PROJECT_COL.document(request.getNewProjectId()).get().addOnSuccessListener(doc -> {
                if (!doc.exists())
                    return;
                newProject = doc.toObject(Project.class);
                batch.update(TRANSFER_REQUESTS_COL.document(request.getTransferId()), "transferStatus", status);
                updateEmployeeData(request);
                updateProjectData(request);
                updateTransferRequests(request);

            });
        }


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
            builder.setPositiveButton("Accept", (dialog, which) -> {
                transferRequestStatus = 1;
                requests.remove(request);
                adapter.notifyItemRemoved(position);
                updateRequestStatus(request, transferRequestStatus);
            });
            builder.setNegativeButton("Reject", (dialogInterface, i) -> {
                transferRequestStatus = 0;
                requests.remove(request);
                adapter.notifyItemRemoved(position);
                updateRequestStatus(request, transferRequestStatus);
            });
            builder.setNeutralButton("Cancel", (dialogInterface, i) -> {

            });
            builder.show();


        }
    };

}