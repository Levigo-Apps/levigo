package org.getcarebase.carebase.repositories;

import android.util.Log;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.getcarebase.carebase.R;
import org.getcarebase.carebase.models.Entity;
import org.getcarebase.carebase.models.Shipment;
import org.getcarebase.carebase.models.User;
import org.getcarebase.carebase.utils.FirestoreReferences;
import org.getcarebase.carebase.utils.Request;
import org.getcarebase.carebase.utils.Resource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

public class ShipmentRepository {
    private static final String TAG = ShipmentRepository.class.getName();

    private final String networkId;

    private final DocumentReference networkReference;
    private final CollectionReference shipmentReference;

    public ShipmentRepository(String networkId) {
        this.networkId = networkId;
        networkReference = FirestoreReferences.getNetworkReference(networkId);
        shipmentReference = FirestoreReferences.getShipmentReference(networkReference);
    }

    public LiveData<Resource<Shipment>> getShipment(User user) {
        DocumentReference tempNetworkReference = FirestoreReferences.getNetworkReference(user.getNetworkId());
        CollectionReference tempShipmentReference = FirestoreReferences.getShipmentReference(tempNetworkReference);
        MutableLiveData<Resource<Shipment>> shipmentLiveData = new MutableLiveData<>();
        tempShipmentReference.get().addOnCompleteListener( task -> {
            if (task.isSuccessful()) {
                QuerySnapshot snapshot = task.getResult();
                if (snapshot != null && !snapshot.isEmpty()) {
                    // get data and convert to Shipment class
                } else {
                    shipmentLiveData.setValue(new Resource<>(null,new Request(R.string.error_something_wrong,Request.Status.ERROR)));
                }
            } else {
                shipmentLiveData.setValue(new Resource<>(null,new Request(R.string.error_something_wrong,Request.Status.ERROR)));
            }
        });
        return shipmentLiveData;
    }

    public LiveData<Resource<String[]>> getShipmentTrackingNumbers() {
        MutableLiveData<Resource<String[]>> trackingLiveData = new MutableLiveData<>();
        shipmentReference.get().addOnCompleteListener(task -> {
           if (task.isSuccessful()) {
               QuerySnapshot snapshot = task.getResult();
               if (snapshot != null && !snapshot.isEmpty()) {
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

    // TODO new schema refactor
    // Assumes shipment already has id
    public LiveData<Request> saveShipment(Shipment shipment) {
        MutableLiveData<Request> saveShipmentRequest = new MutableLiveData<>();
        List<Task<?>> tasks = new ArrayList<>();

        DocumentReference shipmentDocument = shipmentReference.document(shipment.getId());
        shipmentDocument.addSnapshotListener((documentSnapshot, e) -> {
            if (documentSnapshot != null && documentSnapshot.exists()) {
                // add device in shipment object to existing document
                tasks.add(shipmentDocument.update("quantity", shipment.getQuantity()));
            } else {
                // add new shipment to database
                tasks.add(shipmentReference.add(shipment));
            }
        });
//
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
