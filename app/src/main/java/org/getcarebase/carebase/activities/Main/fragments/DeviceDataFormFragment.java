package org.getcarebase.carebase.activities.Main.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.snackbar.Snackbar;

import org.getcarebase.carebase.R;
import org.getcarebase.carebase.activities.Main.AddDeviceActivity;
import org.getcarebase.carebase.databinding.CustomFieldFormBinding;
import org.getcarebase.carebase.databinding.FragmentDeviceDataFormBinding;
import org.getcarebase.carebase.models.DeviceModel;
import org.getcarebase.carebase.utils.Request;
import org.getcarebase.carebase.viewmodels.AddDeviceViewModel;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class DeviceDataFormFragment extends Fragment {
    public static final String TAG = DeviceDataFormFragment.class.getSimpleName();

    private AddDeviceViewModel viewModel;

    private LinearLayout customFieldsLayout;
    private TextView noSpecificationsTextView;
    private LinearLayout shipmentOptionsLayout;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        viewModel = new ViewModelProvider(requireActivity()).get(AddDeviceViewModel.class);
        FragmentDeviceDataFormBinding binding = DataBindingUtil.inflate(inflater,R.layout.fragment_device_data_form,container,false);
        binding.setLifecycleOwner(getViewLifecycleOwner());
        binding.setViewmodel(viewModel);
        binding.toolbar.setNavigationOnClickListener(v -> requireActivity().getSupportFragmentManager().popBackStack());

        DeviceModel deviceModel = Objects.requireNonNull(viewModel.getDeviceModelLiveData().getValue()).getData();

        ChipGroup chipGroup = binding.chipGroup;
        for (String tag : deviceModel.getTags()) {
            Chip chip = new Chip(requireContext());
            chip.setText(tag);
            chipGroup.addView(chip);
        }

        noSpecificationsTextView = binding.noSpecTextView;
        if (deviceModel.getSpecificationList().isEmpty()) {
            noSpecificationsTextView.setVisibility(View.VISIBLE);
        }
        customFieldsLayout = binding.customFieldsLayout;
        for (Map.Entry<String,Object> entry : deviceModel.getSpecificationList()) {
            addCustomField(entry.getKey(),entry.getValue().toString(),false);
        }
        binding.addCustomFieldButton.setOnClickListener(v -> addCustomField("","",true));

        binding.shipmentInfoCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> toggleShipmentOptions(isChecked));
        shipmentOptionsLayout = binding.shipmentOptions;

        binding.buttonSave.setOnClickListener(v -> onSaveClicked());

        viewModel.getErrorsLiveData().observe(getViewLifecycleOwner(),errors -> {
            for (Map.Entry<String,Integer> error : errors.entrySet()) {
                if (error.getKey().equals("all")) {
                    Snackbar.make(requireView(),error.getValue(),Snackbar.LENGTH_LONG).show();
                }
            }
        });

        return binding.getRoot();
    }

    public void onSaveClicked() {
        Map<String,String> specifications = getCustomFields();
        if (specifications != null) {
            viewModel.onSave(specifications);
        }
    }

    // TODO not in mvvm style - move this validation logic out
    public Map<String,String> getCustomFields() {
        Map<String,String> specifications = new HashMap<>();
        int childCount = customFieldsLayout.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View customField = customFieldsLayout.getChildAt(i);
            CustomFieldFormBinding binding = DataBindingUtil.getBinding(customField);
            if (binding != null) {
                if (binding.nameEditText.getText() == null || binding.nameEditText.getText().toString().isEmpty()) {
                    binding.nameLayout.setError(getString(R.string.error_missing_required_fields));
                    return null;
                }
                if (binding.valueEditText.getText() == null || binding.valueEditText.getText().toString().isEmpty()) {
                    binding.valueLayout.setError(getString(R.string.error_missing_required_fields));
                    return null;
                }
                binding.nameLayout.setError(null);
                binding.valueLayout.setError(null);
                String name = binding.nameEditText.getText().toString();
                String value = binding.valueEditText.getText().toString();
                specifications.put(name,value);
            } else {
                return null;
            }
        }
        return specifications;
    }

    public void toggleShipmentOptions(boolean isChecked) {
        shipmentOptionsLayout.setVisibility(isChecked ? View.VISIBLE : View.GONE);
    }

    public void addCustomField(String name, String value, boolean removable) {
        noSpecificationsTextView.setVisibility(View.GONE);
        if (customFieldsLayout.getChildCount() > 11) {
            Snackbar.make(requireView(),"Custom field limit reached", Snackbar.LENGTH_LONG).show();
            return;
        }
        if (removable && getCustomFields() == null) {
            return;
        }
        CustomFieldFormBinding binding = DataBindingUtil.inflate(getLayoutInflater(),R.layout.custom_field_form,customFieldsLayout,false);
        if (removable) {
            binding.removeButton.setOnClickListener(this::onRemoveCustomField);
        } else {
            binding.removeButton.setVisibility(View.GONE);
        }
        binding.setName(name);
        binding.setValue(value);
        binding.setEditable(viewModel.isEditable());
        binding.setLifecycleOwner(getViewLifecycleOwner());
        customFieldsLayout.addView(binding.getRoot());
    }

    public void onRemoveCustomField(View view) {
        if (customFieldsLayout.getChildCount() == 0) {
            noSpecificationsTextView.setVisibility(View.VISIBLE);
        }
        customFieldsLayout.removeView((View) view.getParent());
    }
}
