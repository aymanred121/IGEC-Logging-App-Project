package com.example.igec_admin.fireBase;

import com.google.firebase.database.Exclude;

public class EmployeeOverview {
  @Exclude  private Boolean isSelected=false;
    private String firstName,lastName,title,id;

    public EmployeeOverview(String firstName, String lastName, String title, String id) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.title = title;
        this.id = id;
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

    @Exclude public Boolean getSelected() {
        return isSelected;
    }

    public void setSelected(Boolean selected) {
        isSelected = selected;
    }

    public void setId(String id) {
        this.id = id;
    }
}
