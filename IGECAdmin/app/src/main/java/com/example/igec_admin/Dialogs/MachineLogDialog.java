package com.example.igec_admin.Dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.igec_admin.Adatpers.EmployeeAdapter;
import com.example.igec_admin.Adatpers.MachineLogAdapter;
import com.example.igec_admin.R;
import com.example.igec_admin.fireBase.Machine;
import com.example.igec_admin.fireBase.Machine_Employee;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;


public class MachineLogDialog extends DialogFragment {


    private final Machine machine;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private ArrayList<Machine_Employee> machineSummaryData;
    private MachineLogAdapter adapter;
    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;

    public MachineLogDialog(Machine machine) {
        this.machine = machine;
    }

    private void retrieveMachineSummary() {
        db.collection("Machine_Employee").whereEqualTo("machine.id", machine.getId()).get().addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.size() != 0) {
                        machineSummaryData.addAll(queryDocumentSnapshots.toObjects(Machine_Employee.class));
                        adapter.setMachineEmployees(machineSummaryData);
                        adapter.notifyDataSetChanged();
                    }
                }
        );

    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        Window window = dialog.getWindow();

        if (window != null) {
            dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);
        }

        return dialog;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.FullscreenDialogTheme);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_maching_log_dialog, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initialize(view);

    }

    void initialize(View view) {
        machineSummaryData = new ArrayList<>();
        recyclerView = view.findViewById(R.id.recyclerview);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(getActivity());
        adapter = new MachineLogAdapter(machineSummaryData);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
        retrieveMachineSummary();

    }
}