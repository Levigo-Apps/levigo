package org.getcarebase.carebase.viewmodels;

import androidx.databinding.Bindable;
import androidx.databinding.InverseMethod;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModel;

import org.getcarebase.carebase.R;
import org.getcarebase.carebase.models.DeviceModel;
import org.getcarebase.carebase.models.User;
import org.getcarebase.carebase.repositories.DeviceRepository;
import org.getcarebase.carebase.repositories.FirebaseAuthRepository;
import org.getcarebase.carebase.utils.Request;
import org.getcarebase.carebase.utils.Resource;

import java.util.Objects;

public class AddDeviceViewModel extends ViewModel {
    private final FirebaseAuthRepository authRepository = new FirebaseAuthRepository();
    private DeviceRepository deviceRepository;

    private LiveData<Resource<User>> userLiveData;
    public MutableLiveData<String> uniqueDeviceIdentifierLiveData = new MutableLiveData<>();
    private final MediatorLiveData<Resource<DeviceModel>> deviceModelLiveData = new MediatorLiveData<>();

    public LiveData<Resource<User>> getUserLiveData() {
        if (userLiveData == null) {
            userLiveData = authRepository.getUser();
        }
        return userLiveData;
    }

    public LiveData<Resource<DeviceModel>> getDeviceModelLiveData() {
        return deviceModelLiveData;
    }

    public void setupDeviceRepository(String networkId, String entityId) {
        deviceRepository = new DeviceRepository(networkId,entityId);
    }

    public void onAutoPopulate() {
        String barcode = uniqueDeviceIdentifierLiveData.getValue();
        deviceModelLiveData.setValue(new Resource<>(null,new Request(null,Request.Status.LOADING)));
        LiveData<Resource<DeviceModel>> inventorySource = deviceRepository.autoPopulateFromDatabase(barcode);
        LiveData<Resource<DeviceModel>> gudidSource = deviceRepository.autoPopulateFromGUDID(barcode);
        DeviceSourceObserver deviceSourceObserver = new DeviceSourceObserver(inventorySource,gudidSource);
        deviceModelLiveData.addSource(inventorySource,deviceSourceObserver);
        deviceModelLiveData.addSource(gudidSource,deviceSourceObserver);
    }

    private class DeviceSourceObserver implements Observer<Resource<DeviceModel>> {
        private final LiveData<Resource<DeviceModel>> inventorySource;
        private final LiveData<Resource<DeviceModel>> gudidSource;

        public DeviceSourceObserver(LiveData<Resource<DeviceModel>> inventorySource,LiveData<Resource<DeviceModel>> gudidSource) {
            this.inventorySource = inventorySource;
            this.gudidSource = gudidSource;
        }
        @Override
        public void onChanged(Resource<DeviceModel> deviceModelResource) {
            if (deviceModelResource.getRequest().getStatus() == Request.Status.SUCCESS
                    || deviceModelResource.getRequest().getStatus() == Request.Status.ERROR) {
                mediateDataSource(inventorySource,gudidSource);
            }
        }
    }

    private void mediateDataSource(LiveData<Resource<DeviceModel>> inventorySource, LiveData<Resource<DeviceModel>> gudidSource) {
        Resource<DeviceModel> inventoryResource = Objects.requireNonNull(inventorySource.getValue());
        Resource<DeviceModel> gudidResource = Objects.requireNonNull(gudidSource.getValue());
        if (inventoryResource.getRequest().getStatus() == Request.Status.LOADING ||  gudidResource.getRequest().getStatus() == Request.Status.LOADING) {
            return;
        }

        boolean databaseError = inventoryResource.getRequest().getStatus() == Request.Status.ERROR;
        if (gudidResource.getRequest().getStatus() == Request.Status.ERROR && gudidResource.getRequest().getResourceString() == R.string.error_something_wrong) {
            deviceModelLiveData.setValue(gudidResource);
        }
        else if (inventoryResource.getRequest().getStatus() == Request.Status.ERROR && inventoryResource.getRequest().getResourceString() == R.string.error_something_wrong) {
            deviceModelLiveData.setValue(inventoryResource);
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
                deviceModelLiveData.setValue(new Resource<>(databaseDeviceModel, new Request(null, Request.Status.SUCCESS)));
            } else {
                // device that is in gudid and not in our database
                deviceModelLiveData.setValue(gudidResource);
            }
        } else {
            // device that is not in gudid but has device model information in database
            // device could not be auto populated (no data)
            // or device is in our database
            deviceModelLiveData.setValue(inventoryResource);
        }

        // stop listening to these sources
        deviceModelLiveData.removeSource(inventorySource);
        deviceModelLiveData.removeSource(gudidSource);
    }
}
