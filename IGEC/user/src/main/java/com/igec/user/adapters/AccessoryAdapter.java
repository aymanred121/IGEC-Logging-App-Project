package com.igec.user.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.igec.common.firebase.Accessory;
import com.igec.user.databinding.ItemAccessoryBinding;

import java.util.ArrayList;

public class AccessoryAdapter extends RecyclerView.Adapter<AccessoryAdapter.AccessoryViewHolder> {

    private final ArrayList<Accessory> accessories;

    public AccessoryAdapter(ArrayList<Accessory> accessories) {
        this.accessories = accessories;
    }


    @NonNull
    @Override
    public AccessoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemAccessoryBinding binding  = ItemAccessoryBinding.inflate(LayoutInflater.from(parent.getContext()),parent,false);
        return new AccessoryViewHolder(binding);
    }


    @Override
    public void onBindViewHolder(@NonNull AccessoryViewHolder holder, int position) {
        Accessory accessory = accessories.get(position);
        holder.vName.setText(accessory.getName());
        holder.vImage.setImageBitmap(accessory.getPhoto());
    }

    @Override
    public int getItemCount() {
        return accessories.size();
    }

    static class AccessoryViewHolder extends RecyclerView.ViewHolder {
        public TextView vName;
        public ImageView vImage;

        public AccessoryViewHolder(@NonNull ItemAccessoryBinding itemView) {
            super(itemView.getRoot());
            vName = itemView.nameText;
            vImage = itemView.imageImageView;
        }
    }
}
