package com.igec.admin.fragments;

import static com.igec.common.CONSTANTS.MACHINE_COL;
import static com.igec.common.CONSTANTS.MACHINE_DEFECT_LOG_COL;
import static com.igec.common.CONSTANTS.convertDateToString;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.igec.admin.adapters.MachineAdapter;
import com.igec.admin.databinding.FragmentMachinesBinding;
import com.igec.admin.dialogs.MachineFragmentDialog;
import com.igec.admin.dialogs.MachineLogDialog;
import com.igec.common.firebase.Machine;
import com.igec.common.firebase.MachineDefectsLog;
import com.google.firebase.firestore.DocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class MachinesFragment extends Fragment {
    ArrayList<Machine> machines;
    MachineAdapter adapter;
    RecyclerView.LayoutManager layoutManager;
    private FragmentMachinesBinding binding;
    private boolean opened;

    public void setOpened(boolean opened) {
        this.opened = opened;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentMachinesBinding.inflate(inflater, container, false);
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
        adapter.setOnItemClickListener(itclMachineAdapter);
    }

    private void initialize() {
        machines = new ArrayList<>();
        binding.recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(getActivity());
        adapter = new MachineAdapter(machines);
        binding.recyclerView.setLayoutManager(layoutManager);
        binding.recyclerView.setAdapter(adapter);
        getMachines();
    }

    private void getMachines() {
        MACHINE_COL.addSnapshotListener((queryDocumentSnapshots, e) -> {
            machines.clear();
            for (DocumentSnapshot d : queryDocumentSnapshots) {
                machines.add(d.toObject(Machine.class));
            }
            adapter.setMachinesList(machines);
            adapter.notifyDataSetChanged();
        });

    }



    private final MachineAdapter.OnItemClickListener itclMachineAdapter = new MachineAdapter.OnItemClickListener() {
        @RequiresApi(api = Build.VERSION_CODES.R)
        @Override
        public void onItemClick(int position) {
            if(opened) return;
            opened = true;
            MachineFragmentDialog machineFragmentDialog = new MachineFragmentDialog(adapter.getMachinesList().get(position));
            machineFragmentDialog.show(getParentFragmentManager(), "");
        }

        @Override
        public void onLogClick(int position) {
            MachineLogDialog machineLogDialog = new MachineLogDialog(machines.get(position));
            machineLogDialog.show(getParentFragmentManager(), "");
        }

        @Override
        public void onCommentsClick(int position) {
            AlertDialog.Builder builderSingle = new AlertDialog.Builder(getActivity());
            builderSingle.setTitle("Comments: ");
            final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_selectable_list_item);
            ArrayList<MachineDefectsLog> machineDefectsLogArrayList = new ArrayList<>();
            MACHINE_DEFECT_LOG_COL.whereEqualTo("machineId", machines.get(position).getId()).addSnapshotListener((values, error) -> {
                if (values == null || values.size() == 0) {
                    Snackbar.make(binding.getRoot(), "no comments on that machine", Snackbar.LENGTH_SHORT).show();
                    return;
                }
                machineDefectsLogArrayList.addAll(values.toObjects(MachineDefectsLog.class));
                for (DocumentSnapshot d : values)
                    arrayAdapter.add("Issue Date: " + convertDateToString(d.toObject(MachineDefectsLog.class).getIssueDate().getTime()));
                builderSingle.show();
            });
            builderSingle.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });

            builderSingle.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    MachineDefectsLog currMachineDefectsLog = machineDefectsLogArrayList.get(which);
                    String content = "Employee ID: " + currMachineDefectsLog.getEmployeeId() + '\n'
                            + "Employee Name: " + currMachineDefectsLog.getEmployeeName() + '\n'
                            + "Machine reference: " + currMachineDefectsLog.getMachineRef() + '\n'
                            + "Comment: " + currMachineDefectsLog.getNote();
                    AlertDialog.Builder builderInner = new AlertDialog.Builder(getActivity());
                    builderInner.setMessage(content);
                    builderInner.setTitle("Content");
                    builderInner.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    builderInner.show();
                }
            });

        }
    };
}
