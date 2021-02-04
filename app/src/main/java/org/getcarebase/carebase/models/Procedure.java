package org.getcarebase.carebase.models;

import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.PropertyName;

import java.util.List;

/**
 * represents a medical operation
 */
public class Procedure {
    private String procedureId;
    private String accessionNumber;
    private String fluoroTime;
    private String date;
    private String name;
    private String roomTime;
    private String timeIn;
    private String timeOut;
    private List<DeviceUsage> deviceUsages;

    public Procedure() {}

    @DocumentId
    public String getProcedureId() {
        return procedureId;
    }

    @DocumentId
    public void setProcedureId(String procedureId) {
        this.procedureId = procedureId;
    }

    @PropertyName("accession_number")
    public String getAccessionNumber() {
        return accessionNumber;
    }

    @PropertyName("accession_number")
    public void setAccessionNumber(String accessionNumber) {
        this.accessionNumber = accessionNumber;
    }

    @PropertyName("fluoro_time")
    public String getFluoroTime() {
        return fluoroTime;
    }

    @PropertyName("fluoro_time")
    public void setFluoroTime(String fluoroTime) { this.fluoroTime = fluoroTime + " minutes"; }

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
    public void setRoomTime(String roomTime) { this.roomTime = roomTime; }

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

    @PropertyName("device_usages")
    public List<DeviceUsage> getDeviceUsages() {
        return deviceUsages;
    }

    @PropertyName("device_usages")
    public void setDeviceUsages(List<DeviceUsage> deviceUsages) {
        this.deviceUsages = deviceUsages;
    }
}
