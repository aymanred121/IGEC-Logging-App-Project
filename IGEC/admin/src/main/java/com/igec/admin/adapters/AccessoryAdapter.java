package com.igec.admin.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.igec.admin.R;
import com.igec.admin.databinding.ItemAccessoryBinding;
import com.igec.common.firebase.Accessory;

import java.util.ArrayList;

public class AccessoryAdapter extends RecyclerView.Adapter<AccessoryAdapter.AccessoryViewHolder> {
    private ArrayList<Accessory> accessories;
    private OnItemClickListener listener;

    public AccessoryAdapter(ArrayList<Accessory> accessories) {
        this.accessories = accessories;
    }

    @NonNull
    @Override
    public AccessoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemAccessoryBinding binding  = ItemAccessoryBinding.inflate(LayoutInflater.from(parent.getContext()),parent,false);
        return new AccessoryViewHolder(binding,listener);
    }

    @Override
    public void onBindViewHolder(@NonNull AccessoryViewHolder holder, int position) {
        Accessory accessory = accessories.get(position);
        holder.vName.setText(accessory.getName());
        holder.vImg.setImageBitmap(accessory.getPhoto());

    }

    @Override
    public int getItemCount() {
        return accessories.size();
    }

    public interface OnItemClickListener{
        void onItemClick(int position);
        void onDeleteItem(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }
    public ArrayList<Accessory> getSupplements() {
        return accessories;
    }

    public void setSupplements(ArrayList<Accessory> accessories) {
        this.accessories = accessories;
    }

    public OnItemClickListener getListener() {
        return listener;
    }

    public void setListener(OnItemClickListener listener) {
        this.listener = listener;
    }
    public static class AccessoryViewHolder extends RecyclerView.ViewHolder{
        public TextView vName;
        public ImageView vImg;
        public FloatingActionButton vDelete;

        public AccessoryViewHolder(@NonNull ItemAccessoryBinding itemView, OnItemClickListener listener) {
            super(itemView.getRoot());

            vName = itemView.nameText;
            vImg = itemView.imageImageView;
            vDelete = itemView.deleteButton;

            itemView.getRoot().setOnClickListener(v -> {
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
