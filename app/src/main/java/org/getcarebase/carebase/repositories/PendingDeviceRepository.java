package org.getcarebase.carebase.repositories;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import org.getcarebase.carebase.R;
import org.getcarebase.carebase.models.PendingDevice;
import org.getcarebase.carebase.utils.Request;
import org.getcarebase.carebase.utils.Resource;

import java.util.ArrayList;
import java.util.List;

public class PendingDeviceRepository {
    private static final String TAG = "PendingDeviceRepository";
    private final FirebaseFirestore firestore = FirebaseFirestore.getInstance();

    private final CollectionReference pendingDevicesReference;

    public PendingDeviceRepository(final String networkId, final String hospitalId) {
        pendingDevicesReference = firestore.collection("networks").document(networkId)
                .collection("hospitals").document(hospitalId)
                .collection("departments").document("default_department")
                .collection("pending_udis");
    }

    public LiveData<Request> savePendingDevice(PendingDevice pendingDevice) {
        MutableLiveData<Request> requestMutableLiveData = new MutableLiveData<>();
        pendingDevicesReference.add(pendingDevice).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                requestMutableLiveData.setValue(new Request(null, Request.Status.SUCCESS));
            } else {
                requestMutableLiveData.setValue(new Request(R.string.error_something_wrong, Request.Status.ERROR));
            }
        });
        return requestMutableLiveData;
    }

    public LiveData<Resource<List<PendingDevice>>> getPendingDevices() {
        MutableLiveData<Resource<List<PendingDevice>>> pendingDevicesMutableLiveData = new MutableLiveData<>();
        pendingDevicesReference.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                List<PendingDevice> pendingDevices = new ArrayList<>();
                if (task.getResult() != null ){
                    for (QueryDocumentSnapshot documentSnapshot : task.getResult()) {
                        pendingDevices.add(documentSnapshot.toObject(PendingDevice.class));
                    }
                }
                pendingDevicesMutableLiveData.setValue(new Resource<>(pendingDevices,new Request(null,Request.Status.SUCCESS)));
            } else {
                pendingDevicesMutableLiveData.setValue(new Resource<>(null, new Request(null,Request.Status.ERROR)));
            }
        });
        return pendingDevicesMutableLiveData;
    }

    public LiveData<Request> removePendingDevice(String pendingDeviceId) {
        MutableLiveData<Request> requestMutableLiveData = new MutableLiveData<>();
        pendingDevicesReference.document(pendingDeviceId).delete().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                requestMutableLiveData.setValue(new Request(null, Request.Status.SUCCESS));
            } else {
                requestMutableLiveData.setValue(new Request(R.string.error_something_wrong, Request.Status.ERROR));
            }
        });
        return requestMutableLiveData;
    }
}
