package com.example.igec_admin.Fragments;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;

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
import com.example.igec_admin.fireBase.Supplement;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

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
        View view = inflater.inflate(R.layout.fragment_machines, container, false);
        initialize(view);
        adapter.setOnItemClickListener(itclMachineAdapter);
        return view;
    }

    private void initialize(View view) {
        recyclerView = view.findViewById(R.id.recyclerview);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(getActivity());
        adapter = new MachineAdapter(machines);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
        getMachines();
    }

    private void getMachines() {
        machineRef.addSnapshotListener((queryDocumentSnapshots, e) -> {
            machines.clear();
            for (DocumentSnapshot d : queryDocumentSnapshots) {
                machines.add(d.toObject(Machine.class));
            }
            adapter.setMachinesList(machines);
            adapter.notifyDataSetChanged();
        });

    }

    private String convertDateToString(long selection) {
        @SuppressLint("SimpleDateFormat") SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm a");
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(selection);
        return simpleDateFormat.format(calendar.getTime());
    }

    private final MachineAdapter.OnItemClickListener itclMachineAdapter = new MachineAdapter.OnItemClickListener() {
        @Override
        public void onItemClick(int position) {
            getMachineCover(adapter.getMachinesList().get(position));
        }

        private void getMachineCover(Machine machine) {
            StorageReference ref = FirebaseStorage.getInstance().getReference().child("/imgs/" + machine.getId() + String.format("/%s.jpg", "cover"));
            try {
                final File localFile = File.createTempFile("cover", "jpg");
                ref.getFile(localFile).addOnSuccessListener(taskSnapshot -> {
                    Bitmap bitmap = BitmapFactory.decodeFile(localFile.getAbsolutePath());
                    MachineFragmentDialog machineFragmentDialog = new MachineFragmentDialog(machine,bitmap);
                    machineFragmentDialog.show(getParentFragmentManager(), "");
                }).addOnFailureListener(e -> {
                    MachineFragmentDialog machineFragmentDialog = new MachineFragmentDialog(machine,Bitmap.createBitmap(2,2,Bitmap.Config.ARGB_8888));
                    machineFragmentDialog.show(getParentFragmentManager(), "");
                });
            } catch (IOException e) {
                e.printStackTrace();
                MachineFragmentDialog machineFragmentDialog = new MachineFragmentDialog(machine,Bitmap.createBitmap(2,2,Bitmap.Config.ARGB_8888));
                machineFragmentDialog.show(getParentFragmentManager(), "");
            }

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
            db.collection("MachineDefectsLog").whereEqualTo("machineId", machines.get(position).getId()).addSnapshotListener((values, error) -> {
                if (values == null || values.size() == 0) {
                    Toast.makeText(getActivity(), "no comments on that machine", Toast.LENGTH_SHORT).show();
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
