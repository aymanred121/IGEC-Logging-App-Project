package com.example.igec_admin.fireBase;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

public class Allowance implements Serializable, Parcelable {
    private String name ;
    private int amount ;

    public Allowance() {
    }

    public Allowance(int amount) {
        this.amount = amount;
    }

    protected Allowance(Parcel in) {
        name = in.readString();
        amount = in.readInt();
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

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public Allowance(String name, int amount) {
        this.name = name;
        this.amount = amount;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeInt(amount);
    }
}
