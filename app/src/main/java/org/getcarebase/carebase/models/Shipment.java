package org.getcarebase.carebase.models;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.PropertyName;

import java.sql.Time;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class Shipment {
    private String trackingNumber;
    private String sourceEntityId;
    private String destEntityId;
    private Date shippedTime;
    private Date receivedTime;
    private String udi;
    private String di;
    private String deviceName;
    private int quantity;
    private List<Map<String, String>> items;

    @DocumentId
    public String getTrackingNumber() {
        return trackingNumber;
    }

    @DocumentId
    public void setTrackingNumber(String id) {
        this.trackingNumber = id;
    }

    @PropertyName("source_entity_id")
    public String getSourceEntityId() {
        return sourceEntityId;
    }

    @PropertyName("source_entity_id")
    public void setSourceEntityId (String sourceEntityId) {
        this.sourceEntityId = sourceEntityId;
    }

    @PropertyName("destination_entity_id")
    public String getDestinationEntityId() {
        return destEntityId;
    }

    @PropertyName("destination_entity_id")
    public void setDestinationEntityId(String destEntityId) {
        this.destEntityId = destEntityId;
    }

    @PropertyName("date_time_shipped")
    public Date getShippedTime() {
        return shippedTime;
    }

    @PropertyName("date_time_shipped")
    public void setShippedTime(Date shippedTime) {
        this.shippedTime = shippedTime;
    }

    @PropertyName("date_time_received")
    public Date getReceivedTime() {
        return receivedTime;
    }

    @PropertyName("date_time_received")
    public void setReceivedTime(Date receivedTime) {
        this.receivedTime = receivedTime;
    }

    @PropertyName("items")
    public List<Map<String, String>> getItems() {
        return items;
    }

    @PropertyName("items")
    public void setItems(List<Map<String, String>> items) {
        this.items = items;
    }

    // Individual Device properties ------------------------------

    @Exclude
    public String getDi() {
        return di;
    }

    @Exclude
    public void setDi(String di) {
        this.di = di;
    }

    @Exclude
    public String getUdi() {
        return udi;
    }

    @Exclude
    public void setUdi(String udi) {
        this.udi = udi;
    }

    @Exclude
    public String getDeviceName() {
        return deviceName;
    }

    @Exclude
    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    @Exclude
    public int getQuantity() {
        return quantity;
    }

    @Exclude
    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

}
