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
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.crashlytics.FirebaseCrashlytics;

import org.getcarebase.carebase.activities.Main.MainActivity;
import org.getcarebase.carebase.R;
import org.getcarebase.carebase.models.User;
import org.getcarebase.carebase.utils.Resource;
import org.getcarebase.carebase.viewmodels.AuthViewModel;

/**
 * Logs in user
 */

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = LoginActivity.class.getSimpleName();
    private static final int RC_SIGN_IN = 1;

    private EditText mEmail, mPassword;
    private TextInputLayout emailTextInputLayout,passwordTextInputLayout;
    private Button loginButton;

    private AuthViewModel authViewModel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseApp.initializeApp(this);
        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        // try to sign in with current user
        authViewModel.getUser().observe(this, new Observer<Resource<User>>() {
            @Override
            public void onChanged(Resource<User> userResource) {
                if (userResource.status == Resource.Status.SUCCESS) {
                    signUserIn(userResource.data);
                }
            }
        });

        setContentView(R.layout.activity_login);

        TextWatcher invalidCredentialsWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                // set login button to disabled if there is no text in either email or password
                // or enabled if there is text in both
                loginButton.setEnabled(!mEmail.getText().toString().isEmpty() && !mPassword.getText().toString().isEmpty());
            }
            @Override
            public void afterTextChanged(Editable editable) {
                // clear errors
                emailTextInputLayout.setError(null);
                passwordTextInputLayout.setError(null);
            }
        };

        mEmail = findViewById(R.id.login_email);
        emailTextInputLayout = findViewById(R.id.email_text_input_layout);
        mEmail.addTextChangedListener(invalidCredentialsWatcher);

        mPassword = findViewById(R.id.login_password);
        passwordTextInputLayout = findViewById(R.id.password_text_input_layout);
        mPassword.addTextChangedListener(invalidCredentialsWatcher);

        loginButton = findViewById(R.id.login_button);
    }

    private void signUserIn(User user) {
        Intent mainActivityIntent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(mainActivityIntent);
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            if (resultCode == RESULT_OK) {
                signUserIn((User) data.getSerializableExtra("authenticated_user"));
            } else {
                Log.d(TAG, "Sign in cancelled");
            }
        }
    }

    public void resetPassword(View view) {
        startActivity(new Intent(getApplicationContext(), ResetActivity.class));
    }

    public void login(final View view) {
        // clear previous errors
        emailTextInputLayout.setError(null);
        passwordTextInputLayout.setError(null);

        final String email = mEmail.getText().toString();
        final String password = mPassword.getText().toString();

        authViewModel.signInWithEmailAndPassword(email,password)
                .observe(this, new Observer<Resource<User>>() {
            @Override
            public void onChanged(Resource<User> userResource) {
                if (userResource.status == Resource.Status.SUCCESS) {
                    // sign in user
                    signUserIn(userResource.data);
                }
                else if (userResource.status == Resource.Status.ERROR) {
                    if (userResource.resourceString == R.string.error_invalid_email_format) {
                        emailTextInputLayout.setError(getString(userResource.resourceString));
                    }
                    else if (userResource.resourceString == R.string.error_invalid_email_or_password) {
                        emailTextInputLayout.setError(getString(userResource.resourceString));
                        passwordTextInputLayout.setError(getString(userResource.resourceString));
                    }
                    else  if (userResource.resourceString == R.string.error_too_many_attempts) {
                        Snackbar.make(view, userResource.resourceString, Snackbar.LENGTH_LONG).show();
                    }
                    else if (userResource.resourceString == R.string.error_something_wrong) {
                        Snackbar.make(view, userResource.resourceString, Snackbar.LENGTH_LONG).show();
                    }
                }
            }
        });
    }

    public void register(View view) {
        startActivity(new Intent(getApplicationContext(), SignUpActivity.class));
    }
}