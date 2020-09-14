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
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;

import org.getcarebase.carebase.activities.Main.MainActivity;
import org.getcarebase.carebase.R;

/**
 * Logs in user
 */

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = LoginActivity.class.getSimpleName();
    private static final int RC_SIGN_IN = 1;

    private FirebaseAuth mAuth;
    private EditText mEmail, mPassword;
    private TextInputLayout emailTextInputLayout,passwordTextInputLayout;
    private Button loginButton;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseApp.initializeApp(this);

        mAuth = FirebaseAuth.getInstance();

        userIsLoggedIn();

        setContentView(R.layout.activity_login);

        mEmail = findViewById(R.id.login_email);
        emailTextInputLayout = findViewById(R.id.email_text_input_layout);
        mEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                toggleLoginButton();
            }
            @Override
            public void afterTextChanged(Editable editable) {
                // clear error
                emailTextInputLayout.setError(null);
            }
        });

        mPassword = findViewById(R.id.login_password);
        passwordTextInputLayout = findViewById(R.id.password_text_input_layout);
        mPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                toggleLoginButton();
            }
            @Override
            public void afterTextChanged(Editable editable) {
                // clear error
                passwordTextInputLayout.setError(null);
            }
        });

        loginButton = findViewById(R.id.login_button);
    }

    private void toggleLoginButton() {
        // set login button to disabled if there is no text in either email or password
        // or enabled if there is text in both
        loginButton.setEnabled(!mEmail.getText().toString().isEmpty() && !mPassword.getText().toString().isEmpty());
    }

    private void userIsLoggedIn() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            Intent mainActivityIntent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(mainActivityIntent);
            finish();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            if (resultCode == RESULT_OK) {
                userIsLoggedIn();
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

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            userIsLoggedIn();
                        } else {
                            try {
                                throw task.getException();
                            } catch (FirebaseAuthException e) {
                                String errorCode = e.getErrorCode();
                                switch (errorCode) {
                                    case "ERROR_INVALID_EMAIL":
                                        emailTextInputLayout.setError(getString(R.string.error_invalid_email_format));
                                        break;

                                    case "ERROR_WRONG_PASSWORD":
                                        passwordTextInputLayout.setError(getString(R.string.error_invalid_password));
                                        break;

                                    case "ERROR_USER_NOT_FOUND":
                                        emailTextInputLayout.setError(getString(R.string.error_invalid_email));
                                        break;
                                }
                            }
                            catch (FirebaseTooManyRequestsException e) {
                                Snackbar.make(view, "Can't login due to too many attempts. Retry in 1 minute.", Snackbar.LENGTH_LONG).show();
                            }
                            catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });
    }

    public void register(View view) {
        startActivity(new Intent(getApplicationContext(), SignUpActivity.class));
    }
}