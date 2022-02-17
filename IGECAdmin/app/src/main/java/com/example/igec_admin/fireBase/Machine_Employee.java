package com.example.igec_admin.fireBase;

import com.firebase.geofire.GeoFireUtils;
import com.firebase.geofire.GeoLocation;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Machine_Employee {
    private String machineID,employeeID;
    private double lat,lng;
    private Map<Date,Map<String,Object>>checkIn,CheckOut=new HashMap();

    public Machine_Employee() {
    }
    private Map geoPoint(){
        /**
         * TODO move this function to where gps function will be
         * */
        Map<String, Object> updates = new HashMap<>();
        String hash = GeoFireUtils.getGeoHashForLocation(new GeoLocation(lat, lng));
        updates.put("geohash", hash);
        updates.put("lat", lat);
        updates.put("lng", lng);
        return updates;
    }
    public Machine_Employee(String machineID, String employeeID) {
        this.machineID = machineID;
        this.employeeID = employeeID;
    }

    public String getMachineID() {
        return machineID;
    }

    public void setMachineID(String machineID) {
        this.machineID = machineID;
    }

    public String getEmployeeID() {
        return employeeID;
    }

    public void setEmployeeID(String employeeID) {
        this.employeeID = employeeID;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }


    public void setLng(double lng) {
        this.lng = lng;
    }

    public Map<Date, Map<String, Object>> getCheckIn() {
        return checkIn;
    }

    public Map<Date, Map<String, Object>> getCheckOut() {
        return CheckOut;
    }

    public void setCheckOut(Map<Date, Map<String, Object>> checkOut) {
        CheckOut = checkOut;
    }

    public void setCheckIn(Map<Date, Map<String, Object>> checkIn) {
        this.checkIn = checkIn;
    }
}
