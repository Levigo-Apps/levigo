package org.getcarebase.carebase.activities.Login;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;

import org.getcarebase.carebase.R;
import org.getcarebase.carebase.models.User;
import org.getcarebase.carebase.utils.Request;
import org.getcarebase.carebase.utils.Resource;
import org.getcarebase.carebase.viewmodels.AuthViewModel;

public class LoginFragment extends Fragment {
    public static final String TAG = LoginFragment.class.getName();

    private EditText emailEditText, passwordEditText;
    private TextInputLayout emailTextInputLayout,passwordTextInputLayout;
    private Button loginButton;

    private AuthViewModel authViewModel;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.login_layout, container, false);
        emailEditText = rootView.findViewById(R.id.login_email);
        emailTextInputLayout = rootView.findViewById(R.id.email_text_input_layout);
        passwordEditText = rootView.findViewById(R.id.login_password);
        passwordTextInputLayout = rootView.findViewById(R.id.password_text_input_layout);
        loginButton = rootView.findViewById(R.id.login_button);
        Button registerButton = rootView.findViewById(R.id.register_button);
        registerButton.setOnClickListener(this::navigateToSignUpFragment);
        Button forgotPasswordButton = rootView.findViewById(R.id.forgot_password_button);
        forgotPasswordButton.setOnClickListener(this::navigateToResetFragment);

        authViewModel = new ViewModelProvider(requireActivity()).get(AuthViewModel.class);

        authViewModel.getUserLiveData().observe(getViewLifecycleOwner(), userResource -> {
            if (userResource.getRequest().getStatus() == Request.Status.ERROR && userResource.getRequest().getResourceString() != null) {
                if (userResource.getRequest().getResourceString() == R.string.error_invalid_email_format) {
                    emailTextInputLayout.setError(getString(userResource.getRequest().getResourceString()));
                }
                else if (userResource.getRequest().getResourceString() == R.string.error_invalid_email_or_password) {
                    emailTextInputLayout.setError(getString(userResource.getRequest().getResourceString()));
                    passwordTextInputLayout.setError(getString(userResource.getRequest().getResourceString()));
                }
                else if (userResource.getRequest().getResourceString() == R.string.error_too_many_attempts ||
                        userResource.getRequest().getResourceString() == R.string.error_something_wrong) {
                    Snackbar.make(rootView, userResource.getRequest().getResourceString(), Snackbar.LENGTH_LONG).show();
                }
            }
        });

        TextWatcher invalidCredentialsWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                // set login button to disabled if there is no text in either email or password
                // or enabled if there is text in both
                loginButton.setEnabled(!emailEditText.getText().toString().isEmpty() && !passwordEditText.getText().toString().isEmpty());
            }
            @Override
            public void afterTextChanged(Editable editable) {
                // clear errors
                emailTextInputLayout.setError(null);
                passwordTextInputLayout.setError(null);
            }
        };

        emailEditText.addTextChangedListener(invalidCredentialsWatcher);
        passwordEditText.addTextChangedListener(invalidCredentialsWatcher);
        loginButton.setOnClickListener(this::login);

        return rootView;
    }

    public void login(final View view) {
        // clear previous errors
        emailTextInputLayout.setError(null);
        passwordTextInputLayout.setError(null);

        final String email = emailEditText.getText().toString().trim();
        final String password = passwordEditText.getText().toString().trim();

        authViewModel.signInWithEmailAndPassword(email, password);
    }

    public void navigateToSignUpFragment(final View view) {
        Fragment signUpFragment = new SignUpFragment();
        requireActivity().getSupportFragmentManager().beginTransaction()
                .add(R.id.fragment_container,signUpFragment,SignUpFragment.TAG)
                .addToBackStack(null)
                .commit();
    }

    public void navigateToResetFragment(final View view) {
        Fragment resetFragment = new ResetFragment();
        requireActivity().getSupportFragmentManager().beginTransaction()
                .add(R.id.fragment_container,resetFragment,ResetFragment.TAG)
                .addToBackStack(null)
                .commit();
    }
}