package com.example.igec_admin.Adatpers;

import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class MachineLogAdapter extends RecyclerView.Adapter<MachineLogAdapter.MachineLogViewHolder> {


    @NonNull
    @Override
    public MachineLogViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull MachineLogViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 0;
    }

    public static class MachineLogViewHolder extends RecyclerView.ViewHolder {
        public TextView vEmployee, vStartDate, vEndDate, vCost;

        public MachineLogViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
}
