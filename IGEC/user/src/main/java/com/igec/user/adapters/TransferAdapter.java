package com.igec.user.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.igec.common.firebase.TransferRequests;
import com.igec.user.databinding.ItemTransferRequestBinding;

import java.util.ArrayList;

public class TransferAdapter extends RecyclerView.Adapter<TransferAdapter.TransferViewHolder> {

    private ArrayList<TransferRequests> transfers;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(int position);
    }


    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public TransferAdapter(ArrayList<TransferRequests> transfers) {
        this.transfers = transfers;
    }


    public void setTransfers(ArrayList<TransferRequests> transfers) {
        this.transfers = transfers;
    }

    @NonNull
    @Override
    public TransferViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemTransferRequestBinding binding  = ItemTransferRequestBinding.inflate(LayoutInflater.from(parent.getContext()),parent,false);
        return new TransferViewHolder(binding,listener);
    }

    @Override
    public void onBindViewHolder(@NonNull TransferViewHolder holder, int position) {
        TransferRequests transferRequest = transfers.get(position);
        holder.vTransferredEmployeeProject.setText(String.format("Project: IGEC%s | %s", transferRequest.getNewProjectReference(), transferRequest.getNewProjectId()));
        holder.vTransferredEmployeeId.setText(String.format("Employee Id: %s",transferRequest.getEmployee().getId()));
        holder.vTransferredEmployeeName.setText(String.format("Employee Name: %s %s", transferRequest.getEmployee().getFirstName(), transferRequest.getEmployee().getLastName()));
    }

    @Override
    public int getItemCount() {
        return transfers.size();
    }

    public static class TransferViewHolder extends RecyclerView.ViewHolder {
        public TextView vTransferredEmployeeName,vTransferredEmployeeId,vTransferredEmployeeProject;
        public TransferViewHolder(@NonNull ItemTransferRequestBinding itemView, OnItemClickListener listener) {
            super(itemView.getRoot());
            vTransferredEmployeeId = itemView.TextViewTransferredEmployeeId;
            vTransferredEmployeeName = itemView.TextViewTransferredEmployeeName;
            vTransferredEmployeeProject = itemView.TextViewTransferredEmployeeProject;

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
        }
    }
}
