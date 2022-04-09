package com.example.igecuser.fireBase;

import java.util.ArrayList;

public class EmployeesGrossSalary {
    private String employeeId;
    private double netSalary;
    private ArrayList<Allowance> projectAllowances = new ArrayList<>(), penalties = new ArrayList<>(), bonuses = new ArrayList<>(), allowances = new ArrayList<>();

    public EmployeesGrossSalary() {
    }

    public EmployeesGrossSalary(String employeeId, double netSalary, ArrayList<Allowance> allowances, ArrayList<Allowance> penalties, ArrayList<Allowance> bonuses, ArrayList<Allowance> allowances1) {
        this.employeeId = employeeId;
        this.netSalary = netSalary;
        this.projectAllowances = allowances;
        this.penalties = penalties;
        this.bonuses = bonuses;
        this.allowances = allowances1;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }


    public ArrayList<Allowance> getProjectAllowances() {
        return projectAllowances;
    }

    public void setProjectAllowances(ArrayList<Allowance> projectAllowances) {
        this.projectAllowances = projectAllowances;
    }

    public ArrayList<Allowance> getPenalties() {
        return penalties;
    }

    public void setPenalties(ArrayList<Allowance> penalties) {
        this.penalties = penalties;
    }

    public ArrayList<Allowance> getBonuses() {
        return bonuses;
    }

    public void setBonuses(ArrayList<Allowance> bonuses) {
        this.bonuses = bonuses;
    }

    public double getNetSalary() {
        return netSalary;
    }

    public void setNetSalary(double netSalary) {
        this.netSalary = netSalary;
    }

    public ArrayList<Allowance> getAllowances() {
        return allowances;
    }

    public void setAllowances(ArrayList<Allowance> allowances) {
        this.allowances = allowances;
    }
}
