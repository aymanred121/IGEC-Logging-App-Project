package com.igec.admin.adapters;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.igec.admin.databinding.HolidayItemBinding;
import com.igec.common.firebase.Holiday;

import java.util.ArrayList;

public class HolidayAdapter extends RecyclerView.Adapter<HolidayAdapter.HolidayViewHolder> {
    private ArrayList<Holiday> holidays;
    private HolidayAdapter.OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public void setOnItemClickListener(HolidayAdapter.OnItemClickListener listener) {
        this.listener = listener;
    }

    public static class HolidayViewHolder extends RecyclerView.ViewHolder {
        public HolidayItemBinding binding;

        public HolidayViewHolder(@NonNull HolidayItemBinding itemView, HolidayAdapter.OnItemClickListener listener) {
            super(itemView.getRoot());
            binding = itemView;
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

    public HolidayAdapter(ArrayList<Holiday> holidays) {
        this.holidays = holidays;
    }

    @NonNull
    @Override
    public HolidayAdapter.HolidayViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        HolidayItemBinding binding = HolidayItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new HolidayViewHolder(binding, listener);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull HolidayViewHolder holder, int position) {
        Holiday holiday = holidays.get(position);
        holder.binding.nameText.setText(holiday.getName());
        holder.binding.startDateText.setText(String.format("Starts on %s", holiday.convertDateToString(holiday.getStart())));
        holder.binding.endDateText.setText(String.format("Ends on %s", holiday.convertDateToString(holiday.getEnd())));
    }

    @Override
    public int getItemCount() {
        return holidays.size();
    }
}