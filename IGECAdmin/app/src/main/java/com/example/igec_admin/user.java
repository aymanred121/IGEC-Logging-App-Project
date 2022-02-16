package com.example.igec_admin;

public class user {
    private String FirstName , LastName , Title , Area ,City ,Street ;
    private int projectID , Salary ,SSN;

    public  user(){

    }

    public user(String firstName, String lastName, String Title, String area, String City, String Street, int projectID, int Salary, int SSN) {
        this.FirstName = firstName;
        this.LastName = lastName;
        this.Title = Title;
        this.Area = area;
        this.City = City;
        this.Street = Street;
        this.projectID = projectID;
        this.Salary = Salary;
        this.SSN = SSN;
    }

    public String getFirstName() {
        return FirstName;
    }

    public void setFirstName(String firstName) {
        FirstName = firstName;
    }

    public String getLastName() {
        return LastName;
    }

    public void setLastName(String lastName) {
        LastName = lastName;
    }

    public String getTitle() {
        return Title;
    }

    public void setTitle(String title) {
        Title = title;
    }

    public String getArea() {
        return Area;
    }

    public void setArea(String area) {
        Area = area;
    }

    public String getCity() {
        return City;
    }

    public void setCity(String city) {
        City = city;
    }

    public String getStreet() {
        return Street;
    }

    public void setStreet(String street) {
        Street = street;
    }

    public int getProjectID() {
        return projectID;
    }

    public void setProjectID(int projectID) {
        this.projectID = projectID;
    }

    public int getSalary() {
        return Salary;
    }

    public void setSalary(int salary) {
        Salary = salary;
    }

    public int getSSN() {
        return SSN;
    }

    public void setSSN(int SSN) {
        this.SSN = SSN;
    }
}
