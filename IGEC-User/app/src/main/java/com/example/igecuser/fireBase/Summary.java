package com.example.igecuser.fireBase;

import com.firebase.geofire.GeoFireUtils;
import com.firebase.geofire.GeoLocation;
import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.FieldValue;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Summary {
    @Exclude private double lat,lng;
    @Exclude private String geoHash;
    private Map<String,Object> geoMap = new HashMap<>();
    private FieldValue time= FieldValue.serverTimestamp();


    public Summary() {
    }

    public Summary( double lat, double lng) {
        this.lat = lat;
        this.lng = lng;
        geoHash = GeoFireUtils.getGeoHashForLocation(new GeoLocation(lat, lng));
        geoMap.put("geohash", geoHash);
        geoMap.put("lat", lat);
        geoMap.put("lng", lng);
    }

    public FieldValue getTime() {
        return time;
    }

    public void setTime(FieldValue time) {
        this.time = time;
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
    public Map<String, Object> getGeoMap() {
        return geoMap;
    }
    public void setGeoMap(Map<String, Object> geoMap) {
        this.geoMap = geoMap;
    }
}
