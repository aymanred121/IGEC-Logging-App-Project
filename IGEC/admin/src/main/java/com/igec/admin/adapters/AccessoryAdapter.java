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
        View v = LayoutInflater.from((parent.getContext())).inflate(R.layout.item_accessory,parent,false);
        return new AccessoryViewHolder(v,listener);
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

        public AccessoryViewHolder(@NonNull View itemView, OnItemClickListener listener) {
            super(itemView);

            vName = itemView.findViewById(R.id.name_text);
            vImg = itemView.findViewById(R.id.image_image_view);
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
