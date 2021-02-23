package org.getcarebase.carebase.viewmodels;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.google.mlkit.md.barcodedetection.BarcodeField;
import com.google.mlkit.md.barcodedetection.BarcodeResultFragment;
import com.google.mlkit.md.camera.WorkflowModel;

import org.getcarebase.carebase.R;
import org.getcarebase.carebase.activities.Main.ScanningActivity;
import org.getcarebase.carebase.models.DeviceModel;
import org.getcarebase.carebase.models.User;
import org.getcarebase.carebase.repositories.DeviceRepository;
import org.getcarebase.carebase.repositories.FirebaseAuthRepository;
import org.getcarebase.carebase.utils.DeviceSourceObserver;
import org.getcarebase.carebase.utils.Request;
import org.getcarebase.carebase.utils.Resource;

import java.util.List;

public class ScanningViewModel extends ViewModel {
    private final static String TAG = ScanningViewModel.class.getName();
    private final FirebaseAuthRepository authRepository = new FirebaseAuthRepository();
    private DeviceRepository deviceRepository;

    private WorkflowModel workflowModel;
    private WorkflowModel.WorkflowState currentWorkflowState;
    private LiveData<Resource<DeviceModel>> autoPopulatedLiveData;

    public void setDeviceRepository(final String networkId, final String hospitalId) {
        deviceRepository = new DeviceRepository(networkId,hospitalId);
    }

    public void setWorkflowModel(WorkflowModel workflowModel) {
        this.workflowModel = workflowModel;
        autoPopulatedLiveData = Transformations.switchMap(workflowModel.getDetectedBarcode(),barcode -> {
            if (barcode != null && barcode.getDisplayValue() != null) {
                MediatorLiveData<Resource<DeviceModel>> finalLiveData = new MediatorLiveData<>();
                autoPopulateScannedBarcode(barcode.getDisplayValue(),finalLiveData);
                return finalLiveData;
            }
            return new MutableLiveData<>(new Resource<>(null,new Request(R.string.error_something_wrong,Request.Status.ERROR)));
        });
    }

    public void setCurrentWorkflowState(WorkflowModel.WorkflowState currentWorkflowState) {
        this.currentWorkflowState = currentWorkflowState;
    }

    public LiveData<Resource<User>> getUserLiveData() {
        return authRepository.getUser();
    }

    public LiveData<Resource<DeviceModel>> getAutoPopulatedLiveData() {
        return autoPopulatedLiveData;
    }

    public WorkflowModel getWorkflowModel() {
        return workflowModel;
    }

    public WorkflowModel.WorkflowState getCurrentWorkflowState() {
        return currentWorkflowState;
    }

    private void autoPopulateScannedBarcode(String barcode, MediatorLiveData<Resource<DeviceModel>> finalLiveData) {
        finalLiveData.setValue(new Resource<>(null,new Request(null,Request.Status.LOADING)));
        List<LiveData<Resource<DeviceModel>>> databaseSources = deviceRepository.autoPopulateFromDatabaseAndShipment(barcode);
        LiveData<Resource<DeviceModel>> inventorySource = databaseSources.get(0);
        LiveData<Resource<DeviceModel>> shippedSource = databaseSources.get(1);
        LiveData<Resource<DeviceModel>> gudidSource = deviceRepository.autoPopulateFromGUDID(barcode);
        DeviceSourceObserver deviceSourceObserver = new DeviceSourceObserver(inventorySource,shippedSource,gudidSource,finalLiveData);
        finalLiveData.addSource(inventorySource,deviceSourceObserver);
        finalLiveData.addSource(shippedSource,deviceSourceObserver);
        finalLiveData.addSource(gudidSource,deviceSourceObserver);
    }
}
