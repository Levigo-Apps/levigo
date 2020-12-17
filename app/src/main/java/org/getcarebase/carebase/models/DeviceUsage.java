package org.getcarebase.carebase.models;

public class DeviceUsage {
    private final String deviceIdentifier;
    private final String uniqueDeviceIdentifier;
    private int currentAmount;
    private int amountUsed;

    public DeviceUsage(String deviceIdentifier, String uniqueDeviceIdentifier, int currentAmount) {
        this.deviceIdentifier = deviceIdentifier;
        this.uniqueDeviceIdentifier = uniqueDeviceIdentifier;
        this.currentAmount = currentAmount;
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

    public int getNewQuantity() {
        return Math.max(0, currentAmount - amountUsed);
    }

    public int getAmountUsed() {
        return amountUsed;
    }

    public String getDeviceIdentifier() {
        return deviceIdentifier;
    }

    public String getUniqueDeviceIdentifier() {
        return uniqueDeviceIdentifier;
    }
}
