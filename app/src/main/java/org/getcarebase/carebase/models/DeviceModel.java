package org.getcarebase.carebase.models;

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
    private String equipmentType;
    private String medicalSpecialty;
    private String siteName;
    private String usage;
    private int quantity;
    private final List<DeviceProduction> productions = new ArrayList<>();
    private final Map<String, Object> specifications = new HashMap<String, Object>();

    public DeviceModel() {}

    public DeviceModel(Map<String,Object> data) {
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
        this.medicalSpecialty = (String) data.get("medical_specialty");
        data.remove("medical_specialty");
        this.siteName = (String) data.get("site_name");
        data.remove("site_name");
        this.usage = (String) data.get("usage");
        data.remove("usage");
        try {
            this.quantity = Integer.parseInt((String) data.get("quantity"));
        } catch(ClassCastException e) {
            this.quantity = ((Long) data.get("quantity")).intValue();
        }
        data.remove("quantity");
        // put all remaining keys into the specification map
        specifications.putAll(data);
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

    public void setEquipmentType(String equipmentType) {
        this.equipmentType = equipmentType;
    }

    public void setMedicalSpecialty(String medicalSpecialty) {
        this.medicalSpecialty = medicalSpecialty;
    }

    public void setSiteName(String siteName) {
        this.siteName = siteName;
    }

    public void setUsage(String usage) {
        this.usage = usage;
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

    public String getEquipmentType() {
        return equipmentType;
    }

    public String getMedicalSpecialty() {
        return medicalSpecialty;
    }

    public String getSiteName() {
        return siteName;
    }

    public String getUsage() {
        return usage;
    }

    public int getQuantity() {
        return quantity;
    }

    public List<DeviceProduction> getProductions() {
        return productions;
    }

    public List<Map.Entry<String,Object>> getSpecificationList() {
        return new ArrayList<>(specifications.entrySet());
    }

    // TODO find a way to save to firebase without turning to map
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("company",company);
        map.put("device_description",description);
        map.put("di",deviceIdentifier);
        map.put("equipment_type",equipmentType);
        map.put("medical_specialty",medicalSpecialty);
        map.put("name",name);
//        map.put("quantity",Integer.toString(quantity));
        map.put("quantity",quantity);
        map.put("site_name",siteName);
        map.put("usage",usage);
        map.putAll(specifications);
        return map;
    }
}
