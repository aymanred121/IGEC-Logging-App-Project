package com.example.igecuser.fireBase;

public class Allowance {
    private String name ;
    private int amount ;

    public Allowance() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public Allowance(String name, int amount) {
        this.name = name;
        this.amount = amount;
    }
}
