package com.example.igec_admin.fireBase;

import java.util.ArrayList;

public class EmployeesGrossSalary {
    /*
    * year -> months
    * month -> data
    * data :empid,alltypes
    * */
    private String employeeId;
    private ArrayList<Allowance> allTypes = new ArrayList<>();

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


}
