package com.igec.common.firebase;


import androidx.annotation.NonNull;

public class EmployeeOverview implements Cloneable{
    public boolean isSelected = false;
    private String firstName, lastName, title, id, managerID, projectId;

    public EmployeeOverview() {
    }

    @NonNull
    @Override
    public EmployeeOverview clone() throws CloneNotSupportedException {
        return (EmployeeOverview) super.clone();
    }

    public EmployeeOverview(String firstName, String lastName, String title, String id) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.title = title;
        this.id = id;
    }

    public EmployeeOverview(String firstName, String lastName, String title, String id, String projectId) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.title = title;
        this.id = id;
        this.projectId = projectId;
    }
    public EmployeeOverview(EmployeeOverview e) {
        this.firstName = e.getFirstName();
        this.lastName = e.getLastName();
        this.title = e.getTitle();
        this.id = e.getId();
        this.projectId = e.getProjectId();
    }

    public EmployeeOverview(String firstName, String lastName, String title, String id, String projectId, boolean isSelected) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.title = title;
        this.id = id;
        this.projectId = projectId;
        this.isSelected = isSelected;
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

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getManagerID() {
        return managerID;
    }

    public void setManagerID(String managerID) {
        this.managerID = managerID;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }
}