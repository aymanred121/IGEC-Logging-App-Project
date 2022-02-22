package com.example.igec_admin.fireBase;

public class Accounts {
    private String email,password;
    private int UID;

    public Accounts(){

    }
    public Accounts(String email, String password, int UID) {
        this.email = email;
        this.password = password;
        this.UID = UID;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getUID() {
        return UID;
    }

    public void setUID(int UID) {
        this.UID = UID;
    }
}
