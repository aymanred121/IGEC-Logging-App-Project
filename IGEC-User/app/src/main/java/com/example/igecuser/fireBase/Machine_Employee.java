package com.example.igecuser.fireBase;

import com.firebase.geofire.GeoFireUtils;
import com.firebase.geofire.GeoLocation;
import com.google.firebase.firestore.Exclude;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Machine_Employee extends Summary {
    private Machine machine;
    public Machine_Employee() {
    }



    public Machine_Employee(double lat, double lng, Machine machine) {
        super(lat, lng);
        this.machine = machine;
    }

    public Machine getMachine() {
        return machine;
    }

    public void setMachine(Machine machine) {
        this.machine = machine;
    }
}
