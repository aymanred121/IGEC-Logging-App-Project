package com.example.igecuser.fireBase;

import java.util.Date;

public class Machine {
    private String id,codeName;
    private Date purchaseDate;

    public Machine() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCodeName() {
        return codeName;
    }

    public void setCodeName(String codeName) {
        this.codeName = codeName;
    }

    public Date getPurchaseDate() {
        return purchaseDate;
    }

    public void setPurchaseDate(Date purchaseDate) {
        this.purchaseDate = purchaseDate;
    }

    public Machine(String id, String codeName, Date purchaseDate) {
        this.id = id;
        this.codeName = codeName;
        this.purchaseDate = purchaseDate;
    }


}
