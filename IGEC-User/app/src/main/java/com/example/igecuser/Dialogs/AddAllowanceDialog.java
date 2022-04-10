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
import com.example.igecuser.fireBase.EmployeesGrossSalary;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.stream.IntStream;

public class AddAllowanceDialog extends DialogFragment {

    private FloatingActionButton vAddAllowance, vDone;
    private ArrayList<Allowance> allowances;
    private AllowanceAdapter adapter;
    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private boolean canGivePenalty, canRemove;
    private String employeeId;
    private EmployeesGrossSalary employeesGrossSalary;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public AddAllowanceDialog(ArrayList<Allowance> allowances, boolean canGivePenalty, boolean canRemove) {
        this.allowances = allowances;
        this.canGivePenalty = canGivePenalty;
        this.canRemove = canRemove;
    }

    public AddAllowanceDialog(String employeeId, boolean canGivePenalty, boolean canRemove) {
        this.employeeId = employeeId;
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
                // Do something with the result
                adapter.notifyItemChanged(position);
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_add_allowance_dialog, container, false);
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
        if(allowances != null) {
            adapter.setAllowances(allowances);
        }
        else
        {
            allowances = new ArrayList<>();
            db.collection("EmployeesGrossSalary").document(employeeId).addSnapshotListener((value, error) -> {
                if (!value.exists())
                    return;
                allowances.clear();
                employeesGrossSalary = value.toObject(EmployeesGrossSalary.class);
                //IntStream.range(0, employeesGrossSalary.getPenalties().size()).forEach(i -> employeesGrossSalary.getPenalties().get(i).setAmount(employeesGrossSalary.getPenalties().get(i).getAmount() * -1));
                allowances.addAll(employeesGrossSalary.getAllTypes());
                adapter.setAllowances(allowances);
                adapter.notifyDataSetChanged();
            });
        }

    }

    private View.OnClickListener oclDone = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            dismiss();
        }
    };
    private View.OnClickListener oclAddAllowance = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            AllowanceInfoDialog allowanceInfoDialog = new AllowanceInfoDialog(-1, canGivePenalty);
            allowanceInfoDialog.show(getParentFragmentManager(), "");
        }
    };
    private AllowanceAdapter.OnItemClickListener oclItemClickListener = new AllowanceAdapter.OnItemClickListener() {
        @Override
        public void onItemClick(int position) {
            AllowanceInfoDialog allowanceInfoDialog = new AllowanceInfoDialog(position, allowances.get(position), canGivePenalty);
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