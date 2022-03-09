package com.example.igec_admin.fireBase;

import java.util.Date;

public class Machine {
    private String id, codeName;
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

    public Machine(String id, String codeName, Date buyingDate) {
        this.id = id;
        this.codeName = codeName;
        this.purchaseDate = buyingDate;
    }

    public Date getPurchaseDate() {
        return purchaseDate;
    }

    public void setPurchaseDate(Date purchaseDate) {
        this.purchaseDate = purchaseDate;
    }
}
