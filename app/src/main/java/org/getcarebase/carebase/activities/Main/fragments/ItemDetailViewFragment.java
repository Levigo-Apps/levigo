 package org.getcarebase.carebase.activities.Main.fragments;

import android.app.Activity;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ItemDetailViewFragment extends Fragment {
    public static final String TAG = ItemDetailViewFragment.class.getName();

    private Activity parent;

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    private String mNetworkId;
    private String mHospitalId;
    private String itemQuantity;
    private String currentDate;
    private String currentTime;
    private DeviceViewModel deviceViewModel;


    private LinearLayout linearLayout;
    private LinearLayout itemSpecsLinearLayout;
    private LinearLayout costLayout;

    private ImageView specificationLayout;
    private ImageView costIcon;
    private TextView itemName;
    private TextView udi;
    private TextView deviceIdentifier;
    private TextView quantity;
    private TextView expiration;
    private TextView hospitalName;
    private TextView physicalLocation;
    private TextView type;
    private TextView usage;
    private TextView medicalSpecialty;
    private TextView referenceNumber;
    private TextView lotNumber;
    private TextView manufacturer;
    private TextView lastUpdate;
    private TextView notes;
    private TextView deviceDescription;;
    private List<Map> costDoc;

    private float dp;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        dp = requireContext().getResources().getDisplayMetrics().density;
        final View rootView = inflater.inflate(R.layout.fragment_viewonlyitemdetail, container, false);
        parent = getActivity();
        MaterialToolbar topToolBar = rootView.findViewById(R.id.topAppBar);
        itemName = rootView.findViewById(R.id.itemname_text);
        udi = rootView.findViewById(R.id.barcode_edittext);
        deviceIdentifier = rootView.findViewById(R.id.di_edittext);
        quantity = rootView.findViewById(R.id.quantity_edittext);
        expiration = rootView.findViewById(R.id.expiration_edittext);
        hospitalName = rootView.findViewById(R.id.site_edittext);
        physicalLocation = rootView.findViewById(R.id.physicallocation_edittext);
        type = rootView.findViewById(R.id.type_edittext);
        usage = rootView.findViewById(R.id.usage_edittext);
        medicalSpecialty = rootView.findViewById(R.id.medicalspecialty_edittext);
        referenceNumber = rootView.findViewById(R.id.referencenumber_edittext);
        lotNumber = rootView.findViewById(R.id.lotnumber_edittext);
        manufacturer = rootView.findViewById(R.id.company_edittext);
        lastUpdate = rootView.findViewById(R.id.lasteupdate_edittext);
        notes = rootView.findViewById(R.id.notes_edittext);
        deviceDescription = rootView.findViewById(R.id.devicedescription_edittext);
        specificationLayout = rootView.findViewById(R.id.specifications_plus);
        linearLayout = rootView.findViewById(R.id.itemdetailviewonly_linearlayout);
        LinearLayout specsLinearLayout = rootView.findViewById(R.id.specs_linearlayout);
        itemSpecsLinearLayout = new LinearLayout(rootView.getContext());
        itemSpecsLinearLayout.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        itemSpecsLinearLayout.setOrientation(LinearLayout.VERTICAL);
        itemSpecsLinearLayout.setVisibility(View.GONE);
        linearLayout.addView(itemSpecsLinearLayout, linearLayout.indexOfChild(specsLinearLayout) + 1);
        costDoc = new ArrayList<>();
        costLayout = rootView.findViewById(R.id.cost_linearlayout);
        costIcon = rootView.findViewById(R.id.cost_plus);
        ImageView shipDevice = rootView.findViewById(R.id.shipEquipment);
        ImageView itemNameEdit = rootView.findViewById(R.id.itemname_edit);

        LinearLayout proceduresDropdown = rootView.findViewById(R.id.procedures_dropdown);
        RecyclerView proceduresRecyclerView = rootView.findViewById(R.id.procedures_recycler_view);
        proceduresRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        proceduresRecyclerView.addItemDecoration(new DividerItemDecoration(getContext(),
                DividerItemDecoration.VERTICAL));
        ImageView procedureToggleButton = rootView.findViewById(R.id.procedures_toggle_button);
        proceduresDropdown.setOnClickListener(view -> {
            if (proceduresRecyclerView.getVisibility() == View.VISIBLE) {
                proceduresRecyclerView.setVisibility(View.GONE);
                procedureToggleButton.setImageResource(R.drawable.ic_baseline_plus);
            } else {
                proceduresRecyclerView.setVisibility(View.VISIBLE);
                procedureToggleButton.setImageResource(R.drawable.icon_minimize);
            }
        });


        deviceViewModel = new ViewModelProvider(this).get(DeviceViewModel.class);

        deviceViewModel.getUserLiveData().observe(getViewLifecycleOwner(), userResource -> {
            deviceViewModel.setupDeviceRepository();
            String barcode = getArguments().getString("barcode");
            udi.setText(barcode);
            String di = getArguments().getString("di");
            deviceViewModel.updateDeviceInFirebaseLiveData(di, barcode);

            deviceViewModel.getDeviceInFirebaseLiveData().observe(getViewLifecycleOwner(), resourceData -> {
                if (resourceData.getRequest().getStatus() == org.getcarebase.carebase.utils.Request.Status.SUCCESS) {
                    DeviceModel deviceModel = resourceData.getData();

                    type.setText(deviceModel.getEquipmentType());
                    hospitalName.setText(deviceModel.getSiteName());
                    String usageStr = deviceModel.getUsage();
                    usage.setText(usageStr);
                    deviceDescription.setText(deviceModel.getDescription());
                    deviceIdentifier.setText(deviceModel.getDeviceIdentifier());
                    medicalSpecialty.setText(deviceModel.getMedicalSpecialty());
                    itemName.setText(deviceModel.getName());
                    manufacturer.setText(deviceModel.getCompany());

                    DeviceProduction deviceProduction = deviceModel.getProductions().get(0);
                    expiration.setText(deviceProduction.getExpirationDate());
                    lotNumber.setText(deviceProduction.getLotNumber());
                    notes.setText(deviceProduction.getNotes());
                    physicalLocation.setText(deviceProduction.getPhysicalLocation());
                    itemQuantity = deviceProduction.getStringQuantity();
                    quantity.setText(itemQuantity);
                    currentDate = deviceProduction.getDateAdded();
                    currentTime = deviceProduction.getTimeAdded();
                    lastUpdate.setText(String.format("%s\n%s", currentDate, currentTime));
                    referenceNumber.setText(deviceProduction.getReferenceNumber());

                    addCostInfo(deviceProduction.getCosts(), rootView);

                    for (Map.Entry<String, Object> specification : deviceModel.getSpecificationList()) {
                        addItemSpecs(specification.getKey(), specification.getValue().toString(), rootView);
                    }

                    DeviceProceduresAdapter deviceProceduresAdapter = new DeviceProceduresAdapter(deviceProduction.getProcedures(),deviceProduction.getUniqueDeviceIdentifier());
                    proceduresRecyclerView.setAdapter(deviceProceduresAdapter);
                }
                else if (resourceData.getRequest().getStatus() == org.getcarebase.carebase.utils.Request.Status.ERROR){
                    Toast.makeText(parent.getApplicationContext(), resourceData.getRequest().getResourceString(), Toast.LENGTH_SHORT).show();
                }
            });
        });

        topToolBar.setNavigationOnClickListener(view -> {
            if (parent != null)
                parent.onBackPressed();
        });

        final boolean[] isSpecsMaximized = {false};
        specificationLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isSpecsMaximized[0]) {
                    isSpecsMaximized[0] = false;
                    itemSpecsLinearLayout.setVisibility(View.GONE);
                    specificationLayout.setImageResource(R.drawable.ic_baseline_plus);

                } else {
                    itemSpecsLinearLayout.setVisibility(View.VISIBLE);
                    specificationLayout.setImageResource(R.drawable.icon_minimize);
                    isSpecsMaximized[0] = true;

                }
            }
        });

        itemNameEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ItemDetailFragment fragment = new ItemDetailFragment();
                Bundle bundle = new Bundle();
                bundle.putString("barcode", Objects.requireNonNull(udi.getText()).toString());
                bundle.putBoolean("editingExisting", true);
                bundle.putString("di", deviceIdentifier.getText().toString());
                fragment.setArguments(bundle);

                FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
//            //clears other fragments
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.setCustomAnimations(R.anim.fui_slide_in_right, R.anim.fui_slide_out_left);
                fragmentTransaction.add(R.id.activity_main, fragment);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
            }
        });

        shipDevice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "ship device clicked");

                // Event isn't fired when the Image is clicked because the line above doesn't execute
                // Don't think below lines are the issue
                ShipDeviceFragment fragment = new ShipDeviceFragment();
                Bundle bundle = new Bundle();
                bundle.putString("name", itemName.getText().toString());
                fragment.setArguments(bundle);

                FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
//            //clears other fragments
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.setCustomAnimations(R.anim.fui_slide_in_right, R.anim.fui_slide_out_left);
                fragmentTransaction.add(R.id.activity_main, fragment);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
            }
        });

        return rootView;
    }

    private void addItemSpecs(String key, String value, View view) {

        LinearLayout eachItemSpecsLayout = new LinearLayout(view.getContext());
        eachItemSpecsLayout.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        eachItemSpecsLayout.setOrientation(LinearLayout.HORIZONTAL);
        eachItemSpecsLayout.setBackgroundColor(Color.WHITE);

        LinearLayout.LayoutParams itemSpecsParams = new LinearLayout.LayoutParams(0,
                LinearLayout.LayoutParams.MATCH_PARENT);
        itemSpecsParams.weight = (float) 1.0;
        itemSpecsParams.setMargins(0, (int) (1 * dp), 0, (int) (1 * dp));


        TextView headerKey = new TextView(view.getContext());
        headerKey.setLayoutParams(itemSpecsParams);
        headerKey.setPadding((int) (8 * dp), (int) (8 * dp), (int) (8 * dp), (int) (8 * dp));
        headerKey.setText(key);
        headerKey.setFocusable(false);
        headerKey.setTypeface(headerKey.getTypeface(), Typeface.BOLD);
        headerKey.setTextSize(14);
        headerKey.setTextColor(Color.BLACK);


        LinearLayout.LayoutParams specValueParams = new LinearLayout.LayoutParams(0,
                LinearLayout.LayoutParams.MATCH_PARENT);
        specValueParams.weight = (float) 1.0;
        specValueParams.setMargins(0, (int) (1 * dp), 0, (int) (1 * dp));

        TextView specsValue = new TextView(view.getContext());
        specsValue.setLayoutParams(specValueParams);
        specsValue.setPadding((int) (8 * dp), (int) (8 * dp), (int) (8 * dp), (int) (8 * dp));
        specsValue.setText(value);
        specsValue.setTextSize(14);
        specsValue.setTextColor(Color.BLACK);

        eachItemSpecsLayout.addView(headerKey);
        eachItemSpecsLayout.addView(specsValue);

        itemSpecsLinearLayout.addView(eachItemSpecsLayout);
    }


    private void addCostInfo(List<Cost> costs,View view){
        if(costs.size() > 0) {
            int i;
            final LinearLayout costInfoLayout = new LinearLayout(view.getContext());
            costInfoLayout.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));
            costInfoLayout.setOrientation(LinearLayout.VERTICAL);
            costInfoLayout.setVisibility(View.GONE);

            for (i = 0; i < costs.size(); i++) {

                final LinearLayout eachEquipmentLayout = new LinearLayout(view.getContext());
                eachEquipmentLayout.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT));
                eachEquipmentLayout.setOrientation(LinearLayout.HORIZONTAL);
                eachEquipmentLayout.setBaselineAligned(false);

                final TextInputLayout costDateHeader = new TextInputLayout(view.getContext());
                LinearLayout.LayoutParams costHeaderParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
                costHeaderParams.weight = (float) 1.0;
                costDateHeader.setLayoutParams(costHeaderParams);
                TextInputEditText dateKey = new TextInputEditText(costDateHeader.getContext());
                dateKey.setBackgroundColor(Color.WHITE);
                dateKey.setText(R.string.purchaseDate_lbl);
                dateKey.setTypeface(dateKey.getTypeface(), Typeface.BOLD);
                dateKey.setFocusable(false);
                costDateHeader.addView(dateKey);


                final TextInputLayout costDateText = new TextInputLayout(view.getContext());
                LinearLayout.LayoutParams costParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
                costParams.weight = (float) 1.0;
                costDateText.setLayoutParams(costParams);
                TextInputEditText dateText = new TextInputEditText(costDateText.getContext());
                dateText.setText(costs.get(i).getCostDate());
                dateText.setBackgroundColor(Color.WHITE);
                dateText.setFocusable(false);
                costDateText.addView(dateText);
                costDateText.setEndIconMode(TextInputLayout.END_ICON_CUSTOM);
                costDateText.setEndIconDrawable(R.drawable.ic_baseline_plus);
                costDateText.setEndIconTintList(ColorStateList.valueOf(getResources().
                        getColor(R.color.colorPrimary, requireActivity().getTheme())));

                eachEquipmentLayout.addView(costDateHeader);
                eachEquipmentLayout.addView(costDateText);
                costInfoLayout.addView(eachEquipmentLayout);


                final LinearLayout costInfoDetails = new LinearLayout(view.getContext());
                costInfoDetails.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT));
                costInfoDetails.setOrientation(LinearLayout.VERTICAL);
                costInfoDetails.setVisibility(View.GONE);

                final LinearLayout packagePriceLinearLayout = new LinearLayout(view.getContext());
                packagePriceLinearLayout.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT));
                packagePriceLinearLayout.setOrientation(LinearLayout.HORIZONTAL);
                packagePriceLinearLayout.setBaselineAligned(false);

                final TextInputLayout packagePriceHeader = new TextInputLayout(view.getContext());
                LinearLayout.LayoutParams packageHeaderParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
                packageHeaderParams.weight = (float) 1.0;
                packagePriceHeader.setLayoutParams(packageHeaderParams);
                TextInputEditText packageKey = new TextInputEditText(packagePriceHeader.getContext());
                packageKey.setBackgroundColor(Color.WHITE);
                packageKey.setText(R.string.packageCost_lbl);
                packageKey.setTypeface(packageKey.getTypeface(), Typeface.BOLD);
                packageKey.setFocusable(false);
                packagePriceHeader.addView(packageKey);

                final TextInputLayout packagePriceText = new TextInputLayout(view.getContext());
                LinearLayout.LayoutParams packageParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
                packageParams.weight = (float) 1.0;
                packagePriceText.setLayoutParams(packageParams);
                TextInputEditText packagePriceEditText = new TextInputEditText(packagePriceText.getContext());
                packagePriceEditText.setText(String.format("$ %s", costs.get(i).getPackagePrice()));
                packagePriceEditText.setBackgroundColor(Color.WHITE);
                packagePriceEditText.setFocusable(false);
                packagePriceText.addView(packagePriceEditText);

                packagePriceLinearLayout.addView(packagePriceHeader);
                packagePriceLinearLayout.addView(packagePriceText);

                final LinearLayout numberAddedLinearLayout = new LinearLayout(view.getContext());
                numberAddedLinearLayout .setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT));
                numberAddedLinearLayout.setOrientation(LinearLayout.HORIZONTAL);
                numberAddedLinearLayout.setBaselineAligned(false);


                final TextInputLayout numberAddedHeader = new TextInputLayout(view.getContext());
                LinearLayout.LayoutParams numberAddedHeaderParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
                numberAddedHeaderParams.weight = (float) 1.0;
                numberAddedHeader.setLayoutParams(numberAddedHeaderParams);
                TextInputEditText numberAddedKey = new TextInputEditText(numberAddedHeader.getContext());
                numberAddedKey.setBackgroundColor(Color.WHITE);
                numberAddedKey.setText(R.string.units_lbl);
                numberAddedKey.setTypeface(numberAddedKey.getTypeface(), Typeface.BOLD);
                numberAddedKey.setFocusable(false);
                numberAddedHeader.addView(numberAddedKey);

                final TextInputLayout numberAddedText = new TextInputLayout(view.getContext());
                LinearLayout.LayoutParams numberAddedParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
                numberAddedParams.weight = (float) 1.0;
                numberAddedText.setLayoutParams(numberAddedParams);
                TextInputEditText numberAddedEditText = new TextInputEditText(numberAddedText.getContext());
                numberAddedEditText.setText(Integer.toString(costs.get(i).getNumberAdded()));
                numberAddedEditText.setBackgroundColor(Color.WHITE);
                numberAddedEditText.setFocusable(false);
                numberAddedText.addView(numberAddedEditText);

                numberAddedLinearLayout.addView(numberAddedHeader);
                numberAddedLinearLayout.addView(numberAddedText);

                final LinearLayout unitCostLinearLayout = new LinearLayout(view.getContext());
                unitCostLinearLayout.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT));
                unitCostLinearLayout.setOrientation(LinearLayout.HORIZONTAL);
                unitCostLinearLayout.setBaselineAligned(false);


                final TextInputLayout unitCostHeader = new TextInputLayout(view.getContext());
                LinearLayout.LayoutParams unitCostParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
                unitCostParams.weight = (float) 1.0;
                unitCostHeader.setLayoutParams(unitCostParams);
                TextInputEditText unitCostKey = new TextInputEditText(unitCostHeader.getContext());
                unitCostKey.setBackgroundColor(Color.WHITE);
                unitCostKey.setText(R.string.costUnit_lbl);
                unitCostKey.setTypeface(dateKey.getTypeface(), Typeface.BOLD);
                unitCostKey.setFocusable(false);
                unitCostHeader.addView(unitCostKey);

                final TextInputLayout unitCostText = new TextInputLayout(view.getContext());
                LinearLayout.LayoutParams unitCostTextParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
                unitCostTextParams.weight = (float) 1.0;
                unitCostText.setLayoutParams(unitCostTextParams);
                TextInputEditText unitCostEditText = new TextInputEditText(unitCostText.getContext());
                unitCostEditText.setText(String.format("$ %s", costs.get(i).getUnitPrice()));
                unitCostEditText.setBackgroundColor(Color.WHITE);
                unitCostEditText.setFocusable(false);
                unitCostText.addView(unitCostEditText);

                unitCostLinearLayout.addView(unitCostHeader);
                unitCostLinearLayout.addView(unitCostText);

                costInfoDetails.addView(packagePriceLinearLayout);
                costInfoDetails.addView(numberAddedLinearLayout);
                costInfoDetails.addView(unitCostLinearLayout);

                costInfoLayout.addView(costInfoDetails);



                final boolean[] isDetailMaximized = {false};
                costDateText.setEndIconOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(isDetailMaximized[0]){
                            costInfoDetails.setVisibility(View.GONE);
                            isDetailMaximized[0] = false;
                            costDateText.setEndIconDrawable(R.drawable.ic_baseline_plus);

                        }else{
                            costInfoDetails.setVisibility(View.VISIBLE);
                            isDetailMaximized[0] = true;
                            costDateText.setEndIconDrawable(R.drawable.icon_minimize);
                        }
                    }
                });

            }
            linearLayout.addView(costInfoLayout, linearLayout.indexOfChild(costLayout) + 1);


            final boolean[] isMaximized = {false};
            costIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(isMaximized[0]){
                        costInfoLayout.setVisibility(View.GONE);
                        isMaximized[0] = false;
                        costIcon.setImageResource(R.drawable.ic_baseline_plus);

                    }else{
                        costInfoLayout.setVisibility(View.VISIBLE);
                        isMaximized[0] = true;
                        costIcon.setImageResource(R.drawable.icon_minimize);
                    }
                }
            });
        }
    }

}
