package com.igec.common.fragments;

import static android.content.ContentValues.TAG;

import static com.igec.common.CONSTANTS.ADMIN;
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
import com.google.firebase.firestore.Query;
import com.igec.common.adapters.VacationAdapter;
import com.igec.common.databinding.FragmentVacationsLogBinding;
import com.igec.common.firebase.Employee;
import com.igec.common.firebase.VacationRequest;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Objects;

public class VacationsLogFragment extends Fragment {

    private static VacationAdapter adapter;
    private ArrayList<VacationRequest> vacations;
    private Employee user;
    private boolean loadOwn;
    private FragmentVacationsLogBinding binding;

    public static VacationsLogFragment newInstance(Employee user) {

        Bundle args = new Bundle();
        args.putSerializable("user", user);
        VacationsLogFragment fragment = new VacationsLogFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentVacationsLogBinding.inflate(inflater, container, false);
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
        loadOwn = false;
        assert getArguments() != null;
        user = (Employee) getArguments().getSerializable("user");
        vacations = new ArrayList<>();

        binding.recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
        adapter = new VacationAdapter(vacations, false);
        binding.recyclerView.setLayoutManager(layoutManager);
        binding.recyclerView.setAdapter(adapter);

        if (user == null)// admin
            loadVacations("manager", null);
        else if (user.getManagerID().equals(ADMIN)) // manager
        {
            loadOwn = true;
            loadVacations("manager.id", user.getId());
        } else // employee
        {
            loadVacations("employee.id", user.getId());
        }
    }


    @SuppressLint("NotifyDataSetChanged")
    private void loadVacations(String who, String id) {
        VACATION_COL
                .whereEqualTo(who, id)
                .orderBy("requestDate", Query.Direction.DESCENDING)
                .addSnapshotListener((queryDocumentSnapshots, e) -> {
                    if (e != null) {
                        Log.w(TAG, "Listen failed.", e);
                        return;
                    }
                    vacations.clear();
                    assert queryDocumentSnapshots != null;
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        if (Objects.requireNonNull(doc.toObject(VacationRequest.class)).getVacationStatus() != 0)
                            vacations.add(doc.toObject(VacationRequest.class));
                    }
                    if (loadOwn) {
                        loadOwnVacations();
                    } else {
                        vacations.sort(Comparator.comparing(VacationRequest::getRequestDate));
                        adapter.notifyDataSetChanged();
                    }
                });
    }

    // to avoid recursion
    @SuppressLint("NotifyDataSetChanged")
    private void loadOwnVacations() {
        VACATION_COL
                .whereEqualTo("employee.id", user.getId())
                .orderBy("requestDate", Query.Direction.DESCENDING)
                .addSnapshotListener((queryDocumentSnapshots, e) -> {
                    if (e != null) {
                        Log.w(TAG, "Listen failed.", e);
                        return;
                    }
                    // loads his employees vacations
                    assert queryDocumentSnapshots != null;
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        if (Objects.requireNonNull(doc.toObject(VacationRequest.class)).getVacationStatus() != 0)
                            vacations.add(doc.toObject(VacationRequest.class));
                    }
                    vacations.sort(Comparator.comparing(VacationRequest::getRequestDate).reversed());
                    adapter.notifyDataSetChanged();
                });
    }

}