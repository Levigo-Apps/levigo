package org.getcarebase.carebase.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import org.getcarebase.carebase.models.PendingDevice;
import org.getcarebase.carebase.models.User;
import org.getcarebase.carebase.repositories.FirebaseAuthRepository;
import org.getcarebase.carebase.repositories.PendingDeviceRepository;
import org.getcarebase.carebase.utils.Request;
import org.getcarebase.carebase.utils.Resource;
import org.getcarebase.carebase.utils.SingleEventMediatorLiveData;

import java.util.List;
import java.util.Objects;

public class PendingDeviceViewModel extends ViewModel {
    private PendingDeviceRepository pendingDeviceRepository;
    private final FirebaseAuthRepository authRepository;

    private LiveData<Resource<User>> userLiveData;

    private final SingleEventMediatorLiveData<List<PendingDevice>> pendingDevicesLiveData = new SingleEventMediatorLiveData<>();

    public PendingDeviceViewModel() {
        authRepository = new FirebaseAuthRepository();
    }

    public LiveData<Resource<User>> getUserLiveData() {
        if (userLiveData == null) {
            userLiveData = authRepository.getUser();
        }
        return userLiveData;
    }

    public void setupPendingDeviceRepository() {
        User user = Objects.requireNonNull(userLiveData.getValue()).getData();
        pendingDeviceRepository = new PendingDeviceRepository(user.getNetworkId(), user.getHospitalId());
    }

    public void loadPendingDevices() {
        pendingDevicesLiveData.addSource(pendingDeviceRepository.getPendingDevices());
    }

    public LiveData<Resource<List<PendingDevice>>> getPendingDevicesLiveData() {
        return pendingDevicesLiveData.getLiveData();
    }

    public void savePendingDevice(PendingDevice pendingDevice) {
        pendingDeviceRepository.savePendingDevice(pendingDevice);
    }
}
