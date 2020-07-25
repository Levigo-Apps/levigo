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
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * Signs up new user
 */

public class SignUpActivity extends AppCompatActivity {
    private static final String TAG = SignUpActivity.class.getSimpleName();

    private FirebaseAuth mAuth;
    private FirebaseFirestore levigoDb = FirebaseFirestore.getInstance();
    private CollectionReference invitationCodesRef = levigoDb.collection("invitation_codes");

    private LinearLayout mEmailPasswordLayout;
    private Button mSubmitInvitationCode;
    private TextInputLayout mInvitationCodeLayout;
    private TextInputEditText mInvitationCodeBox;
    private TextInputEditText mEmailField;
    private TextInputEditText mPasswordField;
    private TextInputEditText mConfirmPasswordField;
    private Button mSignUpButton;
    private TextView mNetworkNameTextView;
    private TextView mHospitalNameTextView;

    private String mInvitationCode;

    // ID and name of network and site authorized
    private String mNetwork;
    private String mNetworkName;
    private String mHospital;
    private String mHospitalName;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        mAuth = FirebaseAuth.getInstance();

        mNetworkNameTextView = findViewById(R.id.signup_network_name);
        mHospitalNameTextView = findViewById(R.id.signup_site_name);

        mEmailPasswordLayout = findViewById(R.id.signup_email_password_layout);
        mEmailField = findViewById(R.id.signup_email);
        mPasswordField = findViewById(R.id.signup_password);
        mConfirmPasswordField = findViewById(R.id.signup_password_confirm);
        mSignUpButton = findViewById(R.id.signup_button);

        // Email password fields disabled until valid invitation code
        mEmailPasswordLayout.setVisibility(View.GONE);
        mSignUpButton.setEnabled(false);

        mSubmitInvitationCode = findViewById(R.id.submit_invitation_code_button);
        // Disabled until not empty
        mSubmitInvitationCode.setEnabled(false);
        mInvitationCodeLayout = findViewById(R.id.textInputLayout_invitationCode);
        mInvitationCodeBox = findViewById(R.id.et_InvitationCode);


        // Disable submit invitation code when it's empty
        mInvitationCodeBox.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().trim().length() == 0) {
                    mSubmitInvitationCode.setEnabled(false);
                } else {
                    mSubmitInvitationCode.setEnabled(true);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        mSubmitInvitationCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mInvitationCode = mInvitationCodeBox.getText().toString();

                final DocumentReference docRef = invitationCodesRef.document(mInvitationCode);
                // Verify invitation code
                docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        String toastMessage;
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
//                                Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                                if (document.getBoolean("valid")) {
                                    try {
                                        // Valid code; Check which network & hospital authorized for
                                        mNetwork = document.get("network").toString();
                                        mNetworkName = document.get("network_name").toString();
                                        mHospital = document.get("hospital").toString();
                                        mHospitalName = document.get("hospital_name").toString();

                                        mNetworkNameTextView.setText(mNetworkName);
                                        mHospitalNameTextView.setText(mHospitalName);

                                        mEmailPasswordLayout.setVisibility(View.VISIBLE);
                                        mInvitationCodeLayout.setEnabled(false);

                                        toastMessage = "Validation complete";

                                        // invitation code data missing fields
                                    } catch (NullPointerException e) {
                                        toastMessage = "Error with validation code data; Please contact support";
                                    }

                                } else {
                                    toastMessage = "Invitation code already used; Please contact administrator";
                                }
                            } else {
                                // document for invitation code doesn't exist
                                toastMessage = "Invalid invitation code; Please contact administrator";
                            }
                        } else {
                            toastMessage = "Invitation code validation failed; Please try again and contact support if issue persists";
                            Log.d(TAG, "get failed with ", task.getException());
                        }
                        Toast.makeText(getApplicationContext(), toastMessage, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

//        ImwdlM5c1FoqpDwbsRSU
        TextWatcher emailPasswordWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int
                    count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                String e = mEmailField.getText().toString();
                String p = mPasswordField.getText().toString();
                String cp = mConfirmPasswordField.getText().toString();

                // Disable sign up if password fields don't match
                if (!p.equals(cp)) {
                    mSignUpButton.setEnabled(false);
                    //TODO display warning sign next to confirm password
                }

                // Disable sign up if any field is empty
                if (e.length() == 0 || p.length() == 0 || cp.length() == 0) {
                    mSignUpButton.setEnabled(false);
                } else {
                    mSignUpButton.setEnabled(true);
                }
            }
        };
        mEmailField.addTextChangedListener(emailPasswordWatcher);
        mPasswordField.addTextChangedListener(emailPasswordWatcher);
        mConfirmPasswordField.addTextChangedListener(emailPasswordWatcher);

        mSignUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = mEmailField.getText().toString();
                String password = mPasswordField.getText().toString();

                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(SignUpActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
//                                    Log.d(TAG, "createUserWithEmail:success");
                                    disableValidationCode(mInvitationCode);
                                    Bundle authBundle = new Bundle();
                                    authBundle.putString("network", mNetwork);
                                    authBundle.putString("network_name", mNetworkName);
                                    authBundle.putString("hospital", mHospital);
                                    authBundle.putString("hospital_name", mHospitalName);

                                    Intent mainActivityIntent = new Intent(getApplicationContext(), MainActivity.class);
                                    mainActivityIntent.putExtras(authBundle);
                                    startActivity(mainActivityIntent);
                                } else {
                                    // If sign in fails, display a message to the user.
                                    Log.w(TAG, "createUserWithEmail:failure", task.getException());
                                    Toast.makeText(getApplicationContext(), "Authentication failed.",
                                            Toast.LENGTH_SHORT).show();
                                }
                            }
                        });

            }
        });
    }

    private void disableValidationCode(String invitationCode) {
        DocumentReference currentCodeRef = invitationCodesRef.document(invitationCode);

        // TODO add actions in case of success/failure OR delete custom listener?
        currentCodeRef
                .update("valid", false)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "DocumentSnapshot successfully updated!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error updating document", e);
                    }
                });
    }


}
