package org.getcarebase.carebase.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModel;

import org.getcarebase.carebase.models.DeviceModel;
import org.getcarebase.carebase.models.User;
import org.getcarebase.carebase.repositories.DeviceRepository;
import org.getcarebase.carebase.repositories.FirebaseAuthRepository;
import org.getcarebase.carebase.utils.Request;
import org.getcarebase.carebase.utils.Resource;

import java.util.Objects;

public class DeviceViewModel extends ViewModel {
    private DeviceRepository deviceRepository;
    private final FirebaseAuthRepository authRepository;

    private LiveData<Resource<User>> userLiveData;
    private LiveData<Resource<DeviceModel>> deviceLiveData;

    public DeviceViewModel() {
        authRepository = new FirebaseAuthRepository();
    }

    public LiveData<Resource<User>> getUserLiveData() {
        if (userLiveData == null) {
            userLiveData = authRepository.getUser();
        }
        return userLiveData;
    }

    public void setUpInventoryConnection() {
        User user = Objects.requireNonNull(userLiveData.getValue()).getData();
        deviceRepository = new DeviceRepository(user.getNetworkId(), user.getHospitalId());
    }
    
    public void autoPopulateFromScannedBarcode(String udi) {

    }
}
