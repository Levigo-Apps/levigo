/*
 * Copyright 2020 Levigo Apps
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.levigo.levigoapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * Logs in user
 */

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = LoginActivity.class.getSimpleName();
    private static final int RC_SIGN_IN = 1;

    private boolean clear, signout;
    private FirebaseAuth mAuth;
    private MaterialButton mLogin, mRegister;
    private EditText mEmail, mPassword;
    private MaterialCheckBox mRemember;

    private FirebaseFirestore levigoDb = FirebaseFirestore.getInstance();
    private CollectionReference usersRef = levigoDb.collection("users");


    private String mNetwork, mNetworkName, mSite, mSiteName;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseApp.initializeApp(this);

        mAuth = FirebaseAuth.getInstance();

        clear = true;
        userIsLoggedIn();

        setContentView(R.layout.activity_login);

        mLogin = findViewById(R.id.login_button);
        mRegister = findViewById(R.id.login_register);
        mEmail = findViewById(R.id.login_email);
        mPassword = findViewById(R.id.login_password);
        mRemember = findViewById(R.id.login_remember);

        mLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String email = mEmail.getText().toString(),
                        password = mPassword.getText().toString();
                if (Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    signIn(email, password);
                } else {
                    mEmail.setError("Please enter a valid email address.");
                }
            }
        });

        mRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), SignUpActivity.class));
            }
        });

    }

    private void signIn(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            signout = mRemember.isChecked();
                            userIsLoggedIn();
                        } else {
                            Toast.makeText(LoginActivity.this, "Failed to login.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    // TODO grab actual values for network & site user authorized for
    private void userIsLoggedIn() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            clear = false;

//            Log.d(TAG, "USER: " +user.toString());
//            Log.d(TAG, "USER EMAIL" + user.getEmail());
//            Log.d(TAG, "USER ID: " + user.getUid());
            String userId = user.getUid();

            Bundle authBundle = new Bundle();


            final DocumentReference currentUserRef = usersRef.document(userId);

            currentUserRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    String toastMessage;
                    if (task.isSuccessful()) {
                        Log.d(TAG, "successful");
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            Log.d(TAG, "HERE");
                            try {
                                mNetwork = document.get("network").toString();
                                mNetworkName = document.get("network_name").toString();
                                mSite = document.get("site").toString();
                                mSiteName = document.get("site_name").toString();
                                Log.d(TAG, "=====" + mNetwork + " | " + mNetworkName + " | " + mSite + " | " + mSiteName);
//                                authBundle.putString("network", mNetwork);
//                                authBundle.putString("network_name", mNetworkName);
//                                authBundle.putString("site", mSite);
//                                authBundle.putString("site_name", mSiteName);
                            } catch (NullPointerException e){
                                //TODO
                                Log.d(TAG, "EXCEPTION!");
                            }
                        } else {
                            // document for invitation code doesn't exist
                            toastMessage = "Uesr not found; Please contact support";
                            Toast.makeText(getApplicationContext(), toastMessage, Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        toastMessage = "User lookup failed; Please try again and contact support if issue persists";
                        Toast.makeText(getApplicationContext(), toastMessage, Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "get failed with ", task.getException());
                    }
//                    Toast.makeText(getApplicationContext(), toastMessage, Toast.LENGTH_SHORT).show();
                }
            });

            //TODO NOT WORKING!!!
            authBundle.putString("network", mNetwork);
            authBundle.putString("network_name", mNetworkName);
            authBundle.putString("site", mSite);
            authBundle.putString("site_name", mSiteName);
            Log.d(TAG, "AUTH BUNDLE: " + authBundle);
            Log.d(TAG, authBundle.toString());


            Intent mainActivityIntent = new Intent(getApplicationContext(), MainActivity.class);
            mainActivityIntent.putExtras(authBundle);
            startActivity(mainActivityIntent);

//            startActivity(new Intent(getApplicationContext(), MainActivity.class));
            finish();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            if (resultCode == RESULT_OK) {
                signout = mRemember.isChecked();
                userIsLoggedIn();
            } else {
                Log.d(TAG, "Sign in cancelled");
            }
        }
    }

    @Override
    protected void onStop() {
        if (clear || signout) {
            mAuth.signOut();
            finish();
        }
        super.onStop();
    }
}