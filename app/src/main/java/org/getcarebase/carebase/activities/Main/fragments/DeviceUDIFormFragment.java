package org.getcarebase.carebase.activities.Main.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import org.getcarebase.carebase.R;
import org.getcarebase.carebase.databinding.FragmentDeviceUdiFormBinding;
import org.getcarebase.carebase.viewmodels.AddDeviceViewModel;

public class DeviceUDIFormFragment extends Fragment {
    public static final String TAG = DeviceUDIFormFragment.class.getSimpleName();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        AddDeviceViewModel viewModel = new ViewModelProvider(requireActivity()).get(AddDeviceViewModel.class);
        FragmentDeviceUdiFormBinding binding = DataBindingUtil.inflate(inflater, R.layout.fragment_device_udi_form, container, false);
        binding.setLifecycleOwner(getViewLifecycleOwner());
        binding.setViewmodel(viewModel);
        return binding.getRoot();
    }
}
