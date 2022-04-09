package com.example.igecuser.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.igecuser.Adapters.EmployeeAdapter;
import com.example.igecuser.Dialogs.AddAllowanceDialog;
import com.example.igecuser.R;
import com.example.igecuser.fireBase.Allowance;
import com.example.igecuser.fireBase.Employee;
import com.example.igecuser.fireBase.EmployeeOverview;
import com.example.igecuser.fireBase.EmployeesGrossSalary;
import com.example.igecuser.fireBase.Project;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class ProjectSummaryFragment extends Fragment {

    private RecyclerView recyclerView;
    private EmployeeAdapter adapter;
    private RecyclerView.LayoutManager layoutManager;
    private ArrayList<EmployeeOverview> employeeOverviews;
    private final Employee manager;
    private Project project;
    private ArrayList<Allowance> projectAllowance;
    private TextInputEditText vProjectName, vProjectReference;
    private MaterialButton vUpdate, vShowProjectAllowances;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public ProjectSummaryFragment(com.example.igecuser.fireBase.Employee manager) {
        this.manager = manager;
    }

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

        db.collection("projects").document(manager.getProjectID()).get().addOnSuccessListener(documentSnapshot -> {
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
            vUpdate = view.findViewById(R.id.Button_Update);
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
            vUpdate.setOnClickListener(oclUpdate);
            vShowProjectAllowances.setOnClickListener(oclShowProjectAllowances);
        });

    }

    private final View.OnClickListener oclUpdate = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            //TODO update project list allowances and users allowances
            project.setAllowancesList(projectAllowance);
            db.collection("projects").document(manager.getProjectID()).update("allowancesList", projectAllowance).addOnSuccessListener(unused -> {
                employeeOverviews.forEach(employeeOverview -> {
                    db.collection("EmployeesGrossSalary").document(employeeOverview.getId()).update("projectAllowances", projectAllowance);
                });
            });

        }
    };
    private final View.OnClickListener oclShowProjectAllowances = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            AddAllowanceDialog employeeSummaryDialog = new AddAllowanceDialog(projectAllowance, false, true);
            employeeSummaryDialog.show(getParentFragmentManager(), "");
        }
    };
    private final EmployeeAdapter.OnItemClickListener itemClickListener = new EmployeeAdapter.OnItemClickListener() {
        @Override
        public void onItemClick(int position) {
            //TODO employee gross salary call AddAllowanceDialog with the arraylist WITHOUT NET SALARY
            db.collection("EmployeesGrossSalary").document(employeeOverviews.get(position).getId()).get().addOnSuccessListener(documentSnapshot -> {
                if (!documentSnapshot.exists())
                    return;
                EmployeesGrossSalary employeesGrossSalary = documentSnapshot.toObject(EmployeesGrossSalary.class);
                AddAllowanceDialog employeeSummaryDialog = new AddAllowanceDialog(employeesGrossSalary.getAllowances(), true, true);
                employeeSummaryDialog.show(getParentFragmentManager(), "");
            });
           /* AddAllowanceDialog employeeSummaryDialog = new AddAllowanceDialog(here, true, true);
            employeeSummaryDialog.show(getParentFragmentManager(), "");*/
        }
    };
}