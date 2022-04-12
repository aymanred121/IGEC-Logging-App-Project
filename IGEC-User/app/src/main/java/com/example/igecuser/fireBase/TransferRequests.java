package com.example.igecuser.fireBase;

public class TransferRequests {
    private EmployeeOverview employee;
    private String note, oldProjectName, oldProjectId, oldProjectReference, newProjectName, newProjectId, newProjectReference, transferId;
    private int transferStatus = -1;

    /**
     * -1 means pending
     * 0 means rejected
     * 1 means accepted
     */

    public TransferRequests() {
    }

    public TransferRequests(EmployeeOverview employee, String note, String oldProjectName, String oldProjectId, String oldProjectReference, String newProjectName, String newProjectId, String newProjectReference) {
        this.employee = employee;
        this.note = note;
        this.oldProjectName = oldProjectName;
        this.oldProjectId = oldProjectId;
        this.oldProjectReference = oldProjectReference;
        this.newProjectName = newProjectName;
        this.newProjectId = newProjectId;
        this.newProjectReference = newProjectReference;
    }

    public EmployeeOverview getEmployee() {
        return employee;
    }

    public void setEmployee(EmployeeOverview employee) {
        this.employee = employee;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getOldProjectName() {
        return oldProjectName;
    }

    public void setOldProjectName(String oldProjectName) {
        this.oldProjectName = oldProjectName;
    }

    public String getOldProjectId() {
        return oldProjectId;
    }

    public void setOldProjectId(String oldProjectId) {
        this.oldProjectId = oldProjectId;
    }

    public String getOldProjectReference() {
        return oldProjectReference;
    }

    public void setOldProjectReference(String oldProjectReference) {
        this.oldProjectReference = oldProjectReference;
    }

    public String getNewProjectName() {
        return newProjectName;
    }

    public void setNewProjectName(String newProjectName) {
        this.newProjectName = newProjectName;
    }

    public String getNewProjectId() {
        return newProjectId;
    }

    public void setNewProjectId(String newProjectId) {
        this.newProjectId = newProjectId;
    }

    public String getNewProjectReference() {
        return newProjectReference;
    }

    public void setNewProjectReference(String newProjectReference) {
        this.newProjectReference = newProjectReference;
    }

    public int getTransferStatus() {
        return transferStatus;
    }

    public void setTransferStatus(int transferStatus) {
        this.transferStatus = transferStatus;
    }

    public String getTransferId() {
        return transferId;
    }

    public void setTransferId(String transferId) {
        this.transferId = transferId;
    }
}
