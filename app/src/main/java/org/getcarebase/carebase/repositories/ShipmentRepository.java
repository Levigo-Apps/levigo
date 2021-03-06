package org.getcarebase.carebase.repositories;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.Transaction;

import org.getcarebase.carebase.R;
import org.getcarebase.carebase.models.DeviceModel;
import org.getcarebase.carebase.models.DeviceProduction;
import org.getcarebase.carebase.models.Shipment;
import org.getcarebase.carebase.utils.Event;
import org.getcarebase.carebase.utils.FirestoreReferences;
import org.getcarebase.carebase.utils.Request;
import org.getcarebase.carebase.utils.Resource;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ShipmentRepository {
    private static final String TAG = ShipmentRepository.class.getName();

    private final String networkId;

    private final DocumentReference networkReference;
    private final CollectionReference shipmentReference;

    private final List<Shipment> shipments = new ArrayList<>();
    private DocumentSnapshot lastResult;
    private boolean reachedEnd = false;
    public ShipmentRepository(String networkId) {
        this.networkId = networkId;
        this.networkReference = FirestoreReferences.getNetworkReference(this.networkId);
        this.shipmentReference = FirestoreReferences.getShipmentReference(this.networkReference);
    }

    public LiveData<Resource<List<Shipment>>> getShipments(boolean onRefresh) {
        if (reachedEnd && !onRefresh) {
            Log.d(TAG,"Reached end");
            return new MutableLiveData<>(new Resource<>(null,new Request(null, Request.Status.SUCCESS)));
        }
        MutableLiveData<Resource<List<Shipment>>> shipmentLiveData = new MutableLiveData<>(new Resource<>(null,new Request(null, Request.Status.LOADING)));
        Query query;

        // clear shipment list on refresh
        if (onRefresh) {
            shipments.clear();
            lastResult = null;
            reachedEnd = false;
        }
        // load from beginning of shipment list if on initialization or on refresh
        if (lastResult == null) {
            query = shipmentReference.orderBy("date_time_shipped", Query.Direction.DESCENDING).limit(10);
        } else {
            query = shipmentReference.orderBy("date_time_shipped", Query.Direction.DESCENDING).limit(10).startAfter(lastResult);
        }
        // after getting results, add results to shipment list
        query.get().addOnCompleteListener( task -> {
            if (task.isSuccessful()) {
                QuerySnapshot snapshot = task.getResult();
                if (snapshot != null && !snapshot.isEmpty()) {
                    shipments.addAll(snapshot.toObjects(Shipment.class));
                    lastResult = task.getResult().getDocuments().get(task.getResult().size() - 1);
                    shipmentLiveData.setValue(new Resource<>(shipments, new Request(null, Request.Status.SUCCESS)));
                } else {
                    shipmentLiveData.setValue(new Resource<>(null, new Request(null, Request.Status.SUCCESS)));
                    reachedEnd = true;
                }
            } else {
                shipmentLiveData.setValue(new Resource<>(null,new Request(R.string.error_something_wrong,Request.Status.ERROR)));
            }
        });

        return shipmentLiveData;
    }

    public LiveData<Resource<Shipment>> getShipment(String shipmentId) {
        MutableLiveData<Resource<Shipment>> shipmentLiveData = new MutableLiveData<>();
        shipmentReference.document(shipmentId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                if (task.getResult().exists()) {
                    Shipment shipment = task.getResult().toObject(Shipment.class);
                    shipmentLiveData.setValue(new Resource<>(shipment,new Request(null, Request.Status.SUCCESS)));
                } else {
                    shipmentLiveData.setValue(new Resource<>(null,new Request(R.string.shipment_not_found, Request.Status.ERROR)));
                }

            } else {
                shipmentLiveData.setValue(new Resource<>(null,new Request(R.string.error_something_wrong, Request.Status.ERROR)));
            }
        });
        return shipmentLiveData;
    }

    public LiveData<Resource<Map<String,String>>> getShipmentTrackingNumbers() {
        MutableLiveData<Resource<Map<String,String>>> trackingLiveData = new MutableLiveData<>();
        shipmentReference.orderBy("date_time_shipped", Query.Direction.DESCENDING).limit(5).get().addOnCompleteListener(task -> {
           if (task.isSuccessful()) {
               QuerySnapshot snapshot = task.getResult();
               Map<String,String> trackingNumbers = new LinkedHashMap<>();
               if (snapshot != null && !snapshot.isEmpty()) {
                   for (QueryDocumentSnapshot documentSnapshot : task.getResult()) {
                       trackingNumbers.put(documentSnapshot.getId(),documentSnapshot.getString("destination_entity_id"));
                   }
               }
               trackingLiveData.setValue(new Resource<>(trackingNumbers,new Request(null, Request.Status.SUCCESS)));
           }
           else {
               trackingLiveData.setValue(new Resource<>(null, new Request(R.string.error_something_wrong, Request.Status.ERROR)));
           }
        });

        return trackingLiveData;
    }

    public LiveData<Request> receiveShipment(Shipment shipment) {
        MutableLiveData<Request> receiveShipmentRequest = new MutableLiveData<>(new Request(null,Request.Status.LOADING));
        Task<?> task = FirestoreReferences.getFirestoreReference().runTransaction(new Transaction.Function<Void>() {
            @Nullable
            @Override
            public Void apply(@NonNull Transaction transaction) throws FirebaseFirestoreException {
                // get all information about the device from source entity
                // single devices - productions are not grouped by model
                List<DeviceModel> sourceDevices = new ArrayList<>();
                DocumentReference sourceEntityReference = FirestoreReferences.getEntityReference(networkReference,shipment.getSourceEntityId());
                CollectionReference sourceInventoryReference = FirestoreReferences.getInventoryReference(sourceEntityReference);
                for (Map<String,String> item : shipment.getItems()) {
                    String di = item.get("di");
                    String udi = item.get("udi");

                    DeviceModel deviceModel = new DeviceModel();
                    DocumentReference deviceModelReference = sourceInventoryReference.document(di);
                    DocumentSnapshot deviceModelSnapshot = transaction.get(deviceModelReference);
                    if (deviceModelSnapshot.getData() != null ) {
                        deviceModel.fromMap(deviceModelSnapshot.getData());
                    } else {
                        throw new FirebaseFirestoreException("Device Model does not exist", FirebaseFirestoreException.Code.ABORTED);
                    }

                    DeviceProduction deviceProduction = new DeviceProduction();
                    DocumentReference deviceProductionReference = deviceModelReference.collection("udis").document(udi);
                    DocumentSnapshot deviceProductionSnapshot = transaction.get(deviceProductionReference);
                    if (deviceProductionSnapshot.getData() != null) {
                        deviceProduction.fromMap(deviceProductionSnapshot.getData());
                    } else {
                        throw new FirebaseFirestoreException("Device Production does not exist", FirebaseFirestoreException.Code.ABORTED);
                    }

                    deviceModel.addDeviceProduction(deviceProduction);
                    sourceDevices.add(deviceModel);
                }

                DocumentReference destinationEntityReference = FirestoreReferences.getEntityReference(networkReference,shipment.getDestinationEntityId());
                CollectionReference destinationInventoryReference = FirestoreReferences.getInventoryReference(destinationEntityReference);

                // get all current information about the device being shipped in the destination entity
                // if it exists
                Map<String,DeviceModel> destinationDeviceModelMap = new HashMap<>();
                Map<String,DeviceProduction> destinationDeviceProductionMap = new HashMap<>();
                for (Map<String,String> item : shipment.getItems()) {
                    String di = item.get("di");
                    String udi = item.get("udi");

                    DocumentReference deviceModelReference = destinationInventoryReference.document(di);
                    DocumentSnapshot deviceModelSnapshot = transaction.get(deviceModelReference);
                    if (!deviceModelSnapshot.exists()) {
                        continue;
                    }
                    DeviceModel deviceModel = new DeviceModel(Objects.requireNonNull(deviceModelSnapshot.getData()));
                    destinationDeviceModelMap.put(di,deviceModel);

                    DocumentReference deviceProductionReference = deviceModelReference.collection("udis").document(udi);
                    DocumentSnapshot deviceProductionSnapshot = transaction.get(deviceProductionReference);
                    if (!deviceProductionSnapshot.exists()) {
                        continue;
                    }
                    DeviceProduction deviceProduction = new DeviceProduction(deviceProductionSnapshot.getData());
                    destinationDeviceProductionMap.put(udi,deviceProduction);
                }

                // save all devices
                // if the device exists in the destination entity increment the current quantity
                for (int i = 0; i < shipment.getItems().size(); i++) {
                    Map<String,String> shipmentItem = shipment.getItems().get(i);
                    String di = shipmentItem.get("di");
                    String udi = shipmentItem.get("udi");
                    int units = Integer.parseInt(shipmentItem.get("quantity"));

                    DeviceModel sourceDeviceModel = sourceDevices.get(i);
                    if (destinationDeviceModelMap.containsKey(di)) {
                        // increment quantity
                        DeviceModel destinationDeviceModel = Objects.requireNonNull(destinationDeviceModelMap.get(di));
                        sourceDeviceModel.setQuantity(destinationDeviceModel.getQuantity() + units);
                    } else {
                        sourceDeviceModel.setQuantity(units);
                    }

                    // add equipment type to entities device_types array if it does not exist
                    transaction.update(destinationEntityReference,"device_types", FieldValue.arrayUnion(sourceDeviceModel.getEquipmentType()));
                    DocumentReference deviceTypeRef = destinationEntityReference.collection("device_types").document(sourceDeviceModel.getEquipmentType());
                    Map<String,Object> deviceType = new HashMap<>();
                    deviceType.put("tags",FieldValue.arrayUnion(sourceDeviceModel.getTags().toArray()));
                    transaction.set(deviceTypeRef, deviceType, SetOptions.merge());

                    DeviceProduction sourceDeviceProduction = sourceDeviceModel.getProductions().get(0);
                    if (destinationDeviceProductionMap.containsKey(udi)) {
                        // increment quantity
                        DeviceProduction destinationDeviceProduction = Objects.requireNonNull(destinationDeviceProductionMap.get(udi));
                        sourceDeviceProduction.setQuantity(destinationDeviceProduction.getQuantity() + units);
                    } else {
                        sourceDeviceProduction.setQuantity(units);
                    }

                    DocumentReference deviceModelReference = destinationInventoryReference.document(di);
                    transaction.set(deviceModelReference,sourceDeviceModel.toMap());

                    DocumentReference deviceProductionReference = deviceModelReference.collection("udis").document(udi);
                    transaction.set(deviceProductionReference,sourceDeviceProduction.toMap());
                }

                DocumentReference documentReference = shipmentReference.document(shipment.getTrackingNumber());
                documentReference.update("date_time_received", new Date());
                return null;
            }
        });

        task.addOnCompleteListener( t -> {
            if (t.isSuccessful()) {
                receiveShipmentRequest.setValue(new Request(R.string.new_shipment_devices_saved, Request.Status.SUCCESS));
            } else {
                receiveShipmentRequest.setValue(new Request(R.string.error_something_wrong, Request.Status.ERROR));
                Log.d(TAG,"Receive shipment failed: ", t.getException());
            }
        });
        return receiveShipmentRequest;
    }

    public LiveData<Event<Request>> saveShipment(Shipment shipment) {
        MutableLiveData<Event<Request>> saveShipmentRequest = new MutableLiveData<>();

        List<Map<String, String>> shippedItemsArray = new ArrayList<>();
        Map<String, String> shippedItem = new HashMap<>();
        shippedItem.put("di", shipment.getDi());
        shippedItem.put("udi", shipment.getUdi());
        shippedItem.put("name", shipment.getDeviceName());
        shippedItem.put("quantity", String.valueOf(shipment.getQuantity()));
        shipment.setShippedTime(new Date());
        shipment.setItems(shippedItemsArray);

        Task<?> saveShipmentTask = FirestoreReferences.getFirestoreReference().runTransaction(new Transaction.Function<Void>() {
            @Nullable
            @Override
            public Void apply(@NonNull Transaction transaction) throws FirebaseFirestoreException {
                DocumentReference shipmentDocumentReference;
                if (shipment.getTrackingNumber().contentEquals("Get New Tracking Number")) {
                    shipmentDocumentReference = shipmentReference.document();
                    shippedItemsArray.add(shippedItem);
                } else {
                    shipmentDocumentReference = shipmentReference.document(shipment.getTrackingNumber());
                    DocumentSnapshot shipmentDocumentSnapshot = transaction.get(shipmentDocumentReference);
                    List<Map<String,String>> oldShippedItemsArray = (List<Map<String, String>>) shipmentDocumentSnapshot.get("items");
                    shippedItemsArray.addAll(oldShippedItemsArray);

                    // check if item already exists
                    boolean exists = false;
                    for (Map<String,String> item : shippedItemsArray) {
                        String di = item.getOrDefault("di","");
                        String udi = item.getOrDefault("udi","");
                        if (shipment.getDi().equals(di) && shipment.getUdi().equals(udi)) {
                            // increment quantities
                            int currentQuantity = Integer.parseInt(item.get("quantity"));
                            item.put("quantity",Integer.toString(currentQuantity + shipment.getQuantity()));
                            exists = true;
                            break;
                        }
                    }
                    if (!exists) {
                        shippedItemsArray.add(shippedItem);
                    }
                }

                transaction.set(shipmentDocumentReference,shipment);

                // update device model and production quantities
                DocumentReference currentHospitalReference = FirestoreReferences.getEntityReference(
                        networkReference, shipment.getSourceEntityId());
                CollectionReference inventoryReference = FirestoreReferences.getInventoryReference(currentHospitalReference);
                DocumentReference deviceModelReference = inventoryReference.document(shipment.getDi());
                DocumentReference deviceProductionReference = deviceModelReference.collection("udis").document(shipment.getUdi());
                transaction.update(deviceModelReference,"quantity", FieldValue.increment(-1*shipment.getQuantity()));
                transaction.update(deviceProductionReference,"quantity",FieldValue.increment(-1*shipment.getQuantity()));
                return null;
            }
        });

        saveShipmentTask.addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                saveShipmentRequest.setValue(new Event<>(new Request(null, Request.Status.SUCCESS)));
            }
            else {
                saveShipmentRequest.setValue(new Event<>(new Request(R.string.error_something_wrong, Request.Status.ERROR)));
            }
        });

        return saveShipmentRequest;
    }
}