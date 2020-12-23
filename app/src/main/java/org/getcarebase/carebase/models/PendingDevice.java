package org.getcarebase.carebase.models;

import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.PropertyName;

public class PendingDevice {

    private String id;
    private String uniqueDeviceIdentifier;
    private String siteName;
    private String physicalLocation;
    private String dateAdded;
    private String timeAdded;
    private String notes;
    private String quantity;

    @DocumentId
    public String getId() {
        return id;
    }

    @PropertyName("udi")
    public String getUniqueDeviceIdentifier() {
        return uniqueDeviceIdentifier;
    }

    @PropertyName("site_name")
    public String getSiteName() {
        return siteName;
    }

    @PropertyName("physical_location")
    public String getPhysicalLocation() {
        return physicalLocation;
    }

    @PropertyName("date_in")
    public String getDateAdded() {
        return dateAdded;
    }

    @PropertyName("time_in")
    public String getTimeAdded() {
        return timeAdded;
    }

    @PropertyName("notes")
    public String getNotes() {
        return notes;
    }

    @PropertyName("quantity")
    public String getQuantity() {
        return quantity;
    }

    @DocumentId
    public void setId(String id) {
        this.id = id;
    }

    @PropertyName("udi")
    public void setUniqueDeviceIdentifier(String uniqueDeviceIdentifier) {
        this.uniqueDeviceIdentifier = uniqueDeviceIdentifier;
    }

    @PropertyName("site_name")
    public void setSiteName(String siteName) {
        this.siteName = siteName;
    }

    @PropertyName("physical_location")
    public void setPhysicalLocation(String physicalLocation) {
        this.physicalLocation = physicalLocation;
    }

    @PropertyName("date_in")
    public void setDateAdded(String dateAdded) {
        this.dateAdded = dateAdded;
    }

    @PropertyName("time_in")
    public void setTimeAdded(String timeAdded) {
        this.timeAdded = timeAdded;
    }

    @PropertyName("notes")
    public void setNotes(String notes) {
        this.notes = notes;
    }

    @PropertyName("quantity")
    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }
}
