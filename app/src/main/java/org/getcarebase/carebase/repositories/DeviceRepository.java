package org.getcarebase.carebase.repositories;

import androidx.lifecycle.LiveData;

import com.android.volley.Response;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import org.getcarebase.carebase.models.DeviceModel;
import org.getcarebase.carebase.models.DeviceProduction;
import org.getcarebase.carebase.utils.Request;
import org.getcarebase.carebase.utils.Resource;

/**
 * This is class should handle all of the logic of saving and retrieving for a device.
 */
public class DeviceRepository {
    private final CollectionReference inventoryReference;

    public DeviceRepository(String networkId, String hospitalId) {
        inventoryReference = FirebaseFirestore.getInstance().collection("networks").document(networkId)
                .collection("hospitals").document(hospitalId)
                .collection("departments").document("default_department")
                .collection("dis");
    }

    /**
     * Saves only the model information into the di collection. Does not save production information.
     * @param deviceModel the di level information of the device.
     * @return a Request object detailing the status of the request.
     */
    public LiveData<Request> saveDeviceModel(DeviceModel deviceModel) {
        return null;
    }

    /**
     * Saves only the device production into the udi collection of the di.
     * @param di The di of the device.
     * @param deviceProduction the udi level information of the device.
     * @return a Request object detailing the status of the request.
     */
    public LiveData<Request> saveDeviceProduction(String di, DeviceProduction deviceProduction) {
        return null;
    }

    /**
     * Gets the current device in firestore if it exists. The udi given will be parsed to get its di.
     * @param udi the udi that is scanned.
     * @return The DeviceModel information that is in the database, if the device production information
     * exists it will be stored in the list of production in the DeviceModel.
     */
    public LiveData<Resource<DeviceModel>> autoPopulateFromDatabase(String udi) {
        return null;
    }

    /**
     * Gets the DeviceModel information stored in the GUDID database.
     * @param udi the udi that is scanned.
     * @return The DeviceModel information that is in the database, the device production information
     * will be stored in the list of production in the DeviceModel
     */
    public LiveData<Resource<DeviceModel>> autoPopulateFromGUDID(String udi) {
        return null;
    }

    /**
     * Uses the ParseUDI API to get the di given the udi.
     * @param udi the scanned udi.
     * @param listener the listener to be notified when there is a response.
     */
    private void  getDIFromUDI(String udi, Response.Listener<String> listener) {

    }
 }
