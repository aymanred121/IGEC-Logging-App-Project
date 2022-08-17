package com.igec.admin.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.igec.admin.R;
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

        public ProjectViewHolder(@NonNull View itemView, ProjectAdapter.OnItemClickListener listener) {
            super(itemView);
            vName = itemView.findViewById(R.id.TextView_ProjectName);
            vNEmployee = itemView.findViewById(R.id.TextView_nEmployee);
            vNMachine = itemView.findViewById(R.id.TextView_nMachine);
            vNHMachine = itemView.findViewById(R.id.TextView_nHourMachine);
            vNHEmployee = itemView.findViewById(R.id.TextView_nHourEmployee);

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

    public ProjectAdapter(ArrayList<Project> projectsList) {
        this.projectsList = projectsList;
    }

    @NonNull
    @Override
    public ProjectAdapter.ProjectViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from((parent.getContext())).inflate(R.layout.item_project, parent, false);
        return new ProjectViewHolder(v, listener);
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