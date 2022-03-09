package com.example.igec_admin.fireBase;

public class Machine {
    private String id,codeName, purchaseDate;

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

    public String getPurchaseDate() {
        return purchaseDate;
    }

    public void setPurchaseDate(String purchaseDate) {
        this.purchaseDate = purchaseDate;
    }

    public Machine(String id, String codeName, String buyingDate) {
        this.id = id;
        this.codeName = codeName;
        this.purchaseDate = buyingDate;
    }
}
