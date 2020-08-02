package com.levigo.levigoapp;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.DatePicker;
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
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.zxing.integration.android.IntentIntegrator;
import com.journeyapps.barcodescanner.CaptureActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

public class ItemDetailFragment extends Fragment {

    private String mNetworkId;
    private String mHospitalId;

    // Firebase database
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final CollectionReference usersRef = db.collection("users");

    private DocumentReference typeRef;
    private CollectionReference siteRef;
    private DocumentReference physLocRef;
    private CollectionReference accessionNumberRef;

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
    private LinearLayout itemUsedFields;
    private LinearLayout linearLayout;
    private TextInputLayout procedureDateLayout;
    private TextInputEditText procedureNameEditText;
    private TextInputEditText accessionNumberEditText;
    private TextInputLayout numberAddedLayout;

    private Button saveButton;
    private MaterialButton addProcedure;
    private MaterialButton removeProcedure;
    private MaterialButton removeSizeButton;
    private SwitchMaterial itemUsed;
    private RadioGroup useRadioGroup;
    private RadioButton singleUseButton;
    private RadioButton multiUse;
    private Button addSizeButton;

    private String itemQuantity;
    private String diQuantity;
    private int procedureFieldAdded;
    private int emptySizeFieldCounter = 0;
    private int typeCounter;
    private int siteCounter;
    private int locCounter;
    private int procedureListCounter;
    private long millsIn;
    private boolean chosenType;
    private boolean chosenLocation;
    private boolean isAddSizeButtonClicked;
    private boolean chosenSite;
    private boolean checkEditTexts;
    private boolean checkAutocompleteTexts;
    private boolean checkItemUsed;
    private boolean checkSingleUseButton;
    private boolean checkMultiUseButton;
    private boolean isTimeinSelected;
    private boolean checkProcedureFields;
    private boolean accessionNumberGenerated;
    private List<TextInputEditText> allSizeOptions;
    private ArrayList<String> TYPES;
    private ArrayList<String> SITELOC;
    private ArrayList<String> PHYSICALLOC;
    private List<Map<String, Object>> procedureMapList;
    private List<TextInputEditText> numberUsedList;

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
    private final String PROCEDURE_KEY = "procedure_used";
    private final String PROCEDUREDATE_KEY = "procedure_date";
    private final String AMOUNTUSED_KEY = "amount_used";
    private final String ACCESSION_KEY = "accession_number";
    private final String PHYSICALLOC_KEY = "physical_location";
    private final String TIMEIN_KEY = "time_in";
    private final String TIMEOUT_KEY = "time_out";
    private final String QUANTITY_KEY = "quantity";
    private final String SINGLEORMULTI_KEY = "single_multi";

    private float dp;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        dp = Objects.requireNonNull(getContext()).getResources().getDisplayMetrics().density;
        // Inflate the layout for this fragment
        final View rootView = inflater.inflate(R.layout.fragment_itemdetail, container, false);
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
        TextInputLayout expirationTextLayout = rootView.findViewById(R.id.expiration_date_string);
        TextInputLayout dateInLayout = rootView.findViewById(R.id.in_date_layout);
        final TextInputLayout timeInLayout = rootView.findViewById(R.id.in_time_layout);
        itemUsed = rootView.findViewById(R.id.detail_used_switch);
        saveButton = rootView.findViewById(R.id.detail_save_button);
        Button rescanButton = rootView.findViewById(R.id.detail_rescan_button);
        addProcedure = rootView.findViewById(R.id.button_addpatient);
        removeProcedure = rootView.findViewById(R.id.button_removepatient);
        Button autoPopulateButton = rootView.findViewById(R.id.detail_autopop_button);
        useRadioGroup = rootView.findViewById(R.id.RadioGroup_id);
        TextInputLayout diLayout = rootView.findViewById(R.id.TextInputLayout_di);
        singleUseButton = rootView.findViewById(R.id.RadioButton_single);
        multiUse = rootView.findViewById(R.id.radio_multiuse);
        numberAddedLayout = rootView.findViewById(R.id.numberAddedLayout);
        MaterialToolbar topToolBar = rootView.findViewById(R.id.topAppBar);

        siteConstrainLayout = rootView.findViewById(R.id.site_linearlayout);
        physicalLocationConstrainLayout = rootView.findViewById(R.id.physicalLocationLinearLayout);
        typeConstrainLayout = rootView.findViewById(R.id.typeLinearLayout);
        chosenType = false;
        chosenSite = false;
        chosenLocation = false;
        checkAutocompleteTexts = false;
        checkEditTexts = false;
        checkItemUsed = false;
        checkSingleUseButton = false;
        checkMultiUseButton = false;
        isAddSizeButtonClicked = true;
        isTimeinSelected = false;
        checkProcedureFields = false;
        accessionNumberGenerated = false;
        itemUsed.setChecked(false);
        addSizeButton = rootView.findViewById(R.id.button_addsize);
        itemUsedFields = rootView.findViewById(R.id.layout_itemused);
        itemUsedFields.setVisibility(View.GONE);
        specsTextView = rootView.findViewById(R.id.detail_specs_textview);
        allSizeOptions = new ArrayList<>();
        TYPES = new ArrayList<>();
        SITELOC = new ArrayList<>();
        PHYSICALLOC = new ArrayList<>();
        procedureMapList = new ArrayList<>();
        numberUsedList = new ArrayList<>();

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        String userId = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();

        // Get user information in "users" collection
        final DocumentReference currentUserRef = usersRef.document(userId);
        currentUserRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                String toastMessage;
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        try {
                            mNetworkId = Objects.requireNonNull(document.get("network_id")).toString();
                            mHospitalId = Objects.requireNonNull(document.get("hospital_id")).toString();
                            typeRef = db.collection("networks").document(mNetworkId).collection("hospitals")
                                    .document(mHospitalId).collection("types").document("type_options");
                            siteRef = db.collection("networks").document(mNetworkId)
                                    .collection("hospitals");
                            physLocRef = db.collection("networks").document(mNetworkId)
                                    .collection("hospitals").document(mHospitalId)
                                    .collection("physical_locations").document("locations");
                            accessionNumberRef = db.collection("networks")
                                    .document(mNetworkId)
                                    .collection("hospitals").document(mHospitalId)
                                    .collection("accession_numbers");

                            //get realtime update for Equipment Type field from database
                            updateEquipmentType(rootView);

                            //get realtime update for Site field from database
                            updateSite(rootView);

                            //get realtime update for Physical Location field from database
                            updatePhysicalLocation(rootView);

                        } catch (NullPointerException e) {
                            toastMessage = "Error retrieving user information; Please contact support";
                            Toast.makeText(parent.getApplicationContext(), toastMessage, Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        // document for user doesn't exist
                        toastMessage = "User not found; Please contact support";
                        Toast.makeText(parent.getApplicationContext(), toastMessage, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    toastMessage = "User lookup failed; Please try again and contact support if issue persists";
                    Toast.makeText(parent.getApplicationContext(), toastMessage, Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
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


        // icon listener to search di in database to autopopulate di-specific fields
        diLayout.setEndIconOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                autoPopulateFromDatabase(view, Objects.requireNonNull(deviceIdentifier.getText()).toString().trim());
            }
        });


        //set TextWatcher for required fields
        setTextWatcherRequired();


        autoPopulateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                autoPopulate(rootView);
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
//                parent.onBackPressed();
            }
        });
        //going back to inventory view
        topToolBar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (parent != null)
                    parent.onBackPressed();
            }
        });


        addProcedure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(checkProcedureFields) {
                    removeProcedure.setEnabled(true);
                    checkProcedureFields = false;
                    addProcedureField(view);
                }else{
                    Toast.makeText(rootView.getContext(), "Please fill out added procedue " +
                            "information fields before adding a new one", Toast.LENGTH_LONG).show();
                }
            }
        });

        // when clicked remove one added field "Patient ID"
        removeProcedure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (procedureFieldAdded - procedureListCounter > 0) {
                    if(checkProcedureFields) {
                        procedureMapList.remove(procedureMapList.size() - 1);
                        System.out.println(procedureMapList);
                    }
                    if(accessionNumberGenerated) {
                        removeAccessionNumber(itemUsedFields);
                    }
                    itemUsedFields.removeViewAt(itemUsedFields.indexOfChild(addProcedure) - 1);
                    numberUsedList.remove(numberUsedList.size() - 1);
                    --procedureFieldAdded;
                }
                if (procedureFieldAdded - procedureListCounter == 0) {
                    removeProcedure.setEnabled(false);
                    itemUsed.setChecked(false);
                }
                checkProcedureFields = true;
            }
        });

        itemUsed.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    checkItemUsed = true;
                    itemUsedFields.setVisibility(View.VISIBLE);
                    addProcedureField(rootView);
                    removeProcedure.setEnabled(true);
                    numberAdded.setText("0");
                    numberAddedLayout.setVisibility(View.GONE);

                } else {
                    // enable saveButton
                    checkItemUsed = false;
                    numberAddedLayout.setVisibility(View.VISIBLE);
                    itemUsedFields.setVisibility(View.GONE);
                    while (procedureFieldAdded - procedureListCounter > 0) {
                        itemUsedFields.removeViewAt(itemUsedFields.indexOfChild(addProcedure) - 1);
                        --procedureFieldAdded;
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
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //hardcoded
                if((checkAutocompleteTexts && checkEditTexts) && (checkSingleUseButton || checkMultiUseButton)) {
                    if(checkItemUsed){
                        if(!checkProcedureFields){
                            Toast.makeText(rootView.getContext(), "Please enter procedure information", Toast.LENGTH_SHORT).show();
                            return;
                        }

                    }
                    saveData(rootView, "networks", mNetworkId, "hospitals",
                            mHospitalId, "departments",
                            "default_department", "dis");
                }else{
                    Toast.makeText(rootView.getContext(), "Please fill out all required fields", Toast.LENGTH_SHORT).show();
                }
            }
        });

        if (getArguments() != null) {
            String barcode = getArguments().getString("barcode");
            udiEditText.setText(barcode);
            autoPopulate(rootView);
        }
        return rootView;
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

    private void updatePhysicalLocation(View view) {
        physLocRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w(TAG, "Listen failed.", e);
                    return;
                }
                if (documentSnapshot != null && documentSnapshot.exists()) {
                    Map<String, Object> typeObj = documentSnapshot.getData();
                    locCounter = typeObj.size();
                    for (Object value : typeObj.values()) {
                        if (!PHYSICALLOC.contains(value.toString())) {
                            PHYSICALLOC.add(value.toString());
                        }
                        Collections.sort(PHYSICALLOC);
                    }
                } else {
                    Log.d(TAG, "Current data: null");
                }
            }
        });

        final ArrayAdapter<String> adapterLoc =
                new ArrayAdapter<>(
                        view.getContext(),
                        R.layout.dropdown_menu_popup_item,
                        PHYSICALLOC);
        adapterLoc.add("Other");
        physicalLocation.setAdapter(adapterLoc);
        physicalLocation.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                addNewLoc(adapterView, view, i);
            }
        });

    }


    private void updateSite(View view) {
        siteRef.document("site_options").addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w(TAG, "Listen failed.", e);
                    return;
                }
                if (documentSnapshot != null && documentSnapshot.exists()) {
                    Map<String, Object> typeObj = documentSnapshot.getData();
                    siteCounter = Objects.requireNonNull(typeObj).size();
                    for (Object value : typeObj.values()) {
                        if (!SITELOC.contains(value.toString())) {
                            SITELOC.add(value.toString());
                        }
                        Collections.sort(SITELOC);
                    }
                } else {
                    Log.d(TAG, "Current data: null");
                }
            }
        });

        // Dropdown menu for Site Location field
        final ArrayAdapter<String> adapterSite =
                new ArrayAdapter<>(
                        view.getContext(),
                        R.layout.dropdown_menu_popup_item,
                        SITELOC);
        adapterSite.add("Other");
        hospitalName.setAdapter(adapterSite);
        hospitalName.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                addNewSite(adapterView, view, i);
            }
        });

    }

    private void updateEquipmentType(View view) {
        typeRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w(TAG, "Listen failed.", e);
                    return;
                }
                if (documentSnapshot != null && documentSnapshot.exists()) {
                    Map<String, Object> typeObj = documentSnapshot.getData();
                    typeCounter = Objects.requireNonNull(typeObj).size();
                    for (Object value : typeObj.values()) {
                        if (!TYPES.contains(value.toString())) {
                            TYPES.add(value.toString());
                        }
                        Collections.sort(TYPES);
                    }
                } else {
                    Log.d(TAG, "Current data: null");
                }
            }
        });
        // adapter for dropdown list for Types
        final ArrayAdapter<String> adapterType =
                new ArrayAdapter<>(
                        view.getContext(),
                        R.layout.dropdown_menu_popup_item,
                        TYPES);
        adapterType.add("Other");
        equipmentType.setAdapter(adapterType);


        equipmentType.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                addTypeOptionField(adapterView, view, i);

            }
        });
    }

    private void setTextWatcherRequired() {

        TextWatcher autoCompleteTextWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {

                for (AutoCompleteTextView editText : new AutoCompleteTextView[]{equipmentType,
                        hospitalName, physicalLocation}) {
                    if ((editText.getText().toString().trim().isEmpty())) {
                        checkAutocompleteTexts = false;
                        return;
                    }
                }

                checkAutocompleteTexts = true;
            }
        };
        equipmentType.addTextChangedListener(autoCompleteTextWatcher);
        hospitalName.addTextChangedListener(autoCompleteTextWatcher);
        physicalLocation.addTextChangedListener(autoCompleteTextWatcher);


        TextWatcher editTextWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {

                for (TextInputEditText editText : new TextInputEditText[]{udiEditText, nameEditText,
                        company, expiration, lotNumber, referenceNumber, numberAdded, deviceIdentifier,
                        dateIn, timeIn}) {
                    if (Objects.requireNonNull(editText.getText()).toString().trim().isEmpty()) {
                        checkEditTexts = false;
                        return;
                    }

                }

                checkEditTexts = true;
            }
        };

        udiEditText.addTextChangedListener(editTextWatcher);
        nameEditText.addTextChangedListener(editTextWatcher);
        company.addTextChangedListener(editTextWatcher);
        expiration.addTextChangedListener(editTextWatcher);
        lotNumber.addTextChangedListener(editTextWatcher);
        referenceNumber.addTextChangedListener(editTextWatcher);
        numberAdded.addTextChangedListener(editTextWatcher);
        deviceIdentifier.addTextChangedListener(editTextWatcher);
        dateIn.addTextChangedListener(editTextWatcher);
        timeIn.addTextChangedListener(editTextWatcher);

        singleUseButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                checkSingleUseButton = true;
            }

        });

        multiUse.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                checkMultiUseButton = true;
            }
        });
    }


    private void setIconsAndDialogs(final TextInputEditText procedureDateEditText) {
        final DatePickerDialog.OnDateSetListener date_proc = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
                myCalendar.set(Calendar.YEAR, i);
                myCalendar.set(Calendar.MONTH, i1);
                myCalendar.set(Calendar.DAY_OF_MONTH, i2);
                String myFormat = "yyyy/MM/dd";
                SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
                procedureDateEditText.setText(String.format("%s", sdf.format(myCalendar.getTime())));
            }
        };


        procedureDateLayout.setEndIconOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new DatePickerDialog(view.getContext(), date_proc, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });


    }

    private void incrementNumberUsed(TextInputEditText numberUsed) {
        int newNumber = 0;
        try {
            newNumber = Integer.parseInt(Objects.requireNonNull(numberUsed.getText()).toString());
        } catch (NumberFormatException e) {
            Log.d(TAG, e.toString());
        } finally {
            numberUsed.setText(String.valueOf(++newNumber));
        }
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

    // when clicked adds one more additional field for Patient ID
    private void addProcedureField(View view) {
        checkProcedureFields = false;
        accessionNumberGenerated = false;
        final boolean[] procedureInfoAdded = {false};
        final SimpleDateFormat format = new SimpleDateFormat("HH:mm",Locale.US);
        final Map<String, Object> procedureInfoMap = new HashMap<>();
        procedureFieldAdded++;

        LinearLayout procedureInfoLayout = new LinearLayout(view.getContext());
        procedureInfoLayout.setTag("procedure_info");
        procedureInfoLayout.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        procedureInfoLayout.setOrientation(LinearLayout.VERTICAL);
        procedureInfoLayout.setGravity(Gravity.END);

        final TextView procedureNumber = new TextView(view.getContext());
        procedureNumber.setPadding(0, 10, 0, 0);
        procedureNumber.setTextSize(18);
        procedureNumber.setTypeface(procedureNumber.getTypeface(), Typeface.BOLD);
        SpannableString content = new SpannableString(String.format(Locale.US, "Procedure #%d Description",
                procedureFieldAdded));
        content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
        procedureNumber.setText(content);

        procedureDateLayout = (TextInputLayout) View.inflate(view.getContext(),
                R.layout.activity_itemdetail_materialcomponent, null);
        procedureDateLayout.setHint("Enter procedure date");
        procedureDateLayout.setEndIconTintList(ColorStateList.valueOf(getResources().
                getColor(R.color.colorPrimary, Objects.requireNonNull(getActivity()).getTheme())));
        procedureDateLayout.setEndIconMode(TextInputLayout.END_ICON_CUSTOM);
        procedureDateLayout.setEndIconDrawable(R.drawable.calendar);
        procedureDateLayout.setPadding(0, 10, 0, 0);
        final TextInputEditText procedureDateEditText = new TextInputEditText(procedureDateLayout.getContext());
        setIconsAndDialogs(procedureDateEditText);
        procedureDateEditText.setFocusable(false);
        procedureDateEditText.setClickable(true);
        procedureDateEditText.setLayoutParams(new LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT));



        TextInputLayout procedureTimeInLayout = (TextInputLayout) View.inflate(view.getContext(),
                R.layout.activity_itemdetail_materialcomponent, null);
        procedureTimeInLayout.setHint("Enter time in");
        procedureTimeInLayout.setEndIconMode(TextInputLayout.END_ICON_CUSTOM);
        procedureTimeInLayout.setEndIconDrawable(R.drawable.clock);
        procedureTimeInLayout.setEndIconTintList(ColorStateList.valueOf(getResources().
                getColor(R.color.colorPrimary, Objects.requireNonNull(getActivity()).getTheme())));
        procedureTimeInLayout.setPadding(0, 10, 0, 0);
        final TextInputEditText procedureTimeInEditText = new TextInputEditText(procedureTimeInLayout.getContext());
        procedureTimeInEditText.setFocusable(false);
        procedureTimeInEditText.setLayoutParams(new LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT));
        procedureTimeInLayout.setEndIconOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar mcurrentTime = Calendar.getInstance();
                int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
                final int minute = mcurrentTime.get(Calendar.MINUTE);
                TimePickerDialog mTimePicker;
                mTimePicker = new TimePickerDialog(view.getContext(), new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                        procedureTimeInEditText.setText(String.format(Locale.US, "%02d:%02d", selectedHour, selectedMinute));
                        isTimeinSelected = true;

                        try {
                            Date timeIn = format.parse(selectedHour + ":" + selectedMinute);
                            millsIn = Objects.requireNonNull(timeIn).getTime();

                        } catch (ParseException e) {
                            e.printStackTrace();
                        }

                    }
                }, hour, minute, true);
                mTimePicker.setTitle("Select Time");
                mTimePicker.show();


            }
        });
        procedureTimeInLayout.addView(procedureTimeInEditText);

        TextInputLayout procedureFloorTimeLayout = (TextInputLayout) View.inflate(view.getContext(),
                R.layout.activity_itemdetail_materialcomponent, null);
        procedureFloorTimeLayout.setHint("Floor time");
        procedureFloorTimeLayout.setPadding(0, 10, 0, 0);
        final TextInputEditText procedureFloorTimeEditText = new TextInputEditText(procedureFloorTimeLayout.getContext());
        procedureFloorTimeEditText.setLayoutParams(new LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT));
        procedureFloorTimeEditText.setFocusable(false);
        procedureFloorTimeLayout.addView(procedureFloorTimeEditText);




        TextInputLayout procedureTimeOutLayout = (TextInputLayout) View.inflate(view.getContext(),
                R.layout.activity_itemdetail_materialcomponent, null);
        procedureTimeOutLayout.setHint("Enter time out");
        procedureTimeOutLayout.setEndIconMode(TextInputLayout.END_ICON_CUSTOM);
        procedureTimeOutLayout.setEndIconDrawable(R.drawable.clock);
        procedureTimeOutLayout.setEndIconTintList(ColorStateList.valueOf(getResources().
                getColor(R.color.colorPrimary, Objects.requireNonNull(getActivity()).getTheme())));
        procedureTimeOutLayout.setPadding(0, 10, 0, 0);
        final TextInputEditText procedureTimeOutEditText = new TextInputEditText(procedureTimeOutLayout.getContext());
        procedureTimeOutEditText.setFocusable(false);
        procedureTimeOutEditText.setLayoutParams(new LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT));
        procedureTimeOutLayout.setEndIconOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isTimeinSelected) {
                    Calendar mcurrentTime = Calendar.getInstance();
                    int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
                    int minute = mcurrentTime.get(Calendar.MINUTE);
                    TimePickerDialog mTimePicker;
                    mTimePicker = new TimePickerDialog(view.getContext(), new TimePickerDialog.OnTimeSetListener() {
                        @Override
                        public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                            procedureTimeOutEditText.setText(String.format(Locale.US, "%02d:%02d", selectedHour, selectedMinute));

                            try {
                                Date timeOut = format.parse(selectedHour + ":" + selectedMinute);
                                long millsOut = Objects.requireNonNull(timeOut).getTime();
                                long millsDif = millsOut - millsIn;
                                int hours = (int) millsDif/(1000 * 60 * 60);
                                if(hours < 0){
                                    hours = hours + 24;
                                }
                                int mins = (int) (millsDif/(1000*60)) % 60;
                                String totalTime = (hours * 60 + mins) + " minutes";
                                procedureFloorTimeEditText.setText(totalTime);

                            } catch (ParseException e) {
                                e.printStackTrace();
                            }

                        }
                    }, hour, minute, true);
                    mTimePicker.setTitle("Select Time");
                    mTimePicker.show();

                }else{
                    Toast.makeText(view.getContext(), "Please select time in first, please", Toast.LENGTH_LONG).show();
                }
            }
        });

        procedureTimeOutLayout.addView(procedureTimeOutEditText);


        TextInputLayout procedureNameLayout = (TextInputLayout) View.inflate(view.getContext(),
                R.layout.activity_itemdetail_materialcomponent, null);
        procedureNameLayout.setHint("Enter procedure name");
        procedureNameLayout.setPadding(0, 10, 0, 0);
        procedureNameEditText = new TextInputEditText(procedureNameLayout.getContext());
        procedureNameEditText.setLayoutParams(new LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT));


        final TextInputLayout accessionNumberLayout = (TextInputLayout) View.inflate(view.getContext(),
                R.layout.activity_itemdetail_materialcomponent, null);

        accessionNumberLayout.setPadding(0, 10, 0, 10);
        accessionNumberEditText = new TextInputEditText(procedureNameLayout.getContext());
        accessionNumberEditText.setLayoutParams(new LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT));
        accessionNumberEditText.setClickable(true);
        accessionNumberEditText.setFocusable(false);
        accessionNumberEditText.setHint("Tap here to generate accession number");
        accessionNumberLayout.setTag("accession");
        accessionNumberEditText.setTag("accession_text");
        accessionNumberEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                accessionNumberGenerated = true;
                generateNewNumber(view, accessionNumberEditText);
                accessionNumberLayout.setHint("Accession number");
                accessionNumberEditText.setHint("");
            }
        });






        TextInputLayout numberUsedLayout = (TextInputLayout) View.inflate(view.getContext(),
                R.layout.activity_itemdetail_materialcomponent, null);
        numberUsedLayout.setHint("Enter number of items used");
        numberUsedLayout.setPadding(0, 10, 0, 10);
        numberUsedLayout.setClickable(true);
        numberUsedLayout.setFocusable(false);
        numberUsedLayout.setEndIconMode(TextInputLayout.END_ICON_CUSTOM);
        numberUsedLayout.setEndIconDrawable(R.drawable.plusone);
        numberUsedLayout.setEndIconTintList(ColorStateList.valueOf(getResources().
                getColor(R.color.colorPrimary, Objects.requireNonNull(getActivity()).getTheme())));
        final TextInputEditText numberUsedEditText = new TextInputEditText(procedureNameLayout.getContext());
        numberUsedEditText.setId(View.generateViewId());
        numberUsedList.add(numberUsedEditText);
        // incrementing number by 1 when clicked on the end icon
        numberUsedLayout.setEndIconOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                incrementNumberUsed(numberUsedEditText);
            }
        });
        numberUsedEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showNumberPicker(view, numberUsedEditText);
            }
        });
        numberUsedEditText.setClickable(true);
        numberUsedEditText.setFocusable(false);
        numberUsedEditText.setLayoutParams(new LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT));


        TextWatcher newProcedureTextWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                // saving string to HashMap to save it to database

                procedureInfoMap.put(PROCEDURE_KEY,
                        Objects.requireNonNull(procedureNameEditText.getText()).toString());
                procedureInfoMap.put(PROCEDUREDATE_KEY,
                        Objects.requireNonNull(procedureDateEditText.getText()).toString());
                procedureInfoMap.put(AMOUNTUSED_KEY,
                        Objects.requireNonNull(numberUsedEditText.getText()).toString());
                procedureInfoMap.put(ACCESSION_KEY,
                        Objects.requireNonNull(accessionNumberEditText.getText()).toString());
                procedureInfoMap.put(TIMEIN_KEY,
                        Objects.requireNonNull(procedureTimeInEditText.getText()).toString());
                procedureInfoMap.put(TIMEOUT_KEY,
                        Objects.requireNonNull(procedureTimeOutEditText.getText()).toString());
                procedureInfoMap.put("floor_time",
                        Objects.requireNonNull(procedureFloorTimeEditText.getText()).toString());

                checkProcedureFields = validateFields(new TextInputEditText[] {procedureDateEditText, procedureNameEditText,
                        accessionNumberEditText, numberUsedEditText,
                        procedureTimeInEditText,procedureTimeOutEditText,procedureFloorTimeEditText});
                if(checkProcedureFields && (!(procedureInfoAdded[0]))){
                    procedureMapList.add(procedureInfoMap);
                    procedureInfoAdded[0] = true;
                    System.out.println("added " + procedureMapList);
                }
                if(checkProcedureFields && procedureInfoAdded[0]) {
                    procedureMapList.get(procedureMapList.size() - 1).put(AMOUNTUSED_KEY, numberUsedEditText.getText().toString());
                    System.out.println("changed " + procedureMapList);
                }



            }
        };



        procedureNameEditText.addTextChangedListener(newProcedureTextWatcher);
        procedureDateEditText.addTextChangedListener(newProcedureTextWatcher);
        numberUsedEditText.addTextChangedListener(newProcedureTextWatcher);
        accessionNumberEditText.addTextChangedListener(newProcedureTextWatcher);
        procedureTimeInEditText.addTextChangedListener(newProcedureTextWatcher);
        procedureFloorTimeEditText.addTextChangedListener(newProcedureTextWatcher);
        procedureTimeInEditText.addTextChangedListener(newProcedureTextWatcher);
        procedureTimeOutEditText.addTextChangedListener(newProcedureTextWatcher);


        procedureNameLayout.addView(procedureNameEditText);
        procedureDateLayout.addView(procedureDateEditText);
        numberUsedLayout.addView(numberUsedEditText);
        accessionNumberLayout.addView(accessionNumberEditText);
        procedureInfoLayout.addView(procedureNumber, 0);
        procedureInfoLayout.addView(procedureDateLayout, 1);
        procedureInfoLayout.addView(procedureNameLayout, 2);
        procedureInfoLayout.addView(procedureTimeInLayout, 3);
        procedureInfoLayout.addView(procedureTimeOutLayout, 4);
        procedureInfoLayout.addView(procedureFloorTimeLayout, 5);
        procedureInfoLayout.addView(accessionNumberLayout, 6);
        procedureInfoLayout.addView(numberUsedLayout, 7);

        itemUsedFields.addView(procedureInfoLayout, itemUsedFields.indexOfChild(addProcedure));
    }

    private boolean validateFields(TextInputEditText[] fields){
        for (TextInputEditText currentField : fields) {
            if (Objects.requireNonNull(currentField.getText()).toString().length() <= 0) {
                return false;
            }
        }
        return true;
    }

    //checks whether or not the accession number is unique
    private void checkAccessionNumber(final View view, final String accessionNum, final TextInputEditText accessionNumberEditText) {
        final DocumentReference docRef = accessionNumberRef.document(accessionNum);
        System.out.println(docRef);

        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (Objects.requireNonNull(document).exists()) {
                        generateNewNumber(view, accessionNumberEditText);
                    } else {
                        setAccessionNumber(accessionNumberEditText, accessionNum);
                    }
                } else {
                    Log.d(TAG, "Failed with: ", task.getException());
                }
            }
        });
    }

    // generates new number
    public void generateNewNumber(View view, TextInputEditText accessionNumberEditText) {
        Random rand = new Random();
        String randomAccessionNum = null;
        String randomAccession = String.valueOf(1 + rand.nextInt(999999));
        if (randomAccession.length() == 1) {
            randomAccessionNum = "TZ00000" + randomAccession;
        }
        if (randomAccession.length() == 2) {
            randomAccessionNum = "TZ0000" + randomAccession;
        }
        if (randomAccession.length() == 3) {
            randomAccessionNum = "TZ000" + randomAccession;
        }
        if (randomAccession.length() == 4) {
            randomAccessionNum = "TZ00" + randomAccession;
        }
        if (randomAccession.length() == 5) {
            randomAccessionNum = "TZ0" + randomAccession;
        }
        if (randomAccession.length() == 6) {
            randomAccessionNum = "TZ" + randomAccession;
        }
        checkAccessionNumber(view, randomAccessionNum, accessionNumberEditText);

    }

    //sets accession number and save it to the database
    public void setAccessionNumber(TextInputEditText accessionNumberEditText, String accessionNumber) {
        accessionNumberEditText.setText((accessionNumber));
        Map<String, Object> data = new HashMap<>();
        data.put("accession_number", accessionNumber);
        accessionNumberRef.document(accessionNumber)
                .set(data)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "DocumentSnapshot successfully written!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error writing document", e);
                    }
                });


    }

    // removes the accession number from the database if users removes
    // newly added procedure info fields
    private void removeAccessionNumber(LinearLayout itemUsedFields) {
        LinearLayout procedureInfo = (LinearLayout) itemUsedFields.getChildAt((itemUsedFields.indexOfChild(addProcedure)) - 1);
        TextInputLayout accessionNumLayout = procedureInfo.findViewWithTag("accession");
        TextInputEditText accessionNumEditText = accessionNumLayout.findViewWithTag("accession_text");
        accessionNumberRef.document(String.valueOf(accessionNumEditText.getText()))
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

    // adds new row of size text views if users clicks on a button
    int rowIndex = 1;
    int rowLoc = 1;

    private void addEmptySizeOption(View view) {

        Log.d(TAG, "Adding empty size option!");
        emptySizeFieldCounter++;
        LinearLayout layoutSize = new LinearLayout(getContext());
        layoutSize.setOrientation(LinearLayout.HORIZONTAL);
        layoutSize.setLayoutParams(new LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT));
//        GridLayout gridLayoutSize = new GridLayout(view.getContext());
//
//        GridLayout.LayoutParams paramSizeKey = new GridLayout.LayoutParams();
//        paramSizeKey.height = ViewGroup.LayoutParams.WRAP_CONTENT;
//        paramSizeKey.width = WRAP_CONTENT;
//        paramSizeKey.rowSpec = GridLayout.spec(rowIndex);
//        paramSizeKey.columnSpec = GridLayout.spec(0);
//        paramSizeKey.setMargins(0, 0, 0, 20);
//
//
//        GridLayout.LayoutParams paramSizeValue = new GridLayout.LayoutParams();
//        paramSizeValue.height = ViewGroup.LayoutParams.WRAP_CONTENT;
//        paramSizeValue.width = WRAP_CONTENT;
//        paramSizeValue.rowSpec = GridLayout.spec(rowIndex);
//        paramSizeValue.columnSpec = GridLayout.spec(1);
//        paramSizeValue.setMargins(10, 0, 0, 20);


//        TextInputLayout sizeKeyLayout = (TextInputLayout) View.inflate(view.getContext(),
//                R.layout.activity_itemdetail_materialcomponent, null);
        TextInputLayout sizeKeyLayout = new TextInputLayout(view.getContext());
//        sizeKeyLayout.setLayoutParams(paramSizeKey);
        sizeKeyLayout.setHint("Key");
        LinearLayout.LayoutParams klp = new LinearLayout.LayoutParams(0, MATCH_PARENT, 1f);
        klp.setMargins((int) (4 * dp), (int) (4 * dp), (int) (4 * dp), (int) (4 * dp));
        sizeKeyLayout.setLayoutParams(klp);
        TextInputEditText sizeKey = new TextInputEditText(sizeKeyLayout.getContext());
        sizeKey.setSingleLine();
        sizeKey.setEllipsize(TextUtils.TruncateAt.END);

//        TextInputLayout sizeValueLayout = (TextInputLayout) View.inflate(view.getContext(),
//                R.layout.activity_itemdetail_materialcomponent, null);
        TextInputLayout sizeValueLayout = new TextInputLayout(view.getContext());
//        sizeValueLayout.setLayoutParams(paramSizeValue);
        sizeValueLayout.setHint("Value");
        LinearLayout.LayoutParams vlp = new LinearLayout.LayoutParams(0, MATCH_PARENT, 1f);
        vlp.setMargins((int) (4 * dp), (int) (4 * dp), (int) (4 * dp), (int) (4 * dp));
        sizeValueLayout.setLayoutParams(vlp);
        TextInputEditText sizeValue = new TextInputEditText(sizeKeyLayout.getContext());
        sizeValue.setSingleLine();
        sizeValue.setEllipsize(TextUtils.TruncateAt.END);


//        sizeKey.setLayoutParams(new LinearLayout.LayoutParams(430, WRAP_CONTENT));
        sizeKeyLayout.addView(sizeKey);
//        sizeValue.setLayoutParams(new LinearLayout.LayoutParams(430, WRAP_CONTENT));
        sizeValueLayout.addView(sizeValue);
        layoutSize.addView(sizeKeyLayout);
        layoutSize.addView(sizeValueLayout);
//        gridLayoutSize.addView(sizeKeyLayout);
//        gridLayoutSize.addView(sizeValueLayout);


        allSizeOptions.add(sizeKey);
        allSizeOptions.add(sizeValue);
        linearLayout.addView(layoutSize, (rowLoc++) + linearLayout.indexOfChild(specsTextView));
        rowIndex++;
        System.out.println("row index is " + rowIndex);
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
            System.out.println("row loc is :" + rowLoc);

        }
        if (emptySizeFieldCounter == 0) {
            linearLayout.removeViewAt(linearLayout.indexOfChild(removeSizeButton));
            isAddSizeButtonClicked = true;
        }

        allSizeOptions.remove(allSizeOptions.size() - 1);
        allSizeOptions.remove(allSizeOptions.size() - 1);
        System.out.println(allSizeOptions.size());

    }

    private void addItemSpecs(String key, String value, View view) {
        Log.d(TAG, "Adding item specs!");

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
            if ((checkAutocompleteTexts && checkEditTexts) && (checkSingleUseButton || checkMultiUseButton)) {
                saveButton.setEnabled(true);
            }
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
            if ((checkAutocompleteTexts && checkEditTexts) && (checkSingleUseButton || checkMultiUseButton)) {
                saveButton.setEnabled(true);
            }
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
            if ((checkAutocompleteTexts && checkEditTexts) && (checkSingleUseButton || checkMultiUseButton)) {
                saveButton.setEnabled(true);
            }
            chosenLocation = false;
            linearLayout.removeViewAt(1 + linearLayout.indexOfChild(physicalLocationConstrainLayout));
        }
    }


    // method for saving data to firebase cloud firestore
    public void saveData(View view, String NETWORKS, String NETWORK, String SITES, String SITE,
                         String DEPARTMENTS, String DEPARTMENT, String PRODUCTDIS) {


        Log.d(TAG, "SAVING");
        String barcode_str = Objects.requireNonNull(udiEditText.getText()).toString();
        String name_str = Objects.requireNonNull(nameEditText.getText()).toString();
        String company_str = Objects.requireNonNull(company.getText()).toString();
        String medical_speciality_str = Objects.requireNonNull(medicalSpeciality.getText()).toString();
        String di_str = Objects.requireNonNull(deviceIdentifier.getText()).toString();
        String description_str = Objects.requireNonNull(deviceDescription.getText()).toString();
        String lotNumber_str = Objects.requireNonNull(lotNumber.getText()).toString();
        String referenceNumber_str = Objects.requireNonNull(referenceNumber.getText()).toString();
        String expiration_str = Objects.requireNonNull(expiration.getText()).toString();
        String currentDate_str = Objects.requireNonNull(dateIn.getText()).toString();
        int quantity_int;
        String number_added_str = "0";
        int totalUsed = 0;
        for (int i = 0; i < numberUsedList.size(); i++) {
            totalUsed += Integer.parseInt(Objects.requireNonNull(numberUsedList.get(i).getText()).toString());
        }
        if (itemUsed.isChecked()) {
            quantity_int = Integer.parseInt(itemQuantity) - totalUsed;
            diQuantity = String.valueOf(Integer.parseInt(diQuantity) - totalUsed);
        } else {
            number_added_str = Objects.requireNonNull(numberAdded.getText()).toString();
            quantity_int = Integer.parseInt(itemQuantity) +
                    Integer.parseInt(Objects.requireNonNull(numberAdded.getText()).toString());
            diQuantity = String.valueOf(Integer.parseInt(diQuantity) +
                    Integer.parseInt(numberAdded.getText().toString()));
        }
        String quantity_str = String.valueOf(quantity_int);
        String site_name_str;
        if (chosenSite) {
            site_name_str = Objects.requireNonNull(otherSite_text.getText()).toString();
        } else {
            site_name_str = hospitalName.getText().toString();
        }
        String physical_location_str;
        if (chosenLocation) {
            physical_location_str = Objects.requireNonNull(otherPhysicalLoc_text.getText()).toString().trim();
        } else {
            physical_location_str = physicalLocation.getText().toString().trim();
        }
        String type_str;
        if (chosenType) {
            type_str = Objects.requireNonNull(otherType_text.getText()).toString();
        } else {
            type_str = equipmentType.getText().toString();
        }
        String currentTime_str = Objects.requireNonNull(timeIn.getText()).toString();
        String notes_str = Objects.requireNonNull(notes.getText()).toString();


        boolean is_used = itemUsed.isChecked();
        int radioButtonInt = useRadioGroup.getCheckedRadioButtonId();
        RadioButton radioButton = view.findViewById(radioButtonInt);
        String singleOrMultiUse = radioButton.getText().toString();


        // saving di-specific identifiers using HashMap
        Map<String, Object> diDoc = new HashMap<>();
        diDoc.put(NAME_KEY, name_str);
        diDoc.put(TYPE_KEY, type_str);
        diDoc.put(COMPANY_KEY, company_str);
        String DI_KEY = "di";
        diDoc.put(DI_KEY, di_str);
        diDoc.put(SITE_KEY, site_name_str);
        diDoc.put(DESCRIPTION_KEY, description_str);
        diDoc.put(SPECIALTY_KEY, medical_speciality_str);
        diDoc.put(USAGE_KEY, singleOrMultiUse);
        diDoc.put(QUANTITY_KEY, diQuantity);

        DocumentReference diRef = db.collection(NETWORKS).document(NETWORK)
                .collection(SITES).document(SITE).collection(DEPARTMENTS)
                .document(DEPARTMENT).collection(PRODUCTDIS).document(di_str);
        diRef.set(diDoc)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(getActivity(), "equipment saved", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getActivity(), "Error while saving data!", Toast.LENGTH_SHORT).show();
                        Log.d(TAG, e.toString());
                    }
                });

        // saving udi-specific identifiers using InventoryTemplate class to store multiple items at once
        udiDocument = new InventoryTemplate(barcode_str, is_used, number_added_str, lotNumber_str,
                expiration_str, quantity_str, currentTime_str, physical_location_str, referenceNumber_str,
                notes_str, currentDate_str);

        DocumentReference udiRef = db.collection(NETWORKS).document(NETWORK)
                .collection(SITES).document(SITE).collection(DEPARTMENTS)
                .document(DEPARTMENT).collection(PRODUCTDIS).document(di_str)
                .collection("udis").document(barcode_str);

        //saving data of InventoryTemplate to database
        udiRef.set(udiDocument)
                //in case of success
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(getActivity(), "equipment saved", Toast.LENGTH_SHORT).show();
                    }
                })
                // in case of failure
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getActivity(), "Error while saving data!", Toast.LENGTH_SHORT).show();
                        Log.d(TAG, e.toString());
                    }
                });

        Map<String, Object> procedureQuantity = new HashMap<>();
        procedureQuantity.put("procedure_number", String.valueOf(procedureFieldAdded));

        if (checkItemUsed) {
            for (int i = 0; i < procedureMapList.size(); i++) {
                DocumentReference procedureDocRef = db.collection(NETWORKS).document(NETWORK)
                        .collection(SITES).document(SITE).collection(DEPARTMENTS)
                        .document(DEPARTMENT).collection(PRODUCTDIS).document(di_str)
                        .collection("udis").document(barcode_str).collection("procedures")
                        .document("procedure_" + (procedureListCounter + 1));
                procedureListCounter++;
                procedureDocRef.set(procedureMapList.get(i))
                        //in case of success
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(getActivity(), "equipment saved", Toast.LENGTH_SHORT).show();
                            }
                        })
                        // in case of failure
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(getActivity(), "Error while saving data!", Toast.LENGTH_SHORT).show();
                                Log.d(TAG, e.toString());
                            }
                        });
            }


            udiRef.update(procedureQuantity)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d(TAG, e.toString());
                        }
                    });
            procedureQuantity.clear();
        }


        if (allSizeOptions.size() > 0) {
            int i = 0;
            Map<String, Object> sizeOptions = new HashMap<>();
            while (i < allSizeOptions.size()) {
                sizeOptions.put(Objects.requireNonNull(allSizeOptions.get(i++).getText()).toString().trim(),
                        Objects.requireNonNull(allSizeOptions.get(i++).getText()).toString().trim());
            }
            diRef.update(sizeOptions)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(getActivity(), "equipment saved", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(getActivity(), "Error while saving data!", Toast.LENGTH_SHORT).show();
                            Log.d(TAG, e.toString());
                        }
                    });
        }
    }

    String di = "";

    private void autoPopulate(final View view) {


        final String udiStr = Objects.requireNonNull(udiEditText.getText()).toString();
        Log.d(TAG, udiStr);

        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(parent);
        String url = "https://accessgudid.nlm.nih.gov/api/v2/devices/lookup.json?udi=";
        //  final String[] di = {""};

        url = url + udiStr;

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        JSONObject responseJson;
                        try {
                            responseJson = new JSONObject(response);

                            Log.d(TAG, "RESPONSE: " + response);

                            JSONObject deviceInfo = responseJson.getJSONObject("gudid").getJSONObject("device");
                            JSONObject udi = responseJson.getJSONObject("udi");
                            JSONArray productCodes = responseJson.getJSONArray("productCodes");
                            StringBuilder medicalSpecialties = new StringBuilder();
                            for (int i = 0; i < productCodes.length(); i++) {
                                medicalSpecialties.append(productCodes.getJSONObject(i).getString("medicalSpecialty"));
                                medicalSpecialties.append("; ");
                            }
                            medicalSpecialties = new StringBuilder(medicalSpecialties.substring(0, medicalSpecialties.length() - 2));

                            lotNumber.setText(udi.getString("lotNumber"));
                            lotNumber.setEnabled(false);
                            lotNumber.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);

                            company.setText(deviceInfo.getString("companyName"));

                            company.setEnabled(false);
                            company.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);

                            expiration.setText(udi.getString("expirationDate"));
                            expiration.setEnabled(false);


                            di = udi.getString("di");
                            deviceIdentifier.setText(udi.getString("di"));
                            deviceIdentifier.setEnabled(false);

                            updateProcedureFieldAdded(udiStr, di);


                            nameEditText.setText(deviceInfo.getJSONObject("gmdnTerms").getJSONArray("gmdn").getJSONObject(0).getString("gmdnPTName"));
                            nameEditText.setEnabled(false);
                            nameEditText.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);

                            deviceDescription.setText(deviceInfo.getString("deviceDescription"));
                            deviceDescription.setEnabled(false);
                            deviceDescription.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);

                            referenceNumber.setText(deviceInfo.getString("catalogNumber"));
                            referenceNumber.setEnabled(false);

                            medicalSpeciality.setText(medicalSpecialties.toString());
                            medicalSpeciality.setEnabled(false);
                            medicalSpeciality.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);

                            numberAdded.setText(deviceInfo.getString("deviceCount"));
                            autoPopulateFromDatabase(udiStr);

                            JSONArray deviceSizeArray = deviceInfo.getJSONObject("deviceSizes").getJSONArray("deviceSize");

                            for (int i = 0; i < deviceSizeArray.length(); ++i) {
                                String k;
                                String v;
                                JSONObject currentSizeObject = deviceSizeArray.getJSONObject(i);
                                k = currentSizeObject.getString("sizeType");
                                Log.d(TAG, "KEYS: " + k);
                                if (k.equals("Device Size Text, specify")) {
                                    String customSizeText = currentSizeObject.getString("sizeText");
                                    // Key is usually substring before first number (e.g. "Co-Axial Introducer Needle: 17ga x 14.9cm")
                                    k = customSizeText.split("[0-9]+")[0];

                                    // needs remember the cutoff to retrieve the rest of the string
                                    int cutoff = k.length();
                                    // take off trailing whitespace
                                    try {
                                        k = k.substring(0, k.length() - 2);
                                    } catch (StringIndexOutOfBoundsException e) { // if sizeText starts with number
                                        k = "Size";
                                    }

                                    // Value is assumed to be the substring starting with the number
                                    v = customSizeText.substring(cutoff);
                                    Log.d(TAG, "Custom Key: " + k);
                                    Log.d(TAG, "Custom Value: " + v);

                                } else {
                                    v = currentSizeObject.getJSONObject("size").getString("value")
                                            + " "
                                            + currentSizeObject.getJSONObject("size").getString("unit");
                                    Log.d(TAG, "Value: " + v);
                                }
                                addItemSpecs(k, v, view);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG, "Error in parsing barcode");
            }
        });

// Add the request to the RequestQueue.
        queue.add(stringRequest);


    }

    private void updateProcedureFieldAdded(String udi, String di) {

        DocumentReference UdiDocRef = db.collection("networks").document(mNetworkId)
                .collection("hospitals").document(mHospitalId)
                .collection("departments").document("default_department");
        System.out.println("udi is " + udi);


        UdiDocRef.collection("dis").document(di).collection("udis")
                .document(udi).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (Objects.requireNonNull(document).exists()) {
                        if (document.get("procedure_number") != null) {
                            procedureFieldAdded = Integer.parseInt(
                                    Objects.requireNonNull(document.getString("procedure_number")));
                            procedureListCounter = procedureFieldAdded;


                        } else {
                            procedureFieldAdded = 0;
                            procedureListCounter = 0;

                        }

                    } else {
                        procedureFieldAdded = 0;
                        procedureListCounter = 0;


                        Log.d(TAG, "Document does not exist!");
                    }
                } else {
                    procedureFieldAdded = 0;
                    procedureListCounter = 0;

                    Log.d(TAG, "Failed with: ", task.getException());
                }
            }
        });
    }


    private void autoPopulateFromDatabase(final String udiStr) {
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
                        if (document.get(TYPE_KEY) != null) {
                            equipmentType.setText(document.getString(TYPE_KEY));
//                            equipmentType.setFocusable(false);
                            equipmentType.setEnabled(false);
                        }
                        if (document.get(SITE_KEY) != null) {
                            hospitalName.setText(document.getString(SITE_KEY));
                            hospitalName.setFocusable(false);
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
                        Log.d(TAG, "Document does not exist!");
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
                    } else {
                        itemQuantity = "0";
                        quantity.setText("0");

                        Log.d(TAG, "Document does not exist!");
                    }
                    quantity.setText(document.getString(QUANTITY_KEY));
                    quantity.setEnabled(false);
                } else {
                    Log.d(TAG, "Failed with: ", task.getException());
                }
            }
        });

    }


    private void autoPopulateFromDatabase(final View view, String di) {

        DocumentReference diDocRef = db.collection("networks").document(mNetworkId)
                .collection("hospitals").document(mHospitalId)
                .collection("departments").document("default_department")
                .collection("dis").document(di);


        diDocRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (Objects.requireNonNull(document).exists()) {
                        quantity.setText("0");
                        company.setText(document.getString(COMPANY_KEY));
                        deviceDescription.setText(document.getString(DESCRIPTION_KEY));
                        equipmentType.setText(document.getString(TYPE_KEY));
                        medicalSpeciality.setText(document.getString(SPECIALTY_KEY));
                        nameEditText.setText(document.getString(NAME_KEY));
                        hospitalName.setText(document.getString(SITE_KEY));
                        hospitalName.setText(document.getString(SINGLEORMULTI_KEY));
                    } else {
                        Toast.makeText(view.getContext(), "Equipment has not found", Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "Document does not exist!");
                    }
                } else {
                    Toast.makeText(view.getContext(), "Equipment has not found", Toast.LENGTH_SHORT).show();
                    itemQuantity = "0";
                    quantity.setText("0");
                    Log.d(TAG, "Failed with: ", task.getException());
                }
            }
        });


    }

}