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

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class EntityRepository {
    private static final String TAG = EntityRepository.class.getName();

    private final String networkId;
//    private final String entityId;
    private final DocumentReference networkReference;
//    private final DocumentReference entityReference;
//    private final CollectionReference shipmentReference;

    public EntityRepository() {
        this.networkId = null;
        networkReference = null;
    }

    public EntityRepository(String networkId) {
        this.networkId = networkId;
        this.networkReference = FirestoreReferences.getNetworkReference(networkId);
    }

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
        CollectionReference collectionReference = networkReference.collection("entities");
        collectionReference.get().addOnCompleteListener( task -> {
            if (task.isSuccessful()) {
                Map<String, String> sites = new HashMap<>();
                for (DocumentSnapshot d : task.getResult().getDocuments())
                    //if (!Objects.equals(d.getId(), "site_options"))
                        sites.put(d.getId(), (String) d.get("name"));

                sitesLiveData.setValue(new Resource<>(sites,new Request(null, Request.Status.SUCCESS)));
            } else {
                sitesLiveData.setValue(new Resource<>(null,new Request(null, Request.Status.ERROR)));
            }
        });
        // sitesLiveData.setValue(new Resource<>(null,new Request(null, Request.Status.ERROR)));
        return sitesLiveData;
    }
}
