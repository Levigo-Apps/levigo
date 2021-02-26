package org.getcarebase.carebase.models;

import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.PropertyName;

public class Shipment {
    private String id;
    private String di;
    private String udi;
    private String sourceHospitalId;
    private String destHospital;
    private int shippedQuantity;
    private int receivedQuantity;
    private boolean received;

    @DocumentId
    public String getId() {
        return id;
    }

    @DocumentId
    public void setId(String id) {
        this.id = id;
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

    @PropertyName("source_hospital_id")
    public String getSourceHospitalId() {
        return sourceHospitalId;
    }

    @PropertyName("source_hospital_id")
    public void setSourceHospitalId (String sourceHospitalId) {
        this.sourceHospitalId = sourceHospitalId;
    }

    @PropertyName("destination_hospital_id")
    public String getDestinationHospitalId() {
        return destHospital;
    }

    @PropertyName("destination_hospital_id")
    public void setDestinationHospitalId(String destHospital) {
        this.destHospital = destHospital;
    }

    @PropertyName("shipped_quantity")
    public int getShippedQuantity() {
        return shippedQuantity;
    }

    @PropertyName("shipped_quantity")
    public void setShippedQuantity(int shippedQuantity) {
        this.shippedQuantity = shippedQuantity;
    }

    @PropertyName("received_quantity")
    public int getReceivedQuantity() {
        return receivedQuantity;
    }

    @PropertyName("received_quantity")
    public void setReceivedQuantity(int receivedQuantity) {
        this.receivedQuantity = receivedQuantity;
    }

    @PropertyName("received")
    public boolean isReceived() {
        return received;
    }

    @PropertyName("received")
    public void setReceived(boolean received) {
        this.received = received;
    }
}
