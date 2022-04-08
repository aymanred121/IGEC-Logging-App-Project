package com.example.igecuser;

public class Employee {
    private String name;
    private int id, hours, machine;

    public Employee(String name, int id, int hours, int machine) {
        this.name = name;
        this.id = id;
        this.hours = hours;
        this.machine = machine;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getHours() {
        return hours;
    }

    public void setHours(int hours) {
        this.hours = hours;
    }

    public int getMachine() {
        return machine;
    }

    public void setMachine(int machine) {
        this.machine = machine;
    }
}
