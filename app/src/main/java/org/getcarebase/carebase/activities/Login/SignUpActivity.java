/*
 * Copyright 2020 Carebase Solutions
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

package org.getcarebase.carebase.activities.Login;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedDispatcherOwner;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.getcarebase.carebase.R;
import org.getcarebase.carebase.activities.Main.MainActivity;
import org.getcarebase.carebase.models.InvitationCode;
import org.getcarebase.carebase.models.User;
import org.getcarebase.carebase.utils.Request;
import org.getcarebase.carebase.utils.Resource;
import org.getcarebase.carebase.viewmodels.AuthViewModel;

import java.util.HashMap;
import java.util.Map;

/**
 * Signs up new user
 */

public class SignUpActivity extends AppCompatActivity {
    private static final String TAG = SignUpActivity.class.getSimpleName();

    private LinearLayout emailPasswordLayout;
    private Button submitInvitationCode;
    private TextInputLayout invitationCodeLayout;
    private TextInputEditText invitationCodeBox;
    private TextInputEditText emailField;
    private TextInputEditText passwordField;
    private TextInputEditText confirmPasswordField;
    private Button signUpButton;
    private TextView networkNameTextView;
    private TextView hospitalNameTextView;

    private AuthViewModel authViewModel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);
        authViewModel.getUserLiveData().observe(this, new Observer<Resource<User>>() {
            @Override
            public void onChanged(Resource<User> userResource) {
                if (userResource.getRequest().getStatus() == Request.Status.SUCCESS) {
                    Intent mainActivityIntent = new Intent(getApplicationContext(), MainActivity.class);
                    if (userResource.getRequest().getResourceString() != null) {
                        Toast.makeText(getApplicationContext(), userResource.getRequest().getResourceString(), Toast.LENGTH_LONG).show();
                    }
                    startActivity(mainActivityIntent);
                    finish();
                }
                else if (userResource.getRequest().getStatus() == Request.Status.ERROR) {
                    Toast.makeText(getApplicationContext(), userResource.getRequest().getResourceString(), Toast.LENGTH_LONG).show();
                }
            }
        });

        networkNameTextView = findViewById(R.id.signup_network_name);
        hospitalNameTextView = findViewById(R.id.signup_site_name);

        emailPasswordLayout = findViewById(R.id.signup_email_password_layout);
        emailField = findViewById(R.id.signup_email);
        passwordField = findViewById(R.id.signup_password);
        confirmPasswordField = findViewById(R.id.signup_password_confirm);
        signUpButton = findViewById(R.id.signup_button);

        // Email password fields disabled until valid invitation code
        emailPasswordLayout.setVisibility(View.GONE);
        signUpButton.setEnabled(false);

        submitInvitationCode = findViewById(R.id.submit_invitation_code_button);
        // Disabled until not empty
        submitInvitationCode.setEnabled(false);
        invitationCodeLayout = findViewById(R.id.textInputLayout_invitationCode);
        invitationCodeBox = findViewById(R.id.et_InvitationCode);

        
        invitationCodeBox.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // enables the submit button if invitation code is not empty
                submitInvitationCode.setEnabled(s.toString().trim().length() != 0);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        submitInvitationCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String invitationCode = invitationCodeBox.getText().toString();
                checkInvitationCode(invitationCode);
            }
        });

        TextWatcher emailPasswordWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String e = emailField.getText().toString().trim();
                String p = passwordField.getText().toString().trim();
                String cp = confirmPasswordField.getText().toString().trim();

                // enable sign up if password fields match and
                // if all fields are not empty
                signUpButton.setEnabled(p.equals(cp) && e.length() != 0 || p.length() != 0);
            }
        };
        emailField.addTextChangedListener(emailPasswordWatcher);
        passwordField.addTextChangedListener(emailPasswordWatcher);
        confirmPasswordField.addTextChangedListener(emailPasswordWatcher);

        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailField.getText().toString();
                String password = passwordField.getText().toString();
                createUser(email,password);
            }
        });
    }

    private void checkInvitationCode(final String invitationCode) {
        LiveData<Resource<InvitationCode>> result = authViewModel.isInvitationCodeValid(invitationCode);
        result.observe(this, new Observer<Resource<InvitationCode>>() {
            @Override
            public void onChanged(Resource<InvitationCode> invitationCodeResource) {
                if (invitationCodeResource.getRequest().getStatus() == Request.Status.SUCCESS) {
                    InvitationCode code = invitationCodeResource.getData();
                    // Display authorized network and hospital
                    networkNameTextView.setText(code.getNetworkName());
                    hospitalNameTextView.setText(code.getHospitalName());
                    emailPasswordLayout.setVisibility(View.VISIBLE);
                    invitationCodeLayout.setEnabled(false);
                }
                else if (invitationCodeResource.getRequest().getStatus() == Request.Status.ERROR) {
                    Toast.makeText(getApplicationContext(), invitationCodeResource.getRequest().getResourceString(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void createUser(final String email, final String password) {
        authViewModel.createUser(email,password);
    }

    public void composeEmail(View view) {
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        final String SUPPORT_EMAIL = "elliot@getcarebase.org";
        intent.setData(Uri.parse("mailto:" + SUPPORT_EMAIL));
        intent.putExtra(Intent.EXTRA_SUBJECT, "Admin Account Request");
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }

    public void demoLogin(View view) {
        authViewModel.signInWithEmailAndPassword("demo_user@getcarebase.org", "demo_user");
    }
}
