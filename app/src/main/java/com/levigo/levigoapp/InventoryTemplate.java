package com.levigo.levigoapp;

public class InventoryTemplate {

    private String udi;
    private boolean isUsed;
    private String radioButtonVal;
    private String procedure_used;
    private String procedure_date;
    private String amountUsed;
    private String patient_id;
    private String number_added;
    private String lotNumber;
    private String referenceNumber;
    private String expiration;
    private String quantity;
    private String current_date_time;
    private String physical_location;
    private String notes;

    public InventoryTemplate(){
        // empty Constructor
    }

    public InventoryTemplate(String udi, boolean isUsed, String radioButtonVal, String procedure_used,
    String procedure_date, String amountUsed, String patient_id, String number_added,
    String lotNumber, String expiration, String quantity,
    String current_date_time,  String physical_location, String referenceNumber,
    String notes) {

        this.udi = udi;
        this.isUsed = isUsed;
        this.radioButtonVal = radioButtonVal;
        this.procedure_used = procedure_used;
        this.procedure_date = procedure_date;
        this.amountUsed = amountUsed;
        this.patient_id = patient_id;
        this.number_added = number_added;
        this.lotNumber = lotNumber;
        this.referenceNumber = referenceNumber;
        this.expiration = expiration;
        this.quantity = quantity;
        this.current_date_time = current_date_time;
        this.physical_location = physical_location;
        this.referenceNumber = referenceNumber;
        this.notes = notes;
    }



    public String getUdi() {
        return udi;
    }
    public void setUdi(String udi) {
        this.udi = udi;
    }

    public boolean getIsUsed(){ return isUsed;}
    public void setUsed(boolean isUsed){ this.isUsed = isUsed;}

    public String getRadioButtonVal(){ return radioButtonVal;}
    public void setRadioButtonVal(String radioButtonVal){ this.radioButtonVal = radioButtonVal;}

    public String getProcedure_used() {
        return procedure_used;
    }
    public void setProcedure_used(String procedure_used) {
        this.procedure_used = procedure_used;
    }

    public String getProcedure_date() { return procedure_date; }
    public void setProcedure_date(String procedure_date) {
        this.procedure_date = procedure_date;
    }

    public String getAmountUsed(){ return amountUsed;}
    public void setAmountUsed(String amountUsed){ this.amountUsed = amountUsed;}

    public String getPatient_id() {
        return patient_id;
    }
    public void setPatient_id(String patient_id) {
        this.patient_id = patient_id;
    }

    public String getExpiration() {
        return expiration;
    }
    public void setExpiration(String expiration) {
        this.expiration = expiration;
    }

    public String getQuantity() {
        return quantity;
    }
    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }

    public String getCurrent_date_time() { return current_date_time; }
    public void setCurrent_date_time(String current_date_time) { this.current_date_time = current_date_time; }

    public String getPhysical_location() { return physical_location; }
    public void setPhysical_location(String physical_location) { this.physical_location = physical_location; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public String getNumber_added() { return number_added;}
    public void setNumber_added(String number_added){ this.number_added = number_added;}


    public String getLotNumber() { return lotNumber;}
    public void setLotNumber(String lotNumber) { this.lotNumber = lotNumber;}

    public String getReferenceNumber() { return referenceNumber;}
    public void setReferenceNumber(String referenceNumber) { this.lotNumber = lotNumber;}
}
