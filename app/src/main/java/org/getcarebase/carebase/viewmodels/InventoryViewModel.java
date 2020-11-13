package org.getcarebase.carebase.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import org.getcarebase.carebase.models.DeviceModel;
import org.getcarebase.carebase.models.User;
import org.getcarebase.carebase.repositories.InventoryRepository;
import org.getcarebase.carebase.utils.Resource;

import java.util.Map;

public class InventoryViewModel extends ViewModel {
    private final InventoryRepository inventoryRepository;
    private LiveData<Resource<Map<String, DeviceModel>>> inventoryLiveData;

    public InventoryViewModel() {
        inventoryRepository = new InventoryRepository();
    }

    public LiveData<Resource<Map<String, DeviceModel>>> loadInventory(User user) {
        inventoryLiveData = inventoryRepository.getDeviceModelMapForHospital(user);
        return inventoryLiveData;
    }
}
