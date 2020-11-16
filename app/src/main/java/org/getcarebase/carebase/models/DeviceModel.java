package org.getcarebase.carebase.models;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * immutable class representing the di level information of a device and contains all of the different
 * productions of the device
 */
public class DeviceModel {
    private final String deviceIdentifier;
    private final String name;
    private final String company;
    private final String description;
    private final String equipmentType;
    private final String siteName;
    private final String usage;
    private final int quantity;
    private final List<DeviceProduction> productions;

    public DeviceModel(Map<String,Object> data) {
        this.deviceIdentifier = (String) data.get("di");
        this.name = (String) data.get("name");
        this.company = (String) data.get("company");
        this.description = (String) data.get("description");
        this.equipmentType = (String) data.get("equipment_type");
        this.siteName = (String) data.get("site_name");
        this.usage = (String) data.get("usage");
        this.quantity = Integer.parseInt((String) data.get("quantity"));
        this.productions = new ArrayList<>();
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

    public void addDeviceProduction(DeviceProduction production) {
        productions.add(production);
    }

    public List<DeviceProduction> getProductions() {
        return productions;
    }
}
