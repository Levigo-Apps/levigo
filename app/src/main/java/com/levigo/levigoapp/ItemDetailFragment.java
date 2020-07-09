package com.levigo.levigoapp;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
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
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
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
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.zxing.integration.android.IntentIntegrator;
import com.journeyapps.barcodescanner.CaptureActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

public class ItemDetailFragment extends Fragment {

    LinearLayout linearLayout;

    // Firebase database
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    InventoryTemplate diDocument;
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
    private TextInputEditText otherSiteLoc_text;
    private TextInputEditText procedureDate;
    private TextInputEditText patient_id;
    private TextInputEditText patient_idDefault;
    private TextInputEditText deviceIdentifier;
    private TextInputEditText expiration;
    private TextInputEditText quantity;
    private TextInputEditText lotNumber;
    private TextInputEditText amountUsed;
    private AutoCompleteTextView hospitalName;
    private AutoCompleteTextView physicalLocation;
    private TextInputEditText notes;
    private TextInputEditText numberUsed;
    private TextInputEditText currentDateTime;
    private TextInputEditText numberAdded;
    private TextInputLayout expirationTextLayout;
    private TextInputLayout timeLayout;

    private GridLayout gridLayoutSize;


    private Button saveButton;
    private MaterialButton addPatient;
    private MaterialButton removePatient;
    private MaterialButton submit_otherType;
    private MaterialButton submit_otherPhysicalLoc;
    private MaterialButton submit_otherSiteLoc;
    private SwitchMaterial itemUsed;
    private RadioGroup useRadioGroup;
    private RadioButton radioButton;
    private ImageButton backButton;
    private Button rescanButton;
    private Button addSizeButton;
    private int patientidAdded = 0;
    private boolean chosenType;
    private boolean chosenLocation;
    private boolean chosenReusable;
    private Button autoPopulateButton;
    private List<TextInputEditText> allPatientIds;


    // firebase key labels
    private final String NAME_KEY = "name";
    private final String TYPE_KEY = "equipment_type";
    private final String COMPANY_KEY = "company";
    private final String DI_KEY = "di";
    private final String SITE_KEY = "site_name";


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        final View rootView = inflater.inflate(R.layout.fragment_itemdetail, container, false);
        final Calendar myCalendar = Calendar.getInstance();

        parent = getActivity();

        // TODO add "clear" option for some fields in xml

        linearLayout = rootView.findViewById(R.id.itemdetail_linearlayout);
        udiEditText = (TextInputEditText) rootView.findViewById(R.id.detail_udi);
        nameEditText = (TextInputEditText) rootView.findViewById(R.id.detail_name);
        equipmentType = (AutoCompleteTextView) rootView.findViewById(R.id.detail_type);
        company = (TextInputEditText) rootView.findViewById(R.id.detail_company);
        expiration = (TextInputEditText) rootView.findViewById(R.id.detail_expiration_date);
        hospitalName = (AutoCompleteTextView) rootView.findViewById(R.id.detail_site_location);
        physicalLocation = (AutoCompleteTextView) rootView.findViewById(R.id.detail_physical_location);
        notes = (TextInputEditText) rootView.findViewById(R.id.detail_notes);
        lotNumber = (TextInputEditText) rootView.findViewById(R.id.detail_lot_number);
        numberAdded = (TextInputEditText) rootView.findViewById(R.id.detail_number_added);
        deviceIdentifier = (TextInputEditText) rootView.findViewById(R.id.detail_di);
        currentDateTime = (TextInputEditText) rootView.findViewById(R.id.detail_date_time);
        expirationTextLayout = (TextInputLayout) rootView.findViewById(R.id.expiration_date_string2);
        timeLayout = (TextInputLayout) rootView.findViewById(R.id.time_layout);
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



        itemUsed.setChecked(false);
        addSizeButton = rootView.findViewById(R.id.button_addsize);


        // Dropdown menu for Type field
        final ArrayList<String> TYPES = new ArrayList<>(Arrays.asList("Balloon", "Catheter", "Needle", "Dilator", "Drainage Bag",
                "Biliary Stent", "Wire", "Imaging/US", "Mask", "Picc Line", "Scalpel", "Sheath", "Snare Kit",
                "Stent", "Sterile Tray", "Stopcock", "Tube", "Other"));
        Collections.sort(TYPES);

        final ArrayAdapter<String> adapter =
                new ArrayAdapter<>(
                        rootView.getContext(),
                        R.layout.dropdown_menu_popup_item,
                        TYPES);

        @SuppressLint("CutPasteId") final AutoCompleteTextView type_dropDown =
                rootView.findViewById(R.id.detail_type);
        type_dropDown.setAdapter(adapter);

        type_dropDown.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String selected = (String) adapterView.getItemAtPosition(i);
                TextInputLayout other_type_layout = null;
                if (selected.equals("Other")) {
                    chosenType = true;
                    other_type_layout = new TextInputLayout(rootView.getContext(), null,
                            R.style.Widget_MaterialComponents_TextInputLayout_OutlinedBox);
                    other_type_layout.setHint("Enter type");
                    other_type_layout.setId(View.generateViewId());
                    other_type_layout.setBoxBackgroundMode(TextInputLayout.BOX_BACKGROUND_OUTLINE);
                    otherType_text = new TextInputEditText(other_type_layout.getContext());
                    otherType_text.setLayoutParams(new LinearLayout.LayoutParams(udiEditText.getWidth(), WRAP_CONTENT));
                    other_type_layout.addView(otherType_text);
                    linearLayout.addView(other_type_layout, 1 + linearLayout.indexOfChild(rootView.findViewById(R.id.typeInputLayout)));

                    submit_otherType = new MaterialButton(rootView.getContext(),
                            null, R.attr.materialButtonOutlinedStyle);
                    submit_otherType.setText(R.string.otherType_lbl);
                    submit_otherType.setLayoutParams(new LinearLayout.LayoutParams(udiEditText.getWidth(),
                            WRAP_CONTENT));
                    linearLayout.addView(submit_otherType, 2 + linearLayout.indexOfChild(rootView.findViewById(R.id.typeInputLayout)));

                    submit_otherType.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Toast.makeText(rootView.getContext(), otherType_text.getText().toString(), Toast.LENGTH_SHORT).show();
                            TYPES.add(otherType_text.getText().toString());
                            System.out.println(Arrays.toString(TYPES.toArray()));
                            ArrayAdapter<String> adapter_new =
                                    new ArrayAdapter<>(
                                            rootView.getContext(),
                                            R.layout.dropdown_menu_popup_item,
                                            TYPES);
                            type_dropDown.setAdapter(adapter_new);

                        }
                    });


                }else if(chosenType && (!(selected.equals("Other")))) {
                    chosenType = false;
                    linearLayout.removeViewAt(1 + linearLayout.indexOfChild(rootView.findViewById(R.id.typeInputLayout)));
                    linearLayout.removeViewAt(1 + linearLayout.indexOfChild(rootView.findViewById(R.id.typeInputLayout)));
                }
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

        // Dropdown menu for Physical Location field
        final ArrayList<String> PHYSICALLOC = new ArrayList<>(Arrays.asList("Room", "Box", "Shelf", "Other"));
        Collections.sort(PHYSICALLOC);

        final ArrayAdapter<String> adapter1 =
                new ArrayAdapter<>(
                        rootView.getContext(),
                        R.layout.dropdown_menu_popup_item,
                        PHYSICALLOC);

        @SuppressLint("CutPasteId") final AutoCompleteTextView physicalloc_dropDown =
                rootView.findViewById(R.id.detail_physical_location);
        physicalloc_dropDown.setAdapter(adapter1);

        physicalloc_dropDown.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String selectedOther = (String) adapterView.getItemAtPosition(i);
                final TextInputLayout other_physicaloc_layout;
                if (selectedOther.equals("Other")) {
                    chosenLocation = true;
                    other_physicaloc_layout = new TextInputLayout(rootView.getContext(), null,
                            R.style.Widget_MaterialComponents_TextInputLayout_OutlinedBox);
                    other_physicaloc_layout.setHint("Enter physical location");
                    other_physicaloc_layout.setId(View.generateViewId());
                    other_physicaloc_layout.setBoxBackgroundMode(TextInputLayout.BOX_BACKGROUND_OUTLINE);
                    otherPhysicalLoc_text = new TextInputEditText(other_physicaloc_layout.getContext());
                    otherPhysicalLoc_text.setLayoutParams(new LinearLayout.LayoutParams(udiEditText.getWidth(), WRAP_CONTENT));
                    other_physicaloc_layout.addView(otherPhysicalLoc_text);
                    linearLayout.addView(other_physicaloc_layout, 1 + linearLayout.indexOfChild(rootView.findViewById(R.id.physicalLocationLayout)));

                    submit_otherPhysicalLoc = new MaterialButton(rootView.getContext(),
                            null, R.attr.materialButtonOutlinedStyle);
                    submit_otherPhysicalLoc.setText(R.string.submitLocation_lbl);
                    submit_otherPhysicalLoc.setLayoutParams(new LinearLayout.LayoutParams(udiEditText.getWidth(),
                            WRAP_CONTENT));
                    linearLayout.addView(submit_otherPhysicalLoc, 2 + linearLayout.indexOfChild(rootView.findViewById(R.id.physicalLocationLayout)));

                    submit_otherPhysicalLoc.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Toast.makeText(rootView.getContext(), otherPhysicalLoc_text.getText().toString(), Toast.LENGTH_SHORT).show();
                            PHYSICALLOC.add(otherPhysicalLoc_text.getText().toString());
                            System.out.println(Arrays.toString(PHYSICALLOC.toArray()));
                            ArrayAdapter<String> adapter_new =
                                    new ArrayAdapter<>(
                                            rootView.getContext(),
                                            R.layout.dropdown_menu_popup_item,
                                            PHYSICALLOC);
                            physicalloc_dropDown.setAdapter(adapter_new);

                        }
                    });
                }else if (chosenLocation && (!(selectedOther.equals("Other")))) {
                    chosenLocation = false;
                    linearLayout.removeViewAt(1 + linearLayout.indexOfChild(rootView.findViewById(R.id.physicalLocationLayout)));
                    linearLayout.removeViewAt(1 + linearLayout.indexOfChild(rootView.findViewById(R.id.physicalLocationLayout)));
                }
            }
        });

        // Dropdown menu for Site Location field
        final ArrayList<String> SITELOC = Sites.SITES;
        Collections.sort(SITELOC);

        final ArrayAdapter<String> adapter2 =
                new ArrayAdapter<>(
                        rootView.getContext(),
                        R.layout.dropdown_menu_popup_item,
                        SITELOC);

        @SuppressLint("CutPasteId") final AutoCompleteTextView siteloc_dropDown =
                rootView.findViewById(R.id.detail_site_location);
        siteloc_dropDown.setAdapter(adapter2);


        //TimePicker dialog pops up when clicked on the icon
        timeLayout.setEndIconOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar mcurrentTime = Calendar.getInstance();
                int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
                int minute = mcurrentTime.get(Calendar.MINUTE);
                TimePickerDialog mTimePicker;
                mTimePicker = new TimePickerDialog(rootView.getContext(), new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                        currentDateTime.setText(String.format(Locale.US, "%d:%d: 00 %s", selectedHour,
                                selectedMinute, TimeZone.getDefault().getID()));
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

        final LinearLayout itemUsedFields = rootView.findViewById(R.id.layout_itemused);
        itemUsedFields.setVisibility(View.GONE);

        RadioButton multiUse = rootView.findViewById(R.id.radio_multiuse);


        // TODO update to uniform style
        // when clicked add one more additional field for Patient ID
        addPatient.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                patientidAdded++;
                TextInputLayout patient_id_layout = new TextInputLayout(rootView.getContext(), null,
                        R.style.Widget_MaterialComponents_TextInputLayout_OutlinedBox);
//                patient_id_layout.setLayoutParams(new TextInputLayout.LayoutParams(320, ViewGroup.LayoutParams.WRAP_CONTENT));
                patient_id_layout.setHint("Enter patient ID");
                patient_id_layout.setBoxBackgroundMode(TextInputLayout.BOX_BACKGROUND_OUTLINE);

                patient_id = new TextInputEditText(patient_id_layout.getContext());
                allPatientIds.add(patient_id);
                patient_id.setLayoutParams(new LinearLayout.LayoutParams(udiEditText.getWidth(), ViewGroup.LayoutParams.WRAP_CONTENT));
                patient_id_layout.addView(patient_id);
                itemUsedFields.addView(patient_id_layout,  itemUsedFields.indexOfChild(addPatient));
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
                    itemUsedFields.setVisibility(View.VISIBLE);

                } else {
                    itemUsedFields.setVisibility(View.GONE);
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

        // disabling save button if required fields are empty
//        TextWatcher textWatcher = new TextWatcher() {
//            @Override
//            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
//                mSave.setEnabled(false);
//
//            }

//        barcode.addTextChangedListener(textWatcher);
//        name.addTextChangedListener(textWatcher);
//        equipment_type.addTextChangedListener(textWatcher);
//        company.addTextChangedListener(textWatcher);


        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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

    private void addEmptySizeOption(View view) {
        Log.d(TAG, "Adding empty size option!");
        /*
//        other_physicaloc_layout = new TextInputLayout(rootView.getContext(), null,
//                R.style.Widget_MaterialComponents_TextInputLayout_OutlinedBox);
//        other_physicaloc_layout.setHint("Enter physical location");
//        other_physicaloc_layout.setId(View.generateViewId());
//        other_physicaloc_layout.setBoxBackgroundMode(TextInputLayout.BOX_BACKGROUND_OUTLINE);
//        otherPhysicalLoc_text = new TextInputEditText(other_physicaloc_layout.getContext());
//        otherPhysicalLoc_text.setLayoutParams(new LinearLayout.LayoutParams(udiEditText.getWidth(), WRAP_CONTENT));
//        other_physicaloc_layout.addView(otherPhysicalLoc_text);
//        linearLayout.addView(other_physicaloc_layout, 1 + linearLayout.indexOfChild(rootView.findViewById(R.id.physicalLocationLayout)));
//

//        EditText sizeKey = new EditText(this.getActivity());

//        linearLayout = view.findViewById(R.id.itemdetail_linearlayout);
//        linearLayout.addView(sizeKey);
        gridLayoutSize = view.findViewById(R.id.gridlayout_size);

        TextInputLayout sizeKeyLayout = new TextInputLayout(view.getContext(), null, R.style.Widget_MaterialComponents_TextInputLayout_OutlinedBox);
//        sizeKeyLayout.setWeightSum(1);
        sizeKeyLayout.setBoxBackgroundMode(TextInputLayout.BOX_BACKGROUND_OUTLINE);
//        TextInputLayout.LayoutParams param = new TextInputLayout.LayoutParams();
//        param.width = TextInputLayout.LayoutParams.WRAP_CONTENT;
        //        sizeKeyLayout.setLayoutParams(param);
        sizeKeyLayout.setLayoutParams(new TextInputLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT, 1));

        TextInputEditText sizeKey = new TextInputEditText(view.getContext());
//        sizeKey.setLayoutParams(new GridLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT));
        //TODO create resource
        sizeKey.setHint("Key");
        sizeKeyLayout.addView(sizeKey);
//        sizeKeyLayout.setWeightSum(1);
//        sizeKeyLayout.addView();
        gridLayoutSize.addView(sizeKeyLayout, 0);
//        sizeKeyLayout.column

         */
    }


    // method for saving data to firebase cloud firestore
    public void saveData(View view, String NETWORKS, String NETWORK, String SITES, String SITE,
                         String DEPARTMENTS, String DEPARTMENT, String PRODUCTDIS) {


        Log.d(TAG, "SAVING");
        String barcode_str = udiEditText.getText().toString();
        String name_str = nameEditText.getText().toString();
        String type_str = equipmentType.getText().toString();
        String company_str = company.getText().toString();

        // getting radiobutton value
        boolean isUsed = itemUsed.isChecked();
        int radioButtonInt = useRadioGroup.getCheckedRadioButtonId();
        radioButton = (RadioButton) view.findViewById(radioButtonInt);
        String radioButtonVal = radioButton.getText().toString();

        //if used
        String procedure_used_str = procedureUsed.getText().toString();
        String procedure_date_str = procedureDate.getText().toString();
        String amount_used_str = amountUsed.getText().toString();

        String patient_id_str = patient_idDefault.getText().toString();

        String number_added_str = numberAdded.getText().toString();
        String di_str = deviceIdentifier.getText().toString();
        String lotNumber_str = lotNumber.getText().toString();
        String expiration_str = expiration.getText().toString();


        int quantity_int;
        if(itemUsed.isChecked()){
            //     quantity_int = Integer.parseInt(quantity.getText().toString()) - Integer.parseInt(numberUsed.getText().toString());
        } else {
            //        quantity_int = Integer.parseInt(quantity.getText().toString()) + Integer.parseInt(numberAdded.getText().toString());
        }
        String quantity_str = "2"; // temporarily
        String site_name_str = hospitalName.getText().toString();
        String physical_location_str = physicalLocation.getText().toString();
        String currentDateTime_str = currentDateTime.getText().toString();
        String notes_str = notes.getText().toString();


       // saving di-specific identifiers using HashMap
        Map<String, Object> diDoc = new HashMap<>();
        diDoc.put(NAME_KEY,name_str);
        diDoc.put(TYPE_KEY,type_str);
        diDoc.put(COMPANY_KEY,company_str);
        diDoc.put(DI_KEY,di_str);
        diDoc.put(SITE_KEY,site_name_str);
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
        udiDocument = new InventoryTemplate(barcode_str,isUsed,radioButtonVal,procedure_used_str,
                procedure_date_str, amount_used_str,patient_id_str, number_added_str,lotNumber_str,
                expiration_str, quantity_str,currentDateTime_str,physical_location_str, notes_str);

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

                            lotNumber.setText(udi.getString("lotNumber"));
                            company.setText(deviceInfo.getString("companyName"));
                            expiration.setText(udi.getString("expirationDate"));
                            deviceIdentifier.setText(udi.getString("di"));
//                        Log.d(TAG, deviceInfo.getJSONObject("gmdnTerms").getJSONArray("gmdn").getJSONObject(0).getString("gmdnPTName"));
                            nameEditText.setText(deviceInfo.getJSONObject("gmdnTerms").getJSONArray("gmdn").getJSONObject(0).getString("gmdnPTName"));

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
//                textView.setText("That didn't work!");
                Log.d(TAG, "Error in parsing barcode");
            }
        });

// Add the request to the RequestQueue.
        queue.add(stringRequest);
    }

}