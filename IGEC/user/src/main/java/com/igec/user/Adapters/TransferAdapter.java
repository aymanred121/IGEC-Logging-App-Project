package com.igec.user.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.igec.user.R;
import com.igec.common.firebase.TransferRequests;

import java.util.ArrayList;

public class TransferAdapter extends RecyclerView.Adapter<TransferAdapter.TransferViewHolder> {

    private ArrayList<TransferRequests> transfers;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public OnItemClickListener getOnItemClickListener() {
        return listener;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public TransferAdapter(ArrayList<TransferRequests> transfers) {
        this.transfers = transfers;
    }

    public ArrayList<TransferRequests> getTransfers() {
        return transfers;
    }

    public void setTransfers(ArrayList<TransferRequests> transfers) {
        this.transfers = transfers;
    }

    @NonNull
    @Override
    public TransferViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from((parent.getContext())).inflate(R.layout.item_transfer_request,parent,false);
        return new TransferViewHolder(v,listener);
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
        public TransferViewHolder(@NonNull View itemView, OnItemClickListener listener) {
            super(itemView);
            vTransferredEmployeeId = itemView.findViewById(R.id.TextView_TransferredEmployeeId);
            vTransferredEmployeeName = itemView.findViewById(R.id.TextView_TransferredEmployeeName);
            vTransferredEmployeeProject = itemView.findViewById(R.id.TextView_TransferredEmployeeProject);

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
        }
    }
}
