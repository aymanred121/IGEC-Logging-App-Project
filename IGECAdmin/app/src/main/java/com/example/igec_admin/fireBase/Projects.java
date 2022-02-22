package com.example.igec_admin.fireBase;

import java.util.Date;

public class Projects {
    private String managerID;
    private String name;
    private Date startDate, estimatedEndDate;

    public Projects() {
    }

    public Projects( String managerID, String name, Date startDate, Date estimatedEndDate) {
        this.managerID = managerID;
        this.name = name;
        this.startDate = startDate;
        this.estimatedEndDate = estimatedEndDate;
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

    public void setEstimatedEndDate(Date estimatedEndDate) {
        this.estimatedEndDate = estimatedEndDate;
    }
}
