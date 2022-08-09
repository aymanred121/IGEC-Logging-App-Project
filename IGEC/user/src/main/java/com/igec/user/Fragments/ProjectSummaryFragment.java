package com.igec.user.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.igec.user.Adapters.EmployeeAdapter;
import com.igec.user.Dialogs.AddAllowanceDialog;
import com.igec.user.R;
import com.igec.common.firebase.Allowance;
import com.igec.common.firebase.Employee;
import com.igec.common.firebase.EmployeeOverview;
import com.igec.common.firebase.EmployeesGrossSalary;
import com.igec.common.firebase.Project;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class ProjectSummaryFragment extends Fragment {

    private RecyclerView recyclerView;
    private EmployeeAdapter adapter;
    private RecyclerView.LayoutManager layoutManager;
    private ArrayList<EmployeeOverview> employeeOverviews;
    private Employee manager;
    private Project project;
    private ArrayList<Allowance> projectAllowance;
    private TextInputEditText vProjectName, vProjectReference;
    private MaterialButton vShowProjectAllowances;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public static ProjectSummaryFragment newInstance(Employee manager) {
        Bundle args = new Bundle();
        args.putSerializable("manager", manager);
        ProjectSummaryFragment fragment = new ProjectSummaryFragment();
        fragment.setArguments(args);
        return fragment;
    }
//    public ProjectSummaryFragment(Employee manager) {
//        this.manager = manager;
//    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_project_summary, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initialize(view);

    }


    // Functions
    private void initialize(View view) {
        manager = (Employee) getArguments().getSerializable("manager");
        db.collection("projects").document(manager.getProjectID()).addSnapshotListener((documentSnapshot, error) -> {
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
            vProjectName = view.findViewById(R.id.TextInput_ProjectName);
            vProjectReference = view.findViewById(R.id.TextInput_ProjectReference);
            vShowProjectAllowances = view.findViewById(R.id.Button_ShowProjectAllowances);
            recyclerView = view.findViewById(R.id.recyclerview);
            recyclerView.setHasFixedSize(true);
            layoutManager = new LinearLayoutManager(getActivity());
            adapter = new EmployeeAdapter(employeeOverviews, project);
            recyclerView.setLayoutManager(layoutManager);
            recyclerView.setAdapter(adapter);
            vProjectName.setText(project.getName());
            vProjectReference.setText(project.getReference());
            adapter.setOnItemClickListener(itemClickListener);
            vShowProjectAllowances.setOnClickListener(oclShowProjectAllowances);
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