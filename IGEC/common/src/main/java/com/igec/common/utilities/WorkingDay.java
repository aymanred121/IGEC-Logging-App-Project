package com.igec.common.utilities;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

public class WorkingDay {
    private String day;
    private String month, year;
    private HashMap<String,Double> hours;
    private String name,empName;
    private LocationDetails checkIn, checkOut;
    private String projectName,projectLocation;
    private String type;

    public WorkingDay(String day, String month, String year, HashMap<String,Double> hours, String empName , LocationDetails checkIn, LocationDetails checkOut, String projectName,String projectLocation,String type)    {
        this.day = day;
        this.hours = hours;
        this.month = month;
        this.year = year;
        this.empName = empName;
        this.checkIn = checkIn;
        this.checkOut = checkOut;
        this.projectName = projectName;
        this.projectLocation = projectLocation;
        this.type = type;
    }

    public String getDay() {
        return day;
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

    public String getType() {
        return type;
    }

    public HashMap<String, Double> getHours() {
        return hours;
    }

    public void setHours(HashMap<String, Double> hours) {
        this.hours = hours;
    }

    public void setType(String type) {
        this.type = type;
    }
}
