package com.axiom.fulfillment.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class CourierOrder {
    @SerializedName("AxiomOrderNo")
    @Expose
    private String axiomOrderNo;

    public String getAxiomOrderNo() {
        return axiomOrderNo;
    }

    public void setAxiomOrderNo(String axiomOrderNo) {
        this.axiomOrderNo = axiomOrderNo;
    }

}
