package org.getcarebase.carebase.activities.Main.fragments;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.SetOptions;
import com.google.zxing.integration.android.IntentIntegrator;
import com.journeyapps.barcodescanner.CaptureActivity;

import org.getcarebase.carebase.R;
import org.getcarebase.carebase.activities.Main.InventoryTemplate;
import org.getcarebase.carebase.models.DeviceModel;
import org.getcarebase.carebase.models.DeviceProduction;
import org.getcarebase.carebase.models.User;
import org.getcarebase.carebase.utils.Resource;
import org.getcarebase.carebase.viewmodels.DeviceViewModel;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

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

    private DocumentReference typeRef;
    private CollectionReference siteRef;
    private DocumentReference physLocRef;


    InventoryTemplate udiDocument;

    private static final String TAG = ItemDetailFragment.class.getSimpleName();
    private Activity parent;
    private Calendar myCalendar;

    // USER INPUT VALUES
    private TextInputEditText udiEditText;
    private TextInputEditText nameEditText;
    private AutoCompleteTextView equipmentType;
    private TextInputEditText company;
    private TextInputEditText otherType_text;
    private TextInputEditText otherPhysicalLoc_text;
    private TextInputEditText otherSite_text;
    private TextInputEditText deviceIdentifier;
    private TextInputEditText deviceDescription;
    private TextInputEditText expiration;
    private TextInputEditText quantity;
    private TextInputEditText lotNumber;
    private TextInputEditText referenceNumber;
    private AutoCompleteTextView hospitalName;
    private AutoCompleteTextView physicalLocation;
    private TextInputEditText notes;
    private TextInputEditText dateIn;
    private TextInputEditText timeIn;
    private TextInputEditText numberAdded;
    private TextInputEditText medicalSpeciality;
    private TextView specsTextView;
    private LinearLayout linearLayout;
    private TextInputEditText costEditText;

    private Button saveButton;
    private MaterialButton removeSizeButton;
    private RadioGroup useRadioGroup;
    private RadioButton singleUseButton;
    private RadioButton multiUse;
    private Button addSizeButton;

    String di = "";
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
            "Catheter",
            "Catheter Extraction Tool",
            "Catheter Securement Device",
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
            "Venous Access (Catheters, Central Lines, Introducers, Tunnelers)",
            "Other");
    private final List<String> SITES = Arrays.asList("Other");
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

    HashMap<String, String> procedureInfo;
    private List<HashMap<String, Object>> procedureUdisList;

    private LinearLayout siteConstrainLayout;
    private LinearLayout physicalLocationConstrainLayout;
    private LinearLayout typeConstrainLayout;


    // firebase key labels to avoid hard-coded paths
    private final String NAME_KEY = "name";
    private final String TYPE_KEY = "equipment_type";
    private final String COMPANY_KEY = "company";
    private final String SITE_KEY = "site_name";
    private final String SPECIALTY_KEY = "medical_specialty";
    private final String DESCRIPTION_KEY = "device_description";
    private final String USAGE_KEY = "usage";
    private final String PHYSICALLOC_KEY = "physical_location";
    private final String QUANTITY_KEY = "quantity";

    private float dp;
    private View rightView;

    private DeviceViewModel deviceViewModel;
    private View rootView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        dp = Objects.requireNonNull(getContext()).getResources().getDisplayMetrics().density;
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
        hospitalName = rootView.findViewById(R.id.detail_site_location);
        physicalLocation = rootView.findViewById(R.id.detail_physical_location);
        notes = rootView.findViewById(R.id.detail_notes);
        lotNumber = rootView.findViewById(R.id.detail_lot_number);
        referenceNumber = rootView.findViewById(R.id.detail_reference_number);
        numberAdded = rootView.findViewById(R.id.detail_number_added);
        medicalSpeciality = rootView.findViewById(R.id.detail_medical_speciality);
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
        siteConstrainLayout = rootView.findViewById(R.id.site_linearlayout);
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
        });



        // Get user information in "users" collection
//        final DocumentReference currentUserRef = usersRef.document(userId);
//        currentUserRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
//            @Override
//            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
//                String toastMessage;
//                if (task.isSuccessful()) {
//                    DocumentSnapshot document = task.getResult();
//                    if (Objects.requireNonNull(document).exists()) {
//                        try {
//
//                            mNetworkId = Objects.requireNonNull(document.get("network_id")).toString();
//                            mHospitalId = Objects.requireNonNull(document.get("hospital_id")).toString();
//                            mUser = Objects.requireNonNull(document.get("email")).toString();
//                            mHospitalName = Objects.requireNonNull(document.get("hospital_name")).toString();
//
//
//                            typeRef = db.collection("networks").document(mNetworkId).collection("hospitals")
//                                    .document(mHospitalId).collection("types").document("type_options");
//                            siteRef = db.collection("networks").document(mNetworkId)
//                                    .collection("hospitals");
//                            physLocRef = db.collection("networks").document(mNetworkId)
//                                    .collection("hospitals").document(mHospitalId)
//                                    .collection("physical_locations").document("locations");
//
//                            //get pending equipment info
//                            if (getArguments() != null) {
//                                String barcode = getArguments().getString("barcode");
//                                boolean isPending = getArguments().getBoolean("pending_udi");
//                                editingExisting = getArguments().getBoolean("editingExisting");
//                                if (isPending) {
//                                    getPendingSpecs(barcode);
//                                }
//                                udiEditText.setText(barcode);
//                                if (editingExisting) {
//                                    udiEditText.setEnabled(false);
//                                    autoPopulateButton.setEnabled(false);
//                                    autopopulateNonGudid(barcode, getArguments().getString("di"), rightView);
//                                }
//                            }
//                        } catch (NullPointerException e) {
//                            toastMessage = "Error retrieving user information; Please contact support";
//                            Toast.makeText(parent.getApplicationContext(), toastMessage, Toast.LENGTH_SHORT).show();
//                        }
//                    } else {
//                        // document for user doesn't exist
//                        toastMessage = "User not found; Please contact support";
//                        Toast.makeText(parent.getApplicationContext(), toastMessage, Toast.LENGTH_SHORT).show();
//                    }
//                } else {
//                    toastMessage = "User lookup failed; Please try again and contact support if issue persists";
//                    Toast.makeText(parent.getApplicationContext(), toastMessage, Toast.LENGTH_SHORT).show();
//                    Log.d(TAG, "get failed with ", task.getException());
//                }
//            }
//        });


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
            // hide keyboard
            try {
                InputMethodManager imm = (InputMethodManager) Objects.requireNonNull(getActivity()).getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), 0);
            } catch (Exception e) {
                Log.d(TAG,"onAutoPopulateClick: keyboard not open");
            }
            String barcode = Objects.requireNonNull(udiEditText.getText()).toString();
            deviceViewModel.autoPopulatedScannedBarcode(barcode);
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
//                parent.onBackPressed();
            }
        });
        //going back to inventory view
        topToolBar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentManager fragmentManager = getFragmentManager();
                if (Objects.requireNonNull(fragmentManager).getBackStackEntryCount() > 0) {
                    fragmentManager.popBackStack();
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

        if (getArguments() != null) {
            String barcode = getArguments().getString("barcode");
            procedureInfo = (HashMap<String, String>) getArguments().getSerializable("procedure_info");
            procedureUdisList = (List<HashMap<String, Object>>) getArguments().getSerializable("udi_quantity");
            if (procedureInfo != null && procedureInfo.size() != 0) {
                isProcedureInfoReturned = true;
            }
            if (procedureUdisList != null && procedureUdisList.size() > 0) {
                isUdisReturned = true;
            }
            udiEditText.setText(barcode);
            autoPopulate(rootView);

        }
        return rootView;
    }

    private void setupSaveDevice() {
        deviceViewModel.getSaveDeviceRequestLiveData().observe(getViewLifecycleOwner(),request -> {
            if (request.getStatus() == org.getcarebase.carebase.utils.Request.Status.SUCCESS) {
                Objects.requireNonNull(getActivity()).getSupportFragmentManager().beginTransaction().remove(this).commit();
                Snackbar.make(getActivity().findViewById(R.id.activity_main), "Equipment Saved", Snackbar.LENGTH_LONG).show();
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
                DeviceProduction deviceProduction = deviceModel.getProductions().get(0);
                deviceIdentifier.setText(deviceModel.getDeviceIdentifier());
                deviceIdentifier.setEnabled(deviceModel.getDeviceIdentifier() == null);
                nameEditText.setText(deviceModel.getName());
                nameEditText.setEnabled(deviceModel.getName() == null);
                quantity.setText(Integer.toString(deviceModel.getQuantity()));
                quantity.setEnabled(false);
                expiration.setText(deviceProduction.getExpirationDate());
                expiration.setEnabled(deviceProduction.getExpirationDate() == null);
                hospitalName.setText(deviceModel.getSiteName());
                physicalLocation.setText(deviceProduction.getPhysicalLocation());
                equipmentType.setText(deviceModel.getEquipmentType());
                if (deviceModel.getUsage() != null && deviceModel.getUsage().equals("Single Use")) {
                    singleUseButton.setChecked(true);
                }
                else if (deviceModel.getUsage() != null && deviceModel.getUsage().equals("Reusable")){
                    multiUse.setChecked(true);
                }
                medicalSpeciality.setText(deviceModel.getMedicalSpecialty());
                deviceDescription.setText(deviceModel.getDescription());
                deviceDescription.setEnabled(deviceModel.getDescription() == null);
                lotNumber.setText(deviceProduction.getLotNumber());
                lotNumber.setEnabled(deviceProduction.getLotNumber() == null);
                referenceNumber.setText(deviceProduction.getReferenceNumber());
                referenceNumber.setEnabled(deviceProduction.getReferenceNumber() == null);
                company.setText(deviceModel.getCompany());
                company.setEnabled(deviceModel.getCompany() == null);
                notes.setText(deviceProduction.getNotes());
                notes.setEnabled(deviceProduction.getNotes() == null);
                numberAdded.setText(Integer.toString(deviceProduction.getQuantity()));
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
            if(deviceTypesResource.getRequest().getStatus() == org.getcarebase.carebase.utils.Request.Status.SUCCESS) {
                deviceTypeAdapter.clear();
                deviceTypeAdapter.addAll(DEVICE_TYPES);
                deviceTypeAdapter.addAll(Arrays.asList(deviceTypesResource.getData()));
            } else {
                Log.d(TAG,"Unable to fetch device types");
                Snackbar.make(rootView, R.string.error_something_wrong, Snackbar.LENGTH_LONG).show();
            }
        });
        equipmentType.setAdapter(deviceTypeAdapter);
        equipmentType.setOnItemClickListener((adapterView, view, position, row) -> addTypeOptionField(adapterView, view, position));

        // set up sites
        final ArrayAdapter<String> sitesAdapter = new ArrayAdapter<>(rootView.getContext(), R.layout.dropdown_menu_popup_item, new ArrayList<>());
        deviceViewModel.getSitesLiveData().observe(getViewLifecycleOwner(), sitesResource -> {
            if(sitesResource.getRequest().getStatus() == org.getcarebase.carebase.utils.Request.Status.SUCCESS) {
                sitesAdapter.clear();
                sitesAdapter.addAll(Arrays.asList(sitesResource.getData()));
                sitesAdapter.addAll(SITES);
            } else {
                Log.d(TAG,"Unable to fetch sites");
                Snackbar.make(rootView, R.string.error_something_wrong, Snackbar.LENGTH_LONG).show();
            }
        });
        hospitalName.setAdapter(sitesAdapter);
        hospitalName.setOnItemClickListener((adapterView, view, position, id) -> addNewSite(adapterView, view, position));

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
    }

    private void getPendingSpecs(final String barcode) {
        DocumentReference docRef = db.collection("networks").document(mNetworkId)
                .collection("hospitals").document(mHospitalId).collection("departments")
                .document("default_department").collection("pending_udis").document(barcode);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (Objects.requireNonNull(document).exists()) {
                        List<Map> list = new ArrayList<>();
                        Map<String, Object> map = document.getData();
                        if (map != null) {
                            list.add(map);
                        }
                        autopopulatePendingData(list);
                    }
                }
            }
        });
    }

    private void deletePendingUdi(String barcode) {
        CollectionReference CollectionRef = db.collection("networks").document(mNetworkId)
                .collection("hospitals").document(mHospitalId).collection("departments")
                .document("default_department").collection("pending_udis");

        CollectionRef.document(barcode)
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "DocumentSnapshot successfully deleted!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error deleting document", e);
                    }
                });
    }

    private void autopopulatePendingData(List<Map> list) {
        hospitalName.setText(Objects.requireNonNull(list.get(0).get("site_name")).toString());
        dateIn.setText(Objects.requireNonNull(list.get(0).get("date_in")).toString());
        notes.setText(Objects.requireNonNull(list.get(0).get("notes")).toString());
        timeIn.setText(Objects.requireNonNull(list.get(0).get("time_in")).toString());
        udiEditText.setText(Objects.requireNonNull(list.get(0).get("udi")).toString());
        numberAdded.setText(Objects.requireNonNull(list.get(0).get("number_added")).toString());
        physicalLocation.setText(Objects.requireNonNull(list.get(0).get("physical_location")).toString());
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
    private DeviceModel isFieldsValid() {
        boolean isValid = true;
        for (EditText editText : new EditText[]{udiEditText,deviceIdentifier,nameEditText,expiration,
                hospitalName,physicalLocation,equipmentType,lotNumber,company,numberAdded,dateIn,timeIn}) {
            if (editText.getText().toString().trim().isEmpty()) {
                isValid = false;
            }
        }
        if (isValid) {
            DeviceModel deviceModel = new DeviceModel();
            deviceModel.setDeviceIdentifier(deviceIdentifier.getText().toString().trim());
            deviceModel.setName(nameEditText.getText().toString().trim());
            deviceModel.setCompany(company.getText().toString().trim());
            deviceModel.setDescription(deviceDescription.getText().toString().trim());
            deviceModel.setEquipmentType(equipmentType.getText().toString().trim());
            deviceModel.setMedicalSpecialty(medicalSpeciality.getText().toString().trim());
            deviceModel.setSiteName(hospitalName.getText().toString().trim());
            int radioButtonInt = useRadioGroup.getCheckedRadioButtonId();
            final RadioButton radioButton = rootView.findViewById(radioButtonInt);
            final String usage = radioButton.getText().toString();
            deviceModel.setUsage(usage);
            int currentQuantity = Integer.parseInt(quantity.getText().toString());
            int amount = Integer.parseInt(numberAdded.getText().toString());
            deviceModel.setQuantity(currentQuantity + amount);

            DeviceProduction deviceProduction = new DeviceProduction();
            deviceProduction.setUniqueDeviceIdentifier(udiEditText.getText().toString().trim());
            deviceProduction.setDateAdded(dateIn.getText().toString().trim());
            deviceProduction.setTimeAdded(timeIn.getText().toString().trim());
            deviceProduction.setExpirationDate(expiration.getText().toString().trim());
            deviceProduction.setLotNumber(lotNumber.getText().toString().trim());
            deviceProduction.setNotes(notes.getText().toString().trim());
            deviceProduction.setPhysicalLocation(physicalLocation.getText().toString().trim());
            deviceProduction.setQuantity(amount);
            deviceModel.addDeviceProduction(deviceProduction);

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
        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editTextAdded.setText(String.valueOf(np.getValue())); //set the value to textview
                d.dismiss();
            }
        });
        b2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                d.dismiss(); // dismiss the dialog
            }
        });
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

        removeSizeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                removeEmptySizeOption();
            }
        });
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


    // adds new text field if users choose "other" for type
    private void addTypeOptionField(final AdapterView<?> adapterView, View view, int i) {
        String selected = (String) adapterView.getItemAtPosition(i);
        TextInputLayout other_type_layout;
        if (selected.equals("Other")) {
            saveButton.setEnabled(false);
            chosenType = true;
            other_type_layout = (TextInputLayout) View.inflate(view.getContext(),
                    R.layout.activity_itemdetail_materialcomponent, null);
            other_type_layout.setHint("Enter type");
            other_type_layout.setGravity(Gravity.END);
            other_type_layout.setId(View.generateViewId());
            otherType_text = new TextInputEditText(other_type_layout.getContext());
            TextWatcher typeTextWatcher = new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void afterTextChanged(Editable editable) {
                    if (!(otherType_text.toString().trim().isEmpty())) {
                        saveButton.setEnabled(true);
                    }
                }
            };
            otherType_text.addTextChangedListener(typeTextWatcher);

            otherType_text.setLayoutParams(new LinearLayout.LayoutParams(udiEditText.getWidth(), WRAP_CONTENT));
            other_type_layout.addView(otherType_text);
            linearLayout.addView(other_type_layout, 1 + linearLayout.indexOfChild(typeConstrainLayout));

            MaterialButton submit_otherType = new MaterialButton(view.getContext(),
                    null, R.attr.materialButtonOutlinedStyle);
            submit_otherType.setText(R.string.otherType_lbl);
            submit_otherType.setLayoutParams(new LinearLayout.LayoutParams(udiEditText.getWidth(),
                    WRAP_CONTENT));
            other_type_layout.addView(submit_otherType);

            submit_otherType.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Toast.makeText(view.getContext(), Objects.requireNonNull(otherType_text.getText()).toString(), Toast.LENGTH_SHORT).show();
                    Map<String, Object> newType = new HashMap<>();
                    newType.put("type_" + (++typeCounter), otherType_text.getText().toString());
                    if (typeCounter == 1) {
                        typeRef.set(newType)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Toast.makeText(adapterView.getContext(), "Your input has been saved", Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(adapterView.getContext(), "Error while saving your input", Toast.LENGTH_SHORT).show();
                                        Log.d(TAG, e.toString());
                                    }
                                });
                    } else {
                        typeRef.update(newType)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Toast.makeText(adapterView.getContext(), "Your input has been saved", Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(adapterView.getContext(), "Error while saving your input", Toast.LENGTH_SHORT).show();
                                        Log.d(TAG, e.toString());
                                    }
                                });
                    }

                }
            });
        } else if (chosenType) {
            chosenType = false;
            saveButton.setEnabled(true);
            linearLayout.removeViewAt(1 + linearLayout.indexOfChild(typeConstrainLayout));
        }
    }

    private void addNewSite(final AdapterView<?> adapterView, View view, int i) {
        String selected = (String) adapterView.getItemAtPosition(i);
        TextInputLayout other_site_layout;
        if (selected.equals("Other")) {
            saveButton.setEnabled(false);
            chosenSite = true;
            other_site_layout = (TextInputLayout) View.inflate(view.getContext(),
                    R.layout.activity_itemdetail_materialcomponent, null);
            other_site_layout.setHint("Enter site");
            other_site_layout.setId(View.generateViewId());
            other_site_layout.setGravity(Gravity.END);
            otherSite_text = new TextInputEditText(other_site_layout.getContext());
            TextWatcher siteTextWatcher = new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                }

                @Override
                public void afterTextChanged(Editable editable) {
                    if (!(otherSite_text.toString().trim().isEmpty())) {
                        saveButton.setEnabled(true);
                    }
                }
            };
            otherSite_text.addTextChangedListener(siteTextWatcher);
            otherSite_text.setLayoutParams(new LinearLayout.LayoutParams(udiEditText.getWidth(), WRAP_CONTENT));
            other_site_layout.addView(otherSite_text);
            linearLayout.addView(other_site_layout, 1 + linearLayout.indexOfChild(siteConstrainLayout));

            MaterialButton submitOtherSite = new MaterialButton(view.getContext(),
                    null, R.attr.materialButtonOutlinedStyle);
            submitOtherSite.setText(R.string.submitSite_lbl);
            submitOtherSite.setLayoutParams(new LinearLayout.LayoutParams(udiEditText.getWidth(),
                    WRAP_CONTENT));
            other_site_layout.addView(submitOtherSite);
            submitOtherSite.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Toast.makeText(view.getContext(), Objects.requireNonNull(otherSite_text.getText()).toString(), Toast.LENGTH_SHORT).show();
                    Map<String, Object> newType = new HashMap<>();
                    newType.put("site_" + (++siteCounter), otherSite_text.getText().toString());
                    if (siteCounter == 1) {
                        siteRef.document("site_options").set(newType)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Toast.makeText(adapterView.getContext(), "Your input has been saved", Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(adapterView.getContext(), "Error while saving your input", Toast.LENGTH_SHORT).show();
                                        Log.d(TAG, e.toString());
                                    }
                                });
                    } else {
                        siteRef.document("site_options").update(newType)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Toast.makeText(adapterView.getContext(), "Your input has been saved", Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(adapterView.getContext(), "Error while saving your input", Toast.LENGTH_SHORT).show();
                                        Log.d(TAG, e.toString());
                                    }
                                });
                    }
                }
            });
        } else if (chosenSite) {
            saveButton.setEnabled(true);
            chosenSite = false;
            linearLayout.removeViewAt(1 + linearLayout.indexOfChild(siteConstrainLayout));
        }
    }

    private void addNewLoc(final AdapterView<?> adapterView, View view, int i) {
        String selectedLoc = (String) adapterView.getItemAtPosition(i);
        final TextInputLayout other_physicaloc_layout;
        if (selectedLoc.equals("Other")) {
            saveButton.setEnabled(false);
            chosenLocation = true;
            other_physicaloc_layout = (TextInputLayout) View.inflate(view.getContext(),
                    R.layout.activity_itemdetail_materialcomponent, null);
            other_physicaloc_layout.setHint("Enter physical location");
            other_physicaloc_layout.setGravity(Gravity.END);
            other_physicaloc_layout.setId(View.generateViewId());
            otherPhysicalLoc_text = new TextInputEditText(other_physicaloc_layout.getContext());
            TextWatcher physicalLocationWatcher = new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                }

                @Override
                public void afterTextChanged(Editable editable) {
                    if (!(otherPhysicalLoc_text.toString().trim().isEmpty())) {
                        saveButton.setEnabled(true);
                    }
                }
            };
            otherPhysicalLoc_text.addTextChangedListener(physicalLocationWatcher);
            otherPhysicalLoc_text.setLayoutParams(new LinearLayout.LayoutParams(udiEditText.getWidth(), WRAP_CONTENT));
            other_physicaloc_layout.addView(otherPhysicalLoc_text);
            linearLayout.addView(other_physicaloc_layout, 1 + linearLayout.indexOfChild(physicalLocationConstrainLayout));

            MaterialButton submit_otherPhysicalLoc = new MaterialButton(view.getContext(),
                    null, R.attr.materialButtonOutlinedStyle);
            submit_otherPhysicalLoc.setText(R.string.submitLocation_lbl);
            submit_otherPhysicalLoc.setLayoutParams(new LinearLayout.LayoutParams(udiEditText.getWidth(),
                    WRAP_CONTENT));
            other_physicaloc_layout.addView(submit_otherPhysicalLoc);

            submit_otherPhysicalLoc.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Toast.makeText(view.getContext(), Objects.requireNonNull(otherPhysicalLoc_text.getText()).toString(), Toast.LENGTH_SHORT).show();
                    Map<String, Object> newType = new HashMap<>();
                    newType.put("loc_" + (++locCounter), otherPhysicalLoc_text.getText().toString());
                    if (locCounter == 1) {
                        physLocRef.set(newType)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Toast.makeText(adapterView.getContext(), "Your input has been saved", Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(adapterView.getContext(), "Error while saving your input", Toast.LENGTH_SHORT).show();
                                        Log.d(TAG, e.toString());
                                    }
                                });
                    } else {
                        physLocRef.update(newType)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Toast.makeText(adapterView.getContext(), "Your input has been saved", Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(adapterView.getContext(), "Error while saving your input", Toast.LENGTH_SHORT).show();
                                        Log.d(TAG, e.toString());
                                    }
                                });
                    }
                }
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


    private void saveEquipmentCost(DocumentReference udiRef, TextInputEditText costEditText, TextInputEditText numberAdded, TextInputEditText dateIn){
        if(Objects.requireNonNull(costEditText.getText()).toString().length() > 0){
            Map<String, Object> costInfo = new HashMap<>();
            String cleanString = costEditText.getText().toString().replaceAll("[$,.]", "");
            double parsed = Double.parseDouble(cleanString) / 100;
            double pricePerUnit = parsed / Integer.parseInt(Objects.requireNonNull(numberAdded.getText()).toString());
            double roundOff = Math.round(pricePerUnit * 100.0) / 100.0;

            costInfo.put("cost_date", Objects.requireNonNull(dateIn.getText()).toString());
            costInfo.put("number_added",numberAdded.getText().toString());
            costInfo.put("package_price",parsed);
            costInfo.put("unit_price",roundOff);
            costInfo.put("user",mUser);

            udiRef.collection("equipment_cost")
                    .add(costInfo)
                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                @Override
                public void onSuccess(DocumentReference documentReference) {
                    Log.d(TAG, "DocumentSnapshot written with ID: " + documentReference.getId());
                }
            })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.w(TAG, "Error adding document", e);
                        }
                    });

        }
    }

    private void autoPopulate(final View view) {
        rightView = view;
        String udiStr = Objects.requireNonNull(udiEditText.getText()).toString();
        String url = "https://accessgudid.nlm.nih.gov/api/v2/devices/lookup.json?udi=";

        if (udiStr.equals("")) {
            return;
            // Some UDI starts with '+'; need to strip + and last character and send as a di
        } else if (udiStr.charAt(0) == '+') {
            udiStr = udiStr.substring(1, udiStr.length() - 1);
            url = "https://accessgudid.nlm.nih.gov/api/v2/devices/lookup.json?di=";
            isDi = true;
            di = udiStr;

        }
        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(parent);
        url = url + udiStr;

        // Request a string response from the provided URL.
        final String finalUdiStr = udiStr;
        final String finalUdiStr1 = udiStr;
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        JSONObject responseJson;
                        try {
                            responseJson = new JSONObject(response);
//                            Log.d(TAG, "RESPONSE: " + response);
                            if (responseJson.has("udi")) {
                                JSONObject udi = responseJson.getJSONObject("udi");

                                if (udi.has("lotNumber")) {
                                    lotNumber.setText(udi.getString("lotNumber"));
                                    lotNumber.setEnabled(false);
                                    lotNumber.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
                                }
                                if (udi.has("expirationDate")) {
                                    expiration.setText(udi.getString("expirationDate"));
                                    expiration.setEnabled(false);
                                }
                                if (udi.has("di")) {
                                    di = udi.getString("di");
                                    deviceIdentifier.setText(udi.getString("di"));
                                    deviceIdentifier.setEnabled(false);
                                }
                            }
                            if (isDi) {
                                deviceIdentifier.setText(di);
                                deviceIdentifier.setEnabled(false);
                            }
                            if (responseJson.has("gudid") && responseJson.getJSONObject("gudid").has("device")) {
                                JSONObject deviceInfo = responseJson.getJSONObject("gudid").getJSONObject("device");

                                if (deviceInfo.has("companyName")) {
                                    company.setText(deviceInfo.getString("companyName"));
                                    company.setEnabled(false);
                                    company.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
                                }
                                if (deviceInfo.has("gmdnTerms")) {
                                    nameEditText.setText(deviceInfo.getJSONObject("gmdnTerms").getJSONArray("gmdn").getJSONObject(0).getString("gmdnPTName"));
                                    nameEditText.setEnabled(false);
                                    nameEditText.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
                                }
                                if (deviceInfo.has("deviceDescription")) {
                                    deviceDescription.setText(deviceInfo.getString("deviceDescription"));
                                    deviceDescription.setEnabled(false);
                                    deviceDescription.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
                                }
                                if (deviceInfo.has("catalogNumber")) {
                                    referenceNumber.setText(deviceInfo.getString("catalogNumber"));
                                    referenceNumber.setEnabled(false);
                                }
                                if (deviceInfo.has("deviceCount")) {
                                    numberAdded.setText(deviceInfo.getString("deviceCount"));
                                }
                                if (deviceInfo.has("deviceSizes") && deviceInfo.getJSONObject("deviceSizes").has("deviceSize")) {
                                    JSONArray deviceSizeArray = deviceInfo.getJSONObject("deviceSizes").getJSONArray("deviceSize");
                                    for (int i = 0; i < deviceSizeArray.length(); ++i) {
                                        int colonIndex;
                                        String k;
                                        String v;
                                        JSONObject currentSizeObject = deviceSizeArray.getJSONObject(i);
                                        k = currentSizeObject.getString("sizeType");
                                        if (k.equals("Device Size Text, specify")) {
                                            String customSizeText = currentSizeObject.getString("sizeText");
                                            // Key, Value usually separated by colon
                                            colonIndex = customSizeText.indexOf(":");
                                            if (colonIndex == -1) {
                                                // If no colon, save whole field as "value"
                                                k = "Custom Key";
                                                v = customSizeText;
                                            } else {
                                                k = customSizeText.substring(0, colonIndex);
                                                v = customSizeText.substring(colonIndex + 1).trim();
                                            }
                                        } else {
                                            v = currentSizeObject.getJSONObject("size").getString("value")
                                                    + " "
                                                    + currentSizeObject.getJSONObject("size").getString("unit");
                                        }
                                        addItemSpecs(k, v, view);
                                    }
                                }
                                autoPopulateFromDatabase(finalUdiStr, di, view);
                            }

                            if (responseJson.has("productCodes")) {
                                JSONArray productCodes = responseJson.getJSONArray("productCodes");
                                StringBuilder medicalSpecialties = new StringBuilder();
                                for (int i = 0; i < productCodes.length(); i++) {
                                    medicalSpecialties.append(productCodes.getJSONObject(i).getString("medicalSpecialty"));
                                    medicalSpecialties.append("; ");
                                }
                                medicalSpecialties = new StringBuilder(medicalSpecialties.substring(0, medicalSpecialties.length() - 2));

                                medicalSpeciality.setText(medicalSpecialties.toString());
                                medicalSpeciality.setEnabled(false);
                                medicalSpeciality.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
                            }
                        } catch (JSONException e) {
                            FirebaseCrashlytics.getInstance().recordException(e);
                            e.printStackTrace();
//                            Log.d(TAG, "ERROR: "+ e.getMessage());
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                FirebaseCrashlytics.getInstance().recordException(error);
//                Log.d(TAG, "Error in parsing barcode");
                //Call checkValidUdi
//                nonGudidUdi(finalUdiStr1, view);
                setValidDi(finalUdiStr, view);
            }
        });
        // Add the request to the RequestQueue.
        queue.add(stringRequest);
    }

    //if its valid udi, set diTextView with valid Di
    //Call parseUDi
    private void setValidDi(final String udiStr, final View view) {
        RequestQueue queue = Volley.newRequestQueue(parent);
        String url ="https://accessgudid.nlm.nih.gov/api/v2/parse_udi.json?udi=";
        url = url + udiStr;

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
            new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    JSONObject responseJson;
                    try {
                        responseJson = new JSONObject(response);
                        if (responseJson.has("udi")) {
                            String udi = responseJson.getString("udi");
                            if (responseJson.has("di")) {
                                di = responseJson.getString("di");
                                deviceIdentifier.setText(di);
                                autoPopulateFromDatabase(udiStr, di, view);
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            } ,
            new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    FirebaseCrashlytics.getInstance().recordException(error);
                    nonGudidUdi(udiStr, view);
                }
            });
        queue.add(stringRequest);
    }

    private void notInDatabaseError(View view) {
        new MaterialAlertDialogBuilder(view.getContext())
                .setTitle("Equipment status")
                .setMessage("Equipment has not been stored in inventory yet.\n" +
                        "Please fill out all fields carefully")
                .setNeutralButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                })
                .show();
    }

    private void nonGudidUdi(final String udiStr, final View view) {

        if (Objects.requireNonNull(udiEditText.getText()).toString().length() <= 0 && (!editingExisting)) {
            Toast.makeText(parent, "Please enter barcode and click on Autopopulate again", Toast.LENGTH_LONG).show();
            deviceIdentifier.setError("Enter device identifier (DI)");
        } else if (udiEditText.getText().toString().length() > 0 && (!editingExisting)) {
            DocumentReference udiDocRef = db.collection("networks").document(mNetworkId)
                    .collection("hospitals").document(mHospitalId).collection("departments")
                    .document("default_department").collection("dis")
                    .document(udiStr).collection("udis").document(udiStr);


            udiDocRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (Objects.requireNonNull(document).exists()) {
                            autoPopulateFromDatabase(udiStr, udiStr, view);
                            Toast.makeText(parent, "Equipment already exists in inventory, " +
                                    "please fill out remaining fields", Toast.LENGTH_SHORT).show();
                        } else {
                            notInDatabaseError(view);
                            Log.d(TAG, "No such document");
                        }
                    } else {
                        //Call
                        Log.d(TAG, "get failed with ", task.getException());
                    }
                }
            });
        }
        TextWatcher deviceIdentifierWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (deviceIdentifier.getText().toString().length() > 0) {
                    deviceIdentifier.setError(null);
                }

            }
        };
        deviceIdentifier.addTextChangedListener(deviceIdentifierWatcher);
    }


    private void autopopulateNonGudid(String barcode, String di, View view) {
        autoPopulateFromDatabase(barcode, di, view);
    }

    private void autoPopulateFromDatabase(final String udiStr, String di, final View view) {
        DocumentReference udiDocRef;
        DocumentReference diDocRef;
        udiDocRef = db.collection("networks").document(mNetworkId)
                .collection("hospitals").document(mHospitalId).collection("departments")
                .document("default_department").collection("dis").document(di)
                .collection("udis").document(udiStr);
        diDocRef = db.collection("networks").document(mNetworkId)
                .collection("hospitals").document(mHospitalId).collection("departments")
                .document("default_department").collection("dis").document(di);

        diDocRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (Objects.requireNonNull(document).exists()) {
                        if (document.get(COMPANY_KEY) != null && Objects.requireNonNull(company.getText()).toString().length() <= 0) {
                            company.setText(document.getString(COMPANY_KEY));
                            company.setEnabled(false);
                        }
                        if (document.get(SITE_KEY) != null && hospitalName.getText().toString().length() <= 0) {
                            hospitalName.setText(document.getString(SITE_KEY));
                            hospitalName.setEnabled(false);
                        }
                        if (document.get("di") != null && Objects.requireNonNull(deviceIdentifier.getText()).toString().length() <= 0) {
                            deviceIdentifier.setText(document.getString("di"));
                            deviceIdentifier.setEnabled(false);
                        }

                        if (document.get(DESCRIPTION_KEY) != null) {
                            deviceDescription.setText(document.getString(DESCRIPTION_KEY));
                            deviceDescription.setEnabled(false);
                        }
                        if (document.get(SPECIALTY_KEY) != null && Objects.requireNonNull(medicalSpeciality.getText()).toString().length() <= 0) {
                            medicalSpeciality.setText(document.getString(SPECIALTY_KEY));
                            medicalSpeciality.setEnabled(false);
                        }
                        if (document.get(NAME_KEY) != null && Objects.requireNonNull(nameEditText.getText()).toString().length() <= 0) {
                            nameEditText.setText(document.getString(NAME_KEY));
                            nameEditText.setEnabled(false);
                        }

                        if (document.get(TYPE_KEY) != null && equipmentType.getText().toString().length() <= 0) {
                            equipmentType.setText(document.getString(TYPE_KEY));
                            equipmentType.setEnabled(false);
                        }
                        if (document.get(SITE_KEY) != null && hospitalName.getText().toString().length() <= 0) {
                            hospitalName.setText(document.getString(SITE_KEY));
                            hospitalName.setEnabled(false);
                        }
                        if (document.get(QUANTITY_KEY) != null) {
                            diQuantity = document.getString(QUANTITY_KEY);
                        } else {
                            diQuantity = "0";
                        }
                        if (document.get(USAGE_KEY) != null) {
                            String usage = document.getString(USAGE_KEY);
                            if (Objects.requireNonNull(usage).equalsIgnoreCase("Single Use")) {
                                singleUseButton.setChecked(true);
                            } else if (usage.equalsIgnoreCase("Reusable")) {
                                multiUse.setChecked(true);
                            }
                        }
                    } else {
                        diQuantity = "0";
                        notInDatabaseError(view);
                        Log.d(TAG, "Di doesn't exist in database ", task.getException());
                    }
                } else {
                    Log.d(TAG, "Failed with: ", task.getException());
                }
            }
        });

        udiDocRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (Objects.requireNonNull(document).exists()) {
                        if (document.get("quantity") != null) {
                            itemQuantity = document.getString(QUANTITY_KEY);
                            quantity.setText(itemQuantity);
                        } else {
                            itemQuantity = "0";
                            quantity.setText("0");
                        }
                        if (document.get(PHYSICALLOC_KEY) != null) {
                            physicalLocation.setText(document.getString(PHYSICALLOC_KEY));

                        }

                        if (document.get("reference_number") != null && Objects.requireNonNull(referenceNumber.getText()).toString().length() <= 0) {
                            referenceNumber.setText(document.getString("reference_number"));
                            referenceNumber.setEnabled(false);
                        }
                        if (document.get("expiration") != null && Objects.requireNonNull(expiration.getText()).toString().length() <= 0) {
                            expiration.setText(document.getString("expiration"));
                            expiration.setEnabled(false);
                        }
                        if (document.get("lot_number") != null && Objects.requireNonNull(lotNumber.getText()).toString().length() <= 0) {
                            lotNumber.setText(document.getString("lot_number"));
                            lotNumber.setEnabled(false);
                        }
                        if (document.get("notes") != null) {
                            notes.setText(document.getString("notes"));
                        }

                    } else {
                        itemQuantity = "0";
                        quantity.setText("0");
//                        Log.d(TAG, "Document does not exist!");
                    }
//                    quantity.setText(document.getString(QUANTITY_KEY));
                    quantity.setEnabled(false);
                } else {
                    Log.d(TAG, "Failed with: ", task.getException());
                }
            }
        });
    }
}