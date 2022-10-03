package com.igec.common.firebase;

import com.google.firebase.firestore.Exclude;
import com.igec.common.cryptography.RSAUtil;

import java.io.Serializable;
import java.util.Date;

public class Employee implements Serializable {
    private String firstName, lastName, title, area, city, street,SSN,projectID,managerID,email,password,id,phoneNumber,insuranceNumber;
    private double salary,insuranceAmount,overTime;
    private Date hireDate;
    private int totalNumberOfVacationDays;
    private boolean isAdmin, temporary,locked , isManager;
    private String currency;

    public  Employee(){

    }

    public Employee(String firstName, String lastName, String Title, String area, String City, String Street, double salary,String currency,double overTime, String SSN, Date hireDate, String email, String password,String phoneNumber,String insuranceNumber,boolean labourType,double insuranceAmount,boolean isAdmin ,boolean isManager) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.title = Title;
        this.area = area;
        this.city = City;
        this.street = Street;
        this.salary = salary;
        this.currency = currency;
        this.SSN = SSN;
        this.hireDate = hireDate;
        this.email=email;
        this.password=password;
        this.phoneNumber = phoneNumber;
        totalNumberOfVacationDays = 21;
        this.insuranceNumber=insuranceNumber;
        this.temporary =labourType;
        this.insuranceAmount=insuranceAmount;
        this.isAdmin=isAdmin;
        this.overTime=overTime;
        this.isManager=isManager;
    }

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
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

    public String getInsuranceNumber() {
        return insuranceNumber;
    }

    public void setInsuranceNumber(String insuranceNumber) {
        this.insuranceNumber = insuranceNumber;
    }

    public boolean isAdmin() {
        return isAdmin;
    }

    public void setAdmin(boolean admin) {
        isAdmin = admin;
    }

    public boolean isTemporary() {
        return temporary;
    }

    public void setTemporary(boolean temporary) {
        this.temporary = temporary;
    }

    public double getInsuranceAmount() {
        return insuranceAmount;
    }

    public void setInsuranceAmount(double insuranceAmount) {
        this.insuranceAmount = insuranceAmount;
    }

    public void setSSN(String SSN) {
        this.SSN = SSN;
    }
    public String getManagerID() {
        return managerID;
    }
    public Employee setManagerID(String managerID) {
        this.managerID = managerID;
        return this;
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
    @Exclude public String getDecryptedPassword(){
        try {
            return RSAUtil.decrypt(password, RSAUtil.privateKey);
        }catch (Exception e){
            return null;
        }
    }
    public void setPassword(String password) {
        this.password = password;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public int getTotalNumberOfVacationDays() {
        return totalNumberOfVacationDays;
    }

    public void setTotalNumberOfVacationDays(int totalNumberOfVacationDays) {
        this.totalNumberOfVacationDays = totalNumberOfVacationDays;
    }

    public double getOverTime() {
        return overTime;
    }

    public void setOverTime(double overTime) {
        this.overTime = overTime;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public boolean isManager() {
        return isManager;
    }

    public void setManager(boolean manager) {
        isManager = manager;
    }
}