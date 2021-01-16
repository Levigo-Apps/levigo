package org.getcarebase.carebase.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModel;

import org.getcarebase.carebase.models.InvitationCode;
import org.getcarebase.carebase.models.User;
import org.getcarebase.carebase.repositories.FirebaseAuthRepository;
import org.getcarebase.carebase.utils.Request;
import org.getcarebase.carebase.utils.Resource;
import org.getcarebase.carebase.utils.SingleEventMediatorLiveData;

/**
 * An authentication abstraction so that relevant views do not directly deal with data layer
 * and so the authentication can outlive activity/fragment lifecycle events
 */
public class AuthViewModel extends ViewModel {

    private final FirebaseAuthRepository authRepository;
    private LiveData<Resource<InvitationCode>> invitationCodeLiveData;
    private final SingleEventMediatorLiveData<User> userLiveData = new SingleEventMediatorLiveData<>();

    public AuthViewModel() {
        authRepository = new FirebaseAuthRepository();
    }

    public void signInWithEmailAndPassword(final String email, final String password) {
        userLiveData.addSource(authRepository.firebaseSignInWithEmailAndPassword(email,password));
    }

    public void getUser() {
        userLiveData.addSource(authRepository.getUser());
    }

    public LiveData<Resource<User>> getUserLiveData() { return userLiveData.getLiveData(); }

    public LiveData<Request> resetPasswordWithEmail(final String email) {
        return authRepository.resetPasswordWithEmail(email);
    }

    public LiveData<Resource<InvitationCode>> isInvitationCodeValid(final String invitationCode) {
        invitationCodeLiveData = authRepository.isInvitationCodeValid(invitationCode);
        return invitationCodeLiveData;
    }

    public void createUser(final String email, final String password) {
        userLiveData.addSource(authRepository.createUserWithInvitationCode(email,password,invitationCodeLiveData.getValue().getData()));
    }
}
