package com.igec.user.dialogs;

import static com.igec.common.CONSTANTS.EMPLOYEE_GROSS_SALARY_COL;
import static com.igec.common.CONSTANTS.PROJECT_COL;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.collection.ArraySet;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentResultListener;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.ListenerRegistration;
import com.igec.common.adapters.AllowanceAdapter;
import com.igec.user.R;
import com.igec.common.firebase.Allowance;
import com.igec.common.firebase.Employee;
import com.igec.common.firebase.EmployeeOverview;
import com.igec.common.firebase.EmployeesGrossSalary;
import com.igec.common.firebase.Project;
import com.igec.common.utilities.allowancesEnum;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.igec.user.activities.DateInaccurate;
import com.igec.user.databinding.DialogAddAllowanceBinding;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.stream.Collectors;

public class AddAllowanceDialog extends DialogFragment {
    private ArrayList<Allowance> allowances;
    private AllowanceAdapter adapter;
    private RecyclerView.LayoutManager layoutManager;
    private final boolean canGivePenalty;
    private final boolean canRemove;
    private boolean isProject;
    private EmployeeOverview employee;
    private Project project;
    private Employee manager;
    private String month, year;
    private Double baseSalary = (double) 0;
    private String currency;

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private ArrayList<ListenerRegistration> tasks = new ArrayList<>();

    public AddAllowanceDialog(Employee manager, ArrayList<Allowance> allowances, boolean canGivePenalty, boolean canRemove) {
        this.manager = manager;
        this.canGivePenalty = canGivePenalty;
        this.canRemove = canRemove;
        this.allowances = new ArrayList<>();
        allowances.forEach(allowance -> {
            try {
                this.allowances.add((Allowance) allowance.clone());
            } catch (CloneNotSupportedException e) {
                e.printStackTrace();
            }
        });
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
                Allowance allowance = (Allowance) bundle.getSerializable("allowance");

                allowances.get(position).setName(allowance.getName());
                allowances.get(position).setAmount(allowance.getAmount());
                allowances.get(position).setCurrency(allowance.getCurrency());
                allowances.get(position).setType(allowance.getType());
                allowances.get(position).setNote(allowance.getNote());
                // Do something with the result
                adapter.notifyItemChanged(position);
            }
        });
    }

    private DialogAddAllowanceBinding binding;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = DialogAddAllowanceBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        tasks.forEach(ListenerRegistration::remove);
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
        binding.addFab.setOnClickListener(oclAddAllowance);
        binding.doneFab.setOnClickListener(oclDone);
        adapter.setOnItemClickListener(oclItemClickListener);
    }

    @SuppressLint("DefaultLocale")
    private void initialize() {
        binding.recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(getActivity());
        binding.recyclerView.setLayoutManager(layoutManager);
        adapter = new AllowanceAdapter(canRemove);
        final Calendar today = Calendar.getInstance();
        year = String.valueOf(today.get(Calendar.YEAR));
        month = String.format("%02d", today.get(Calendar.MONTH) + 1);
        if (today.get(Calendar.DAY_OF_MONTH) > 25) {
            if (Integer.parseInt(month) + 1 == 13) {
                month = "01";
                year = String.format("%d", Integer.parseInt(year) + 1);
            } else {
                month = String.format("%02d", Integer.parseInt(month) + 1);
            }
        }
        isProject = allowances != null;
        if (isProject) {
            adapter.setAllowances(allowances);
            binding.recyclerView.setAdapter(adapter);

        } else {
            tasks.add(EMPLOYEE_GROSS_SALARY_COL.document(employee.getId()).collection(year).document(month).addSnapshotListener((value, error) -> {
                allowances = new ArrayList<>();
                adapter.setAllowances(allowances);
                binding.recyclerView.setAdapter(adapter);

                if (!value.exists())
                    return;
                //allowances.clear();
                EmployeesGrossSalary employeesGrossSalary = value.toObject(EmployeesGrossSalary.class);
                for (Allowance allowance : employeesGrossSalary.getAllTypes()) {
                    if (allowance.getType() == allowancesEnum.NETSALARY.ordinal()) {
                        currency = allowance.getCurrency();
                        baseSalary = allowance.getAmount();
                    }
                }
//                baseSalary = employeesGrossSalary.getAllTypes().stream().filter(x -> x.getType() == allowancesEnum.NETSALARY.ordinal()).findFirst().map(Allowance::getAmount).orElse(baseSalary);
                //Adds only Bonuses and penalties
                // get (penalty || bonus )
                employeesGrossSalary.getAllTypes().stream().filter(allowance -> allowance.getType() == allowancesEnum.RETENTION.ordinal() || allowance.getType() == allowancesEnum.BONUS.ordinal()).forEach(allowance -> allowances.add(allowance));
                //get baseAllowances
                employeesGrossSalary.getBaseAllowances().stream().filter(allowance -> allowance.getType() != allowancesEnum.PROJECT.ordinal()).forEach(allowance -> allowances.add(allowance));
                adapter.notifyDataSetChanged();
            }));
        }

    }

    private final View.OnClickListener oclDone = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            ArrayList<Allowance> oneTimeAllowances = new ArrayList<>();
            ArrayList<Allowance> permanentAllowances = new ArrayList<>();
            if (!isProject) {
                EMPLOYEE_GROSS_SALARY_COL.document(employee.getId()).get().addOnSuccessListener((value) -> {
                    if (!value.exists())
                        return;
                    for (Allowance allowance : allowances) {
                        if (allowance.getType() == allowancesEnum.BONUS.ordinal() || allowance.getType() == allowancesEnum.RETENTION.ordinal()) {
                            oneTimeAllowances.add(allowance);
                        } else {
                            permanentAllowances.add(allowance);
                        }
                    }
                    EmployeesGrossSalary employeesGrossSalary1 = value.toObject(EmployeesGrossSalary.class);
                    employeesGrossSalary1.getAllTypes().removeIf(allowance -> allowance.getType() != allowancesEnum.NETSALARY.ordinal() && allowance.getType() != allowancesEnum.PROJECT.ordinal());
                    employeesGrossSalary1.getAllTypes().addAll(permanentAllowances);
                    EMPLOYEE_GROSS_SALARY_COL.document(employee.getId()).update("allTypes", employeesGrossSalary1.getAllTypes());
                    EMPLOYEE_GROSS_SALARY_COL.document(employee.getId()).collection(year).document(month).get().addOnSuccessListener(doc -> {
                        if (!doc.exists()) {
                            //new month
                            //add project allowances
                            employeesGrossSalary1.setBaseAllowances(employeesGrossSalary1.getAllTypes().stream().filter(x -> !x.getProjectId().trim().isEmpty()).collect(Collectors.toCollection(ArrayList::new)));
                            employeesGrossSalary1.getAllTypes().removeIf(x -> x.getType() != allowancesEnum.NETSALARY.ordinal());
                            employeesGrossSalary1.getAllTypes().addAll(oneTimeAllowances);
                            employeesGrossSalary1.getBaseAllowances().addAll(permanentAllowances);
                            db.document(doc.getReference().getPath()).set(employeesGrossSalary1, SetOptions.mergeFields("allTypes", "baseAllowances"));
                            return;
                        }
                        EmployeesGrossSalary employeesGrossSalary = doc.toObject(EmployeesGrossSalary.class);
                        employeesGrossSalary.getBaseAllowances().removeIf(x -> x.getType() != allowancesEnum.PROJECT.ordinal());
                        employeesGrossSalary.getBaseAllowances().addAll(permanentAllowances);
                        employeesGrossSalary.getAllTypes().removeIf(x -> x.getType() == allowancesEnum.RETENTION.ordinal() || x.getType() == allowancesEnum.BONUS.ordinal());
                        employeesGrossSalary.getAllTypes().addAll(oneTimeAllowances);
                        db.document(doc.getReference().getPath()).update("allTypes", employeesGrossSalary.getAllTypes(), "baseAllowances", employeesGrossSalary.getBaseAllowances()).addOnSuccessListener(unused -> {
                            oneTimeAllowances.clear();
                            permanentAllowances.clear();
                        });
                    });

                    dismiss();
                });
            } else {
                //Added projectId to each allowance that is coming from project
                allowances.forEach(allowance -> {
                    allowance.setProjectId(manager.getProjectID());
                    allowance.setType(allowancesEnum.PROJECT.ordinal());
                });
                PROJECT_COL.document(manager.getProjectID()).get().addOnSuccessListener(documentSnapshot -> {
                    if (!documentSnapshot.exists())
                        return;
                    project = documentSnapshot.toObject(Project.class);
                    ArrayList<Allowance> projectAllowances = allowances;
                    project.setAllowancesList(projectAllowances);
                    PROJECT_COL.document(manager.getProjectID()).update("allowancesList", project.getAllowancesList());
                    for (EmployeeOverview employee : project.getEmployees()) {
                        EMPLOYEE_GROSS_SALARY_COL.document(employee.getId()).get().addOnSuccessListener((value) -> {
                            if (!value.exists())
                                return;
                            EmployeesGrossSalary employeesGrossSalary = value.toObject(EmployeesGrossSalary.class);
                            employeesGrossSalary.getAllTypes().removeIf(allowance -> allowance.getType() == allowancesEnum.PROJECT.ordinal() && allowance.getProjectId().equals(project.getId()));
                            employeesGrossSalary.getAllTypes().addAll(projectAllowances);
                            EMPLOYEE_GROSS_SALARY_COL.document(employee.getId()).update("allTypes", employeesGrossSalary.getAllTypes());
                            EMPLOYEE_GROSS_SALARY_COL.document(employee.getId()).collection(year).document(month).get().addOnSuccessListener(doc -> {
                                if (!doc.exists() || doc.getData().size() == 0) {
                                    //new month
//                                    ArrayList<Allowance> allowanceArrayList = new ArrayList<>();
//                                    for (Allowance allowance : employeesGrossSalary.getAllTypes()) {
//                                        if (allowance.getType() == allowancesEnum.PROJECT.ordinal()) {
//                                            allowanceArrayList.add(allowance);
//                                        }
//                                    }
//                                    employeesGrossSalary.setBaseAllowances(allowanceArrayList);
                                    employeesGrossSalary.getBaseAllowances().addAll(projectAllowances);
                                    employeesGrossSalary.getAllTypes().removeIf(x -> x.getType() == allowancesEnum.PROJECT.ordinal());
                                    EMPLOYEE_GROSS_SALARY_COL.document(employee.getId()).collection(year).document(month).set(employeesGrossSalary, SetOptions.mergeFields("allTypes", "baseAllowances"));
                                    return;
                                }
                                EmployeesGrossSalary employeesGrossSalary1 = doc.toObject(EmployeesGrossSalary.class);
                                employeesGrossSalary1.getBaseAllowances().removeIf(x -> x.getType() == allowancesEnum.PROJECT.ordinal() && x.getProjectId().equals(project.getId()));
                                employeesGrossSalary1.getBaseAllowances().addAll(projectAllowances);
                                db.document(doc.getReference().getPath()).update("baseAllowances", employeesGrossSalary1.getBaseAllowances());
                            });
                        });
                    }
                    dismiss();
                });
            }

        }
    };
    private final View.OnClickListener oclAddAllowance = new View.OnClickListener() {
        @Override
        public void onClick(View v) {


            if (employee == null) {
                AllowanceInfoDialog allowanceInfoDialog = new AllowanceInfoDialog(-1, canGivePenalty, isProject, null, baseSalary, currency);
                allowanceInfoDialog.show(getParentFragmentManager(), "");
            } else {
                EMPLOYEE_GROSS_SALARY_COL.document(employee.getId()).get().addOnSuccessListener(doc -> {
                    if (!doc.exists())
                        return;
                    EmployeesGrossSalary employeesGrossSalary = doc.toObject(EmployeesGrossSalary.class);
                    employeesGrossSalary.getAllTypes().forEach(allowance -> {
                        if (allowance.getType() == allowancesEnum.NETSALARY.ordinal()) {
                            baseSalary = allowance.getAmount();
                            currency = allowance.getCurrency();
                        }
                    });
                    AllowanceInfoDialog allowanceInfoDialog = new AllowanceInfoDialog(-1, canGivePenalty, isProject, employee.getId(), baseSalary, currency);
                    allowanceInfoDialog.show(getParentFragmentManager(), "");
                });
            }
        }

    };
    private final AllowanceAdapter.OnItemClickListener oclItemClickListener = new AllowanceAdapter.OnItemClickListener() {
        @Override
        public void onItemClick(int position) {

            if (employee == null) {
                AllowanceInfoDialog allowanceInfoDialog = new AllowanceInfoDialog(position, allowances.get(position), canGivePenalty, isProject, null, baseSalary, currency);
                allowanceInfoDialog.show(getParentFragmentManager(), "");
            } else {
                EMPLOYEE_GROSS_SALARY_COL.document(employee.getId()).get().addOnSuccessListener(doc -> {
                    if (!doc.exists())
                        return;
                    EmployeesGrossSalary employeesGrossSalary = doc.toObject(EmployeesGrossSalary.class);
                    employeesGrossSalary.getAllTypes().forEach(allowance -> {
                        if (allowance.getType() == allowancesEnum.NETSALARY.ordinal()) {
                            baseSalary = allowance.getAmount();
                            currency = allowance.getCurrency();
                        }

                    });
                    AllowanceInfoDialog allowanceInfoDialog = new AllowanceInfoDialog(position, allowances.get(position), canGivePenalty, isProject, employee.getId(), baseSalary, currency);
                    allowanceInfoDialog.show(getParentFragmentManager(), "");
                });
            }

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