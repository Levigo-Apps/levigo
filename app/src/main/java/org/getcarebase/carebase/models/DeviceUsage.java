package org.getcarebase.carebase.models;

import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.PropertyName;

/**
 * represents the usage of a device in a medical procedure
 */
public class DeviceUsage {
    private String deviceIdentifier;
    private String uniqueDeviceIdentifier;
    private String name;
    private int currentProductionAmount;
    private int amountUsed;
    private int currentModelQuantity;

    public DeviceUsage() {}

    public DeviceUsage(String deviceIdentifier, String uniqueDeviceIdentifier, String name, int currentProductionAmount, int currentModelQuantity) {
        this.deviceIdentifier = deviceIdentifier;
        this.uniqueDeviceIdentifier = uniqueDeviceIdentifier;
        this.name = name;
        this.currentProductionAmount = currentProductionAmount;
        this.currentModelQuantity = currentModelQuantity;
        this.amountUsed = 1;
    }

    public void incrementAmountUsed() {
        amountUsed++;
        checkAmountUsed();
    }

    public void decrementAmountUsed() {
        amountUsed--;
        checkAmountUsed();
    }

    private void checkAmountUsed() {
        if (amountUsed < 1)
            throw new Error("The amount used of the device cannot be less than one");
    }

    @Exclude
    public int getNewProductionQuantity() {
        return Math.max(0, currentProductionAmount - amountUsed);
    }

    @Exclude
    public int getNewModelQuantity() { return Math.max(0, currentModelQuantity - Math.min(currentProductionAmount, amountUsed)); }

    @PropertyName("amount_used")
    public int getAmountUsed() {
        return amountUsed;
    }

    @PropertyName("amount_used")
    public void setAmountUsed(int amountUsed) {
        this.amountUsed = amountUsed;
    }

    @PropertyName("di")
    public String getDeviceIdentifier() {
        return deviceIdentifier;
    }

    @PropertyName("di")
    public void setDeviceIdentifier(String deviceIdentifier) {
        this.deviceIdentifier = deviceIdentifier;
    }

    @PropertyName("udi")
    public String getUniqueDeviceIdentifier() {
        return uniqueDeviceIdentifier;
    }

    @PropertyName("udi")
    public void setUniqueDeviceIdentifier(java.lang.String uniqueDeviceIdentifier) {
        this.uniqueDeviceIdentifier = uniqueDeviceIdentifier;
    }

    @PropertyName("name")
    public String getName() {
        return name;
    }

    @PropertyName("name")
    public void setName(String name) {
        this.name = name;
    }
}
