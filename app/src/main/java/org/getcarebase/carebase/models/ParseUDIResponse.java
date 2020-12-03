package org.getcarebase.carebase.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ParseUDIResponse {
    @SerializedName("udi")
    @Expose
    private String udi;
    @SerializedName("issuingAgency")
    @Expose
    private String issuingAgency;
    @SerializedName("di")
    @Expose
    private String di;

    public String getUdi() {
        return udi;
    }

    public void setUdi(String udi) {
        this.udi = udi;
    }

    public String getIssuingAgency() {
        return issuingAgency;
    }

    public void setIssuingAgency(String issuingAgency) {
        this.issuingAgency = issuingAgency;
    }

    public String getDi() {
        return di;
    }

    public void setDi(String di) {
        this.di = di;
    }
}
