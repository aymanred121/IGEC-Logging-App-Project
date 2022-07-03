package com.example.igecuser.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.igecuser.R;
import com.example.igecuser.fireBase.EmployeeOverview;
import com.example.igecuser.fireBase.Project;

import java.util.ArrayList;

public class EmployeeAdapter extends RecyclerView.Adapter<EmployeeAdapter.EmployeeAdapterViewHolder> {
    private final ArrayList<EmployeeOverview> employeeOverviews;
    private OnItemClickListener listener;
    private Project project;

    public EmployeeAdapter(ArrayList<EmployeeOverview> employeeOverviews, Project project) {
        this.employeeOverviews = employeeOverviews;
        this.project = project;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public EmployeeAdapterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from((parent.getContext())).inflate(R.layout.item_employee, parent, false);
        return new EmployeeAdapterViewHolder(v, listener);
    }

    @Override
    public void onBindViewHolder(@NonNull EmployeeAdapterViewHolder holder, int position) {
        EmployeeOverview EmployeeOverview = employeeOverviews.get(position);
        holder.vEmployeeName.setText(String.format("Employee Name: %s %s", EmployeeOverview.getFirstName(), EmployeeOverview.getLastName()));
        holder.vEmployeeID.setText(String.format("Employee Id: %s", EmployeeOverview.getId()));
        if (project.getEmployeeWorkedTime().get(EmployeeOverview.getId()) == null)
            holder.vWorkingHours.setText("0 Hours");
        else{
            double workingTime = (double) project.getEmployeeWorkedTime().get(EmployeeOverview.getId());
            workingTime /=3600.0;
            holder.vWorkingHours.setText(String.format("%s Hours", workingTime));
        }
    }

    @Override
    public int getItemCount() {
        return employeeOverviews.size();
    }

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public static class EmployeeAdapterViewHolder extends RecyclerView.ViewHolder {
        public TextView vEmployeeName, vEmployeeID, vWorkingHours, vCurrentMachine;

        public EmployeeAdapterViewHolder(@NonNull View itemView, OnItemClickListener listener) {
            super(itemView);
            vEmployeeID = itemView.findViewById(R.id.TextView_EmployeeID);
            vEmployeeName = itemView.findViewById(R.id.TextView_EmployeeName);
            vWorkingHours = itemView.findViewById(R.id.TextView_EmployeeHours);
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onItemClick(position);
                    }
                }
            });

        }
    }
}