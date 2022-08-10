package com.igec.admin.Adapters;

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

    private ArrayList<WorkingDay> workingDays;
    private WorkingDayAdapter.OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public void setOnItemClickListener(WorkingDayAdapter.OnItemClickListener listener) {
        this.listener = listener;
    }

    public ArrayList<WorkingDay> getWorkingDays() {
        return workingDays;
    }

    public void setWorkingDays(ArrayList<WorkingDay> workingDays) {
        this.workingDays = workingDays;
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

    @Override
    public void onBindViewHolder(@NonNull DayViewHolder holder, int position) {
        WorkingDay workingDay = workingDays.get(position);
        try {
            holder.vDay.setText(workingDay.getDay() + "\n" + workingDay.getName());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        holder.vHours.setText(String.format("%s Hour(s)", (int) workingDay.getHours()));
    }

    @Override
    public int getItemCount() {
        return workingDays.size();
    }
}
