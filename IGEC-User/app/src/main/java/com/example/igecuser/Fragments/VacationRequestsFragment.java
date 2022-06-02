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
import com.example.igecuser.R;
import com.example.igecuser.fireBase.Employee;
import com.example.igecuser.fireBase.VacationRequest;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;


public class VacationRequestsFragment extends Fragment {

    private RecyclerView recyclerView;
    private VacationAdapter adapter;
    private RecyclerView.LayoutManager layoutManager;
    private ArrayList<VacationRequest> vacationRequests;
    private final Employee currManager;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private String unPaidDays = "";

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
                        vacationRequests.add(vacations.toObject(VacationRequest.class));
                    }
                    adapter.setVacationsList(vacationRequests);
                    adapter.notifyDataSetChanged();
                });
    }

    private int getDays(VacationRequest vacation) {
        long days = vacation.getEndDate().getTime() - vacation.getStartDate().getTime();
        days /= (24 * 3600 * 1000);
        return (int) days;
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
                int requestedDays = getDays(vacationRequest);
                AlertDialog.Builder editTextBuilder = new AlertDialog.Builder(getActivity());
                final TextInputLayout layout = new TextInputLayout(new ContextThemeWrapper(getActivity(), R.style.Widget_MaterialComponents_TextInputLayout_OutlinedBox));
                FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                layoutParams.height = 250;
                layoutParams.width = FrameLayout.MarginLayoutParams.MATCH_PARENT;
                layoutParams.setMargins(50, 20, 50, 10);
                layout.setLayoutParams(layoutParams);
                layout.setHint("Unpaid Days");
                layout.setBoxBackgroundColor(ContextCompat.getColor(getActivity(), android.R.color.white));
                layout.setBoxBackgroundMode(TextInputLayout.BOX_BACKGROUND_OUTLINE);
                final TextInputEditText editText = new TextInputEditText(layout.getContext());
                editText.setInputType(InputType.TYPE_CLASS_NUMBER);
                editText.setFilters(new InputFilter[] { new InputFilter.LengthFilter(2) });
                editText.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                    }

                    @Override
                    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                    }

                    @Override
                    public void afterTextChanged(Editable editable) {
                        int days;
                        try {
                            days = Integer.parseInt(editable.toString());
                        }catch (Exception e) {
                            days = 0;
                        }
                        if(days > requestedDays)
                            layout.setError("You can't set more than "+ requestedDays + " days");
                        else if (days < 0)
                            layout.setError("You can't set less than 0 days");
                        else {
                            layout.setError(null);
                            layout.setErrorEnabled(false);
                        }
                    }
                });
                layout.addView(editText);
                editTextBuilder.setView(layout);
                editTextBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        unPaidDays = editText.getText().toString();
                        Toast.makeText(getActivity(), unPaidDays, Toast.LENGTH_SHORT).show();
                    }
                });
                editTextBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                editTextBuilder.show();
                layout.setLayoutParams(layoutParams);
                //TODO validate if the entered days are less than days requested in the request
                //TODO unComment the following lines to update the employee vacation days
//                if (employee.getTotalNumberOfVacationDays() - Integer.parseInt(getDays(vacationRequest)) < 0) {
//                    Toast.makeText(getActivity(), "This vacation can't be accepted duo to available days for this Employee", Toast.LENGTH_LONG).show();
//                    return;
//                }
//                db.collection("Vacation")
//                        .document(vacationRequest.getId())
//                        .update("vacationStatus", 1);
//
//                db.collection("employees")
//                        .document(employee.getId())
//                        .update("totalNumberOfVacationDays"
//                                , employee.getTotalNumberOfVacationDays() - Integer.parseInt(getDays(vacationRequest)));


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