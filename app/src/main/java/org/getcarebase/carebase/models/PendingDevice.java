package org.getcarebase.carebase.models;

import com.google.firebase.firestore.PropertyName;

public class PendingDevice {
    @PropertyName("udi")
    String uniqueDeviceIdentifier;
    @PropertyName("date_in")
    String dateAdded;
    @PropertyName("time_in")
    String timeAdded;
    @PropertyName("notes")
    String notes;
    @PropertyName("quantity")
    int quantity = 0;

    public PendingDevice() {}
}
