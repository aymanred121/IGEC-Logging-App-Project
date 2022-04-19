package com.example.igec_admin.Adatpers;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.igec_admin.R;
import com.example.igec_admin.fireBase.Allowance;
import com.google.android.material.button.MaterialButton;

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
        View v = LayoutInflater.from((parent.getContext())).inflate(R.layout.item_allowance, parent, false);
        return new AllowanceViewHolder(v, listener);
    }

    @Override
    public void onBindViewHolder(@NonNull AllowanceViewHolder holder, int position) {
        Allowance allowance = allowances.get(position);
        holder.vName.setText(allowance.getName());
        holder.vAmount.setText(String.format("%s EGP", allowance.getAmount()));
        holder.vAmount.setTextColor(Color.rgb(0, 153, 0));
        holder.vAmount.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_round_trending_up_24, 0, 0, 0);
        setTextViewDrawableColor(holder.vAmount, Color.rgb(0, 153, 0));
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

        public AllowanceViewHolder(@NonNull View itemView, OnItemClickListener listener) {
            super(itemView);

            vName = itemView.findViewById(R.id.TextView_ReasonFor);
            vAmount = itemView.findViewById(R.id.TextView_MountOf);
            vDelete = itemView.findViewById(R.id.button_delete);

            vDelete.setOnClickListener(v->{
                if(listener != null)
                {
                    int position = getAdapterPosition();
                    if(position != RecyclerView.NO_POSITION)
                    {
                        listener.onDeleteItem(position);
                    }
                }
            });
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
