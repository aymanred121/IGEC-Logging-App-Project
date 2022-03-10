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

import com.example.igecuser.R;
import com.example.igecuser.Adapters.VacationAdapter;
import com.example.igecuser.fireBase.Employee;
import com.example.igecuser.fireBase.VacationRequest;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Collections;


public class VacationsLogFragment extends Fragment {

    private RecyclerView recyclerView;
    private static VacationAdapter adapter;
    private RecyclerView.LayoutManager layoutManager;

    private ArrayList<VacationRequest> vacations = new ArrayList<>();
    private static Employee user;
    private static String query;
    private final boolean isEmployee;
    private static FirebaseFirestore db = FirebaseFirestore.getInstance();

    public VacationsLogFragment(boolean isEmployee) {
        this.isEmployee = isEmployee;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_vacations_log, container, false);
        Initialize(view);

        // Inflate the layout for this fragment
        return view;
    }
    // Functions
    private void Initialize(View view) {
        recyclerView = view.findViewById(R.id.recyclerview);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(getActivity());
        user= (Employee) getArguments().getSerializable((isEmployee)?"emp":"mgr");
        query = (isEmployee)?"employee.id":"manager.id";
        adapter = new VacationAdapter(vacations);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
        loadVacations();
    }
    public static void loadVacations(){

        db.collection("Vacation")
                .whereEqualTo(query,user.getId())
                .whereNotEqualTo("vacationStatus",0)
                .addSnapshotListener((queryDocumentSnapshots, e) -> {
                    if (e != null) {
                        Log.w(TAG, "Listen failed.", e);
                        return;
                    }
                    ArrayList<VacationRequest>vacationRequests=new ArrayList<>();
                    for(DocumentSnapshot vacations: queryDocumentSnapshots){
                        vacationRequests.add(vacations.toObject(VacationRequest.class));
                    }
                    Collections.reverse(vacationRequests);
                    adapter.setVacationsList(vacationRequests);
                    adapter.notifyDataSetChanged();
                });
    }

}