package com.igec.admin.adapters;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.igec.admin.R;
import com.igec.common.utilities.WorkingDay;

import java.text.ParseException;
import java.util.ArrayList;

public class WorkingDayAdapter extends RecyclerView.Adapter<WorkingDayAdapter.DayViewHolder> {

    private final ArrayList<WorkingDay> workingDays;
    private WorkingDayAdapter.OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public void setOnItemClickListener(WorkingDayAdapter.OnItemClickListener listener) {
        this.listener = listener;
    }

    public static class DayViewHolder extends RecyclerView.ViewHolder {
        public TextView vDay;
        public TextView vHours;

        public DayViewHolder(@NonNull View itemView, WorkingDayAdapter.OnItemClickListener listener) {
            super(itemView);
            vDay = itemView.findViewById(R.id.TextView_day);
            vHours = itemView.findViewById(R.id.TextView_hours);
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

    public WorkingDayAdapter(ArrayList<WorkingDay> workingDays) {
        this.workingDays = workingDays;
    }

    @NonNull
    @Override
    public DayViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from((parent.getContext())).inflate(R.layout.item_workday, parent, false);
        return new DayViewHolder(v, listener);
    }

    @SuppressLint("DefaultLocale")
    @Override
    public void onBindViewHolder(@NonNull DayViewHolder holder, int position) {
        WorkingDay workingDay = workingDays.get(position);
        try {
            holder.vDay.setText(String.format("%s\n%s", workingDay.getDay(), workingDay.getName()));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        holder.vHours.setText(String.format("%.2f %s", workingDay.getHours(), workingDay.getHours() > 1 ? "Hours" : "Hour"));
    }

    @Override
    public int getItemCount() {
        return workingDays.size();
    }
}
