package com.igec.user.fragments;

import static com.igec.common.CONSTANTS.PROJECT_COL;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.igec.user.R;
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
    private Project selectedProject;
    private ArrayList<Project> projects;
    private ArrayAdapter<String> projectIdsAdapter;
    private ArrayList<String> projectsRef;

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
        employeeOverviews = new ArrayList<>();
        projects = new ArrayList<>();
        projectsRef = new ArrayList<>();
        binding.projectAuto.addTextChangedListener(twProject);
        manager = (Employee) getArguments().getSerializable("manager");
        PROJECT_COL.addSnapshotListener((queryDocumentSnapshots, error) -> {
            if (queryDocumentSnapshots == null)
                return;
            projects.clear();
            projectsRef.clear();
            for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                Project project = document.toObject(Project.class);
                if (!manager.getProjectIds().contains(project.getId()))
                    continue;
                projects.add(project);
                projectsRef.add(String.format("IGEC%s | %s", project.getReference(), project.getName()));
            }
            if(projects.size() == 0) return;
            projectIdsAdapter = new ArrayAdapter<>(getActivity(), R.layout.item_dropdown, projectsRef);
            selectedProject = projects.get(0);
            binding.recyclerView.setHasFixedSize(true);
            layoutManager = new LinearLayoutManager(getActivity());
            adapter = new EmployeeAdapter(employeeOverviews, selectedProject);
            binding.recyclerView.setLayoutManager(layoutManager);
            binding.recyclerView.setAdapter(adapter);
            adapter.setOnItemClickListener(itemClickListener);
            binding.projectAuto.setText(String.format("IGEC%s | %s", selectedProject.getReference(), selectedProject.getName()));
            binding.projectAuto.setAdapter(projectIdsAdapter);
            binding.showProjectAllowancesButton.setOnClickListener(oclShowProjectAllowances);
        });

    }

    private final TextWatcher twProject = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void afterTextChanged(Editable editable) {
            for(Project p : projects){
                if(String.format("IGEC%s | %s", p.getReference(), p.getName()).equals(editable.toString())){
                    selectedProject = p;
                    employeeOverviews.clear();
                    employeeOverviews.addAll(p.getEmployees());
                    employeeOverviews.removeIf(employeeOverview -> employeeOverview.isManager);
                    Log.d("ProjectSummaryFragment", "initialize: " + employeeOverviews.size());
                    adapter.notifyDataSetChanged();
                }
            }
        }
    };
    private final View.OnClickListener oclShowProjectAllowances = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            AddAllowanceDialog employeeSummaryDialog = new AddAllowanceDialog(selectedProject, false, true);
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