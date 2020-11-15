package org.getcarebase.carebase.repositories;

import android.util.Log;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import org.getcarebase.carebase.R;
import org.getcarebase.carebase.models.DeviceModel;
import org.getcarebase.carebase.models.DeviceProduction;
import org.getcarebase.carebase.models.User;
import org.getcarebase.carebase.utils.Request;
import org.getcarebase.carebase.utils.Resource;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This repository is concerned with inventory data
 */
public class InventoryRepository {
    private final FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    private final Map<String,DeviceModel> deviceModelMap = new HashMap<>();
    private final String TAG = getClass().getSimpleName();

    public LiveData<Resource<Map<String,DeviceModel>>> getDeviceModelMapForHospital(User user) {
        Resource<Map<String,DeviceModel>> deviceModelMapResource = new Resource<>(deviceModelMap, new Request(null, Request.Status.LOADING));
        final MutableLiveData<Resource<Map<String,DeviceModel>>> deviceModelMapLiveData = new MutableLiveData<>(deviceModelMapResource);
        String inventoryRefUrl = "networks/" + user.getNetworkId() + "/hospitals/"
                + user.getHospitalId() + "/departments/default_department/dis";

        firestore.collection(inventoryRefUrl).addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                // handle error
                if (e != null) {
                    try {
                        throw e;
                    } catch (FirebaseFirestoreException firebaseFirestoreException) {
                        firebaseFirestoreException.printStackTrace();
                        Resource<Map<String,DeviceModel>> result = new Resource<>(deviceModelMap, new Request(R.string.error_something_wrong, Request.Status.LOADING));
                        deviceModelMapLiveData.setValue(result);
                    }
                    return;
                }

                if (queryDocumentSnapshots == null) return;
                for (DocumentChange documentChange: queryDocumentSnapshots.getDocumentChanges()) {
                    if (documentChange.getType() == DocumentChange.Type.ADDED) {
                        final DeviceModel deviceModel = new DeviceModel(documentChange.getDocument().getData());
                        // TODO separate udi listeners from di listeners
                        // set listener for each production of mode (udi of di)
                        documentChange.getDocument().getReference().collection("udis").addSnapshotListener(new EventListener<QuerySnapshot>() {
                            @Override
                            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                                // handle error
                                if (e != null) {
                                    try {
                                        throw e;
                                    } catch (FirebaseFirestoreException firebaseFirestoreException) {
                                        firebaseFirestoreException.printStackTrace();
                                        Resource<Map<String, DeviceModel>> result = new Resource<>(deviceModelMap, new Request(R.string.error_something_wrong, Request.Status.LOADING));
                                        deviceModelMapLiveData.setValue(result);
                                    }
                                    return;
                                }

                                if (queryDocumentSnapshots == null) return;
                                for (DocumentChange documentChange: queryDocumentSnapshots.getDocumentChanges()) {
                                    if (documentChange.getType() == DocumentChange.Type.ADDED
                                            || documentChange.getType() == DocumentChange.Type.MODIFIED) {
                                        DeviceProduction deviceProduction = new DeviceProduction(documentChange.getDocument().getData());
                                        deviceModel.addDeviceProduction(deviceProduction);
                                    }
                                    else if (documentChange.getType() == DocumentChange.Type.REMOVED) {
                                        String udi = (String) documentChange.getDocument().get("udi");
                                        deviceModel.removeProduction(udi);
                                    }
                                }
                                Resource<Map<String,DeviceModel>> result = new Resource<>(deviceModelMap, new Request(null, Request.Status.SUCCESS));
                                deviceModelMapLiveData.setValue(result);
                            }
                        });
                        deviceModelMap.put(deviceModel.getDeviceIdentifier(),deviceModel);
                    }
                    else if (documentChange.getType() == DocumentChange.Type.MODIFIED) {
                        String di = (String) documentChange.getDocument().get("di");
                        DeviceModel deviceModel = deviceModelMap.get(di);
                        deviceModel.updateFields(documentChange.getDocument().getData());
                    }
                    else if (documentChange.getType() == DocumentChange.Type.REMOVED) {
                        String di = (String) documentChange.getDocument().get("di");
                        deviceModelMap.remove(di);
                    }
                    Resource<Map<String,DeviceModel>> result = new Resource<>(deviceModelMap,new Request(null , Request.Status.SUCCESS));
                    deviceModelMapLiveData.setValue(result);
                }
            }
        });
        return deviceModelMapLiveData;
    }
}
