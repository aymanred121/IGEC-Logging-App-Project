package com.example.igec_admin.Adatpers;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.igec_admin.R;
import com.example.igec_admin.fireBase.EmployeeOverview;
import com.google.android.material.checkbox.MaterialCheckBox;

import java.util.ArrayList;

public class EmployeeAdapter extends RecyclerView.Adapter<EmployeeAdapter.EmployeeViewHolder> {
    private ArrayList<EmployeeOverview> employeesList;
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
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            listener.onItemClick(position);
                        }
                    }
                }
            });

            vSelected = itemView.findViewById(R.id.ImageView_EmployeeSelected);
            vSelected.setVisibility(isAdd? View.VISIBLE : View.GONE);
            vSelected.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            listener.onCheckboxClick(position);
                        }
                    }
                }
            });

        }
    }

    public EmployeeAdapter(ArrayList<EmployeeOverview> employeesList, boolean isAdd) {
        this.employeesList = employeesList;
        this.isAdd = isAdd;
    }

    @NonNull
    @Override
    public EmployeeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from((parent.getContext())).inflate(R.layout.employee_item, parent, false);
        EmployeeViewHolder evh = new EmployeeViewHolder(v, listener, isAdd);
        return evh;
    }

    @Override
    public void onBindViewHolder(@NonNull EmployeeViewHolder holder, int position) {
        EmployeeOverview employee = employeesList.get(position);
        holder.vName.setText("Name: " + employee.getFirstName() + " " + employee.getLastName());
        holder.vID.setText("ID: " + employee.getId());
        holder.vSelected.setChecked(employee.getSelected());
    }

    public ArrayList<EmployeeOverview> getEmployeesList() {
        return employeesList;
    }

    public void setEmployeesList(ArrayList<EmployeeOverview> employeesList) {
        this.employeesList = employeesList;
    }

    public OnItemClickListener getListener() {
        return listener;
    }

    public void setListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    @Override
    public int getItemCount() {
        return employeesList.size();
    }
}
