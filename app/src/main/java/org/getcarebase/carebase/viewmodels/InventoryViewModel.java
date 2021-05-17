package org.getcarebase.carebase.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import org.getcarebase.carebase.models.DeviceModel;
import org.getcarebase.carebase.models.Entity;
import org.getcarebase.carebase.models.Procedure;
import org.getcarebase.carebase.models.User;
import org.getcarebase.carebase.repositories.EntityRepository;
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
    private final EntityRepository entityRepository;

    private LiveData<Resource<User>> userLiveData;

    private final SingleEventMediatorLiveData<List<String>> typeListLiveData = new SingleEventMediatorLiveData<>();
    private final SingleEventMediatorLiveData<List<DeviceModel>> deviceModelLiveData = new SingleEventMediatorLiveData<>();

    public InventoryViewModel() {
        inventoryRepository = new InventoryRepository();
        authRepository = new FirebaseAuthRepository();
        entityRepository = new EntityRepository();
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

    public void loadTypeList() {
        typeListLiveData.addSource(inventoryRepository.getAllTypes(Objects.requireNonNull(userLiveData.getValue()).getData()));
    }

    public void loadDeviceModel(String type) {
        deviceModelLiveData.addSource(inventoryRepository.getDevicesWithType(Objects.requireNonNull(userLiveData.getValue()).getData(), type));
    }

    public LiveData<Resource<List<String>>> getTypeListLiveData() {
        return typeListLiveData.getLiveData();
    }

    public LiveData<Resource<List<DeviceModel>>> getDeviceModelListWithTypeLiveData(String type) {
        return deviceModelLiveData.getLiveData();
    }

    public LiveData<Resource<Entity>> getEntityLiveData() {
        return entityRepository.getEntity(Objects.requireNonNull(userLiveData.getValue()).getData());
    }
}
