package org.getcarebase.carebase.utils;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class FirestoreReferences {

    public static DocumentReference getNetworkReference(String networkId) {
        return FirebaseFirestore.getInstance().collection("networks").document(networkId);
    }

    public static DocumentReference getHospitalReference(DocumentReference networkReference, String hospitalId) {
        return networkReference.collection("hospitals").document(hospitalId);
    }

    public static CollectionReference getInventoryReference(DocumentReference hospitalReference) {
        return hospitalReference.collection("departments").document("default_department")
                .collection("dis");
    }

    public static CollectionReference getProceduresReference(DocumentReference hospitalReference) {
        return hospitalReference.collection("departments").document("default_department")
                .collection("procedures");
    }

    public static DocumentReference getDeviceTypesReference(DocumentReference hospitalReference) {
        return hospitalReference.collection("types").document("type_options");
    }

    public static DocumentReference getPhysicalLocations(DocumentReference hospitalReference) {
        return hospitalReference.collection("physical_locations").document("locations");
    }

    public static CollectionReference getShipmentReference(DocumentReference hospitalRefence) {
        return hospitalRefence.collection("shipments");
    }
}
