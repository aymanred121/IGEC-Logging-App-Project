package com.example.igecuser.Adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.example.igecuser.R;
import com.example.igecuser.fireBase.VacationRequest;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class VacationAdapter extends RecyclerView.Adapter<VacationAdapter.VacationViewHolder> {
    private ArrayList<VacationRequest> vacationsList;
    private OnItemClickListener listener;

    public interface OnItemClickListener{
        void onItemClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public VacationAdapter(ArrayList<VacationRequest> vacationsList) {
        this.vacationsList = vacationsList;
    }

    @NonNull
    @Override
    public VacationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from((parent.getContext())).inflate(R.layout.item_vacation_request,parent,false);
        return new VacationViewHolder(v,listener);
        
    }

    @Override
    public void onBindViewHolder(@NonNull VacationViewHolder holder, int position) {
        VacationRequest vacation = vacationsList.get(position);
        holder.vName.setText(String.format("Name: %s %s", vacation.getEmployee().getFirstName(), vacation.getEmployee().getLastName()));
        holder.vID.setText(String.format("ID: %s", vacation.getEmployee().getId()));

        holder.vVacationStartDate.setText(String.format("Start Date: %s", formatDate(vacation.getStartDate())));
        switch (vacation.getVacationStatus()) {
            case 1:
            holder.vVacationsStatus.setColorFilter(Color.rgb(0, 153, 0));
            break;
            case -1:
            holder.vVacationsStatus.setColorFilter(Color.rgb(153,0,0));
            break;
            default:
                holder.vVacationsStatus.setColorFilter(Color.GRAY);
        }
        holder.vVacationDays.setText(String.format("for : %s days", getDays(vacation)));
    }

    private String formatDate(Date Date)
    {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(Date.getTime());
        return simpleDateFormat.format(calendar.getTime());
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