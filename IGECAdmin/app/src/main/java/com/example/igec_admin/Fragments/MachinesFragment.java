package com.example.igec_admin.Fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.igec_admin.Adatpers.MachineAdapter;
import com.example.igec_admin.Dialogs.MachineFragmentDialog;
import com.example.igec_admin.Dialogs.MachineLogDialog;
import com.example.igec_admin.R;
import com.example.igec_admin.fireBase.Machine;
import com.example.igec_admin.fireBase.MachineDefectsLog;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class MachinesFragment extends Fragment {
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    CollectionReference machineRef = db.collection("machine");
    ArrayList<Machine> machines = new ArrayList();
    MachineAdapter adapter;
    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_machines,container,false);
        initialize(view);
        adapter.setOnItemClickListener(itclMachineAdapter);
        return view;
    }
    void initialize(View view){
        recyclerView = view.findViewById(R.id.recyclerview);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(getActivity());
        adapter = new MachineAdapter(machines);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
        getMachines();
    }
    void getMachines(){
        machineRef.addSnapshotListener((queryDocumentSnapshots, e) -> {
            machines.clear();
            for(DocumentSnapshot d : queryDocumentSnapshots){
                machines.add(d.toObject(Machine.class));
            }
            adapter.setMachinesList(machines);
            adapter.notifyDataSetChanged();
        });

    }

    private final MachineAdapter.OnItemClickListener itclMachineAdapter = new MachineAdapter.OnItemClickListener() {
        @Override
        public void onItemClick(int position) {
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
            db.collection("MachineDefectsLog").whereEqualTo("machineId",machines.get(position).getId()).addSnapshotListener((values,error)->{
               if(values==null || values.size()==0)
                   return;
                machineDefectsLogArrayList.addAll(values.toObjects(MachineDefectsLog.class));
                for(DocumentSnapshot d: values)
                arrayAdapter.add(d.toObject(MachineDefectsLog.class).getNote());
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
                    MachineDefectsLog currMachineDefectsLog =  machineDefectsLogArrayList.get(which);
                    String strName = "Employee Name: "+currMachineDefectsLog.getEmployeeName()+'\n'
                            +"Machine reference: "+currMachineDefectsLog.getMachineRef()+'\n'
                            +"Issue Date: "+currMachineDefectsLog.getIssueDate();
                    AlertDialog.Builder builderInner = new AlertDialog.Builder(getActivity());
                    builderInner.setMessage(strName);
                    builderInner.setTitle("Content");
                    builderInner.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog,int which) {
                            dialog.dismiss();
                        }
                    });
                    builderInner.show();
                }
            });
            builderSingle.show();
        }
    };
}
