package org.getcarebase.carebase.utils;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class FirestoreReferences {

    public static FirebaseFirestore getFirestoreReference() {
        return FirebaseFirestore.getInstance();
    }

    public static DocumentReference getProductCodeReference(String productCode) {
        return FirebaseFirestore.getInstance().collection("fda_product_codes").document(productCode);
    }

    public static DocumentReference getNetworkReference(String networkId) {
        return FirebaseFirestore.getInstance().collection("networks").document(networkId);
    }

    public static DocumentReference getEntityReference(DocumentReference networkReference, String entityId) {
        return networkReference.collection("entities").document(entityId);
    }

    public static CollectionReference getShipmentReference(DocumentReference networkReference) {
        return networkReference.collection("shipments");
    }

    public static CollectionReference getInventoryReference(DocumentReference entityReference) {
        return entityReference.collection("departments").document("default_department")
                .collection("dis");
    }

    public static CollectionReference getProceduresReference(DocumentReference entityReference) {
        return entityReference.collection("departments").document("default_department")
                .collection("procedures");
    }

    public static DocumentReference getDeviceTypesReference(DocumentReference entityReference) {
        return entityReference.collection("types").document("type_options");
    }

    public static DocumentReference getPhysicalLocations(DocumentReference entityReference) {
        return entityReference.collection("physical_locations").document("locations");
    }
}
