package com.example.igec_admin.fireBase;

import java.util.Date;

public class Employees {
    private String firstName, lastName, title, area, city, street,SSN,projectID,managerID;
    private double salary;
    private Date hireDate;

    public  Employees(){

    }

    public Employees(String firstName, String lastName, String Title, String area, String City, String Street, String projectID, double salary, String SSN, Date hireDate) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.title = Title;
        this.area = area;
        this.city = City;
        this.street = Street;
        this.projectID = projectID;
        this.salary = salary;
        this.SSN = SSN;
        this.hireDate=hireDate;
    }

    public Date getHireDate() {
        return hireDate;
    }

    public void setHireDate(Date hireDate) {
        this.hireDate = hireDate;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getProjectID() {
        return projectID;
    }

    public void setProjectID(String projectID) {
        this.projectID = projectID;
    }

    public double getSalary() {
        return salary;
    }

    public void setSalary(double salary) {
        this.salary = salary;
    }

    public String getSSN() {
        return SSN;
    }

    public void setSSN(String SSN) {
        this.SSN = SSN;
    }
    public String getManagerID() {
        return managerID;
    }
    public void setManagerID(String managerID) {
        this.managerID = managerID;
    }
}
