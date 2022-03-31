package com.example.igec_admin.fireBase;

import java.util.Date;

public class Machine {
    private String id, codeName;
    private Date purchaseDate;

    public Allowance getAllowance() {
        return allowance;
    }

    public void setAllowance(Allowance allowance) {
        this.allowance = allowance;
    }

    private Allowance allowance ;

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

    public Machine(String id, String codeName, Date buyingDate , Allowance allowance) {
        this.id = id;
        this.codeName = codeName;
        this.purchaseDate = buyingDate;
        this.allowance = allowance;
    }

    public Date getPurchaseDate() {
        return purchaseDate;
    }

    public void setPurchaseDate(Date purchaseDate) {
        this.purchaseDate = purchaseDate;
    }
}
