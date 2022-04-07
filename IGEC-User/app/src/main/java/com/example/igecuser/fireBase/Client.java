package com.example.igecuser.fireBase;

import java.io.Serializable;

public class Client implements Serializable  {
   private String name , Email , phoneNumber;

    public Client(String name, String Email, String phoneNumber) {
        this.name = name;
        this.Email = Email;
        this.phoneNumber = phoneNumber;
    }
    public Client()
    {

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
