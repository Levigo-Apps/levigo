package org.getcarebase.carebase.activities.Login;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.getcarebase.carebase.R;
import org.getcarebase.carebase.utils.Request;
import org.getcarebase.carebase.viewmodels.AuthViewModel;

public class SplashFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.splash_layout, container, false);
        AuthViewModel authViewModel = new ViewModelProvider(requireActivity()).get(AuthViewModel.class);
        authViewModel.getUserLiveData().observe(getViewLifecycleOwner(), userResource -> {
            if (userResource.getRequest().getStatus() != Request.Status.SUCCESS) {
                // navigate to login screen
                Navigation.findNavController(requireActivity(),R.id.main_content).navigate(R.id.action_splashFragment_to_loginFragment);
            }
        });

        // try to log in
        authViewModel.getUser();

        return rootView;
    }
}