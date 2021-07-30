package org.getcarebase.carebase.models;

import android.util.Log;

import org.getcarebase.carebase.R;
import org.getcarebase.carebase.utils.NonEmptyValidationRule;
import org.getcarebase.carebase.utils.ValidationRule;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * class representing the di level information of a device and contains all of the different
 * productions of the device
 */
public class DeviceModel implements Serializable {
    private String deviceIdentifier;
    private String name;
    private String company;
    private String description;
    // fda product code classification
    private String productCode;
    private String equipmentType;
    private List<String> tags;
    private int quantity;
    private final List<DeviceProduction> productions = new ArrayList<>();
    private final Map<String, Object> specifications = new HashMap<String, Object>();

    public DeviceModel() {}

    public DeviceModel(Map<String,Object> data) {
        fromMap(data);
    }

    public void setDeviceIdentifier(String deviceIdentifier) {
        this.deviceIdentifier = deviceIdentifier;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setProductCode(String productCode) {
        this.productCode = productCode;
    }

    public void setEquipmentType(String equipmentType) {
        this.equipmentType = equipmentType;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public void addDeviceProduction(DeviceProduction production) {
        productions.add(production);
    }

    public void addSpecification(String spec, String value) {
        specifications.put(spec,value);
    }

    public String getDeviceIdentifier() {
        return deviceIdentifier;
    }

    public String getName() {
        return name;
    }

    public String getCompany() {
        return company;
    }

    public String getDescription() {
        return description;
    }

    public String getProductCode() {
        return productCode;
    }

    public String getEquipmentType() {
        return equipmentType;
    }

    public List<String> getTags() { return tags; }

    public int getQuantity() {
        return quantity;
    }

    public List<DeviceProduction> getProductions() {
        return productions;
    }

    public List<Map.Entry<String,Object>> getSpecificationList() {
        return new ArrayList<>(specifications.entrySet());
    }

    public Map<String,Integer> isValid() {
        Map<String,Integer> errors = new HashMap<>();
        List<ValidationRule<DeviceModel,?>> rules = new ArrayList<>();
        rules.add(new NonEmptyValidationRule<>("name",this::getName));
        rules.add(new NonEmptyValidationRule<>("description",this::getDescription));

        try {
            for (ValidationRule<DeviceModel,?> rule : rules) {
                String name = rule.getFieldName();
                if (!rule.validate(this)) errors.put(name,rule.getReferenceString());
            }
            for (DeviceProduction production : productions) {
                errors.putAll(production.isValid());
            }
        } catch (Exception e) {
            Log.e(DeviceModel.class.getSimpleName(),e.getMessage(),e);
            errors.clear();
            errors.put("all", R.string.error_something_wrong);
        }
        return errors;
    }

    public void fromMap(Map<String,Object> data) {
        this.deviceIdentifier = (String) data.get("di");
        data.remove("di");
        this.name = (String) data.get("name");
        data.remove("name");
        this.company = (String) data.get("company");
        data.remove("company");
        this.description = (String) data.get("device_description");
        data.remove("device_description");
        this.equipmentType = (String) data.get("equipment_type");
        data.remove("equipment_type");
        this.tags = (List<String>) data.get("tags");
        data.remove("tags");
        // DEPRECIATED
        data.remove("medical_specialty");
        // DEPRECIATED
        data.remove("site_name");
        // DEPRECIATED
        data.remove("usage");
        // DEPRECIATED
        data.remove("sub_type");
        try {
            this.quantity = Integer.parseInt((String) data.get("quantity"));
        } catch(ClassCastException e) {
            this.quantity = ((Long) data.get("quantity")).intValue();
        }
        data.remove("quantity");
        // put all remaining keys into the specification map
        specifications.putAll(data);
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("company",company);
        map.put("device_description",description);
        map.put("di",deviceIdentifier);
        map.put("equipment_type",equipmentType);
        map.put("tags",tags);
        map.put("name",name);
        map.put("quantity",quantity);
        map.putAll(specifications);
        return map;
    }
}
