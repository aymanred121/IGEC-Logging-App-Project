package com.example.igec_admin;

public class user {
    private String Name;
    private int projectID;
    private int SSN;
    public  user(){

    }
    public user(String name, int projectID, int SSN) {
        Name = name;
        this.projectID = projectID;
        this.SSN = SSN;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public int getProjectID() {
        return projectID;
    }

    public void setProjectID(int projectID) {
        this.projectID = projectID;
    }

    public int getSSN() {
        return SSN;
    }

    public void setSSN(int SSN) {
        this.SSN = SSN;
    }
}
