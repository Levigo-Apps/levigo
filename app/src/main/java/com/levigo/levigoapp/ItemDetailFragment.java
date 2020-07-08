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
import com.google.firebase.firestore.SetOptions;
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
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

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
    private TextInputEditText procedure_used;
    private TextInputEditText otherType_text;
    private TextInputEditText otherPhysicalLoc_text;
    private TextInputEditText otherSiteLoc_text;
    private TextInputEditText procedureDate;
    private TextInputEditText patient_id;
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
    private TextInputLayout expiration_textLayout;
    private TextInputLayout timeLayout;


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
    private int patientidAdded = 0;
    private boolean chosenType;
    private boolean chosenLocation;

    private Button autoPopulateButton;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        final View rootView = inflater.inflate(R.layout.fragment_itemdetail, container, false);
        final Calendar myCalendar = Calendar.getInstance();

        parent = getActivity();

        // TODO add "clear" option for some fields in xml

        linearLayout = rootView.findViewById(R.id.linear_layout);
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
        expiration_textLayout = (TextInputLayout) rootView.findViewById(R.id.expiration_date_string2);
        timeLayout = (TextInputLayout) rootView.findViewById(R.id.time_layout);
        itemUsed = rootView.findViewById(R.id.detail_used_switch);
        saveButton = rootView.findViewById(R.id.detail_save_button);
        backButton = rootView.findViewById(R.id.detail_back_button);
        rescanButton = rootView.findViewById(R.id.detail_rescan_button);
        addPatient = rootView.findViewById(R.id.button_addpatient);
        removePatient = rootView.findViewById(R.id.button_removepatient);
        autoPopulateButton = rootView.findViewById(R.id.detail_autopop_button);
        useRadioGroup = rootView.findViewById(R.id.RadioGroup_id);
        procedure_used = rootView.findViewById(R.id.edittext_procedure_used);
        procedureDate = rootView.findViewById(R.id.edittext_procedure_date);
        amountUsed = rootView.findViewById(R.id.amountUsed_id);
        patient_id = rootView.findViewById(R.id.patientID_id);

        itemUsed.setChecked(false);


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
                    otherType_text.setLayoutParams(new LinearLayout.LayoutParams(udiEditText.getWidth(), ViewGroup.LayoutParams.WRAP_CONTENT));
                    other_type_layout.addView(otherType_text);
                    linearLayout.addView(other_type_layout, 1 + linearLayout.indexOfChild(rootView.findViewById(R.id.typeInputLayout)));

                    submit_otherType = new MaterialButton(rootView.getContext(),
                            null, R.attr.materialButtonOutlinedStyle);
                    submit_otherType.setText(R.string.otherType_lbl);
                    submit_otherType.setLayoutParams(new LinearLayout.LayoutParams(udiEditText.getWidth(),
                            ViewGroup.LayoutParams.WRAP_CONTENT));
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
                String selected = (String) adapterView.getItemAtPosition(i);
                final TextInputLayout other_physicaloc_layout;
                if (selected.equals("Other")) {
                    chosenLocation = true;
                    other_physicaloc_layout = new TextInputLayout(rootView.getContext(), null,
                            R.style.Widget_MaterialComponents_TextInputLayout_OutlinedBox);
                    other_physicaloc_layout.setHint("Enter physical location");
                    other_physicaloc_layout.setId(View.generateViewId());
                    other_physicaloc_layout.setBoxBackgroundMode(TextInputLayout.BOX_BACKGROUND_OUTLINE);
                    otherPhysicalLoc_text = new TextInputEditText(other_physicaloc_layout.getContext());
                    otherPhysicalLoc_text.setLayoutParams(new LinearLayout.LayoutParams(udiEditText.getWidth(), ViewGroup.LayoutParams.WRAP_CONTENT));
                    other_physicaloc_layout.addView(otherPhysicalLoc_text);
                    linearLayout.addView(other_physicaloc_layout, 1 + linearLayout.indexOfChild(rootView.findViewById(R.id.physicalLocationLayout)));

                    submit_otherPhysicalLoc = new MaterialButton(rootView.getContext(),
                            null, R.attr.materialButtonOutlinedStyle);
                    submit_otherPhysicalLoc.setText(R.string.submitLocation_lbl);
                    submit_otherPhysicalLoc.setLayoutParams(new LinearLayout.LayoutParams(udiEditText.getWidth(),
                            ViewGroup.LayoutParams.WRAP_CONTENT));
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
                            if (chosenLocation) {
                                linearLayout.removeViewAt(1 + linearLayout.indexOfChild(rootView.findViewById(R.id.physicalLocationLayout)));
                                linearLayout.removeViewAt(1 + linearLayout.indexOfChild(rootView.findViewById(R.id.physicalLocationLayout)));
                            }

                        }
                    });
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


        // TODO update to correct position, uniform style
        // when clicked add one more additional field for Patient ID
        addPatient.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                patientidAdded++;
                TextInputLayout patient_id_layout = new TextInputLayout(rootView.getContext(), null,
                        R.style.Widget_MaterialComponents_TextInputLayout_OutlinedBox);
                patient_id_layout.setHint("Enter patient ID");
                patient_id_layout.setBoxBackgroundMode(TextInputLayout.BOX_BACKGROUND_OUTLINE);
                patient_id = new TextInputEditText(patient_id_layout.getContext());
                patient_id.setLayoutParams(new LinearLayout.LayoutParams(udiEditText.getWidth(), ViewGroup.LayoutParams.WRAP_CONTENT));
                patient_id_layout.addView(patient_id);
                linearLayout.addView(patient_id_layout, 2 + linearLayout.indexOfChild(itemUsed));
            }
        });

        // when clicked remove one added field "Patient ID"
        removePatient.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (patientidAdded > 0) {
                    linearLayout.removeViewAt(2 + linearLayout.indexOfChild(itemUsed));
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
//                    addPatient = new MaterialButton(rootView.getContext(),
//                            null, R.attr.materialButtonOutlinedStyle);
//                    addPatient.setText(R.string.addID_lbl);
//                    addPatient.setLayoutParams(new LinearLayout.LayoutParams(udiEditText.getWidth(),
//                            ViewGroup.LayoutParams.WRAP_CONTENT));
//
//                    removePatient = new MaterialButton(rootView.getContext(),
//                            null, R.attr.materialButtonOutlinedStyle);
//                    removePatient.setText(R.string.removeID_lbl);
//                    removePatient.setLayoutParams(new LinearLayout.LayoutParams(udiEditText.getWidth(),
//                            ViewGroup.LayoutParams.WRAP_CONTENT));


//                    linearLayout.addView(addPatient, 2 + linearLayout.indexOfChild(itemUsed));
//                    linearLayout.addView(removePatient, 3 + linearLayout.indexOfChild(itemUsed));

                    // if users changes from reusable to single us removes all unnecessary fields.
                } else {
                    addPatient.setVisibility(View.GONE);
                    removePatient.setVisibility(View.GONE);
//                    linearLayout.removeViewAt(linearLayout.indexOfChild(addPatient));
//                    linearLayout.removeViewAt(linearLayout.indexOfChild(removePatient));
//                    while (patientidAdded > 0) {
//                        linearLayout.removeViewAt(2 + linearLayout.indexOfChild(itemUsed));
//                        patientidAdded--;
//                    }

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
        expiration_textLayout.setEndIconOnClickListener(new View.OnClickListener() {
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
                saveData(rootView);
            }
        });

        String barcode = getArguments().getString("barcode");
        udiEditText.setText(barcode);

        autoPopulate();

        return rootView;
    }

    // method for saving data to firebase cloud firestore
    public void saveData(View view) {

        //assigning user input values to string parameters
        // naming each document with barcode number of the equipment
        /* each document needs (which will include an equipment) a document number. I am assigning
        barcode number of the equipment to a document number. it is possible to have random id as
        a document number as well
         */

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
        String procedure_used_str = procedure_used.getText().toString();
        String procedure_date_str = procedureDate.getText().toString();
        String amount_used_str = amountUsed.getText().toString();
        String patient_id_str = patient_id.getText().toString();

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

       // di identifiers, hardcoded KEY names need to be changed
        Map<String, Object> diDoc = new HashMap<>();
        diDoc.put("name",name_str);
        diDoc.put("equipment_type",type_str);
        diDoc.put("company",company_str);
        diDoc.put("di",di_str);
        diDoc.put("site_name",site_name_str);
        DocumentReference diRef = db.collection("Networks").document("Network1")
                .collection("Sites").document("Hospital 1").collection("Hospital 1 Departments")
                .document("Department 1").collection("Department 1 dis").document(di_str);
        diRef.set(diDoc);

        // udi identifiers
        udiDocument = new InventoryTemplate(barcode_str, name_str, type_str,
                company_str, isUsed,radioButtonVal,procedure_used_str, procedure_date_str, amount_used_str,patient_id_str,
                number_added_str,di_str,lotNumber_str, expiration_str,
                quantity_str, site_name_str, physical_location_str,currentDateTime_str, notes_str);

        DocumentReference udiRef = db.collection("Networks").document("Network1")
        .collection("Sites").document("Hospital 1").collection("Hospital 1 Departments")
        .document("Department 1").collection("Department 1 dis").document(di_str)
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
    }


    public void autoPopulate() {
//        String udi = "(01)00885672101114(17)180401(10)DP02149";
//        String udi = "01008856721011141718040110DP02149";
//        String udi = "01008844501987111722053110K1147572";   // no device found for di
//        String udi = "01049873507728791719083110170906";
        String udi = udiEditText.getText().toString();
        Log.d(TAG, udi);

        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(parent);
        String url = "https://accessgudid.nlm.nih.gov/api/v2/devices/lookup.json?udi=";

        url = url + udi;
//        Log.d(TAG, "URL: " + url);

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