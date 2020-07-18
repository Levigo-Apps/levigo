package com.levigo.levigoapp;

import android.app.Activity;
import android.content.DialogInterface;
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

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Arrays;
import java.util.List;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = LoginActivity.class.getSimpleName();//"LoginActivity";
    private static final int RC_SIGN_IN = 1;

    private boolean clear, signout;
    private FirebaseAuth mAuth;
    private MaterialButton mLogin, mRegister;
    private EditText mEmail, mPassword;
    private MaterialCheckBox mRemember;

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
                final String  email = mEmail.getText().toString(),
                        password = mPassword.getText().toString();
                if(Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    signIn(email, password);
                }
                else {
                    mEmail.setError("Please enter a valid email address.");
                }
            }
        });

        mRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                startActivity(new Intent(getApplicationContext(), SignUpActivity.class));
//                final String  email = mEmail.getText().toString(),
//                        password = mPassword.getText().toString();
//                List<AuthUI.IdpConfig> providers = Arrays.asList(new AuthUI.IdpConfig.EmailBuilder().build());
//                startActivityForResult(AuthUI.getInstance().createSignInIntentBuilder().setAvailableProviders(providers).build(), RC_SIGN_IN);
//
//                Log.d(TAG, "about to match");
//                if(Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
//                    Log.d(TAG, "here");
//                    signUp(email, password);   //create a sign up activity
//
//                }
//                else {
//                    Log.d(TAG, "there");
//                    mEmail.setError("Please enter a valid email address.");
//                }
            }
        });

    }

    private void signIn(String email, String password){
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            signout = mRemember.isChecked();
                            userIsLoggedIn();
                        }
                        else {
                            Toast.makeText(LoginActivity.this, "Failed to login.",Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
/*
    private void signUp(String email, String password) {
        mAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if(task.isSuccessful()) {
                        Log.d(TAG, "Sign in successful");
                        signout = mRemember.isChecked();
                    }
                    else {
                        Toast.makeText(LoginActivity.this, "Failed to sign up",Toast.LENGTH_LONG).show();
                    }
                }
            });
    }
*/
    private void userIsLoggedIn() {
        FirebaseUser user = mAuth.getCurrentUser();
        if(user != null){
            clear = false;
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
            finish();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == RC_SIGN_IN) {
            if(resultCode == RESULT_OK) {
                signout = mRemember.isChecked();
                userIsLoggedIn();
            }
            else {
                Log.d(TAG,"Sign in cancelled");
            }
        }
    }

    @Override
    protected void onStop() {
        if(clear || signout) {
            mAuth.signOut();
            finish();
        }
        super.onStop();
    }
}
