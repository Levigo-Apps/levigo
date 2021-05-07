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
import org.getcarebase.carebase.models.Procedure;
import org.getcarebase.carebase.models.Shipment;
import org.getcarebase.carebase.models.User;
import org.getcarebase.carebase.repositories.DeviceRepository;
import org.getcarebase.carebase.repositories.FirebaseAuthRepository;
import org.getcarebase.carebase.repositories.EntityRepository;
import org.getcarebase.carebase.repositories.PendingDeviceRepository;
import org.getcarebase.carebase.repositories.ShipmentRepository;
import org.getcarebase.carebase.utils.Event;
import org.getcarebase.carebase.utils.Request;
import org.getcarebase.carebase.utils.Resource;
import org.getcarebase.carebase.utils.SingleEventMediatorLiveData;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public class DeviceViewModel extends ViewModel {
    private DeviceRepository deviceRepository;
    private PendingDeviceRepository pendingDeviceRepository;
    private EntityRepository entityRepository;
    private ShipmentRepository shipmentRepository;
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

    private final SingleEventMediatorLiveData<List<Shipment>> shipmentsLiveData = new SingleEventMediatorLiveData<>();

    private final MutableLiveData<Shipment> saveShipmentLiveData = new MutableLiveData<>();
    private final LiveData<Request> saveShipmentRequestLiveData =
            Transformations.switchMap(saveShipmentLiveData, shipment -> shipmentRepository.saveShipment(shipment));

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
        deviceRepository = new DeviceRepository(user.getNetworkId(), user.getEntityId());
        pendingDeviceRepository = new PendingDeviceRepository(user.getNetworkId(),user.getEntityId());
    }

    // TODO new schema refactor
    public void setupEntityRepository() {
        User user = Objects.requireNonNull(userLiveData.getValue()).getData();
        entityRepository = new EntityRepository(user.getNetworkId());
        shipmentRepository = new ShipmentRepository(user.getNetworkId());
    }

    public Map<String, List<String>> getDeviceTypes() {
        return deviceRepository.getDeviceTypeOptions();
    }

    public LiveData<Resource<Map<String, String>>> getSitesLiveData() {
        return entityRepository.getSiteOptions();
    }

    public LiveData<Resource<Map<String,String>>> getShipmentTrackingNumbersLiveData() {
        return shipmentRepository.getShipmentTrackingNumbers();
    }

    public LiveData<Resource<String[]>> getPhysicalLocationsLiveData() {
        return deviceRepository.getPhysicalLocationOptions();
    }

//    public LiveData<Resource<List<Shipment>>> getShipmentsLiveData() {
//        return shipmentRepository.getShipments();
//    }

    public void loadShipments(boolean onRefresh) {
        shipmentsLiveData.addSource(shipmentRepository.getShipments(onRefresh));
    }

    public LiveData<Resource<List<Shipment>>> getShipmentsLiveData() {
        return shipmentsLiveData.getLiveData();
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

    public LiveData<Request> getSaveShipmentRequestLiveData() {
        return saveShipmentRequestLiveData;
    }

    public void saveShipment(Shipment shipment) {
        saveShipmentLiveData.setValue(shipment);
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
        DeviceSourceObserver deviceSourceObserver = new DeviceSourceObserver(inventorySource,shippedSource,gudidSource);
        autoPopulatedDeviceLiveData.addSource(inventorySource,deviceSourceObserver);
        autoPopulatedDeviceLiveData.addSource(shippedSource,deviceSourceObserver);
        autoPopulatedDeviceLiveData.addSource(gudidSource,deviceSourceObserver);
    }

    private class DeviceSourceObserver implements Observer<Resource<DeviceModel>> {
        private final LiveData<Resource<DeviceModel>> inventorySource;
        private final LiveData<Resource<DeviceModel>> shippedSource;
        private final LiveData<Resource<DeviceModel>> gudidSource;
        public DeviceSourceObserver(LiveData<Resource<DeviceModel>> inventorySource, LiveData<Resource<DeviceModel>> shippedSource, LiveData<Resource<DeviceModel>> gudidSource) {
            this.inventorySource = inventorySource;
            this.shippedSource = shippedSource;
            this.gudidSource = gudidSource;
        }
        @Override
        public void onChanged(Resource<DeviceModel> deviceModelResource) {
            if (deviceModelResource.getRequest().getStatus() == Request.Status.SUCCESS
                || deviceModelResource.getRequest().getStatus() == Request.Status.ERROR) {
                mediateDataSource(inventorySource,shippedSource,gudidSource);
            }
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
            autoPopulatedDeviceLiveData.setValue(gudidResource);
        }
        else if (inventoryResource.getRequest().getStatus() == Request.Status.ERROR && inventoryResource.getRequest().getResourceString() == R.string.error_something_wrong) {
            autoPopulatedDeviceLiveData.setValue(inventoryResource);
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
                autoPopulatedDeviceLiveData.setValue(new Resource<>(databaseDeviceModel, new Request(null, Request.Status.SUCCESS)));
            } else {
                // device that is in gudid and not in our database
                autoPopulatedDeviceLiveData.setValue(gudidResource);
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
                    autoPopulatedDeviceLiveData.setValue(shippedResource);
                } else {
                    inventoryResource.getData().setShipment(shippedResource.getData().getShipment());
                    autoPopulatedDeviceLiveData.setValue(inventoryResource);
                }
            } else {
                autoPopulatedDeviceLiveData.setValue(inventoryResource);
            }
        }

        // stop listening to these sources
        autoPopulatedDeviceLiveData.removeSource(inventorySource);
        autoPopulatedDeviceLiveData.removeSource(shippedSource);
        autoPopulatedDeviceLiveData.removeSource(gudidSource);
    }
}
