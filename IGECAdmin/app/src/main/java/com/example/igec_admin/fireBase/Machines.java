package com.example.igec_admin.fireBase;

public class Machines {
    private String id,codeName,buyingDate;

    public Machines() {
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

    public String getBuyingDate() {
        return buyingDate;
    }

    public void setBuyingDate(String buyingDate) {
        this.buyingDate = buyingDate;
    }

    public Machines(String id, String codeName, String buyingDate) {
        this.id = id;
        this.codeName = codeName;
        this.buyingDate = buyingDate;
    }
}
