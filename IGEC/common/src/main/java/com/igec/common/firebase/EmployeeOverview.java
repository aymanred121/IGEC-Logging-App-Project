package com.igec.common.firebase;


import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.StringJoiner;

public class EmployeeOverview implements Cloneable, Parcelable {
    public boolean isSelected = false, isManager = false;
    private String firstName, lastName, title, id, managerID;
    private ArrayList<String> projectIds = new ArrayList<>();

    public EmployeeOverview() {
    }

    protected EmployeeOverview(Parcel in) {
        isSelected = in.readByte() != 0;
        isManager = in.readByte() != 0;
        firstName = in.readString();
        lastName = in.readString();
        title = in.readString();
        id = in.readString();
        managerID = in.readString();
        projectIds = in.createStringArrayList();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByte((byte) (isSelected ? 1 : 0));
        dest.writeByte((byte) (isManager ? 1 : 0));
        dest.writeString(firstName);
        dest.writeString(lastName);
        dest.writeString(title);
        dest.writeString(id);
        dest.writeString(managerID);
        dest.writeStringList(projectIds);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<EmployeeOverview> CREATOR = new Creator<EmployeeOverview>() {
        @Override
        public EmployeeOverview createFromParcel(Parcel in) {
            return new EmployeeOverview(in);
        }

        @Override
        public EmployeeOverview[] newArray(int size) {
            return new EmployeeOverview[size];
        }
    };

    @NonNull
    @Override
    public EmployeeOverview clone() throws CloneNotSupportedException {
        return (EmployeeOverview) super.clone();
    }

    public EmployeeOverview(String firstName, String lastName, String title, String id) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.title = title;
        this.id = id;
    }

    public EmployeeOverview(String firstName, String lastName, String title, String id, ArrayList<String> projectIds) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.title = title;
        this.id = id;
        this.projectIds.clear();
        this.projectIds.addAll(projectIds);
    }

    public EmployeeOverview(String firstName, String lastName, String title, String id, ArrayList<String> projectIds, boolean isSelected) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.title = title;
        this.id = id;
        this.projectIds.clear();
        this.projectIds.addAll(projectIds);
        this.isSelected = isSelected;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getManagerID() {
        return managerID;
    }

    public void setManagerID(String managerID) {
        this.managerID = managerID;
    }


    public ArrayList<String> getProjectIds() {
        return projectIds;
    }

    public void setProjectIds(ArrayList<String> projectIds) {
        this.projectIds.clear();
        this.projectIds.addAll(projectIds);
    }


//    public void setProjectId(String projectId) {
//        this.projectIds.add(projectId);
//    }
//
//    public String getProjectId() {
//        StringJoiner joiner = new StringJoiner(",");
//        for (String s : projectIds) {
//            joiner.add(s);
//        }
//        return joiner.toString();
//    }

}
