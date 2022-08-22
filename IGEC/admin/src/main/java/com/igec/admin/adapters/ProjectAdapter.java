package com.igec.admin.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.igec.admin.databinding.ItemProjectBinding;
import com.igec.common.firebase.Project;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

public class ProjectAdapter extends RecyclerView.Adapter<ProjectAdapter.ProjectViewHolder> {
    private ArrayList<Project> projectsList;
    private ProjectAdapter.OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public void setOnItemClickListener(ProjectAdapter.OnItemClickListener listener) {
        this.listener = listener;
    }

    public static class ProjectViewHolder extends RecyclerView.ViewHolder {

        public TextView vName, vNEmployee, vNMachine, vNHEmployee, vNHMachine;

        public ProjectViewHolder(@NonNull ItemProjectBinding itemView, OnItemClickListener listener) {
            super(itemView.getRoot());
            vName = itemView.TextViewProjectName;
            vNEmployee = itemView.TextViewNEmployee;
            vNMachine = itemView.TextViewNMachine;
            vNHMachine = itemView.TextViewNHourMachine;
            vNHEmployee = itemView.TextViewNHourEmployee;

            itemView.getRoot().setOnClickListener(v -> {
                if (listener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onItemClick(position);
                    }
                }
            });


        }
    }

    public ProjectAdapter(ArrayList<Project> projectsList) {
        this.projectsList = projectsList;
    }

    @NonNull
    @Override
    public ProjectAdapter.ProjectViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
       ItemProjectBinding binding  = ItemProjectBinding.inflate(LayoutInflater.from(parent.getContext()),parent,false);
        return new ProjectViewHolder(binding, listener);
    }

    @Override
    public void onBindViewHolder(@NonNull ProjectAdapter.ProjectViewHolder holder, int position) {
        Project project = projectsList.get(position);

        holder.vName.setText(project.getName());
        holder.vNEmployee.setText(String.format(Locale.getDefault(), "# Employee: %d", project.getEmployees().size()));
        holder.vNMachine.setText(String.format(Locale.getDefault(), "# Machine: %d", project.getMachineWorkedTime().size()));
        holder.vNHEmployee.setText(String.format(Locale.getDefault(), "# Hours by Employee: %d", projectWorkingHours(project, 1)));
        holder.vNHMachine.setText(String.format(Locale.getDefault(), "# Hours by Machine: %d", projectWorkingHours(project, 0)));


    }

    private long projectWorkingHours(Project project, int flag) {
        long seconds = 0;
        HashMap<String, Object> workingHoursMap;
        if (flag == 1) {
            workingHoursMap = project.getEmployeeWorkedTime();
        } else
            workingHoursMap = project.getMachineWorkedTime();
        for (String key : workingHoursMap.keySet()) {
            Object time = workingHoursMap.get(key);
            if (time == null) return 0;
            seconds += (long) time;
        }
        return seconds / 3600;
    }


    public ArrayList<Project> getProjectsList() {
        return projectsList;
    }

    public void setProjectsList(ArrayList<Project> projectsList) {
        this.projectsList = projectsList;
    }

    @Override
    public int getItemCount() {
        return projectsList.size();
    }
}