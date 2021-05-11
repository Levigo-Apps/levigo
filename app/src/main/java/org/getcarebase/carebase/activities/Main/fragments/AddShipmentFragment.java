package org.getcarebase.carebase.activities.Main.fragments;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;

import org.getcarebase.carebase.R;
import org.getcarebase.carebase.activities.Main.adapters.AddShipmentAdapter;
import org.getcarebase.carebase.models.Shipment;
import org.getcarebase.carebase.utils.Request;
import org.getcarebase.carebase.utils.Resource;
import org.getcarebase.carebase.viewmodels.DeviceViewModel;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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
        MaterialButton saveButton = rootView.findViewById(R.id.button_submit);
        MaterialToolbar toolbar = rootView.findViewById(R.id.topAppBar);
        toolbar.setNavigationOnClickListener(v -> requireActivity().getSupportFragmentManager().popBackStack());
        itemsRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        Drawable divider = Objects.requireNonNull(ContextCompat.getDrawable(requireContext(),R.drawable.divider));
        DividerItemDecoration itemDecoration = new DividerItemDecoration(requireContext(),DividerItemDecoration.VERTICAL);
        itemDecoration.setDrawable(divider);
        itemsRecyclerView.addItemDecoration(itemDecoration);

        shipmentAdapter = new AddShipmentAdapter();
        itemsRecyclerView.setAdapter(shipmentAdapter);

        String shipment_id = requireArguments().getString("shipment_id");
        shipmentIdTextView.setText(shipment_id);

        saveButton.setOnClickListener(v -> saveReceiveShipment());

       deviceViewModel = new ViewModelProvider(this).get(DeviceViewModel.class);
       deviceViewModel.getUserLiveData().observe(getViewLifecycleOwner(),userResource -> {
           deviceViewModel.setupDeviceRepository();
           deviceViewModel.setupEntityRepository();
           deviceViewModel.getShipment(shipment_id).observe(getViewLifecycleOwner(),this::loadShipment);
           deviceViewModel.getPhysicalLocationsLiveData().observe(getViewLifecycleOwner(),this::loadPhysicalLocations);
           saveButton.setEnabled(true);
       });

       deviceViewModel.getReceiveShipmentRequestLiveData().observe(getViewLifecycleOwner(),request -> {
           if (request.getStatus() == Request.Status.SUCCESS) {
               Snackbar.make(requireActivity().findViewById(R.id.activity_main),request.getResourceString(),Snackbar.LENGTH_LONG).show();
               requireActivity().getSupportFragmentManager().popBackStack();
           } else if (request.getStatus() == Request.Status.ERROR) {
               Snackbar.make(rootView,request.getResourceString(),Snackbar.LENGTH_LONG).show();
           }
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

            // check if shipment already been scanned
            if (shipment.getReceivedTime() != null) {
                Snackbar.make(requireActivity().findViewById(R.id.activity_main),"Shipment has already been scanned",Snackbar.LENGTH_LONG).show();
                requireActivity().getSupportFragmentManager().popBackStack();
            }

            shipmentAdapter.setItems(shipment.getItems());
            shipmentAdapter.notifyDataSetChanged();
        } else if (shipmentResource.getRequest().getStatus() == Request.Status.ERROR) {
            Snackbar.make(rootView,shipmentResource.getRequest().getResourceString(),Snackbar.LENGTH_LONG).show();
        }
    }

    private void saveReceiveShipment() {
        // check if all physical locations are set
        for (Map<String,String> item : shipmentAdapter.getItems()) {
            if (!item.containsKey("physical_location")) {
                Snackbar.make(rootView,"Set the physical location for each device", Snackbar.LENGTH_LONG).show();
                return;
            }
        }

        deviceViewModel.receiveShipment(shipmentAdapter.getItems());
    }
}
