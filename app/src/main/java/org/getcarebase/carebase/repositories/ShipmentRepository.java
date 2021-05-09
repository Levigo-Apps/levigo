package org.getcarebase.carebase.repositories;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.getcarebase.carebase.R;
import org.getcarebase.carebase.models.Shipment;
import org.getcarebase.carebase.utils.FirestoreReferences;
import org.getcarebase.carebase.utils.Request;
import org.getcarebase.carebase.utils.Resource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

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
                Shipment shipment = task.getResult().toObject(Shipment.class);
                shipmentLiveData.setValue(new Resource<>(shipment,new Request(null, Request.Status.SUCCESS)));
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
               Map<String,String> trackingNumbers = new TreeMap<>();
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

    public LiveData<Request> saveShipment(Shipment shipment) {
        MutableLiveData<Request> saveShipmentRequest = new MutableLiveData<>();
        List<Task<?>> tasks = new ArrayList<>();

        List<Map<String, String>> ship_items = new ArrayList<>();
        Map<String, String> ship_item = new HashMap<>();
        ship_item.put("di", shipment.getDi());
        ship_item.put("udi", shipment.getUdi());
        ship_item.put("name", shipment.getDeviceName());
        ship_item.put("quantity", String.valueOf(shipment.getQuantity()));

        if (shipment.getTrackingNumber().contentEquals("temptrackingnumber")) {
            ship_items.add(ship_item);
            shipment.setItems(ship_items);

            DocumentReference newShipmentDocument = shipmentReference.document();
            tasks.add(newShipmentDocument.set(shipment));
        } else {
            // TODO make this atomic
            DocumentReference oldShipmentDocument = shipmentReference.document(shipment.getTrackingNumber());
            oldShipmentDocument.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DocumentSnapshot documentSnapshot = task.getResult();
                    if (documentSnapshot != null && documentSnapshot.exists()) {
                        // Add device in shipment object to existing document
                        ship_items.clear();
                        ship_items.addAll((List<Map<String, String>>) documentSnapshot.get("items"));
                        ship_items.add(ship_item);
                        shipment.setItems(ship_items);
                        tasks.add(oldShipmentDocument.set(shipment));
                    }
                }
            });
        }

//        // update device model and production quantities
//        DocumentReference currentHospitalReference = FirestoreReferences.getHospitalReference(
//                networkReference, shipment.getSourceHospitalId());
//        CollectionReference inventoryReference = FirestoreReferences.getInventoryReference(currentHospitalReference);
//        DocumentReference deviceModelReference = inventoryReference.document(shipment.getDi());
//        DocumentReference deviceProductionReference = deviceModelReference.collection("udis").document(shipment.getUdi());
//        tasks.add(deviceModelReference.update("quantity", FieldValue.increment(-1*shipment.getShippedQuantity())));
//        tasks.add(deviceProductionReference.update("quantity",FieldValue.increment(-1*shipment.getShippedQuantity())));
//
        Tasks.whenAllComplete(tasks).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                saveShipmentRequest.setValue(new Request(null, Request.Status.SUCCESS));
            }
            else {
                saveShipmentRequest.setValue(new Request(null, Request.Status.ERROR));
            }
        });
        //saveShipmentRequest.setValue(new Request(null, Request.Status.ERROR));
        return saveShipmentRequest;
    }
}