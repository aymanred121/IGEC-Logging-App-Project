package com.example.igec_admin.fireBase;

import java.util.Date;

public class Machine {
    private String id, reference;
    private double monthlyRentPrice,dailyRentPrice,weeklyRentPrice;
    private Date purchaseDate;
    private Boolean isUsed;


    public Allowance getAllowance() {
        return allowance;
    }

    public void setAllowance(Allowance allowance) {
        this.allowance = allowance;
    }

    private Allowance allowance ;

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

    public Machine(String id, String codeName, Date buyingDate , Allowance allowance) {
        this.id = id;
        this.reference = codeName;
        this.purchaseDate = buyingDate;
        this.allowance = allowance;
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

    public Boolean getUsed() {
        return isUsed;
    }

    public void setUsed(Boolean used) {
        isUsed = used;
    }

    public void setWeeklyRentPrice(double weeklyRentPrice) {
        this.weeklyRentPrice = weeklyRentPrice;
    }
}
