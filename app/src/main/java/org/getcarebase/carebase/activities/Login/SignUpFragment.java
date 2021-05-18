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
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

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

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Signs up new user
 */

public class SignUpFragment extends Fragment {
    public static final String TAG = SignUpFragment.class.getName();

    private View rootView;
    private View invitationCodeLayout;
    private Button submitInvitationCodeButton;
    private TextInputLayout invitationCodeTextInputLayout;
    private TextInputEditText invitationCodeEditText;

    private View createAccountLayout;
    private TextView networkNameTextView;
    private TextView entityNameTextView;
    private TextInputLayout emailTextInputLayout;
    private TextInputEditText emailField;
    private TextInputLayout passwordTextInputLayout;
    private TextInputEditText passwordField;
    private TextInputLayout confirmPasswordTextInputLayout;
    private TextInputEditText confirmPasswordField;
    private Button signUpButton;

    private AuthViewModel authViewModel;

    final Pattern emailPattern = Pattern.compile("\\b[\\w\\.-]+@[\\w\\.-]+\\.\\w{2,4}\\b");
    final Pattern passwordPattern = Pattern.compile("^(?=.*\\d)(?=.*[a-z])(?=.*[A-Z])(?=.*[a-zA-Z]).{8,}$");

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

        MaterialToolbar toolbar = rootView.findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> Navigation.findNavController(requireActivity(),R.id.main_content).popBackStack());

        invitationCodeLayout = rootView.findViewById(R.id.invitation_code_layout);
        submitInvitationCodeButton = rootView.findViewById(R.id.submit_invitation_code_button);
        invitationCodeTextInputLayout = rootView.findViewById(R.id.invitation_code_text_input_layout);
        invitationCodeEditText = rootView.findViewById(R.id.invitation_code_edit_text);

        createAccountLayout = rootView.findViewById(R.id.signup_email_password_layout);
        networkNameTextView = rootView.findViewById(R.id.signup_network_name);
        entityNameTextView = rootView.findViewById(R.id.signup_site_name);
        emailTextInputLayout = rootView.findViewById(R.id.signup_email_text_input_layout);
        emailField = rootView.findViewById(R.id.signup_email);
        passwordTextInputLayout = rootView.findViewById(R.id.password_text_input_layout);
        passwordField = rootView.findViewById(R.id.signup_password);
        confirmPasswordTextInputLayout = rootView.findViewById(R.id.confirm_password_text_input_layout);
        confirmPasswordField = rootView.findViewById(R.id.signup_password_confirm);
        signUpButton = rootView.findViewById(R.id.signup_button);


        invitationCodeEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // enables the submit button if invitation code is not empty
                submitInvitationCodeButton.setEnabled(s.toString().trim().length() != 0);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        submitInvitationCodeButton.setOnClickListener(v -> {
            String invitationCode = Objects.requireNonNull(invitationCodeEditText.getText()).toString();
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

                emailTextInputLayout.setError(null);
                passwordTextInputLayout.setError(null);
                confirmPasswordTextInputLayout.setError(null);

                Matcher emailMatcher = emailPattern.matcher(e);
                Matcher passwordMatcher = passwordPattern.matcher(p);
                if (!emailMatcher.matches()) {
                    emailTextInputLayout.setError("Enter a valid email");
                } else if (!passwordMatcher.matches())  {
                    passwordTextInputLayout.setError("Password should have minimum 8 characters, at least 1 uppercase letter, 1 lowercase letter and 1 number");
                } else if (!p.equals(cp) && passwordMatcher.matches()) {
                    passwordTextInputLayout.setError("Passwords do not match");
                    confirmPasswordTextInputLayout.setError("Passwords do not match");
                } else {
                    signUpButton.setEnabled(true);
                }

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
                entityNameTextView.setText(code.getEntityName());
                createAccountLayout.setVisibility(View.VISIBLE);
                invitationCodeLayout.setVisibility(View.GONE);
            }
            else if (invitationCodeResource.getRequest().getStatus() == Request.Status.ERROR) {
                invitationCodeTextInputLayout.setError("Invalid invitation code");
                Snackbar.make(rootView,invitationCodeResource.getRequest().getResourceString(),Snackbar.LENGTH_LONG);
            }
        });
    }

    private void createUser(final String email, final String password) {
        authViewModel.createUser(email,password);
    }
}
