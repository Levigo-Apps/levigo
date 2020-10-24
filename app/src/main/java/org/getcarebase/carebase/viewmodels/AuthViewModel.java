package org.getcarebase.carebase.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import org.getcarebase.carebase.models.User;
import org.getcarebase.carebase.repositories.FirebaseAuthRepository;
import org.getcarebase.carebase.utils.Resource;

/**
 * An authentication abstraction so that relevant views do not directly deal with data layer
 * and so the authentication can outlive activity/fragment lifecycle events
 */
public class AuthViewModel extends ViewModel {

    private final FirebaseAuthRepository authRepository;

    public AuthViewModel() {
        authRepository = new FirebaseAuthRepository();
    }

    public LiveData<Resource<User>> signInWithEmailAndPassword(final String email, final String password) {
        return authRepository.firebaseSignInWithEmailAndPassword(email,password);
    }

    public LiveData<Resource<User>> getUser() {
        return authRepository.getUser();
    }

    public LiveData<Resource<Object>> resetPasswordWithEmail(final String email) {
        return authRepository.resetPasswordWithEmail(email);
    }





}
