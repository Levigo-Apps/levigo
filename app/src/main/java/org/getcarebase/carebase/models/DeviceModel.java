package org.getcarebase.carebase.models;

import java.util.HashMap;
import java.util.Map;

public class DeviceModel {
    private String deviceIdentifier;
    private String name;
    private String company;
    private String description;
    private String equipmentType;
    private String siteName;
    private String usage;
    private int quantity;
    private final Map<String,DeviceProduction> productions;

    public DeviceModel(Map<String,Object> data) {
        updateFields(data);
        this.productions = new HashMap<>();
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

    public String getSiteName() {
        return siteName;
    }

    public String getUsage() {
        return usage;
    }

    public int getQuantity() {
        return quantity;
    }

    public void updateFields(Map<String,Object> data) {
        this.deviceIdentifier = (String) data.get("di");
        this.name = (String) data.get("name");
        this.company = (String) data.get("company");
        this.description = (String) data.get("description");
        this.equipmentType = (String) data.get("equipment_type");
        this.siteName = (String) data.get("site_name");
        this.usage = (String) data.get("usage");
        this.quantity = Integer.parseInt((String) data.get("quantity"));
    }

    public void addDeviceProduction(DeviceProduction production) {
        productions.put(production.getUniqueDeviceIdentifier(), production);
    }

    public DeviceProduction removeProduction(String udi) {
        return productions.remove(udi);
    }

    public Map<String,DeviceProduction> getProductions() {
        return productions;
    }
}
