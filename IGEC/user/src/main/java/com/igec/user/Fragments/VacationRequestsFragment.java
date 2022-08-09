package com.igec.user.Fragments;

import static android.content.ContentValues.TAG;

import android.app.AlertDialog;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.igec.user.Adapters.VacationAdapter;
import com.igec.user.Dialogs.VacationDialog;
import com.igec.user.R;
import com.igec.common.firebase.Allowance;
import com.igec.common.firebase.Employee;
import com.igec.common.firebase.EmployeesGrossSalary;
import com.igec.common.firebase.VacationRequest;
import com.igec.common.utilities.allowancesEnum;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.stream.Collectors;


public class VacationRequestsFragment extends Fragment {

    private Employee employee;
    private int requestedDays;
    private AlertDialog dialog;
    private RecyclerView recyclerView;
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
//    public VacationRequestsFragment(Employee currManager) {
//        this.currManager = currManager;
//    }

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
        currManager= (Employee) getArguments().getSerializable("currManager");
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
                        vacationRequests.add(vacations.toObject(VacationRequest.class));
                    }
                    adapter.setVacationsList(vacationRequests);
                    adapter.notifyDataSetChanged();
                });
    }

    VacationAdapter.OnItemClickListener itclVacationAdapter = position -> {
        VacationDialog vacationDialog = new VacationDialog(vacationRequests.get(position));
        vacationDialog.show(getParentFragmentManager(), "");
    };

}