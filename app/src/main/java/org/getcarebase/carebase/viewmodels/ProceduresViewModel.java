package org.getcarebase.carebase.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import org.getcarebase.carebase.models.Procedure;
import org.getcarebase.carebase.models.User;
import org.getcarebase.carebase.repositories.FirebaseAuthRepository;
import org.getcarebase.carebase.repositories.ProcedureRepository;
import org.getcarebase.carebase.utils.Resource;
import org.getcarebase.carebase.utils.SingleEventMediatorLiveData;

import java.util.List;
import java.util.Objects;

public class ProceduresViewModel extends ViewModel {
    private final FirebaseAuthRepository authRepository;
    private ProcedureRepository procedureRepository;

    private final SingleEventMediatorLiveData<User> userLiveData = new SingleEventMediatorLiveData<>();
    private final SingleEventMediatorLiveData<List<Procedure>> proceduresLiveData = new SingleEventMediatorLiveData<>();

    public ProceduresViewModel() {
        authRepository = new FirebaseAuthRepository();
    }

    public LiveData<Resource<User>> getUserLiveData() {
        userLiveData.addSource(authRepository.getUser());
        return userLiveData.getLiveData();
    }

    public void setupRepository() {
        User user = Objects.requireNonNull(userLiveData.getLiveData().getValue().getData());
        procedureRepository = new ProcedureRepository(user.getNetworkId(),user.getEntityId());
    }

    public void loadProcedures(boolean onRefresh) {
        proceduresLiveData.addSource(procedureRepository.getProcedures(onRefresh));
    }

    public LiveData<Resource<List<Procedure>>> getProceduresLiveData() {
        return proceduresLiveData.getLiveData();
    }
}
