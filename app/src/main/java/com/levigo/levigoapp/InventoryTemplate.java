package com.levigo.levigoapp;

public class InventoryTemplate {

    private String udi;
    private String name;
    private String equipment_type;
    private String company;
    private boolean isUsed;
    private String radioButtonVal;
    private String procedure_used;
    private String procedure_date;
    private String amountUsed;
    private String patient_id;
    private String number_added;
    private String di;
    private String lotNumber;
    private String expiration;
    private String quantity;
    private String site_name;
    private String current_date_time;
    private String physical_location;
    private String notes;

    public InventoryTemplate(){
        // empty Constructor
    }

    public InventoryTemplate(String name,String equipment_type,String company,String di,String site_name){
        this.name = name;
        this.equipment_type = equipment_type;
        this.company = company;
        this.di = di;
        this.site_name = site_name;
    }

    public InventoryTemplate(String udi, String name,
                             String equipment_type,
                             String company, boolean isUsed, String radioButtonVal, String procedure_used,
                             String procedure_date, String amountUsed, String patient_id, String number_added,
                             String di, String lotNumber, String expiration, String quantity,  String site_name,
                             String current_date_time,  String physical_location,
                             String notes) {

        this.udi = udi;
        this.name = name;
        this.equipment_type = equipment_type;
        this.company = company;
        this.isUsed = isUsed;
        this.radioButtonVal = radioButtonVal;
        this.procedure_used = procedure_used;
        this.procedure_date = procedure_date;
        this.amountUsed = amountUsed;
        this.patient_id = patient_id;
        this.number_added = number_added;
        this.di = di;
        this.lotNumber = lotNumber;
        this.expiration = expiration;
        this.quantity = quantity;
        this.site_name = site_name;
        this.current_date_time = current_date_time;
        this.physical_location = physical_location;
        this.notes = notes;
    }



    public String getUdi() {
        return udi;
    }
    public void setUdi(String udi) {
        this.udi = udi;
    }

    public String getDi() {
        return di;
    }
    public void setDi(String di) {
        this.di = di;
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

    public String getCompany() {
        return company;
    }
    public void setCompany(String company) {
        this.company = company;
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

    public String getSite_name() {
        return site_name;
    }
    public void setSite_name(String site_name) {
        this.site_name = site_name;
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
}
