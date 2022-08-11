package com.igec.admin.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.igec.admin.R;
import com.igec.common.firebase.Supplement;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;

public class SupplementAdapter extends RecyclerView.Adapter<SupplementAdapter.SupplementViewHolder> {
    private ArrayList<Supplement> supplements;
    private OnItemClickListener listener;

    public SupplementAdapter(ArrayList<Supplement> supplements) {
        this.supplements = supplements;
    }

    @NonNull
    @Override
    public SupplementViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from((parent.getContext())).inflate(R.layout.item_supplement,parent,false);
        return new SupplementViewHolder(v,listener);
    }

    @Override
    public void onBindViewHolder(@NonNull SupplementViewHolder holder, int position) {
        Supplement supplement = supplements.get(position);
        holder.vName.setText(supplement.getName());
        holder.vImg.setImageBitmap(supplement.getPhoto());

    }

    @Override
    public int getItemCount() {
        return supplements.size();
    }

    public interface OnItemClickListener{
        void onItemClick(int position);
        void onDeleteItem(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }
    public ArrayList<Supplement> getSupplements() {
        return supplements;
    }

    public void setSupplements(ArrayList<Supplement> supplements) {
        this.supplements = supplements;
    }

    public OnItemClickListener getListener() {
        return listener;
    }

    public void setListener(OnItemClickListener listener) {
        this.listener = listener;
    }
    public static class SupplementViewHolder extends RecyclerView.ViewHolder{
        public TextView vName;
        public ImageView vImg;
        public MaterialButton vDelete;

        public SupplementViewHolder(@NonNull View itemView, OnItemClickListener listener) {
            super(itemView);

            vName = itemView.findViewById(R.id.TextView_SupplementName);
            vImg = itemView.findViewById(R.id.TextView_SupplementIMG);
            vDelete = itemView.findViewById(R.id.delete_button);

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

        }
    }

}
