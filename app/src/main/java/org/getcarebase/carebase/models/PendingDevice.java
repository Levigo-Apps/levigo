package org.getcarebase.carebase.models;

import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.PropertyName;

public class PendingDevice {
    @DocumentId
    String id;
    @PropertyName("udi")
    String uniqueDeviceIdentifier;
    @PropertyName("date_in")
    String dateAdded;
    @PropertyName("time_in")
    String timeAdded;
    @PropertyName("notes")
    String notes;
    @PropertyName("quantity")
    String quantity;
}
