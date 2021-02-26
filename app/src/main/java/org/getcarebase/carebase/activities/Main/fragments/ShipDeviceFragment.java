package org.getcarebase.carebase.activities.Main.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.getcarebase.carebase.R;
import org.getcarebase.carebase.utils.Resource;
import org.getcarebase.carebase.viewmodels.DeviceViewModel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;

public class ShipDeviceFragment extends Fragment {
    public static final String TAG = ShipDeviceFragment.class.getName();
    private Activity parent;
    TextView deviceName;
    TextView deviceUdi;
    TextInputLayout deviceQty;
    TextInputLayout deviceDest;

    private View rootView;
    private DeviceViewModel deviceViewModel;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.ship_device, container, false);
        parent = getActivity();
        MaterialToolbar topToolBar = rootView.findViewById(R.id.topAppBar);
        deviceName = rootView.findViewById(R.id.device_name);
        deviceUdi = rootView.findViewById(R.id.device_udi);
        deviceQty = rootView.findViewById(R.id.device_quantity);

        deviceDest = rootView.findViewById(R.id.device_dest);
        AutoCompleteTextView destOptions = (AutoCompleteTextView) deviceDest.getEditText();

        deviceViewModel = new ViewModelProvider(this).get(DeviceViewModel.class);

        final ArrayAdapter<String> sitesAdapter = new ArrayAdapter<>(rootView.getContext(), R.layout.dropdown_menu_popup_item, new ArrayList<>());
        deviceViewModel.getUserLiveData().observe(getViewLifecycleOwner(), userResource -> {
            deviceViewModel.setupDeviceRepository();
            LiveData<Resource<String[]>> siteOptions = deviceViewModel.getSitesLiveData();
            deviceViewModel.getSitesLiveData().observe(getViewLifecycleOwner(), sitesResource -> {
                if(sitesResource.getRequest().getStatus() == org.getcarebase.carebase.utils.Request.Status.SUCCESS) {
                    sitesAdapter.clear();
                    sitesAdapter.addAll(Arrays.asList(sitesResource.getData()));
                } else {
                    Log.d(TAG,"Unable to fetch sites");
                    Snackbar.make(rootView, R.string.error_something_wrong, Snackbar.LENGTH_LONG).show();
                }
            });
        });
        destOptions.setAdapter(sitesAdapter);

        // TODO: setup object to save destination input through device view model
        handleArguments();

        topToolBar.setNavigationOnClickListener(view -> {
            if (requireActivity().getSupportFragmentManager().getBackStackEntryCount() > 0) {
                requireActivity().getSupportFragmentManager().popBackStack();
            } else {
                if (parent != null)
                    parent.onBackPressed();
            }
        });

        return rootView;
    }

    public void handleArguments() {
        if (getArguments() != null) {
            String name, barcode, qty;
            if ((name = getArguments().getString("name")) != null) {
                deviceName.setText(name);
            }
            if ((barcode = getArguments().getString("barcode")) != null) {
                deviceUdi.setText(barcode);
            }
            if ((qty = getArguments().getString("qty")) != null) {
                deviceQty.getEditText().setText(qty);
            }
        }
    }
}