package org.getcarebase.carebase.repositories;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.getcarebase.carebase.R;
import org.getcarebase.carebase.api.AccessGUDIDAPI;
import org.getcarebase.carebase.api.AccessGUDIDAPIInstanceFactory;
import org.getcarebase.carebase.models.Cost;
import org.getcarebase.carebase.models.DeviceModel;
import org.getcarebase.carebase.models.DeviceModelGUDIDDeserializer;
import org.getcarebase.carebase.models.DeviceProduction;
import org.getcarebase.carebase.models.ParseUDIResponse;
import org.getcarebase.carebase.models.Procedure;
import org.getcarebase.carebase.utils.Request;
import org.getcarebase.carebase.utils.Resource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

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
        List<Task<?>> tasks = new ArrayList<>();
        // save device model
        DocumentReference deviceModelReference = inventoryReference.document(deviceModel.getDeviceIdentifier());
        tasks.add(deviceModelReference.set(deviceModel.toMap()));

        // save device production
        DeviceProduction deviceProduction = deviceModel.getProductions().get(0);
        // if udi and di are same we make udi unique so other scans do not overwrite (hibcc)
        if (deviceProduction.getUniqueDeviceIdentifier().equals(deviceModel.getDeviceIdentifier())) {
            String expirationDate = deviceProduction.getExpirationDate();
            String mmyyString = expirationDate.substring(5, 7) + expirationDate.substring(2, 4);
            String newUDI = deviceProduction.getUniqueDeviceIdentifier() + "$$" + mmyyString + deviceProduction.getLotNumber();
            deviceProduction.setUniqueDeviceIdentifier(newUDI);
        }

        DocumentReference deviceProductionReference = deviceModelReference.collection("udis").document(deviceProduction.getUniqueDeviceIdentifier());
        tasks.add(deviceProductionReference.set(deviceProduction));

        if (deviceProduction.getCosts().size() != 0) {
            Cost cost = deviceProduction.getCosts().get(0);
            cost.setUser(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getEmail());
            tasks.add(deviceProductionReference.collection("equipment_cost").add(cost));
        }

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
     * Saves the procedure information into every device used in the procedure
     * @param procedures a list of procedures that fundamentally are all the same but rather there
     *                   is a entry for each device used in the procedure
     * @return a Request object detailing the status of the request.
     */
    public LiveData<Request> saveProcedure(List<Procedure> procedures) {
        MutableLiveData<Request> saveProceduresRequest = new MutableLiveData<>();
        List<Task<?>> tasks = new ArrayList<>();

        for (Procedure procedure : procedures) {
            DocumentReference deviceProductionReference = inventoryReference.document(procedure.getDeviceIdentifier())
                    .collection("udis").document(procedure.getUniqueDeviceIdentifier());
            tasks.add(deviceProductionReference.collection("procedures").add(procedure));
        }

        Tasks.whenAllComplete(tasks).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                saveProceduresRequest.setValue(new Request(null, Request.Status.SUCCESS));
            } else {
                // TODO make resource error string
                saveProceduresRequest.setValue(new Request(null, Request.Status.ERROR));
            }
        });
        return saveProceduresRequest;
    }

    /**
     * Gets the current device in firestore if it exists. The udi given will be parsed to get its di.
     * @param udi the udi that is scanned.
     * @return The DeviceModel information that is in the database, if the device production information
     * exists it will be stored in the list of production in the DeviceModel.
     */
    public LiveData<Resource<DeviceModel>> autoPopulateFromDatabase(final String udi) {
        MutableLiveData<Resource<DeviceModel>> deviceLiveData = new MutableLiveData<>();
        deviceLiveData.setValue(new Resource<>(null, new Request(null,Request.Status.LOADING)));
        String hibccDi = extractHibcc(udi);
        if (hibccDi == null) {
            accessGUDIDAPI.getParseUdiResponse(udi).enqueue(new Callback<ParseUDIResponse>() {
                @Override
                public void onResponse(Call<ParseUDIResponse> call, retrofit2.Response<ParseUDIResponse> response) {
                    if (response.isSuccessful()) {
                        getAutoPopulatedDeviceFromFirestore(Objects.requireNonNull(response.body()).getDi(), response.body().getUdi(), deviceLiveData);
                    } else {
                        getAutoPopulatedDeviceFromFirestore(udi, udi, deviceLiveData);
                    }
                }

                @Override
                public void onFailure(Call<ParseUDIResponse> call, Throwable t) {
                    Log.e(TAG, "onFailure: ", t);
                    deviceLiveData.setValue(new Resource<>(null, new Request(R.string.error_something_wrong, Request.Status.ERROR)));
                }
            });
        } else {
            // if it is in hibcc format
            getAutoPopulatedDeviceFromFirestore(udi,udi,deviceLiveData);
        }

        return deviceLiveData;
    }

    /**
     * Gets full device information (device model, device production, costs, and procedures) from firebase.
     * The di and udi are expected to be valid and in firestore
     */
    public LiveData<Resource<DeviceModel>> getDeviceFromFirebase(final String di, final String udi) {
        MutableLiveData<Resource<DeviceModel>> deviceLiveData = new MutableLiveData<>();
        deviceLiveData.setValue(new Resource<>(null, new Request(null, Request.Status.LOADING)));
        AtomicReference<Resource<DeviceModel>> deviceModelAtomicReference = new AtomicReference<>();
        AtomicReference<Resource<DeviceProduction>> deviceProductionAtomicReference = new AtomicReference<>();
        AtomicReference<Resource<List<Cost>>> costsAtomicReference = new AtomicReference<>();
        AtomicReference<Resource<List<Procedure>>> proceduresAtomicReference = new AtomicReference<>();

        Task<?> deviceModelTask = getDeviceModelFromFirestore(di,deviceModelAtomicReference);
        Task<?> deviceProductionTask = getDeviceProductionFromFirestore(di,udi,deviceProductionAtomicReference);
        Task<?> costsTask = getDeviceCostsFromFirestore(di,udi,costsAtomicReference);
        Task<?> proceduresTask = getDeviceProceduresFromFirestore(di,udi,proceduresAtomicReference);

        Tasks.whenAllComplete(deviceModelTask,deviceProductionTask,costsTask,proceduresTask).addOnCompleteListener(tasks -> {
            if (tasks.isSuccessful()) {
                try {
                    DeviceModel deviceModel = deviceModelAtomicReference.get().getData();
                    DeviceProduction deviceProduction = deviceProductionAtomicReference.get().getData();
                    List<Cost> costs = costsAtomicReference.get().getData();
                    deviceProduction.addCosts(costs);
                    List<Procedure> procedures = proceduresAtomicReference.get().getData();
                    deviceProduction.addProcedures(procedures);
                    deviceModel.addDeviceProduction(deviceProduction);
                    deviceLiveData.setValue(new Resource<>(deviceModel, new Request(null, Request.Status.SUCCESS)));
                } catch (NullPointerException e) {
                    Log.e(TAG,e.getMessage());
                    deviceLiveData.setValue(new Resource<>(null, new Request(R.string.error_something_wrong, Request.Status.ERROR)));
                }
            } else {
                deviceLiveData.setValue(new Resource<>(null, new Request(R.string.error_something_wrong, Request.Status.ERROR)));
            }
        });

        return deviceLiveData;
    }

    /**
     * Helper method for autoPopulateFromDatabase
     * Retrieves DeviceModel from Firestore given a di and udi
     * Does not get procedures and costs associated with the device
     */
    private void getAutoPopulatedDeviceFromFirestore(String di, String udi, MutableLiveData<Resource<DeviceModel>> deviceLiveData) {
        AtomicReference<Resource<DeviceModel>> deviceModelAtomicReference = new AtomicReference<>();
        AtomicReference<Resource<DeviceProduction>> deviceProductionAtomicReference = new AtomicReference<>();

        Task<?> deviceModelTask = getDeviceModelFromFirestore(di,deviceModelAtomicReference);
        Task<?> deviceProductionTask = getDeviceProductionFromFirestore(di,udi,deviceProductionAtomicReference);

        Tasks.whenAllComplete(deviceModelTask,deviceProductionTask).addOnCompleteListener(tasks -> {
            Resource<DeviceModel> deviceModelResource = deviceModelAtomicReference.get();
            Resource<DeviceProduction> deviceProductionResource = deviceProductionAtomicReference.get();
            if (deviceModelResource.getRequest().getStatus() == Request.Status.SUCCESS) {
                DeviceModel deviceModel = deviceModelResource.getData();
                if (deviceProductionResource.getRequest().getStatus() == Request.Status.SUCCESS) {
                    DeviceProduction deviceProduction = deviceProductionResource.getData();
                    deviceModel.addDeviceProduction(deviceProduction);
                    deviceLiveData.setValue(new Resource<>(deviceModel,new Request(null, Request.Status.SUCCESS)));
                } else if (deviceProductionResource.getRequest().getStatus() == Request.Status.ERROR) {
                    deviceLiveData.setValue(new Resource<>(deviceModel,new Request(R.string.error_partial_data_in_database, Request.Status.ERROR)));
                }
            } else if (deviceModelResource.getRequest().getStatus() == Request.Status.ERROR) {
                deviceLiveData.setValue(deviceModelResource);
            }
        });
    }

    private Task<?> getDeviceModelFromFirestore(final String di, final AtomicReference<Resource<DeviceModel>> deviceModelAtomicReference) {
        Task<DocumentSnapshot> deviceModelTask = inventoryReference.document(di).get();
        deviceModelTask.addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = Objects.requireNonNull(task.getResult());
                if (document.exists()) {
                    deviceModelAtomicReference.set(new Resource<>(new DeviceModel(document.getData()),new Request(null,Request.Status.SUCCESS)));
                } else {
                    deviceModelAtomicReference.set(new Resource<>(null,new Request(R.string.error_device_lookup, Request.Status.ERROR)));
                }
            } else {
                deviceModelAtomicReference.set(new Resource<>(null,new Request(R.string.error_something_wrong, Request.Status.ERROR)));
            }
        });
        return deviceModelTask;
    }

    private Task<?> getDeviceProductionFromFirestore(final String di, final String udi, final AtomicReference<Resource<DeviceProduction>> deviceProductionAtomicReference) {
        Task<DocumentSnapshot> deviceProductionTask = inventoryReference.document(di).collection("udis").document(udi).get();
        deviceProductionTask.addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = Objects.requireNonNull(task.getResult());
                if (document.exists()) {
                    deviceProductionAtomicReference.set(new Resource<>(new DeviceProduction(document.getData()),new Request(null, Request.Status.SUCCESS)));
                } else {
                    deviceProductionAtomicReference.set(new Resource<>(null,new Request(R.string.error_device_lookup, Request.Status.ERROR)));
                }
            } else {
                deviceProductionAtomicReference.set(new Resource<>(null,new Request(R.string.error_something_wrong, Request.Status.ERROR)));
            }
        });
        return deviceProductionTask;
    }

    private Task<?> getDeviceCostsFromFirestore(final String di, final String udi, final AtomicReference<Resource<List<Cost>>> costsAtomicReference) {
        Task<QuerySnapshot> costsTask = inventoryReference.document(di).collection("udis").document(udi).collection("equipment_cost").get();
        costsTask.addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                QuerySnapshot costSnapshots = task.getResult();
                List<Cost> costs = new ArrayList<>();
                for (QueryDocumentSnapshot documentSnapshot : costSnapshots) {
                    costs.add(documentSnapshot.toObject(Cost.class));
                }
                costsAtomicReference.set(new Resource<>(costs,new Request(null, Request.Status.SUCCESS)));
            } else {
                costsAtomicReference.set(new Resource<>(null,new Request(R.string.error_something_wrong, Request.Status.ERROR)));
            }
        });
        return costsTask;
    }

    private Task<?> getDeviceProceduresFromFirestore(final String di, final String udi, final AtomicReference<Resource<List<Procedure>>> proceduresAtomicReference) {
        Task<QuerySnapshot> proceduresTask = inventoryReference.document(di).collection("udis").document(udi).collection("procedures").get();
        proceduresTask.addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                QuerySnapshot procedureSnapshots = task.getResult();
                List<Procedure> procedures = new ArrayList<>();
                for (QueryDocumentSnapshot documentSnapshot : procedureSnapshots) {
                    procedures.add(documentSnapshot.toObject(Procedure.class));
                }
                proceduresAtomicReference.set(new Resource<>(procedures,new Request(null, Request.Status.SUCCESS)));
            } else {
                proceduresAtomicReference.set(new Resource<>(null,new Request(R.string.error_something_wrong, Request.Status.ERROR)));
            }
        });
        return proceduresTask;
    }

    /**
     * Gets the DeviceModel information stored in the GUDID database.
     * @param udi the udi that is scanned.
     * @return The DeviceModel information that is in the database, the device production information
     * will be stored in the list of production in the DeviceModel
     */
    public LiveData<Resource<DeviceModel>> autoPopulateFromGUDID(final String udi) {
        MutableLiveData<Resource<DeviceModel>> deviceLiveData = new MutableLiveData<>();
        deviceLiveData.setValue(new Resource<>(null, new Request(null,Request.Status.LOADING)));

        Callback<DeviceModel> callback = new Callback<DeviceModel>() {
            @Override
            public void onResponse(Call<DeviceModel> call, Response<DeviceModel> response) {
                DeviceModel deviceModel = response.body();
                if (response.isSuccessful() && deviceModel != null) {
                    if (deviceModel.getDeviceIdentifier() == null) {
                        deviceModel.setDeviceIdentifier(udi);
                    }
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
        };

        String hibccDi = extractHibcc(udi);
        if (hibccDi != null) {
            accessGUDIDAPI.getDIDeviceModel(hibccDi).enqueue(callback);
        } else {
            accessGUDIDAPI.getDeviceModel(udi).enqueue(callback);
        }

        return deviceLiveData;
    }

    // removes plus and check character from valid hibcc di
    // if it is not it will return null
    public static String extractHibcc(String udi) {
        if (udi.charAt(0) == '+')
            return udi.substring(1, udi.length() - 1);
        return null;
    }

 }
