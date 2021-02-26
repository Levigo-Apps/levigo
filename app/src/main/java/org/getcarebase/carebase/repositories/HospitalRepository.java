package org.getcarebase.carebase.repositories;

import android.util.Pair;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import org.getcarebase.carebase.api.AccessGUDIDAPI;
import org.getcarebase.carebase.api.AccessGUDIDAPIInstanceFactory;
import org.getcarebase.carebase.models.DeviceModel;
import org.getcarebase.carebase.models.DeviceModelGUDIDDeserializer;
import org.getcarebase.carebase.models.Shipment;
import org.getcarebase.carebase.utils.FirestoreReferences;
import org.getcarebase.carebase.utils.Request;
import org.getcarebase.carebase.utils.Resource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

public class HospitalRepository {
    private static final String TAG = HospitalRepository.class.getName();

    private final String networkId;
    private final String hospitalId;
    private final DocumentReference networkReference;
    private final DocumentReference hospitalReference;
    private final CollectionReference shipmentReference;

    public HospitalRepository(String networkId, String hospitalId) {
        this.networkId = networkId;
        this.hospitalId = hospitalId;
        networkReference = FirestoreReferences.getNetworkReference(networkId);
        hospitalReference = FirestoreReferences.getHospitalReference(networkReference, hospitalId);
        shipmentReference = FirestoreReferences.getShipmentReference(hospitalReference);
    }

    /**
     * Gets the possible site options
     * @return a LiveData containing a Resource with a String array
     */
    public LiveData<Resource<Map<String, String>>> getSiteOptions() {
        MutableLiveData<Resource<Map<String, String>>> sitesLiveData = new MutableLiveData<>();
        CollectionReference collectionReference = networkReference.collection("hospitals");
        collectionReference.get().addOnCompleteListener( task -> {
            if (task.isSuccessful()) {
                Map<String, String> sites = new HashMap<>();
                for (DocumentSnapshot d : task.getResult().getDocuments())
                    if (!Objects.equals(d.getId(), "site_options"))
                        sites.put(d.getId(), (String) d.get("name"));

                sitesLiveData.setValue(new Resource<>(sites,new Request(null, Request.Status.SUCCESS)));
            } else {
                // TODO make error message in strings
                sitesLiveData.setValue(new Resource<>(null,new Request(null, Request.Status.ERROR)));
            }
        });
        return sitesLiveData;
    }

    public LiveData<Request> saveShipment(Shipment shipment) {
        MutableLiveData<Request> saveShipmentRequest = new MutableLiveData<>();
        List<Task<?>> tasks = new ArrayList<>();

        tasks.add(shipmentReference.add(shipment));

        // update device model and production quantities
        DocumentReference currentHospitalReference = FirestoreReferences.getHospitalReference(
                networkReference, shipment.getSourceHospitalId());
        CollectionReference inventoryReference = FirestoreReferences.getInventoryReference(currentHospitalReference);
        DocumentReference deviceModelReference = inventoryReference.document(shipment.getDi());
        DocumentReference deviceProductionReference = deviceModelReference.collection("udis").document(shipment.getUdi());
        tasks.add(deviceModelReference.update("quantity", FieldValue.increment(-1*shipment.getShippedQuantity())));
        tasks.add(deviceProductionReference.update("quantity",FieldValue.increment(-1*shipment.getShippedQuantity())));

        Tasks.whenAllComplete(tasks).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                saveShipmentRequest.setValue(new Request(null, Request.Status.SUCCESS));
            }
            else {
                saveShipmentRequest.setValue(new Request(null, Request.Status.ERROR));
            }
        });

        return saveShipmentRequest;
    }
}
