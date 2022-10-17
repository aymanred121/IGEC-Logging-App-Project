package com.igec.admin.adapters;

import static com.igec.common.CONSTANTS.CHECK_IN_FROM_HOME;
import static com.igec.common.CONSTANTS.CHECK_IN_FROM_OFFICE;
import static com.igec.common.CONSTANTS.CHECK_IN_FROM_SITE;
import static com.igec.common.CONSTANTS.CHECK_IN_FROM_SUPPORT;
import static com.igec.common.CONSTANTS.OFFICE_REF;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.igec.admin.R;
import com.igec.admin.databinding.ItemWorkdayBinding;
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
        public TextView vType_text;
        public ImageView vType_image;

        public DayViewHolder(@NonNull ItemWorkdayBinding itemView, OnItemClickListener listener) {
            super(itemView.getRoot());
            vDay = itemView.TextViewDay;
            vHours = itemView.TextViewHours;
            vType_text = itemView.typeText;
            vType_image = itemView.typeImage;
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

    public WorkingDayAdapter(ArrayList<WorkingDay> workingDays) {
        this.workingDays = workingDays;
    }

    @NonNull
    @Override
    public DayViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemWorkdayBinding binding = ItemWorkdayBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new DayViewHolder(binding, listener);
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
        int hours = (int) workingDay.getHours();
        int minutes = (int) ((workingDay.getHours() - hours) * 60);
        holder.vHours.setText(String.format("%d %s %d %s", hours, hours > 1 ? "Hrs" : "Hr", minutes, "Min"));
        holder.vType_text.setText(workingDay.getProjectName());
        switch (workingDay.getType()) {
            case CHECK_IN_FROM_HOME:
                holder.vType_image.setImageResource(R.drawable.ic_home_work);
                break;
            case CHECK_IN_FROM_OFFICE:
                holder.vType_image.setImageResource(R.drawable.ic_office);
                break;
            case CHECK_IN_FROM_SITE:
                if (workingDay.getProjectReference().equals(OFFICE_REF))
                    holder.vType_image.setImageResource(R.drawable.ic_office);
                else
                    holder.vType_image.setImageResource(R.drawable.ic_site);
                break;
            case CHECK_IN_FROM_SUPPORT:
                holder.vType_image.setImageResource(R.drawable.ic_support);
                break;
        }
    }

    @Override
    public int getItemCount() {
        return workingDays.size();
    }
}
