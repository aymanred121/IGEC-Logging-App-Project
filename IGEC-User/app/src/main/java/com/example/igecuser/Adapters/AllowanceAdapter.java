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
import com.example.igecuser.fireBase.Allowance;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;

public class AllowanceAdapter extends RecyclerView.Adapter<AllowanceAdapter.AllowanceViewHolder> {
    private ArrayList<Allowance> allowances;
    private OnItemClickListener listener;
    private boolean canRemove;

    public AllowanceAdapter(ArrayList<Allowance> supplements, boolean canRemoveItem) {
        this.allowances = supplements;
        this.canRemove = canRemoveItem;
    }

    @NonNull
    @Override
    public AllowanceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from((parent.getContext())).inflate(canRemove ? R.layout.allowance_item : R.layout.salary_summary_item, parent, false);
        return new AllowanceViewHolder(v, listener,canRemove);
    }

    @Override
    public void onBindViewHolder(@NonNull AllowanceViewHolder holder, int position) {
        Allowance allowance = allowances.get(position);
        holder.vName.setText(allowance.getName());
        holder.vAmount.setText(String.format("%s EGP", allowance.getAmount()));

        if (allowance.getAmount() > 0) {
            holder.vAmount.setTextColor(Color.rgb(0, 153, 0));
            holder.vAmount.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_round_trending_up_24, 0, 0, 0);
            setTextViewDrawableColor(holder.vAmount, Color.rgb(0, 153, 0));
        } else {
            holder.vAmount.setTextColor(Color.rgb(153, 0, 0));
            holder.vAmount.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_round_trending_down_24, 0, 0, 0);
            setTextViewDrawableColor(holder.vAmount, Color.rgb(153, 0, 0));
        }
    }

    private void setTextViewDrawableColor(TextView textView, int color) {
        for (Drawable drawable : textView.getCompoundDrawables()) {
            if (drawable != null) {
                drawable.setColorFilter(color, PorterDuff.Mode.MULTIPLY);
            }
        }
    }

    @Override
    public int getItemCount() {
        return allowances.size();
    }

    public interface OnItemClickListener {
        void onItemClick(int position);

        void onDeleteItem(int position);
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

    public static class AllowanceViewHolder extends RecyclerView.ViewHolder {
        public TextView vName, vAmount;
        public MaterialButton vDelete;

        public AllowanceViewHolder(@NonNull View itemView, OnItemClickListener listener, boolean canRemove) {
            super(itemView);

            vName = itemView.findViewById(R.id.TextView_ReasonFor);
            vAmount = itemView.findViewById(R.id.TextView_MountOf);

            if(canRemove) {
                vDelete = itemView.findViewById(R.id.button_delete);

                vDelete.setOnClickListener(v -> {
                    if (listener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            listener.onDeleteItem(position);
                        }
                    }
                });
            }
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

