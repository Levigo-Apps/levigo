package org.getcarebase.carebase.repositories;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;

import org.getcarebase.carebase.models.Shipment;
import org.getcarebase.carebase.utils.FirestoreReferences;
import org.getcarebase.carebase.utils.Request;

import java.util.ArrayList;
import java.util.List;

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

    // TODO new schema refactor
    public LiveData<Request> saveShipment(Shipment shipment) {
        MutableLiveData<Request> saveShipmentRequest = new MutableLiveData<>();
        List<Task<?>> tasks = new ArrayList<>();

        tasks.add(shipmentReference.add(shipment));
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
        saveShipmentRequest.setValue(new Request(null, Request.Status.ERROR));
        return saveShipmentRequest;
    }
}
