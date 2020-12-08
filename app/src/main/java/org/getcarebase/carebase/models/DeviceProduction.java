package org.getcarebase.carebase.models;

import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.PropertyName;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * class representing the udi level information of a device
 */
public class DeviceProduction {
    private String uniqueDeviceIdentifier;
    private String dateAdded;
    private String timeAdded;
    private String expirationDate;
    private String lotNumber;
    private String notes;
    private String physicalLocation;
    private int quantity;
    private String referenceNumber;
    private List<Cost> costs = new ArrayList<>();

    public DeviceProduction() {}

    public DeviceProduction(Map<String,Object> data) {
        this.uniqueDeviceIdentifier = (String) data.get("udi");
        this.dateAdded = (String) data.get("current_date");
        this.timeAdded = (String) data.get("current_time");
        this.expirationDate = (String) data.get("expiration");
        this.lotNumber = (String) data.get("lot_number");
        this.notes = (String) data.get("notes");
        this.physicalLocation = (String) data.get("physical_location");
        this.quantity = Integer.parseInt((String) data.get("quantity"));
        this.referenceNumber = (String) data.get("reference_number");
    }

    public void setUniqueDeviceIdentifier(String uniqueDeviceIdentifier) {
        this.uniqueDeviceIdentifier = uniqueDeviceIdentifier;
    }

    public void setDateAdded(String dateAdded) {
        this.dateAdded = dateAdded;
    }

    public void setTimeAdded(String timeAdded) {
        this.timeAdded = timeAdded;
    }

    public void setExpirationDate(String expirationDate) {
        this.expirationDate = expirationDate;
    }

    public void setLotNumber(String lotNumber) {
        this.lotNumber = lotNumber;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public void setPhysicalLocation(String physicalLocation) {
        this.physicalLocation = physicalLocation;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public void setReferenceNumber(String referenceNumber) {
        this.referenceNumber = referenceNumber;
    }

    public void addCost(Cost cost) {
        costs.add(cost);
    }

    @PropertyName("udi")
    public String getUniqueDeviceIdentifier() {
        return uniqueDeviceIdentifier;
    }
    @PropertyName("current_date")
    public String getDateAdded() {
        return dateAdded;
    }
    @PropertyName("current_time")
    public String getTimeAdded() {
        return timeAdded;
    }
    @PropertyName("expiration")
    public String getExpirationDate() {
        return expirationDate;
    }
    @PropertyName("lot_number")
    public String getLotNumber() {
        return lotNumber;
    }
    @PropertyName("notes")
    public String getNotes() {
        return notes;
    }
    @PropertyName("physical_location")
    public String getPhysicalLocation() {
        return physicalLocation;
    }
    @PropertyName("quantity")
    public String getStringQuantity() {
        return Integer.toString(quantity);
    }
    @Exclude
    public int getQuantity() {
        return quantity;
    }
    @PropertyName("reference_number")
    public String getReferenceNumber() {
        return referenceNumber;
    }
    @Exclude
    public List<Cost> getCosts() {
        return costs;
    }
}
