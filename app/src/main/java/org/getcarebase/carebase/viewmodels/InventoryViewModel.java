package org.getcarebase.carebase.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import org.getcarebase.carebase.models.DeviceModel;
import org.getcarebase.carebase.models.User;
import org.getcarebase.carebase.repositories.FirebaseAuthRepository;
import org.getcarebase.carebase.repositories.InventoryRepository;
import org.getcarebase.carebase.utils.Request;
import org.getcarebase.carebase.utils.Resource;
import org.getcarebase.carebase.utils.SingleEventMediatorLiveData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class InventoryViewModel extends ViewModel {
    private final InventoryRepository inventoryRepository;
    private final FirebaseAuthRepository authRepository;

    private final SingleEventMediatorLiveData<List<DeviceModel>> inventoryLiveData = new SingleEventMediatorLiveData<>();
    private final LiveData<Resource<Map<String,List<DeviceModel>>>> categoricalInventoryLiveData = Transformations.map(inventoryLiveData.getLiveData(), this::sortInventoryByCategories);
    private LiveData<Resource<User>> userLiveData;

    public InventoryViewModel() {
        inventoryRepository = new InventoryRepository();
        authRepository = new FirebaseAuthRepository();
    }

    public LiveData<Resource<User>> getUserLiveData() {
        if (userLiveData == null) {
            userLiveData = authRepository.getUser();
        }
        return userLiveData;
    }

    public void signOut() {
        authRepository.signOut();
    }

    /**
     * Updates inventoryLiveData with the LiveData from the repository
     */
    public void loadInventory() {
        inventoryLiveData.addSource(inventoryRepository.getDeviceModelListForHospital(Objects.requireNonNull(userLiveData.getValue()).getData()));
    }

    public LiveData<Resource<Map<String, List<DeviceModel>>>> getCategoricalInventoryLiveData() {
        return categoricalInventoryLiveData;
    }

    /**
     * Sorts a list of devices from a repository call into a map of categories and devices
     * @param listResource the result of the repository call
     * @return the converted Resource
     */
    private Resource<Map<String,List<DeviceModel>>> sortInventoryByCategories(Resource<List<DeviceModel>> listResource) {
        if (listResource.getRequest().getStatus() == Request.Status.ERROR
                || listResource.getRequest().getStatus() == Request.Status.LOADING) {
            return new Resource<>(null,listResource.getRequest());
        }

        Map<String,List<DeviceModel>> categoricalInventoryMap = new HashMap<>();
        List<DeviceModel> deviceModelList = listResource.getData();
        for (DeviceModel deviceModel : deviceModelList) {
            List<DeviceModel> category = categoricalInventoryMap.getOrDefault(deviceModel.getEquipmentType(), new ArrayList<>());
            category.add(deviceModel);
            categoricalInventoryMap.putIfAbsent(deviceModel.getEquipmentType(), category);
        }
        return new Resource<>(categoricalInventoryMap,new Request(null, Request.Status.SUCCESS));
    }
}
