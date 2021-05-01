package org.getcarebase.carebase.repositories;

import android.util.Log;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.getcarebase.carebase.R;
import org.getcarebase.carebase.models.Shipment;
import org.getcarebase.carebase.models.User;
import org.getcarebase.carebase.utils.FirestoreReferences;
import org.getcarebase.carebase.utils.Request;
import org.getcarebase.carebase.utils.Resource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

public class ShipmentRepository {
    private static final String TAG = ShipmentRepository.class.getName();

    private final String networkId;

    private final DocumentReference networkReference;
    private final CollectionReference shipmentReference;

    public ShipmentRepository(String networkId) {
        this.networkId = networkId;
        this.networkReference = FirestoreReferences.getNetworkReference(this.networkId);
        this.shipmentReference = FirestoreReferences.getShipmentReference(this.networkReference);
    }

    public LiveData<Resource<List<Shipment>>> getShipments() {
        MutableLiveData<Resource<List<Shipment>>> shipmentLiveData = new MutableLiveData<>();
        shipmentReference.get().addOnCompleteListener( task -> {
            if (task.isSuccessful()) {
                QuerySnapshot snapshot = task.getResult();
                if (snapshot != null && !snapshot.isEmpty()) {
                    List<Shipment> shipments = new ArrayList<>(snapshot.size());
                    for (int i = 0; i < snapshot.size(); i++) {
                        Shipment tempShipment = new Shipment();
                        DocumentSnapshot d = snapshot.getDocuments().get(i);

                        // Set values of shipment object
                        tempShipment.setTrackingNumber(d.getId());
                        tempShipment.setSourceEntityId(String.valueOf(d.get("source_entity_id")));
                        tempShipment.setDestinationEntityId(String.valueOf(d.get("destination_entity_id")));
                        tempShipment.setShippedTime(String.valueOf(d.get("date_time_shipped")));
                        tempShipment.setItems((List<Map<String, String>>) d.get("items"));

                        // Add shipment to shipment array
                        shipments.add(i, tempShipment);
                    }
                    shipmentLiveData.setValue(new Resource<>(shipments,new Request(null,Request.Status.SUCCESS)));
                } else {
                    shipmentLiveData.setValue(new Resource<>(null,new Request(R.string.error_something_wrong,Request.Status.ERROR)));
                }
            } else {
                shipmentLiveData.setValue(new Resource<>(null,new Request(R.string.error_something_wrong,Request.Status.ERROR)));
            }
        });

        return shipmentLiveData;
    }

//    CollectionReference itemsReference = shipmentReference.document(d.getId()).collection("items");
//                        itemsReference.get().addOnCompleteListener(task1 -> {
//        if (task1.isSuccessful()) {
//            QuerySnapshot snapshot1 = task1.getResult();
//            if (snapshot1 != null && !snapshot1.isEmpty()) {
//                List<Map<String, String>> ship_items = new ArrayList<>(snapshot1.size());
//                for (int i1 = 0; i1 < snapshot1.size(); i1++) {
//                    Map<String, String> ship_item = new HashMap<>();
//                    DocumentSnapshot d1 = snapshot1.getDocuments().get(i1);
//                    ship_item.put("di", String.valueOf(d1.get("di")));
//                    ship_item.put("udi", String.valueOf(d1.get("udi")));
//                    ship_item.put("quantity", String.valueOf(d1.get("quantity")));
//                    ship_items.add(i1, ship_item);
//                }
//                tempShipment.setItems(ship_items);
//            } else {
//                shipmentLiveData.setValue(new Resource<>(null,new Request(R.string.error_something_wrong,Request.Status.ERROR)));
//            }
//        } else {
//            Log.d(TAG, "didn't work");
//            shipmentLiveData.setValue(new Resource<>(null,new Request(R.string.error_something_wrong,Request.Status.ERROR)));
//        }
//    });

    public LiveData<Resource<String[]>> getShipmentTrackingNumbers() {
        MutableLiveData<Resource<String[]>> trackingLiveData = new MutableLiveData<>();
        shipmentReference.get().addOnCompleteListener(task -> {
           if (task.isSuccessful()) {
               QuerySnapshot snapshot = task.getResult();
               if (snapshot != null) {
                   String[] trackingNumbers = new String[snapshot.size()+1];
                   int tracking_index = 0;
                   for (DocumentSnapshot d: snapshot.getDocuments()) {
                       trackingNumbers[tracking_index] = d.getId();
                       tracking_index++;
                   }
                   trackingNumbers[tracking_index] = "Get New Tracking Number";
                   trackingLiveData.setValue(new Resource<>(trackingNumbers,new Request(null, Request.Status.SUCCESS)));
               }
               else {
                   Log.d(TAG, "No tracking numbers were found");
                   trackingLiveData.setValue(new Resource<>(null, new Request(R.string.error_something_wrong, Request.Status.ERROR)));
               }
           }
           else {
               trackingLiveData.setValue(new Resource<>(null, new Request(R.string.error_something_wrong, Request.Status.ERROR)));
           }
        });

        return trackingLiveData;
    }

    public LiveData<Resource<Map<String, String>>> getShipmentDestinationSites() {
        MutableLiveData<Resource<Map<String, String>>> destLiveData = new MutableLiveData<>();
        shipmentReference.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                QuerySnapshot snapshot = task.getResult();
                if (snapshot != null) {
                    Map<String, String> destSites = new HashMap<>();
                    for (DocumentSnapshot d: snapshot.getDocuments())
                        destSites.put(d.getId(), (String) d.get("destination_entity_id"));
                    destLiveData.setValue(new Resource<>(destSites,new Request(null, Request.Status.SUCCESS)));
                }
                else {
                    Log.d(TAG, "No destination ids were found");
                    destLiveData.setValue(new Resource<>(null, new Request(R.string.error_something_wrong, Request.Status.ERROR)));
                }
            }
            else {
                destLiveData.setValue(new Resource<>(null, new Request(R.string.error_something_wrong, Request.Status.ERROR)));
            }
        });

        return destLiveData;
    }

    // Assumes shipment already has id/tracking number
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