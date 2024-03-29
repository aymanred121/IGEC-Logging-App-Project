package com.igec.common.firebase;

import static com.igec.common.CONSTANTS.PENDING;
import static com.igec.common.CONSTANTS.isThereAFriday;

import android.annotation.SuppressLint;

import com.google.firebase.database.Exclude;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class VacationRequest implements Serializable {
    private Date startDate, endDate, requestDate;
    private Employee manager, employee;
    private String vacationNote, id;
    private int vacationStatus;
    private int vacationDays, unpaidDays, sickDays;
    private String feedback = "";
    /*
    vacationNotification:
    -1  = no one read it
     0  = manager/admin had read it
     1  = employee had read it
     */
    private int vacationNotification = -1;

    public VacationRequest() {
    }

    public VacationRequest(Date startDate, Date endDate, Date requestDate, Employee manager, Employee employee, String vacationNote) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.requestDate = requestDate;
        this.manager = manager;
        this.employee = employee;
        this.vacationNote = vacationNote;
        vacationStatus = PENDING;
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

    @Exclude
    public String getRequestedDaysString() {
        long days = endDate.getTime() - startDate.getTime();
        days /= (24 * 3600 * 1000);
        if(!isThereAFriday(startDate.getTime(), endDate.getTime())) days++;
        return String.valueOf(days);
    }

    @Exclude
    public long getRequestedDays() {
        long days = endDate.getTime() - startDate.getTime();
        days /= (24 * 3600 * 1000);
        if(!isThereAFriday(startDate.getTime(), endDate.getTime())) days++;
        return days;
    }

    @Exclude
    public long getRemainingDays() {
        return employee.getTotalNumberOfVacationDays();
    }

    @Exclude
    public String formattedStartDate() {
        @SuppressLint("SimpleDateFormat") SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy");
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(startDate.getTime());
        return simpleDateFormat.format(calendar.getTime());
    }

    @Exclude
    public String formattedEndDate() {
        @SuppressLint("SimpleDateFormat") SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy");
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(endDate.getTime());
        return simpleDateFormat.format(calendar.getTime());
    }

    public int getVacationNotification() {
        return vacationNotification;
    }

    public void setVacationNotification(int vacationNotification) {
        this.vacationNotification = vacationNotification;
    }

    public int getVacationDays() {
        return vacationDays;
    }

    public void setVacationDays(int vacationDays) {
        this.vacationDays = vacationDays;
    }

    public int getUnpaidDays() {
        return unpaidDays;
    }

    public void setUnpaidDays(int unpaidDays) {
        this.unpaidDays = unpaidDays;
    }

    public int getSickDays() {
        return sickDays;
    }

    public void setSickDays(int sickDays) {
        this.sickDays = sickDays;
    }

    public String getFeedback() {
        return feedback;
    }

    public void setFeedback(String feedback) {
        this.feedback = feedback;
    }
}
