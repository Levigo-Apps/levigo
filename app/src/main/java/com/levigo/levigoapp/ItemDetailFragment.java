package com.levigo.levigoapp;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
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
import java.util.TimeZone;

import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

public class ItemDetailFragment extends Fragment {

    // Firebase database
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    DocumentReference typeRef = db.collection("networks").document("types");
    DocumentReference siteRef = db.collection("networks").document("network1")
            .collection("sites").document("site_options");
    DocumentReference physLocRef = db.collection("networks").document("network1")
            .collection("sites").document("n1_hospital3")
            .collection("physical_locations").document("locations");
    private DocumentReference udiRef;

    InventoryTemplate udiDocument;

    private static final String TAG = ItemDetailFragment.class.getSimpleName();
    private Activity parent;
    private NetworkActivity Sites = new NetworkActivity();

    // USER INPUT VALUES
    private TextInputEditText udiEditText;
    private TextInputEditText nameEditText;
    private AutoCompleteTextView equipmentType;
    private TextInputEditText company;
    private TextInputEditText procedureUsed;
    private TextInputEditText otherType_text;
    private TextInputEditText otherPhysicalLoc_text;
    private TextInputEditText otherSite_text;
    private TextInputEditText procedureDate;
    private TextInputEditText patient_id;
    private TextInputEditText patient_idDefault;
    private TextInputEditText deviceIdentifier;
    private TextInputEditText deviceDescription;
    private TextInputEditText expiration;
    private TextInputEditText quantity;
    private TextInputEditText numberUsed;
    private TextInputEditText lotNumber;
    private TextInputEditText referenceNumber;
    private TextInputEditText amountUsed;
    private AutoCompleteTextView hospitalName;
    private AutoCompleteTextView physicalLocation;
    private TextInputEditText notes;
    private TextInputEditText dateIn;
    private TextInputEditText timeIn;
    private TextInputEditText numberAdded;
    private TextInputLayout expirationTextLayout;
    private TextInputLayout dateInLayout;
    private TextInputLayout siteLocationLayout;
    private TextInputLayout physLocationLayout;
    private TextInputEditText medicalSpeciality;
    private TextInputLayout timeInLayout;
    private TextInputLayout typeInputLayout;
    private TextView specsTextView;
    private ScrollView scrollView;
    private LinearLayout itemUsedFields;
    private LinearLayout linearLayout;


    private Button saveButton;
    private MaterialButton addPatient;
    private MaterialButton removePatient;
    private MaterialButton submit_otherType;
    private MaterialButton submitOtherSite;
    private MaterialButton submit_otherPhysicalLoc;
    private MaterialButton removeSizeButton;
    private SwitchMaterial itemUsed;
    private RadioGroup useRadioGroup;
    private RadioButton radioButton;
    private ImageButton backButton;
    private Button rescanButton;
    private Button addSizeButton;

    private int patientidAdded = 0;
    private int emptySizeFieldCounter = 0;
    private int typeCounter;
    private int siteCounter;
    private int locCounter;
    private boolean chosenType;
    private boolean chosenLocation;
    private boolean chosenReusable;
    private boolean isAddSizeButtonClicked;
    private boolean chosenSite;
    private boolean dropDownSelected;
    private Button autoPopulateButton;
    private RadioButton multiUse;
    private List<TextInputEditText> allPatientIds;
    private List<TextInputEditText> allSizeOptions;
    private ArrayList<String> TYPES;
    private ArrayList<String> SITELOC;
    private ArrayList<String> PHYSICALLOC;


    // firebase key labels
    private final String NAME_KEY = "name";
    private final String TYPE_KEY = "equipment_type";
    private final String COMPANY_KEY = "company";
    private final String DI_KEY = "di";
    private final String SITE_KEY = "site_name";
    private final String SPECIALTY_KEY = "medical_specialty";
    private final String DESCRIPTION_KEY = "device_description";
    private final String USAGE_KEY = "usage";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View rootView = inflater.inflate(R.layout.fragment_itemdetail, container, false);
        final Calendar myCalendar = Calendar.getInstance();
        parent = getActivity();

        // TODO add "clear" option for some fields in xml
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
        dateIn = rootView.findViewById(R.id.detail_in_date);
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.US);
        dateIn.setText(dateFormat.format(new Date()));
        timeIn = rootView.findViewById(R.id.detail_in_time);
        expirationTextLayout = rootView.findViewById(R.id.expiration_date_string);
        dateInLayout = rootView.findViewById(R.id.in_date_layout);
        timeInLayout = rootView.findViewById(R.id.in_time_layout);
        itemUsed = rootView.findViewById(R.id.detail_used_switch);
        saveButton = rootView.findViewById(R.id.detail_save_button);
        backButton = rootView.findViewById(R.id.detail_back_button);
        rescanButton = rootView.findViewById(R.id.detail_rescan_button);
        addPatient = rootView.findViewById(R.id.button_addpatient);
        removePatient = rootView.findViewById(R.id.button_removepatient);
        autoPopulateButton = rootView.findViewById(R.id.detail_autopop_button);
        useRadioGroup = rootView.findViewById(R.id.RadioGroup_id);
        procedureUsed = rootView.findViewById(R.id.edittext_procedure_used);
        procedureDate = rootView.findViewById(R.id.edittext_procedure_date);
        amountUsed = rootView.findViewById(R.id.amountUsed_id);
        patient_idDefault = rootView.findViewById(R.id.patientID_id);
        chosenReusable = false;
        chosenType = false;
        chosenSite = false;
        chosenLocation= false;
        dropDownSelected = false;
        itemUsed.setChecked(false);
        saveButton.setEnabled(false);
        addSizeButton = rootView.findViewById(R.id.button_addsize);
        scrollView = rootView.findViewById(R.id.scrollView);
        itemUsedFields = rootView.findViewById(R.id.layout_itemused);
        itemUsedFields.setVisibility(View.GONE);
        multiUse = rootView.findViewById(R.id.radio_multiuse);
        isAddSizeButtonClicked = true;
        specsTextView = rootView.findViewById(R.id.detail_specs_textview);
        typeInputLayout = rootView.findViewById(R.id.typeInputLayout);
        siteLocationLayout = rootView.findViewById(R.id.siteLocationLayout);
        physLocationLayout = rootView.findViewById(R.id.physicalLocationLayout);
        allSizeOptions = new ArrayList<>();
        TYPES = new ArrayList<>();
        SITELOC = new ArrayList<>();
        PHYSICALLOC = new ArrayList<>();




        final TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) { }
            @Override
            public void afterTextChanged(Editable editable) {
                for(TextInputEditText editText : new TextInputEditText[] { udiEditText,nameEditText,
                        company, expiration,lotNumber,referenceNumber,numberAdded, deviceIdentifier,
                        dateIn,timeIn}){
                    if(editText.getText().toString().trim().isEmpty()){
                        saveButton.setEnabled(false);
                        return;
                    }if(dropDownSelected) {
                        saveButton.setEnabled(true);
                    }
                }
            }
        };

        udiEditText.addTextChangedListener(textWatcher);
        nameEditText.addTextChangedListener(textWatcher);
        company.addTextChangedListener(textWatcher);
        expiration.addTextChangedListener(textWatcher);
        lotNumber.addTextChangedListener(textWatcher);
        referenceNumber.addTextChangedListener(textWatcher);
        numberAdded.addTextChangedListener(textWatcher);
        deviceIdentifier.addTextChangedListener(textWatcher);
        dateIn.addTextChangedListener(textWatcher);
        timeIn.addTextChangedListener(textWatcher);


        final TextWatcher textWatcherDropDown = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                saveButton.setEnabled(false);
            }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) { }
            @Override
            public void afterTextChanged(Editable editable) {
                for(AutoCompleteTextView editText : new AutoCompleteTextView[]{ equipmentType,
                hospitalName,physicalLocation}){
                    if(editText.getText().toString().trim().isEmpty()){
                        System.out.println("text is:" + editText.getText().toString());
                        saveButton.setEnabled(false);
                        return;
                    }
                    dropDownSelected = true;
                    saveButton.setEnabled(true);
                }
            }
        };

        equipmentType.addTextChangedListener(textWatcherDropDown);
        hospitalName.addTextChangedListener(textWatcherDropDown);
        physicalLocation.addTextChangedListener(textWatcherDropDown);



        // Dropdown menu for Type field
        // real time type update
        typeRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w(TAG, "Listen failed.", e);
                    return;
                }
                if (documentSnapshot != null && documentSnapshot.exists()) {
                    Map<String, Object> typeObj = documentSnapshot.getData();
                    typeCounter = typeObj.size();
                    for(Object value : typeObj.values()) {
                        if(!TYPES.contains(value.toString())) {
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
                        rootView.getContext(),
                        R.layout.dropdown_menu_popup_item,
                        TYPES);
        equipmentType.setAdapter(adapterType);


        equipmentType.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String selected = (String) adapterView.getItemAtPosition(i);
                if(selected.isEmpty()){
                    saveButton.setEnabled(false);
                }else{
                    saveButton.setEnabled(true);
                }


                addTypeOptionField(adapterView,view,i,l);

            }
        });
        // real time site update
        siteRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w(TAG, "Listen failed.", e);
                    return;
                }
                if (documentSnapshot != null && documentSnapshot.exists()) {
                    Map<String, Object> typeObj = documentSnapshot.getData();
                    siteCounter = typeObj.size();
                    for(Object value : typeObj.values()) {
                        if(!SITELOC.contains(value.toString())) {
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
                        rootView.getContext(),
                        R.layout.dropdown_menu_popup_item,
                        SITELOC);
        hospitalName.setAdapter(adapterSite);
        hospitalName.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                addNewSite(adapterView,view,i,l);
            }
        });



        // Dropdown menu for Physical Location field
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
                    for(Object value : typeObj.values()) {
                        if(!PHYSICALLOC.contains(value.toString())) {
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
                        rootView.getContext(),
                        R.layout.dropdown_menu_popup_item,
                        PHYSICALLOC);
        physicalLocation.setAdapter(adapterLoc);
        physicalLocation.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                addNewLoc(adapterView,view,i,l);
            }
        });

        autoPopulateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                autoPopulate();
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
                Calendar mcurrentTime = Calendar.getInstance();
                int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
                int minute = mcurrentTime.get(Calendar.MINUTE);
                TimePickerDialog mTimePicker;
                mTimePicker = new TimePickerDialog(rootView.getContext(), new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                        timeIn.setText(String.format(Locale.US,"%d:%d:00", selectedHour, selectedMinute));
                    }
                }, hour, minute, true);
                mTimePicker.setTitle("Select Time");
                mTimePicker.show();
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
                parent.onBackPressed();
            }
        });

        //going back to inventory view
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (parent != null)
                    parent.onBackPressed();
            }
        });

        addPatient.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addPatientIdField(view);
            }
        });

        // when clicked remove one added field "Patient ID"
        removePatient.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (patientidAdded > 0) {
                    itemUsedFields.removeViewAt(itemUsedFields.indexOfChild(addPatient) - 1);
                    patientidAdded--;
                }
            }
        });

        // Checks which buttons is chosen
        multiUse.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                // if reusable button is chosen, gives an user option to add multiple patient IDs
                if (b) {
                    addPatient.setVisibility(View.VISIBLE);
                    removePatient.setVisibility(View.VISIBLE);
                    allPatientIds = new ArrayList<TextInputEditText>();
                    chosenReusable = true;

                    // if users changes from reusable to single us removes all unnecessary fields.
                } else {
                    addPatient.setVisibility(View.GONE);
                    removePatient.setVisibility(View.GONE);
                    chosenReusable = false;
                    while(patientidAdded > 0){
                        itemUsedFields.removeViewAt(itemUsedFields.indexOfChild(addPatient) - 1);
                        patientidAdded--;
                    }

                }
            }
        });

        TextInputLayout procedureDateTimeLayout = rootView.findViewById(R.id.textinputlayout_proceduredatetime);
        procedureDate = rootView.findViewById(R.id.edittext_procedure_date);
        final DatePickerDialog.OnDateSetListener date_proc = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
                myCalendar.set(Calendar.YEAR, i);
                myCalendar.set(Calendar.MONTH, i1);
                myCalendar.set(Calendar.DAY_OF_MONTH, i2);
                String myFormat = "yyyy/MM/dd";
                SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
                procedureDate.setText(String.format("%s %s", sdf.format(myCalendar.getTime()),
                        TimeZone.getDefault().getDisplayName(false,TimeZone.SHORT)));
            }
        };
        procedureDateTimeLayout.setEndIconOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new DatePickerDialog(view.getContext(), date_proc, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        // TODO make sure hidden fields at the time of saving are not saved
        itemUsed.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    saveButton.setEnabled(false);
                    itemUsedFields.setVisibility(View.VISIBLE);
                    TextWatcher textWatcher = new TextWatcher() {
                        @Override
                        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }
                        @Override
                        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) { }
                        @Override
                        public void afterTextChanged(Editable editable) {
                            for (TextInputEditText editText : new TextInputEditText[]{procedureUsed,
                                    procedureDate, amountUsed, patient_idDefault}) {
                                if (editText.getText().toString().trim().isEmpty()) {
                                    saveButton.setEnabled(false);
                                    return;
                                }
                                saveButton.setEnabled(true);
                            }
                        }
                    };
                    procedureUsed.addTextChangedListener(textWatcher);
                    procedureDate.addTextChangedListener(textWatcher);
                    amountUsed.addTextChangedListener(textWatcher);
                    patient_idDefault.addTextChangedListener(textWatcher);

                } else {
                    // enable saveButton
                    saveButton.setEnabled(true);

                    // drop textwatchers for some fields
                    itemUsedFields.setVisibility(View.GONE);
                    procedureUsed.removeTextChangedListener(textWatcher);
                    procedureDate.removeTextChangedListener(textWatcher);
                    amountUsed.removeTextChangedListener(textWatcher);
                    patient_idDefault.removeTextChangedListener(textWatcher);
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
                expiration.setText(String.format("%s %s", sdf.format(myCalendar.getTime()),
                        TimeZone.getDefault().getDisplayName(false, TimeZone.SHORT)));
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
                dateIn.setText(String.format("%s %s", sdf.format(myCalendar.getTime()),
                        TimeZone.getDefault().getDisplayName(false, TimeZone.SHORT)));
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
        //TODO determine hospital based on user
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //   b.setEnabled(!s1.trim().isEmpty() && !s2.trim().isEmpty());
                saveData(rootView, "networks", "network1","sites",
                        "n1_hospital3","n1_h3_departments",
                        "department1","n1_h1_d1 productids");
            }
        });

        String barcode = getArguments().getString("barcode");
        udiEditText.setText(barcode);
        autoPopulate();
        return rootView;
    }

    // TODO update to uniform style
    // when clicked add one more additional field for Patient ID
    private void addPatientIdField(View view){
        patientidAdded++;
        TextInputLayout patient_id_layout =(TextInputLayout) this.getLayoutInflater().inflate(R.layout.activity_itemdetail_materialcomponent,
                null,false);
        patient_id_layout.setHint("patient ID");
        patient_id_layout.setPadding(0,10,0,0);
        patient_id_layout.setBoxBackgroundMode(TextInputLayout.BOX_BACKGROUND_OUTLINE);

        patient_id = new TextInputEditText(patient_id_layout.getContext());
        allPatientIds.add(patient_id);
        patient_id.setLayoutParams(new LinearLayout.LayoutParams(udiEditText.getWidth(), ViewGroup.LayoutParams.WRAP_CONTENT));
        patient_id_layout.addView(patient_id);
        itemUsedFields.addView(patient_id_layout,  itemUsedFields.indexOfChild(addPatient));
    }

    // adds new row of size text views if users clicks on a button
    int rowIndex = 1;
    private void addEmptySizeOption(View view) {

        Log.d(TAG, "Adding empty size option!");
        emptySizeFieldCounter++;
        GridLayout gridLayoutSize = new GridLayout(view.getContext());
        GridLayout.LayoutParams paramSizeKey = new GridLayout.LayoutParams();
        paramSizeKey.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        paramSizeKey.width = WRAP_CONTENT;
        paramSizeKey.rowSpec = GridLayout.spec(rowIndex);
        paramSizeKey.columnSpec = GridLayout.spec(0);
        paramSizeKey.setMargins(0,0,0,20);


        GridLayout.LayoutParams paramSizeValue = new GridLayout.LayoutParams();
        paramSizeValue.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        paramSizeValue.width = WRAP_CONTENT;
        paramSizeValue.rowSpec = GridLayout.spec(rowIndex);
        paramSizeValue.columnSpec = GridLayout.spec(1);
        paramSizeValue.setMargins(10,0,0,20);


        TextInputLayout sizeKeyLayout = (TextInputLayout) View.inflate(view.getContext(),
                R.layout.activity_itemdetail_materialcomponent, null);
        sizeKeyLayout.setLayoutParams(paramSizeKey);
        sizeKeyLayout.setHint("Key");
        TextInputEditText sizeKey = new TextInputEditText(sizeKeyLayout.getContext());

        TextInputLayout sizeValueLayout = (TextInputLayout)View.inflate(view.getContext(),
                R.layout.activity_itemdetail_materialcomponent, null);
        sizeValueLayout.setLayoutParams(paramSizeValue);
        sizeValueLayout.setHint("Value");
        TextInputEditText sizeValue = new TextInputEditText(sizeKeyLayout.getContext());


        sizeKey.setLayoutParams(new LinearLayout.LayoutParams(300, WRAP_CONTENT));
        sizeKeyLayout.addView(sizeKey);
        sizeValue.setLayoutParams(new LinearLayout.LayoutParams(550, WRAP_CONTENT));
        sizeValueLayout.addView(sizeValue);
        gridLayoutSize.addView(sizeKeyLayout);
        gridLayoutSize.addView(sizeValueLayout);

        allSizeOptions.add(sizeKey);
        allSizeOptions.add(sizeValue);
        System.out.println(allSizeOptions.size());

        linearLayout.addView(gridLayoutSize, 1 + linearLayout.indexOfChild(specsTextView));
        rowIndex++;
        if(isAddSizeButtonClicked) {
            removeSizeButton = new MaterialButton(view.getContext(),
                    null, R.attr.materialButtonOutlinedStyle);
            removeSizeButton.setText("Remove size field");
            removeSizeButton.setLayoutParams(new LinearLayout.LayoutParams(udiEditText.getWidth(),
                    WRAP_CONTENT));
            linearLayout.addView(removeSizeButton, 1 + linearLayout.indexOfChild(addSizeButton));
        }

        removeSizeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                removeEmptySizeOption(view);

            }
        });
        isAddSizeButtonClicked = false;

    }
    //removes one row of size text entry
    private void removeEmptySizeOption(View view){
        if(emptySizeFieldCounter > 0){
            linearLayout.removeViewAt(linearLayout.indexOfChild(specsTextView) + 1);
            emptySizeFieldCounter--;

        }
        if(emptySizeFieldCounter == 0){
            linearLayout.removeViewAt(linearLayout.indexOfChild(removeSizeButton));
            isAddSizeButtonClicked = true;
        }

        allSizeOptions.remove(allSizeOptions.size() - 1);
        allSizeOptions.remove(allSizeOptions.size() - 1);
        System.out.println(allSizeOptions.size());

    }

    // adds new text field if users choose "other" for type
    private void addTypeOptionField (final AdapterView < ? > adapterView, View view,int i, long l){
        String selected = (String) adapterView.getItemAtPosition(i);
        TextInputLayout other_type_layout = null;
        if (selected.equals("Other")) {
            chosenType = true;
            other_type_layout = (TextInputLayout) this.getLayoutInflater().inflate(R.layout.activity_itemdetail_materialcomponent,
                    null, false);
            other_type_layout.setHint("Enter type");
            other_type_layout.setId(View.generateViewId());
            other_type_layout.setBoxBackgroundMode(TextInputLayout.BOX_BACKGROUND_OUTLINE);
            otherType_text = new TextInputEditText(other_type_layout.getContext());
            otherType_text.setLayoutParams(new LinearLayout.LayoutParams(udiEditText.getWidth(), WRAP_CONTENT));
            other_type_layout.addView(otherType_text);
            linearLayout.addView(other_type_layout, 1 + linearLayout.indexOfChild(typeInputLayout));

            submit_otherType = new MaterialButton(view.getContext(),
                    null, R.attr.materialButtonOutlinedStyle);
            submit_otherType.setText(R.string.otherType_lbl);
            submit_otherType.setLayoutParams(new LinearLayout.LayoutParams(udiEditText.getWidth(),
                    WRAP_CONTENT));
            linearLayout.addView(submit_otherType, 2 + linearLayout.indexOfChild(typeInputLayout));

            submit_otherType.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Toast.makeText(view.getContext(), otherType_text.getText().toString(), Toast.LENGTH_SHORT).show();
                    Map<String, Object> newType = new HashMap<>();
                    newType.put("type_" + typeCounter, otherType_text.getText().toString());
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
            });
        } else if (chosenType && (!(selected.equals("Other")))) {
            chosenType = false;
            linearLayout.removeViewAt(1 + linearLayout.indexOfChild(typeInputLayout));
            linearLayout.removeViewAt(1 + linearLayout.indexOfChild(typeInputLayout));
        }
    }

    private void addNewSite(final AdapterView < ? > adapterView, View view, int i, long l){
        String selected = (String) adapterView.getItemAtPosition(i);
        TextInputLayout other_site_layout = null;
        if (selected.equals("Other")) {
            chosenSite = true;
            other_site_layout = (TextInputLayout) this.getLayoutInflater().inflate(R.layout.activity_itemdetail_materialcomponent,
                    null, false);
            other_site_layout.setHint("Enter site");
            other_site_layout.setId(View.generateViewId());
            other_site_layout.setBoxBackgroundMode(TextInputLayout.BOX_BACKGROUND_OUTLINE);
            otherSite_text = new TextInputEditText(other_site_layout.getContext());
            otherSite_text.setLayoutParams(new LinearLayout.LayoutParams(udiEditText.getWidth(), WRAP_CONTENT));
            other_site_layout.addView(otherSite_text);
            linearLayout.addView(other_site_layout, 1 + linearLayout.indexOfChild(siteLocationLayout));

            submitOtherSite = new MaterialButton(view.getContext(),
                    null, R.attr.materialButtonOutlinedStyle);
            submitOtherSite.setText(R.string.submitSite_lbl);
            submitOtherSite.setLayoutParams(new LinearLayout.LayoutParams(udiEditText.getWidth(),
                    WRAP_CONTENT));
            linearLayout.addView(submitOtherSite, 2 + linearLayout.indexOfChild(siteLocationLayout));

            submitOtherSite.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Toast.makeText(view.getContext(), otherSite_text.getText().toString(), Toast.LENGTH_SHORT).show();
                    Map<String, Object> newType = new HashMap<>();
                    newType.put("site_" + siteCounter, otherSite_text.getText().toString());
                    siteRef.update(newType)
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
            });
        } else if (chosenSite && (!(selected.equals("Other")))) {
            chosenSite = false;
            linearLayout.removeViewAt(1 + linearLayout.indexOfChild(siteLocationLayout));
            linearLayout.removeViewAt(1 + linearLayout.indexOfChild(siteLocationLayout));
        }
    }

    private void addNewLoc(final AdapterView<?> adapterView, View view, int i, long l){
        String selectedLoc = (String) adapterView.getItemAtPosition(i);
        final TextInputLayout other_physicaloc_layout;
        if (selectedLoc.equals("Other")) {
            chosenLocation = true;
            other_physicaloc_layout = (TextInputLayout) this.getLayoutInflater().inflate(R.layout.activity_itemdetail_materialcomponent,
                    null, false);
            other_physicaloc_layout.setHint("Enter physical location");
            other_physicaloc_layout.setId(View.generateViewId());
            other_physicaloc_layout.setBoxBackgroundMode(TextInputLayout.BOX_BACKGROUND_OUTLINE);
            otherPhysicalLoc_text = new TextInputEditText(other_physicaloc_layout.getContext());
            otherPhysicalLoc_text.setLayoutParams(new LinearLayout.LayoutParams(udiEditText.getWidth(), WRAP_CONTENT));
            other_physicaloc_layout.addView(otherPhysicalLoc_text);
            linearLayout.addView(other_physicaloc_layout, 1 + linearLayout.indexOfChild(physLocationLayout));

            submit_otherPhysicalLoc = new MaterialButton(view.getContext(),
                    null, R.attr.materialButtonOutlinedStyle);
            submit_otherPhysicalLoc.setText(R.string.submitLocation_lbl);
            submit_otherPhysicalLoc.setLayoutParams(new LinearLayout.LayoutParams(udiEditText.getWidth(),
                    WRAP_CONTENT));
            linearLayout.addView(submit_otherPhysicalLoc, 2 + linearLayout.indexOfChild(physLocationLayout));

            submit_otherPhysicalLoc.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Toast.makeText(view.getContext(), otherPhysicalLoc_text.getText().toString(), Toast.LENGTH_SHORT).show();
                    Map<String, Object> newType = new HashMap<>();
                    newType.put("loc_" + locCounter, otherPhysicalLoc_text.getText().toString());
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
            });
        }else if (chosenLocation && (!(selectedLoc.equals("Other")))) {
            chosenLocation = false;
            linearLayout.removeViewAt(1 + linearLayout.indexOfChild(physLocationLayout));
            linearLayout.removeViewAt(1 + linearLayout.indexOfChild(physLocationLayout));
        }
    }


    // method for saving data to firebase cloud firestore
    public void saveData(View view, String NETWORKS, String NETWORK, String SITES, String SITE,
                         String DEPARTMENTS, String DEPARTMENT, String PRODUCTDIS) {


        Log.d(TAG, "SAVING");
        String barcode_str = udiEditText.getText().toString();
        String name_str = nameEditText.getText().toString();
        String type_str = equipmentType.getText().toString();
        String company_str = company.getText().toString();
        String medical_speciality_str = medicalSpeciality.getText().toString();
        String di_str = deviceIdentifier.getText().toString();
        String description_str = deviceDescription.getText().toString();
        String lotNumber_str = lotNumber.getText().toString();
        String referenceNumber_str = referenceNumber.getText().toString();
        String expiration_str = expiration.getText().toString();
        String number_added_str = numberAdded.getText().toString();
        int quantity_int;
        // TODO remove??
        if(itemUsed.isChecked()){
            //     quantity_int = Integer.parseInt(quantity.getText().toString()) - Integer.parseInt(numberUsed.getText().toString());
        } else {
            //        quantity_int = Integer.parseInt(quantity.getText().toString()) + Integer.parseInt(numberAdded.getText().toString());
        }
        String quantity_str = "2"; // temporarily
        String site_name_str = "";
        if(chosenSite){
            site_name_str = otherSite_text.getText().toString();
        }else{
            site_name_str = hospitalName.getText().toString();
        }
        String physical_location_str = "";
        if(chosenLocation){
            physical_location_str = otherPhysicalLoc_text.getText().toString().trim();
        }else{
            physical_location_str = physicalLocation.getText().toString().trim();
        }
        String currentDateTime_str = timeIn.getText().toString();
        String notes_str = notes.getText().toString();


        // getting radiobutton value
        boolean isUsed = itemUsed.isChecked();
        int radioButtonInt = useRadioGroup.getCheckedRadioButtonId();
        radioButton = view.findViewById(radioButtonInt);
        String singleOrMultiUse;
        try {
            singleOrMultiUse = radioButton.getText().toString();
        } catch (NullPointerException e){
            singleOrMultiUse = "";
        }


       // saving di-specific identifiers using HashMap
        Map<String, Object> diDoc = new HashMap<>();
        diDoc.put(NAME_KEY,name_str);
        diDoc.put(TYPE_KEY,type_str);
        diDoc.put(COMPANY_KEY,company_str);
        diDoc.put(DI_KEY,di_str);
        diDoc.put(SITE_KEY,site_name_str);
        diDoc.put(DESCRIPTION_KEY,description_str);
        diDoc.put(SPECIALTY_KEY,medical_speciality_str);
        diDoc.put(USAGE_KEY,singleOrMultiUse);
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
       udiDocument = new InventoryTemplate(barcode_str,isUsed, number_added_str,lotNumber_str,
                expiration_str, quantity_str,currentDateTime_str,physical_location_str,referenceNumber_str, notes_str);

        udiRef = db.collection(NETWORKS).document(NETWORK)
        .collection(SITES).document(SITE).collection(DEPARTMENTS)
        .document(DEPARTMENT).collection(PRODUCTDIS).document(di_str)
                .collection("UDIs").document(barcode_str);

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

        //if item is used
        if(itemUsed.isChecked()){

            String procedure_used_str = procedureUsed.getText().toString();
            String procedure_date_str = procedureDate.getText().toString();
            String amount_used_str = amountUsed.getText().toString();
            String patient_id_str = patient_idDefault.getText().toString();



            Map<String, Object> ifUsedFields = new HashMap<>();
            ifUsedFields.put("procedure_used",procedure_used_str);
            ifUsedFields.put("procedure_date",procedure_date_str);
            ifUsedFields.put("amount_used",amount_used_str);
            ifUsedFields.put("patient_id",patient_id_str);

            udiRef.update(ifUsedFields)
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


        // HashMap for additional patient ids if chosen reusable
        if(chosenReusable) {
            Map<String, Object> patientIds = new HashMap<>();
            for (int i = 0; i < allPatientIds.size(); i++) {
                patientIds.put("patient_id_" + (i + 2), allPatientIds.get(i).getText().toString());
            }
            udiRef.update(patientIds)
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

        if(allSizeOptions.size() > 0){
            int i = 0;
            Map<String, Object> sizeOptions = new HashMap<>();
            while(i < allSizeOptions.size()){
                sizeOptions.put(allSizeOptions.get(i++).getText().toString().trim(),
                        allSizeOptions.get(i++).getText().toString().trim());
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



        public void autoPopulate() {

        String udi = udiEditText.getText().toString();
        Log.d(TAG, udi);

        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(parent);
        String url = "https://accessgudid.nlm.nih.gov/api/v2/devices/lookup.json?udi=";

        url = url + udi;

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        JSONObject responseJson = null;
                        try {
                            responseJson = new JSONObject(response);

                            Log.d(TAG, "Response: " + response);

                            JSONObject deviceInfo = responseJson.getJSONObject("gudid").getJSONObject("device");
                            JSONObject udi = responseJson.getJSONObject("udi");
                            JSONArray productCodes = responseJson.getJSONArray("productCodes");
                            String medicalSpecialties = "";
                            for (int i = 0; i < productCodes.length(); i++){
                                medicalSpecialties += productCodes.getJSONObject(i).getString("medicalSpecialty");
                                medicalSpecialties += "; ";
//                                Log.d(TAG, "MEDICAL SPECIALTY: " + medicalSpecialties);
                            }
                            medicalSpecialties = medicalSpecialties.substring(0, medicalSpecialties.length() - 2);

                            lotNumber.setText(udi.getString("lotNumber"));
                            company.setText(deviceInfo.getString("companyName"));
                            expiration.setText(udi.getString("expirationDate"));

                            deviceIdentifier.setText(udi.getString("di"));
                            nameEditText.setText(deviceInfo.getJSONObject("gmdnTerms").getJSONArray("gmdn").getJSONObject(0).getString("gmdnPTName"));
                            deviceDescription.setText(deviceInfo.getString("deviceDescription"));
                            referenceNumber.setText(deviceInfo.getString("catalogNumber"));
                            medicalSpeciality.setText(medicalSpecialties);

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

}