package org.getcarebase.carebase.repositories;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.getcarebase.carebase.R;
import org.getcarebase.carebase.models.DeviceModel;
import org.getcarebase.carebase.models.DeviceProduction;
import org.getcarebase.carebase.models.DeviceType;
import org.getcarebase.carebase.models.User;
import org.getcarebase.carebase.utils.FirestoreReferences;
import org.getcarebase.carebase.utils.Request;
import org.getcarebase.carebase.utils.Resource;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * This repository is concerned with retrieving inventory data.
 * Also maintains the list of the devices until it is cleaned up by garbage collector.
 * All other components will only use this list defined in this instance of the repository
 */
public class InventoryRepository {
    private final FirebaseFirestore firestore = FirebaseFirestore.getInstance();

    private List<DeviceType> deviceTypeList = new ArrayList<>();
    private final List<DeviceModel> deviceModelList = new ArrayList<>();

    // a function that gets all of the types in the inventory
    public LiveData<Resource<List<DeviceType>>> getAllTypes(User user) {
        deviceTypeList.clear();
        Resource<List<DeviceType>> deviceTypeListResource = new Resource<>(deviceTypeList, new Request(null, Request.Status.LOADING));
        final MutableLiveData<Resource<List<DeviceType>>> deviceTypeListLiveData = new MutableLiveData<>(deviceTypeListResource);

        DocumentReference networkReference = FirestoreReferences.getNetworkReference(user.getNetworkId());
        DocumentReference entityReference = FirestoreReferences.getEntityReference(networkReference,user.getEntityId());
        CollectionReference deviceTypesReference = FirestoreReferences.getDeviceTypesReference(entityReference);
        deviceTypesReference.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                List<DeviceType> types = task.getResult().toObjects(DeviceType.class);
                deviceTypeListLiveData.setValue(new Resource<>(types,new Request(null,Request.Status.SUCCESS)));
            } else {
                deviceTypeListLiveData.setValue(new Resource<>(null,new Request(R.string.error_something_wrong,Request.Status.ERROR)));
            }
        });

        return deviceTypeListLiveData;
    }

    // a function that gets the devices with the given type
    public LiveData<Resource<List<DeviceModel>>> getDevicesWithType(User user, String selectedType, List<String> selectedTags) {
        deviceModelList.clear();
        Resource<List<DeviceModel>> deviceModelListResource = new Resource<>(deviceModelList, new Request(null, Request.Status.LOADING));
        final MutableLiveData<Resource<List<DeviceModel>>> deviceModelListLiveData = new MutableLiveData<>(deviceModelListResource);

        DocumentReference networkReference = FirestoreReferences.getNetworkReference(user.getNetworkId());
        DocumentReference entityReference = FirestoreReferences.getEntityReference(networkReference,user.getEntityId());
        CollectionReference inventoryReference = FirestoreReferences.getInventoryReference(entityReference);
        Query query;
        if (selectedTags.isEmpty()) {
            query = inventoryReference.whereEqualTo("equipment_type", selectedType);
        } else {
            query = inventoryReference.whereEqualTo("equipment_type", selectedType).whereArrayContainsAny("tags", selectedTags);
        }
        query.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                List<Task<QuerySnapshot>> tasks = new ArrayList<>();
                for (QueryDocumentSnapshot modelSnapshot : Objects.requireNonNull(task.getResult())) {
                    final DeviceModel deviceModel = new DeviceModel(modelSnapshot.getData());

                    Task<QuerySnapshot> productionTask = modelSnapshot.getReference().collection("udis").get();
                    tasks.add(productionTask);
                    productionTask.addOnCompleteListener(completeProductionTask -> {
                        for (QueryDocumentSnapshot productionSnapshot : Objects.requireNonNull(completeProductionTask.getResult())) {
                            if (completeProductionTask.isSuccessful()) {
                                DeviceProduction deviceProduction = new DeviceProduction(productionSnapshot.getData());
                                deviceModel.addDeviceProduction(deviceProduction);
                            } else {
                                // error if getting all productions for a model fails
                                deviceModelListLiveData.setValue(new Resource<>(null, new Request(R.string.error_something_wrong, Request.Status.ERROR)));
                            }
                        }
                    });
                    deviceModelList.add(deviceModel);
                }
                Tasks.whenAllSuccess(tasks).addOnCompleteListener(task1 -> deviceModelListLiveData.setValue(new Resource(deviceModelList, new Request(null, Request.Status.SUCCESS))));
            } else {
                // error if getting all models fails
                deviceModelListLiveData.setValue(new Resource(null, new Request(R.string.error_something_wrong, Request.Status.ERROR)));
            }
        });

        return deviceModelListLiveData;
    }

}

