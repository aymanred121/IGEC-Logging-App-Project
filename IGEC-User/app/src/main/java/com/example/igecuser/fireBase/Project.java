package com.example.igecuser.fireBase;


import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class Project {
    private String managerID,managerName,name,id , reference ,  locationCity ,locationArea ,locationStreet ;
    private ArrayList<Allowance> allowancesList = new ArrayList<>();
    private Client client ;
    private Date startDate;
    private ArrayList<EmployeeOverview>  employees;
    private HashMap<String, Object> employeeWorkedTime = new HashMap<>();
    private HashMap<String, Object> machineWorkedTime = new HashMap<>();

    public HashMap<String, Object> getEmployeeWorkedTime() {
        return employeeWorkedTime;
    }

    public void setEmployeeWorkedTime(HashMap<String, Object> employeeWorkedTime) {
        this.employeeWorkedTime = employeeWorkedTime;
    }

    public Project() {
    }

    public Project(String managerName,String managerID, String name, Date startDate, ArrayList<EmployeeOverview> employees , String reference , String locationCity , String locationArea , String locationStreet) {
        this.managerID = managerID;
        this.name = name;
        this.startDate = startDate;
        this.employees = employees;
        this.managerName=managerName;
        this.reference = reference;
        this.locationArea = locationArea;
        this.locationCity = locationCity;
        this.locationStreet = locationStreet;
    }

    public String getLocationCity() {
        return locationCity;
    }

    public void setLocationCity(String locationCity) {
        this.locationCity = locationCity;
    }

    public String getLocationArea() {
        return locationArea;
    }

    public void setLocationArea(String locationArea) {
        this.locationArea = locationArea;
    }

    public String getLocationStreet() {
        return locationStreet;
    }

    public void setLocationStreet(String locationStreet) {
        this.locationStreet = locationStreet;
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


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    public ArrayList<Allowance> getAllowancesList() {
        return allowancesList;
    }

    public void setAllowancesList(ArrayList<Allowance> allowancesList) {
        this.allowancesList = allowancesList;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public ArrayList<EmployeeOverview> getEmployees() {
        return employees;
    }

    public void setEmployees(ArrayList<EmployeeOverview> employees) {
        this.employees = employees;
    }

    public HashMap<String, Object> getMachineWorkedTime() {
        return machineWorkedTime;
    }

    public void setMachineWorkedTime(HashMap<String, Object> machineWorkedTime) {
        this.machineWorkedTime = machineWorkedTime;
    }
}
