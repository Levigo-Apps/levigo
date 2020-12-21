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
    private int amountUsed;
    // TODO change device production quantity to type int instead of string in firebase
    private String newQuantity;
    private String fluoroTime;
    private String date;
    private String name;
    private String roomTime;
    private String timeIn;
    private String timeOut;

    public Procedure() {}

    // helper constructor for procedure form
    public Procedure(DeviceUsage deviceUsage, Procedure procedureDetails) {
        this.deviceIdentifier = deviceUsage.getDeviceIdentifier();
        this.uniqueDeviceIdentifier = deviceUsage.getUniqueDeviceIdentifier();
        this.amountUsed = deviceUsage.getAmountUsed();
        this.newQuantity = Integer.toString(deviceUsage.getNewQuantity());
        this.accessionNumber = procedureDetails.getAccessionNumber();
        this.fluoroTime = procedureDetails.getFluoroTime();
        this.date = procedureDetails.getDate();
        this.name = procedureDetails.getName();
        this.roomTime = procedureDetails.getRoomTime();
        this.timeIn = procedureDetails.getTimeIn();
        this.timeOut = procedureDetails.getTimeOut();
    }

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
    public int getAmountUsed() {
        return amountUsed;
    }

    @PropertyName("amount_used")
    public void setAmountUsed(int amountUsed) {
        this.amountUsed = amountUsed;
    }

    @Exclude
    public String getNewQuantity() {
        return newQuantity;
    }

    @PropertyName("fluoro_time")
    public String getFluoroTime() {
        return fluoroTime;
    }

    @PropertyName("fluoro_time")
    public void setFluoroTime(String fluoroTime) {
        this.fluoroTime = fluoroTime;
    }

    @PropertyName("date")
    public String getDate() {
        return date;
    }

    @PropertyName("date")
    public void setDate(String date) {
        this.date = date;
    }

    @PropertyName("name")
    public String getName() {
        return name;
    }

    @PropertyName("name")
    public void setName(String name) {
        this.name = name;
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
