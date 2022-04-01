package com.example.igec_admin.Adatpers;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.igec_admin.R;
import com.example.igec_admin.fireBase.Allowance;

import java.util.ArrayList;

public class AllowanceAdapter extends RecyclerView.Adapter<AllowanceAdapter.AllowanceViewHolder> {
    private ArrayList<Allowance> allowances;
    private OnItemClickListener listener;

    public AllowanceAdapter(ArrayList<Allowance> supplements) {
        this.allowances = supplements;
    }

    @NonNull
    @Override
    public AllowanceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from((parent.getContext())).inflate(R.layout.allowance_item,parent,false);
        return new AllowanceViewHolder(v,listener);
    }

    @Override
    public void onBindViewHolder(@NonNull AllowanceViewHolder holder, int position) {
        Allowance allowance = allowances.get(position);
        holder.vName.setText(allowance.getName());
        holder.vAmount.setText(String.valueOf(allowance.getAmount()));
    }

    @Override
    public int getItemCount() {
        return allowances.size();
    }

    public interface OnItemClickListener{
        void onItemClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }
    public ArrayList<Allowance> getAllowances() {
        return allowances;
    }

    public void setAllowances(ArrayList<Allowance> allowances) {
        this.allowances = allowances;
    }

    public OnItemClickListener getListener() {
        return listener;
    }

    public void setListener(OnItemClickListener listener) {
        this.listener = listener;
    }
    public static class AllowanceViewHolder extends RecyclerView.ViewHolder{
        public TextView vName, vAmount;
        public AllowanceViewHolder(@NonNull View itemView, OnItemClickListener listener) {
            super(itemView);

            vName = itemView.findViewById(R.id.TextView_ReasonFor);
            vAmount = itemView.findViewById(R.id.TextView_MountOf);

            itemView.setOnClickListener(v -> {
                if(listener != null)
                {
                    int position = getAdapterPosition();
                    if(position != RecyclerView.NO_POSITION)
                    {
                        listener.onItemClick(position);
                    }
                }
            });

        }
    }

}
