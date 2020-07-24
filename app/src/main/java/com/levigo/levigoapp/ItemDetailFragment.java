package com.levigo.levigoapp;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.SpannableString;
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
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import android.widget.Toolbar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
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
import com.google.android.gms.tasks.Tasks;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.zxing.integration.android.IntentIntegrator;
import com.journeyapps.barcodescanner.CaptureActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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

    // Firebase database
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    DocumentReference typeRef = db.collection("networks").document("types");
    DocumentReference siteRef = db.collection("networks").document("network1")
            .collection("sites").document("site_options");
    DocumentReference physLocRef = db.collection("networks").document("network1")
            .collection("sites").document("n1_hospital3")
            .collection("physical_locations").document("locations");
    DocumentReference siteDocRef = db.collection("networks").document("network1")
            .collection("sites").document("n1_hospital3");
    CollectionReference accessionNumberRef = db.collection("networks")
            .document("network1")
            .collection("sites").document("n1_hospital3")
            .collection("accession_numbers");


    InventoryTemplate udiDocument;

    private static final String TAG = ItemDetailFragment.class.getSimpleName();
    private Activity parent;
    private Calendar myCalendar;
    private NetworkActivity Sites = new NetworkActivity();

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
    private TextInputLayout siteLocationLayout;
    private TextInputLayout physLocationLayout;
    private TextInputLayout diLayout;
    private TextInputLayout numberAddedLayout;
    private TextInputEditText medicalSpeciality;
    private TextInputLayout typeInputLayout;
    private TextView specsTextView;
    private LinearLayout itemUsedFields;
    private LinearLayout linearLayout;
    private TextInputLayout procedureDateLayout;
    private TextInputLayout procedureNameLayout;
    private TextInputLayout accessionNumberLayout;
    private TextInputEditText procedureNameEditText;
    private TextInputEditText accessionNumberEditText;
    private TextView usageHeader;

    private Button saveButton;
    private MaterialButton addProcedure;
    private MaterialButton removeProcedure;
    private MaterialButton removeSizeButton;
    private SwitchMaterial itemUsed;
    private RadioGroup useRadioGroup;
    private RadioButton singleUseButton;
    private RadioButton multiUse;
    private Button addSizeButton;
    private MaterialToolbar topToolBar;

    private String itemQuantity;
    private String diQuantity;
    private int numberUsedEditTextId;
    private int procedureDateEditTextId;
    private int procedureFieldAdded;
    private int emptySizeFieldCounter = 0;
    private int typeCounter;
    private int siteCounter;
    private int locCounter;
    private int procedureCount;
    private int procedureListCounter;
    private boolean isValid;
    private boolean chosenType;
    private boolean chosenLocation;
    private boolean chosenReusable;
    private boolean isAddSizeButtonClicked;
    private boolean chosenSite;
    private boolean checkEditTexts;
    private boolean checkAutocompleteTexts;
    private boolean checkItemUsed;
    private boolean checkSingleUseButton;
    private boolean checkMultiUseButton;
    private boolean isCountRead;
    private boolean checkProcedureInfo;
    private Button autoPopulateButton;
    private List<TextInputEditText> allSizeOptions;
    private ArrayList<String> TYPES;
    private ArrayList<String> SITELOC;
    private ArrayList<String> PHYSICALLOC;
    private List<Map<String,Object>> procedureMapList;
    private List<String> procedureDocuments;
    private List<List<String>> procedureDoc;
    private TextWatcher textWatcher;
    private List<TextInputEditText> numberUsedList;

    private ConstraintLayout siteConstrainLayout;
    private ConstraintLayout physicalLocationConstrainLayout;
    private ConstraintLayout typeConstrainLayout;
    private ConstraintLayout numberAddedConstrainLayout;


    // firebase key labels to avoid hard-coded paths
    private final String NAME_KEY = "name";
    private final String TYPE_KEY = "equipment_type";
    private final String COMPANY_KEY = "company";
    private final String DI_KEY = "di";
    private final String SITE_KEY = "site_name";
    private final String SPECIALTY_KEY = "medical_specialty";
    private final String DESCRIPTION_KEY = "device_description";
    private final String USAGE_KEY = "usage";
    private final String PROCEDURE_KEY = "procedure_used";
    private final String PROCEDUREDATE_KEY = "procedure_date";
    private final String AMOUNTUSED_KEY = "amount_used";
    private final String ACCESSION_KEY = "accession_number";
    private final String PHYSICALLOC_KEY = "physical_location";
    private final String TIME_KEY = "current_time";
    private final String DATE_KEY = "current_date";
    private final String EXPIRATION_KEY = "expiration";
    private final String ISUSED_KEY = "is_used";
    private final String LOT_KEY = "lot_number";
    private final String NOTE_KEY = "notes";
    private final String QUANTITY_KEY = "quantity";
    private final String SINGLEORMULTI_KEY = "single_multi";
    private final String REFERENCE_KEY = "reference_number";
    private final String UDI_KEY = "udi";
    private String accessionNumber;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View rootView = inflater.inflate(R.layout.fragment_itemdetail, container, false);
        myCalendar = Calendar.getInstance();
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
        autoPopulateButton = rootView.findViewById(R.id.detail_autopop_button);
        useRadioGroup = rootView.findViewById(R.id.RadioGroup_id);
        diLayout = rootView.findViewById(R.id.TextInputLayout_di);
        singleUseButton = rootView.findViewById(R.id.RadioButton_single);
        multiUse = rootView.findViewById(R.id.radio_multiuse);
        numberAddedLayout = rootView.findViewById(R.id.numberAddedLayout);
        topToolBar = rootView.findViewById(R.id.topAppBar);

        siteConstrainLayout = rootView.findViewById(R.id.site_linearlayout);
        physicalLocationConstrainLayout= rootView.findViewById(R.id.physicalLocationLinearLayout);
        typeConstrainLayout= rootView.findViewById(R.id.typeLinearLayout);
        numberAddedConstrainLayout = rootView.findViewById(R.id.numberAddedLinearLayout);
        chosenReusable = false;
        chosenType = false;
        chosenSite = false;
        chosenLocation = false;
        checkAutocompleteTexts = false;
        checkEditTexts = false;
        checkItemUsed = false;
        checkSingleUseButton = false;
        checkMultiUseButton = false;
        checkProcedureInfo = false;
        isCountRead = false;
        isValid = false;
        itemUsed.setChecked(false);
        saveButton.setEnabled(false);
        addSizeButton = rootView.findViewById(R.id.button_addsize);
        itemUsedFields = rootView.findViewById(R.id.layout_itemused);
        itemUsedFields.setVisibility(View.GONE);
        isAddSizeButtonClicked = true;
        specsTextView = rootView.findViewById(R.id.detail_specs_textview);
        typeInputLayout = rootView.findViewById(R.id.typeInputLayout);
        siteLocationLayout = rootView.findViewById(R.id.siteLocationLayout);
        physLocationLayout = rootView.findViewById(R.id.physicalLocationLayout);
        usageHeader = rootView.findViewById(R.id.detail_usage_textview);
        allSizeOptions = new ArrayList<>();
        TYPES = new ArrayList<>();
        SITELOC = new ArrayList<>();
        PHYSICALLOC = new ArrayList<>();
        procedureMapList = new ArrayList<>();
        procedureDoc = new ArrayList<>();
        numberUsedList = new ArrayList<>();



        // NumberPicker Dialog for NumberAdded field
        numberAdded.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showNumberPicker(rootView,numberAdded);
            }
        });


        // incrementing number by 1 when clicked on the end icon
        numberAddedLayout.setEndIconOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                incrementNumberAdded(rootView);
            }
        });


        // icon listener to search di in database to autopopulate di-specific fields
        diLayout.setEndIconOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                autoPopulateFromDatabase(view, deviceIdentifier.getText().toString().trim());
            }
        });



        //set TextWatcher for required fields
        setTextWatcherRequired();


        //get realtime update for Equipment Type field from database
        // Dropdown menu for Type field
        // real time type update
        updateEquipmentType(rootView);

        //get realtime update for Site field from database
        // real time site update
        updateSite(rootView);

        //get realtime update for Physical Location field from database
        // Dropdown menu for Physical Location field
        updatePhysicalLocation(rootView);


        autoPopulateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                autoPopulate(siteDocRef,rootView);
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
                removeProcedure.setEnabled(true);
                saveButton.setEnabled(false);
                addProcedureField(view);
            }
        });

        // when clicked remove one added field "Patient ID"
        removeProcedure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (procedureFieldAdded - procedureListCounter > 0) {
                    removeAccessionNumber(view,itemUsedFields);
                    itemUsedFields.removeViewAt(itemUsedFields.indexOfChild(addProcedure) - 1);
                    numberUsedList.remove(numberUsedList.size() - 1);
                    --procedureFieldAdded;
                }if(procedureFieldAdded - procedureListCounter == 0){
                    removeProcedure.setEnabled(false);
                }
            }
        });

        itemUsed.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    //temporary start
                    List<List<String>> test = new ArrayList<>();
                 //   addProcedureInfoFields(test, rootView);
                    //temporary end

                    checkItemUsed = true;
                    saveButton.setEnabled(false);
                    itemUsedFields.setVisibility(View.VISIBLE);
                    numberAdded.setText("0");
                    numberAdded.removeTextChangedListener(textWatcher);
                    numberAddedConstrainLayout.setVisibility(View.GONE);
                    removeProcedure.setEnabled(false);

                } else {
                    // enable saveButton
                    saveButton.setEnabled(true);
                    checkItemUsed = false;
                    numberAddedConstrainLayout.setVisibility(View.VISIBLE);
                    itemUsedFields.setVisibility(View.GONE);
                    while (procedureFieldAdded  - procedureListCounter > 0) {
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
        //TODO determine hospital based on user
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //hardcoded
                saveData(rootView, "networks", "network1", "sites",
                        "n1_hospital3", "n1_h3_departments",
                        "department1", "n1_h1_d1 productids");
            }
        });

        assert getArguments() != null;
        String barcode = getArguments().getString("barcode");
        udiEditText.setText(barcode);
        return rootView;
    }

    private void timeInLayoutPicker(View view){
        Calendar mcurrentTime = Calendar.getInstance();
        int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
        int minute = mcurrentTime.get(Calendar.MINUTE);
        TimePickerDialog mTimePicker;
        mTimePicker = new TimePickerDialog(view.getContext(), new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                timeIn.setText(String.format(Locale.US, "%d:%d:00", selectedHour, selectedMinute));
            }
        }, hour, minute, true);
        mTimePicker.setTitle("Select Time");
        mTimePicker.show();
    }

    private void updatePhysicalLocation(View view){
        physLocRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w(TAG, "Listen failed.", e);
                    return;
                }
                if (documentSnapshot != null && documentSnapshot.exists()) {
                    Map<String, Object> typeObj = documentSnapshot.getData();
                    assert typeObj != null;
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
        physicalLocation.setAdapter(adapterLoc);
        physicalLocation.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                addNewLoc(adapterView, view, i, l);
            }
        });

    }


    private void updateSite(View view){
        siteRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w(TAG, "Listen failed.", e);
                    return;
                }
                if (documentSnapshot != null && documentSnapshot.exists()) {
                    Map<String, Object> typeObj = documentSnapshot.getData();
                    assert typeObj != null;
                    siteCounter = typeObj.size();
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
        hospitalName.setAdapter(adapterSite);
        hospitalName.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                addNewSite(adapterView, view, i, l);
            }
        });

    }

    private void updateEquipmentType(View view){
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
        equipmentType.setAdapter(adapterType);


        equipmentType.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                addTypeOptionField(adapterView, view, i, l);

            }
        });
    }
    private void setTextWatcherRequired(){
        textWatcher = new TextWatcher() {
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
                    if(!(editText.getText().toString().trim().isEmpty())){
                        checkAutocompleteTexts = true;
                        continue;
                    }
                    if (editText.getText().toString().trim().isEmpty()) {
                        checkAutocompleteTexts = false;
                        break;
                    }
                }

                if((checkAutocompleteTexts && checkEditTexts) && (checkSingleUseButton || checkMultiUseButton)){
                    saveButton.setEnabled(true);
                }

                for (TextInputEditText editText : new TextInputEditText[]{udiEditText, nameEditText,
                        company, expiration, lotNumber, referenceNumber, numberAdded, deviceIdentifier,
                        dateIn, timeIn}) {
                    if(!(Objects.requireNonNull(editText.getText()).toString().trim().isEmpty())){
                        checkEditTexts = true;
                        continue;
                    }
                    if (Objects.requireNonNull(editText.getText()).toString().trim().isEmpty()) { ;
                        checkEditTexts = false;
                        break;
                    }
                }

                if((checkAutocompleteTexts && checkEditTexts) && (checkSingleUseButton || checkMultiUseButton)){
                    saveButton.setEnabled(true);
                }


                singleUseButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                        checkSingleUseButton = true;
                        if((b && checkAutocompleteTexts) && checkEditTexts){
                            saveButton.setEnabled(true);
                        }
                    }
                });

                multiUse.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                        checkMultiUseButton = true;
                        if((b && checkAutocompleteTexts) && checkEditTexts){

                            saveButton.setEnabled(true);
                        }
                    }
                });
            }
        };

        if((checkAutocompleteTexts && checkEditTexts) && (checkSingleUseButton || checkMultiUseButton)){
            saveButton.setEnabled(true);
        }


        equipmentType.addTextChangedListener(textWatcher);
        hospitalName.addTextChangedListener(textWatcher);
        physicalLocation.addTextChangedListener(textWatcher);
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
    }




    private void setIconsAndDialogs(View view, final TextInputEditText procedureDateEditText){
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

    private void incrementNumberUsed(View view,TextInputEditText numberUsed){
        int newNumber = 0;
        try {
            newNumber = Integer.parseInt(Objects.requireNonNull(numberUsed.getText()).toString());
        }catch (NumberFormatException e){
            newNumber = 0;
        }finally {
            numberUsed.setText(String.valueOf(++newNumber));
        }
    }

    private void incrementNumberAdded(View view){
        int newNumber = 0;
        try {
            newNumber = Integer.parseInt(Objects.requireNonNull(numberAdded.getText()).toString());
        }catch (NumberFormatException e){
            newNumber = 0;
        }finally {
            numberAdded.setText(String.valueOf(++newNumber));
        }
    }


    private void showNumberPicker(View view, final TextInputEditText editTextAdded){

        final Dialog d = new Dialog(view.getContext());
        d.setTitle("NumberPicker");
        d.setContentView(R.layout.dialog);
        Button b1 = (Button) d.findViewById(R.id.button1);
        Button b2 = (Button) d.findViewById(R.id.button2);
        final NumberPicker np = (NumberPicker) d.findViewById(R.id.numberPicker1);
        np.setMaxValue(1000); // max value 100
        np.setMinValue(0);   // min value 0

        np.setWrapSelectorWheel(true);
        b1.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                editTextAdded.setText(String.valueOf(np.getValue())); //set the value to textview
                d.dismiss();
            }
        });
        b2.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                d.dismiss(); // dismiss the dialog
            }
        });
        d.show();

    }

    // when clicked adds one more additional field for Patient ID
    private void addProcedureField(View view) {
        saveButton.setEnabled(false);
        final Map<String, Object> procedureInfoMap = new HashMap<>();
        procedureFieldAdded++;

        LinearLayout procedureInfoLayout = new LinearLayout(view.getContext());
        procedureInfoLayout.setTag("procedure_info");
        procedureInfoLayout.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        procedureInfoLayout.setOrientation(LinearLayout.VERTICAL);
        procedureInfoLayout.setGravity(Gravity.END);

        final TextView procedureNumber = new TextView(view.getContext());
        procedureNumber.setPadding(0,10,0,0);
        procedureNumber.setTextSize(18);
        procedureNumber.setTypeface(procedureNumber.getTypeface(), Typeface.BOLD);
        SpannableString content = new SpannableString(String.format(Locale.US,"Procedure #%d Description",
                procedureFieldAdded));
        content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
        procedureNumber.setText(content);

        procedureDateLayout = (TextInputLayout) View.inflate(view.getContext(),
                R.layout.activity_itemdetail_materialcomponent, null);
        procedureDateLayout.setHint("Enter Procedure Date");
        procedureDateLayout.setEndIconTintList(ColorStateList.valueOf(getResources().
                getColor(R.color.colorPrimary, Objects.requireNonNull(getActivity()).getTheme())));
        procedureDateLayout.setEndIconMode(TextInputLayout.END_ICON_CUSTOM);
        procedureDateLayout.setEndIconDrawable(R.drawable.calendar);
        procedureDateLayout.setPadding(0, 10, 0, 0);
        final TextInputEditText procedureDateEditText = new TextInputEditText(procedureDateLayout.getContext());
        setIconsAndDialogs(view,procedureDateEditText);
        procedureDateEditTextId = procedureDateEditText.getId();
        procedureDateEditText.setFocusable(false);
        procedureDateEditText.setClickable(true);
        procedureDateEditText.setLayoutParams(new LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT));


        TextInputLayout procedureTimeLayout = (TextInputLayout) View.inflate(view.getContext(),
                R.layout.activity_itemdetail_materialcomponent, null);
        procedureTimeLayout.setHint("Enter Time");
        procedureTimeLayout.setEndIconMode(TextInputLayout.END_ICON_CUSTOM);
        procedureTimeLayout.setEndIconDrawable(R.drawable.clock);
        procedureTimeLayout.setEndIconTintList(ColorStateList.valueOf(getResources().
                getColor(R.color.colorPrimary, Objects.requireNonNull(getActivity()).getTheme())));
        procedureTimeLayout.setPadding(0, 10, 0, 0);
        final TextInputEditText procedureTimeEditText = new TextInputEditText(procedureTimeLayout.getContext());
        procedureTimeEditText.setLayoutParams(new LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT));
        procedureTimeLayout.setEndIconOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar mcurrentTime = Calendar.getInstance();
                int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
                int minute = mcurrentTime.get(Calendar.MINUTE);
                TimePickerDialog mTimePicker;
                mTimePicker = new TimePickerDialog(view.getContext(), new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                        procedureTimeEditText.setText(String.format(Locale.US, "%d:%d:00", selectedHour, selectedMinute));
                    }
                }, hour, minute, true);
                mTimePicker.setTitle("Select Time");
                mTimePicker.show();

            }
        });
        procedureTimeLayout.addView(procedureTimeEditText);



        procedureNameLayout = (TextInputLayout) View.inflate(view.getContext(),
                R.layout.activity_itemdetail_materialcomponent, null);
        procedureNameLayout.setHint("Enter Procedure");
        procedureNameLayout.setPadding(0, 10, 0, 0);
        procedureNameEditText = new TextInputEditText(procedureNameLayout.getContext());
        procedureNameEditText.setLayoutParams(new LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT));



        accessionNumberLayout = (TextInputLayout) View.inflate(view.getContext(),
                R.layout.activity_itemdetail_materialcomponent, null);
        accessionNumberLayout.setHint("Enter Accession Number");
        accessionNumberLayout.setPadding(0, 10, 0, 10);
        accessionNumberEditText= new TextInputEditText(procedureNameLayout.getContext());
        accessionNumberEditText.setLayoutParams(new LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT));
        accessionNumberLayout.setTag("accession");
        accessionNumberEditText.setTag("accession_text");
        generateNewNumber(view,accessionNumberEditText);



        TextInputLayout numberUsedLayout = (TextInputLayout) View.inflate(view.getContext(),
                R.layout.activity_itemdetail_materialcomponent, null);
        numberUsedLayout.setHint("Enter Number of Items Used");
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
        numberUsedEditTextId = numberUsedEditText.getId();
        // incrementing number by 1 when clicked on the end icon
        numberUsedLayout.setEndIconOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                incrementNumberUsed(view,numberUsedEditText);
            }
        });
        numberUsedEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showNumberPicker(view,numberUsedEditText);
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
                        procedureNameEditText.getText().toString());
                procedureInfoMap.put(PROCEDUREDATE_KEY,
                        procedureDateEditText.getText().toString());
                procedureInfoMap.put(AMOUNTUSED_KEY,
                        numberUsedEditText.getText().toString());
                procedureInfoMap.put(ACCESSION_KEY,
                        accessionNumberEditText.getText().toString());
                procedureInfoMap.put(TIME_KEY,
                        procedureTimeEditText.getText().toString());
                for(TextInputEditText text : new TextInputEditText[]{
                        procedureDateEditText,procedureNameEditText,
                        accessionNumberEditText,numberUsedEditText,
                        procedureTimeEditText}){
                    if(text.toString().trim().isEmpty()){
                        saveButton.setEnabled(false);
                        break;
                    }
                    saveButton.setEnabled(true);
                }
            }
        };
        procedureMapList.add(procedureInfoMap);
        procedureNameEditText.addTextChangedListener(newProcedureTextWatcher);
        procedureDateEditText.addTextChangedListener(newProcedureTextWatcher);
        numberUsedEditText.addTextChangedListener(newProcedureTextWatcher);
        accessionNumberEditText.addTextChangedListener(newProcedureTextWatcher);
        procedureTimeEditText.addTextChangedListener(newProcedureTextWatcher);


        procedureNameLayout.addView(procedureNameEditText);
        procedureDateLayout.addView(procedureDateEditText);
        numberUsedLayout.addView(numberUsedEditText);
        accessionNumberLayout.addView(accessionNumberEditText);
        procedureInfoLayout.addView(procedureNumber,0);
        procedureInfoLayout.addView(procedureDateLayout,1);
        procedureInfoLayout.addView(procedureTimeLayout,2);
        procedureInfoLayout.addView(procedureNameLayout,3);
        procedureInfoLayout.addView(accessionNumberLayout,4);
        procedureInfoLayout.addView(numberUsedLayout,5);

        itemUsedFields.addView(procedureInfoLayout, itemUsedFields.indexOfChild(addProcedure));
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
                    if (document.exists()) {
                        generateNewNumber(view,accessionNumberEditText);
                    } else {
                        setAccessionNumber(view,accessionNumberEditText,accessionNum);
                    }
                } else {
                    Log.d(TAG, "Failed with: ", task.getException());
                }
            }
        });
    }

    // generates new number
    public void generateNewNumber(View view, TextInputEditText accessionNumberEditText){
        Random rand = new Random();
        String randomAccessionNum = null;
        String randomAccession = String.valueOf (1 + rand.nextInt(999999));
        if(randomAccession.length() == 1){
            randomAccessionNum = "TZ00000" + randomAccession;
        }if(randomAccession.length() == 2){
            randomAccessionNum = "TZ0000" + randomAccession;
        }
        if(randomAccession.length() == 3){
            randomAccessionNum = "TZ000" + randomAccession;
        }
        if(randomAccession.length() == 4){
            randomAccessionNum = "TZ00" + randomAccession;
        }
        if(randomAccession.length() == 5){
            randomAccessionNum = "TZ0" + randomAccession;
        }if(randomAccession.length() == 6){
            randomAccessionNum = "TZ" + randomAccession;
        }
        checkAccessionNumber(view, randomAccessionNum, accessionNumberEditText);

    }

    //sets accession number and save it to the database
    public void setAccessionNumber(View view, TextInputEditText accessionNumberEditText,String accessionNumber){
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
    private void removeAccessionNumber(View view,LinearLayout itemUsedFields){
        LinearLayout procedureInfo = (LinearLayout) itemUsedFields.getChildAt((itemUsedFields.indexOfChild(addProcedure))-1);
        TextInputLayout accessionNumLayout = procedureInfo.findViewWithTag("accession");
        TextInputEditText accessionNumEditText =  accessionNumLayout.findViewWithTag("accession_text");
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
        GridLayout gridLayoutSize = new GridLayout(view.getContext());
        GridLayout.LayoutParams paramSizeKey = new GridLayout.LayoutParams();
        paramSizeKey.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        paramSizeKey.width = WRAP_CONTENT;
        paramSizeKey.rowSpec = GridLayout.spec(rowIndex);
        paramSizeKey.columnSpec = GridLayout.spec(0);
        paramSizeKey.setMargins(0, 0, 0, 20);


        GridLayout.LayoutParams paramSizeValue = new GridLayout.LayoutParams();
        paramSizeValue.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        paramSizeValue.width = WRAP_CONTENT;
        paramSizeValue.rowSpec = GridLayout.spec(rowIndex);
        paramSizeValue.columnSpec = GridLayout.spec(1);
        paramSizeValue.setMargins(10, 0, 0, 20);


        TextInputLayout sizeKeyLayout = (TextInputLayout) View.inflate(view.getContext(),
                R.layout.activity_itemdetail_materialcomponent, null);
        sizeKeyLayout.setLayoutParams(paramSizeKey);
        sizeKeyLayout.setHint("Key");
        TextInputEditText sizeKey = new TextInputEditText(sizeKeyLayout.getContext());

        TextInputLayout sizeValueLayout = (TextInputLayout) View.inflate(view.getContext(),
                R.layout.activity_itemdetail_materialcomponent, null);
        sizeValueLayout.setLayoutParams(paramSizeValue);
        sizeValueLayout.setHint("Value");
        TextInputEditText sizeValue = new TextInputEditText(sizeKeyLayout.getContext());


        sizeKey.setLayoutParams(new LinearLayout.LayoutParams(430, WRAP_CONTENT));
        sizeKeyLayout.addView(sizeKey);
        sizeValue.setLayoutParams(new LinearLayout.LayoutParams(430, WRAP_CONTENT));
        sizeValueLayout.addView(sizeValue);
        gridLayoutSize.addView(sizeKeyLayout);
        gridLayoutSize.addView(sizeValueLayout);


        allSizeOptions.add(sizeKey);
        allSizeOptions.add(sizeValue);
        linearLayout.addView(gridLayoutSize, (rowLoc++) + linearLayout.indexOfChild(specsTextView));
        rowIndex++;
        System.out.println("row index is " + rowIndex);
        if (isAddSizeButtonClicked) {
            removeSizeButton = new MaterialButton(view.getContext(),
                    null, R.attr.materialButtonOutlinedStyle);
            removeSizeButton.setText(R.string.removeSize_label);
            removeSizeButton.setLayoutParams(new LinearLayout.LayoutParams(MATCH_PARENT,
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
    private void removeEmptySizeOption(View view) {
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
    private void addItemSpecs(String key,String value, View view){
        Log.d(TAG, "Adding item specs!");
        GridLayout gridLayoutSize = new GridLayout(view.getContext());
        GridLayout.LayoutParams paramSizeKey = new GridLayout.LayoutParams();
        paramSizeKey.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        paramSizeKey.width = WRAP_CONTENT;
        paramSizeKey.rowSpec = GridLayout.spec(rowIndex);
        paramSizeKey.columnSpec = GridLayout.spec(0);
        paramSizeKey.setMargins(0, 0, 0, 20);


        GridLayout.LayoutParams paramSizeValue = new GridLayout.LayoutParams();
        paramSizeValue.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        paramSizeValue.width = WRAP_CONTENT;
        paramSizeValue.rowSpec = GridLayout.spec(rowIndex);
        paramSizeValue.columnSpec = GridLayout.spec(1);
        paramSizeValue.setMargins(10, 0, 0, 20);


        TextInputLayout sizeKeyLayout = (TextInputLayout) View.inflate(view.getContext(),
                R.layout.activity_itemdetail_materialcomponent, null);
        sizeKeyLayout.setLayoutParams(paramSizeKey);
        sizeKeyLayout.setHint("Key");
        TextInputEditText sizeKey = new TextInputEditText(sizeKeyLayout.getContext());
        sizeKey.setText(key);
        sizeKey.setFocusable(false);

        TextInputLayout sizeValueLayout = (TextInputLayout) View.inflate(view.getContext(),
                R.layout.activity_itemdetail_materialcomponent, null);
        sizeValueLayout.setLayoutParams(paramSizeValue);
        sizeValueLayout.setHint("Value");
        TextInputEditText sizeValue = new TextInputEditText(sizeKeyLayout.getContext());
        sizeValue.setText(value);
        sizeValue.setFocusable(false);


        sizeKey.setLayoutParams(new LinearLayout.LayoutParams(430, WRAP_CONTENT));
        sizeKeyLayout.addView(sizeKey);
        sizeValue.setLayoutParams(new LinearLayout.LayoutParams(430, WRAP_CONTENT));
        sizeValueLayout.addView(sizeValue);
        gridLayoutSize.addView(sizeKeyLayout);
        gridLayoutSize.addView(sizeValueLayout);


        allSizeOptions.add(sizeKey);
        allSizeOptions.add(sizeValue);
        linearLayout.addView(gridLayoutSize, (rowLoc++) + linearLayout.indexOfChild(specsTextView));
        rowIndex++;
    }


    // adds new text field if users choose "other" for type
    private void addTypeOptionField(final AdapterView<?> adapterView, View view, int i, long l) {
        String selected = (String) adapterView.getItemAtPosition(i);
        TextInputLayout other_type_layout = null;
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
                    if(!(otherType_text.toString().trim().isEmpty())){
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
            if((checkAutocompleteTexts && checkEditTexts) && (checkSingleUseButton || checkMultiUseButton)) {
                saveButton.setEnabled(true);
            }
            linearLayout.removeViewAt(1 + linearLayout.indexOfChild(typeConstrainLayout));
        }
    }


    private void addNewSite(final AdapterView<?> adapterView, View view, int i, long l) {
        String selected = (String) adapterView.getItemAtPosition(i);
        TextInputLayout other_site_layout = null;
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
                    if(!(otherSite_text.toString().trim().isEmpty())){
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
            if((checkAutocompleteTexts && checkEditTexts) && (checkSingleUseButton || checkMultiUseButton)) {
                saveButton.setEnabled(true);
            }
            chosenSite = false;
            linearLayout.removeViewAt( 1 +  linearLayout.indexOfChild(siteConstrainLayout));
        }
    }

    private void addNewLoc(final AdapterView<?> adapterView, View view, int i, long l) {
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
                    if(!(otherPhysicalLoc_text.toString().trim().isEmpty())){
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
        } else if (chosenLocation && (!(selectedLoc.equals("Other")))) {
            if((checkAutocompleteTexts && checkEditTexts) && (checkSingleUseButton || checkMultiUseButton)) {
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
        String type_str = equipmentType.getText().toString();
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
        TextInputEditText numberUsedEditText = view.findViewById(numberUsedEditTextId);
        int totalUsed = 0;
        for(int i = 0; i < numberUsedList.size(); i++){
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
        String site_name_str = "";
        if (chosenSite) {
            site_name_str = Objects.requireNonNull(otherSite_text.getText()).toString();
        } else {
            site_name_str = hospitalName.getText().toString();
        }
        String physical_location_str = "";
        if (chosenLocation) {
            physical_location_str = Objects.requireNonNull(otherPhysicalLoc_text.getText()).toString().trim();
        } else {
            physical_location_str = physicalLocation.getText().toString().trim();
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
        diDoc.put(DI_KEY, di_str);
        diDoc.put(SITE_KEY, site_name_str);
        diDoc.put(DESCRIPTION_KEY, description_str);
        diDoc.put(SPECIALTY_KEY, medical_speciality_str);
        diDoc.put(USAGE_KEY, singleOrMultiUse);
        diDoc.put(QUANTITY_KEY,diQuantity);

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

        Map<String, Object> procedureQuantity = new HashMap<>();
        procedureQuantity.put("procedure_number",String.valueOf(procedureFieldAdded));

        if (checkItemUsed) {
            for(int i = 0; i < procedureMapList.size(); i++){
                DocumentReference procedureDocRef = db.collection(NETWORKS).document(NETWORK)
                        .collection(SITES).document(SITE).collection(DEPARTMENTS)
                        .document(DEPARTMENT).collection(PRODUCTDIS).document(di_str)
                        .collection("UDIs").document(barcode_str).collection("procedures")
                        .document("procedure_" + (procedureListCounter+1));
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
                            Log.d(TAG,e.toString());
                        }
                    });
            procedureQuantity.clear();
        }



        if (allSizeOptions.size() > 0) {
            int i = 0;
            Map<String, Object> sizeOptions = new HashMap<>();
            while (i < allSizeOptions.size()) {
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

    String di = "";
    private void autoPopulate(final DocumentReference siteDocRef, final View view) {


        final String udiStr = udiEditText.getText().toString();
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
                        JSONObject responseJson = null;
                        try {
                            responseJson = new JSONObject(response);

                            Log.d(TAG, "RESPONSE: " + response);

                            JSONObject deviceInfo = responseJson.getJSONObject("gudid").getJSONObject("device");
                            JSONObject udi = responseJson.getJSONObject("udi");
                            JSONArray productCodes = responseJson.getJSONArray("productCodes");
                            String medicalSpecialties = "";
                            for (int i = 0; i < productCodes.length(); i++) {
                                medicalSpecialties += productCodes.getJSONObject(i).getString("medicalSpecialty");
                                medicalSpecialties += "; ";
                            }
                            medicalSpecialties = medicalSpecialties.substring(0, medicalSpecialties.length() - 2);

                            lotNumber.setText(udi.getString("lotNumber"));
                            lotNumber.setFocusable(false);

                            company.setText(deviceInfo.getString("companyName"));
                            company.setFocusable(false);

                            expiration.setText(udi.getString("expirationDate"));
                            expiration.setFocusable(false);


                            di = udi.getString("di");
                            deviceIdentifier.setText(udi.getString("di"));
                            deviceIdentifier.setFocusable(false);


                            nameEditText.setText(deviceInfo.getJSONObject("gmdnTerms").getJSONArray("gmdn").getJSONObject(0).getString("gmdnPTName"));

                            nameEditText.setFocusable(false);

                            deviceDescription.setText(deviceInfo.getString("deviceDescription"));
                            deviceDescription.setFocusable(false);


                            referenceNumber.setText(deviceInfo.getString("catalogNumber"));

                            referenceNumber.setFocusable(false);

                            medicalSpeciality.setText(medicalSpecialties);
                            medicalSpeciality.setFocusable(false);


                            numberAdded.setText(deviceInfo.getString("deviceCount"));
                            autoPopulateFromDatabase(udi, siteDocRef,udiStr, view);

                            JSONArray deviceSizeArray = deviceInfo.getJSONObject("deviceSizes").getJSONArray("deviceSize");

                            for (int i = 0; i < deviceSizeArray.length(); ++i){
                                String k;
                                String v;
                                JSONObject currentSizeObject = deviceSizeArray.getJSONObject(i);
                                k = currentSizeObject.getString("sizeType");
                                Log.d(TAG, "KEYS: " + k);
                                if (k.equals("Device Size Text, specify")){
                                    String customSizeText = currentSizeObject.getString("sizeText");
                                    // Key is usually substring before first number (e.g. "Co-Axial Introducer Needle: 17ga x 14.9cm")
                                    k = customSizeText.split("[0-9]+")[0];

                                    // needs remember the cutoff to retrieve the rest of the string
                                    int cutoff = k.length();
                                    // take off trailing whitespace
                                    try {
                                        k = k.substring(0, k.length() - 2);
                                    } catch (StringIndexOutOfBoundsException e){ // if sizeText starts with number
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
                                addItemSpecs(k,v,view);
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

        // getting and updating procedure number added to database
        TextWatcher diWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                updateProcedureFieldAdded(udiStr, deviceIdentifier.getText().toString());

            }
        };
        deviceIdentifier.addTextChangedListener(diWatcher);


    }

    private void updateProcedureFieldAdded(String udi, String di){
        DocumentReference UdiDocRef = db.collection("networks").document("network1")
                .collection("sites").document("n1_hospital3")
                .collection("n1_h3_departments").document("department1")
                .collection("n1_h1_d1 productids").document(di).collection("UDIs")
                .document(udi);

        UdiDocRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        if(document.get("procedure_number") != null){
                            procedureFieldAdded = Integer.parseInt(
                                    Objects.requireNonNull(document.getString("procedure_number")));
                            procedureListCounter = procedureFieldAdded;


                        }else{
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


    private void autoPopulateFromDatabase(final JSONObject udi, final DocumentReference siteDocRef, final String udiStr, final View view) {
        DocumentReference udiDocRef = null;
        DocumentReference diDocRef = null;
        try {

            udiDocRef = siteDocRef
                    .collection("n1_h3_departments").document("department1")
                    .collection("n1_h1_d1 productids").document(udi.getString("di"))
                    .collection("UDIs").document(udiStr);

            diDocRef = siteDocRef
                    .collection("n1_h3_departments").document("department1")
                    .collection("n1_h1_d1 productids").document(udi.getString("di"));

        } catch (JSONException e) {
            e.printStackTrace();
        }

        udiDocRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        if(document.get("procedure_number") != null){
                            procedureCount = Integer.parseInt(
                                    Objects.requireNonNull(document.getString("procedure_number")));
                            getProcedureInfo(procedureCount,siteDocRef,udi, udiStr, view);
                        }else{
                            procedureCount = 0;
                        }

                    } else {
                        procedureCount = 0;

                        Log.d(TAG, "Document does not exist!");
                    }
                } else {
                    procedureCount = 0;
                    Log.d(TAG, "Failed with: ", task.getException());
                }
            }
        });

        diDocRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        if(document.get(TYPE_KEY) != null){
                            equipmentType.setText(document.getString(TYPE_KEY));
                            equipmentType.setFocusable(false);

                        }if(document.get(SITE_KEY) != null){
                            hospitalName.setText(document.getString(SITE_KEY));
                            hospitalName.setFocusable(false);
                        }if(document.get(QUANTITY_KEY) != null){
                            diQuantity = document.getString(QUANTITY_KEY);
                        }else{
                            diQuantity = "0";
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
                    if (document.exists()) {
                        if(document.get("quantity") != null) {
                            itemQuantity = document.getString(QUANTITY_KEY);
                            quantity.setText(itemQuantity);
                        }else{
                            itemQuantity = "0";
                            quantity.setText("0");
                        }if(document.get(PHYSICALLOC_KEY) != null){
                            physicalLocation.setText(document.getString(PHYSICALLOC_KEY));
                            physicalLocation.setFocusable(false);

                        }
                    } else {
                        itemQuantity = "0";
                        quantity.setText("0");

                        Log.d(TAG, "Document does not exist!");
                    }
                    quantity.setText(document.getString(QUANTITY_KEY));
                } else {
                    Log.d(TAG, "Failed with: ", task.getException());
                }
            }
        });

    }

    private void getProcedureInfo(final int procedureCount, DocumentReference siteDocRef, JSONObject udi,
                                  String udiStr, final View view){
        final int[] check = {0};
        DocumentReference procedureRef;

        try {
            for ( int i = 0; i < procedureCount; i++) {
                procedureRef = siteDocRef
                        .collection("n1_h3_departments").document("department1")
                        .collection("n1_h1_d1 productids").document(udi.getString("di"))
                        .collection("UDIs").document(udiStr).collection("procedures")
                        .document("procedure_" + (i + 1));
                procedureRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                Map<String, Object> map = document.getData();
                                if (map != null) {
                                    check[0]++;
                                    procedureDocuments = new ArrayList<>();
                                    for (Object entry : map.values()) {
                                        procedureDocuments.add(entry.toString());
                                    }
                                    map.clear();
                                }
                            }
                            procedureDoc.add(procedureDocuments);
                            if(check[0] == procedureCount) {
                                addProcedureInfoFields(procedureDoc,view);
                            }
                        }
                    }
                });
            }
        }catch(JSONException e){
            Log.d(TAG, e.toString());
        }
    }

    // need to create procedure info fields for each procedure.
    // data is already queried.
    private void addProcedureInfoFields(final List<List<String>> procedureDoc, View view){
        System.out.println(procedureDoc);

        final LinearLayout procedureInfoLayout = new LinearLayout(view.getContext());
        procedureInfoLayout.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        procedureInfoLayout.setOrientation(LinearLayout.VERTICAL);
        GridLayout procedureInfo = new GridLayout(view.getContext());;


        GridLayout.LayoutParams procedureDateParams = new GridLayout.LayoutParams();
        procedureDateParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        procedureDateParams.width = (((View) view.getParent()).getWidth())/2;
        procedureDateParams.rowSpec = GridLayout.spec(0);
        procedureDateParams.columnSpec = GridLayout.spec(0);
        procedureDateParams.setMargins(0, 0, 0, 5);
        TextInputLayout procedureDateHeader = (TextInputLayout) View.inflate(view.getContext(),
                R.layout.activity_itemdetail_materialcomponent, null);
        procedureDateHeader.setLayoutParams(procedureDateParams);

        TextInputEditText dateKey = new TextInputEditText(procedureDateHeader.getContext());
        dateKey.setText("Procedure Date");
        dateKey.setClickable(false);
        dateKey.setFocusable(false);
        procedureDateHeader.addView(dateKey);


        GridLayout.LayoutParams procedureParams = new GridLayout.LayoutParams();
        procedureParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        procedureParams.width = (((View) view.getParent()).getWidth())/2;
        procedureParams.rowSpec = GridLayout.spec(0);
        procedureParams.columnSpec = GridLayout.spec(1);
        procedureParams.setMargins(0, 0, 0, 5);
        final TextInputLayout procedureDateText = (TextInputLayout) View.inflate(view.getContext(),
                R.layout.activity_itemdetail_materialcomponent, null);
        procedureDateText.setLayoutParams(procedureParams);

        TextInputEditText dateText = new TextInputEditText(procedureDateText.getContext());
        dateText.setText("2020/09/09");
        dateText.setFocusable(false);
        procedureDateText.addView(dateText);
        procedureDateText.setEndIconMode(TextInputLayout.END_ICON_CUSTOM);
        procedureDateText.setEndIconDrawable(R.drawable.ic_baseline_plus);
        procedureDateText.setEndIconTintList(ColorStateList.valueOf(getResources().
                getColor(R.color.colorPrimary, Objects.requireNonNull(getActivity()).getTheme())));

        final boolean[] isMaximized = {false};

        procedureDateText.setEndIconOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isMaximized[0]){
                    procedureDateText.setEndIconDrawable(R.drawable.ic_baseline_plus);
                    procedureDateText.setEndIconTintList(ColorStateList.valueOf(getResources().
                            getColor(R.color.colorPrimary, Objects.requireNonNull(getActivity()).getTheme())));
                    isMaximized[0] = false;
                    addProcedureSubFields(procedureInfoLayout,view,procedureDoc);
                }else{
                    procedureDateText.setEndIconDrawable(R.drawable.ic_remove_minimize);
                    procedureDateText.setEndIconTintList(ColorStateList.valueOf(getResources().
                            getColor(R.color.colorPrimary, Objects.requireNonNull(getActivity()).getTheme())));
                    isMaximized[0] = true;
                }
            }
        });

        procedureInfo.addView(procedureDateHeader);
        procedureInfo.addView(procedureDateText);
        procedureInfoLayout.addView(procedureInfo);
        linearLayout.addView(procedureInfoLayout,linearLayout.indexOfChild(usageHeader) +   1);

    }

    private void addProcedureSubFields(LinearLayout procedureInfoLayout, View view, List<List<String>> procedureDoc){

    }
    private void autoPopulateFromDatabase(final View view, String di) {

        DocumentReference diDocRef = db.collection("networks").document("network1")
                .collection("sites").document("n1_hospital3")
                .collection("n1_h3_departments").document("department1")
                .collection("n1_h1_d1 productids").document(di);



        diDocRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
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