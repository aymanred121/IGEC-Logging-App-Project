package com.igec.common.Fragments;

import static android.content.ContentValues.TAG;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.igec.common.Adapters.VacationAdapter;
import com.igec.common.databinding.FragmentVacationRequestsBinding;
import com.igec.common.firebase.Employee;
import com.igec.common.firebase.VacationRequest;
import com.igec.common.Dialogs.VacationDialog;

import java.util.ArrayList;


public class VacationRequestsFragment extends Fragment {

    private Employee employee;
    private int requestedDays;
    private AlertDialog dialog;
    private VacationAdapter adapter;
    private RecyclerView.LayoutManager layoutManager;
    private ArrayList<VacationRequest> vacationRequests;
    private Employee currManager;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private int unPaidDays;


    public static VacationRequestsFragment newInstance(Employee currManager) {

        Bundle args = new Bundle();
        args.putSerializable("currManager", currManager);
        VacationRequestsFragment fragment = new VacationRequestsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    private FragmentVacationRequestsBinding binding;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentVacationRequestsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        binding = null;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initialize();
        adapter.setOnItemClickListener(itclVacationAdapter);

    }

    // Functions
    private void initialize() {
        currManager = (Employee) getArguments().getSerializable("currManager");
        vacationRequests = new ArrayList<>();
        binding.recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(getActivity());
        adapter = new VacationAdapter(vacationRequests);
        binding.recyclerView.setLayoutManager(layoutManager);
        binding.recyclerView.setAdapter(adapter);
        loadVacations();


    }

    private void loadVacations() {
        if (currManager != null) {
            db.collection("Vacation")
                    .whereEqualTo("manager.id", currManager.getId())
                    .whereEqualTo("vacationStatus", 0)
                    .addSnapshotListener((queryDocumentSnapshots, e) -> {
                        if (e != null) {
                            Log.w(TAG, "Listen failed.", e);
                            return;
                        }
                        vacationRequests.clear();
                        for (DocumentSnapshot vacations : queryDocumentSnapshots) {
                            vacationRequests.add(vacations.toObject(VacationRequest.class));
                        }
                        adapter.setVacationsList(vacationRequests);
                        adapter.notifyDataSetChanged();
                    });
        } else {
            db.collection("Vacation")
                    .whereEqualTo("employee.managerID", "adminID")
                    .whereEqualTo("vacationStatus", 0)
                    .addSnapshotListener((queryDocumentSnapshots, e) -> {
                        if (e != null) {
                            Log.w(TAG, "Listen failed.", e);
                            return;
                        }
                        vacationRequests.clear();
                        for (DocumentSnapshot vacations : queryDocumentSnapshots) {
                            vacationRequests.add(vacations.toObject(VacationRequest.class));
                        }
                        adapter.setVacationsList(vacationRequests);
                        adapter.notifyDataSetChanged();
                    });
        }
    }

    VacationAdapter.OnItemClickListener itclVacationAdapter = position -> {
        VacationDialog vacationDialog = new VacationDialog(vacationRequests.get(position));
        vacationDialog.show(getParentFragmentManager(), "");
    };

}