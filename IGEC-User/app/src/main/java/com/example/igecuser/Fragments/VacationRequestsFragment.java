package com.example.igecuser.Fragments;

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

import com.example.igecuser.Adapters.VacationAdapter;
import com.example.igecuser.Dialogs.VacationDialog;
import com.example.igecuser.R;
import com.example.igecuser.fireBase.Allowance;
import com.example.igecuser.fireBase.Employee;
import com.example.igecuser.fireBase.EmployeesGrossSalary;
import com.example.igecuser.fireBase.VacationRequest;
import com.example.igecuser.utilites.allowancesEnum;
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
    private final Employee currManager;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private int unPaidDays;

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
    //TODO use when accepting the vacation but with lesser amount of days
    // TODO move it to VacationDialog
    private void updateVacationEndDate(VacationRequest vacationRequest,int vacationDays){
        Calendar c =Calendar.getInstance();
        Date requestStartDate = vacationRequest.getStartDate();
        Date requestEndDate = vacationRequest.getEndDate();
        c.setTime(requestStartDate);
        c.add(Calendar.DATE,vacationDays);
        Date newEndDate = c.getTime();
        vacationRequest.setEndDate(newEndDate);
        vacationRequest.setVacationStatus(1);
        db.collection("Vacation").document(vacationRequest.getId())
                .set(vacationRequest,SetOptions.merge());
        db.collection("employees").document(vacationRequest.getEmployee().getId())
                .update("totalNumberOfVacationDays",FieldValue.increment(-vacationDays));
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
        vacationDialog.show(getParentFragmentManager(),"");
    };

}