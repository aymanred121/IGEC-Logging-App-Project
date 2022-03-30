package com.example.igecuser.Adapters;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.igecuser.R;
import com.example.igecuser.dummySalarySummary;
import com.example.igecuser.dummySalarySummary.SalaryType;

import java.util.ArrayList;

public class SalarySummaryAdapter extends RecyclerView.Adapter<SalarySummaryAdapter.SalarySummaryViewHolder> {
    private final ArrayList<dummySalarySummary> salarySummaries;
    private OnItemClickListener listener;

    public SalarySummaryAdapter(ArrayList<dummySalarySummary> salarySummaries) {
        this.salarySummaries = salarySummaries;
    }


    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }


    @NonNull
    @Override
    public SalarySummaryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from((parent.getContext())).inflate(R.layout.salary_summary_item, parent, false);
        return new SalarySummaryAdapter.SalarySummaryViewHolder(v, listener);
    }

    @Override
    public void onBindViewHolder(@NonNull SalarySummaryViewHolder holder, int position) {
        dummySalarySummary dummySalarySummary = salarySummaries.get(position);
        holder.vReasonFor.setText(dummySalarySummary.getReasonFor());
        holder.vMountOf.setText(String.format("%s EGP", dummySalarySummary.getMountOf()));
        if (dummySalarySummary.getSalaryType() == SalaryType.allowance || dummySalarySummary.getSalaryType() == SalaryType.overtime) {
            holder.vMountOf.setTextColor(Color.rgb(0, 153, 0));
            holder.vMountOf.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_round_trending_up_24, 0, 0, 0);
            setTextViewDrawableColor(holder.vMountOf, Color.rgb(0, 153, 0));

        } else if (dummySalarySummary.getSalaryType() == SalaryType.penalty) {
            holder.vMountOf.setTextColor(Color.rgb(153, 0, 0));
            holder.vMountOf.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_round_trending_down_24, 0, 0, 0);
            setTextViewDrawableColor(holder.vMountOf, Color.rgb(153, 0, 0));
        } else if (dummySalarySummary.getSalaryType() == SalaryType.base) {
            holder.vMountOf.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_round_trending_flat_24, 0, 0, 0);
        }
    }

    @Override
    public int getItemCount() {
        return salarySummaries.size();
    }

    private void setTextViewDrawableColor(TextView textView, int color) {
        for (Drawable drawable : textView.getCompoundDrawables()) {
            if (drawable != null) {
                drawable.setColorFilter(color, PorterDuff.Mode.MULTIPLY);
            }
        }
    }

    public static class SalarySummaryViewHolder extends RecyclerView.ViewHolder {
        public TextView vReasonFor;
        public TextView vMountOf;

        public SalarySummaryViewHolder(@NonNull View itemView, OnItemClickListener listener) {
            super(itemView);
            vReasonFor = itemView.findViewById(R.id.TextView_ReasonFor);
            vMountOf = itemView.findViewById(R.id.TextView_MountOf);
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
}
