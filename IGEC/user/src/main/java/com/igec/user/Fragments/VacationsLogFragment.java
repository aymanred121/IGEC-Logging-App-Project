package com.igec.user.Fragments;

import static android.content.ContentValues.TAG;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.igec.user.R;
import com.igec.common.Adapters.VacationAdapter;
import com.igec.common.firebase.Employee;
import com.igec.common.firebase.VacationRequest;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;


public class VacationsLogFragment extends Fragment {

    private RecyclerView recyclerView;
    private static VacationAdapter adapter;
    private RecyclerView.LayoutManager layoutManager;

    private ArrayList<VacationRequest> vacations;
    private Employee user;
    private boolean isEmployee;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    public static VacationsLogFragment newInstance(Employee user,boolean isEmployee) {

        Bundle args = new Bundle();
        args.putBoolean("isEmployee",isEmployee);
        args.putSerializable("user", user);
        VacationsLogFragment fragment = new VacationsLogFragment();
        fragment.setArguments(args);
        return fragment;
    }
//    public VacationsLogFragment(boolean isEmployee, Employee user) {
//        this.isEmployee = isEmployee;
//        this.user = user;
//
//    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_vacations_log, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Initialize(view);
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            loadVacations();
        }
    }

    // Functions
    private void Initialize(View view) {
        user = (Employee) getArguments().getSerializable("user");
        isEmployee = getArguments().getBoolean("isEmployee");
        vacations = new ArrayList<>();
        recyclerView = view.findViewById(R.id.recyclerview);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(getActivity());
        adapter = new VacationAdapter(vacations);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
        loadVacations();
    }

    private void loadVacations() {

        if (isEmployee) {
            loadOwnVacations(new ArrayList<>());
        } else {
            db.collection("Vacation")
                    .whereEqualTo("manager.id", user.getId())
                    .orderBy("requestDate", Query.Direction.DESCENDING)
                    .addSnapshotListener((queryDocumentSnapshots, e) -> {
                        if (e != null) {
                            Log.w(TAG, "Listen failed.", e);
                            loadOwnVacations(new ArrayList<>());
                            return;
                        }
                        ArrayList<VacationRequest> vacationRequests = new ArrayList<>();
                        // loads his employees vacations
                        for (DocumentSnapshot vacations : queryDocumentSnapshots) {
                            if (vacations.toObject(VacationRequest.class).getVacationStatus() != 0)
                                vacationRequests.add(vacations.toObject(VacationRequest.class));
                        }
                        loadOwnVacations(vacationRequests);
                    });
        }
    }

    private void loadOwnVacations(ArrayList<VacationRequest> vacationRequests) {
        db.collection("Vacation")
                .whereEqualTo("employee.id", user.getId())
                .orderBy("requestDate", Query.Direction.DESCENDING)
                .addSnapshotListener((queryDocumentSnapshots, er) -> {
                    if (er != null) {
                        Log.w(TAG, "Listen failed.", er);
                        adapter.setVacationsList(vacationRequests);
                        adapter.notifyDataSetChanged();
                        return;
                    }
                    ArrayList<VacationRequest> ownVacationRequests = new ArrayList<>();
                    for (DocumentSnapshot vacations : queryDocumentSnapshots) {
                        ownVacationRequests.add(vacations.toObject(VacationRequest.class));
                    }
                    adapter.getVacationsList().clear();
                    adapter.getVacationsList().addAll(vacationRequests);
                    adapter.getVacationsList().addAll(ownVacationRequests);
                    adapter.notifyDataSetChanged();
                });
    }

}