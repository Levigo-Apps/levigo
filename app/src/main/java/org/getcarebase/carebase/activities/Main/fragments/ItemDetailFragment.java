package org.getcarebase.carebase.activities.Main.fragments;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.TimePicker;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.zxing.integration.android.IntentIntegrator;
import com.journeyapps.barcodescanner.CaptureActivity;

import org.getcarebase.carebase.R;;
import org.getcarebase.carebase.models.Cost;
import org.getcarebase.carebase.models.DeviceModel;
import org.getcarebase.carebase.models.DeviceProduction;
import org.getcarebase.carebase.models.PendingDevice;
import org.getcarebase.carebase.utils.Request;
import org.getcarebase.carebase.viewmodels.DeviceViewModel;
import org.getcarebase.carebase.viewmodels.ProcedureViewModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

public class ItemDetailFragment extends Fragment {

    private String mNetworkId;
    private String mHospitalId;
    private String mUser;
    private String mHospitalName;

    // Firebase database
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final CollectionReference usersRef = db.collection("users");

    private static final String TAG = ItemDetailFragment.class.getSimpleName();
    private Activity parent;
    private Calendar myCalendar;

    // USER INPUT VALUES
    private TextInputEditText udiEditText;
    private TextInputEditText nameEditText;
    private AutoCompleteTextView equipmentType;
    private TextInputEditText company;
    private TextInputEditText otherType_text;
    private TextInputEditText deviceIdentifier;
    private TextInputEditText deviceDescription;
    private TextInputEditText expiration;
    private TextInputEditText quantity;
    private TextInputEditText lotNumber;
    private TextInputEditText referenceNumber;
    private AutoCompleteTextView physicalLocation;
    private TextInputEditText notes;
    private TextInputEditText dateIn;
    private TextInputEditText timeIn;
    private TextInputEditText numberAdded;
    private TextView specsTextView;
    private LinearLayout linearLayout;
    private TextInputEditText costEditText;

    private Button saveButton;
    private MaterialButton removeSizeButton;
    private RadioGroup useRadioGroup;
    private RadioButton singleUseButton;
    private RadioButton multiUse;
    private Button addSizeButton;

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

    private LinearLayout physicalLocationConstrainLayout;
    private LinearLayout typeConstrainLayout;

    private float dp;

    private DeviceViewModel deviceViewModel;
    private View rootView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        dp = requireActivity().getResources().getDisplayMetrics().density;
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_itemdetail, container, false);
        myCalendar = Calendar.getInstance();
        parent = getActivity();
        linearLayout = rootView.findViewById(R.id.itemdetail_linearlayout);
        udiEditText = rootView.findViewById(R.id.detail_udi);
        nameEditText = rootView.findViewById(R.id.detail_name);
        equipmentType = rootView.findViewById(R.id.detail_type);
        company = rootView.findViewById(R.id.detail_company);
        expiration = rootView.findViewById(R.id.detail_expiration_date);
        physicalLocation = rootView.findViewById(R.id.detail_physical_location);
        notes = rootView.findViewById(R.id.detail_notes);
        lotNumber = rootView.findViewById(R.id.detail_lot_number);
        referenceNumber = rootView.findViewById(R.id.detail_reference_number);
        numberAdded = rootView.findViewById(R.id.detail_number_added);
        deviceIdentifier = rootView.findViewById(R.id.detail_di);
        deviceDescription = rootView.findViewById(R.id.detail_description);
        quantity = rootView.findViewById(R.id.detail_quantity);
        dateIn = rootView.findViewById(R.id.detail_in_date);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd", Locale.US);
        dateIn.setText(dateFormat.format(new Date()));
        timeIn = rootView.findViewById(R.id.detail_in_time);
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss", Locale.US);
        timeIn.setText(timeFormat.format(new Date()));
        TextInputLayout expirationTextLayout = rootView.findViewById(R.id.expiration_date_string);
        TextInputLayout dateInLayout = rootView.findViewById(R.id.in_date_layout);
        TextInputLayout timeInLayout = rootView.findViewById(R.id.in_time_layout);
        saveButton = rootView.findViewById(R.id.detail_save_button);
        Button rescanButton = rootView.findViewById(R.id.detail_rescan_button);
        final Button autoPopulateButton = rootView.findViewById(R.id.detail_autopop_button);
        useRadioGroup = rootView.findViewById(R.id.RadioGroup_id);
        singleUseButton = rootView.findViewById(R.id.RadioButton_single);
        singleUseButton.setChecked(true);
        multiUse = rootView.findViewById(R.id.radio_multiuse);
        TextInputLayout numberAddedLayout = rootView.findViewById(R.id.numberAddedLayout);
        MaterialToolbar topToolBar = rootView.findViewById(R.id.topAppBar);
        costEditText = rootView.findViewById(R.id.detail_equipment_cost);
        physicalLocationConstrainLayout = rootView.findViewById(R.id.physicalLocationLinearLayout);
        typeConstrainLayout = rootView.findViewById(R.id.typeLinearLayout);
        chosenType = false;
        chosenSite = false;
        isProcedureInfoReturned = false;
        chosenLocation = false;
        checkAutocompleteTexts = false;
        checkEditTexts = false;
        isUdisReturned = false;
        isAddSizeButtonClicked = true;
        editingExisting = false;
        addSizeButton = rootView.findViewById(R.id.button_addsize);
        specsTextView = rootView.findViewById(R.id.detail_specs_textview);
        allSizeOptions = new ArrayList<>();

        deviceViewModel = new ViewModelProvider(this).get(DeviceViewModel.class);
        // if it is viable, find a way to get the user from inventoryViewModel from main activity
        // instead of waiting again to get the user again
        deviceViewModel.getUserLiveData().observe(getViewLifecycleOwner(), userResource -> {
            deviceViewModel.setupDeviceRepository();
            setupOptionFields();
            setupAutoPopulate();
            setupSaveDevice();
            handleArguments();
        });

        // NumberPicker Dialog for NumberAdded field
        numberAdded.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showNumberPicker(rootView, numberAdded);
            }
        });


        // incrementing number by 1 when clicked on the end icon
        numberAddedLayout.setEndIconOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                incrementNumberAdded();
            }
        });

        autoPopulateButton.setOnClickListener(view -> {
            String barcode = Objects.requireNonNull(udiEditText.getText()).toString().trim();
            if (!barcode.isEmpty()) {
                deviceViewModel.autoPopulatedScannedBarcode(barcode);
                hideKeyboard();
            }
        });

        addSizeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addEmptySizeOption(view);
            }
        });

        //TimePicker dialog pops up when clicked on the icon
        timeInLayout.setEndIconOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                timeInLayoutPicker(view);
            }
        });

        // going back to the scanner view
        rescanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                IntentIntegrator integrator = new IntentIntegrator(getActivity());
                integrator.setCaptureActivity(CaptureActivity.class);
                integrator.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES);
                integrator.setBarcodeImageEnabled(true);
                integrator.initiateScan();
            }
        });

        //going back to inventory view
        topToolBar.setNavigationOnClickListener(new View.OnClickListener() {
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

        // date picker for date in if entered manually
        final DatePickerDialog.OnDateSetListener dateInListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
                myCalendar.set(Calendar.YEAR, i);
                myCalendar.set(Calendar.MONTH, i1);
                myCalendar.set(Calendar.DAY_OF_MONTH, i2);
                String myFormat = "yyyy/MM/dd";
                SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
                dateIn.setText(String.format("%s", sdf.format(myCalendar.getTime())));
            }
        };

        dateInLayout.setEndIconOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new DatePickerDialog(view.getContext(), dateInListener, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        // saves data into database
        saveButton.setOnClickListener(v -> saveData());
        return rootView;
    }

    private void hideKeyboard() {
        try {
            InputMethodManager imm = (InputMethodManager) requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), 0);
        } catch (Exception e) {
            Log.d(TAG,"Keyboard not open");
        }
    }

    private void setupSaveDevice() {
        deviceViewModel.getSaveDeviceRequestLiveData().observe(getViewLifecycleOwner(),event -> {
            Request request = event.getContentIfNotHandled();
            if (request == null) {
                return;
            }
            if (request.getStatus() == org.getcarebase.carebase.utils.Request.Status.SUCCESS) {
                FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
                // if addEquipmentFragment is in the backstack tell the fragment to retry getting the
                // device from the database
                Fragment addEquipmentFragment = fragmentManager.findFragmentByTag(AddEquipmentFragment.TAG);
                View nextView = requireActivity().findViewById(R.id.activity_main);
                if (addEquipmentFragment != null) {
                    ProcedureViewModel procedureViewModel = new ViewModelProvider(requireActivity()).get(ProcedureViewModel.class);
                    procedureViewModel.retryDeviceUsed();
                    nextView = addEquipmentFragment.getView();
                }

                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.remove(this).commit();
                Snackbar.make(nextView, "Device saved to inventory", Snackbar.LENGTH_LONG).show();
            } else {
                Log.d(TAG,"error while saving device");
                Snackbar.make(rootView, R.string.error_something_wrong, Snackbar.LENGTH_LONG).show();
            }
        });
    }

    private void setupAutoPopulate() {
        deviceViewModel.getAutoPopulatedDeviceLiveData().observe(getViewLifecycleOwner(), deviceModelResource -> {
            if (deviceModelResource.getRequest().getStatus() == org.getcarebase.carebase.utils.Request.Status.SUCCESS) {
                DeviceModel deviceModel = deviceModelResource.getData();
                if (deviceModel.getProductions().size() != 0) {
                    DeviceProduction deviceProduction = deviceModel.getProductions().get(0);
                    expiration.setText(deviceProduction.getExpirationDate());
                    expiration.setEnabled(deviceProduction.getExpirationDate() == null);
                    physicalLocation.setText(deviceProduction.getPhysicalLocation());
                    lotNumber.setText(deviceProduction.getLotNumber());
                    lotNumber.setEnabled(deviceProduction.getLotNumber() == null);
                    referenceNumber.setText(deviceProduction.getReferenceNumber());
                    referenceNumber.setEnabled(deviceProduction.getReferenceNumber() == null);
                    notes.setText(deviceProduction.getNotes());
                    notes.setEnabled(deviceProduction.getNotes() == null);
                }

                deviceIdentifier.setText(deviceModel.getDeviceIdentifier());
                deviceIdentifier.setEnabled(deviceModel.getDeviceIdentifier() == null);
                nameEditText.setText(deviceModel.getName());
                nameEditText.setEnabled(deviceModel.getName() == null);
                quantity.setText(Integer.toString(deviceModel.getQuantity()));
                quantity.setEnabled(false);
                equipmentType.setText(deviceModel.getEquipmentType());
                if (deviceModel.getUsage() != null && deviceModel.getUsage().equals("Single Use")) {
                    singleUseButton.setChecked(true);
                }
                else if (deviceModel.getUsage() != null && deviceModel.getUsage().equals("Reusable")){
                    multiUse.setChecked(true);
                }
                deviceDescription.setText(deviceModel.getDescription());
                deviceDescription.setEnabled(deviceModel.getDescription() == null);
                company.setText(deviceModel.getCompany());
                company.setEnabled(deviceModel.getCompany() == null);
                if (numberAdded.getText() == null || numberAdded.getText().toString().trim().isEmpty()) {
                    numberAdded.setText("1");
                }
                for (Map.Entry<String,Object> specification : deviceModel.getSpecificationList()) {
                    addItemSpecs(specification.getKey(),specification.getValue().toString(),rootView);
                }
            } else if (deviceModelResource.getRequest().getStatus() == org.getcarebase.carebase.utils.Request.Status.ERROR) {
                Snackbar.make(rootView, deviceModelResource.getRequest().getResourceString(), Snackbar.LENGTH_LONG).show();
            }
        });
    }

    /**
     * sets up the option fields such as device type, site, and physical location
     */
    private void setupOptionFields() {
        // set up device types
        final ArrayAdapter<String> deviceTypeAdapter = new ArrayAdapter<>(rootView.getContext(), R.layout.dropdown_menu_popup_item,new ArrayList<>());
        deviceViewModel.getDeviceTypesLiveData().observe(getViewLifecycleOwner(), deviceTypesResource -> {
            if (deviceTypesResource.getRequest().getStatus() == org.getcarebase.carebase.utils.Request.Status.SUCCESS) {
                deviceTypeAdapter.clear();
                List<String> types = new ArrayList<>(DEVICE_TYPES);
                types.addAll(deviceTypesResource.getData());
                types = types.stream().distinct().sorted().collect(Collectors.toList());
                // display unique device types
                deviceTypeAdapter.addAll(types);
            } else {
                Snackbar.make(rootView, R.string.error_something_wrong, Snackbar.LENGTH_LONG).show();
            }
        });
        equipmentType.setAdapter(deviceTypeAdapter);

        // set up physical locations
        final ArrayAdapter<String> physicalLocationsAdapter = new ArrayAdapter<>(rootView.getContext(), R.layout.dropdown_menu_popup_item,new ArrayList<>());
        deviceViewModel.getPhysicalLocationsLiveData().observe(getViewLifecycleOwner(), physicalLocationsResource -> {
            if(physicalLocationsResource.getRequest().getStatus() == org.getcarebase.carebase.utils.Request.Status.SUCCESS) {
                physicalLocationsAdapter.clear();
                physicalLocationsAdapter.addAll(PHYSICAL_LOCATIONS);
                physicalLocationsAdapter.addAll(Arrays.asList(physicalLocationsResource.getData()));
            } else {
                Log.d(TAG,"Unable to fetch physical locations");
                Snackbar.make(rootView, R.string.error_something_wrong, Snackbar.LENGTH_LONG).show();
            }
        });
        physicalLocation.setAdapter(physicalLocationsAdapter);
        physicalLocation.setOnItemClickListener((adapterView, view, position, id) -> addNewLoc(adapterView, view, position));

        deviceViewModel.getSavePhysicalLocationRequestLiveData().observe(getViewLifecycleOwner(),request -> {
            if (request.getStatus() == org.getcarebase.carebase.utils.Request.Status.SUCCESS || request.getStatus() == org.getcarebase.carebase.utils.Request.Status.ERROR) {
                Snackbar.make(rootView, request.getResourceString(), Snackbar.LENGTH_LONG).show();
            }
        });
    }

    public void handleArguments() {
        if (getArguments() != null) {
            String barcode;
            String pendingDeviceId;
            if ((barcode = getArguments().getString("barcode")) != null) {
                udiEditText.setText(barcode);
                deviceViewModel.autoPopulatedScannedBarcode(barcode);
            // this device is a pending device
            } else if ((pendingDeviceId = getArguments().getString("pending_device_id")) != null) {
                deviceViewModel.getPendingDeviceLiveData().observe(getViewLifecycleOwner(),pendingDeviceResource -> {
                    if (pendingDeviceResource.getRequest().getStatus() == Request.Status.SUCCESS) {
                        setPendingDataFields(pendingDeviceResource.getData());
                    } else if (pendingDeviceResource.getRequest().getStatus() == Request.Status.ERROR) {
                        FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
                        fragmentManager.popBackStack();
                        Fragment fragment = fragmentManager.findFragmentByTag(PendingUdiFragment.TAG);
                        Snackbar.make(Objects.requireNonNull(fragment).requireView(),pendingDeviceResource.getRequest().getResourceString(),Snackbar.LENGTH_LONG).show();
                    }
                });
                deviceViewModel.setPendingDeviceIdLiveData(pendingDeviceId);
            }

        }
    }

    private void setPendingDataFields(PendingDevice pendingDevice) {
        dateIn.setText(pendingDevice.getDateAdded());
        notes.setText(pendingDevice.getNotes());
        timeIn.setText(pendingDevice.getTimeAdded());
        udiEditText.setText(pendingDevice.getUniqueDeviceIdentifier());
        numberAdded.setText(pendingDevice.getQuantity());
        physicalLocation.setText(pendingDevice.getPhysicalLocation());
    }

    private void timeInLayoutPicker(View view) {
        Calendar mcurrentTime = Calendar.getInstance();
        int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
        int minute = mcurrentTime.get(Calendar.MINUTE);
        TimePickerDialog mTimePicker;
        mTimePicker = new TimePickerDialog(view.getContext(), new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                timeIn.setText(String.format(Locale.US, "%02d:%02d:00", selectedHour, selectedMinute));
            }
        }, hour, minute, true);
        mTimePicker.setTitle("Select Time");
        mTimePicker.show();
    }

    // not in mvvm style - need to use data bindings
    // packages all the fields into a DeviceModel object
    private DeviceModel isFieldsValid() {
        boolean isValid = true;

        List<EditText> requiredEditTexts = new ArrayList<>(allSizeOptions);
        requiredEditTexts.addAll(Arrays.asList(udiEditText, deviceIdentifier, nameEditText, expiration, physicalLocation, equipmentType, lotNumber, company, numberAdded, dateIn, timeIn));
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
            deviceModel.setDescription(Objects.requireNonNull(deviceDescription.getText()).toString().trim());
            deviceModel.setEquipmentType(equipmentType.getText().toString().trim());
            int radioButtonInt = useRadioGroup.getCheckedRadioButtonId();
            final RadioButton radioButton = rootView.findViewById(radioButtonInt);
            final String usage = radioButton.getText().toString();
            deviceModel.setUsage(usage);
            int currentQuantity = Integer.parseInt(Objects.requireNonNull(quantity.getText()).toString());
            int amount = Integer.parseInt(Objects.requireNonNull(numberAdded.getText()).toString());
            deviceModel.setQuantity(currentQuantity + amount);

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
            deviceProduction.setDateAdded(Objects.requireNonNull(dateIn.getText()).toString().trim());
            deviceProduction.setTimeAdded(Objects.requireNonNull(timeIn.getText()).toString().trim());
            deviceProduction.setExpirationDate(Objects.requireNonNull(expiration.getText()).toString().trim());
            deviceProduction.setLotNumber(Objects.requireNonNull(lotNumber.getText()).toString().trim());
            deviceProduction.setNotes(Objects.requireNonNull(notes.getText()).toString().trim());
            deviceProduction.setPhysicalLocation(physicalLocation.getText().toString().trim());
            deviceProduction.setQuantity(amount);
            deviceModel.addDeviceProduction(deviceProduction);

            if (deviceViewModel.getAutoPopulatedDeviceLiveData().getValue() != null
                    && deviceViewModel.getAutoPopulatedDeviceLiveData().getValue().getData() != null
                    && deviceViewModel.getAutoPopulatedDeviceLiveData().getValue().getData().getShipment() != null) {
                deviceModel.setShipment(deviceViewModel.getAutoPopulatedDeviceLiveData().getValue().getData().getShipment());
            }

            if(!Objects.requireNonNull(costEditText.getText()).toString().trim().isEmpty()){
                String cleanString = costEditText.getText().toString().replaceAll("[$,.]", "");
                double packagePrice = Double.parseDouble(cleanString) / 100;
                Cost cost = new Cost(Objects.requireNonNull(dateIn.getText()).toString(),amount,packagePrice);
                deviceProduction.addCost(cost);
            }

            return deviceModel;
        }
        return null;
    }

    private void incrementNumberAdded() {
        int newNumber = 0;
        try {
            newNumber = Integer.parseInt(Objects.requireNonNull(numberAdded.getText()).toString());
        } catch (NumberFormatException e) {
            Log.d(TAG, e.toString());
        } finally {
            numberAdded.setText(String.valueOf(++newNumber));
        }
    }


    private void showNumberPicker(View view, final TextInputEditText editTextAdded) {
        final Dialog d = new Dialog(view.getContext());
        d.setTitle("NumberPicker");
        d.setContentView(R.layout.dialog);
        Button b1 = d.findViewById(R.id.button1);
        Button b2 = d.findViewById(R.id.button2);
        final NumberPicker np = d.findViewById(R.id.numberPicker1);
        np.setMaxValue(1000); // max value 100
        np.setMinValue(0);   // min value 0

        np.setWrapSelectorWheel(true);
        b1.setOnClickListener(v -> {
            //set the value to textview
            editTextAdded.setText(String.valueOf(np.getValue()));
            d.dismiss();
        });
        b2.setOnClickListener(v -> d.dismiss());
        d.show();

    }

    // adds new row of size text views if users clicks on a button
    int rowIndex = 1;
    int rowLoc = 1;

    private void addEmptySizeOption(View view) {
        Log.d(TAG, "Adding empty size option!");
        emptySizeFieldCounter++;
        LinearLayout layoutSize = new LinearLayout(getContext());
        layoutSize.setOrientation(LinearLayout.HORIZONTAL);
        layoutSize.setLayoutParams(new LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT));

        TextInputLayout sizeKeyLayout = new TextInputLayout(view.getContext());
        sizeKeyLayout.setHint("Key");
        LinearLayout.LayoutParams klp = new LinearLayout.LayoutParams(0, MATCH_PARENT, 1f);
        klp.setMargins((int) (4 * dp), (int) (4 * dp), (int) (4 * dp), (int) (4 * dp));
        sizeKeyLayout.setLayoutParams(klp);
        TextInputEditText sizeKey = new TextInputEditText(sizeKeyLayout.getContext());
        sizeKey.setSingleLine();
        sizeKey.setEllipsize(TextUtils.TruncateAt.END);

        TextInputLayout sizeValueLayout = new TextInputLayout(view.getContext());
        sizeValueLayout.setHint("Value");
        LinearLayout.LayoutParams vlp = new LinearLayout.LayoutParams(0, MATCH_PARENT, 1f);
        vlp.setMargins((int) (4 * dp), (int) (4 * dp), (int) (4 * dp), (int) (4 * dp));
        sizeValueLayout.setLayoutParams(vlp);
        TextInputEditText sizeValue = new TextInputEditText(sizeKeyLayout.getContext());
        sizeValue.setSingleLine();
        sizeValue.setEllipsize(TextUtils.TruncateAt.END);

        sizeKeyLayout.addView(sizeKey);
        sizeValueLayout.addView(sizeValue);
        layoutSize.addView(sizeKeyLayout);
        layoutSize.addView(sizeValueLayout);

        allSizeOptions.add(sizeKey);
        allSizeOptions.add(sizeValue);
        linearLayout.addView(layoutSize, (rowLoc++) + linearLayout.indexOfChild(specsTextView));
        rowIndex++;

        if (isAddSizeButtonClicked) {
            removeSizeButton = new MaterialButton(view.getContext(),
                    null, R.attr.materialButtonOutlinedStyle);
            removeSizeButton.setText(R.string.removeSize_label);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT);
            lp.setMargins((int) (4 * dp), 0, (int) (4 * dp), 0);
            removeSizeButton.setLayoutParams(lp);
            linearLayout.addView(removeSizeButton, linearLayout.indexOfChild(addSizeButton));
        }

        removeSizeButton.setOnClickListener(buttonView -> removeEmptySizeOption());
        isAddSizeButtonClicked = false;
    }

    //removes one row of size text entry
    private void removeEmptySizeOption() {
        if (emptySizeFieldCounter > 0) {
            linearLayout.removeViewAt(linearLayout.indexOfChild(specsTextView) + --rowLoc);
            emptySizeFieldCounter--;

        }
        if (emptySizeFieldCounter == 0) {
            linearLayout.removeViewAt(linearLayout.indexOfChild(removeSizeButton));
            isAddSizeButtonClicked = true;
        }
        allSizeOptions.remove(allSizeOptions.size() - 1);
        allSizeOptions.remove(allSizeOptions.size() - 1);
    }

    private void addItemSpecs(String key, String value, View view) {
        Log.d(TAG, "Adding item specs!");

        LinearLayout layoutSize = new LinearLayout(view.getContext());
        layoutSize.setOrientation(LinearLayout.HORIZONTAL);
        layoutSize.setLayoutParams(new LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT));

        TextInputLayout sizeKeyLayout = new TextInputLayout(view.getContext());
        sizeKeyLayout.setHint("Key");
        LinearLayout.LayoutParams klp = new LinearLayout.LayoutParams(0, MATCH_PARENT, 1f);
        klp.setMargins((int) (4 * dp), (int) (4 * dp), (int) (4 * dp), (int) (4 * dp));
        sizeKeyLayout.setLayoutParams(klp);
        TextInputEditText sizeKey = new TextInputEditText(sizeKeyLayout.getContext());
        sizeKey.setSingleLine();
        sizeKey.setEllipsize(TextUtils.TruncateAt.END);
        sizeKey.setEnabled(false);
        sizeKey.setText(key);

        TextInputLayout sizeValueLayout = new TextInputLayout(view.getContext());
        sizeValueLayout.setHint("Value");
        LinearLayout.LayoutParams vlp = new LinearLayout.LayoutParams(0, MATCH_PARENT, 1f);
        vlp.setMargins((int) (4 * dp), (int) (4 * dp), (int) (4 * dp), (int) (4 * dp));
        sizeValueLayout.setLayoutParams(vlp);
        TextInputEditText sizeValue = new TextInputEditText(sizeKeyLayout.getContext());
        sizeValue.setSingleLine();
        sizeValue.setEllipsize(TextUtils.TruncateAt.END);
        sizeValue.setEnabled(false);
        sizeValue.setText(value);

        sizeKeyLayout.addView(sizeKey);
        sizeValueLayout.addView(sizeValue);

        layoutSize.addView(sizeKeyLayout);
        layoutSize.addView(sizeValueLayout);

        allSizeOptions.add(sizeKey);
        allSizeOptions.add(sizeValue);
        linearLayout.addView(layoutSize, (rowLoc++) + linearLayout.indexOfChild(specsTextView));
        rowIndex++;
    }

    private void addNewLoc(final AdapterView<?> adapterView, View view, int i) {
        String selected = (String) adapterView.getItemAtPosition(i);
        final TextInputLayout otherPhysicalLocationLayout;
        if (selected.equals("Other")) {
            saveButton.setEnabled(false);
            chosenLocation = true;
            otherPhysicalLocationLayout = (TextInputLayout) View.inflate(view.getContext(), R.layout.activity_itemdetail_materialcomponent, null);
            otherPhysicalLocationLayout.setHint("Enter physical location");
            otherPhysicalLocationLayout.setGravity(Gravity.END);
            otherPhysicalLocationLayout.setId(View.generateViewId());
            EditText otherPhysicalLocationEditText = new TextInputEditText(otherPhysicalLocationLayout.getContext());
            otherPhysicalLocationEditText.setLayoutParams(new LinearLayout.LayoutParams(udiEditText.getWidth(), WRAP_CONTENT));
            otherPhysicalLocationLayout.addView(otherPhysicalLocationEditText);
            linearLayout.addView(otherPhysicalLocationLayout, 1 + linearLayout.indexOfChild(physicalLocationConstrainLayout));

            MaterialButton submitPhysicalLocationButton = new MaterialButton(view.getContext(), null, R.attr.materialButtonOutlinedStyle);
            submitPhysicalLocationButton.setText(R.string.submitLocation_lbl);
            submitPhysicalLocationButton.setLayoutParams(new LinearLayout.LayoutParams(udiEditText.getWidth(),
                    WRAP_CONTENT));
            otherPhysicalLocationLayout.addView(submitPhysicalLocationButton);
            TextWatcher physicalLocationWatcher = new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

                @Override
                public void afterTextChanged(Editable editable) {
                    if (!(editable.toString().trim().isEmpty())) {
                       submitPhysicalLocationButton.setEnabled(true);
                    }
                }
            };
            otherPhysicalLocationEditText.addTextChangedListener(physicalLocationWatcher);

            submitPhysicalLocationButton.setOnClickListener(submitPhysicalLocationView -> {
                hideKeyboard();
                deviceViewModel.savePhysicalLocation(Objects.requireNonNull(otherPhysicalLocationEditText.getText()).toString().trim());
                saveButton.setEnabled(true);
            });

        } else if (chosenLocation) {
            saveButton.setEnabled(true);
            chosenLocation = false;
            linearLayout.removeViewAt(1 + linearLayout.indexOfChild(physicalLocationConstrainLayout));
        }
    }

    private void saveData() {
        DeviceModel deviceModel = isFieldsValid();
        if (deviceModel != null) {
            deviceViewModel.saveDevice(deviceModel);
        } else {
            Snackbar.make(rootView, "Please fill all required fields", Snackbar.LENGTH_LONG).show();
        }
    }
}