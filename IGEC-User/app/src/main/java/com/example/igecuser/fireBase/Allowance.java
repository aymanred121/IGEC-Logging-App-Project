package com.example.igecuser.fireBase;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

public class Allowance implements Serializable, Parcelable {
    private String name ;
    private double amount ;
    private int type; // if 0 projectAllowances , if 1 penalties , if 2 bonuses , if 3 personal allowances , if 4 net salary

    public Allowance() {
    }


    public Allowance(int type) {
        this.type = type;
    }

    public Allowance(double amount , int type) {
        this.amount = amount;
        this.type = type;
    }
    public Allowance(String name,double amount , int type) {
        this.name = name;
        this.amount = amount;
        this.type = type;
    }
    public Allowance(String name,double amount) {
        this.name = name;
        this.amount = amount;
    }

    protected Allowance(Parcel in) {
        name = in.readString();
        amount = in.readDouble();
        type = in.readInt();

    }

    public static final Creator<Allowance> CREATOR = new Creator<Allowance>() {
        @Override
        public Allowance createFromParcel(Parcel in) {
            return new Allowance(in);
        }

        @Override
        public Allowance[] newArray(int size) {
            return new Allowance[size];
        }
    };

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeDouble(amount);
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}
