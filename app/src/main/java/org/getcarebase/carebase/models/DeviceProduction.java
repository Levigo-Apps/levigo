package org.getcarebase.carebase.models;

import java.util.Map;

/**
 * immutable class representing the udi level information of a device
 */
public class DeviceProduction {
    private final String uniqueDeviceIdentifier;
    private final String dateAdded;
    private final String timeAdded;
    private final String expirationDate;
    private final String lotNumber;
    private final String notes;
    private final String physicalLocation;
    private final int quantity;
    private final String referenceNumber;

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

    public String getUniqueDeviceIdentifier() {
        return uniqueDeviceIdentifier;
    }

    public String getDateAdded() {
        return dateAdded;
    }

    public String getTimeAdded() {
        return timeAdded;
    }

    public String getExpirationDate() {
        return expirationDate;
    }

    public String getLotNumber() {
        return lotNumber;
    }

    public String getNotes() {
        return notes;
    }

    public String getPhysicalLocation() {
        return physicalLocation;
    }

    public int getQuantity() {
        return quantity;
    }

    public String getReferenceNumber() {
        return referenceNumber;
    }
}
