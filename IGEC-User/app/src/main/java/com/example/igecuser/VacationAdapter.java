package com.example.igecuser;

import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.example.igecuser.fireBase.VacationRequest;
import com.google.android.material.checkbox.MaterialCheckBox;

import java.util.ArrayList;

public class VacationAdapter extends RecyclerView.Adapter<VacationAdapter.VacationViewHolder> {
    private ArrayList<VacationRequest> vacationsList;
    private OnItemClickListener listener;

    public interface OnItemClickListener{
        void onItemClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public static class VacationViewHolder extends RecyclerView.ViewHolder{

        public TextView vName;
        public TextView vID;
        public TextView vVacationStartDate;
        public TextView vVacationDays;
        public ImageView vVacationsStatus;


        public VacationViewHolder(@NonNull View itemView,OnItemClickListener listener) {
            super(itemView);
            vName = itemView.findViewById(R.id.TextView_Name);
            vID = itemView.findViewById(R.id.TextView_Id);
            vVacationDays = itemView.findViewById(R.id.TextView_VacationDays);
            vVacationsStatus = itemView.findViewById(R.id.ImageView_VacationStatus);
            vVacationStartDate = itemView.findViewById(R.id.TextView_VacationStartDate);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(listener != null)
                    {
                        int position = getAdapterPosition();
                        if(position != RecyclerView.NO_POSITION)
                        {
                            listener.onItemClick(position);
                        }
                    }
                }
            });


        }
    }

    public VacationAdapter(ArrayList<VacationRequest> vacationslist) {
        this.vacationsList = vacationslist;
    }

    @NonNull
    @Override
    public VacationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from((parent.getContext())).inflate(R.layout.vacation_request_item,parent,false);
        VacationViewHolder vvh = new VacationViewHolder(v,listener);
        return vvh;
    }

    @Override
    public void onBindViewHolder(@NonNull VacationViewHolder holder, int position) {
        VacationRequest vacation = vacationsList.get(position);
        holder.vName.setText("Name: " + vacation.getEmployee().getFirstName()+" "+vacation.getEmployee().getLastName());
        holder.vID.setText("ID: ");
        holder.vVacationStartDate.setText("Start Date: " + vacation.getStartDate());
        //Todo fix missing img
        //holder.vVacationsStatus.setImageResource(vacation.getVacationStatus());
        holder.vVacationDays.setText("for : " + getDays(vacation) + " days");
    }

    private String getDays(VacationRequest vacation) {
        long days= vacation.getEndDate().getTime()-vacation.getStartDate().getTime();
        days /=(24*3600*1000);
        return String.valueOf(days);
    }

    public ArrayList<VacationRequest> getVacationsList() {
        return vacationsList;
    }

    public void setVacationsList(ArrayList<VacationRequest> vacationsList) {
        this.vacationsList = vacationsList;
    }

    @Override
    public int getItemCount() {
        return vacationsList.size();
    }
}