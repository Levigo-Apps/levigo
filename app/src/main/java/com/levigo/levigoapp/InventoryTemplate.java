package com.levigo.levigoapp;

public class InventoryTemplate {

    private String udi;
    private boolean is_used;
    private String number_added;
    private String lot_number;
    private String reference_number;
    private String expiration;
    private String quantity;
    private String current_time;
    private String physical_location;
    private String notes;
    private String current_date;

    public InventoryTemplate(){
        // empty Constructor
    }

    public InventoryTemplate(String udi, boolean is_used, String number_added,
    String lot_number, String expiration, String quantity,
    String current_time,  String physical_location, String reference_number,
    String notes,String current_date) {

        this.udi = udi;
        this.is_used = is_used;
        this.number_added = number_added;
        this.lot_number = lot_number;
        this.expiration = expiration;
        this.quantity = quantity;
        this.current_time = current_time;
        this.physical_location = physical_location;
        this.reference_number = reference_number;
        this.notes = notes;
        this.current_date = current_date;
    }



    public String getUdi() {
        return udi;
    }
    public void setUdi(String udi) {
        this.udi = udi;
    }

    public boolean getIsUsed(){ return is_used;}
    public void setUsed(boolean is_used){ this.is_used = is_used;}

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

    public String getCurrent_date_time() { return current_time; }
    public void setCurrent_date_time(String current_date_time) { this.current_time = current_date_time; }

    public String getPhysical_location() { return physical_location; }
    public void setPhysical_location(String physical_location) { this.physical_location = physical_location; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public String getNumber_added() { return number_added;}
    public void setNumber_added(String number_added){ this.number_added = number_added;}


    public String getLotNumber() { return lot_number;}
    public void setLotNumber(String lot_number) { this.lot_number = lot_number;}

    public String getReferenceNumber() { return reference_number;}
    public void setReferenceNumber(String reference_number) { this.reference_number = reference_number;}

    public String getCurrent_date() { return current_date;}
    public void setCurrent_date(String current_date){ this.current_date = current_date;}
}
