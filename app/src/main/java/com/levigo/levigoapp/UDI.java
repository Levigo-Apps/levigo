package com.levigo.levigoapp;


import org.json.JSONObject;

public class UDI {
    private static final String TAG = "udi";
    public static final String URL = "https://accessgudid.nlm.nih.gov/api/v2/parse_udi.json?udi=";

//    private String udi;
//    private String issuingAgency;
//    private String di;
//    private String serialNumber;
//    private String donationId;
//    private String expirationDateOriginalFormat;
//    private String expirationDateOriginal;
//    private String expirationDate;
//    private String manufacturingDateOriginalFormat;
//    private String manufacturingDateOriginal;
//    private String manufacturingDate;
//    private String lotNumber;

    private String json;



    public UDI(String json) {
        this.json = json;
    }

}
