package com.example.igec_admin.Adatpers;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.igec_admin.R;
import com.example.igec_admin.fireBase.Machine;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;

public class MachineAdapter extends RecyclerView.Adapter<MachineAdapter.MachineViewHolder> {
    private ArrayList<Machine> machinesList;
    private OnItemClickListener listener;

    public interface OnItemClickListener{
        void onItemClick(int position);
        void onLogClick(int position);
        void onCommentsClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public static class MachineViewHolder extends RecyclerView.ViewHolder{
        TextView vID, vCodeName;
        MaterialButton vLog,vComments;
        public MachineViewHolder(@NonNull View itemView, OnItemClickListener listener) {
            super(itemView);
            vID = itemView.findViewById(R.id.TextView_MachineID);
            vCodeName = itemView.findViewById(R.id.TextView_MachineCodeName);
            vLog = itemView.findViewById(R.id.Button_Log);
            vComments = itemView.findViewById(R.id.Button_Comment);

            vLog.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            listener.onLogClick(position);
                        }
                    }
                }
            });
            vComments.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            listener.onCommentsClick(position);
                        }
                    }
                }
            });
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

        }
    }

    public MachineAdapter(ArrayList<Machine> machinesList) {
        this.machinesList = machinesList;
    }

    @NonNull
    @Override
    public MachineViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from((parent.getContext())).inflate(R.layout.item_machine,parent,false);
       MachineViewHolder vvh = new MachineViewHolder(v,listener);
        return vvh;
    }

    @Override
    public void onBindViewHolder(@NonNull MachineViewHolder holder, int position) {
        Machine machine = machinesList.get(position);
        holder.vID.setText(String.format("ID: %s", machine.getId()));
        holder.vCodeName.setText(String.format("Code Name: %s", machine.getReference()));
    }


    public ArrayList<Machine> getMachinesList() {
        return machinesList;
    }

    public void setMachinesList(ArrayList<Machine> machinesList) {
        this.machinesList = machinesList;
    }

    @Override
    public int getItemCount() {
        return machinesList.size();
    }
}