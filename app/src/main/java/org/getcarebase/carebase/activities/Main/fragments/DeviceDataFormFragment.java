package org.getcarebase.carebase.activities.Main.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import org.getcarebase.carebase.R;
import org.getcarebase.carebase.databinding.FragmentDeviceDataFormBinding;
import org.getcarebase.carebase.models.DeviceModel;
import org.getcarebase.carebase.viewmodels.AddDeviceViewModel;

import java.util.Objects;

public class DeviceDataFormFragment extends Fragment {
    public static final String TAG = DeviceDataFormFragment.class.getSimpleName();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        AddDeviceViewModel viewModel = new ViewModelProvider(requireActivity()).get(AddDeviceViewModel.class);
        FragmentDeviceDataFormBinding binding = DataBindingUtil.inflate(inflater,R.layout.fragment_device_data_form,container,false);
        binding.setLifecycleOwner(getViewLifecycleOwner());
        binding.setViewmodel(viewModel);

        DeviceModel deviceModel = Objects.requireNonNull(viewModel.getDeviceModelLiveData().getValue()).getData();
        
        ChipGroup chipGroup = binding.chipGroup;
        for (String tag : deviceModel.getTags()) {
            Chip chip = new Chip(requireContext());
            chip.setText(tag);
            chipGroup.addView(chip);
        }
        return binding.getRoot();
    }
}
