package org.getcarebase.carebase.activities.Main.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;

import org.getcarebase.carebase.R;
import org.getcarebase.carebase.models.DeviceModel;
import org.getcarebase.carebase.models.Shipment;
import org.getcarebase.carebase.utils.Request;
import org.getcarebase.carebase.viewmodels.DeviceViewModel;

import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

public class ShipDeviceFragment extends Fragment {
    public static final String TAG = ShipDeviceFragment.class.getName();
    private Activity parent;
    TextView deviceName;
    TextView deviceUdi;
    TextInputLayout deviceQty;
    TextInputLayout deviceDest;

    Button saveButton;

    String currentHospitalName;

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

        saveButton = rootView.findViewById(R.id.save_shipment);

        deviceViewModel = new ViewModelProvider(this).get(DeviceViewModel.class);

        final ArrayAdapter<String> sitesAdapter = new ArrayAdapter<>(rootView.getContext(), R.layout.dropdown_menu_popup_item, new ArrayList<>());
        deviceViewModel.getUserLiveData().observe(getViewLifecycleOwner(), userResource -> {
            deviceViewModel.setupDeviceRepository();
            if (userResource.getRequest().getStatus() == org.getcarebase.carebase.utils.Request.Status.SUCCESS) {
                currentHospitalName = userResource.getData().getHospitalName();
                deviceViewModel.setHospitalRepository(userResource.getData().getHospitalId());
            }
            deviceViewModel.getSitesLiveData().observe(getViewLifecycleOwner(), sitesResource -> {
                if(sitesResource.getRequest().getStatus() == org.getcarebase.carebase.utils.Request.Status.SUCCESS) {
                    sitesAdapter.clear();
                    sitesAdapter.addAll(sitesResource.getData().values());
                } else {
                    Log.d(TAG,"Unable to fetch sites");
                    Snackbar.make(rootView, R.string.error_something_wrong, Snackbar.LENGTH_LONG).show();
                }
            });
        });
        destOptions.setAdapter(sitesAdapter);

        handleArguments();

        deviceViewModel.getSaveShipmentRequestLiveData().observe(getViewLifecycleOwner(), request -> {
            if (request.getStatus() == Request.Status.SUCCESS) {
                FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
                View nextView = requireActivity().findViewById(R.id.activity_main);
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.remove(this).commit();
                Snackbar.make(nextView, "Shipment saved to inventory", Snackbar.LENGTH_LONG).show();
            } else {
                Log.d(TAG,"error while saving shipment");
                Snackbar.make(rootView, R.string.error_something_wrong, Snackbar.LENGTH_LONG).show();
            }
        });
        saveButton.setOnClickListener(v -> saveData());

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

    private void saveData() {
        // TODO: Check if inputs are complete and valid
        Shipment shipment = new Shipment();
        shipment.setUdi((String) deviceUdi.getText());
        String di;
        shipment.setDi((di = getArguments().getString("di")) != null ? di : "");
        Integer shippedQuantity = Integer.parseInt(deviceQty.getEditText().getText().toString());
        shipment.setShippedQuantity(shippedQuantity);

        // TODO: Reduce quantities of device model and device production
//        deviceViewModel.getDeviceInFirebaseLiveData().observe(getViewLifecycleOwner(), deviceModelResource -> {
//            DeviceModel deviceModel = deviceModelResource.getData();
//            deviceModel.setQuantity(deviceModel.getQuantity() - shippedQuantity);
//            deviceViewModel.saveDevice(deviceModel);
//        });
        
        deviceViewModel.getSitesLiveData().observe(getViewLifecycleOwner(), sitesResource -> {
            Map<String, String> sitesMap = sitesResource.getData();
            Boolean destSet = false, sourceSet = false;
            for (String id : sitesMap.keySet()) {
                if (Objects.requireNonNull(deviceDest.getEditText()).getText().toString().contentEquals(sitesMap.get(id))) {
                    shipment.setDestHospital(id);
                    deviceViewModel.setHospitalRepository(id);
                    destSet = true;
                }
                if (currentHospitalName.contentEquals(sitesMap.get(id))) {
                    shipment.setSourceHospitalId(id);
                    sourceSet = true;
                }
                if (destSet && sourceSet) break;
            }

            deviceViewModel.saveShipment(shipment);
        });
    }
}