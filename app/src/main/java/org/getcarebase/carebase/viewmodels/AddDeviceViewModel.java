package org.getcarebase.carebase.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import org.getcarebase.carebase.R;
import org.getcarebase.carebase.models.DeviceModel;
import org.getcarebase.carebase.models.Entity;
import org.getcarebase.carebase.models.Shipment;
import org.getcarebase.carebase.models.User;
import org.getcarebase.carebase.repositories.DeviceRepository;
import org.getcarebase.carebase.repositories.EntityRepository;
import org.getcarebase.carebase.repositories.FirebaseAuthRepository;
import org.getcarebase.carebase.repositories.ShipmentRepository;
import org.getcarebase.carebase.utils.Event;
import org.getcarebase.carebase.utils.Request;
import org.getcarebase.carebase.utils.Resource;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class AddDeviceViewModel extends ViewModel {
    private final FirebaseAuthRepository authRepository = new FirebaseAuthRepository();
    private EntityRepository entityRepository;
    private DeviceRepository deviceRepository;
    private ShipmentRepository shipmentRepository;

    private LiveData<Resource<User>> userLiveData;
    public MutableLiveData<String> uniqueDeviceIdentifierLiveData = new MutableLiveData<>();
    private LiveData<Boolean> editableLiveData;
    private final MediatorLiveData<Resource<DeviceModel>> deviceModelLiveData = new MediatorLiveData<>();
    private final MutableLiveData<Map<String,Integer>> errorsLiveData = new MutableLiveData<>();
    private final MutableLiveData<DeviceModel> saveDeviceLiveData = new MutableLiveData<>();
    private final MutableLiveData<Shipment> saveShipment = new MutableLiveData<>(null);
    public final LiveData<Event<Request>> saveRequestLivedata = Transformations.switchMap(saveDeviceLiveData,device -> {
        LiveData<Event<Request>> saveDeviceRequestLiveData = deviceRepository.saveDevice(device);
        if (saveShipment.getValue() != null) {
            return Transformations.switchMap(saveDeviceRequestLiveData, saveDeviceRequest -> {
                Request request = saveDeviceRequest.getContentIfNotHandled();
                if (request.getStatus() == Request.Status.SUCCESS) {
                    return shipmentRepository.saveShipment(saveShipment.getValue());
                }
                return new MutableLiveData<>(new Event<>(request));
            });
        }
        return saveDeviceRequestLiveData;
    });

    public LiveData<Resource<User>> getUserLiveData() {
        if (userLiveData == null) {
            userLiveData = authRepository.getUser();
        }
        return userLiveData;
    }

    public LiveData<Resource<DeviceModel>> getDeviceModelLiveData() {
        return deviceModelLiveData;
    }

    public LiveData<Resource<Map<String, String>>> getSitesLiveData() {
        // remove user's entity
        return Transformations.map(entityRepository.getSiteOptions(),sitesResource -> {
            if (sitesResource.getRequest().getStatus() == Request.Status.SUCCESS) {
                String entityId = Objects.requireNonNull(userLiveData.getValue()).getData().getEntityId();
                sitesResource.getData().remove(entityId);
            }
            return sitesResource;
        });
    }

    public LiveData<Resource<Map<String,String>>> getShipmentTrackingNumbersLiveData() {
        return shipmentRepository.getShipmentTrackingNumbers();
    }

    public MutableLiveData<Map<String, Integer>> getErrorsLiveData() {
        return errorsLiveData;
    }

    public LiveData<Boolean> isEditable() {
        if (editableLiveData == null) {
            LiveData<Resource<Entity>> entityLiveData = entityRepository.getEntity(Objects.requireNonNull(userLiveData.getValue()).getData());
            editableLiveData = Transformations.map(entityLiveData, entityResource -> {
                if (entityResource.getRequest().getStatus() == Request.Status.SUCCESS) {
                    Entity entity = entityResource.getData();
                    return !entity.getType().equals("hospital");
                } else {
                    return false;
                }
            });
        }
        return editableLiveData;
    }

    public LiveData<Boolean> isShipment() {
        return Transformations.map(saveShipment, Objects::nonNull);
    }

    public void setupRepositories(String networkId, String entityId) {
        deviceRepository = new DeviceRepository(networkId,entityId);
        entityRepository = new EntityRepository(networkId);
        shipmentRepository = new ShipmentRepository(networkId);
    }

    public void onNameChanged(CharSequence name) {
        Objects.requireNonNull(deviceModelLiveData.getValue()).getData().setName(name.toString());
    }

    public void onExpirationDateChanged(CharSequence expirationDate) {
        Objects.requireNonNull(deviceModelLiveData.getValue()).getData().getProductions().get(0).setExpirationDate(expirationDate.toString());
    }

    public void onDescriptionChanged(CharSequence description) {
        Objects.requireNonNull(deviceModelLiveData.getValue()).getData().setDescription(description.toString());
    }

    public void onLotNumberChanged(CharSequence lotNumber) {
        Objects.requireNonNull(deviceModelLiveData.getValue()).getData().getProductions().get(0).setLotNumber(lotNumber.toString());
    }

    public void onReferenceNumberChanged(CharSequence referenceNumber) {
        Objects.requireNonNull(deviceModelLiveData.getValue()).getData().getProductions().get(0).setLotNumber(referenceNumber.toString());
    }

    public void onQuantityChanged(CharSequence quantity) {
        DeviceModel deviceModel = Objects.requireNonNull(deviceModelLiveData.getValue()).getData();
        if (quantity.toString().equals("")) {
            deviceModel.getProductions().get(0).setQuantity(0);
        } else {
            deviceModel.getProductions().get(0).setQuantity((Integer.parseInt(quantity.toString())));
        }
    }

    public void toggleShipment(boolean isShipment) {
        if (isShipment) {
            Shipment shipment = new Shipment();
            shipment.setUdi(uniqueDeviceIdentifierLiveData.getValue());
            shipment.setDi(Objects.requireNonNull(deviceModelLiveData.getValue()).getData().getDeviceIdentifier());
            shipment.setSourceEntityId(Objects.requireNonNull(userLiveData.getValue()).getData().getEntityId());
            shipment.setSourceEntityName(userLiveData.getValue().getData().getEntityName());
            saveShipment.setValue(shipment);
        } else {
            saveShipment.setValue(null);
        }
    }

    public void onShipmentTrackerNumberChanged(String trackingNumber) {
        Shipment shipment = Objects.requireNonNull(saveShipment.getValue());
        shipment.setTrackingNumber(trackingNumber);
    }

    public void onShipmentDestinationChange(String destinationId, String destinationName) {
        if (destinationId == null && destinationName != null) {
            Map<String,Integer> invalidDestination = new HashMap<>();
            invalidDestination.put("all",R.string.error_something_wrong);
            errorsLiveData.setValue(invalidDestination);
        }
        Shipment shipment = Objects.requireNonNull(saveShipment.getValue());
        shipment.setDestinationEntityId(destinationId);
        shipment.setDestinationEntityName(destinationName);
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

    /**
     * Initiates save process by handling validation and then sends of request through livedata
     * transformation
     */
    public void onSave(Map<String,String> specifications) {
        errorsLiveData.setValue(Objects.requireNonNull(deviceModelLiveData.getValue()).getData().isValid());
        DeviceModel device = deviceModelLiveData.getValue().getData();
        device.setSpecifications(specifications);
        saveDeviceLiveData.setValue(device);
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
