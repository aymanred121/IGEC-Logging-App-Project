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
import com.example.igecuser.Dialogs.SalarySummaryDialog;
import com.example.igecuser.R;
import com.example.igecuser.Employee;
import com.example.igecuser.dummySalarySummary;
import com.example.igecuser.dummySalarySummary.SalaryType;
import com.example.igecuser.fireBase.Project;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class ProjectSummaryFragment extends Fragment {

    private RecyclerView recyclerView;
    private EmployeeAdapter adapter;
    private RecyclerView.LayoutManager layoutManager;
    private ArrayList<Employee> Employees;
    private com.example.igecuser.fireBase.Employee manager;
    private Project project;
    private TextInputEditText vProjectName,vProjectReference;
    private MaterialButton vUpdate,vShowProjectAllowances;
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
        adapter.setOnItemClickListener(itemClickListener);
        vUpdate.setOnClickListener(oclUpdate);
        vShowProjectAllowances.setOnClickListener(oclShowProjectAllowances);
    }

    // Functions
    private void initialize(View view) {

        db.collection("projects").document(manager.getProjectID()).get().addOnSuccessListener(documentSnapshot -> {
            if(!documentSnapshot.exists())
                return;
            project = documentSnapshot.toObject(Project.class);

            Employees = new ArrayList<>();
            // TODO: fill with data
            vProjectName = view.findViewById(R.id.TextInput_ProjectName);
            vProjectReference = view.findViewById(R.id.TextInput_ProjectReference);
            vUpdate = view.findViewById(R.id.Button_Update);
            vShowProjectAllowances = view.findViewById(R.id.Button_ShowProjectAllowances);
            recyclerView = view.findViewById(R.id.recyclerview);
            recyclerView.setHasFixedSize(true);
            layoutManager = new LinearLayoutManager(getActivity());
            adapter = new EmployeeAdapter(Employees);
            recyclerView.setLayoutManager(layoutManager);
            recyclerView.setAdapter(adapter);


            vProjectName.setText(project.getName());
            vProjectReference.setText(project.getReference());
        });

    }
    private View.OnClickListener oclUpdate = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

        }
    };
    private View.OnClickListener oclShowProjectAllowances = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

        }
    };
    private final EmployeeAdapter.OnItemClickListener itemClickListener = new EmployeeAdapter.OnItemClickListener() {
        @Override
        public void onItemClick(int position) {
            ArrayList<dummySalarySummary> salarySummaries = new ArrayList<>();
            // TODO: to be updated when firebase is done
            salarySummaries.add(new dummySalarySummary("Machine", 50.0f, SalaryType.allowance));
            salarySummaries.add(new dummySalarySummary("Base Salary", 1500.0f, SalaryType.base));
            salarySummaries.add(new dummySalarySummary("Late attendance", 50.0f, SalaryType.penalty));
            salarySummaries.add(new dummySalarySummary("Over Time", 50.0f, SalaryType.overtime));
            SalarySummaryDialog employeeSummaryDialog = new SalarySummaryDialog(salarySummaries);
            employeeSummaryDialog.show(getParentFragmentManager(), "");
        }
    };
}