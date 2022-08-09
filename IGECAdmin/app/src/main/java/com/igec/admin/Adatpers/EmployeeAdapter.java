package com.igec.admin.Adatpers;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.igec.admin.R;
import com.igec.admin.fireBase.EmployeeOverview;
import com.google.android.material.checkbox.MaterialCheckBox;


import java.util.ArrayList;

public class EmployeeAdapter extends RecyclerView.Adapter<EmployeeAdapter.EmployeeViewHolder> {
    private ArrayList<EmployeeOverview> employeeOverviewsList;
    private OnItemClickListener listener;
    private boolean isAdd;

    public interface OnItemClickListener {
        void onItemClick(int position);

        void onCheckboxClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public static class EmployeeViewHolder extends RecyclerView.ViewHolder {

        public TextView vName;
        public TextView vID;
        public MaterialCheckBox vSelected;


        public EmployeeViewHolder(@NonNull View itemView, OnItemClickListener listener, boolean isAdd) {
            super(itemView);
            vName = itemView.findViewById(R.id.TextView_Name);
            vID = itemView.findViewById(R.id.TextView_ID);
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onItemClick(position);
                    }
                }
            });

            vSelected = itemView.findViewById(R.id.ImageView_EmployeeSelected);
            vSelected.setVisibility(isAdd ? View.VISIBLE : View.GONE);
            vSelected.setOnClickListener(v -> {
                if (listener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onCheckboxClick(position);
                    }
                }
            });

        }
    }

    public EmployeeAdapter(ArrayList<EmployeeOverview> employeeOverviewsList, boolean isAdd) {
        this.employeeOverviewsList = employeeOverviewsList;
        this.isAdd = isAdd;
    }

    @NonNull
    @Override
    public EmployeeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from((parent.getContext())).inflate(R.layout.item_employee, parent, false);
        return new EmployeeViewHolder(v, listener, isAdd);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull EmployeeViewHolder holder, int position) {
        EmployeeOverview employee = employeeOverviewsList.get(position);
        holder.vName.setText("Name: " + employee.getFirstName() + " " + employee.getLastName());
        holder.vID.setText("ID: " + employee.getId());
        holder.vSelected.setChecked(employee.isSelected);
        holder.vSelected.setEnabled((employee.getManagerID() == null || !employee.getManagerID().equals("adminID")));
    }

    public ArrayList<EmployeeOverview> getEmployeeOverviewsList() {
        return employeeOverviewsList;
    }

    public void setEmployeeOverviewsList(ArrayList<EmployeeOverview> employeeOverviewsList) {
        this.employeeOverviewsList = employeeOverviewsList;
    }

    public OnItemClickListener getListener() {
        return listener;
    }

    public void setListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    @Override
    public int getItemCount() {
        return employeeOverviewsList.size();
    }
}
