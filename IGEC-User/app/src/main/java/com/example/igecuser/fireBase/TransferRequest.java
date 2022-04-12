package com.example.igecuser.fireBase;

public class TransferRequest {
    private EmployeeOverview employee;
    private String wantedProject , note;
    private int transferStatus;

    public TransferRequest(EmployeeOverview employee, String wantedProject, String note, int transferStatus) {
        this.employee = employee;
        this.wantedProject = wantedProject;
        this.note = note;
        this.transferStatus = transferStatus;
    }

    public String getWantedProject() {
        return wantedProject;
    }

    public void setWantedProject(String wantedProject) {
        this.wantedProject = wantedProject;
    }

    public EmployeeOverview getEmployee() {
        return employee;
    }

    public void setEmployee(EmployeeOverview employee) {
        this.employee = employee;
    }

    public int getTransferStatus() {
        return transferStatus;
    }

    public void setTransferStatus(int transferStatus) {
        this.transferStatus = transferStatus;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
