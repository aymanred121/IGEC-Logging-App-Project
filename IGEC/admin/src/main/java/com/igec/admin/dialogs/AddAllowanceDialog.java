package com.igec.admin.dialogs;

import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import com.igec.admin.R;
import com.igec.admin.databinding.DialogAddAllowanceBinding;
import com.igec.common.adapters.AllowanceAdapter;
import com.igec.common.firebase.Allowance;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;

public class AddAllowanceDialog extends DialogFragment {

    private ArrayList<Allowance> allowances;
    private AllowanceAdapter adapter;
    private RecyclerView.LayoutManager layoutManager;
    private double salary;
    private String currency;
    private boolean canGivePenalty;

    // for project
    public AddAllowanceDialog(ArrayList<Allowance> allowances) {
        this.allowances = new ArrayList<>();
        allowances.forEach(allowance -> {
            try {
                this.allowances.add((Allowance) allowance.clone());
            } catch (CloneNotSupportedException e) {
                e.printStackTrace();
            }
        });

    }

    // for employee
    public AddAllowanceDialog(ArrayList<Allowance> allowances, double salary, String currency) {
        this.allowances = new ArrayList<>();
        allowances.forEach(allowance -> {
            try {
                this.allowances.add((Allowance) allowance.clone());
            } catch (CloneNotSupportedException e) {
                e.printStackTrace();
            }
        });
        this.salary = salary;   // employee salary
        this.currency = currency;   // employee currency
        this.canGivePenalty = true;
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
        getParentFragmentManager().setFragmentResultListener("addAllowance", this, (requestKey, bundle) -> {
            // We use a String here, but any type that can be put in a Bundle is supported
            allowances.add((Allowance) bundle.getSerializable("allowance"));
            // Do something with the result
            adapter.notifyDataSetChanged();
        });

        getParentFragmentManager().setFragmentResultListener("editAllowance", this, (requestKey, bundle) -> {
            // We use a String here, but any type that can be put in a Bundle is supported
            int position = bundle.getInt("position");
            Allowance allowance = (Allowance) bundle.getSerializable("allowance");

            allowances.get(position).setName(allowance.getName());
            allowances.get(position).setAmount(allowance.getAmount());
            allowances.get(position).setCurrency(allowance.getCurrency());
            adapter.notifyItemChanged(position);
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
        binding = null;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initialize();
        binding.addFab.setOnClickListener(oclAddAllowance);
        binding.doneFab.setOnClickListener(oclDone);
        adapter.setOnItemClickListener(oclItemClickListener);
    }

    private void initialize() {
        binding.recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(getActivity());
        adapter = new AllowanceAdapter(allowances, true, true);
        binding.recyclerView.setLayoutManager(layoutManager);
        binding.recyclerView.setAdapter(adapter);
    }

    private View.OnClickListener oclDone = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            binding.doneFab.setEnabled(false);
            Bundle result = new Bundle();
            result.putSerializable("allowances", allowances);
            getParentFragmentManager().setFragmentResult("allowances", result);
            binding.doneFab.setEnabled(true);
            dismiss();
        }
    };
    private View.OnClickListener oclAddAllowance = v -> {
        AllowanceInfoDialog allowanceInfoDialog = new AllowanceInfoDialog(-1);
        allowanceInfoDialog.setEmployeeCurrency(currency);
        allowanceInfoDialog.setEmployeeSalary(salary);
        allowanceInfoDialog.canGivePenalty(canGivePenalty);
        allowanceInfoDialog.show(getParentFragmentManager(), "");
    };
    private AllowanceAdapter.OnItemClickListener oclItemClickListener = new AllowanceAdapter.OnItemClickListener() {
        @Override
        public void onItemClick(int position) {
            AllowanceInfoDialog allowanceInfoDialog = new AllowanceInfoDialog(position, allowances.get(position));
            allowanceInfoDialog.setEmployeeCurrency(currency);
            allowanceInfoDialog.setEmployeeSalary(salary);
            allowanceInfoDialog.canGivePenalty(canGivePenalty);
            allowanceInfoDialog.show(getParentFragmentManager(), "");
        }

        @Override
        public void onDeleteItem(int position) {
            MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(getActivity());
            builder.setTitle(getString(R.string.Delete))
                    .setMessage(getString(R.string.AreUSure))
                    .setNegativeButton(getString(R.string.no), (dialogInterface, i) -> {
                    })
                    .setPositiveButton(getString(R.string.yes), (dialogInterface, i) -> {
                        allowances.remove(position);
                        adapter.notifyItemRemoved(position);
                    })
                    .show();

        }
    };
}