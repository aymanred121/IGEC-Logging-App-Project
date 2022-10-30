package com.igec.common.firebase;

import android.annotation.SuppressLint;
import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.Timestamp;
import com.google.firebase.database.Exclude;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

public class Holiday implements Parcelable {

    // attributes
    private String name;
    private Date start, end;

    public Holiday() {

    }

    public Holiday(String name, Date start, Date end) {
        this.name = name;
        this.start = start;
        this.end = end;
    }

    public Holiday(HashMap o) {
        this.name = (String) o.get("name");
        this.start = ((Timestamp) o.get("start")).toDate();
        this.end = ((Timestamp) o.get("end")).toDate();
    }

    protected Holiday(Parcel in) {
        name = in.readString();
        start = new Date(in.readLong());
        end = new Date(in.readLong());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeLong(start.getTime());
        dest.writeLong(end.getTime());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Holiday> CREATOR = new Creator<Holiday>() {
        @Override
        public Holiday createFromParcel(Parcel in) {
            return new Holiday(in);
        }

        @Override
        public Holiday[] newArray(int size) {
            return new Holiday[size];
        }
    };

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getStart() {
        return start;
    }

    public void setStart(Date start) {
        this.start = start;
    }

    public Date getEnd() {
        return end;
    }

    public void setEnd(Date end) {
        this.end = end;
    }

    @Exclude
    public String formattedStartDate() {
        @SuppressLint("SimpleDateFormat") SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy");
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(start.getTime());
        return simpleDateFormat.format(calendar.getTime());
    }

    @Exclude
    public String formattedEndDate() {
        @SuppressLint("SimpleDateFormat") SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy");
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(end.getTime());
        return simpleDateFormat.format(calendar.getTime());
    }
}
