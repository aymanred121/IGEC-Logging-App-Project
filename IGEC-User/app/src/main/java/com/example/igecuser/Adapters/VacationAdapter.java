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
        holder.vID.setText("ID: " + vacation.getEmployee().getId());

        holder.vVacationStartDate.setText("Start Date: " + formatDate(vacation.getStartDate()) );
        //Todo fix missing img
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
        holder.vVacationDays.setText("for : " + getDays(vacation) + " days");
    }

    private String formatDate(Date Date)
    {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy");
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis((long) Date.getTime());
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
}