package com.igec.common.firebase;
import java.util.ArrayList;
import java.util.Date;

public class Machine {
    private String id, reference,employeeFirstName,employeeId,machineEmployeeID;
    private double monthlyRentPrice, dailyRentPrice, weeklyRentPrice;
    private Date purchaseDate;
    private Boolean isUsed;
    private ArrayList<String> supplementsNames;

    public Machine() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public Machine(String id, String reference, Date buyingDate) {
        this.id = id;
        this.reference = reference;
        this.purchaseDate = buyingDate;
        removeEmployeeDependency();
    }

    public Date getPurchaseDate() {
        return purchaseDate;
    }

    public void setPurchaseDate(Date purchaseDate) {
        this.purchaseDate = purchaseDate;
    }

    public double getMonthlyRentPrice() {
        return monthlyRentPrice;
    }

    public void setMonthlyRentPrice(double monthlyRentPrice) {
        this.monthlyRentPrice = monthlyRentPrice;
    }

    public double getDailyRentPrice() {
        return dailyRentPrice;
    }

    public void setDailyRentPrice(double dailyRentPrice) {
        this.dailyRentPrice = dailyRentPrice;
    }

    public double getWeeklyRentPrice() {
        return weeklyRentPrice;
    }

    public Boolean getIsUsed() {
        return isUsed;
    }

    public void setIsUsed(Boolean isUsed) {
        this.isUsed = isUsed;
    }

    public void setWeeklyRentPrice(double weeklyRentPrice) {
        this.weeklyRentPrice = weeklyRentPrice;
    }

    public ArrayList<String> getSupplementsNames() {
        return supplementsNames;
    }

    public void setSupplementsNames(ArrayList<String> supplementsNames) {
        this.supplementsNames = supplementsNames;
    }

    public String getEmployeeFirstName() {
        return employeeFirstName;
    }

    public void setEmployeeFirstName(String employeeFirstName) {
        this.employeeFirstName = employeeFirstName;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }

    public String getMachineEmployeeID() {
        return machineEmployeeID;
    }

    public void setMachineEmployeeID(String machineEmployeeID) {
        this.machineEmployeeID = machineEmployeeID;
    }
    public void removeEmployeeDependency(){
        isUsed=false;
        employeeFirstName="";
        employeeId="";
        machineEmployeeID="";
    }
}

