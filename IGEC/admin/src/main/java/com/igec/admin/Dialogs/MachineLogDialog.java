package com.igec.admin.Dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.igec.admin.Adapters.MachineLogAdapter;
import com.igec.admin.R;
import com.igec.admin.databinding.DialogMachineLogBinding;
import com.igec.common.firebase.Machine;
import com.igec.common.firebase.Machine_Employee;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;


public class MachineLogDialog extends DialogFragment {


    private final Machine machine;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private ArrayList<Machine_Employee> machineSummaryData;
    private MachineLogAdapter adapter;
    private RecyclerView.LayoutManager layoutManager;

    public MachineLogDialog(Machine machine) {
        this.machine = machine;
    }

    private void retrieveMachineSummary() {
        db.collection("Machine_Employee").whereEqualTo("machine.id", machine.getId()).get().addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.size() != 0) {
                        machineSummaryData.addAll(queryDocumentSnapshots.toObjects(Machine_Employee.class));
                        adapter.setMachineEmployees(machineSummaryData);
                        binding.stateText.setVisibility(View.GONE);
                        adapter.notifyDataSetChanged();
                    }
                    else
                    {
                        binding.stateText.setVisibility(View.VISIBLE);
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

    private DialogMachineLogBinding binding;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = DialogMachineLogBinding.inflate(inflater,container,false);
        return binding.getRoot();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        binding = null;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initialize();
    }

    void initialize() {
        machineSummaryData = new ArrayList<>();
        binding.recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(getActivity());
        adapter = new MachineLogAdapter(machineSummaryData);
        binding.recyclerView.setLayoutManager(layoutManager);
        binding.recyclerView.setAdapter(adapter);
        retrieveMachineSummary();

    }
}