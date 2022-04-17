package com.example.igecuser.Dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentResultListener;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.igecuser.Adapters.AllowanceAdapter;
import com.example.igecuser.R;
import com.example.igecuser.fireBase.Allowance;
import com.example.igecuser.fireBase.Employee;
import com.example.igecuser.fireBase.EmployeeOverview;
import com.example.igecuser.fireBase.EmployeesGrossSalary;
import com.example.igecuser.fireBase.Project;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.stream.Collectors;

public class AddAllowanceDialog extends DialogFragment {
    private final int PROJECT = 0;
    private final int NETSALARY = 1;
    private final int ALLOWANCE = 2;
    private final int BONUS = 3;
    private final int PENALTY = 4;
    private FloatingActionButton vAddAllowance, vDone;
    private ArrayList<Allowance> allowances;
    private AllowanceAdapter adapter;
    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private final boolean canGivePenalty;
    private final boolean canRemove;
    private boolean isProject;
    private EmployeeOverview employee;
    private EmployeesGrossSalary employeesGrossSalary;
    private Project project;
    private Employee manager;

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public AddAllowanceDialog(Employee manager, ArrayList<Allowance> allowances, boolean canGivePenalty, boolean canRemove) {
        this.manager = manager;
        this.allowances = allowances;
        this.canGivePenalty = canGivePenalty;
        this.canRemove = canRemove;
    }

    public AddAllowanceDialog(EmployeeOverview employee, boolean canGivePenalty, boolean canRemove) {
        this.employee = employee;
        this.canRemove = canRemove;
        this.canGivePenalty = canGivePenalty;
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
        getParentFragmentManager().setFragmentResultListener("addAllowance", this, new FragmentResultListener() {
            @Override
            public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle bundle) {
                // We use a String here, but any type that can be put in a Bundle is supported
                allowances.add((Allowance) bundle.getSerializable("allowance"));
                // Do something with the result
                adapter.notifyDataSetChanged();
            }
        });
        getParentFragmentManager().setFragmentResultListener("editAllowance", this, new FragmentResultListener() {
            @Override
            public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle bundle) {
                // We use a String here, but any type that can be put in a Bundle is supported
                int position = bundle.getInt("position");
                String note = bundle.getString("note");
                Allowance allowance = (Allowance) bundle.getSerializable("allowance");

                allowances.get(position).setName(allowance.getName());
                allowances.get(position).setAmount(allowance.getAmount());
                allowances.get(position).setType(allowance.getType());
                allowances.get(position).setNote(note);
                // Do something with the result
                adapter.notifyItemChanged(position);
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.dialog_add_allowance, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initialize(view);
        vAddAllowance.setOnClickListener(oclAddAllowance);
        vDone.setOnClickListener(oclDone);
        adapter.setOnItemClickListener(oclItemClickListener);
    }

    private void initialize(View view) {
        vAddAllowance = view.findViewById(R.id.Button_AddAllowance);
        vDone = view.findViewById(R.id.Button_Done);
        recyclerView = view.findViewById(R.id.recyclerview);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);
        adapter = new AllowanceAdapter(canRemove);
        recyclerView.setAdapter(adapter);
        isProject = allowances != null;
        if (isProject) {
            adapter.setAllowances(allowances);
        } else {
            allowances = new ArrayList<>();
            db.collection("EmployeesGrossSalary").document(employee.getId()).addSnapshotListener((value, error) -> {
                if (!value.exists())
                    return;
                allowances.clear();
                employeesGrossSalary = value.toObject(EmployeesGrossSalary.class);
                //Adds only Bonuses and penalties
                employeesGrossSalary.getAllTypes().stream().filter(allowance -> allowance.getType() == PENALTY || allowance.getType() == BONUS).forEach(allowance -> allowances.add(allowance));
                adapter.setAllowances(allowances);
                adapter.notifyDataSetChanged();
            });
        }

    }

    private final View.OnClickListener oclDone = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            if (!isProject) {
                db.collection("EmployeesGrossSalary").document(employee.getId()).get().addOnSuccessListener((value) -> {
                    if (!value.exists())
                        return;
                    employeesGrossSalary = value.toObject(EmployeesGrossSalary.class);
                    employeesGrossSalary.getAllTypes().removeIf(allowance -> allowance.getType() == PENALTY || allowance.getType() == BONUS);
                    employeesGrossSalary.getAllTypes().addAll(allowances);
                    db.collection("EmployeesGrossSalary").document(employee.getId()).update("allTypes", employeesGrossSalary.getAllTypes());
                    dismiss();
                });
            } else {
                //Added projectId to each allowance that is coming from project
                allowances.stream().flatMap(allowance -> {
                    allowance.setProjectId(manager.getProjectID());
                    return null;
                }).collect(Collectors.toList());
                db.collection("projects").document(manager.getProjectID()).get().addOnSuccessListener(documentSnapshot -> {

                    if (!documentSnapshot.exists())
                        return;
                    project = documentSnapshot.toObject(Project.class);
                    project.setAllowancesList(allowances);
                    for (EmployeeOverview employee : project.getEmployees()) {
                        db.collection("EmployeesGrossSalary").document(employee.getId()).get().addOnSuccessListener((value) -> {
                            if (!value.exists())
                                return;
                            employeesGrossSalary = value.toObject(EmployeesGrossSalary.class);
                            employeesGrossSalary.getAllTypes().removeIf(allowance -> allowance.getType() == PROJECT && allowance.getProjectId().equals(project.getId()));
                            employeesGrossSalary.getAllTypes().addAll(allowances);
                            db.collection("EmployeesGrossSalary").document(employee.getId()).update("allTypes", employeesGrossSalary.getAllTypes());
                        });
                    }
                    db.collection("projects").document(manager.getProjectID()).update("allowancesList", allowances);
                    dismiss();
                });
            }

        }
    };
    private final View.OnClickListener oclAddAllowance = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            AllowanceInfoDialog allowanceInfoDialog = new AllowanceInfoDialog(-1, canGivePenalty, isProject);
            allowanceInfoDialog.show(getParentFragmentManager(), "");
        }
    };
    private final AllowanceAdapter.OnItemClickListener oclItemClickListener = new AllowanceAdapter.OnItemClickListener() {
        @Override
        public void onItemClick(int position) {
            AllowanceInfoDialog allowanceInfoDialog = new AllowanceInfoDialog(position, allowances.get(position), canGivePenalty, isProject);
            allowanceInfoDialog.show(getParentFragmentManager(), "");
        }

        @Override
        public void onDeleteItem(int position) {
            MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(getActivity());
            builder.setTitle(getString(R.string.Delete))
                    .setMessage(getString(R.string.AreUSure))
                    .setNegativeButton(getString(R.string.cancel), (dialogInterface, i) -> {
                    })
                    .setPositiveButton(getString(R.string.accept), (dialogInterface, i) -> {
                        allowances.remove(position);
                        adapter.notifyItemRemoved(position);
                    })
                    .show();

        }
    };
}