package com.example.igecuser.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.igecuser.R;
import com.example.igecuser.fireBase.Machine;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;

public class MachineAdapter extends RecyclerView.Adapter<MachineAdapter.MachineViewHolder> {
    private ArrayList<Machine> machinesList;
    private MachineAdapter.OnItemClickListener listener;

    public interface OnItemClickListener{
        void onItemClick(int position);
        void onCheckInOutClick(int position);
    }

    public void setOnItemClickListener(MachineAdapter.OnItemClickListener listener) {
        this.listener = listener;
    }

    public static class MachineViewHolder extends RecyclerView.ViewHolder{
        TextView vID,vCodeName;
        MaterialButton vCheckInOut;
        public MachineViewHolder(@NonNull View itemView, MachineAdapter.OnItemClickListener listener) {
            super(itemView);
            vID = itemView.findViewById(R.id.TextView_MachineID);
            vCodeName = itemView.findViewById(R.id.TextView_MachineCodeName);
            vCheckInOut = itemView.findViewById(R.id.Button_CheckInOut);

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

            vCheckInOut.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(listener != null)
                    {
                        int position = getAdapterPosition();
                        if(position != RecyclerView.NO_POSITION)
                        {
                            listener.onCheckInOutClick(position);
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
    public MachineAdapter.MachineViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from((parent.getContext())).inflate(R.layout.machine_item,parent,false);
       MachineAdapter.MachineViewHolder vvh = new MachineAdapter.MachineViewHolder(v,listener);
        return vvh;
    }

    @Override
    public void onBindViewHolder(@NonNull MachineAdapter.MachineViewHolder holder, int position) {
        Machine machine = machinesList.get(position);
        holder.vID.setText(machine.getId());
        holder.vCodeName.setText(machine.getCodeName());
        holder.vCheckInOut.setText(""/*machine.getstatus*/);
        //holder.vCheckInOut.setBackgroundColor((isIn)? Color.rgb(153, 0, 0): Color.rgb(0,153,0));


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