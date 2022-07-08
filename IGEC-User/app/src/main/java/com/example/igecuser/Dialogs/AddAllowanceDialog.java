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
import java.util.Calendar;
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
        final Calendar today = Calendar.getInstance();
        int sameMonth = today.get(Calendar.MONTH) + 1, nextMonth = today.get(Calendar.MONTH) + 2;
        String month, year;
        year = String.valueOf(today.get(Calendar.YEAR));
        month = String.format("%02d", sameMonth);
        isProject = allowances != null;
        if (isProject) {
            adapter.setAllowances(allowances);
        } else {
            allowances = new ArrayList<>();
            db.collection("EmployeesGrossSalary").document(employee.getId()).collection(year).document(month).addSnapshotListener((value, error) -> {
                adapter.setAllowances(allowances);
                if (!value.exists())
                    return;
                //allowances.clear();
                EmployeesGrossSalary employeesGrossSalary = value.toObject(EmployeesGrossSalary.class);
                //Adds only Bonuses and penalties
                // get (penalty || gift )
                employeesGrossSalary.getAllTypes().stream().filter(allowance -> allowance.getType() == allowancesEnum.PENALTY.ordinal() || allowance.getType() == allowancesEnum.GIFT.ordinal()).forEach(allowance -> allowances.add(allowance));
                // get (bonus)
                employeesGrossSalary.getBaseAllowances().stream().filter(allowance -> allowance.getType() == allowancesEnum.BONUS.ordinal()).forEach(allowance -> allowances.add(allowance));
                adapter.notifyDataSetChanged();
            });
        }

    }

    private final View.OnClickListener oclDone = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
            String currentDateAndTime = sdf.format(new Date());
            String day = currentDateAndTime.substring(0, 2);
            String month = currentDateAndTime.substring(3, 5);
            String year = currentDateAndTime.substring(6, 10);
            int dayInt = Integer.parseInt(day);
            if (dayInt > 25) {
                if (Integer.parseInt(month) + 1 == 13) {
                    month = "01";
                    year = Integer.parseInt(year) + 1 + "";
                } else {
                    month = Integer.parseInt(month) + 1 + "";
                    if (month.length() == 1) {
                        month = "0" + month;
                    }
                }
            }
            final String finalMonth = month;
            final String finalYear = year;
            ArrayList<Allowance> oneTimeAllowances = new ArrayList<>();
            ArrayList<Allowance> permanentAllowances = new ArrayList<>();
            if (!isProject) {
                db.collection("EmployeesGrossSalary").document(employee.getId()).get().addOnSuccessListener((value) -> {
                    ArrayList<Allowance> bonus = new ArrayList<>();
                    if (!value.exists())
                        return;
                    for (Allowance allowance : allowances) {
                        if (allowance.getType() == allowancesEnum.GIFT.ordinal()) {
                            oneTimeAllowances.add(allowance);
                        } else if (allowance.getType() == allowancesEnum.BONUS.ordinal()) {
                            bonus.add(allowance);
                        } else if (allowance.getType() == allowancesEnum.PENALTY.ordinal()) {
                            oneTimeAllowances.add(allowance);
                        } else {
                            permanentAllowances.add(allowance);
                        }
                    }
                    EmployeesGrossSalary employeesGrossSalary1 = value.toObject(EmployeesGrossSalary.class);
                    employeesGrossSalary1.getAllTypes().removeIf(allowance -> allowance.getType() == allowancesEnum.PENALTY.ordinal() || allowance.getType() == allowancesEnum.BONUS.ordinal());
                    employeesGrossSalary1.getAllTypes().addAll(permanentAllowances);
                    db.collection("EmployeesGrossSalary").document(employee.getId()).update("allTypes", employeesGrossSalary1.getAllTypes());
                    db.collection("EmployeesGrossSalary").document(employee.getId()).collection(finalYear).document(finalMonth).get().addOnSuccessListener(doc -> {
                        if (!doc.exists()) {
                            //new month
                            employeesGrossSalary1.setBaseAllowances(employeesGrossSalary1.getAllTypes().stream().filter(x -> x.getType() == allowancesEnum.PROJECT.ordinal()).collect(Collectors.toCollection(ArrayList::new)));
                            employeesGrossSalary1.getAllTypes().removeIf(x -> x.getType() == allowancesEnum.PROJECT.ordinal());
                            employeesGrossSalary1.getAllTypes().addAll(oneTimeAllowances);
                            employeesGrossSalary1.getBaseAllowances().addAll(permanentAllowances);
                            db.document(doc.getReference().getPath()).set(employeesGrossSalary1, SetOptions.mergeFields("allTypes", "baseAllowances"));
                            return;
                        }
                        EmployeesGrossSalary employeesGrossSalary = doc.toObject(EmployeesGrossSalary.class);
                        employeesGrossSalary.getBaseAllowances().addAll(permanentAllowances);
                        /*
                         if bonus(es) exists then remove the old ones and add the new modified ones
                         else remove it from the baseAllowances
                        */
                        if (bonus.size() != 0) {
                            employeesGrossSalary.getBaseAllowances().removeIf(x -> x.getType() == allowancesEnum.BONUS.ordinal());
                            employeesGrossSalary.getBaseAllowances().addAll(bonus);
                        } else {
                            employeesGrossSalary.getBaseAllowances().removeIf(x -> x.getType() == allowancesEnum.BONUS.ordinal());
                        }
                        employeesGrossSalary.getAllTypes().removeIf(x -> x.getType() == allowancesEnum.PENALTY.ordinal() || x.getType() == allowancesEnum.GIFT.ordinal());
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
                allowances.forEach(allowance -> allowance.setProjectId(manager.getProjectID()));
                db.collection("projects").document(manager.getProjectID()).get().addOnSuccessListener(documentSnapshot -> {
                    if (!documentSnapshot.exists())
                        return;
                    project = documentSnapshot.toObject(Project.class);
                    ArrayList<Allowance> projectAllowances = allowances;
                    project.setAllowancesList(projectAllowances);
                    db.collection("projects").document(manager.getProjectID()).update("allowancesList", project.getAllowancesList());
                    for (EmployeeOverview employee : project.getEmployees()) {
                        db.collection("EmployeesGrossSalary").document(employee.getId()).get().addOnSuccessListener((value) -> {
                            if (!value.exists())
                                return;
                            EmployeesGrossSalary employeesGrossSalary = value.toObject(EmployeesGrossSalary.class);
                            employeesGrossSalary.getAllTypes().removeIf(allowance -> allowance.getType() == allowancesEnum.PROJECT.ordinal() && allowance.getProjectId().equals(project.getId()));
                            employeesGrossSalary.getAllTypes().addAll(projectAllowances);
                            db.collection("EmployeesGrossSalary").document(employee.getId()).update("allTypes", employeesGrossSalary.getAllTypes());
                            db.collection("EmployeesGrossSalary").document(employee.getId()).collection(finalYear).document(finalMonth).get().addOnSuccessListener(doc -> {
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
                                    db.collection("EmployeesGrossSalary").document(employee.getId()).collection(finalYear).document(finalMonth).set(employeesGrossSalary, SetOptions.mergeFields("allTypes", "baseAllowances"));
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
            AllowanceInfoDialog allowanceInfoDialog = new AllowanceInfoDialog(-1, canGivePenalty, isProject, (employee != null) ? employee.getId() : null);
            allowanceInfoDialog.show(getParentFragmentManager(), "");
        }
    };
    private final AllowanceAdapter.OnItemClickListener oclItemClickListener = new AllowanceAdapter.OnItemClickListener() {
        @Override
        public void onItemClick(int position) {
            AllowanceInfoDialog allowanceInfoDialog = new AllowanceInfoDialog(position, allowances.get(position), canGivePenalty, isProject, (employee != null) ? employee.getId() : null);
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