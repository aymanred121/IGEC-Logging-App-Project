package com.example.igecuser.fireBase;

import java.io.Serializable;

public class Allowance implements Serializable {
    private String name;
    private double amount;
    //TODO added String type = [Net Salary, Allowance, Penalty, Bonus, Project]

    public Allowance() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public Allowance(String name, double amount) {
        this.name = name;
        this.amount = amount;
    }
}
