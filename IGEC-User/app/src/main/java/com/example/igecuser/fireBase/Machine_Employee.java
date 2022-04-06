package com.example.igecuser.fireBase;

public class Machine_Employee extends Summary {
    private Machine machine;
    private double cost;
    public Machine_Employee() {
    }



    public Machine_Employee(double lat, double lng, Machine machine) {
        super(lat, lng);
        this.machine = machine;
    }

    public Machine getMachine() {
        return machine;
    }

    public void setMachine(Machine machine) {
        this.machine = machine;
    }

    public double getCost() {
        return cost;
    }

    public void setCost(double cost) {
        this.cost = cost;
    }
}
