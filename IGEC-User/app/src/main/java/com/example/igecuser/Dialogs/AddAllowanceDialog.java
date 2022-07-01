package com.example.igecuser.Dialogs;

import android.annotation.SuppressLint;
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
import com.example.igecuser.utilites.allowancesEnum;
import com.google.android.gms.common.util.NumberUtils;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.zxing.common.StringUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.stream.Collectors;

public class AddAllowanceDialog extends DialogFragment {
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
                employeesGrossSalary.getAllTypes().stream().filter(allowance -> allowance.getType() == allowancesEnum.PENALTY.ordinal() || allowance.getType() == allowancesEnum.BONUS.ordinal()).forEach(allowance -> allowances.add(allowance));
                adapter.setAllowances(allowances);
                adapter.notifyDataSetChanged();
            });
        }

    }

    private final View.OnClickListener oclDone = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
            String currentDateAndTime = sdf.format(new Date());
            String day = currentDateAndTime.substring(0,2);
            String month = currentDateAndTime.substring(3,5);
            String year = currentDateAndTime.substring(6,10);
            //todo tbd what is permanent and what is oneTime only
            ArrayList<Allowance> oneTimeAllowances = new ArrayList<>();
            ArrayList<Allowance> permanentAllowances = new ArrayList<>();
            if (!isProject) {
                db.collection("EmployeesGrossSalary").document(employee.getId()).get().addOnSuccessListener((value) -> {
                    if (!value.exists())
                        return;
                    allowances.forEach(allowance -> {
                        if (allowance.getType() == allowancesEnum.GIFT.ordinal()) {
                            oneTimeAllowances.add(allowance);
                        } else if (allowance.getType() == allowancesEnum.BONUS.ordinal()) {
                            oneTimeAllowances.add(allowance);
                        } else if (allowance.getType() == allowancesEnum.PENALTY.ordinal()) {
                            oneTimeAllowances.add(allowance);
                        }else {
                            permanentAllowances.add(allowance);
                        }

                    });

                    employeesGrossSalary = value.toObject(EmployeesGrossSalary.class);
                    employeesGrossSalary.getAllTypes().removeIf(allowance -> allowance.getType() == allowancesEnum.PENALTY.ordinal() || allowance.getType() == allowancesEnum.BONUS.ordinal());
                    employeesGrossSalary.getAllTypes().addAll(permanentAllowances);
                    db.collection("EmployeesGrossSalary").document(employee.getId()).update("allTypes", employeesGrossSalary.getAllTypes());
                    db.collection("EmployeesGrossSalary").document(employee.getId()).collection(year).document(month).get().addOnSuccessListener(doc->{
                        if(!doc.exists()){
                            //new month
                            db.collection("EmployeesGrossSalary").document(employee.getId()).collection(year).document(month).set(employeesGrossSalary, SetOptions.mergeFields("allTypes"));
                            return;
                        }
                        EmployeesGrossSalary employeesGrossSalary = doc.toObject(EmployeesGrossSalary.class);
                        permanentAllowances.forEach(allowance -> {
                            employeesGrossSalary.getAllTypes().removeIf(x->x.getName().trim().equals(allowance.getName().trim())&& x.getType()==allowance.getType());
                        });
                        employeesGrossSalary.getAllTypes().addAll(allowances);
                        db.collection("EmployeesGrossSalary").document(employee.getId()).collection(year).document(month).update("allTypes", employeesGrossSalary.getAllTypes());

                    });
                    oneTimeAllowances.clear();
                    permanentAllowances.clear();
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
                            employeesGrossSalary.getAllTypes().removeIf(allowance -> allowance.getType() == allowancesEnum.PROJECT.ordinal() && allowance.getProjectId().equals(project.getId()));
                            employeesGrossSalary.getAllTypes().addAll(allowances);
                            db.collection("EmployeesGrossSalary").document(employee.getId()).update("allTypes",employeesGrossSalary.getAllTypes());
                            db.collection("EmployeesGrossSalary").document(employee.getId()).collection(year).document(month).get().addOnSuccessListener(doc->{
                                if(!doc.exists()){
                                    //new month
                                    db.collection("EmployeesGrossSalary").document(employee.getId()).collection(year).document(month).set(employeesGrossSalary, SetOptions.mergeFields("allTypes"));
                                    return;
                                }
                                EmployeesGrossSalary employeesGrossSalary = doc.toObject(EmployeesGrossSalary.class);
                                employeesGrossSalary.getAllTypes().removeIf(x->x.getProjectId().equals(project.getId()) &&!x.getNote().trim().matches("-?\\d+(\\.\\d+)?"));
                                employeesGrossSalary.getAllTypes().addAll(allowances);
                                db.document(doc.getReference().getPath()).update("allTypes", employeesGrossSalary.getAllTypes());

                            });

                            //db.collection("EmployeesGrossSalary").document(employee.getId()).collection(year).document(month).set(employeesGrossSalary, SetOptions.mergeFields("allTypes"));
                        });
                    }
                    //db.collection("projects").document(manager.getProjectID()).update("allowancesList", allowances);
                    oneTimeAllowances.clear();
                    permanentAllowances.clear();
                    dismiss();
                });
            }

        }
    };
    private final View.OnClickListener oclAddAllowance = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            AllowanceInfoDialog allowanceInfoDialog = new AllowanceInfoDialog(-1, canGivePenalty, isProject, (employee != null)? employee.getId() : null);
            allowanceInfoDialog.show(getParentFragmentManager(), "");
        }
    };
    private final AllowanceAdapter.OnItemClickListener oclItemClickListener = new AllowanceAdapter.OnItemClickListener() {
        @Override
        public void onItemClick(int position) {
            AllowanceInfoDialog allowanceInfoDialog = new AllowanceInfoDialog(position, allowances.get(position), canGivePenalty, isProject , (employee != null)? employee.getId() : null);
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