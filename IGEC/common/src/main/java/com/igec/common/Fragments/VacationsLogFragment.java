package com.igec.common.Fragments;

import static android.content.ContentValues.TAG;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.igec.common.Adapters.VacationAdapter;
import com.igec.common.databinding.FragmentVacationsLogBinding;
import com.igec.common.firebase.Employee;
import com.igec.common.firebase.VacationRequest;
import com.igec.common.R;

import java.util.ArrayList;

public class VacationsLogFragment extends Fragment {

    private static VacationAdapter adapter;
    private RecyclerView.LayoutManager layoutManager;

    private ArrayList<VacationRequest> vacations;
    private Employee user;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentVacationsLogBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        loadOwn = false;
        user = (Employee) getArguments().getSerializable("user");
        vacations = new ArrayList<>();

        binding.recyclerview.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(getActivity());
        adapter = new VacationAdapter(vacations);
        binding.recyclerview.setLayoutManager(layoutManager);
        binding.recyclerview.setAdapter(adapter);

        if (user == null)// admin
            loadVacations("employee.managerID", "adminID");
        else if (user.getManagerID().equals("adminID")) // manager
        {
            loadOwn = true;
            loadVacations("manager.id", user.getId());
        } else // employee
        {
            loadVacations("employee.id", user.getId());
        }
    }


    private void loadVacations(String who, String id) {
        db.collection("Vacation")
                .whereEqualTo(who, id)
                .orderBy("requestDate", Query.Direction.DESCENDING)
                .addSnapshotListener((queryDocumentSnapshots, e) -> {
                    if (e != null) {
                        Log.w(TAG, "Listen failed.", e);
                        return;
                    }
                    vacations.clear();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        if (doc.toObject(VacationRequest.class).getVacationStatus() != 0)
                            vacations.add(doc.toObject(VacationRequest.class));
                    }
                    adapter.notifyDataSetChanged();
                    if (loadOwn) {
                        loadOwnVacations();
                    }
                });
    }

    // to avoid recursion
    private void loadOwnVacations() {
        db.collection("Vacation")
                .whereEqualTo("employee.id", user.getId())
                .orderBy("requestDate", Query.Direction.DESCENDING)
                .addSnapshotListener((queryDocumentSnapshots, e) -> {
                    if (e != null) {
                        Log.w(TAG, "Listen failed.", e);
                        return;
                    }
                    // loads his employees vacations
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        if (doc.toObject(VacationRequest.class).getVacationStatus() != 0)
                            vacations.add(doc.toObject(VacationRequest.class));
                    }
                    adapter.notifyDataSetChanged();
                });
    }

}