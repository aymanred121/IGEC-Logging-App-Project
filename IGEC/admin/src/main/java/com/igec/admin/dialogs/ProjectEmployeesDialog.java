package com.igec.admin.dialogs;

import static com.igec.common.CONSTANTS.EMPLOYEE_OVERVIEW_REF;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.firebase.firestore.ListenerRegistration;
import com.igec.admin.R;
import com.igec.admin.adapters.EmployeeAdapter;
import com.igec.admin.databinding.DialogProjectEmployeesBinding;
import com.igec.common.firebase.EmployeeOverview;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public class ProjectEmployeesDialog extends DialogFragment {
    private boolean save = false, loaded = false;
    private ListenerRegistration task;
    private EmployeeAdapter adapter;
    private ArrayList<EmployeeOverview> employees;
    private ArrayList<EmployeeOverview> unSavedTeamMembers;
    private ArrayList<String> unSavedTeamMembersIds;
    private ArrayList<EmployeeOverview> savedTeamMembers;
    private ArrayList<String> savedTeamMembersIds;

    public static ProjectEmployeesDialog newInstance(ArrayList<EmployeeOverview> team) {

        Bundle args = new Bundle();
        args.putParcelableArrayList("teamMembers", team);
        ProjectEmployeesDialog fragment = new ProjectEmployeesDialog();
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

    private DialogProjectEmployeesBinding binding;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = DialogProjectEmployeesBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        binding = null;
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        Bundle result = new Bundle();
        if (save) {
            result.putParcelableArrayList("teamMembers", unSavedTeamMembers);
            getParentFragmentManager().setFragmentResult("teamMembers", result);
        } else {
            result.putParcelableArrayList("teamMembers", savedTeamMembers);
            getParentFragmentManager().setFragmentResult("teamMembers", result);
            clearTeam();
        }
        task.remove();
    }


    @Override
    public void onStop() {
        super.onStop();
        if (!save)
            clearTeam();
    }

    private void clearTeam() {
        Map<String, Object> empInfoMap = new HashMap<>();
        for (EmployeeOverview employee : employees) {
            employee.isSelected = false;
            ArrayList<String> empInfo = new ArrayList<>();
            empInfo.add(employee.getFirstName());
            empInfo.add(employee.getLastName());
            empInfo.add(employee.getTitle());
            empInfo.add(null);
            empInfo.add(null);
            empInfo.add(savedTeamMembersIds.contains(employee.getId()) ? "1" : "0");
            empInfo.add("0");
            empInfoMap.put(employee.getId(), empInfo);
            EMPLOYEE_OVERVIEW_REF.update(empInfoMap);
        }
        unSavedTeamMembers.clear();
        unSavedTeamMembersIds.clear();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        unSavedTeamMembersIds = new ArrayList<>();
        unSavedTeamMembers = new ArrayList<>();
        savedTeamMembers = getArguments().getParcelableArrayList("teamMembers");
        savedTeamMembersIds = new ArrayList<>();
        for (EmployeeOverview employee : savedTeamMembers) {
            unSavedTeamMembersIds.add(employee.getId());
            savedTeamMembersIds.add(employee.getId());
        }
        employees = new ArrayList<>();
        adapter = new EmployeeAdapter(employees, EmployeeAdapter.Type.employee);
        binding.recyclerView.setAdapter(adapter);
        getEmployees();
        adapter.setOnItemClickListener(onItemClickListener);
        binding.doneFab.setOnClickListener(saveListener);
    }

    private void getEmployees() {
        task = EMPLOYEE_OVERVIEW_REF
                .addSnapshotListener((documentSnapshot, e) -> {
                    if (e != null) {
                        return;
                    }
                    if (!documentSnapshot.exists())
                        return;
                    HashMap empMap = (HashMap) documentSnapshot.getData();
                    filterEmployees(empMap);
                });
    }

    private void filterEmployees(Map<String, ArrayList<String>> empMap) {
        employees.clear();
        // filter: no a manager, not currently working
        for (String key : empMap.keySet()) {
            String id = (key);
            String firstName = empMap.get(key).get(0);
            String lastName = empMap.get(key).get(1);
            String title = empMap.get(key).get(2);
            String managerID = empMap.get(key).get(3);
            String projectID = empMap.get(key).get(4);
            boolean isSelected = empMap.get(key).get(5).equals("1");
            boolean isManager = empMap.get(key).get(6).equals("1");
            boolean matchDb = (unSavedTeamMembersIds.contains(id) == isSelected);

            // !isManager = not a manager
            // matchDb = not working employees and the selected employees
            if (!isManager && matchDb) {
                EmployeeOverview emp = new EmployeeOverview();
                emp.setId(id);
                emp.setFirstName(firstName);
                emp.setLastName(lastName);
                emp.setTitle(title);
                emp.setManagerID(managerID);
                emp.setProjectId(projectID);
                emp.isSelected = isSelected;
                emp.isManager = false;
                employees.add(emp);
                if (!loaded && isSelected) {
                    unSavedTeamMembers.add(emp);
                }
            }
        }
        employees.sort(Comparator.comparing(EmployeeOverview::getId));
        loaded = true;
        adapter.notifyDataSetChanged();
    }

    private EmployeeAdapter.OnItemClickListener onItemClickListener = position -> {
        employees.get(position).isSelected = !employees.get(position).isSelected;
        adapter.notifyItemChanged(position);
        Map<String, Object> empInfoMap = new HashMap<>();
        ArrayList<String> empInfo = new ArrayList<>();
        empInfo.add(employees.get(position).getFirstName());
        empInfo.add(employees.get(position).getLastName());
        empInfo.add(employees.get(position).getTitle());
        empInfo.add(null);
        empInfo.add(null);

        if (employees.get(position).isSelected) {
            // add to team and team ids
            unSavedTeamMembers.add(employees.get(position));
            unSavedTeamMembersIds.add(employees.get(position).getId());
            // update isSelected in db
            empInfo.add("1");
        } else {
            // remove from team and team ids
            unSavedTeamMembers.remove(employees.get(position));
            unSavedTeamMembersIds.remove(employees.get(position).getId());
            // update isSelected in db
            empInfo.add("0");
        }
        empInfo.add("0");
        empInfoMap.put(employees.get(position).getId(), empInfo);
        EMPLOYEE_OVERVIEW_REF.update(empInfoMap);
    };
    private View.OnClickListener saveListener = v -> {
        save = true;
        dismiss();
    };

}
