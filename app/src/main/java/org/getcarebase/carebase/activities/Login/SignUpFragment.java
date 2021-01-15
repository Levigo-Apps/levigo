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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.getcarebase.carebase.R;
import org.getcarebase.carebase.activities.Main.MainActivity;
import org.getcarebase.carebase.models.InvitationCode;
import org.getcarebase.carebase.models.User;
import org.getcarebase.carebase.utils.Request;
import org.getcarebase.carebase.utils.Resource;
import org.getcarebase.carebase.viewmodels.AuthViewModel;

/**
 * Signs up new user
 */

public class SignUpFragment extends Fragment {
    public static final String TAG = SignUpFragment.class.getName();

    private View rootView;
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.signup_layout,container,false);
        authViewModel = new ViewModelProvider(requireActivity()).get(AuthViewModel.class);
        authViewModel.getUserLiveData().observe(getViewLifecycleOwner(), userResource -> {
            if (userResource.getRequest().getStatus() == Request.Status.SUCCESS) {
                // go to main screen
            }
            else if (userResource.getRequest().getStatus() == Request.Status.ERROR) {
                Snackbar.make(rootView,userResource.getRequest().getResourceString(),Snackbar.LENGTH_LONG);
            }
        });

        networkNameTextView = rootView.findViewById(R.id.signup_network_name);
        hospitalNameTextView = rootView.findViewById(R.id.signup_site_name);

        emailPasswordLayout = rootView.findViewById(R.id.signup_email_password_layout);
        emailField = rootView.findViewById(R.id.signup_email);
        passwordField = rootView.findViewById(R.id.signup_password);
        confirmPasswordField = rootView.findViewById(R.id.signup_password_confirm);
        signUpButton = rootView.findViewById(R.id.signup_button);

        // Email password fields disabled until valid invitation code
        emailPasswordLayout.setVisibility(View.GONE);
        signUpButton.setEnabled(false);

        submitInvitationCode = rootView.findViewById(R.id.submit_invitation_code_button);
        // Disabled until not empty
        submitInvitationCode.setEnabled(false);
        invitationCodeLayout = rootView.findViewById(R.id.textInputLayout_invitationCode);
        invitationCodeBox = rootView.findViewById(R.id.et_InvitationCode);

        Button contactUsButton = rootView.findViewById(R.id.contact_us_button);
        contactUsButton.setOnClickListener(this::composeEmail);

        Button demoButton = rootView.findViewById(R.id.demo_login_button);
        demoButton.setOnClickListener(this::demoLogin);

        MaterialToolbar toolbar = rootView.findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> requireActivity().getSupportFragmentManager().popBackStack());

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

        submitInvitationCode.setOnClickListener(v -> {
            String invitationCode = invitationCodeBox.getText().toString();
            checkInvitationCode(invitationCode);
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

        signUpButton.setOnClickListener(v -> {
            String email = emailField.getText().toString();
            String password = passwordField.getText().toString();
            createUser(email,password);
        });

        return rootView;
    }

    private void checkInvitationCode(final String invitationCode) {
        LiveData<Resource<InvitationCode>> result = authViewModel.isInvitationCodeValid(invitationCode);
        result.observe(getViewLifecycleOwner(), invitationCodeResource -> {
            if (invitationCodeResource.getRequest().getStatus() == Request.Status.SUCCESS) {
                InvitationCode code = invitationCodeResource.getData();
                // Display authorized network and hospital
                networkNameTextView.setText(code.getNetworkName());
                hospitalNameTextView.setText(code.getHospitalName());
                emailPasswordLayout.setVisibility(View.VISIBLE);
                invitationCodeLayout.setEnabled(false);
            }
            else if (invitationCodeResource.getRequest().getStatus() == Request.Status.ERROR) {
                Snackbar.make(rootView,invitationCodeResource.getRequest().getResourceString(),Snackbar.LENGTH_LONG);
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
        if (intent.resolveActivity(requireActivity().getPackageManager()) != null) {
            startActivity(intent);
        }
    }

    public void demoLogin(View view) {
        authViewModel.signInWithEmailAndPassword("demo_user@getcarebase.org", "demo_user");
    }
}
