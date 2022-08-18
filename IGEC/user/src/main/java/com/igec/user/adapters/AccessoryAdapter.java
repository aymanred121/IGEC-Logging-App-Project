package com.igec.user.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.igec.user.R;
import com.igec.common.firebase.Accessory;

import java.util.ArrayList;

public class AccessoryAdapter extends RecyclerView.Adapter<AccessoryAdapter.AccessoryViewHolder> {

    private final ArrayList<Accessory> accessories;

    public AccessoryAdapter(ArrayList<Accessory> accessories) {
        this.accessories = accessories;
    }


    @NonNull
    @Override
    public AccessoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from((parent.getContext())).inflate(R.layout.item_accessory, parent, false);
        return new AccessoryViewHolder(v);
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

        public AccessoryViewHolder(@NonNull View itemView) {
            super(itemView);
            vName = itemView.findViewById(R.id.name_text);
            vImage = itemView.findViewById(R.id.image_image_view);
        }
    }
}
