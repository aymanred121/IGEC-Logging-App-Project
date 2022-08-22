package com.igec.admin.adapters;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.igec.admin.databinding.ItemMachineLogBinding;
import com.igec.common.firebase.Machine_Employee;
import com.google.firebase.Timestamp;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class MachineLogAdapter extends RecyclerView.Adapter<MachineLogAdapter.MachineLogViewHolder> {

    private ArrayList<Machine_Employee> machineEmployees;
    public MachineLogAdapter(ArrayList<Machine_Employee> machineEmployees) {
        this.machineEmployees = machineEmployees;
    }

    @NonNull
    @Override
    public MachineLogViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemMachineLogBinding binding  = ItemMachineLogBinding.inflate(LayoutInflater.from(parent.getContext()),parent,false);
        return new MachineLogViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull MachineLogViewHolder holder, int position) {

        Machine_Employee employeeMachine = machineEmployees.get(position);
        holder.vEmployee.setText(String.format("%s %s", employeeMachine.getEmployee().getFirstName(), employeeMachine.getEmployee().getLastName()));


        holder.vStartDate.setText(convertDateToString((long) ((Timestamp) employeeMachine.getCheckIn().get("Time")).getSeconds() * 1000));
        if (employeeMachine.getCheckOut().get("Time") != null)
            holder.vEndDate.setText(convertDateToString((long) ((Timestamp) employeeMachine.getCheckOut().get("Time")).getSeconds() * 1000));
        holder.vCost.setText(String.format("%.2f EGP", employeeMachine.getCost()));
    }

    private String convertDateToString(long selection) {
        @SuppressLint("SimpleDateFormat") SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy");
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(selection);
        return simpleDateFormat.format(calendar.getTime());
    }

    @Override
    public int getItemCount() {
        return machineEmployees.size();
    }

    public ArrayList<Machine_Employee> getMachineEmployees() {
        return machineEmployees;
    }

    public void setMachineEmployees(ArrayList<Machine_Employee> machineEmployees) {
        this.machineEmployees = machineEmployees;
    }

    public static class MachineLogViewHolder extends RecyclerView.ViewHolder {
        public TextView vEmployee, vStartDate, vEndDate, vCost;

        public MachineLogViewHolder(@NonNull ItemMachineLogBinding itemView) {
            super(itemView.getRoot());
            vEmployee = itemView.TextViewEmployee;
            vStartDate = itemView.TextViewStartDate;
            vEndDate = itemView.TextViewEndDate;
            vCost = itemView.TextViewCost;
        }
    }
}
