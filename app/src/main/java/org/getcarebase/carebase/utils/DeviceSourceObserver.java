package org.getcarebase.carebase.utils;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.Observer;

import org.getcarebase.carebase.R;
import org.getcarebase.carebase.models.DeviceModel;

import java.util.Objects;

public class DeviceSourceObserver implements Observer<Resource<DeviceModel>> {
    private final LiveData<Resource<DeviceModel>> inventorySource;
    private final LiveData<Resource<DeviceModel>> shippedSource;
    private final LiveData<Resource<DeviceModel>> gudidSource;
    private final MediatorLiveData<Resource<DeviceModel>> finalLiveData;
    public DeviceSourceObserver(LiveData<Resource<DeviceModel>> inventorySource, LiveData<Resource<DeviceModel>> shippedSource,
                                LiveData<Resource<DeviceModel>> gudidSource, MediatorLiveData<Resource<DeviceModel>> finalLiveData) {
        this.inventorySource = inventorySource;
        this.shippedSource = shippedSource;
        this.gudidSource = gudidSource;
        this.finalLiveData = finalLiveData;
    }
    @Override
    public void onChanged(Resource<DeviceModel> deviceModelResource) {
        if (deviceModelResource.getRequest().getStatus() == Request.Status.SUCCESS
                || deviceModelResource.getRequest().getStatus() == Request.Status.ERROR) {
            mediateDataSource(inventorySource,shippedSource,gudidSource);
        }
    }
    private void mediateDataSource(LiveData<Resource<DeviceModel>> inventorySource, LiveData<Resource<DeviceModel>> shippedSource, LiveData<Resource<DeviceModel>> gudidSource) {
        Resource<DeviceModel> inventoryResource = Objects.requireNonNull(inventorySource.getValue());
        Resource<DeviceModel> shippedResource = Objects.requireNonNull(shippedSource.getValue());
        Resource<DeviceModel> gudidResource = Objects.requireNonNull(gudidSource.getValue());
        if (inventoryResource.getRequest().getStatus() == Request.Status.LOADING || shippedResource.getRequest().getStatus() == Request.Status.LOADING ||  gudidResource.getRequest().getStatus() == Request.Status.LOADING) {
            return;
        }

        boolean databaseError = inventoryResource.getRequest().getStatus() == Request.Status.ERROR && shippedResource.getRequest().getStatus() == Request.Status.ERROR;
        if (gudidResource.getRequest().getStatus() == Request.Status.ERROR && gudidResource.getRequest().getResourceString() == R.string.error_something_wrong) {
            finalLiveData.setValue(gudidResource);
        }
        else if (inventoryResource.getRequest().getStatus() == Request.Status.ERROR && inventoryResource.getRequest().getResourceString() == R.string.error_something_wrong) {
            finalLiveData.setValue(inventoryResource);
        }
        else if (gudidResource.getRequest().getStatus() == Request.Status.SUCCESS
                && databaseError) {
            if (inventoryResource.getData() != null) {
                // device that is in gudid also has device model information in database
                DeviceModel databaseDeviceModel = inventoryResource.getData();
                DeviceModel gudidDeviceModel = gudidResource.getData();
                if (gudidResource.getData().getProductions().size() != 0) {
                    databaseDeviceModel.addDeviceProduction(gudidDeviceModel.getProductions().get(0));
                }
                finalLiveData.setValue(new Resource<>(databaseDeviceModel, new Request(null, Request.Status.SUCCESS)));
            } else {
                // device that is in gudid and not in our database
                finalLiveData.setValue(gudidResource);
            }
        } else {
            // device that is not in gudid but has device model information in database
            // device could not be auto populated (no data)
            // or device is in our database
            if (shippedResource.getRequest().getStatus() == Request.Status.SUCCESS) {
                if (inventoryResource.getRequest().getResourceString() != null && inventoryResource.getRequest().getResourceString() == R.string.error_device_lookup) {
                    shippedResource.getData().getProductions().get(0).setPhysicalLocation("");
                    shippedResource.getData().setEquipmentType("");
                    shippedResource.getData().setQuantity(0);
                    finalLiveData.setValue(shippedResource);
                } else {
                    inventoryResource.getData().setShipment(shippedResource.getData().getShipment());
                    finalLiveData.setValue(inventoryResource);
                }
            } else {
                finalLiveData.setValue(inventoryResource);
            }
        }

        // stop listening to these sources
        finalLiveData.removeSource(inventorySource);
        finalLiveData.removeSource(shippedSource);
        finalLiveData.removeSource(gudidSource);
    }
}