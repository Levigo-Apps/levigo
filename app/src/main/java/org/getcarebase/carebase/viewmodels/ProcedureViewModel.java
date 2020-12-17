package org.getcarebase.carebase.viewmodels;

import android.util.Pair;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import org.getcarebase.carebase.R;
import org.getcarebase.carebase.models.DeviceModel;
import org.getcarebase.carebase.models.DeviceUsage;
import org.getcarebase.carebase.models.Procedure;
import org.getcarebase.carebase.models.User;
import org.getcarebase.carebase.repositories.DeviceRepository;
import org.getcarebase.carebase.repositories.FirebaseAuthRepository;
import org.getcarebase.carebase.utils.Request;
import org.getcarebase.carebase.utils.Resource;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class ProcedureViewModel extends ViewModel {
    private final List<DeviceUsage> devicesUsed = new ArrayList<>();
    // The procedure that is to be saved to all the devices - di and udi are null
    private Procedure procedureDetails;

    private DeviceRepository deviceRepository;
    private final FirebaseAuthRepository authRepository;
    private LiveData<Resource<User>> userLiveData;

    private final MutableLiveData<String> addDeviceLiveData = new MutableLiveData<>();
    private final LiveData<Resource<DeviceModel>> deviceLiveData = Transformations.switchMap(addDeviceLiveData, barcode -> {
        // check if udi is already in device list
        if (devicesUsed.stream().anyMatch(deviceUsage -> barcode.equals(deviceUsage.getUniqueDeviceIdentifier()))) {
            return new MutableLiveData<>(new Resource<>(null,new Request(R.string.error_device_already_entered, Request.Status.ERROR)));
        }
        return deviceRepository.autoPopulateFromDatabase(barcode);
    });
    private final LiveData<Request> addDeviceRequestLiveData = Transformations.map(deviceLiveData, resource -> {
        if (resource.getRequest().getStatus() == Request.Status.SUCCESS) {
            String di = resource.getData().getDeviceIdentifier();
            String udi = resource.getData().getProductions().get(0).getUniqueDeviceIdentifier();
            int currentQuantity = resource.getData().getProductions().get(0).getQuantity();
            DeviceUsage deviceUsage = new DeviceUsage(di,udi,currentQuantity);
            devicesUsed.add(deviceUsage);
        }
        return resource.getRequest();
    });

    private final MutableLiveData<List<Procedure>> saveProcedureLiveData = new MutableLiveData<>();
    private final LiveData<Request> saveProcedureRequestLiveData = Transformations.switchMap(saveProcedureLiveData, procedures -> deviceRepository.saveProcedure(procedures));

    public ProcedureViewModel() {
        authRepository = new FirebaseAuthRepository();
    }

    public LiveData<Resource<User>> getUserLiveData() {
        if (userLiveData == null) {
            userLiveData = authRepository.getUser();
        }
        return userLiveData;
    }

    public void setupDeviceRepository() {
        User user = Objects.requireNonNull(userLiveData.getValue()).getData();
        deviceRepository = new DeviceRepository(user.getNetworkId(), user.getHospitalId());
    }

    public Procedure getProcedureDetails() {
        return procedureDetails;
    }

    public void setProcedureDetails(Procedure procedureDetails) {
        this.procedureDetails = procedureDetails;
    }

    public List<DeviceUsage> getDevicesUsed() {
        return devicesUsed;
    }

    public LiveData<Request> getAddDeviceRequestLiveData() {
        return addDeviceRequestLiveData;
    }

    public void addDeviceUsed(final String barcode) {
        addDeviceLiveData.setValue(barcode);
    }

    public String getScannedDevice() {
        return Objects.requireNonNull(addDeviceLiveData.getValue());
    }

    public LiveData<Request> getSaveProcedureRequestLiveData() {
        return saveProcedureRequestLiveData;
    }

    public void saveProcedure() {
        Objects.requireNonNull(procedureDetails);
        if (devicesUsed.size() == 0) {
            throw new Error("a procedure must have used at least one device");
        }
        List<Procedure> procedures = new ArrayList<>();
        for (DeviceUsage usage : devicesUsed) {
            Procedure procedure = new Procedure(usage,procedureDetails);
            procedures.add(procedure);
        }
        saveProcedureLiveData.setValue(procedures);
    }
}
