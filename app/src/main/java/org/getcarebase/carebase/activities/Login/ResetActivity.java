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

import android.os.Bundle;
import android.text.Editable;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;

import android.widget.Button;
import android.text.TextWatcher;


import org.getcarebase.carebase.R;
import org.getcarebase.carebase.utils.Resource;
import org.getcarebase.carebase.viewmodels.AuthViewModel;

/**
 * Resets password with email
*/

public class ResetActivity extends AppCompatActivity {
    private TextInputEditText mEmailField;
    private Button resetButton;

    private AuthViewModel authViewModel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset);
        mEmailField = findViewById(R.id.reset_email);
        resetButton = findViewById(R.id.send_reset_link_button);
        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        mEmailField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                resetButton.setEnabled(!mEmailField.getText().toString().isEmpty());
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        });

    }

    public void resetWithEmail(View view) {
        String email = mEmailField.getText().toString().trim();
        authViewModel.resetPasswordWithEmail(email).observe(this, new Observer<Resource<Object>>() {
            @Override
            public void onChanged(Resource<Object> result) {
                finish();
                Toast.makeText(getApplicationContext(), result.resourceString, Toast.LENGTH_LONG).show();
            }
        });
    }
}
