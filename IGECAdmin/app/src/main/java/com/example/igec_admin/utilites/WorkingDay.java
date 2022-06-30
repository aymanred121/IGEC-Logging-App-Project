package com.example.igec_admin.utilites;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class WorkingDay {
    private String day;
    private String month, year;
    private double hours;
    private String name;

    public WorkingDay(String day, String month, String year, double hours) {
        this.day = day;
        this.hours = hours;
        this.month = month;
        this.year = year;
    }

    public String getDay() {
        return day;
    }

    public double getHours() {
        return hours;
    }

    public String getName() throws ParseException {
        String sDate = day + "/" + month + "/" + year;
        Date date = new SimpleDateFormat("dd/MM/yyyy").parse(sDate);
        SimpleDateFormat outFormat = new SimpleDateFormat("EEE");
        String goal = outFormat.format(date);
        return goal;
    }
}
