package org.getcarebase.carebase.models;

import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.PropertyName;

public class Shipment {
    private String trackingNumber;
    private String sourceEntityId;
    private String destEntityId;
    private String shippedTime;
    private String receivedTime;
    private String udi;
    private String di;
    private int quantity;

    @DocumentId
    public String getId() {
        return trackingNumber;
    }

    @DocumentId
    public void setId(String id) {
        this.trackingNumber = id;
    }

    @PropertyName("di")
    public String getDi() {
        return di;
    }

    @PropertyName("di")
    public void setDi(String di) {
        this.di = di;
    }

    @PropertyName("udi")
    public String getUdi() {
        return udi;
    }

    @PropertyName("udi")
    public void setUdi(String udi) {
        this.udi = udi;
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

    @PropertyName("quantity")
    public int getQuantity() {
        return quantity;
    }

    @PropertyName("quantity")
    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    @PropertyName("date_time_shipped")
    public String getShippedTime() {
        return shippedTime;
    }

    @PropertyName("date_time_shipped")
    public void setShippedTime(String shippedTime) {
        this.shippedTime = shippedTime;
    }

    @PropertyName("date_time_received")
    public String getReceivedTime() {
        return receivedTime;
    }

    @PropertyName("date_time_received")
    public void setReceivedTime(String receivedTime) {
        this.receivedTime = receivedTime;
    }
}
