package com.example.igecuser.fireBase;

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
    private Object workedTime;
    private Employee employee;
    private Timestamp lastCheckInTime;
    private String lastDayPath;

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

    public Object getWorkedTime() {
        return workedTime;
    }

    public void setWorkedTime(Object workedTime) {
        this.workedTime = workedTime;
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
}
