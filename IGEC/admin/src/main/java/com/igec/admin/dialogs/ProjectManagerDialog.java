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
import com.igec.admin.databinding.DialogProjectManagerBinding;
import com.igec.common.firebase.EmployeeOverview;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public class ProjectManagerDialog extends DialogFragment {
    private int lastSelectedPosition = -1;
    private ListenerRegistration task;
    private boolean save = false;
    private EmployeeAdapter adapter;
    private ArrayList<EmployeeOverview> employees;
    private EmployeeOverview unSavedManager;
    private String unSavedManagerId;
    private EmployeeOverview savedManager;
    private String savedManagerId;

    public static ProjectManagerDialog newInstance(EmployeeOverview manager) {
        Bundle args = new Bundle();
        args.putParcelable("manager", manager);
        ProjectManagerDialog fragment = new ProjectManagerDialog();
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


    private DialogProjectManagerBinding binding;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = DialogProjectManagerBinding.inflate(inflater, container, false);
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
            result.putParcelable("manager", unSavedManager);
        } else {
            result.putParcelable("manager", savedManager);
        }
        getParentFragmentManager().setFragmentResult("manager", result);
        task.remove();
    }

    @Override
    public void onStop() {
        super.onStop();
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        savedManager = getArguments().getParcelable("manager");
        if (savedManager != null) {
            try {
                unSavedManager = savedManager.clone();
            } catch (CloneNotSupportedException e) {
                e.printStackTrace();
            }
            savedManagerId = savedManager.getId();
            unSavedManagerId = savedManager.getId();
        }
        employees = new ArrayList<>();
        adapter = new EmployeeAdapter(employees, EmployeeAdapter.Type.manager);
        binding.recyclerView.setAdapter(adapter);
        getManagers();
        adapter.setOnItemClickListener(onItemClickListener);
        binding.doneFab.setOnClickListener(saveListener);
    }

    private void getManagers() {
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

    private void filterEmployees(Map<String, ArrayList<Object>> empMap) {
        employees.clear();
        // filter: no a manager, not currently working
        for (String key : empMap.keySet()) {
            String id = (key);
            String firstName = (String) (empMap.get(key)).get(0);
            String lastName = (String) empMap.get(key).get(1);
            String title = (String) empMap.get(key).get(2);
            String managerID = (String) empMap.get(key).get(3);
            ArrayList<String> projectIds = (ArrayList<String>) ((HashMap) empMap.get(key).get(4)).get("pids");
            boolean isSelected = (Boolean) empMap.get(key).get(5);
            boolean isManager = (Boolean) empMap.get(key).get(6);

            // !isManager = not a manager
            // matchDb = not working employees and the selected employees
            if (isManager) {
                EmployeeOverview emp = new EmployeeOverview();
                emp.setId(id);
                emp.setFirstName(firstName);
                emp.setLastName(lastName);
                emp.setTitle(title);
                emp.setManagerID(managerID);
                emp.setProjectIds(projectIds);
                emp.isSelected = isSelected;
                emp.isManager = true;
                employees.add(emp);
            }
        }
        adapter.setMID(savedManagerId);
        employees.sort(Comparator.comparing(EmployeeOverview::getId));
        for (EmployeeOverview employeeOverview : employees) {
            if (employeeOverview.getId().equals(savedManagerId)) {
                lastSelectedPosition = employees.indexOf(employeeOverview);
                employeeOverview.isSelected = true;
                break;
            }
        }
        adapter.notifyDataSetChanged();
    }

    private EmployeeAdapter.OnItemClickListener onItemClickListener = position -> {
        employees.get(position).isSelected = true;
        if (lastSelectedPosition != -1) {
            employees.get(lastSelectedPosition).isSelected = false;
        }
        unSavedManagerId = employees.get(position).getId();
        unSavedManager = employees.get(position);
        adapter.setMID(employees.get(position).getId());
        adapter.notifyItemChanged(position);
        if (lastSelectedPosition != -1)
            adapter.notifyItemChanged(lastSelectedPosition);
        lastSelectedPosition = position;
    };
    private View.OnClickListener saveListener = v -> {
        save = true;
        dismiss();
    };
}
