package org.getcarebase.carebase.activities.Main.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;

import org.getcarebase.carebase.R;
import org.getcarebase.carebase.activities.Main.adapters.AddShipmentAdapter;
import org.getcarebase.carebase.models.Shipment;
import org.getcarebase.carebase.utils.Request;
import org.getcarebase.carebase.utils.Resource;
import org.getcarebase.carebase.viewmodels.DeviceViewModel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class AddShipmentFragment extends Fragment {
    public static final String TAG = AddShipmentFragment.class.getSimpleName();

    private View rootView;
    private AddShipmentAdapter shipmentAdapter;

    private DeviceViewModel deviceViewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_add_shipment,container,false);
        TextView shipmentIdTextView = rootView.findViewById(R.id.shipment_id_field_value);
        RecyclerView itemsRecyclerView = rootView.findViewById(R.id.recycler_view);
        itemsRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        shipmentAdapter = new AddShipmentAdapter();
        itemsRecyclerView.setAdapter(shipmentAdapter);

        String shipment_id = requireArguments().getString("shipment_id");
        shipmentIdTextView.setText(shipment_id);

       deviceViewModel = new ViewModelProvider(this).get(DeviceViewModel.class);
       deviceViewModel.getUserLiveData().observe(getViewLifecycleOwner(),userResource -> {
           deviceViewModel.setupDeviceRepository();
           deviceViewModel.setupEntityRepository();
           deviceViewModel.getShipment(shipment_id).observe(getViewLifecycleOwner(),this::loadShipment);
           deviceViewModel.getPhysicalLocationsLiveData().observe(getViewLifecycleOwner(),this::loadPhysicalLocations);
       });

        return rootView;
    }

    private void loadPhysicalLocations(Resource<String[]> physicalLocationsResource) {
        if (physicalLocationsResource.getRequest().getStatus() == Request.Status.SUCCESS) {
            List<String> physicalLocations = Arrays.asList(physicalLocationsResource.getData());
            ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),R.layout.dropdown_menu_popup_item,physicalLocations);
            shipmentAdapter.setPhysicalLocationAdapter(adapter);
            shipmentAdapter.notifyDataSetChanged();
        } else if (physicalLocationsResource.getRequest().getStatus() == Request.Status.ERROR) {
            Snackbar.make(rootView,physicalLocationsResource.getRequest().getResourceString(),Snackbar.LENGTH_LONG).show();
        }
    }

    private void loadShipment(Resource<Shipment> shipmentResource) {
        if (shipmentResource.getRequest().getStatus() == Request.Status.SUCCESS){
            Shipment shipment = shipmentResource.getData();
            shipmentAdapter.setItems(shipment.getItems());
            shipmentAdapter.notifyDataSetChanged();
        } else if (shipmentResource.getRequest().getStatus() == Request.Status.ERROR) {
            Snackbar.make(rootView,shipmentResource.getRequest().getResourceString(),Snackbar.LENGTH_LONG).show();
        }
    }
}
