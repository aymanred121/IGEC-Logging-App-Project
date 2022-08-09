package com.igec.admin.fireBase;

import java.util.ArrayList;

public class EmployeesGrossSalary {
    private String employeeId;
    private ArrayList<Allowance> allTypes = new ArrayList<>();
    private ArrayList<Allowance> baseAllowances = new ArrayList<>();

    public EmployeesGrossSalary() {
    }

    public EmployeesGrossSalary(String employeeId , ArrayList<Allowance> allTypes) {
        this.employeeId = employeeId;
        this.allTypes = allTypes;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }


    public ArrayList<Allowance> getAllTypes() {
        return allTypes;
    }

    public void setAllTypes(ArrayList<Allowance> allTypes) {
        this.allTypes = allTypes;
    }

    public ArrayList<Allowance> getBaseAllowances() {
        return baseAllowances;
    }

    public void setBaseAllowances(ArrayList<Allowance> baseAllowances) {
        this.baseAllowances = baseAllowances;
    }
}
