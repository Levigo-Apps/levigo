package com.levigo.levigoapp;

public class InventoryTemplate {
    private String barcode_field;
    private String barcode;
    private String name_field;
    private String name;
    private String equipment_type_field;
    private String equipment_type;
    private String manufacturer_field;
    private String manufacturer;
    private String procedure_used_field;
    private String procedure_used;
    private String procedure_date_field;
    private String procedure_date;
    private String patiend_id_field;
    private String patient_id;
    private String expiration_field;
    private String expiration;
    private String quantity_field;
    private String quantity;
    private String hospital_name_field;
    private String hospital_name;
    private String current_date_time_field;
    private String current_date_time;
    private String physical_location_field;
    private String physical_location;
    private String notes_field;
    private String notes;

    public InventoryTemplate(){
        // empty Constructor
    }

    public InventoryTemplate(String barcode_field, String barcode, String name_field, String name,
                             String equipment_type_field, String equipment_type, String manufacturer_field,
                             String manufacturer, String procedure_used_field, String procedure_used,
                             String procedure_date_field, String procedure_date, String patiend_id_field,
                             String patient_id, String expiration_field, String expiration, String quantity_field,
                             String quantity, String hospital_name_field, String hospital_name,String current_date_time_field,
                             String current_date_time, String physical_location_field, String physical_location,
                             String notes_field, String notes) {
        this.barcode_field = barcode_field;
        this.barcode = barcode;
        this.name_field = name_field;
        this.name = name;
        this.equipment_type_field = equipment_type_field;
        this.equipment_type = equipment_type;
        this.manufacturer_field = manufacturer_field;
        this.manufacturer = manufacturer;
        this.procedure_used_field = procedure_used_field;
        this.procedure_used = procedure_used;
        this.procedure_date_field = procedure_date_field;
        this.procedure_date = procedure_date;
        this.patiend_id_field = patiend_id_field;
        this.patient_id = patient_id;
        this.expiration_field = expiration_field;
        this.expiration = expiration;
        this.quantity_field = quantity_field;
        this.quantity = quantity;
        this.hospital_name_field = hospital_name_field;
        this.hospital_name = hospital_name;
        this.current_date_time_field = current_date_time_field;
        this.current_date_time = current_date_time;
        this.physical_location_field = physical_location_field;
        this.physical_location = physical_location;
        this.notes_field = notes_field;
        this.notes = notes;
    }

    public String getBarcode_field() {
        return barcode_field;
    }

    public void setBarcode_field(String barcode_field) {
        this.barcode_field = barcode_field;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public String getName_field() {
        return name_field;
    }

    public void setName_field(String name_field) {
        this.name_field = name_field;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEquipment_type_field() {
        return equipment_type_field;
    }

    public void setEquipment_type_field(String equipment_type_field) {
        this.equipment_type_field = equipment_type_field;
    }

    public String getEquipment_type() {
        return equipment_type;
    }

    public void setEquipment_type(String equipment_type) {
        this.equipment_type = equipment_type;
    }

    public String getManufacturer_field() {
        return manufacturer_field;
    }

    public void setManufacturer_field(String manufacturer_field) {
        this.manufacturer_field = manufacturer_field;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public String getProcedure_used_field() {
        return procedure_used_field;
    }

    public void setProcedure_used_field(String procedure_used_field) {
        this.procedure_used_field = procedure_used_field;
    }

    public String getProcedure_used() {
        return procedure_used;
    }

    public void setProcedure_used(String procedure_used) {
        this.procedure_used = procedure_used;
    }

    public String getProcedure_date_field() {
        return procedure_date_field;
    }

    public void setProcedure_date_field(String procedure_date_field) {
        this.procedure_date_field = procedure_date_field;
    }

    public String getProcedure_date() {
        return procedure_date;
    }

    public void setProcedure_date(String procedure_date) {
        this.procedure_date = procedure_date;
    }

    public String getPatiend_id_field() {
        return patiend_id_field;
    }

    public void setPatiend_id_field(String patiend_id_field) {
        this.patiend_id_field = patiend_id_field;
    }

    public String getPatient_id() {
        return patient_id;
    }

    public void setPatient_id(String patient_id) {
        this.patient_id = patient_id;
    }

    public String getExpiration_field() {
        return expiration_field;
    }

    public void setExpiration_field(String expiration_field) {
        this.expiration_field = expiration_field;
    }

    public String getExpiration() {
        return expiration;
    }

    public void setExpiration(String expiration) {
        this.expiration = expiration;
    }

    public String getQuantity_field() {
        return quantity_field;
    }

    public void setQuantity_field(String quantity_field) {
        this.quantity_field = quantity_field;
    }

    public String getQuantity() {
        return quantity;
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }

    public String getHospital_name_field() {
        return hospital_name_field;
    }

    public void setHospital_name_field(String hospital_name_field) {
        this.hospital_name_field = hospital_name_field;
    }

    public String getHospital_name() {
        return hospital_name;
    }

    public void setHospital_name(String hospital_name) {
        this.hospital_name = hospital_name;
    }

    public String getCurrent_date_time_field(){
        return current_date_time_field;
    }

    public void setCurrent_date_time_field(String current_date_time_field) {
        this.current_date_time_field = current_date_time_field;
    }
    public String getCurrent_date_time() {
        return current_date_time;
    }

    public void setCurrent_date_time(String current_date_time) {
        this.current_date_time = current_date_time;
    }



    public String getPhysical_location_field() {
        return physical_location_field;
    }

    public void setPhysical_location_field(String physical_location_field) {
        this.physical_location_field = physical_location_field;
    }

    public String getPhysical_location() {
        return physical_location;
    }

    public void setPhysical_location(String physical_location) {
        this.physical_location = physical_location;
    }

    public String getNotes_field() {
        return notes_field;
    }

    public void setNotes_field(String notes_field) {
        this.notes_field = notes_field;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
