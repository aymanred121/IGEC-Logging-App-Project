package com.igec.admin.fireBase;

import java.io.Serializable;

public class Client implements Serializable {
   private String name , Email , phoneNumber , note ;
   private double defaultPrice  , overTimePrice , FridaysPrice;

   public Client()
   {

   }
    public Client(String name, String email, String phoneNumber, String note, double defaultPrice, double overTimePrice, double fridaysPrice) {
        this.name = name;
        Email = email;
        this.phoneNumber = phoneNumber;
        this.note = note;
        this.defaultPrice = defaultPrice;
        this.overTimePrice = overTimePrice;
        FridaysPrice = fridaysPrice;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return Email;
    }

    public void setEmail(String email) {
        Email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public double getDefaultPrice() {
        return defaultPrice;
    }

    public void setDefaultPrice(double defaultPrice) {
        this.defaultPrice = defaultPrice;
    }

    public double getOverTimePrice() {
        return overTimePrice;
    }

    public void setOverTimePrice(double overTimePrice) {
        this.overTimePrice = overTimePrice;
    }

    public double getFridaysPrice() {
        return FridaysPrice;
    }

    public void setFridaysPrice(double fridaysPrice) {
        FridaysPrice = fridaysPrice;
    }
}
