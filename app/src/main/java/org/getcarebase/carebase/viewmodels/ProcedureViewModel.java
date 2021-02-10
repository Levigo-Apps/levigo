package org.getcarebase.carebase.viewmodels;

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
import org.getcarebase.carebase.repositories.ProcedureRepository;
import org.getcarebase.carebase.utils.Request;
import org.getcarebase.carebase.utils.Resource;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ProcedureViewModel extends ViewModel {
    private final List<DeviceUsage> devicesUsed = new ArrayList<>();

    // represents the current page that the user is on
    // 0 - entering procedure details
    // 1 - recording devices used in the procedures
    private final MutableLiveData<Integer> currentStep = new MutableLiveData<>(0);

    private Procedure procedureDetails;

    private DeviceRepository deviceRepository;
    private ProcedureRepository procedureRepository;
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
            String name = resource.getData().getName();

            int currentProductionQuantity = resource.getData().getProductions().get(0).getQuantity();
            int currentModelQuantity = resource.getData().getQuantity();
            DeviceUsage deviceUsage = new DeviceUsage(di, udi, name, currentProductionQuantity, currentModelQuantity);
            devicesUsed.add(deviceUsage);
        }
        return resource.getRequest();
    });

    private final MutableLiveData<Procedure> saveProcedureLiveData = new MutableLiveData<>();
    private final LiveData<Request> saveProcedureRequestLiveData = Transformations.switchMap(saveProcedureLiveData, procedures -> procedureRepository.saveProcedure(procedures));

    public ProcedureViewModel() {
        authRepository = new FirebaseAuthRepository();
    }

    public LiveData<Integer> getCurrentStep() {
        return currentStep;
    }

    public void goToProcedureDetails() {
        currentStep.setValue(0);
    }

    public void goToDeviceUsed() {
        currentStep.setValue(1);
    }

    public void goToInventory() {
        currentStep.setValue(-1);
    }

    public LiveData<Resource<User>> getUserLiveData() {
        if (userLiveData == null) {
            userLiveData = authRepository.getUser();
        }
        return userLiveData;
    }

    public void setupRepositories() {
        User user = Objects.requireNonNull(userLiveData.getValue()).getData();
        deviceRepository = new DeviceRepository(user.getNetworkId(), user.getHospitalId());
        procedureRepository = new ProcedureRepository(user.getNetworkId(), user.getHospitalId());
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

    public void retryDeviceUsed() {
        Objects.requireNonNull(addDeviceLiveData.getValue());
        addDeviceLiveData.setValue(addDeviceLiveData.getValue());
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
        procedureDetails.setDeviceUsages(devicesUsed);
        saveProcedureLiveData.setValue(procedureDetails);
    }
}
