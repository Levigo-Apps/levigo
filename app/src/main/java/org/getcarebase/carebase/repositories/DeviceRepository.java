package org.getcarebase.carebase.repositories;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.getcarebase.carebase.api.AccessGUDIDAPI;
import org.getcarebase.carebase.api.AccessGUDIDAPIInstanceFactory;
import org.getcarebase.carebase.models.DeviceModel;
import org.getcarebase.carebase.models.DeviceModelGUDIDDeserializer;
import org.getcarebase.carebase.models.DeviceProduction;
import org.getcarebase.carebase.models.ParseUDIResponse;
import org.getcarebase.carebase.utils.Request;
import org.getcarebase.carebase.utils.Resource;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * This is class should handle all of the logic of saving and retrieving for a device.
 */
public class DeviceRepository {
    private static final String TAG = "DeviceRepository";
    private final CollectionReference inventoryReference;
    private final AccessGUDIDAPI accessGUDIDAPI = AccessGUDIDAPIInstanceFactory.getRetrofitInstance(DeviceModel.class, new DeviceModelGUDIDDeserializer()).create(AccessGUDIDAPI.class);

    public DeviceRepository(String networkId, String hospitalId) {
        inventoryReference = FirebaseFirestore.getInstance().collection("networks").document(networkId)
                .collection("hospitals").document(hospitalId)
                .collection("departments").document("default_department")
                .collection("dis");
    }

    /**
     * Saves a device and all its associated data (DeviceProcedures and Specifications) into the database.
     * @param deviceModel A device (only one production should be in the array)
     * @return a Request object detailing the status of the request.
     */
    public LiveData<Request> saveDevice(DeviceModel deviceModel) {
        MutableLiveData<Request> saveDeviceRequest = new MutableLiveData<>();
        List<Task<Void>> tasks = new ArrayList<>();
        // save device model
        DocumentReference deviceModelReference = inventoryReference.document(deviceModel.getDeviceIdentifier());
        tasks.add(deviceModelReference.set(deviceModel.toMap()));

        // save device production
        DeviceProduction deviceProduction = deviceModel.getProductions().get(0);
        DocumentReference deviceProductionReference = deviceModelReference.collection("udis").document(deviceProduction.getUniqueDeviceIdentifier());
        tasks.add(deviceProductionReference.set(deviceProduction));

        Tasks.whenAllComplete(tasks).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                saveDeviceRequest.setValue(new Request(null, Request.Status.SUCCESS));
            } else {
                // TODO make resource error string
                saveDeviceRequest.setValue(new Request(null, Request.Status.ERROR));
            }
        });

        return saveDeviceRequest;
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
        MutableLiveData<Resource<DeviceModel>> deviceLiveData = new MutableLiveData<>();
        deviceLiveData.setValue(new Resource<>(null, new Request(null,Request.Status.LOADING)));

        accessGUDIDAPI.getDeviceModel(udi).enqueue(new Callback<DeviceModel>() {
            @Override
            public void onResponse(Call<DeviceModel> call, Response<DeviceModel> response) {
                deviceLiveData.setValue(new Resource<>(response.body(), new Request(null, Request.Status.SUCCESS)));
            }

            @Override
            public void onFailure(Call<DeviceModel> call, Throwable t) {
                Log.e(TAG, "onFailure: ", t);
                deviceLiveData.setValue(new Resource<>(null,new Request(null, Request.Status.ERROR)));
            }
        });

        return deviceLiveData;
    }

 }
