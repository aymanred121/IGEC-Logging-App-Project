package com.example.igecuser;

public class dummySalarySummary {
    private String reasonFor;
    private float mountOf;


    private SalaryType salaryType;

    public dummySalarySummary(String reasonFor, float mountOf, SalaryType salaryType) {
        this.reasonFor = reasonFor;
        this.mountOf = mountOf;
        this.salaryType = salaryType;
    }

    public String getReasonFor() {
        return reasonFor;
    }

    public void setReasonFor(String reasonFor) {
        this.reasonFor = reasonFor;
    }

    public float getMountOf() {
        return mountOf;
    }

    public void setMountOf(float mountOf) {
        this.mountOf = mountOf;
    }

    public SalaryType getSalaryType() {
        return salaryType;
    }

    public void setSalaryType(SalaryType salaryType) {
        this.salaryType = salaryType;
    }

    public enum SalaryType{
        base,allowance,penalty,overtime
    }
}
