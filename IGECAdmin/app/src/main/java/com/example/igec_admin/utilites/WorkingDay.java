package com.example.igec_admin.utilites;

import com.example.igec_admin.fireBase.Project;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class WorkingDay {
    private String day;
    private String month, year;
    private double hours;
    private String name,empName;
    private LocationDetails checkIn, checkOut;
    private String projectName,projectLocation;

    public WorkingDay(String day, String month, String year, double hours, String empName , LocationDetails checkIn, LocationDetails checkOut, String projectName,String projectLocation) {
        this.day = day;
        this.hours = hours;
        this.month = month;
        this.year = year;
        this.empName = empName;
        this.checkIn = checkIn;
        this.checkOut = checkOut;
        this.projectName = projectName;
        this.projectLocation = projectLocation;
    }

    public String getDay() {
        return day;
    }

    public double getHours() {
        return hours;
    }

    public String getEmpName() {
        return empName;
    }

    public void setEmpName(String empName) {
        this.empName = empName;
    }

    public void setDay(String day) {
        this.day = day;
    }

    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public void setHours(double hours) {
        this.hours = hours;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocationDetails getCheckIn() {
        return checkIn;
    }

    public LocationDetails getCheckOut() {
        return checkOut;
    }

    public String getProjectName() {
        return projectName;
    }

    public String getProjectLocation() {
        return projectLocation;
    }

    public String getName() throws ParseException {
        String sDate = day + "/" + month + "/" + year;
        Date date = new SimpleDateFormat("dd/MM/yyyy").parse(sDate);
        SimpleDateFormat outFormat = new SimpleDateFormat("EEE");
        String goal = outFormat.format(date);
        return goal;
    }
}
