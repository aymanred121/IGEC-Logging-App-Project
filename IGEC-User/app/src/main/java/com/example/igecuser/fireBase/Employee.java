package com.example.igecuser.fireBase;

import java.io.Serializable;
import java.util.Date;
@SuppressWarnings("serial")
public class Employee  implements Serializable {
    private String firstName, lastName, title, area, city, street,SSN,projectID,managerID,email,password;
    private double salary;
    private Date hireDate;

    public  Employee(){

    }

    public Employee(String firstName, String lastName, String Title, String area, String City, String Street, String projectID, double salary, String SSN, Date hireDate,String email,String password) {
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
        this.email=email;
        this.password=password;
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
