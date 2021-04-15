package org.getcarebase.carebase.repositories;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;

import org.getcarebase.carebase.R;
import org.getcarebase.carebase.models.Entity;
import org.getcarebase.carebase.models.Shipment;
import org.getcarebase.carebase.models.User;
import org.getcarebase.carebase.utils.FirestoreReferences;
import org.getcarebase.carebase.utils.Request;
import org.getcarebase.carebase.utils.Resource;

import java.util.Map;

public class EntityRepository {
    private static final String TAG = EntityRepository.class.getName();

//    private final String networkId;
//    private final String entityId;
//    private final DocumentReference networkReference;
//    private final DocumentReference entityReference;
//    private final CollectionReference shipmentReference;

//    public EntityRepository(String networkId, String entityId) {
//        this.networkId = networkId;
//        this.entityId = entityId;
//        networkReference = FirestoreReferences.getNetworkReference(networkId);
//        entityReference = FirestoreReferences.getEntityReference(networkReference, entityId);
//        shipmentReference = FirestoreReferences.getShipmentReference(entityReference);
//    }

    public LiveData<Resource<Entity>> getEntity(User user) {
        DocumentReference networkReference = FirestoreReferences.getNetworkReference(user.getNetworkId());
        DocumentReference entityReference = FirestoreReferences.getEntityReference(networkReference,user.getEntityId());
        MutableLiveData<Resource<Entity>> entityLiveData = new MutableLiveData<>();
        entityReference.get().addOnCompleteListener( task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot snapshot = task.getResult();
                if (snapshot != null && snapshot.exists()) {
                    Entity entity = snapshot.toObject(Entity.class);
                    entityLiveData.setValue(new Resource<>(entity,new Request(null,Request.Status.SUCCESS)));
                } else {
                    entityLiveData.setValue(new Resource<>(null,new Request(R.string.error_something_wrong,Request.Status.ERROR)));
                }
            } else {
                entityLiveData.setValue(new Resource<>(null,new Request(R.string.error_something_wrong,Request.Status.ERROR)));
            }
        });
        return entityLiveData;
    }

    /**
     * Gets the possible site options
     * @return a LiveData containing a Resource with a String array
     */
    public LiveData<Resource<Map<String, String>>> getSiteOptions() {
        MutableLiveData<Resource<Map<String, String>>> sitesLiveData = new MutableLiveData<>();
//        CollectionReference collectionReference = networkReference.collection("hospitals");
//        collectionReference.get().addOnCompleteListener( task -> {
//            if (task.isSuccessful()) {
//                Map<String, String> sites = new HashMap<>();
//                for (DocumentSnapshot d : task.getResult().getDocuments())
//                    if (!Objects.equals(d.getId(), "site_options"))
//                        sites.put(d.getId(), (String) d.get("name"));
//
//                sitesLiveData.setValue(new Resource<>(sites,new Request(null, Request.Status.SUCCESS)));
//            } else {
//                sitesLiveData.setValue(new Resource<>(null,new Request(null, Request.Status.ERROR)));
//            }
//        });
        sitesLiveData.setValue(new Resource<>(null,new Request(null, Request.Status.ERROR)));
        return sitesLiveData;
    }

    // TODO new schema refactor
    public LiveData<Request> saveShipment(Shipment shipment) {
        MutableLiveData<Request> saveShipmentRequest = new MutableLiveData<>();
//        List<Task<?>> tasks = new ArrayList<>();
//
//        tasks.add(shipmentReference.add(shipment));
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
//        Tasks.whenAllComplete(tasks).addOnCompleteListener(task -> {
//            if (task.isSuccessful()) {
//                saveShipmentRequest.setValue(new Request(null, Request.Status.SUCCESS));
//            }
//            else {
//                saveShipmentRequest.setValue(new Request(null, Request.Status.ERROR));
//            }
//        });
        saveShipmentRequest.setValue(new Request(null, Request.Status.ERROR));
        return saveShipmentRequest;
    }
}
