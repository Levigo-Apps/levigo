package org.getcarebase.carebase.models;

import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.PropertyName;

/**
 * represents usage of a device
 */
public class Procedure {
    private String deviceIdentifier;
    private String uniqueDeviceIdentifier;
    private String accessionNumber;
    private String amountUsed;
    private String fluoroTime;
    private String date;
    private String type;
    private String roomTime;
    private String timeIn;
    private String timeOut;

    @Exclude
    public String getDeviceIdentifier() {
        return deviceIdentifier;
    }

    @Exclude
    public void setDeviceIdentifier(String deviceIdentifier) {
        this.deviceIdentifier = deviceIdentifier;
    }

    @Exclude
    public String getUniqueDeviceIdentifier() {
        return uniqueDeviceIdentifier;
    }

    @Exclude
    public void setUniqueDeviceIdentifier(String uniqueDeviceIdentifier) {
        this.uniqueDeviceIdentifier = uniqueDeviceIdentifier;
    }

    @PropertyName("accession_number")
    public String getAccessionNumber() {
        return accessionNumber;
    }

    @PropertyName("accession_number")
    public void setAccessionNumber(String accessionNumber) {
        this.accessionNumber = accessionNumber;
    }

    @PropertyName("amount_used")
    public String getAmountUsed() {
        return amountUsed;
    }

    @PropertyName("amount_used")
    public void setAmountUsed(String amountUsed) {
        this.amountUsed = amountUsed;
    }

    @PropertyName("fluoro_time")
    public String getFluoroTime() {
        return fluoroTime;
    }

    @PropertyName("fluoro_time")
    public void setFluoroTime(String fluoroTime) {
        this.fluoroTime = fluoroTime;
    }

    @PropertyName("procedure_date")
    public String getDate() {
        return date;
    }

    @PropertyName("procedure_date")
    public void setDate(String date) {
        this.date = date;
    }

    @PropertyName("procedure_used")
    public String getType() {
        return type;
    }

    @PropertyName("procedure_used")
    public void setType(String type) {
        this.type = type;
    }

    @PropertyName("room_time")
    public String getRoomTime() {
        return roomTime;
    }

    @PropertyName("room_time")
    public void setRoomTime(String roomTime) {
        this.roomTime = roomTime;
    }

    @PropertyName("time_in")
    public String getTimeIn() {
        return timeIn;
    }

    @PropertyName("time_in")
    public void setTimeIn(String timeIn) {
        this.timeIn = timeIn;
    }

    @PropertyName("time_out")
    public String getTimeOut() {
        return timeOut;
    }

    @PropertyName("time_out")
    public void setTimeOut(String timeOut) {
        this.timeOut = timeOut;
    }
}
