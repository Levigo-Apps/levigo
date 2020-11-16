package org.getcarebase.carebase.repositories;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.getcarebase.carebase.R;
import org.getcarebase.carebase.models.DeviceModel;
import org.getcarebase.carebase.models.DeviceProduction;
import org.getcarebase.carebase.models.User;
import org.getcarebase.carebase.utils.Request;
import org.getcarebase.carebase.utils.Resource;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * This repository is concerned with retrieving inventory data.
 * Also maintains the list of the devices until it is cleaned up by garbage collector.
 * All other components will only use this list defined in this instance of the repository
 */
public class InventoryRepository {
    private final FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    private final List<DeviceModel> deviceModelList = new ArrayList<>();

    /**
     * Populates the deviceModelList with all the models with all of it productions
     * @param user The currently signed in user
     * @return LiveData : Holds the resource (status) of the reference to the list
     */
    public LiveData<Resource<List<DeviceModel>>> getDeviceModelMapForHospital(User user) {
        deviceModelList.clear();
        Resource<List<DeviceModel>> deviceModelListResource = new Resource<>(deviceModelList, new Request(null, Request.Status.LOADING));
        final MutableLiveData<Resource<List<DeviceModel>>> deviceModelListLiveData = new MutableLiveData<>(deviceModelListResource);
        String inventoryRefUrl = "networks/" + user.getNetworkId() + "/hospitals/"
                + user.getHospitalId() + "/departments/default_department/dis";

        firestore.collection(inventoryRefUrl).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                List<Task<QuerySnapshot>> tasks = new ArrayList<>();
                for (QueryDocumentSnapshot modelSnapshot : Objects.requireNonNull(task.getResult())) {
                    final DeviceModel deviceModel = new DeviceModel(modelSnapshot.getData());

                    Task<QuerySnapshot> productionTask = modelSnapshot.getReference().collection("udis").get();
                    tasks.add(productionTask);
                    productionTask.addOnCompleteListener(completeProductionTask -> {
                        for (QueryDocumentSnapshot productionSnapshot : Objects.requireNonNull(completeProductionTask.getResult())) {
                            if (completeProductionTask.isSuccessful()) {
                                DeviceProduction deviceProduction = new DeviceProduction(productionSnapshot.getData());
                                deviceModel.addDeviceProduction(deviceProduction);
                            } else {
                                // error if getting all productions for a model fails
                                deviceModelListLiveData.setValue(new Resource<>(null, new Request(R.string.error_something_wrong, Request.Status.ERROR)));
                            }
                        }
                    });
                    deviceModelList.add(deviceModel);
                }
                Tasks.whenAllSuccess(tasks).addOnCompleteListener(task1 -> deviceModelListLiveData.setValue(new Resource<>(deviceModelList, new Request(null, Request.Status.SUCCESS))));
            } else {
                // error if getting all models fails
                deviceModelListLiveData.setValue(new Resource<>(null, new Request(R.string.error_something_wrong, Request.Status.ERROR)));
            }
        });
        return deviceModelListLiveData;
    }

}
