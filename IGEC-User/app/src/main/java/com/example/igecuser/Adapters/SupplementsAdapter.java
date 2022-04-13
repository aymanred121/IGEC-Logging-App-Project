package com.example.igecuser.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.igecuser.R;
import com.example.igecuser.fireBase.Supplement;

import java.util.ArrayList;

public class SupplementsAdapter extends RecyclerView.Adapter<SupplementsAdapter.SupplementViewHolder> {

    private ArrayList<Supplement> supplements;

    public SupplementsAdapter(ArrayList<Supplement> supplements) {
        this.supplements = supplements;
    }


    @NonNull
    @Override
    public SupplementViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from((parent.getContext())).inflate(R.layout.item_supplement, parent, false);
        return new SupplementViewHolder(v);
    }


    @Override
    public void onBindViewHolder(@NonNull SupplementViewHolder holder, int position) {
        Supplement supplement = supplements.get(position);
        holder.vSupplementName.setText(supplement.getName());
        holder.vSupplementImage.setImageBitmap(supplement.getPhoto());
    }

    @Override
    public int getItemCount() {
        return supplements.size();
    }

    public ArrayList<Supplement> getSupplements() {
        return supplements;
    }

    public void setSupplements(ArrayList<Supplement> supplements) {
        this.supplements = supplements;
    }

    static class SupplementViewHolder extends RecyclerView.ViewHolder {
        public TextView vSupplementName;
        public ImageView vSupplementImage;

        public SupplementViewHolder(@NonNull View itemView) {
            super(itemView);
            vSupplementName = itemView.findViewById(R.id.TextView_supplementName);
            vSupplementImage = itemView.findViewById(R.id.ImageView_supplementImage);
        }
    }
}
