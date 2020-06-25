package com.levigo.levigoapp;

public class InventoryTemplate {

    private String barcode;
    private String name;
    private String equipment_type;
    private String manufacturer;
    private String procedure_used;
    private String procedure_date;
    private String patient_id;
    private String expiration;
    private String quantity;
    private String hospital_name;
    private String current_date_time;
    private String physical_location;
    private String notes;

    public InventoryTemplate(){
        // empty Constructor
    }

    public InventoryTemplate(String barcode, String name,
                             String equipment_type,
                             String manufacturer,  String procedure_used,
                             String procedure_date,
                             String patient_id,  String expiration,
                             String quantity,  String hospital_name,
                             String current_date_time,  String physical_location,
                              String notes) {

        this.barcode = barcode;
        this.name = name;
        this.equipment_type = equipment_type;
        this.manufacturer = manufacturer;
        this.procedure_used = procedure_used;
        this.procedure_date = procedure_date;
        this.patient_id = patient_id;
        this.expiration = expiration;
        this.quantity = quantity;
        this.hospital_name = hospital_name;
        this.current_date_time = current_date_time;
        this.physical_location = physical_location;
        this.notes = notes;
    }



    public String getBarcode() {
        return barcode;
    }
    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public String getEquipment_type() {
        return equipment_type;
    }
    public void setEquipment_type(String equipment_type) {
        this.equipment_type = equipment_type;
    }

    public String getManufacturer() {
        return manufacturer;
    }
    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

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

    public String getHospital_name() {
        return hospital_name;
    }
    public void setHospital_name(String hospital_name) {
        this.hospital_name = hospital_name;
    }

    public String getCurrent_date_time() { return current_date_time; }
    public void setCurrent_date_time(String current_date_time) { this.current_date_time = current_date_time; }

    public String getPhysical_location() { return physical_location; }
    public void setPhysical_location(String physical_location) { this.physical_location = physical_location; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
