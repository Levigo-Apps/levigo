package org.getcarebase.carebase.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import org.getcarebase.carebase.models.Procedure;
import org.getcarebase.carebase.models.User;
import org.getcarebase.carebase.repositories.FirebaseAuthRepository;
import org.getcarebase.carebase.repositories.ProcedureRepository;
import org.getcarebase.carebase.utils.Resource;
import org.getcarebase.carebase.utils.SingleEventMediatorLiveData;

public class ProcedureDetailViewModel extends ViewModel {

    private ProcedureRepository procedureRepository;
    private final FirebaseAuthRepository authRepository;

    private final SingleEventMediatorLiveData<User> userLiveData = new SingleEventMediatorLiveData<>();
    private final SingleEventMediatorLiveData<Procedure> procedureLiveData = new SingleEventMediatorLiveData<>();

    public ProcedureDetailViewModel() {
        authRepository = new FirebaseAuthRepository();
    }

    public void setProcedureRepository(String networkId, String hospitalId) {
        procedureRepository = new ProcedureRepository(networkId,hospitalId);
    }

    public void getUser() {
        userLiveData.addSource(authRepository.getUser());
    }

    public LiveData<Resource<User>> getUserLiveData() {
        return userLiveData.getLiveData();
    }

    public void getProcedure(String procedureId) {
        procedureLiveData.addSource(procedureRepository.getProcedure(procedureId));
    }

    public LiveData<Resource<Procedure>> getProcedureLiveData() {
        return procedureLiveData.getLiveData();
    }

}
