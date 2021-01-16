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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;

import android.widget.Button;
import android.text.TextWatcher;


import org.getcarebase.carebase.R;
import org.getcarebase.carebase.utils.Request;
import org.getcarebase.carebase.utils.Resource;
import org.getcarebase.carebase.viewmodels.AuthViewModel;

/**
 * Resets password with email
*/

public class ResetFragment extends Fragment {
    public static final String TAG = ResetFragment.class.getName();
    private TextInputEditText emailField;
    private Button resetButton;

    private AuthViewModel authViewModel;

    private View rootView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.reset_layout,container,false);
        emailField = rootView.findViewById(R.id.reset_email);
        resetButton = rootView.findViewById(R.id.send_reset_link_button);
        authViewModel = new ViewModelProvider(requireActivity()).get(AuthViewModel.class);
        MaterialToolbar toolbar = rootView.findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> Navigation.findNavController(requireActivity(),R.id.main_content).popBackStack());

        emailField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                resetButton.setEnabled(!emailField.getText().toString().isEmpty());
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        });
        resetButton.setOnClickListener(this::resetWithEmail);
        return rootView;
    }

    public void resetWithEmail(View view) {
        String email = emailField.getText().toString().trim();
        authViewModel.resetPasswordWithEmail(email).observe(this, request -> {
            // display request status
            Snackbar.make(rootView, request.getResourceString(), Snackbar.LENGTH_LONG).show();
        });
    }
}
