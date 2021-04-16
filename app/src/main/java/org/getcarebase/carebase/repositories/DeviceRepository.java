package org.getcarebase.carebase.repositories;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;

import org.getcarebase.carebase.R;
import org.getcarebase.carebase.api.AccessGUDIDAPI;
import org.getcarebase.carebase.api.AccessGUDIDAPIInstanceFactory;
import org.getcarebase.carebase.models.Cost;
import org.getcarebase.carebase.models.DeviceModel;
import org.getcarebase.carebase.models.DeviceModelGUDIDDeserializer;
import org.getcarebase.carebase.models.DeviceProduction;
import org.getcarebase.carebase.models.ParseUDIResponse;
import org.getcarebase.carebase.models.Procedure;
import org.getcarebase.carebase.models.Shipment;
import org.getcarebase.carebase.utils.Event;
import org.getcarebase.carebase.utils.FirestoreReferences;
import org.getcarebase.carebase.utils.Request;
import org.getcarebase.carebase.utils.Resource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * This is class should handle all of the logic of saving and retrieving for a device.
 */
public class DeviceRepository {
    private static final String TAG = DeviceRepository.class.getName();
    private final FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    private final AccessGUDIDAPI accessGUDIDAPI = AccessGUDIDAPIInstanceFactory.getRetrofitInstance(DeviceModel.class, new DeviceModelGUDIDDeserializer()).create(AccessGUDIDAPI.class);

    private final DocumentReference networkReference;
    private final DocumentReference hospitalReference;
    private final CollectionReference inventoryReference;
    private final CollectionReference proceduresReference;
    private final CollectionReference shipmentReference;
    private final DocumentReference physicalLocationsReference;

    private ListenerRegistration deviceModelListenerRegistration;
    private ListenerRegistration deviceProductionListenerRegistration;


    public DeviceRepository(String networkId, String entityId) {
        networkReference = FirestoreReferences.getNetworkReference(networkId);
        hospitalReference = FirestoreReferences.getEntityReference(networkReference, entityId);
        inventoryReference = FirestoreReferences.getInventoryReference(hospitalReference);
        proceduresReference = FirestoreReferences.getProceduresReference(hospitalReference);
        shipmentReference = FirestoreReferences.getShipmentReference(hospitalReference);
        physicalLocationsReference = FirestoreReferences.getPhysicalLocations(hospitalReference);
    }

    public void destroy() {
        if (deviceModelListenerRegistration != null) {
            deviceModelListenerRegistration.remove();
        }
        if (deviceProductionListenerRegistration != null){
            deviceProductionListenerRegistration.remove();
        }
    }

    /**
     * Gets the possible device types
     * @return a map of device types
     */
    public static Map<String,List<String>> getDeviceTypeOptions() {
        Map<String,List<String>> deviceTypes = new LinkedHashMap<>();
        deviceTypes.put("Balloons", null);
        deviceTypes.put("Catheters",Arrays.asList("Microcatheter","Diagnostic Catheter","Flush","Directional"));
        deviceTypes.put("Dilators",null);
        deviceTypes.put("Drainage",Arrays.asList("Biliary","Nephrostomy","Multipurpose"));
        deviceTypes.put("Embolic Agents",Arrays.asList("Coils","Gelfoam","PVA"));
        deviceTypes.put("Inflation Devices",null);
        deviceTypes.put("Needles",Arrays.asList("Micropuncture","Non-vascular access","Biopsy","Other"));
        deviceTypes.put("Sheath",null);
        deviceTypes.put("Stents",null);
        deviceTypes.put("Vascular Access",Arrays.asList("Picc","Dialysis","Port","Hickman","Other"));
        deviceTypes.put("Wire",Arrays.asList("Microwire","Glidewire","Regular Wire", "Stiff Wire"));
        deviceTypes.put("Other",Arrays.asList("U/S Equipment","Syringe","Drainage Bags/Kits","Securement/Sutures"));
        return deviceTypes;
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
    public LiveData<Event<Request>> saveDevice(DeviceModel deviceModel) {
        MutableLiveData<Event<Request>> saveDeviceRequest = new MutableLiveData<>();
        List<Task<?>> tasks = new ArrayList<>();
        // save device model
        DocumentReference deviceModelReference = inventoryReference.document(deviceModel.getDeviceIdentifier());
        tasks.add(deviceModelReference.set(deviceModel.toMap()));

        // save device production
        DeviceProduction deviceProduction = deviceModel.getProductions().get(0);
        deviceProduction.setUniqueDeviceIdentifier(cleanBarcode(deviceProduction.getUniqueDeviceIdentifier()));
        // if udi and di are same we make udi unique so other scans do not overwrite (hibcc)
        if (deviceProduction.getUniqueDeviceIdentifier().equals(deviceModel.getDeviceIdentifier())) {
            String expirationDate = deviceProduction.getExpirationDate();
            String mmyyString = expirationDate.substring(5, 7) + expirationDate.substring(2, 4);
            String newUDI = deviceProduction.getUniqueDeviceIdentifier() + "$$" + mmyyString + deviceProduction.getLotNumber();
            deviceProduction.setUniqueDeviceIdentifier(newUDI);
        }

        // save shipment information if present
        if (deviceModel.getShipment() != null) {
            DocumentReference shipmentDocumentReference = shipmentReference.document(deviceModel.getShipment().getId());
            tasks.add(shipmentDocumentReference.update("received_quantity", FieldValue.increment(deviceProduction.getQuantity())));
            if (deviceModel.getShipment().getReceivedQuantity() + deviceProduction.getQuantity() >= deviceModel.getShipment().getShippedQuantity()) {
                tasks.add(shipmentDocumentReference.update("received",true));
            }
        }

        DocumentReference deviceProductionReference = deviceModelReference.collection("udis").document(deviceProduction.getUniqueDeviceIdentifier());
        tasks.add(deviceProductionReference.set(deviceProduction.toMap(), SetOptions.merge()));

//        if (deviceProduction.getCosts().size() != 0) {
//            Cost cost = deviceProduction.getCosts().get(0);
//            cost.setUser(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getEmail());
//            tasks.add(deviceProductionReference.collection("equipment_cost").add(cost));
//        }

        // update device_type collection count field
        tasks.add(hospitalReference.update("device_types", FieldValue.arrayUnion(deviceModel.getEquipmentType())));

        Tasks.whenAllComplete(tasks).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                saveDeviceRequest.setValue(new Event(new Request(null, Request.Status.SUCCESS)));
            } else {
                // TODO make resource error string
                saveDeviceRequest.setValue(new Event(new Request(null, Request.Status.ERROR)));
            }
        });

        return saveDeviceRequest;
    }

    /**
     * Gets the current device in firestore if it exists. The udi given will be parsed to get its di.
     * @param temp_udi the udi that is scanned (cleaned before usage inside function).
     * @return The DeviceModel information that is in the database, if the device production information
     * exists it will be stored in the list of production in the DeviceModel.
     */
    public List<LiveData<Resource<DeviceModel>>> autoPopulateFromDatabaseAndShipment(final String temp_udi) {
        String udi = cleanBarcode(temp_udi);
        MutableLiveData<Resource<DeviceModel>> deviceLiveData = new MutableLiveData<>();
        MutableLiveData<Resource<DeviceModel>> shippedLiveData = new MutableLiveData<>();
        List<LiveData<Resource<DeviceModel>>> liveData = new ArrayList<>();
        liveData.add(deviceLiveData);
        liveData.add(shippedLiveData);
        deviceLiveData.setValue(new Resource<>(null, new Request(null,Request.Status.LOADING)));
        shippedLiveData.setValue(new Resource<>(null, new Request(null, Request.Status.LOADING)));
        String hibccDi = extractHibcc(udi);
        if (hibccDi == null) {
            accessGUDIDAPI.getParseUdiResponse(udi).enqueue(new Callback<ParseUDIResponse>() {
                @Override
                public void onResponse(Call<ParseUDIResponse> call, retrofit2.Response<ParseUDIResponse> response) {
                    String deviceModelId;
                    String deviceProductionId;
                    if (response.isSuccessful()) {
                        deviceModelId = Objects.requireNonNull(response.body()).getDi();
                        deviceProductionId = response.body().getUdi();

                    } else {
                        deviceModelId = udi;
                        deviceProductionId = udi;
                    }
                    getAutoPopulatedDeviceFromFirestore(inventoryReference,deviceModelId,deviceProductionId,deviceLiveData,null);
                    getDeviceFromShipment(deviceModelId,deviceProductionId,shippedLiveData);
                }

                @Override
                public void onFailure(Call<ParseUDIResponse> call, Throwable t) {
                    Log.e(TAG, "onFailure: ", t);
                    deviceLiveData.setValue(new Resource<>(null, new Request(R.string.error_something_wrong, Request.Status.ERROR)));
                }
            });
        } else {
            // if it is in hibcc format
            getAutoPopulatedDeviceFromFirestore(inventoryReference,udi,udi,deviceLiveData,null);
            getDeviceFromShipment(udi,udi,shippedLiveData);
        }

        return liveData;
    }

    public LiveData<Resource<DeviceModel>> autoPopulateFromDatabase(final String temp_udi) {
        String udi = cleanBarcode(temp_udi);
        MutableLiveData<Resource<DeviceModel>> deviceLiveData = new MutableLiveData<>();
        deviceLiveData.setValue(new Resource<>(null, new Request(null,Request.Status.LOADING)));
        String hibccDi = extractHibcc(udi);
        if (hibccDi == null) {
            accessGUDIDAPI.getParseUdiResponse(udi).enqueue(new Callback<ParseUDIResponse>() {
                @Override
                public void onResponse(Call<ParseUDIResponse> call, retrofit2.Response<ParseUDIResponse> response) {
                    String deviceModelId;
                    String deviceProductionId;
                    if (response.isSuccessful()) {
                        deviceModelId = Objects.requireNonNull(response.body()).getDi();
                        deviceProductionId = response.body().getUdi();
                    } else {
                        deviceModelId = udi;
                        deviceProductionId = udi;
                    }
                    getAutoPopulatedDeviceFromFirestore(inventoryReference,deviceModelId,deviceProductionId,deviceLiveData,null);
                }

                @Override
                public void onFailure(Call<ParseUDIResponse> call, Throwable t) {
                    Log.e(TAG, "onFailure: ", t);
                    deviceLiveData.setValue(new Resource<>(null, new Request(R.string.error_something_wrong, Request.Status.ERROR)));
                }
            });
        } else {
            // if it is in hibcc format
            getAutoPopulatedDeviceFromFirestore(inventoryReference,udi,udi,deviceLiveData,null);
        }

        return deviceLiveData;
    }
    /**
     * Gets the DeviceModel and DeviceProduction in firestore with given params and updates
     * automatically to local changes to those documents
     * The di and udi are expected to be valid and in firestore
     */
    public LiveData<Resource<DeviceModel>> getDeviceFromFirebase(final String di, final String udi) {
        MutableLiveData<Resource<DeviceModel>> deviceLiveData = new MutableLiveData<>();
        deviceLiveData.setValue(new Resource<>(null, new Request(null, Request.Status.LOADING)));
        AtomicReference<Resource<DeviceModel>> deviceModelAtomicReference = new AtomicReference<>();
        AtomicReference<Resource<DeviceProduction>> deviceProductionAtomicReference = new AtomicReference<>();
        AtomicReference<Resource<List<Cost>>> costsAtomicReference = new AtomicReference<>();
        AtomicReference<Resource<List<Procedure>>> proceduresAtomicReference = new AtomicReference<>();

        Task<?> deviceModelTask = getDeviceModelFromFirestore(inventoryReference,di,deviceModelAtomicReference);
        Task<?> deviceProductionTask = getDeviceProductionFromFirestore(inventoryReference,di,udi,deviceProductionAtomicReference);
//        Task<?> costsTask = getDeviceCostsFromFirestore(di,udi,costsAtomicReference);
        Task<?> proceduresTask = getDeviceProceduresFromFirestore(di,udi,proceduresAtomicReference);

        Tasks.whenAllComplete(deviceModelTask,deviceProductionTask,proceduresTask).addOnCompleteListener(tasks -> {
            if (tasks.isSuccessful()) {
                try {
                    DeviceModel deviceModel = deviceModelAtomicReference.get().getData();
                    DeviceProduction deviceProduction = deviceProductionAtomicReference.get().getData();
//                    List<Cost> costs = costsAtomicReference.get().getData();
//                    deviceProduction.addCosts(costs);
                    List<Procedure> procedures = proceduresAtomicReference.get().getData();
                    deviceProduction.addProcedures(procedures);
                    deviceModel.addDeviceProduction(deviceProduction);
                    deviceLiveData.setValue(new Resource<>(deviceModel, new Request(null, Request.Status.SUCCESS)));
                    listenToDeviceModelFromFirestore(inventoryReference,di,deviceLiveData);
                    listenToDeviceProductionFromFirestore(inventoryReference,di,udi,deviceLiveData);
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

    // Removes parentheses from barcode (udi) for uniform structure
    private String cleanBarcode(final String barcode) {
        StringBuilder ans = new StringBuilder();
        for (int i = 0; i < barcode.length(); i++) {
            char c = barcode.charAt(i);
            if (c != '(' && c != ')') ans.append(c);
        }
        return ans.toString();
    }

    /**
     * Helper method for autoPopulateFromDatabase
     * Retrieves DeviceModel from Firestore given a di and udi
     * Does not get procedures and costs associated with the device
     */
    private void getAutoPopulatedDeviceFromFirestore(CollectionReference inventoryReference, String di, String udi, MutableLiveData<Resource<DeviceModel>> deviceLiveData, Shipment shipment) {
        AtomicReference<Resource<DeviceModel>> deviceModelAtomicReference = new AtomicReference<>();
        AtomicReference<Resource<DeviceProduction>> deviceProductionAtomicReference = new AtomicReference<>();

        Task<?> deviceModelTask = getDeviceModelFromFirestore(inventoryReference,di,deviceModelAtomicReference);
        Task<?> deviceProductionTask = getDeviceProductionFromFirestore(inventoryReference,di,udi,deviceProductionAtomicReference);

        Tasks.whenAllComplete(deviceModelTask,deviceProductionTask).addOnCompleteListener(tasks -> {
            Resource<DeviceModel> deviceModelResource = deviceModelAtomicReference.get();
            Resource<DeviceProduction> deviceProductionResource = deviceProductionAtomicReference.get();
            if (deviceModelResource.getRequest().getStatus() == Request.Status.SUCCESS) {
                DeviceModel deviceModel = deviceModelResource.getData();
                deviceModel.setShipment(shipment);
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

    private void getDeviceFromShipment(String di,String udi,MutableLiveData<Resource<DeviceModel>> shippedDeviceLiveData) {
        Task<QuerySnapshot> shipmentTask = shipmentReference.whereEqualTo("udi",udi).whereEqualTo("di",di).whereEqualTo("received",false).get();
        shipmentTask.addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null && !task.getResult().isEmpty()) {
                Shipment shipment = task.getResult().getDocuments().get(0).toObject(Shipment.class);
                DocumentReference sourceHospitalReference = FirestoreReferences.getEntityReference(networkReference,shipment.getSourceHospitalId());
                CollectionReference sourceInventoryReference = FirestoreReferences.getInventoryReference(sourceHospitalReference);
                getAutoPopulatedDeviceFromFirestore(sourceInventoryReference,shipment.getDi(),shipment.getUdi(),shippedDeviceLiveData,shipment);
            } else {
                shippedDeviceLiveData.setValue(new Resource<>(null,new Request(R.string.error_something_wrong,Request.Status.ERROR)));
            }
        });
    }

    private Task<?> getDeviceModelFromFirestore(CollectionReference inventoryReference, final String di, final AtomicReference<Resource<DeviceModel>> deviceModelAtomicReference) {
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

    private void listenToDeviceModelFromFirestore(CollectionReference inventoryReference, final String di, final MutableLiveData<Resource<DeviceModel>> deviceModelLiveData) {
        if (deviceModelListenerRegistration != null ){
            Log.d(TAG, "Device model already has listener");
            return;
        }
        final DocumentReference deviceModelReference = inventoryReference.document(di);
        deviceModelListenerRegistration = deviceModelReference.addSnapshotListener(((snapshot, e) -> {
            if (e != null || snapshot == null || !snapshot.exists() || snapshot.getData() == null) {
                // set live data to error
                Log.e(TAG,"Device Model listen failed", e);
                deviceModelLiveData.setValue(new Resource<>(null, new Request(R.string.error_something_wrong, Request.Status.ERROR)));
                return;
            }
            // listen to only local changes
            if (snapshot.getMetadata().hasPendingWrites()) {
                DeviceModel deviceModel = Objects.requireNonNull(deviceModelLiveData.getValue()).getData();
                deviceModel.fromMap(snapshot.getData());
                deviceModelLiveData.setValue(new Resource<>(deviceModel,new Request(null, Request.Status.SUCCESS)));
            }
        }));
    }

    private Task<?> getDeviceProductionFromFirestore(CollectionReference inventoryReference, final String di, final String udi, final AtomicReference<Resource<DeviceProduction>> deviceProductionAtomicReference) {
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

    private void listenToDeviceProductionFromFirestore(CollectionReference inventoryReference, final String di, final String udi, final MutableLiveData<Resource<DeviceModel>> deviceModelLiveData) {
        if (deviceProductionListenerRegistration != null ){
            Log.d(TAG, "Device production already has listener");
            return;
        }
        final DocumentReference deviceProductionReference = inventoryReference.document(di).collection("udis").document(udi);
        deviceProductionListenerRegistration = deviceProductionReference.addSnapshotListener(((snapshot, e) -> {
            if (e != null || snapshot == null || !snapshot.exists() || snapshot.getData() == null) {
                // set live data to error
                Log.e(TAG,"Device Model listen failed", e);
                deviceModelLiveData.setValue(new Resource<>(null, new Request(R.string.error_something_wrong, Request.Status.ERROR)));
                return;
            }
            // listen to only local changes
            if (snapshot.getMetadata().hasPendingWrites()) {
                DeviceModel deviceModel = Objects.requireNonNull(deviceModelLiveData.getValue()).getData();
                DeviceProduction deviceProduction = deviceModel.getProductions().get(0);
                deviceProduction.fromMap(snapshot.getData());
                deviceModelLiveData.setValue(new Resource<>(deviceModel,new Request(null, Request.Status.SUCCESS)));
            }
        }));
    }

//    private Task<?> getDeviceCostsFromFirestore(final String di, final String udi, final AtomicReference<Resource<List<Cost>>> costsAtomicReference) {
//        Task<QuerySnapshot> costsTask = inventoryReference.document(di).collection("udis").document(udi).collection("equipment_cost").get();
//        costsTask.addOnCompleteListener(task -> {
//            if (task.isSuccessful()) {
//                QuerySnapshot costSnapshots = task.getResult();
//                List<Cost> costs = new ArrayList<>();
//                for (QueryDocumentSnapshot documentSnapshot : costSnapshots) {
//                    costs.add(documentSnapshot.toObject(Cost.class));
//                }
//                costsAtomicReference.set(new Resource<>(costs,new Request(null, Request.Status.SUCCESS)));
//            } else {
//                costsAtomicReference.set(new Resource<>(null,new Request(R.string.error_something_wrong, Request.Status.ERROR)));
//            }
//        });
//        return costsTask;
//    }

    private Task<?> getDeviceProceduresFromFirestore(final String di, final String udi, final AtomicReference<Resource<List<Procedure>>> proceduresAtomicReference) {
        Task<QuerySnapshot> proceduresTask = proceduresReference.whereArrayContains("udis",udi).get();
        proceduresTask.addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
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
     * @param temp_udi the udi that is scanned (cleaned before usage inside function).
     * @return The DeviceModel information that is in the database, the device production information
     * will be stored in the list of production in the DeviceModel
     */
    public LiveData<Resource<DeviceModel>> autoPopulateFromGUDID(final String temp_udi) {
        String udi = cleanBarcode(temp_udi);
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
