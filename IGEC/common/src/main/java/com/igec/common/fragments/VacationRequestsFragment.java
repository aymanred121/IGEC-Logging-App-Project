package com.igec.common.fragments;

import static android.content.ContentValues.TAG;

import static com.igec.common.CONSTANTS.ADMIN;
import static com.igec.common.CONSTANTS.PENDING;
import static com.igec.common.CONSTANTS.VACATION_COL;

import android.annotation.SuppressLint;
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
import com.igec.common.adapters.VacationAdapter;
import com.igec.common.databinding.FragmentVacationRequestsBinding;
import com.igec.common.firebase.Employee;
import com.igec.common.firebase.VacationRequest;
import com.igec.common.dialogs.VacationDialog;

import java.util.ArrayList;


public class VacationRequestsFragment extends Fragment {
    private VacationAdapter adapter;
    private ArrayList<VacationRequest> vacationRequests;
    private Employee currManager;


    public static VacationRequestsFragment newInstance(Employee currManager) {

        Bundle args = new Bundle();
        args.putSerializable("currManager", currManager);
        VacationRequestsFragment fragment = new VacationRequestsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    private FragmentVacationRequestsBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
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
        assert getArguments() != null;
        currManager = (Employee) getArguments().getSerializable("currManager");
        vacationRequests = new ArrayList<>();
        binding.recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
        adapter = new VacationAdapter(vacationRequests,true);
        binding.recyclerView.setLayoutManager(layoutManager);
        binding.recyclerView.setAdapter(adapter);
        loadVacations();


    }

    @SuppressLint("NotifyDataSetChanged")
    private void loadVacations() {
        if (currManager != null) {
            VACATION_COL
                    .whereEqualTo("manager.id", currManager.getId())
                    .whereEqualTo("vacationStatus", PENDING)
                    .addSnapshotListener((queryDocumentSnapshots, e) -> {
                        if (e != null) {
                            Log.w(TAG, "Listen failed.", e);
                            return;
                        }
                        vacationRequests.clear();
                        assert queryDocumentSnapshots != null;
                        for (DocumentSnapshot vacations : queryDocumentSnapshots) {
                            vacationRequests.add(vacations.toObject(VacationRequest.class));
                        }
                        adapter.setVacationsList(vacationRequests);
                        adapter.notifyDataSetChanged();
                    });
        } else {
            VACATION_COL
                    .whereEqualTo("manager", null)
                    .whereEqualTo("vacationStatus", PENDING)
                    .addSnapshotListener((queryDocumentSnapshots, e) -> {
                        if (e != null) {
                            Log.w(TAG, "Listen failed.", e);
                            return;
                        }
                        vacationRequests.clear();
                        assert queryDocumentSnapshots != null;
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