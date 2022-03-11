package com.example.igecuser.Fragments;

import static android.content.ContentValues.TAG;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.igecuser.Dialogs.VacationRequestFragmentDialog;
import com.example.igecuser.R;
import com.example.igecuser.Adapters.VacationAdapter;
import com.example.igecuser.fireBase.Employee;
import com.example.igecuser.fireBase.VacationRequest;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;


public class VacationRequestsFragment extends Fragment {

    private RecyclerView recyclerView;
    private VacationAdapter adapter;
    private RecyclerView.LayoutManager layoutManager;
    private ArrayList<VacationRequest> vacations = new ArrayList<>();
    private Employee currManager;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    public VacationRequestsFragment(Employee currManager) {
        this.currManager = currManager;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_vacation_requests, container, false);
        Initialize(view);

        adapter.setOnItemClickListener(itclVacationAdapter);

        return view;
    }

    // Functions
    private void Initialize(View view) {

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
                .whereEqualTo("manager.id", currManager.getId())
                .whereEqualTo("vacationStatus", 0)
                .addSnapshotListener((queryDocumentSnapshots, e) -> {
                    if (e != null) {
                        Log.w(TAG, "Listen failed.", e);
                        return;
                    }
                    ArrayList<VacationRequest> vacationRequests = new ArrayList<>();
                    for (DocumentSnapshot vacations : queryDocumentSnapshots) {
                        vacationRequests.add(vacations.toObject(VacationRequest.class));
                    }
                    adapter.setVacationsList(vacationRequests);
                    adapter.notifyDataSetChanged();
                });
    }

    VacationAdapter.OnItemClickListener itclVacationAdapter = position -> {
        VacationRequestFragmentDialog vacationRequestFragmentDialog = new VacationRequestFragmentDialog(adapter.getVacationsList().get(position));
        vacationRequestFragmentDialog.show(getFragmentManager(), "");
    };

}