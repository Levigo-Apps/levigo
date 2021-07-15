package org.getcarebase.carebase.activities.Main.fragments;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.zxing.integration.android.IntentIntegrator;
import com.journeyapps.barcodescanner.CaptureActivity;

import org.getcarebase.carebase.R;
import org.getcarebase.carebase.activities.Main.MainActivity;
import org.getcarebase.carebase.models.Cost;
import org.getcarebase.carebase.models.DeviceModel;
import org.getcarebase.carebase.models.DeviceProduction;
import org.getcarebase.carebase.models.PendingDevice;
import org.getcarebase.carebase.utils.Request;
import org.getcarebase.carebase.utils.Resource;
import org.getcarebase.carebase.viewmodels.DeviceViewModel;
import org.getcarebase.carebase.viewmodels.ProcedureViewModel;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;


public class EditEquipmentFragment extends Fragment {

    private static final String TAG = EditEquipmentFragment.class.getSimpleName();
    private Activity parent;
    private Calendar myCalendar;

    // USER INPUT VALUES
    private TextInputEditText udiEditText;
    private TextInputEditText nameEditText;
    private AutoCompleteTextView equipmentType;
    private AutoCompleteTextView subTypeTextView;
    private TextInputLayout subTypeLayout;
    private TextInputEditText deviceIdentifier;
    private TextInputEditText quantity;
    private TextInputEditText lotNumber;
    private TextInputEditText expiration;
    private TextInputEditText company;
//    private AutoCompleteTextView equipmentType;
    private TextInputEditText updateDateEditText;
    private TextInputEditText updateTimeEditText;

    private Button saveButton;

    private String currentDate;
    private String currentTime;

    private int modelQuantityBeforeEdit;
    private int productionQuantityBeforeEdit;

    private DeviceViewModel deviceViewModel;
    private View rootView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_edit_equipment, container, false);
        myCalendar = Calendar.getInstance();
        parent = getActivity();
        udiEditText = rootView.findViewById(R.id.detail_udi);
        nameEditText = rootView.findViewById(R.id.detail_name);
        equipmentType = rootView.findViewById(R.id.detail_type);
        subTypeTextView = rootView.findViewById(R.id.detail_subtype);
        subTypeLayout = rootView.findViewById(R.id.detail_subtype_layout);
        deviceIdentifier = rootView.findViewById(R.id.detail_di);
        quantity = rootView.findViewById(R.id.detail_quantity);
        lotNumber = rootView.findViewById(R.id.detail_lot_number);
        expiration = rootView.findViewById(R.id.detail_expiration_date);
        company = rootView.findViewById(R.id.detail_company);
        updateDateEditText = rootView.findViewById(R.id.detail_update_date);
        updateTimeEditText = rootView.findViewById(R.id.detail_update_time);
        saveButton = rootView.findViewById(R.id.detail_save_button);

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        updateDateEditText.setText(dateFormat.format(new Date()));
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss", Locale.US);
        updateTimeEditText.setText(timeFormat.format(new Date()));

        MaterialToolbar toolBar = rootView.findViewById(R.id.toolbar);

        deviceViewModel = new ViewModelProvider(requireActivity()).get(DeviceViewModel.class);
        setupSaveDevice();
        Resource<DeviceModel> deviceModelResource = deviceViewModel.getDeviceInFirebaseLiveData().getValue();
        if (deviceModelResource != null && deviceModelResource.getRequest().getStatus() == Request.Status.SUCCESS) {
            DeviceModel deviceModel = deviceModelResource.getData();

            nameEditText.setText(deviceModel.getName());
            equipmentType.setText(deviceModel.getEquipmentType());
            if (deviceModel.getSubType() != null) {
                subTypeTextView.setText(deviceModel.getSubType());
                subTypeLayout.setVisibility(View.VISIBLE);
            }
            deviceIdentifier.setText(deviceModel.getDeviceIdentifier());;
            company.setText(deviceModel.getCompany());
            modelQuantityBeforeEdit = deviceModel.getQuantity();

            DeviceProduction deviceProduction = deviceModel.getProductions().get(0);
            udiEditText.setText(deviceProduction.getUniqueDeviceIdentifier());
            productionQuantityBeforeEdit = deviceProduction.getQuantity();
            quantity.setText(deviceProduction.getStringQuantity());
            lotNumber.setText(deviceProduction.getLotNumber());
            expiration.setText(deviceProduction.getExpirationDate());
            currentDate = deviceProduction.getDateAdded();
            currentTime = deviceProduction.getTimeAdded();
            updateDateEditText.setText(currentDate);
            updateTimeEditText.setText(currentTime);

            udiEditText.setEnabled(false);
            deviceIdentifier.setEnabled(deviceModel.getDeviceIdentifier() == null);
            lotNumber.setEnabled(deviceProduction.getLotNumber() == null);
            expiration.setEnabled(deviceProduction.getExpirationDate() == null);
            company.setEnabled(deviceModel.getCompany() == null);
            updateDateEditText.setEnabled(false);
            updateTimeEditText.setEnabled(false);
        }
        else if (deviceModelResource.getRequest().getStatus() == Request.Status.ERROR) {
            Snackbar.make(rootView, deviceModelResource.getRequest().getResourceString(), Snackbar.LENGTH_LONG).show();
        }

        // set up device types
        final ArrayAdapter<String> deviceTypeAdapter = new ArrayAdapter<>(rootView.getContext(), R.layout.dropdown_menu_popup_item,new ArrayList<>());
        final ArrayAdapter<String> subTypeAdapter = new ArrayAdapter<>(rootView.getContext(),R.layout.dropdown_menu_popup_item,new ArrayList<>());
        subTypeTextView.setAdapter(subTypeAdapter);
        Map<String,List<String>> deviceTypes = deviceViewModel.getDeviceTypes();
        deviceTypeAdapter.addAll(deviceTypes.keySet());
        equipmentType.setAdapter(deviceTypeAdapter);

        equipmentType.setOnItemClickListener((parent, view, position, id) -> {
            String type = parent.getItemAtPosition(position).toString();
            subTypeAdapter.clear();
            if (deviceTypes.get(type) != null) {
                // make subtype text field appear
                subTypeAdapter.addAll(deviceTypes.get(type));
                subTypeLayout.setVisibility(View.VISIBLE);
            } else {
                // make subtype text field disappear
                subTypeLayout.setVisibility(View.GONE);
            }
            subTypeAdapter.notifyDataSetChanged();
            subTypeTextView.setText("",false);
        });

        //going back to inventory view
        toolBar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (requireActivity().getSupportFragmentManager().getBackStackEntryCount() > 0) {
                    requireActivity().getSupportFragmentManager().popBackStack();
                } else {
                    if (parent != null) {
                        parent.onBackPressed();
                    }
                }
            }
        });

        // saves data into database
        saveButton.setOnClickListener(v -> saveData());
        return rootView;
    }

    private void setupSaveDevice() {
        deviceViewModel.getSaveDeviceRequestLiveData().observe(getViewLifecycleOwner(),event -> {
            Request request = event.getContentIfNotHandled();
            if (request == null) {
                return;
            }
            if (request.getStatus() == org.getcarebase.carebase.utils.Request.Status.SUCCESS) {
                Fragment fragment = Objects.requireNonNull(requireActivity().getSupportFragmentManager().findFragmentByTag(ItemDetailViewFragment.TAG));
                requireActivity().getSupportFragmentManager().popBackStack();
                Intent returnIntent = new Intent();
                requireActivity().setResult(MainActivity.RESULT_EDITED,returnIntent);
                Snackbar.make(fragment.requireView(), "Edits saved to inventory", Snackbar.LENGTH_LONG).show();
            } else {
                Log.d(TAG,"error while saving changes");
                Snackbar.make(rootView, R.string.error_something_wrong, Snackbar.LENGTH_LONG).show();
            }
        });
    }

    private void saveData() {
        DeviceModel deviceModel = isFieldsValid();
        if (deviceModel != null) {
            deviceViewModel.saveDevice(deviceModel);
        }
    }

    // not in mvvm style - need to use data bindings
    // packages all the fields into a DeviceModel object
    private DeviceModel isFieldsValid() {
        if ( !isPositiveInteger(quantity) ) {
            Snackbar.make(rootView, "Invalid input in quantity field", Snackbar.LENGTH_LONG).show();
            return null;
        }

        boolean isValid = true;

        List<EditText> requiredEditTexts = new ArrayList<>(Arrays.asList(udiEditText, nameEditText, deviceIdentifier,
                quantity, expiration, company,
                equipmentType));
        for (EditText editText : requiredEditTexts) {
            if (editText.getText().toString().trim().isEmpty()) {
                isValid = false;
            }
        }

        // check that equipment types and sub types are valid
        Map<String, List<String>> deviceTypes = deviceViewModel.getDeviceTypes();
        List<String> subTypes = null;
        if (deviceTypes.containsKey(equipmentType.getText().toString())) {
            subTypes = deviceTypes.get(equipmentType.getText().toString());
            if (subTypes != null && !subTypes.contains(subTypeTextView.getText().toString())) {
                isValid = false;
            }
        } else {
            isValid = false;
        }

        if (isValid) {
            DeviceModel deviceModel = new DeviceModel();
            deviceModel.setDeviceIdentifier(Objects.requireNonNull(deviceIdentifier.getText()).toString().trim());
            deviceModel.setName(Objects.requireNonNull(nameEditText.getText()).toString().trim());
            deviceModel.setCompany(Objects.requireNonNull(company.getText()).toString().trim());
            deviceModel.setEquipmentType(equipmentType.getText().toString().trim());
            if (subTypes != null) deviceModel.setSubType(subTypeTextView.getText().toString().trim());
            int currentProductionQuantity = Integer.parseInt(Objects.requireNonNull(quantity.getText()).toString());
            int quantityDifference = currentProductionQuantity - productionQuantityBeforeEdit;
            deviceModel.setQuantity(modelQuantityBeforeEdit+quantityDifference);

            DeviceProduction deviceProduction = new DeviceProduction();
            deviceProduction.setUniqueDeviceIdentifier(Objects.requireNonNull(udiEditText.getText()).toString().trim());
            deviceProduction.setExpirationDate(Objects.requireNonNull(expiration.getText()).toString().trim());
            deviceProduction.setLotNumber(Objects.requireNonNull(lotNumber.getText()).toString().trim());
            // set to number difference because FieldValue.increment() is used in DeviceProduction toMap()
            deviceProduction.setQuantity(quantityDifference);
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            deviceProduction.setDateAdded(dateFormat.format(myCalendar.getTime()));
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss", Locale.US);
            deviceProduction.setTimeAdded(timeFormat.format(myCalendar.getTime()));
            deviceModel.addDeviceProduction(deviceProduction);

            return deviceModel;
        } else {
            Snackbar.make(rootView, "Please fill all required fields", Snackbar.LENGTH_LONG).show();
        }
        return null;
    }

    private boolean isPositiveInteger(TextInputEditText input) {
        try {
            return Integer.parseInt(input.getText().toString()) > 0;
        } catch (Exception e) {
            Snackbar.make(rootView, "Invalid input in field Quantity! Please enter positive integer.", Snackbar.LENGTH_LONG).show();
            return false;
        }
    }

}