package com.igec.admin.fireBase;

import java.util.Date;

public class MachineDefectsLog {
    private String note,machineRef,machineId,employeeId,employeeName;
    private Date issueDate;

    public MachineDefectsLog() {
    }

    public MachineDefectsLog(String note, String machineRef, String machineId, String employeeId, String employeeName, Date issueDate) {
        this.note = note;
        this.machineRef = machineRef;
        this.machineId = machineId;
        this.employeeId = employeeId;
        this.employeeName = employeeName;
        this.issueDate = issueDate;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getMachineRef() {
        return machineRef;
    }

    public void setMachineRef(String machineRef) {
        this.machineRef = machineRef;
    }

    public String getMachineId() {
        return machineId;
    }

    public void setMachineId(String machineId) {
        this.machineId = machineId;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }

    public String getEmployeeName() {
        return employeeName;
    }

    public void setEmployeeName(String employeeName) {
        this.employeeName = employeeName;
    }

    public Date getIssueDate() {
        return issueDate;
    }

    public void setIssueDate(Date issueDate) {
        this.issueDate = issueDate;
    }
}
