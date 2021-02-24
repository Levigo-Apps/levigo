package org.getcarebase.carebase.activities.Main.fragments;

import android.app.Activity;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.FirebaseFirestore;

import org.getcarebase.carebase.R;
import org.getcarebase.carebase.activities.Main.adapters.DeviceProceduresAdapter;
import org.getcarebase.carebase.models.Cost;
import org.getcarebase.carebase.models.DeviceModel;
import org.getcarebase.carebase.models.DeviceProduction;
import org.getcarebase.carebase.viewmodels.DeviceViewModel;
import org.getcarebase.carebase.views.DetailLabeledTextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ItemDetailViewFragment extends Fragment {
    public static final String TAG = ItemDetailViewFragment.class.getName();

    private Activity parent;

    private String itemQuantity;
    private String currentDate;
    private String currentTime;
    private DeviceViewModel deviceViewModel;

    private TextView itemName;
    private TextView udi;
    private DetailLabeledTextView deviceIdentifier;
    private DetailLabeledTextView quantity;
    private DetailLabeledTextView expiration;
    private DetailLabeledTextView physicalLocation;
    private DetailLabeledTextView type;
    private DetailLabeledTextView usage;
    private DetailLabeledTextView medicalSpecialty;
    private DetailLabeledTextView referenceNumber;
    private DetailLabeledTextView lotNumber;
    private DetailLabeledTextView manufacturer;
    private DetailLabeledTextView lastUpdate;
    private DetailLabeledTextView notes;
    private DetailLabeledTextView deviceDescription;;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_viewonlyitemdetail, container, false);
        parent = getActivity();
        MaterialToolbar topToolBar = rootView.findViewById(R.id.topAppBar);

        itemName = rootView.findViewById(R.id.itemname_text);
        udi = rootView.findViewById(R.id.udi_edittext);
        deviceIdentifier = rootView.findViewById(R.id.di_edittext);
        quantity = rootView.findViewById(R.id.quantity_edittext);
        expiration = rootView.findViewById(R.id.expiration_edittext);
        physicalLocation = rootView.findViewById(R.id.physicallocation_edittext);
        type = rootView.findViewById(R.id.type_edittext);
        usage = rootView.findViewById(R.id.usage_edittext);
        medicalSpecialty = rootView.findViewById(R.id.medicalspecialty_edittext);
        referenceNumber = rootView.findViewById(R.id.referencenumber_edittext);
        lotNumber = rootView.findViewById(R.id.lotnumber_edittext);
        manufacturer = rootView.findViewById(R.id.company_edittext);
        lastUpdate = rootView.findViewById(R.id.lasteupdate_edittext);
        deviceDescription = rootView.findViewById(R.id.devicedescription_edittext);

        deviceViewModel = new ViewModelProvider(requireActivity()).get(DeviceViewModel.class);

        String barcode = requireArguments().getString("udi");
        String di = requireArguments().getString("di");
        udi.setText(barcode);
        deviceViewModel.updateDeviceInFirebaseLiveData(di, barcode);

        deviceViewModel.getDeviceInFirebaseLiveData().observe(getViewLifecycleOwner(), resourceData -> {
            if (resourceData.getRequest().getStatus() == org.getcarebase.carebase.utils.Request.Status.SUCCESS) {
                DeviceModel deviceModel = resourceData.getData();

                type.setTextValue(deviceModel.getEquipmentType());
                String usageStr = deviceModel.getUsage();
                usage.setTextValue(usageStr);
                deviceDescription.setTextValue(deviceModel.getDescription());
                deviceIdentifier.setTextValue(deviceModel.getDeviceIdentifier());
                medicalSpecialty.setTextValue(deviceModel.getMedicalSpecialty());
                itemName.setText(deviceModel.getName());
                manufacturer.setTextValue(deviceModel.getCompany());

                DeviceProduction deviceProduction = deviceModel.getProductions().get(0);
                expiration.setTextValue(deviceProduction.getExpirationDate());
                lotNumber.setTextValue(deviceProduction.getLotNumber());
                physicalLocation.setTextValue(deviceProduction.getPhysicalLocation());
                itemQuantity = deviceProduction.getStringQuantity();
                quantity.setTextValue(itemQuantity);
                currentDate = deviceProduction.getDateAdded();
                currentTime = deviceProduction.getTimeAdded();
                lastUpdate.setTextValue(String.format("%s\n%s", currentDate, currentTime));
                referenceNumber.setTextValue(deviceProduction.getReferenceNumber());
            }
            else if (resourceData.getRequest().getStatus() == org.getcarebase.carebase.utils.Request.Status.ERROR){
                Toast.makeText(parent.getApplicationContext(), resourceData.getRequest().getResourceString(), Toast.LENGTH_SHORT).show();
            }
        });

        topToolBar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.itemname_edit) {
                showEditDeviceFragment();
                return true;
            }
//            if (item.getItemId() == R.id.item_ship) {
//                showShipDeviceFragment();
//                return true;
//            }
            return false;
        });

        topToolBar.setNavigationOnClickListener(view -> {
            requireActivity().finish();
        });

        return rootView;
    }

    private void showEditDeviceFragment() {
        EditEquipmentFragment fragment = new EditEquipmentFragment();
        Bundle bundle = new Bundle();
        bundle.putString("udi", Objects.requireNonNull(udi.getText().toString()));
        bundle.putString("di", deviceIdentifier.getTextValue().toString());
        fragment.setArguments(bundle);

        FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.setCustomAnimations(R.anim.fui_slide_in_right, R.anim.fui_slide_out_left);
        fragmentTransaction.add(R.id.frame_layout, fragment);
        fragmentTransaction.addToBackStack(TAG);
        fragmentTransaction.commit();
    }

    private void showShipDeviceFragment() {
        ShipDeviceFragment shipFragment = new ShipDeviceFragment();
        Bundle bundle = new Bundle();
        bundle.putString("barcode", Objects.requireNonNull(udi.getText().toString()));
        bundle.putString("qty", quantity.getTextValue().toString());
        bundle.putString("name", itemName.getText().toString());
        shipFragment.setArguments(bundle);

        FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.setCustomAnimations(R.anim.fui_slide_in_right, R.anim.fui_slide_out_left);
        fragmentTransaction.add(R.id.frame_layout, shipFragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

}
