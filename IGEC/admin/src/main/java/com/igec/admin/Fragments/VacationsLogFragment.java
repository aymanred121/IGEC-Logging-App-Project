package com.igec.admin.Fragments;

import static android.content.ContentValues.TAG;

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

import com.igec.common.Adapters.VacationAdapter;
import com.igec.admin.R;
import com.igec.common.firebase.Employee;
import com.igec.common.firebase.VacationRequest;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;


public class VacationsLogFragment extends Fragment {

    private RecyclerView recyclerView;
    private static VacationAdapter adapter;
    private RecyclerView.LayoutManager layoutManager;

    private ArrayList<VacationRequest> vacations;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

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
        db.collection("Vacation")
                .whereEqualTo("employee.managerID", "adminID")
                .orderBy("requestDate", Query.Direction.DESCENDING)
                .addSnapshotListener((queryDocumentSnapshots, e) -> {
                    if (e != null) {
                        Log.w(TAG, "Listen failed.", e);
                        return;
                    }
                    ArrayList<VacationRequest> vacationRequests = new ArrayList<>();
                    for (DocumentSnapshot vacations : queryDocumentSnapshots) {
                        if (vacations.toObject(VacationRequest.class).getVacationStatus() != 0)
                            vacationRequests.add(vacations.toObject(VacationRequest.class));
                    }
                    adapter.setVacationsList(vacationRequests);
                    adapter.notifyDataSetChanged();
                });

    }

}