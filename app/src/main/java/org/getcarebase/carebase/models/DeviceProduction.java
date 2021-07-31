package org.getcarebase.carebase.models;

import android.util.Log;

import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.PropertyName;

import org.getcarebase.carebase.R;
import org.getcarebase.carebase.utils.GreaterThanZeroValidationRule;
import org.getcarebase.carebase.utils.NonEmptyValidationRule;
import org.getcarebase.carebase.utils.ValidationRule;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * class representing the udi level information of a device
 */
public class DeviceProduction {
    private String uniqueDeviceIdentifier;
    private String dateAdded;
    private String timeAdded;
    private String expirationDate;
    private String lotNumber;
    private String notes;
    private int quantity;
    private String referenceNumber;
    private final List<Cost> costs = new ArrayList<>();
    private final List<Procedure> procedures = new ArrayList<>();

    public DeviceProduction() {}

    // TODO use firebase @PropertyName and @Exclude to construct object
    public DeviceProduction(Map<String,Object> data) {
        fromMap(data);
    }

    public void setUniqueDeviceIdentifier(String uniqueDeviceIdentifier) {
        this.uniqueDeviceIdentifier = uniqueDeviceIdentifier;
    }

    public void setDateAdded(String dateAdded) {
        this.dateAdded = dateAdded;
    }

    public void setTimeAdded(String timeAdded) {
        this.timeAdded = timeAdded;
    }

    public void setExpirationDate(String expirationDate) {
        this.expirationDate = expirationDate;
    }

    public void setLotNumber(String lotNumber) {
        this.lotNumber = lotNumber;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public void setReferenceNumber(String referenceNumber) {
        this.referenceNumber = referenceNumber;
    }

    public void addCost(Cost cost) {
        costs.add(cost);
    }

    public void addCosts(List<Cost> costs) {
        this.costs.addAll(costs);
    }

    public void addProcedure(Procedure procedure) {
        procedures.add(procedure);
    }

    public void addProcedures(List<Procedure> procedures) {
        this.procedures.addAll(procedures);
    }

    public Map<String,Integer> isValid() {
        Map<String,Integer> errors = new HashMap<>();
        List<ValidationRule<DeviceProduction,?>> rules = new ArrayList<>();
        rules.add(new NonEmptyValidationRule<>("expirationDate",this::getExpirationDate));
        rules.add(new GreaterThanZeroValidationRule<>("quantity",this::getQuantity));
        try {
            for (ValidationRule<DeviceProduction,?> rule : rules) {
                String name = rule.getFieldName();
                if (!rule.validate(this)) errors.put(name,rule.getReferenceString());
            }
        } catch (Exception e) {
            Log.e(DeviceProduction.class.getSimpleName(),e.getMessage(),e);
            errors.clear();
            errors.put("all", R.string.error_something_wrong);
        }
        return errors;
    }

    public void fromMap(Map<String,Object> data) {
        this.uniqueDeviceIdentifier = (String) data.get("udi");
        this.dateAdded = (String) data.get("current_date");
        this.timeAdded = (String) data.get("current_time");
        this.expirationDate = (String) data.get("expiration");
        this.lotNumber = (String) data.get("lot_number");
        this.notes = (String) data.get("notes");
        try {
            this.quantity = Integer.parseInt((String) data.get("quantity"));
        } catch(ClassCastException e) {
            this.quantity = ((Long) data.get("quantity")).intValue();
        }
        this.referenceNumber = (String) data.get("reference_number");
    }

    public Map<String,Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("udi", uniqueDeviceIdentifier);
        map.put("current_date",dateAdded);
        map.put("current_time",timeAdded);
        map.put("expiration",expirationDate);
        map.put("lot_number",lotNumber);
        map.put("notes",notes);
        map.put("quantity", FieldValue.increment(quantity));
        map.put("reference_number",referenceNumber);
        map.put("physical_location","");
        return map;
    }

    @PropertyName("udi")
    public String getUniqueDeviceIdentifier() {
        return uniqueDeviceIdentifier;
    }
    @PropertyName("current_date")
    public String getDateAdded() {
        return dateAdded;
    }
    @PropertyName("current_time")
    public String getTimeAdded() {
        return timeAdded;
    }
    @PropertyName("expiration")
    public String getExpirationDate() {
        return expirationDate;
    }
    @PropertyName("lot_number")
    public String getLotNumber() {
        return lotNumber;
    }
    @PropertyName("notes")
    public String getNotes() {
        return notes;
    }
    @Exclude
    public String getStringQuantity() {
        return Integer.toString(quantity);
    }
    @PropertyName("quantity")
    public int getQuantity() {
        return quantity;
    }
    @PropertyName("reference_number")
    public String getReferenceNumber() {
        return referenceNumber;
    }
    @Exclude
    public List<Cost> getCosts() {
        return costs;
    }
    @Exclude
    public List<Procedure> getProcedures() {
        return procedures;
    }
}
