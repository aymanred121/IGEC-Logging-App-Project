package com.igec.user.fragments;

import static com.igec.common.CONSTANTS.PROJECT_COL;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.igec.user.activities.DateInaccurate;
import com.igec.user.adapters.EmployeeAdapter;
import com.igec.user.databinding.FragmentProjectSummaryBinding;
import com.igec.user.dialogs.AddAllowanceDialog;
import com.igec.common.firebase.Allowance;
import com.igec.common.firebase.Employee;
import com.igec.common.firebase.EmployeeOverview;
import com.igec.common.firebase.Project;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class ProjectSummaryFragment extends Fragment {

    private EmployeeAdapter adapter;
    private RecyclerView.LayoutManager layoutManager;
    private ArrayList<EmployeeOverview> employeeOverviews;
    private Employee manager;
    private Project project;
    private ArrayList<Allowance> projectAllowance;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public static ProjectSummaryFragment newInstance(Employee manager) {
        Bundle args = new Bundle();
        args.putSerializable("manager", manager);
        ProjectSummaryFragment fragment = new ProjectSummaryFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    private FragmentProjectSummaryBinding binding;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentProjectSummaryBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        binding = null;
    }
    @Override
    public void onResume() {
        super.onResume();
        validateDate(getActivity());
    }
    private void validateDate(Context c) {
        if (Settings.Global.getInt(c.getContentResolver(), Settings.Global.AUTO_TIME, 0) != 1) {
            Intent intent = new Intent(getActivity(), DateInaccurate.class);
            startActivity(intent);
            getActivity().finish();
        }
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initialize();
    }


    // Functions
    private void initialize() {
        manager = (Employee) getArguments().getSerializable("manager");
        PROJECT_COL.document(manager.getProjectID()).addSnapshotListener((documentSnapshot, error) -> {
            if (!documentSnapshot.exists())
                return;
            project = documentSnapshot.toObject(Project.class);

            employeeOverviews = new ArrayList<>();
            employeeOverviews = project.getEmployees();
            for (int i = 0; i < employeeOverviews.size(); i++) {
                if (employeeOverviews.get(i).getId().equals(project.getManagerID())) {
                    employeeOverviews.remove(i);
                    break;
                }
            }
            projectAllowance = new ArrayList<>();
            projectAllowance.addAll(project.getAllowancesList());
            binding.recyclerView.setHasFixedSize(true);
            layoutManager = new LinearLayoutManager(getActivity());
            adapter = new EmployeeAdapter(employeeOverviews, project);
            binding.recyclerView.setLayoutManager(layoutManager);
            binding.recyclerView.setAdapter(adapter);
            binding.projectNameEdit.setText(project.getName());
            binding.projectReferenceEdit.setText(project.getReference());
            adapter.setOnItemClickListener(itemClickListener);
            binding.showProjectAllowancesButton.setOnClickListener(oclShowProjectAllowances);
        });

    }

    private final View.OnClickListener oclShowProjectAllowances = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            AddAllowanceDialog employeeSummaryDialog = new AddAllowanceDialog(manager, (ArrayList<Allowance>) projectAllowance.clone(), false, true);
            employeeSummaryDialog.show(getParentFragmentManager(), "");
        }
    };
    private final EmployeeAdapter.OnItemClickListener itemClickListener = new EmployeeAdapter.OnItemClickListener() {
        @Override
        public void onItemClick(int position) {
            AddAllowanceDialog employeeSummaryDialog = new AddAllowanceDialog(employeeOverviews.get(position), true, true);
            employeeSummaryDialog.show(getParentFragmentManager(), "");
        }
    };
}