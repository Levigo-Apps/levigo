package org.getcarebase.carebase.activities.Main.fragments;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;

import org.getcarebase.carebase.R;
import org.getcarebase.carebase.models.Shipment;
import org.getcarebase.carebase.utils.Request;
import org.getcarebase.carebase.viewmodels.DeviceViewModel;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;

public class ShipDeviceFragment extends Fragment {
    public static final String TAG = ShipDeviceFragment.class.getName();
    TextView deviceName;
    TextView deviceUdi;
    TextInputLayout deviceQty;
    TextInputLayout deviceDest;
    TextInputLayout deviceTracker;

    Button saveButton;

    String sourceEntityName;
    String sourceEntityId;

    Map<String,String> trackingNumbersToEntityIdMap;
    Map<String, String> entityIdToEntityNameMap;

    private View rootView;
    private DeviceViewModel deviceViewModel;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.ship_device, container, false);
        MaterialToolbar topToolBar = rootView.findViewById(R.id.topAppBar);
        deviceName = rootView.findViewById(R.id.device_name);
        deviceUdi = rootView.findViewById(R.id.device_udi);
        deviceQty = rootView.findViewById(R.id.device_quantity);

        deviceDest = rootView.findViewById(R.id.device_dest);
        AutoCompleteTextView destOptions = (AutoCompleteTextView) deviceDest.getEditText();

        deviceTracker = rootView.findViewById(R.id.device_tracker);
        AutoCompleteTextView trackOptions = (AutoCompleteTextView) deviceTracker.getEditText();

        saveButton = rootView.findViewById(R.id.save_shipment);

        deviceViewModel = new ViewModelProvider(this).get(DeviceViewModel.class);

        final ArrayAdapter<String> sitesAdapter = new ArrayAdapter<>(rootView.getContext(), R.layout.dropdown_menu_popup_item, new ArrayList<>());
        final ArrayAdapter<String> trackingAdapter = new ArrayAdapter<>(rootView.getContext(), R.layout.dropdown_menu_popup_item, new ArrayList<>());
        deviceViewModel.getUserLiveData().observe(getViewLifecycleOwner(), userResource -> {
            deviceViewModel.setupDeviceRepository();
            deviceViewModel.setupEntityRepository();
            if (userResource.getRequest().getStatus() == org.getcarebase.carebase.utils.Request.Status.SUCCESS) {
                sourceEntityName = userResource.getData().getEntityName();
                sourceEntityId = userResource.getData().getEntityId();
            }
            deviceViewModel.getSitesLiveData().observe(getViewLifecycleOwner(), sitesResource -> {
                if(sitesResource.getRequest().getStatus() == org.getcarebase.carebase.utils.Request.Status.SUCCESS) {
                    entityIdToEntityNameMap = sitesResource.getData();

                    sitesAdapter.clear();
                    Collection<String> siteOptions = entityIdToEntityNameMap.values();
                    siteOptions.remove(sourceEntityName);
                    sitesAdapter.addAll(siteOptions);

                    deviceDest.setEnabled(true);

                    if (entityIdToEntityNameMap != null && trackingNumbersToEntityIdMap != null) {
                        saveButton.setEnabled(true);
                    }
                } else {
                    Log.d(TAG,"Unable to fetch sites");
                    Snackbar.make(rootView, R.string.error_something_wrong, Snackbar.LENGTH_LONG).show();
                }
            });

            deviceViewModel.getShipmentTrackingNumbersLiveData().observe(getViewLifecycleOwner(), trackingNumberResource -> {
                if (trackingNumberResource.getRequest().getStatus() == Request.Status.SUCCESS) {
                    trackingNumbersToEntityIdMap = trackingNumberResource.getData();

                    // Set tracking number options
                    trackingAdapter.clear();
                    trackingAdapter.add("Get New Tracking Number");
                    trackingAdapter.addAll(trackingNumbersToEntityIdMap.keySet());

                    deviceTracker.setEnabled(true);

                    if (entityIdToEntityNameMap != null && trackingNumbersToEntityIdMap != null) {
                        saveButton.setEnabled(true);
                    }

                    // Set tracking number input change listener
                    deviceTracker.getEditText().addTextChangedListener(new TextWatcher() {
                        @Override
                        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

                        @Override
                        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                            if (charSequence.toString().contentEquals("Get New Tracking Number")) {
                                deviceDest.setVisibility(View.VISIBLE);
                                deviceDest.setEnabled(true);
                            } else {
                                String entityId = trackingNumbersToEntityIdMap.get(charSequence.toString());
                                Log.d(TAG,"Matching Entity Id: " + entityId);
                                String entityName = entityIdToEntityNameMap.get(entityId);
                                Log.d(TAG, "Matching Entity Name: " + entityName);
                                deviceDest.getEditText().setText(entityName);
                                deviceDest.setVisibility(View.VISIBLE);
                            }
                        }

                        @Override
                        public void afterTextChanged(Editable editable) {}
                    });
                } else {
                    Log.d(TAG,"Unable to fetch shipment tracking numbers");
                    Snackbar.make(rootView, R.string.error_something_wrong, Snackbar.LENGTH_LONG).show();
                }
            });
        });
        destOptions.setAdapter(sitesAdapter);
        trackOptions.setAdapter(trackingAdapter);

        handleArguments();

        deviceViewModel.getSaveShipmentRequestLiveData().observe(getViewLifecycleOwner(), event -> {
            Request request = event.getContentIfNotHandled();
            if (request.getStatus() == Request.Status.SUCCESS) {
                FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
                View nextView = requireActivity().findViewById(R.id.frame_layout);
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.remove(this).commit();
                Snackbar.make(nextView, "Shipment saved to inventory", Snackbar.LENGTH_LONG).show();
            } else {
                Log.d(TAG,"error while saving shipment");
                Snackbar.make(rootView, R.string.error_something_wrong, Snackbar.LENGTH_LONG).show();
            }
        });
        saveButton.setOnClickListener(v -> saveData());

        topToolBar.setNavigationOnClickListener(view -> requireActivity().getSupportFragmentManager().popBackStack());

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

    private void saveData() {
        Shipment shipment = new Shipment();
        shipment.setUdi((String) deviceUdi.getText());
        shipment.setDeviceName((String) deviceName.getText());
        String di;
        shipment.setDi((di = getArguments().getString("di")) != null ? di : "");
        if (deviceQty.getEditText().getText().toString().isEmpty()) {
            Snackbar.make(rootView, "Enter a quantity", Snackbar.LENGTH_LONG).show();
            return;
        }
        if (deviceDest.getEditText().getText().toString().isEmpty()) {
            Snackbar.make(rootView, "Select a destination", Snackbar.LENGTH_LONG).show();
            return;
        }
        if (deviceTracker.getEditText().getText().toString().isEmpty()) {
            Snackbar.make(rootView, "Select a tracking number", Snackbar.LENGTH_LONG).show();
            return;
        }
        int shippedQuantity = Integer.parseInt(deviceQty.getEditText().getText().toString());
        if (shippedQuantity > Integer.parseInt(getArguments().getString("qty")) || shippedQuantity <= 0) {
            Snackbar.make(rootView, "Invalid quantity", Snackbar.LENGTH_LONG).show();
            return;
        }
        shipment.setQuantity(shippedQuantity);

        String tracker = Objects.requireNonNull(deviceTracker.getEditText()).getText().toString();
        shipment.setTrackingNumber(tracker.contentEquals("Get New Tracking Number") ? "temptrackingnumber" : tracker);

        shipment.setSourceEntityId(sourceEntityId);
        shipment.setSourceEntityName(sourceEntityName);

        for (Map.Entry<String,String> entry : entityIdToEntityNameMap.entrySet()) {
            if (entry.getValue().equals(deviceDest.getEditText().getText().toString().trim())) {
                shipment.setDestinationEntityId(entry.getKey());
                shipment.setDestinationEntityName(entry.getValue());
            }
        }

        deviceViewModel.saveShipment(shipment);
    }
}