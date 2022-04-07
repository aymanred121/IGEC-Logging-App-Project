package com.example.igec_admin.Adatpers;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.igec_admin.R;
import com.example.igec_admin.fireBase.EmployeeOverview;
import com.example.igec_admin.fireBase.Machine_Employee;
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
        View v = LayoutInflater.from((parent.getContext())).inflate(R.layout.machine_log_item, parent, false);
        MachineLogViewHolder vvh = new MachineLogViewHolder(v);
        return vvh;
    }

    @Override
    public void onBindViewHolder(@NonNull MachineLogViewHolder holder, int position) {

        Machine_Employee employeeMachine = machineEmployees.get(position);
        holder.vEmployee.setText(String.format("%s %s", employeeMachine.getEmployee().getFirstName(), employeeMachine.getEmployee().getLastName()));


        holder.vStartDate.setText(convertDateToString((long) ((Timestamp) employeeMachine.getCheckIn().get("Time")).getSeconds()*1000));
        holder.vEndDate.setText(convertDateToString((long) ((Timestamp) employeeMachine.getCheckOut().get("Time")).getSeconds()*1000));
        holder.vCost.setText(String.valueOf(employeeMachine.getCost()));
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

        public MachineLogViewHolder(@NonNull View itemView) {
            super(itemView);
            vEmployee = itemView.findViewById(R.id.TextView_Employee);
            vStartDate = itemView.findViewById(R.id.TextView_StartDate);
            vEndDate = itemView.findViewById(R.id.TextView_EndDate);
            vCost = itemView.findViewById(R.id.TextView_Cost);
        }
    }
}
