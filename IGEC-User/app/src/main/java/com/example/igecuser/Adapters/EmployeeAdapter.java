package com.example.igecuser.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.igecuser.R;
import com.example.igecuser.dummyEmployee;

import java.util.ArrayList;

public class EmployeeAdapter extends RecyclerView.Adapter<EmployeeAdapter.EmployeeAdapterViewHolder> {
    private final ArrayList<dummyEmployee> dummyEmployees;
    private OnItemClickListener listener;

    public EmployeeAdapter(ArrayList<dummyEmployee> dummyEmployees) {
        this.dummyEmployees = dummyEmployees;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public EmployeeAdapterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from((parent.getContext())).inflate(R.layout.employee_item, parent, false);
        return new EmployeeAdapterViewHolder(v, listener);
    }

    @Override
    public void onBindViewHolder(@NonNull EmployeeAdapterViewHolder holder, int position) {
        //TODO
        dummyEmployee dummyEmployee = dummyEmployees.get(position);
        holder.vEmployeeName.setText(dummyEmployee.getName());
        holder.vEmployeeID.setText(String.valueOf(dummyEmployee.getId()));
        holder.vWorkingHours.setText(String.format("%s Hours", dummyEmployee.getHours()));
        holder.vCurrentMachine.setText(String.valueOf(dummyEmployee.getMachine()));
    }

    @Override
    public int getItemCount() {
        return dummyEmployees.size();
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
            vCurrentMachine = itemView.findViewById(R.id.TextView_CurrentMachine);
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