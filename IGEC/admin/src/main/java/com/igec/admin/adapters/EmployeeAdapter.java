package com.igec.admin.adapters;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.radiobutton.MaterialRadioButton;
import com.igec.admin.databinding.ItemEmployeeBinding;
import com.igec.common.firebase.EmployeeOverview;
import com.google.android.material.checkbox.MaterialCheckBox;


import java.util.ArrayList;

public class EmployeeAdapter extends RecyclerView.Adapter<EmployeeAdapter.EmployeeViewHolder> {
    private ArrayList<EmployeeOverview> employeeOverviewsList;
    private OnItemClickListener listener;
    private String MID = null;
    private final boolean isCheckable;

    public interface OnItemClickListener {
        void onItemClick(int position);

        void onCheckboxClick(int position);

        void onRadioClick(int position);
    }

    public void setMID(String MID) {
        this.MID = MID;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public static class EmployeeViewHolder extends RecyclerView.ViewHolder {

        public TextView vName;
        public TextView vID;
        public MaterialCheckBox vSelected;
        public MaterialRadioButton vManager;


        public EmployeeViewHolder(@NonNull ItemEmployeeBinding itemView, OnItemClickListener listener, boolean isCheckable) {
            super(itemView.getRoot());
            vName = itemView.TextViewName;
            vID = itemView.TextViewID;
            vSelected = itemView.ImageViewEmployeeSelected;
            vManager = itemView.managerRadioButton;
            if (!isCheckable) {
                itemView.getRoot().setOnClickListener(v -> {
                    if (listener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            listener.onItemClick(position);
                        }
                    }
                });
            }
            vSelected.setVisibility(isCheckable ? View.VISIBLE : View.GONE);
            vManager.setVisibility(isCheckable ? View.VISIBLE : View.GONE);
            vSelected.setOnClickListener(v -> {
                if (listener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onCheckboxClick(position);
                    }
                }
            });
            vManager.setOnClickListener(v -> {
                if (listener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onRadioClick(position);
                    }
                }
            });

        }
    }

    public EmployeeAdapter(ArrayList<EmployeeOverview> employeeOverviewsList, boolean isCheckable) {
        this.employeeOverviewsList = employeeOverviewsList;
        this.isCheckable = isCheckable;
    }

    @NonNull
    @Override
    public EmployeeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemEmployeeBinding binding = ItemEmployeeBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new EmployeeViewHolder(binding, listener, isCheckable);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull EmployeeViewHolder holder, int position) {
        EmployeeOverview employee = employeeOverviewsList.get(position);
        holder.vName.setText("Name: " + employee.getFirstName() + " " + employee.getLastName());
        holder.vID.setText("ID: " + employee.getId());
        holder.vSelected.setChecked(employee.isSelected);
        holder.vManager.setVisibility(employee.isSelected ? View.VISIBLE : View.GONE);
        boolean isHeTheManager = employee.getId().equals(MID);
        holder.vManager.setChecked(isHeTheManager);
        holder.vSelected.setEnabled(!isHeTheManager);
    }

    public ArrayList<EmployeeOverview> getEmployeeOverviewsList() {
        return employeeOverviewsList;
    }

    public void setEmployeeOverviewsList(ArrayList<EmployeeOverview> employeeOverviewsList) {
        this.employeeOverviewsList = employeeOverviewsList;
    }

    @Override
    public int getItemCount() {
        return employeeOverviewsList.size();
    }
}
