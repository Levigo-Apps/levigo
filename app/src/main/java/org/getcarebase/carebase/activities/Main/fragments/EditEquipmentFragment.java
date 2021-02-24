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
    private TextInputEditText equipmentType;
    private TextInputEditText deviceIdentifier;
    private TextInputEditText quantity;
    private TextInputEditText lotNumber;
    private TextInputEditText expiration;
    private TextInputEditText usageEditText;
//    private AutoCompleteTextView physicalLocation;
    private TextInputEditText physicalLocation;
    private TextInputEditText company;
//    private AutoCompleteTextView equipmentType;
    private TextInputEditText medicalSpeciality;
    private TextInputEditText updateDateEditText;
    private TextInputEditText updateTimeEditText;

    private Button saveButton;

    private String currentDate;
    private String currentTime;

    private int modelQuantityBeforeEdit;
    private int productionQuantityBeforeEdit;

    // TODO remove hardcoded fields
    private final List<String> DEVICE_TYPES = Arrays.asList(
            "Balloon",
            "Biliary Stent",
            "Brush",
            "Catheter",
            "Catheter Extraction Tool",
            "Catheter Securement Device",
            "Compression Device",
            "Central Venous Access",
            "Chest (Bag, Catheter, Pneumo Kit, Thoracentesis Kit)",
            "Coaxial Needle",
            "Core Biopsy Gun",
            "CT Biopsy Grid",
            "Dilator",
            "Drainage Bags, Kits, Tubes",
            "Embolization (coils, microcoils, gel foam, particles)",
            "Equipment",
            "Flow Switch",
            "Footballs",
            "Gastro Equipment (feeding tube)",
            "Gloves",
            "Gown",
            "Guide Sheath",
            "Inflation Device",
            "Introducer Sheath",
            "Lidocaine",
            "Micropuncture Kit",
            "Needle",
            "Nephro Tubes/Stents",
            "Non-vascular Access Kit",
            "Patient Cover",
            "Picc line",
            "Pneumothorax Kit/Flesh Kit",
            "Povidone",
            "Scalpel",
            "Sheath",
            "Sleeve",
            "Snare",
            "Stents and Embolization Coils",
            "Sterile Tray",
            "Stopcock",
            "Tube",
            "Wire",
            "Ultrasound/Imaging related",
            "Venous Access (Catheters, Central Lines, Introducers, Tunnelers)");
    private final List<String> PHYSICAL_LOCATIONS = Arrays.asList(
            "Box - Central Lines",
            "Box - Picc Lines",
            "Box - Tunnels/ports",
            "Box - Short Wires",
            "Box - Perma dialysis",
            "Box - Triple lumen dialysis",
            "Box - Other permacath",
            "Box - Microcath",
            "Box - Biopsy",
            "Cabinet 1",
            "Cabinet 2",
            "Cabinet 3",
            "Hanger - drainage cath",
            "Hanger - Nephrostemy",
            "Hanger - Misc catheters",
            "Hanger - 4 french catheters",
            "Hanger - 5 french catheters",
            "Hanger - Kumpe - 5 french",
            "Hanger - Drainage tube",
            "Hanger - Biliary catheters",
            "Hanger - Specialized sheaths/introducers",
            "Shelf - G J Tube",
            "Shelf - Lung Biopsy, Flesh Kit",
            "Shelf - Micropuncture sets/Wires",
            "Other");

    private LinearLayout siteConstrainLayout;
    private LinearLayout physicalLocationConstrainLayout;
    private LinearLayout typeConstrainLayout;

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
        deviceIdentifier = rootView.findViewById(R.id.detail_di);
        quantity = rootView.findViewById(R.id.detail_quantity);
        lotNumber = rootView.findViewById(R.id.detail_lot_number);
        expiration = rootView.findViewById(R.id.detail_expiration_date);
        usageEditText = rootView.findViewById(R.id.detail_usage);
        physicalLocation = rootView.findViewById(R.id.detail_physical_location);
        company = rootView.findViewById(R.id.detail_company);
        medicalSpeciality = rootView.findViewById(R.id.detail_medical_speciality);
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
            deviceIdentifier.setText(deviceModel.getDeviceIdentifier());
            String usageStr = deviceModel.getUsage();
            usageEditText.setText(usageStr);
            company.setText(deviceModel.getCompany());
            medicalSpeciality.setText(deviceModel.getMedicalSpecialty());
            modelQuantityBeforeEdit = deviceModel.getQuantity();

            DeviceProduction deviceProduction = deviceModel.getProductions().get(0);
            udiEditText.setText(deviceProduction.getUniqueDeviceIdentifier());
            productionQuantityBeforeEdit = deviceProduction.getQuantity();
            quantity.setText(deviceProduction.getStringQuantity());
            lotNumber.setText(deviceProduction.getLotNumber());
            expiration.setText(deviceProduction.getExpirationDate());
            physicalLocation.setText(deviceProduction.getPhysicalLocation());
            currentDate = deviceProduction.getDateAdded();
            currentTime = deviceProduction.getTimeAdded();
            updateDateEditText.setText(currentDate);
            updateTimeEditText.setText(currentTime);

            udiEditText.setEnabled(false);
            deviceIdentifier.setEnabled(deviceModel.getDeviceIdentifier() == null);
            lotNumber.setEnabled(deviceProduction.getLotNumber() == null);
            expiration.setEnabled(deviceProduction.getExpirationDate() == null);
            usageEditText.setEnabled(deviceModel.getUsage() == null);
            physicalLocation.setEnabled(deviceProduction.getPhysicalLocation() == null);
            company.setEnabled(deviceModel.getCompany() == null);
            medicalSpeciality.setEnabled(deviceModel.getMedicalSpecialty() == null);
            updateDateEditText.setEnabled(false);
            updateTimeEditText.setEnabled(false);
        }
        else if (deviceModelResource.getRequest().getStatus() == Request.Status.ERROR) {
            Snackbar.make(rootView, deviceModelResource.getRequest().getResourceString(), Snackbar.LENGTH_LONG).show();
        }

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
                returnIntent.putExtra("edit", true);
                requireActivity().setResult(Activity.RESULT_OK,returnIntent);
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

        // style, cost, size, length are not listed
        List<EditText> requiredEditTexts = new ArrayList<>(Arrays.asList(udiEditText, nameEditText, deviceIdentifier,
                quantity, lotNumber, expiration, usageEditText, physicalLocation, company,
                equipmentType, medicalSpeciality));
        for (EditText editText : requiredEditTexts) {
            if (editText.getText().toString().trim().isEmpty()) {
                isValid = false;
            }
        }

        if (isValid) {
            DeviceModel deviceModel = new DeviceModel();
            deviceModel.setDeviceIdentifier(Objects.requireNonNull(deviceIdentifier.getText()).toString().trim());
            deviceModel.setName(Objects.requireNonNull(nameEditText.getText()).toString().trim());
            deviceModel.setCompany(Objects.requireNonNull(company.getText()).toString().trim());
            deviceModel.setEquipmentType(equipmentType.getText().toString().trim());
            deviceModel.setMedicalSpecialty(Objects.requireNonNull(medicalSpeciality.getText()).toString().trim());
            int currentProductionQuantity = Integer.parseInt(Objects.requireNonNull(quantity.getText()).toString());
            int quantityDifference = currentProductionQuantity - productionQuantityBeforeEdit;
            deviceModel.setQuantity(modelQuantityBeforeEdit+quantityDifference);
            deviceModel.setUsage(usageEditText.getText().toString().trim());

            DeviceProduction deviceProduction = new DeviceProduction();
            deviceProduction.setUniqueDeviceIdentifier(Objects.requireNonNull(udiEditText.getText()).toString().trim());
            deviceProduction.setExpirationDate(Objects.requireNonNull(expiration.getText()).toString().trim());
            deviceProduction.setLotNumber(Objects.requireNonNull(lotNumber.getText()).toString().trim());
            deviceProduction.setPhysicalLocation(physicalLocation.getText().toString().trim());
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