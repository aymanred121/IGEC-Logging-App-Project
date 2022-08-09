package com.igec.common.utilities;

public class LocationDetails {
    private String geoHash;
    private double lat,lng;

    public LocationDetails(String geoHash, double lat, double lng) {
        this.geoHash = geoHash;
        this.lat = lat;
        this.lng = lng;
    }

    public String getGeoHash() {
        return geoHash;
    }

    public void setGeoHash(String geoHash) {
        this.geoHash = geoHash;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }
}
