package org.getcarebase.carebase.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import org.getcarebase.carebase.models.DeviceModel;
import org.getcarebase.carebase.models.User;
import org.getcarebase.carebase.repositories.DeviceRepository;
import org.getcarebase.carebase.repositories.FirebaseAuthRepository;
import org.getcarebase.carebase.utils.Request;
import org.getcarebase.carebase.utils.Resource;

import java.util.Objects;

public class DeviceViewModel extends ViewModel {
    private DeviceRepository deviceRepository;
    private final FirebaseAuthRepository authRepository;

    private LiveData<Resource<User>> userLiveData;

    private final MediatorLiveData<Resource<DeviceModel>> autoPopulatedDeviceLiveData = new MediatorLiveData<>();
    
    // when a user tries to save a device this live data will be updated
    private final MutableLiveData<DeviceModel> saveDeviceLiveData = new MutableLiveData<>();

    private final MutableLiveData<String> saveDeviceTypeLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> savePhysicalLocationLiveData = new MutableLiveData<>();

    // requests to save a device will be sent to this live data
    private final LiveData<Request> saveDeviceRequestLiveData = Transformations.switchMap(saveDeviceLiveData, deviceModel -> deviceRepository.saveDevice(deviceModel));

    private final LiveData<Request> saveDeviceTypeRequestLiveData = Transformations.switchMap(saveDeviceTypeLiveData, deviceType -> deviceRepository.saveDeviceType(deviceType));
    private final LiveData<Request> savePhysicalLocationRequestLiveData = Transformations.switchMap(savePhysicalLocationLiveData, physicalLocation -> deviceRepository.savePhysicalLocation(physicalLocation));



    public DeviceViewModel() {
        authRepository = new FirebaseAuthRepository();
    }

    public LiveData<Resource<User>> getUserLiveData() {
        if (userLiveData == null) {
            userLiveData = authRepository.getUser();
        }
        return userLiveData;
    }

    public void setupDeviceRepository() {
        User user = Objects.requireNonNull(userLiveData.getValue()).getData();
        deviceRepository = new DeviceRepository(user.getNetworkId(), user.getHospitalId());
    }

    public LiveData<Resource<String[]>> getDeviceTypesLiveData() {
        return deviceRepository.getDeviceTypeOptions();
    }

    public void saveDeviceType(String deviceType) {
        saveDeviceTypeLiveData.setValue(deviceType);
    }

    public LiveData<Resource<String[]>> getSitesLiveData() {
        return deviceRepository.getSiteOptions();
    }

    public LiveData<Resource<String[]>> getPhysicalLocationsLiveData() {
        return deviceRepository.getPhysicalLocationOptions();
    }

    public void savePhysicalLocation(String physicalLocation) {
        savePhysicalLocationLiveData.setValue(physicalLocation);
    }

    public void saveDevice(DeviceModel deviceModel) {
        saveDeviceLiveData.setValue(deviceModel);
    }

    public LiveData<Resource<DeviceModel>> getAutoPopulatedDeviceLiveData() {
        return autoPopulatedDeviceLiveData;
    }

    public void autoPopulatedScannedBarcode(String barcode) {
        autoPopulatedDeviceLiveData.setValue(new Resource<>(null,new Request(null,Request.Status.LOADING)));
        LiveData<Resource<DeviceModel>> databaseSource = deviceRepository.autoPopulateFromDatabase(barcode);
        LiveData<Resource<DeviceModel>> gudidSource = deviceRepository.autoPopulateFromGUDID(barcode);
        DeviceSourceObserver deviceSourceObserver = new DeviceSourceObserver(databaseSource,gudidSource);
        autoPopulatedDeviceLiveData.addSource(databaseSource,deviceSourceObserver);
        autoPopulatedDeviceLiveData.addSource(gudidSource,deviceSourceObserver);
    }

    private class DeviceSourceObserver implements Observer<Resource<DeviceModel>> {
        private final LiveData<Resource<DeviceModel>> databaseSource;
        private final LiveData<Resource<DeviceModel>> gudidSource;
        public DeviceSourceObserver(LiveData<Resource<DeviceModel>> databaseSource, LiveData<Resource<DeviceModel>> gudidSource) {
            this.databaseSource = databaseSource;
            this.gudidSource = gudidSource;
        }
        @Override
        public void onChanged(Resource<DeviceModel> deviceModelResource) {
            if (deviceModelResource.getRequest().getStatus() == Request.Status.SUCCESS
                || deviceModelResource.getRequest().getStatus() == Request.Status.ERROR) {
                mediateDataSource(databaseSource,gudidSource);
            }
        }
    }

    private void mediateDataSource(LiveData<Resource<DeviceModel>> databaseSource, LiveData<Resource<DeviceModel>> gudidSource) {
        Resource<DeviceModel> databaseResource = Objects.requireNonNull(databaseSource.getValue());
        Resource<DeviceModel> gudidResource = Objects.requireNonNull(gudidSource.getValue());
        if (databaseResource.getRequest().getStatus() == Request.Status.LOADING || gudidResource.getRequest().getStatus() == Request.Status.LOADING) {
            return;
        }

        if (gudidResource.getRequest().getStatus() == Request.Status.ERROR
                && databaseResource.getRequest().getStatus() == Request.Status.ERROR) {
            if (databaseResource.getData() != null) {
                // device that is not in gudid has device model information in database
                autoPopulatedDeviceLiveData.setValue(databaseResource);
            } else {
                // device could not auto populated
                // TODO make error message in strings
                autoPopulatedDeviceLiveData.setValue(new Resource<>(null, new Request(null, Request.Status.ERROR)));
            }
        } else if (gudidResource.getRequest().getStatus() == Request.Status.SUCCESS
                && databaseResource.getRequest().getStatus() == Request.Status.ERROR) {
            if (databaseResource.getData() != null) {
                // device that is in gudid also has device model information in database
                DeviceModel gudidDeviceModel = gudidResource.getData();
                DeviceModel databaseDeviceModel = databaseResource.getData();
                databaseDeviceModel.addDeviceProduction(gudidDeviceModel.getProductions().get(0));
                autoPopulatedDeviceLiveData.setValue(new Resource<>(databaseDeviceModel, new Request(null, Request.Status.SUCCESS)));
            } else {
                // device that is in gudid and not in our database
                autoPopulatedDeviceLiveData.setValue(gudidResource);
            }

        }
        else if (databaseResource.getRequest().getStatus() == Request.Status.SUCCESS) {
            // device is in our database
            autoPopulatedDeviceLiveData.setValue(databaseSource.getValue());
        }
        autoPopulatedDeviceLiveData.removeSource(databaseSource);
        autoPopulatedDeviceLiveData.removeSource(gudidSource);
    }
}
