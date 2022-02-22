package com.example.igec_admin.fireBase;

import java.util.Date;

public class VacationRequest {
    private Date startDate,duration,requestDate;
    private boolean isAccepted;
    private String requestID,employeeID,managerID;

    public VacationRequest() {
    }

    public VacationRequest(Date startDate, Date duration, Date requestDate, String requestID, String employeeID, String managerID) {
        this.startDate = startDate;
        this.duration = duration;
        this.requestDate = requestDate;
        this.requestID = requestID;
        this.employeeID = employeeID;
        this.managerID = managerID;
    }

    public Date getStartDate() {
        return startDate;
    }

    public Date getDuration() {
        return duration;
    }

    public boolean isAccepted() {
        return isAccepted;
    }

    public String getRequestID() {
        return requestID;
    }

    public String getEmployeeID() {
        return employeeID;
    }

    public String getManagerID() {
        return managerID;
    }

    public Date getRequestDate() {
        return requestDate;
    }

    public void setAccepted(boolean accepted) {
        isAccepted = accepted;
    }
}
