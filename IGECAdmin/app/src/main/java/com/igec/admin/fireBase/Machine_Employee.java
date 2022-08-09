package com.igec.admin.fireBase;

import java.util.HashMap;
import java.util.Map;

public class Machine_Employee {
    private Machine machine;
    private Employee employee;
    private long workedTime;
    private Map<String,Object>checkIn;
    private Map<String, Object> CheckOut=new HashMap<>();
    private double cost;
    private Client client;

    public Machine_Employee() {
    }

    public Machine_Employee(Machine machine, Employee employee) {
        this.machine = machine;
        this.employee = employee;
    }

    public long getWorkedTime() {
        return workedTime;
    }

    public void setWorkedTime(long workedTime) {
        this.workedTime = workedTime;
    }

    public Machine getMachine() {
        return machine;
    }

    public void setMachine(Machine machine) {
        this.machine = machine;
    }

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    public Map<String, Object> getCheckIn() {
        return checkIn;
    }

    public Map<String, Object> getCheckOut() {
        return CheckOut;
    }

    public void setCheckOut(Map<String, Object> checkOut) {
        CheckOut = checkOut;
    }

    public void setCheckIn(Map<String, Object> checkIn) {
        this.checkIn = checkIn;
    }

    public double getCost() {
        return cost;
    }

    public void setCost(double cost) {
        this.cost = cost;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    public Client getClient() {
        return client;
    }
}
