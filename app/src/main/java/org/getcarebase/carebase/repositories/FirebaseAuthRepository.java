package org.getcarebase.carebase.repositories;

import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.getcarebase.carebase.R;
import org.getcarebase.carebase.activities.Main.MainActivity;
import org.getcarebase.carebase.models.InvitationCode;
import org.getcarebase.carebase.models.User;
import org.getcarebase.carebase.utils.Resource;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * This repository is concerned with firebase authentication actions
 */
public class FirebaseAuthRepository {
    private final FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    private final FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    private final String TAG = getClass().getSimpleName();

    /**
     * Signs into firebase auth with email and password. If there are errors they will be logged
     * and provided in the resource objects. It is expected that the caller handle these errors.
     * @param email the user's email
     * @param password the user's password
     * @return an observable object that will carry the user object
     */
    public LiveData<Resource<User>> firebaseSignInWithEmailAndPassword(final String email, final String password) {
        final MutableLiveData<Resource<User>> userMutableLiveData = new MutableLiveData<>();
        firebaseAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                    if (firebaseUser == null) {
                        Resource<User> userResource = new Resource<>(null,R.string.error_something_wrong, Resource.Status.ERROR);
                        Log.e(TAG,"Sign in failed",new NullPointerException());
                        userMutableLiveData.setValue(userResource);
                        return;
                    }
                    getUserFromFirebaseUser(firebaseUser,userMutableLiveData);
                }
                else {
                    Integer errorResourceString = null;
                    try {
                        throw task.getException();
                    } catch (FirebaseAuthException e) {
                        switch (e.getErrorCode()) {
                            case "ERROR_INVALID_EMAIL":
                                errorResourceString = R.string.error_invalid_email_format;
                                break;

                            case "ERROR_WRONG_PASSWORD": case "ERROR_USER_NOT_FOUND":
                                errorResourceString = R.string.error_invalid_email_or_password;
                                break;
                        }

                    } catch (FirebaseTooManyRequestsException e) {
                        errorResourceString = R.string.error_too_many_attempts;
                    } catch (Exception e) {
                        errorResourceString = R.string.error_something_wrong;
                        Log.e(TAG,"Sign in failed", e);
                        FirebaseCrashlytics.getInstance().recordException(e);
                    }
                    Resource<User> userResource = new Resource<>(null,errorResourceString,Resource.Status.ERROR);
                    userMutableLiveData.setValue(userResource);
                }
            }
        });
        return userMutableLiveData;
    }

    /**
     * tries to get currently signed in user
     * @return an observable object that will carry the user object
     */
    public LiveData<Resource<User>> getUser() {
        MutableLiveData<Resource<User>> userMutableLiveData = new MutableLiveData<>();
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        if (firebaseUser != null) {
            getUserFromFirebaseUser(firebaseUser,userMutableLiveData);
        }
        else {
            Log.e(TAG,"User is not signed in");
            userMutableLiveData.setValue(new Resource<User>(null,null, Resource.Status.ERROR));
        }
        return userMutableLiveData;
    }

    /**
     * sends an email to reset the the user's password if that email is associated with
     * an account
     * @param email the address the email should be sent to
     * @return the status of the request
     */
    public LiveData<Resource<Object>> resetPasswordWithEmail(final String email) {
        final MutableLiveData<Resource<Object>> result = new MutableLiveData<>();
        firebaseAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        result.setValue(new Resource<>(null,R.string.forgot_password_email_sent, Resource.Status.SUCCESS));
                    }
                });
        return result;
    }

    /**
     * checks if the given invitationCodeString is valid
     * @param invitationCodeString The user provided invitation code
     * @return an observable object that carries the invitation code if it
     * is valid or the error if it is not valid or any error had occurred
     */
    public LiveData<Resource<InvitationCode>> isInvitationCodeValid(final String invitationCodeString) {
        final MutableLiveData<Resource<InvitationCode>> result = new MutableLiveData<>();
        firestore.collection("invitation_codes").document(invitationCodeString).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                if (document.getBoolean("valid")) {
                                    try {
                                        // invitation code found and information about the code will be sent back
                                        String networkId = document.get("network_id").toString();
                                        String networkName = document.get("network_name").toString();
                                        String hospitalId = document.get("hospital_id").toString();
                                        String hospitalName = document.get("hospital_name").toString();

                                        InvitationCode invitationCode = new InvitationCode(invitationCodeString,networkId,networkName,hospitalId,hospitalName,true);
                                        Resource<InvitationCode> request = new Resource<>(invitationCode,null, Resource.Status.SUCCESS);
                                        result.setValue(request);
                                    } catch (NullPointerException e) {
                                        // invitation code data missing fields
                                        FirebaseCrashlytics.getInstance().recordException(e);
                                        result.setValue(new Resource<InvitationCode>(null,R.string.error_something_wrong, Resource.Status.ERROR));
                                    }
                                } else {
                                    // invitation code is invalid - all ready used
                                    result.setValue(new Resource<InvitationCode>(null,R.string.error_something_wrong, Resource.Status.ERROR));
                                }
                            } else {
                                // document for invitation code doesn't exist
                                result.setValue(new Resource<InvitationCode>(null,R.string.error_invalid_invitation, Resource.Status.ERROR));
                            }
                        } else {
                            result.setValue(new Resource<InvitationCode>(null,R.string.error_something_wrong, Resource.Status.ERROR));
                            FirebaseCrashlytics.getInstance().recordException(task.getException());
                        }
                    }
                });
        return result;
    }

    /**
     * creates a new user with give user and password in firebase and in firestore
     * @param email The email of the new user
     * @param password The password of the new user
     * @param invitationCode The invitation code given to the new user
     * @return an observable object that carries the newly created user and information about the
     * request
     */
    public LiveData<Resource<User>> createUserWithInvitationCode(final String email, final String password, final InvitationCode invitationCode) {
        final MutableLiveData<Resource<User>> result = new MutableLiveData<>();
        assert invitationCode != null;
        firebaseAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    String userId = firebaseAuth.getCurrentUser().getUid();

                    // disable invitation code
                    DocumentReference invitationCodeDocumentReference = firestore.collection("invitation_codes")
                            .document(invitationCode.getInvitationCode());
                    invitationCodeDocumentReference.update("valid", false);
                    invitationCodeDocumentReference.update("authorized_user", userId);

                    // create user document in "users" collection
                    User newUser = new User(userId,email,invitationCode);
                    firestore.collection("users").document(userId).set(newUser.toMap());

                    // give user admin access
                    firestore.collection("networks").document(invitationCode.getNetworkId()).update("auth_users." + userId, "editor");
                    Resource<User> newUserResource = new Resource<>(newUser,null, Resource.Status.SUCCESS);
                    result.setValue(newUserResource);
                }
                else {
                    Resource<User> newUserResource = new Resource<>(null,R.string.error_something_wrong, Resource.Status.ERROR);
                    result.setValue(newUserResource);
                }
            }
        });
        return result;
    }


    /**
     * Updates userMutableLiveData with the user data tied with the given firebaseUser
     * @param firebaseUser The firebase auth user object
     * @param userMutableLiveData The object the user should be stored in
     */
    private void getUserFromFirebaseUser(final FirebaseUser firebaseUser, final MutableLiveData<Resource<User>> userMutableLiveData) {
        firestore.collection("users").document(firebaseUser.getUid()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    assert document != null;
                    if (document.exists()) {
                        String networkId = Objects.requireNonNull(document.get("network_id")).toString();
                        String networkName = Objects.requireNonNull(document.get("network_name")).toString();
                        String hospitalId = Objects.requireNonNull(document.get("hospital_id")).toString();
                        String hospitalName = Objects.requireNonNull(document.get("hospital_name")).toString();
                        User user = new User(firebaseUser.getUid(),firebaseUser.getEmail(),hospitalId,hospitalName,networkId,networkName);
                        Resource<User> userResource = new Resource<>(user,null, Resource.Status.SUCCESS);
                        userMutableLiveData.setValue(userResource);
                    }
                    // user document does not exist
                    else {
                        Resource<User> userResource = new Resource<>(null,R.string.error_something_wrong, Resource.Status.ERROR);
                        Log.e(TAG,String.format("User %s does not exist",firebaseUser.getUid()));
                        userMutableLiveData.setValue(userResource);
                    }
                }
                // query failure
                else {
                    Resource<User> userResource = new Resource<>(null,R.string.error_something_wrong, Resource.Status.ERROR);
                    Log.e(TAG,String.format("User %s failed to sign in",firebaseUser.getUid()));
                    userMutableLiveData.setValue(userResource);
                }
            }
        });
    }

}
