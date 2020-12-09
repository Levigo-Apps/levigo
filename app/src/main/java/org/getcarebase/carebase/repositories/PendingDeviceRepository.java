package org.getcarebase.carebase.repositories;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

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
}
