package com.axiom.fulfillment.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ButtonStatusResult {

    @SerializedName("Status")
    @Expose
    private Status status;
    @SerializedName("CanCancel")
    @Expose
    private Boolean canCancel;
    @SerializedName("ShowInvoice")
    @Expose
    private Boolean showInvoice;

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Boolean getCanCancel() {
        return canCancel;
    }

    public void setCanCancel(Boolean canCancel) {
        this.canCancel = canCancel;
    }

    public Boolean getShowInvoice() {
        return showInvoice;
    }

    public void setShowInvoice(Boolean showInvoice) {
        this.showInvoice = showInvoice;
    }
}