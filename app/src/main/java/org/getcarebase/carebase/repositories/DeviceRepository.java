package org.getcarebase.carebase.repositories;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.getcarebase.carebase.api.AccessGUDIDAPI;
import org.getcarebase.carebase.api.AccessGUDIDAPIService;
import org.getcarebase.carebase.models.DeviceModel;
import org.getcarebase.carebase.models.DeviceProduction;
import org.getcarebase.carebase.models.ParseUDIResponse;
import org.getcarebase.carebase.utils.Request;
import org.getcarebase.carebase.utils.Resource;

import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;

/**
 * This is class should handle all of the logic of saving and retrieving for a device.
 */
public class DeviceRepository {
    private static final String TAG = "DeviceRepository";
    private final CollectionReference inventoryReference;
    private AccessGUDIDAPI accessGUDIDAPI;

    public DeviceRepository(String networkId, String hospitalId) {
        inventoryReference = FirebaseFirestore.getInstance().collection("networks").document(networkId)
                .collection("hospitals").document(hospitalId)
                .collection("departments").document("default_department")
                .collection("dis");
        accessGUDIDAPI = AccessGUDIDAPIService.createService(accessGUDIDAPI.getClass());
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
        MutableLiveData<Resource<DeviceModel>> deviceLiveData = new MutableLiveData<>();
        deviceLiveData.setValue(new Resource<>(null, new Request(null,Request.Status.LOADING)));
        accessGUDIDAPI.getParseUdiResponse(udi).enqueue(new Callback<ParseUDIResponse>() {
            @Override
            public void onResponse(Call<ParseUDIResponse> call, retrofit2.Response<ParseUDIResponse> response) {
                getDeviceFromFirestore(response.body().getDi(), response.body().getUdi(), deviceLiveData);
            }

            @Override
            public void onFailure(Call<ParseUDIResponse> call, Throwable t) {
                Log.e(TAG, "onFailure: ", t);
                getDeviceFromFirestore(udi, udi, deviceLiveData);
            }
        });

        return deviceLiveData;
    }

    /**
     * Helper method for autoPopulateFromDatabase
     * Retrieves DeviceModel from Firestore given a di and udi
     */
    private void getDeviceFromFirestore(String di, String udi, MutableLiveData<Resource<DeviceModel>> deviceLiveData) {
        DocumentReference deviceModelReference = inventoryReference.document(di);
        // get device model information
        deviceModelReference.get().addOnCompleteListener(diTask -> {
            if (diTask.isSuccessful()) {
                DocumentSnapshot document = Objects.requireNonNull(diTask.getResult());
                if (document.exists()) {
                    DeviceModel deviceModel = new DeviceModel(Objects.requireNonNull(document.getData()));
                    // get device production information
                    deviceModelReference.collection("udis").document(udi).get().addOnCompleteListener(udiTask -> {
                        if (udiTask.isSuccessful()) {
                            DocumentSnapshot udiDocument = Objects.requireNonNull(udiTask.getResult());
                            if (document.exists()) {
                                DeviceProduction deviceProduction = new DeviceProduction(Objects.requireNonNull(udiDocument.getData()));
                                deviceModel.addDeviceProduction(deviceProduction);
                                deviceLiveData.setValue(new Resource<>(deviceModel,new Request(null, Request.Status.SUCCESS)));
                            }
                        }
                        else {
                            // udi information is not in the database
                            deviceLiveData.setValue(new Resource<>(deviceModel,new Request(null, Request.Status.SUCCESS)));
                        }
                    });
                }
            }
            else {
                // device with given di is not in database
                // TODO make error message in strings
                deviceLiveData.setValue(new Resource<>(null,new Request(null, Request.Status.ERROR)));
            }
        });

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

 }
