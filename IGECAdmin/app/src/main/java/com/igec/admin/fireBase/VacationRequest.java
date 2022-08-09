package com.igec.admin.fireBase;

import java.io.Serializable;
import java.util.Date;

public class VacationRequest implements Serializable {
    private Date startDate, endDate,requestDate;
    private Employee manager,employee;
    private String vacationNote,id;
    private int vacationStatus;
    public VacationRequest() {
    }

    public VacationRequest(Date startDate, Date endDate, Date requestDate, Employee manager, Employee employee, String vacationNote) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.requestDate = requestDate;
        this.manager = manager;
        this.employee = employee;
        this.vacationNote = vacationNote;
        vacationStatus=0;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public Date getRequestDate() {
        return requestDate;
    }

    public void setRequestDate(Date requestDate) {
        this.requestDate = requestDate;
    }

    public Employee getManager() {
        return manager;
    }

    public void setManager(Employee manager) {
        this.manager = manager;
    }

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    public String getVacationNote() {
        return vacationNote;
    }

    public void setVacationNote(String vacationNote) {
        this.vacationNote = vacationNote;
    }

    public int getVacationStatus() {
        return vacationStatus;
    }

    public void setVacationStatus(int vacationStatus) {
        this.vacationStatus = vacationStatus;
    }
}
