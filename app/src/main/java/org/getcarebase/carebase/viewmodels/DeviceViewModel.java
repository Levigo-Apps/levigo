package org.getcarebase.carebase.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import org.getcarebase.carebase.R;
import org.getcarebase.carebase.models.DeviceModel;
import org.getcarebase.carebase.models.PendingDevice;
import org.getcarebase.carebase.models.User;
import org.getcarebase.carebase.repositories.DeviceRepository;
import org.getcarebase.carebase.repositories.FirebaseAuthRepository;
import org.getcarebase.carebase.repositories.PendingDeviceRepository;
import org.getcarebase.carebase.utils.DeviceSourceObserver;
import org.getcarebase.carebase.utils.Event;
import org.getcarebase.carebase.utils.Request;
import org.getcarebase.carebase.utils.Resource;

import java.util.List;
import java.util.Objects;

public class DeviceViewModel extends ViewModel {
    private DeviceRepository deviceRepository;
    private PendingDeviceRepository pendingDeviceRepository;
    private final FirebaseAuthRepository authRepository;

    private LiveData<Resource<User>> userLiveData;

    private final MediatorLiveData<Resource<DeviceModel>> autoPopulatedDeviceLiveData = new MediatorLiveData<>();

    private LiveData<Resource<DeviceModel>> deviceInFirebaseLiveData;

    private final MutableLiveData<String> savePhysicalLocationLiveData = new MutableLiveData<>();

    private final LiveData<Request> savePhysicalLocationRequestLiveData = Transformations.switchMap(savePhysicalLocationLiveData, physicalLocation -> deviceRepository.savePhysicalLocation(physicalLocation));

    private final MutableLiveData<String> pendingDeviceIdLiveData = new MutableLiveData<>();
    private final LiveData<Resource<PendingDevice>> pendingDeviceLiveData = Transformations.switchMap(pendingDeviceIdLiveData,pendingDeviceId -> pendingDeviceRepository.getPendingDevice(pendingDeviceId));

    // when a user tries to save a device this live data will be updated
    private final MutableLiveData<DeviceModel> saveDeviceLiveData = new MutableLiveData<>();
    // requests to save a device will be sent to this live data
    private final LiveData<Event<Request>> saveDeviceRequestLiveData = Transformations.switchMap(saveDeviceLiveData, deviceModel -> {
        if (pendingDeviceIdLiveData.getValue() != null) {
            pendingDeviceRepository.removePendingDevice(pendingDeviceIdLiveData.getValue());
        }
        return deviceRepository.saveDevice(deviceModel);
    });

    public DeviceViewModel() {
        authRepository = new FirebaseAuthRepository();
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        deviceRepository.destroy();
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
        pendingDeviceRepository = new PendingDeviceRepository(user.getNetworkId(),user.getHospitalId());
    }

    public LiveData<Resource<List<String>>> getDeviceTypesLiveData() {
        return deviceRepository.getDeviceTypeOptions();
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

    public LiveData<Request> getSavePhysicalLocationRequestLiveData() {
        return savePhysicalLocationRequestLiveData;
    }

    public LiveData<Event<Request>> getSaveDeviceRequestLiveData() {
        return saveDeviceRequestLiveData;
    }

    public void saveDevice(DeviceModel deviceModel) {
        saveDeviceLiveData.setValue(deviceModel);
    }

    public LiveData<Resource<DeviceModel>> getAutoPopulatedDeviceLiveData() {
        return autoPopulatedDeviceLiveData;
    }

    public LiveData<Resource<DeviceModel>> getDeviceInFirebaseLiveData() {
        return deviceInFirebaseLiveData;
    }

    public void updateDeviceInFirebaseLiveData(String di, String udi) {
        deviceInFirebaseLiveData = deviceRepository.getDeviceFromFirebase(di, udi);
    }

    public void setPendingDeviceIdLiveData(String pendingDeviceId) {
       pendingDeviceIdLiveData.setValue(pendingDeviceId);
    }

    public LiveData<Resource<PendingDevice>> getPendingDeviceLiveData() {
        return pendingDeviceLiveData;
    }

    public void autoPopulatedScannedBarcode(String barcode) {
        autoPopulatedDeviceLiveData.setValue(new Resource<>(null,new Request(null,Request.Status.LOADING)));
        List<LiveData<Resource<DeviceModel>>> databaseSources = deviceRepository.autoPopulateFromDatabaseAndShipment(barcode);
        LiveData<Resource<DeviceModel>> inventorySource = databaseSources.get(0);
        LiveData<Resource<DeviceModel>> shippedSource = databaseSources.get(1);
        LiveData<Resource<DeviceModel>> gudidSource = deviceRepository.autoPopulateFromGUDID(barcode);
        DeviceSourceObserver deviceSourceObserver = new DeviceSourceObserver(inventorySource,shippedSource,gudidSource,autoPopulatedDeviceLiveData);
        autoPopulatedDeviceLiveData.addSource(inventorySource,deviceSourceObserver);
        autoPopulatedDeviceLiveData.addSource(shippedSource,deviceSourceObserver);
        autoPopulatedDeviceLiveData.addSource(gudidSource,deviceSourceObserver);
    }

}
