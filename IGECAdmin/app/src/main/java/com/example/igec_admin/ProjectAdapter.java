package com.example.igec_admin;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.igec_admin.fireBase.Project;
import com.google.android.material.checkbox.MaterialCheckBox;

import java.util.ArrayList;

public class ProjectAdapter extends RecyclerView.Adapter<ProjectAdapter.ProjectViewHolder> {
    private ArrayList<Project> projectsList;
    private ProjectAdapter.OnItemClickListener listener;

    public interface OnItemClickListener{
        void onItemClick(int position);
    }

    public void setOnItemClickListener(ProjectAdapter.OnItemClickListener listener) {
        this.listener = listener;
    }

    public static class ProjectViewHolder extends RecyclerView.ViewHolder{

        public TextView vName,vNEmployee,vNMachine,vNHEmployee,vNHMachine;
        public ProjectViewHolder(@NonNull View itemView, ProjectAdapter.OnItemClickListener listener) {
            super(itemView);
            vName = itemView.findViewById(R.id.TextView_Name);
            vNEmployee = itemView.findViewById(R.id.TextView_nEmployee);
            vNMachine = itemView.findViewById(R.id.TextView_nMachine);
            vNHMachine = itemView.findViewById(R.id.TextView_nHourMachine);
            vNHEmployee = itemView.findViewById(R.id.TextView_nHourEmployee);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(listener != null)
                    {
                        int position = getAdapterPosition();
                        if(position != RecyclerView.NO_POSITION)
                        {
                            listener.onItemClick(position);
                        }
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
        View v = LayoutInflater.from((parent.getContext())).inflate(R.layout.employee_item,parent,false);
        ProjectAdapter.ProjectViewHolder evh = new ProjectAdapter.ProjectViewHolder(v,listener);
        return evh;
    }

    @Override
    public void onBindViewHolder(@NonNull ProjectAdapter.ProjectViewHolder holder, int position) {
        Project project = projectsList.get(position);
        holder.vName.setText(project.getName());
        holder.vNEmployee.setText(project.getEmployees().size());
        holder.vNMachine.setText(0);
        holder.vNHEmployee.setText(0);
        holder.vNHMachine.setText(0);
    }

    public ArrayList<Project> getProjectsList() {
        return projectsList;
    }

    public void setProjectsList(ArrayList<Project> projectsList) {
        this.projectsList = projectsList;
    }

    public ProjectAdapter.OnItemClickListener getListener() {
        return listener;
    }

    public void setListener(ProjectAdapter.OnItemClickListener listener) {
        this.listener = listener;
    }

    @Override
    public int getItemCount() {
        return projectsList.size();
    }
}