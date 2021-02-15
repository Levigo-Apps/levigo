package org.getcarebase.carebase.activities.Main.fragments;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;

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
import org.getcarebase.carebase.viewmodels.DeviceViewModel;

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

    // Firebase database
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final CollectionReference usersRef = db.collection("users");

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
    private TextInputEditText costEditText;
//    private AutoCompleteTextView physicalLocation;
    private TextInputEditText physicalLocation;
    private TextInputEditText company;
//    private AutoCompleteTextView equipmentType;
    private TextInputEditText medicalSpeciality;
    private TextInputEditText sizeEditText;
    private TextInputEditText lengthEditText;
    private TextInputEditText updateDateEditText;
    private TextInputEditText updateTimeEditText;

    private LinearLayout linearLayout;

    private Button saveButton;

    private String currentDate;
    private String currentTime;

    private String itemQuantity = "0";
    private String diQuantity = "0";
    private int emptySizeFieldCounter = 0;
    private int typeCounter;
    private int siteCounter;
    private int locCounter;
    private boolean chosenType;
    private boolean chosenLocation;
    private boolean isAddSizeButtonClicked;
    private boolean chosenSite;
    private boolean checkEditTexts;
    private boolean checkAutocompleteTexts;
    private boolean isProcedureInfoReturned;
    private boolean isUdisReturned;
    private boolean editingExisting;
    private boolean isDi;
    private List<TextInputEditText> allSizeOptions;
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

    private float dp;

    private DeviceViewModel deviceViewModel;
    private View rootView;

//    public EditEquipmentFragment() {
//        // Required empty public constructor
//    }
//
//    /**
//     * Use this factory method to create a new instance of
//     * this fragment using the provided parameters.
//     *
//     * @param param1 Parameter 1.
//     * @param param2 Parameter 2.
//     * @return A new instance of fragment EditEquipmentFragment.
//     */
//    // TODO: Rename and change types and number of parameters
//    public static EditEquipmentFragment newInstance(String param1, String param2) {
//        EditEquipmentFragment fragment = new EditEquipmentFragment();
//        Bundle args = new Bundle();
//        args.putString(ARG_PARAM1, param1);
//        args.putString(ARG_PARAM2, param2);
//        fragment.setArguments(args);
//        return fragment;
//    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        dp = requireContext().getResources().getDisplayMetrics().density;
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_edit_equipment, container, false);
        myCalendar = Calendar.getInstance();
        parent = getActivity();
        linearLayout = rootView.findViewById(R.id.editequipment_linearlayout);
        udiEditText = rootView.findViewById(R.id.detail_udi);
        nameEditText = rootView.findViewById(R.id.detail_name);
        equipmentType = rootView.findViewById(R.id.detail_type);
        deviceIdentifier = rootView.findViewById(R.id.detail_di);
        quantity = rootView.findViewById(R.id.detail_quantity);
        lotNumber = rootView.findViewById(R.id.detail_lot_number);
        expiration = rootView.findViewById(R.id.detail_expiration_date);
        usageEditText = rootView.findViewById(R.id.detail_usage);
        costEditText = rootView.findViewById(R.id.detail_equipment_cost);
        physicalLocation = rootView.findViewById(R.id.detail_physical_location);
        company = rootView.findViewById(R.id.detail_company);
        medicalSpeciality = rootView.findViewById(R.id.detail_medical_speciality);
        sizeEditText = rootView.findViewById(R.id.detail_size);
        lengthEditText = rootView.findViewById(R.id.detail_length);
        updateDateEditText = rootView.findViewById(R.id.detail_update_date);
        updateTimeEditText = rootView.findViewById(R.id.detail_update_time);
        saveButton = rootView.findViewById(R.id.detail_save_button);

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        updateDateEditText.setText(dateFormat.format(new Date()));
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss", Locale.US);
        updateTimeEditText.setText(timeFormat.format(new Date()));
        TextInputLayout expirationTextLayout = rootView.findViewById(R.id.expiration_date_string);

        MaterialToolbar toolBar = rootView.findViewById(R.id.toolbar);

//        siteConstrainLayout = rootView.findViewById(R.id.site_linearlayout);
//        physicalLocationConstrainLayout = rootView.findViewById(R.id.physicalLocationLinearLayout);
//        typeConstrainLayout = rootView.findViewById(R.id.typeLinearLayout);
//        chosenType = false;
//        chosenSite = false;
//        isProcedureInfoReturned = false;
//        chosenLocation = false;
//        checkAutocompleteTexts = false;
//        checkEditTexts = false;
//        isUdisReturned = false;
//        isAddSizeButtonClicked = true;
//        editingExisting = false;


        deviceViewModel = new ViewModelProvider(this).get(DeviceViewModel.class);
        // if it is viable, find a way to get the user from inventoryViewModel from main activity
        // instead of waiting again to get the user again
        deviceViewModel.getUserLiveData().observe(getViewLifecycleOwner(), userResource -> {
            deviceViewModel.setupDeviceRepository();
            String udi = getArguments().getString("udi");
            String di = getArguments().getString("di");
            udiEditText.setText(udi);
            deviceViewModel.updateDeviceInFirebaseLiveData(di, udi);
            deviceViewModel.getDeviceInFirebaseLiveData().observe(getViewLifecycleOwner(), resourceData -> {
                if (resourceData.getRequest().getStatus() == org.getcarebase.carebase.utils.Request.Status.SUCCESS) {
                    DeviceModel deviceModel = resourceData.getData();

                    nameEditText.setText(deviceModel.getName());
                    nameEditText.setEnabled(deviceModel.getName() == null);
                    equipmentType.setText(deviceModel.getEquipmentType());
                    deviceIdentifier.setText(deviceModel.getDeviceIdentifier());
                    deviceIdentifier.setEnabled(deviceModel.getDeviceIdentifier() == null);
                    String usageStr = deviceModel.getUsage();
                    usageEditText.setText(usageStr);
                    company.setText(deviceModel.getCompany());
                    company.setEnabled(deviceModel.getCompany() == null);
                    medicalSpeciality.setText(deviceModel.getMedicalSpecialty());

                    DeviceProduction deviceProduction = deviceModel.getProductions().get(0);
                    itemQuantity = deviceProduction.getStringQuantity();
                    quantity.setText(itemQuantity);
                    quantity.setEnabled(false);
                    lotNumber.setText(deviceProduction.getLotNumber());
                    lotNumber.setEnabled(deviceProduction.getLotNumber() == null);
                    expiration.setText(deviceProduction.getExpirationDate());
                    expiration.setEnabled(deviceProduction.getExpirationDate() == null);
                    physicalLocation.setText(deviceProduction.getPhysicalLocation());
                    currentDate = deviceProduction.getDateAdded();
                    currentTime = deviceProduction.getTimeAdded();
                    updateDateEditText.setText(currentDate);
                    updateDateEditText.setEnabled(false);
                    updateTimeEditText.setText(currentTime);
                    updateTimeEditText.setEnabled(false);
                }
                else if (resourceData.getRequest().getStatus() == org.getcarebase.carebase.utils.Request.Status.ERROR){
                    Toast.makeText(parent.getApplicationContext(), resourceData.getRequest().getResourceString(), Toast.LENGTH_SHORT).show();
                }
            });
//            setupOptionFields();
//            setupAutoPopulate();
//            setupSaveDevice();
//            handleArguments();
        });

//        // NumberPicker Dialog for NumberAdded field
//        numberAdded.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                showNumberPicker(rootView, numberAdded);
//            }
//        });


//        // incrementing number by 1 when clicked on the end icon
//        numberAddedLayout.setEndIconOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                incrementNumberAdded();
//            }
//        });

//        autoPopulateButton.setOnClickListener(view -> {
//            String barcode = Objects.requireNonNull(udiEditText.getText()).toString().trim();
//            if (!barcode.isEmpty()) {
//                deviceViewModel.autoPopulatedScannedBarcode(barcode);
//                hideKeyboard();
//            }
//        });

//        addSizeButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                addEmptySizeOption(view);
//            }
//        });


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

        // date picker for expiration date if entered manually
        final DatePickerDialog.OnDateSetListener date_exp = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
                myCalendar.set(Calendar.YEAR, i);
                myCalendar.set(Calendar.MONTH, i1);
                myCalendar.set(Calendar.DAY_OF_MONTH, i2);
                String myFormat = "yyyy/MM/dd";
                SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
                expiration.setText(String.format("%s", sdf.format(myCalendar.getTime())));
            }
        };

        expirationTextLayout.setEndIconOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new DatePickerDialog(view.getContext(), date_exp, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        // saves data into database
        saveButton.setOnClickListener(v -> saveData());
        return rootView;
    }

    private void saveData() {
        DeviceModel deviceModel = isFieldsValid();
        if (deviceModel != null) {
            deviceViewModel.saveDevice(deviceModel);
        } else {
            Snackbar.make(rootView, "Please fill all required fields", Snackbar.LENGTH_LONG).show();
        }
    }

    // not in mvvm style - need to use data bindings
    // packages all the fields into a DeviceModel object
    private DeviceModel isFieldsValid() {
        boolean isValid = true;

        List<EditText> requiredEditTexts = new ArrayList<>(allSizeOptions);
        // style, cost, size, length are not listed
        requiredEditTexts.addAll(Arrays.asList(udiEditText, nameEditText, deviceIdentifier,
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
            int currentQuantity = Integer.parseInt(Objects.requireNonNull(quantity.getText()).toString());
            deviceModel.setQuantity(currentQuantity);
            deviceModel.setUsage(usageEditText.getText().toString().trim());

            if (allSizeOptions.size() > 0) {
                int i = 0;
                while (i < allSizeOptions.size()) {
                    String key = Objects.requireNonNull(allSizeOptions.get(i++).getText()).toString().trim();
                    String value = Objects.requireNonNull(allSizeOptions.get(i++).getText()).toString().trim();
                    deviceModel.addSpecification(key,value);
                }
            }

            DeviceProduction deviceProduction = new DeviceProduction();
            deviceProduction.setUniqueDeviceIdentifier(Objects.requireNonNull(udiEditText.getText()).toString().trim());
            deviceProduction.setExpirationDate(Objects.requireNonNull(expiration.getText()).toString().trim());
            deviceProduction.setLotNumber(Objects.requireNonNull(lotNumber.getText()).toString().trim());
            deviceProduction.setPhysicalLocation(physicalLocation.getText().toString().trim());
            deviceModel.addDeviceProduction(deviceProduction);

            return deviceModel;
        }
        return null;
    }

}