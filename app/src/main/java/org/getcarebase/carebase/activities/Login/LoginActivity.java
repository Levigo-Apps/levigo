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
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.google.firebase.FirebaseApp;

import org.getcarebase.carebase.activities.Main.MainActivity;
import org.getcarebase.carebase.R;
import org.getcarebase.carebase.models.User;
import org.getcarebase.carebase.utils.Request;
import org.getcarebase.carebase.utils.Resource;
import org.getcarebase.carebase.viewmodels.AuthViewModel;

/**
 * Logs in user
 */

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = LoginActivity.class.getSimpleName();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseApp.initializeApp(this);
        setContentView(R.layout.login_host);
        AuthViewModel authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        authViewModel.getUserLiveData().observe(this, userResource -> {
            if (userResource.getRequest().getStatus() == Request.Status.SUCCESS) {
                signUserIn();
            }
        });

        // go to login screen
        if (savedInstanceState == null) {
            Fragment loginFragment = new LoginFragment();
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragment_container,loginFragment,LoginFragment.TAG)
                    .commit();
        }

        // try to sign in with current user
        authViewModel.getUser();
    }

    private void signUserIn() {
        Intent mainActivityIntent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(mainActivityIntent);
        finish();
    }
}