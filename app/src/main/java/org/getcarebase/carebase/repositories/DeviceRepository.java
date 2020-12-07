package org.getcarebase.carebase.repositories;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import org.getcarebase.carebase.R;
import org.getcarebase.carebase.api.AccessGUDIDAPI;
import org.getcarebase.carebase.api.AccessGUDIDAPIInstanceFactory;
import org.getcarebase.carebase.models.DeviceModel;
import org.getcarebase.carebase.models.DeviceModelGUDIDDeserializer;
import org.getcarebase.carebase.models.DeviceProduction;
import org.getcarebase.carebase.models.ParseUDIResponse;
import org.getcarebase.carebase.utils.Request;
import org.getcarebase.carebase.utils.Resource;
import org.w3c.dom.Document;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Random;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * This is class should handle all of the logic of saving and retrieving for a device.
 */
public class DeviceRepository {
    private static final String TAG = "DeviceRepository";
    private final FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    private final AccessGUDIDAPI accessGUDIDAPI = AccessGUDIDAPIInstanceFactory.getRetrofitInstance(DeviceModel.class, new DeviceModelGUDIDDeserializer()).create(AccessGUDIDAPI.class);

    private final String networkId;
    private final String hospitalId;
    private final CollectionReference inventoryReference;
    private final DocumentReference deviceTypesReference;
    private final DocumentReference physicalLocationsReference;

    public DeviceRepository(String networkId, String hospitalId) {
        this.networkId = networkId;
        this.hospitalId = hospitalId;
        inventoryReference = firestore.collection("networks").document(networkId)
                .collection("hospitals").document(hospitalId)
                .collection("departments").document("default_department")
                .collection("dis");
        deviceTypesReference = firestore.collection("networks").document(networkId)
                .collection("hospitals").document(hospitalId)
                .collection("types").document("type_options");
        physicalLocationsReference = firestore.collection("networks").document(networkId)
                .collection("hospitals").document(hospitalId)
                .collection("physical_locations").document("locations");
    }

    /**
     * Gets the possible device types and updates the returned LiveData when changes are made
     * @return a LiveData containing a Resource with a String array
     */
    public LiveData<Resource<String[]>> getDeviceTypeOptions() {
        MutableLiveData<Resource<String[]>> deviceTypesLiveData = new MutableLiveData<>();
        deviceTypesReference.addSnapshotListener((documentSnapshot, e) -> {
            if (documentSnapshot != null && documentSnapshot.exists()) {
                // convert to array of strings
                Object[] objects = documentSnapshot.getData().values().toArray();
                String[] types = (String[]) Arrays.asList(objects).toArray(new String[objects.length]);
                Arrays.sort(types);
                deviceTypesLiveData.setValue(new Resource<>(types,new Request(null, Request.Status.SUCCESS)));
            }
            else {
                if (e != null) {
                    Log.e(TAG, "Listen failed.", e);
                }
                // TODO make error message in strings
                deviceTypesLiveData.setValue(new Resource<>(null,new Request(null, Request.Status.ERROR)));
            }
        });
        return deviceTypesLiveData;
    }

    /**
     * Saves new device type
     * @param deviceType the device type to be added
     * @return a LiveData of Request indicating whether the addition was successful
     */
    public LiveData<Request> saveDeviceType(String deviceType) {
        MutableLiveData<Request> saveDeviceTypeRequest = new MutableLiveData<>();
        // random key as key does not matter -> need to change doc to array in firebase
        String key = "type_" + ((int) (Math.random() * 1000000) + 1);
        deviceTypesReference.update(key,deviceType).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                saveDeviceTypeRequest.setValue(new Request(R.string.new_device_type_saved, Request.Status.SUCCESS));
            } else {
                saveDeviceTypeRequest.setValue(new Request(R.string.error_something_wrong, Request.Status.ERROR));
            }
        });
        return saveDeviceTypeRequest;
    }

    /**
     * Gets the possible site options
     * @return a LiveData containing a Resource with a String array
     */
    public LiveData<Resource<String[]>> getSiteOptions() {
        MutableLiveData<Resource<String[]>> sitesLiveData = new MutableLiveData<>();
        DocumentReference documentReference = firestore.collection("networks").document(networkId)
                .collection("hospitals").document("site_options");
        documentReference.get().addOnCompleteListener( task -> {
            if (task.isSuccessful()) {
                // convert to array of strings
                Object[] objects = task.getResult().getData().values().toArray();
                String[] sites = (String[]) Arrays.asList(objects).toArray(new String[objects.length]);
                Arrays.sort(sites);
                sitesLiveData.setValue(new Resource<>(sites,new Request(null, Request.Status.SUCCESS)));
            } else {
                // TODO make error message in strings
                sitesLiveData.setValue(new Resource<>(null,new Request(null, Request.Status.ERROR)));
            }
        });
        return sitesLiveData;
    }

    /**
     * Gets the possible physical locations in the current hospital and updates the returned LiveData
     * when any changes are made
     * @return a LiveData containing a Resource with a String array
     */
    public LiveData<Resource<String[]>> getPhysicalLocationOptions(){
        MutableLiveData<Resource<String[]>> physicalLocationsLiveData = new MutableLiveData<>();
        physicalLocationsReference.addSnapshotListener((documentSnapshot, e) -> {
            if (documentSnapshot != null && documentSnapshot.exists()) {
                // convert to array of strings
                Object[] objects = documentSnapshot.getData().values().toArray();
                String[] physicalLocations = (String[]) Arrays.asList(objects).toArray(new String[objects.length]);
                Arrays.sort(physicalLocations);
                physicalLocationsLiveData.setValue(new Resource<>(physicalLocations,new Request(null, Request.Status.SUCCESS)));
            }
            else {
                if (e != null) {
                    Log.e(TAG, "Listen failed.", e);
                }
                // TODO make error message in strings
                physicalLocationsLiveData.setValue(new Resource<>(null,new Request(null, Request.Status.ERROR)));
            }
        });
        return physicalLocationsLiveData;
    }

    /**
     * Saves new physical location
     * @param physicalLocation physical location to be added
     * @return a LiveData of Request indicating whether the addition was successful
     */
    public LiveData<Request> savePhysicalLocation(String physicalLocation) {
        MutableLiveData<Request> saveDeviceTypeRequest = new MutableLiveData<>();
        // random key as key does not matter -> need to change doc to array in firebase
        String key = "loc_" + ((int) (Math.random() * 1000000) + 1);
        physicalLocationsReference.update(key,physicalLocation).addOnCompleteListener(task -> {
            // TODO make success message and error message in strings
            if (task.isSuccessful()) {
                saveDeviceTypeRequest.setValue(new Request(R.string.new_physical_location_saved, Request.Status.SUCCESS));
            } else {
                saveDeviceTypeRequest.setValue(new Request(R.string.error_something_wrong, Request.Status.ERROR));
            }
        });
        return saveDeviceTypeRequest;
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
                if (response.isSuccessful()) {
                    getDeviceFromFirestore(Objects.requireNonNull(response.body()).getDi(), response.body().getUdi(), deviceLiveData);
                } else {
                    getDeviceFromFirestore(udi, udi, deviceLiveData);
                }
            }

            @Override
            public void onFailure(Call<ParseUDIResponse> call, Throwable t) {
                Log.e(TAG, "onFailure: ", t);
                deviceLiveData.setValue(new Resource<>(null, new Request(R.string.error_something_wrong, Request.Status.ERROR)));
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
                            if (udiDocument.exists()) {
                                DeviceProduction deviceProduction = new DeviceProduction(Objects.requireNonNull(udiDocument.getData()));
                                deviceModel.addDeviceProduction(deviceProduction);
                                deviceLiveData.setValue(new Resource<>(deviceModel,new Request(null, Request.Status.SUCCESS)));
                            }
                            else {
                                // device production information is not in the database (partial data)
                                deviceLiveData.setValue(new Resource<>(deviceModel,new Request(R.string.error_partial_data_in_database, Request.Status.ERROR)));
                            }
                        }
                        else {
                            // something went wrong during device production retrieval
                            deviceLiveData.setValue(new Resource<>(null,new Request(R.string.error_something_wrong, Request.Status.ERROR)));
                        }
                    });
                }
                else {
                    // device with given di is not in database
                    deviceLiveData.setValue(new Resource<>(null,new Request(R.string.error_device_lookup, Request.Status.ERROR)));
                }
            }
            else {
                // something went wrong during device model retrieval
                deviceLiveData.setValue(new Resource<>(null,new Request(R.string.error_something_wrong, Request.Status.ERROR)));
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
                if (response.isSuccessful()) {
                    deviceLiveData.setValue(new Resource<>(response.body(), new Request(null, Request.Status.SUCCESS)));
                } else {
                    deviceLiveData.setValue(new Resource<>(null,new Request(R.string.error_device_lookup, Request.Status.ERROR)));
                }

            }

            @Override
            public void onFailure(Call<DeviceModel> call, Throwable t) {
                Log.e(TAG, "onFailure: ", t);
                deviceLiveData.setValue(new Resource<>(null,new Request(R.string.error_something_wrong, Request.Status.ERROR)));
            }
        });

        return deviceLiveData;
    }

 }
