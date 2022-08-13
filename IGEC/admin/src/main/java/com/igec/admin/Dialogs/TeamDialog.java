package com.igec.admin.Dialogs;

import static android.content.ContentValues.TAG;

import static com.igec.common.CONSTANTS.ADMIN;

import android.app.Dialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.github.javafaker.Team;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;
import com.igec.admin.Adapters.EmployeeAdapter;
import com.igec.admin.R;
import com.igec.admin.databinding.DialogTeamBinding;
import com.igec.common.firebase.EmployeeOverview;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public class TeamDialog extends DialogFragment {

    private EmployeeAdapter adapter;
    private String projectId;
    private EmployeeOverview selectedManager;
    private ArrayList<EmployeeOverview> team;
    private ArrayList<String> teamId;
    private ArrayList<EmployeeOverview> employees;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final DocumentReference employeeOverviewRef = db.collection("EmployeeOverview")
            .document("emp");
    private WriteBatch batch = FirebaseFirestore.getInstance().batch();

    public static TeamDialog newInstance(String projectId) {
        Bundle args = new Bundle();
        args.putString("ProjectId", projectId);
        TeamDialog fragment = new TeamDialog();
        fragment.setArguments(args);
        return fragment;
    }

    public static TeamDialog newInstance(ArrayList<EmployeeOverview> team, String projectId) {

        Bundle args = new Bundle();
        args.putSerializable("team", team);
        args.putSerializable("ProjectId", projectId);
        TeamDialog fragment = new TeamDialog();
        fragment.setArguments(args);
        return fragment;
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

    private DialogTeamBinding binding;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = DialogTeamBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        projectId = getArguments().getString("ProjectId");
        team = (ArrayList<EmployeeOverview>) getArguments().getSerializable("team");
        binding.managerIdLayout.setEnabled(false);
        teamId = new ArrayList<>();
        employees = new ArrayList<>();
        adapter = new EmployeeAdapter(employees, true);
        adapter.setProjectId(projectId);
        binding.recyclerView.setHasFixedSize(true);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        binding.recyclerView.setAdapter(adapter);
        binding.managerIdAuto.addTextChangedListener(twManagerID);
        if (team != null) {
            binding.managerIdLayout.setEnabled(true);
            for (EmployeeOverview emp : team) {
                if (emp.getManagerID().equals(ADMIN))
                    binding.managerIdAuto.setText(emp.getId());
                teamId.add(emp.getId());
            }
            ArrayAdapter<String> idAdapter = new ArrayAdapter<>(getActivity(), R.layout.item_dropdown, teamId);
            binding.managerIdAuto.setAdapter(idAdapter);
        } else
            team = new ArrayList<>();
        adapter.setSelected(teamId);
        getEmployees();

        adapter.setOnItemClickListener(new EmployeeAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {

            }

            @Override
            public void onCheckboxClick(int position) {
                changeSelectedTeam(position);
            }
        });
        binding.doneButton.setOnClickListener(v -> {
            Bundle result = new Bundle();
            result.putSerializable("team", team);
            result.putString("managerId", selectedManager.getId());
            getParentFragmentManager().setFragmentResult("team", result);
            dismiss();
        });
    }

    void getEmployees() {
        employeeOverviewRef
                .addSnapshotListener((documentSnapshot, e) -> {
                    if (e != null) {
                        Log.w(TAG, "Listen failed.", e);
                        return;
                    }

                    if (!documentSnapshot.exists())
                        return;
                    HashMap empMap = (HashMap) documentSnapshot.getData();
                    retrieveEmployees(empMap);
                });
    }

    void retrieveEmployees(Map<String, ArrayList<String>> empMap) {
        employees.clear();
        for (String key : empMap.keySet()) {
            String firstName = empMap.get(key).get(0);
            String lastName = empMap.get(key).get(1);
            String title = empMap.get(key).get(2);
            String managerID = empMap.get(key).get(3);
            String projectID = empMap.get(key).get(4);
            boolean isSelected = empMap.get(key).get(5).equals("1");
            String id = (key);
            /*
            *
                d l
                00 => show
                10 => hide
                01 => doesn't exist
                11 => show
            * */
//            if (teamId.contains(id) == isSelected && managerID == null)
            EmployeeOverview emp = new EmployeeOverview(firstName, lastName, title, id, projectID, isSelected);
            emp.setManagerID(managerID);
            employees.add(emp);

        }
        employees.sort(Comparator.comparing(EmployeeOverview::getId));
        adapter.setEmployeeOverviewsList(employees);
        adapter.notifyDataSetChanged();

    }

    private void changeSelectedTeam(int position) {

        employees.get(position).isSelected = !employees.get(position).isSelected;
        ArrayList<String> empInfo = new ArrayList<>();
        empInfo.add(employees.get(position).getFirstName());
        empInfo.add(employees.get(position).getLastName());
        empInfo.add(employees.get(position).getTitle());
        empInfo.add(null);
        empInfo.add(null);
        batch = FirebaseFirestore.getInstance().batch();
        if (employees.get(position).isSelected) {
//            employees.get(position).setProjectId(projectId);
            team.add(employees.get(position));
            teamId.add(String.valueOf(employees.get(position).getId()));
            empInfo.add("1");
            Map<String, Object> empInfoMap = new HashMap<>();
            empInfoMap.put(employees.get(position).getId(), empInfo);
            if (employees.get(position).getManagerID() == null)
                batch.update(db.collection("EmployeeOverview")
                        .document("emp"), empInfoMap);

        } else {

            if (!binding.managerIdAuto.getText().toString().isEmpty() && binding.managerIdAuto.getText().toString().equals(employees.get(position).getId()))
                binding.managerIdAuto.setText("");
            team.removeIf(employeeOverview -> employeeOverview.getId().equals(employees.get(position).getId()));
            teamId.remove(String.valueOf(employees.get(position).getId()));
//            employees.get(position).setProjectId(null);
            empInfo.add("0");
            Map<String, Object> empInfoMap = new HashMap<>();
            empInfoMap.put(employees.get(position).getId(), empInfo);
            if (employees.get(position).getManagerID() == null)
                batch.update(db.collection("EmployeeOverview")
                        .document("emp"), empInfoMap);
        }
        batch.commit();
        binding.managerIdLayout.setEnabled(team.size() > 0);
        if (selectedManager != null)
            binding.managerIdAuto.setText(selectedManager.getId());
        ArrayAdapter<String> idAdapter = new ArrayAdapter<>(getActivity(), R.layout.item_dropdown, teamId);
        binding.managerIdAuto.setAdapter(idAdapter);
        adapter.notifyItemChanged(position);
    }

    private final TextWatcher twManagerID = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            if (binding.managerIdAuto.getText().length() > 0) {
                if (selectedManager == null || !selectedManager.getId().equals(binding.managerIdAuto.getText().toString())) {
                    for (int i = 0; i < team.size(); i++) {
                        if (String.valueOf(team.get(i).getId()).equals(s.toString())) {
                            selectedManager = team.get(i);
                        }
                    }
                }
                binding.managerNameEdit.setText(String.format("%s %s", selectedManager.getFirstName(), selectedManager.getLastName()));
            } else {
                binding.managerNameEdit.setText(null);
                selectedManager = null;
            }
            for (EmployeeOverview emp : team) {
                if (!emp.getId().equals(binding.managerIdAuto.getText().toString())) {
                    emp.setManagerID(!binding.managerIdAuto.getText().toString().equals("") ? binding.managerIdAuto.getText().toString() : null);
                } else {
                    emp.setManagerID(ADMIN);
                }
            }
            binding.managerIdLayout.setError(null);
            binding.managerIdLayout.setErrorEnabled(false);
        }
    };
}
