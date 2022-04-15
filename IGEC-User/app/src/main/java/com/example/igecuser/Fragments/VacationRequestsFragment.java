package com.example.igecuser.Fragments;

import static android.content.ContentValues.TAG;

import android.app.AlertDialog;
import android.content.DialogInterface;
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

import com.example.igecuser.Adapters.VacationAdapter;
import com.example.igecuser.R;
import com.example.igecuser.fireBase.Employee;
import com.example.igecuser.fireBase.VacationRequest;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.Objects;


public class VacationRequestsFragment extends Fragment {

    private RecyclerView recyclerView;
    private VacationAdapter adapter;
    private RecyclerView.LayoutManager layoutManager;
    private ArrayList<VacationRequest> vacationRequests;
    private final Employee currManager;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public VacationRequestsFragment(Employee currManager) {
        this.currManager = currManager;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_vacation_requests, container, false);

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initialize(view);
        adapter.setOnItemClickListener(itclVacationAdapter);

    }

    // Functions
    private void initialize(View view) {
        vacationRequests = new ArrayList<>();
        recyclerView = view.findViewById(R.id.recyclerview);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(getActivity());
        adapter = new VacationAdapter(vacationRequests);
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
                    vacationRequests.clear();
                    for (DocumentSnapshot vacations : queryDocumentSnapshots) {
                        //TODO filter passed out vacations
                        vacationRequests.add(vacations.toObject(VacationRequest.class));
                    }
                    adapter.setVacationsList(vacationRequests);
                    adapter.notifyDataSetChanged();
                });
    }

    private String getDays(VacationRequest vacation) {
        long days = vacation.getEndDate().getTime() - vacation.getStartDate().getTime();
        days /= (24 * 3600 * 1000);
        return String.valueOf(days);
    }

    VacationAdapter.OnItemClickListener itclVacationAdapter = position -> {

        VacationRequest vacationRequest = vacationRequests.get(position);
        String content = vacationRequest.getVacationNote();
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(content);
        builder.setTitle("Note");
        builder.setPositiveButton("Accept", (dialogInterface, i) -> {
            db.collection("employees").document(vacationRequest.getEmployee().getId()).get().addOnSuccessListener((value) -> {
                Employee employee = value.toObject(Employee.class);
                if (employee.getTotalNumberOfVacationDays() - Integer.parseInt(getDays(vacationRequest)) < 0) {
                    Toast.makeText(getActivity(), "This vacation can't be accepted duo to available days for this Employee", Toast.LENGTH_LONG).show();
                    return;
                }
                db.collection("Vacation")
                        .document(vacationRequest.getId())
                        .update("vacationStatus", 1);

                db.collection("employees")
                        .document(employee.getId())
                        .update("totalNumberOfVacationDays"
                                , employee.getTotalNumberOfVacationDays() - Integer.parseInt(getDays(vacationRequest)));


            });
        });
        builder.setNegativeButton("Reject", (dialogInterface, i) -> {
            db.collection("Vacation")
                    .document(vacationRequest.getId())
                    .update("vacationStatus", -1);
        });
        builder.setNeutralButton("Cancel", (dialogInterface, i) -> {
        });
        builder.show();

    };

}