package com.igec.common.firebase;

import com.firebase.geofire.GeoFireUtils;
import com.firebase.geofire.GeoLocation;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.Exclude;

import java.util.HashMap;

public class Summary {
    @Exclude
    private double lat, lng;
    @Exclude
    private String geoHash;
    @Exclude
    private HashMap<String, Object> geoMap = new HashMap<>();
    private HashMap<String, Object> checkIn;
    private HashMap<String, Object> checkOut;
    private Employee employee;
    private HashMap<String,Object> WorkingTime;
    private Timestamp lastCheckInTime;
    private String lastDayPath;
    private String lastProjectId;
    private HashMap<String, String> projectIds = new HashMap<>();

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    public HashMap<String, Object> getCheckIn() {
        return checkIn;
    }

    public void setCheckIn(HashMap<String, Object> checkIn) {
        this.checkIn = checkIn;
    }

    public HashMap<String, Object> getCheckOut() {
        return checkOut;
    }

    public void setCheckOut(HashMap<String, Object> checkOut) {
        this.checkOut = checkOut;
    }


    public Summary() {
    }

    public Summary(double lat, double lng) {
        this.lat = lat;
        this.lng = lng;
        geoHash = GeoFireUtils.getGeoHashForLocation(new GeoLocation(lat, lng));
        geoMap.put("geohash", geoHash);
        geoMap.put("lat", lat);
        geoMap.put("lng", lng);
    }

    public HashMap<String, Object> getWorkingTime() {
        return WorkingTime;
    }

    public void setWorkingTime(HashMap<String, Object> workingTime) {
        WorkingTime = workingTime;
    }

    @Exclude
    public double getLat() {
        return lat;
    }

    @Exclude
    public void setLat(double lat) {
        this.lat = lat;
    }

    @Exclude
    public double getLng() {
        return lng;
    }

    @Exclude
    public void setLng(double lng) {
        this.lng = lng;
    }

    @Exclude
    public String getGeoHash() {
        return geoHash;
    }

    @Exclude
    public void setGeoHash(String geoHash) {
        this.geoHash = geoHash;
    }

    @Exclude
    public HashMap<String, Object> getGeoMap() {
        return geoMap;
    }

    @Exclude
    public void setGeoMap(HashMap<String, Object> geoMap) {
        this.geoMap = geoMap;
    }


    public Timestamp getLastCheckInTime() {
        return lastCheckInTime;
    }

    public void setLastCheckInTime(Timestamp lastCheckInTime) {
        this.lastCheckInTime = lastCheckInTime;
    }

    public String getLastDayPath() {
        return lastDayPath;
    }

    public void setLastDayPath(String lastDayPath) {
        this.lastDayPath = lastDayPath;
    }

    public HashMap<String, String> getProjectIds() {
        return projectIds;
    }

    public void setProjectIds(HashMap<String, String> projectIds) {
        this.projectIds = projectIds;
    }

    public String getLastProjectId() {
        return lastProjectId;
    }

    public void setLastProjectId(String lastProjectId) {
        this.lastProjectId = lastProjectId;
    }
}
