package com.example.igec_admin.fireBase;

import java.io.Serializable;

public class Client implements Serializable {
   private String name , Email , phoneNumber , note ;

    public Client(String name, String Email, String phoneNumber, String note) {
        this.name = name;
        this.Email = Email;
        this.phoneNumber = phoneNumber;
        this.note = note;
    }
    public Client()
    {

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
}
