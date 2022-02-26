package com.example.igec_admin.fireBase;

import com.google.firebase.database.Exclude;

import java.util.ArrayList;
import java.util.Date;
import java.util.Map;

public class Project {
    private String managerID,managerName,name,id;
    private Date startDate, estimatedEndDate;
  private ArrayList<EmployeeOverview>  employees;
    private String location;
    public Project() {
    }

    public Project(String managerName,String managerID, String name, Date startDate, Date estimatedEndDate, ArrayList<EmployeeOverview> employees,String location) {
        this.managerID = managerID;
        this.name = name;
        this.startDate = startDate;
        this.estimatedEndDate = estimatedEndDate;
        this.employees = employees;
        this.managerName=managerName;
        this.location=location;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getManagerName() {
        return managerName;
    }

    public void setManagerName(String managerName) {
        this.managerName = managerName;
    }

    public String getManagerID() {
        return managerID;
    }

    public void setManagerID(String managerID) {
        this.managerID = managerID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEstimatedEndDate() {
        return estimatedEndDate;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setEstimatedEndDate(Date estimatedEndDate) {
        this.estimatedEndDate = estimatedEndDate;
    }

  public ArrayList<EmployeeOverview>  getEmployees() {
        return employees;
    }

    public void setEmployees(ArrayList<EmployeeOverview>  employees) {
        this.employees = employees;
    }
}
