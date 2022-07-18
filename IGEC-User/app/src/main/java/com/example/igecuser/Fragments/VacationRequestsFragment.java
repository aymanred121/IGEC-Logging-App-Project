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

import java.util.ArrayList;
import java.util.Calendar;
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

    private void showUnpaidDialog(int requestedDays, VacationRequest vacationRequest) {
        AlertDialog.Builder editTextBuilder = new AlertDialog.Builder(getActivity());
        final TextInputLayout layout = new TextInputLayout(new ContextThemeWrapper(getActivity(), R.style.Widget_MaterialComponents_TextInputLayout_OutlinedBox));
        layout.setEndIconMode(TextInputLayout.END_ICON_CLEAR_TEXT);
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        layoutParams.height = 250;
        layoutParams.width = FrameLayout.MarginLayoutParams.MATCH_PARENT;
        layoutParams.setMargins(50, 20, 50, 10);
        layout.setLayoutParams(layoutParams);
        layout.setHint("Unpaid Days");
        layout.setBoxBackgroundColor(ContextCompat.getColor(getActivity(), android.R.color.white));
        layout.setBoxBackgroundMode(TextInputLayout.BOX_BACKGROUND_OUTLINE);
        final TextInputEditText editText = new TextInputEditText(layout.getContext());
        int allowed = requestedDays - employee.getTotalNumberOfVacationDays();
        editText.setText(String.format("%d", allowed));
        editText.setInputType(InputType.TYPE_CLASS_NUMBER);
        editText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(2)});
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {

                try {
                    unPaidDays = Integer.parseInt(editable.toString());
                } catch (Exception e) {
                    unPaidDays = 0;
                }
                int allowed = requestedDays - employee.getTotalNumberOfVacationDays();
                if (unPaidDays > requestedDays)
                    layout.setError(String.format("You can't set more than %d days", requestedDays));
                else if (allowed > 0 && unPaidDays < allowed)
                    layout.setError(String.format("You must set an unPaid Days with at least %d days", allowed));
                else {
                    layout.setError(null);
                    layout.setErrorEnabled(false);
                }


                ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(layout.getError() == null);


            }
        });
        layout.addView(editText);
        editTextBuilder.setView(layout);
        editTextBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                db.collection("Vacation")
                        .document(vacationRequest.getId())
                        .update("vacationStatus", 1);
                db.collection("employees")
                        .document(employee.getId())
                        .update("totalNumberOfVacationDays"
                                , employee.getTotalNumberOfVacationDays() - (requestedDays - unPaidDays));
                final Calendar today = Calendar.getInstance();
                int sameMonth = today.get(Calendar.MONTH) + 1, nextMonth = today.get(Calendar.MONTH) + 2;
                String day, month, year;
                year = String.valueOf(today.get(Calendar.YEAR));
                month = String.format("%02d", sameMonth);
                day = String.format("%02d", today.get(Calendar.DAY_OF_MONTH));
                if (today.get(Calendar.DAY_OF_MONTH) > 25) {
                    if (nextMonth > 12) {
                        year = String.valueOf(today.get(Calendar.YEAR) + 1);
                        month = String.format("%02d", today.get(Calendar.JANUARY));
                    } else {
                        month = String.format("%02d", nextMonth);
                    }
                }
                double vacationCost = -((vacationRequest.getEmployee().getSalary() / 30.0) * unPaidDays);
                Allowance cost = new Allowance();
                cost.setName("unpaid " + unPaidDays + " days ");
                cost.setAmount(vacationCost);
                cost.setType(allowancesEnum.PENALTY.ordinal());
                cost.setProjectId(vacationRequest.getEmployee().getProjectID());
                cost.setNote(String.format("%d", unPaidDays));
                db.collection("EmployeesGrossSalary")
                        .document(vacationRequest.getEmployee().getId())
                        .collection(year)
                        .document(month)
                        .get().addOnSuccessListener(doc->{
                    if(!doc.exists()){
                        //new month
                        db.collection("EmployeesGrossSalary").document(vacationRequest.getEmployee().getId()).get().addOnSuccessListener(documentSnapshot -> {
                            if (!documentSnapshot.exists()) return;
                            EmployeesGrossSalary employeesGrossSalary = documentSnapshot.toObject(EmployeesGrossSalary.class);
                            employeesGrossSalary.setBaseAllowances(employeesGrossSalary.getAllTypes().stream().filter(x->x.getType()==allowancesEnum.PROJECT.ordinal()).collect(Collectors.toCollection(ArrayList::new)));
                            employeesGrossSalary.getAllTypes().removeIf(x->x.getType()==allowancesEnum.PROJECT.ordinal());
                            employeesGrossSalary.getBaseAllowances().add(cost);
                            db.document(doc.getReference().getPath()).set(employeesGrossSalary, SetOptions.merge());
                        });
                        return;
                    }
                    db.document(doc.getReference().getPath()).update("allTypes",FieldValue.arrayUnion(cost));
                        });


            }
        });
        editTextBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        dialog = editTextBuilder.create();
        dialog.show();
        layout.setLayoutParams(layoutParams);

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
                employee = value.toObject(Employee.class);
                requestedDays = getDays(vacationRequest);
                showUnpaidDialog(requestedDays, vacationRequest);


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