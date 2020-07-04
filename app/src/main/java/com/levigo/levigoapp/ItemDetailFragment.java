package com.levigo.levigoapp;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.res.ColorStateList;
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
import androidx.core.content.ContextCompat;
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
import java.util.Locale;
import java.util.TimeZone;

public class ItemDetailFragment extends Fragment {

    LinearLayout linearLayout;

    // Firebase database
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    InventoryTemplate in;

    private static final String TAG = ItemDetailFragment.class.getSimpleName();

    private Activity parent;

    // USER INPUT VALUES
    private TextInputEditText udiEditText;
    private TextInputEditText nameEditText;
    private AutoCompleteTextView equipmentType;
    private TextInputEditText company;
    private TextInputEditText procedure_used;
    private TextInputEditText otherType_text;
    private TextInputEditText procedure_date;
    private TextInputEditText patient_id;
    private TextInputEditText productId;
    private TextInputEditText expiration;
    private TextInputEditText quantity;
    private TextInputEditText lotNumber;
    private TextInputEditText hospitalName;
    private TextInputEditText physicalLocation;
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
    private SwitchMaterial itemUsed;
    private ImageButton backButton;
    private Button rescanButton;
    private int patientidAdded = 0;

    private Button autoPopulateButton;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        final View rootView = inflater.inflate(R.layout.fragment_itemdetail, container, false);
        final Calendar myCalendar = Calendar.getInstance();

        parent = getActivity();

        linearLayout = rootView.findViewById(R.id.linear_layout);
        udiEditText = (TextInputEditText) rootView.findViewById(R.id.detail_udi);
        nameEditText = (TextInputEditText) rootView.findViewById(R.id.detail_name);
        equipmentType = (AutoCompleteTextView) rootView.findViewById(R.id.detail_type);
        company = (TextInputEditText) rootView.findViewById(R.id.detail_company);
        expiration = (TextInputEditText) rootView.findViewById(R.id.detail_expiration_date);
        hospitalName = (TextInputEditText) rootView.findViewById(R.id.detail_site_location);
        physicalLocation = (TextInputEditText) rootView.findViewById(R.id.detail_physical_location);
        notes = (TextInputEditText) rootView.findViewById(R.id.detail_notes);
        lotNumber = (TextInputEditText) rootView.findViewById(R.id.detail_lot_number);
        numberAdded = (TextInputEditText) rootView.findViewById(R.id.detail_number_added);
        productId = (TextInputEditText) rootView.findViewById(R.id.detail_di);
        currentDateTime = (TextInputEditText) rootView.findViewById(R.id.detail_date_time);
        expiration_textLayout = (TextInputLayout) rootView.findViewById(R.id.expiration_date_string2);
        timeLayout = (TextInputLayout) rootView.findViewById(R.id.time_layout);
        itemUsed = rootView.findViewById(R.id.detail_used_switch);
        saveButton = rootView.findViewById(R.id.detail_save_button);
        backButton = rootView.findViewById(R.id.detail_back_button);
        rescanButton = rootView.findViewById(R.id.detail_rescan_button);
        autoPopulateButton = rootView.findViewById(R.id.detail_autopop_button);

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
                final TextInputLayout other_type_layout;
                if (selected.equals("Other")) {
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


                } else {
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



        // Checks which buttons is chosen
        multiUse.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                // if reusable button is chosen, gives an user option to add multiple patient IDs
                if (b) {
                    addPatient = new MaterialButton(rootView.getContext(),
                            null, R.attr.materialButtonOutlinedStyle);
                    addPatient.setText(R.string.addID_lbl);
                    addPatient.setLayoutParams(new LinearLayout.LayoutParams(udiEditText.getWidth(),
                            ViewGroup.LayoutParams.WRAP_CONTENT));

                    removePatient = new MaterialButton(rootView.getContext(),
                            null, R.attr.materialButtonOutlinedStyle);
                    removePatient.setText(R.string.removeID_lbl);
                    removePatient.setLayoutParams(new LinearLayout.LayoutParams(udiEditText.getWidth(),
                            ViewGroup.LayoutParams.WRAP_CONTENT));

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
                            linearLayout.addView(patient_id_layout, 4 + linearLayout.indexOfChild(itemUsed));
                        }
                    });

                    // when clicked remove one added field "Patient ID"
                    removePatient.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if (patientidAdded > 0) {
                                linearLayout.removeViewAt(5 + linearLayout.indexOfChild(itemUsed));
                                patientidAdded--;
                            }
                        }
                    });
                    linearLayout.addView(addPatient, 5 + linearLayout.indexOfChild(itemUsed));
                    linearLayout.addView(removePatient, 6 + linearLayout.indexOfChild(itemUsed));

                    // if users changes from reusable to single us removes all unnecessary fields.
                } else {
                    linearLayout.removeViewAt(linearLayout.indexOfChild(addPatient));
                    linearLayout.removeViewAt(linearLayout.indexOfChild(removePatient));
                    while (patientidAdded > 0) {
                        linearLayout.removeViewAt(5 + linearLayout.indexOfChild(itemUsed));
                        patientidAdded--;
                    }

                }
            }
        });

        // programmatically creates four additional fields if item is used
//        TextInputLayout procedure_layout = new TextInputLayout(rootView.getContext(), null,
//                R.style.Widget_MaterialComponents_TextInputLayout_OutlinedBox);
//        procedure_layout.setHint("Enter procedure");
//        procedure_layout.setBoxBackgroundMode(TextInputLayout.BOX_BACKGROUND_OUTLINE);
//        procedure_used = new TextInputEditText(procedure_layout.getContext());
//        procedure_used.setLayoutParams(new LinearLayout.LayoutParams(udiEditText.getWidth(), ViewGroup.LayoutParams.WRAP_CONTENT));
//        procedure_layout.addView(procedure_used);
//        linearLayout.addView(procedure_layout, 2 + linearLayout.indexOfChild(itemUsed));
//
//        TextInputLayout procedure_dateTime_layout = new TextInputLayout(rootView.getContext(), null,
//                R.style.Widget_MaterialComponents_TextInputLayout_OutlinedBox);
//        procedure_dateTime_layout.setHint("Enter procedure date");
//        procedure_dateTime_layout.setBoxBackgroundMode(TextInputLayout.BOX_BACKGROUND_OUTLINE);
//        procedure_date = new TextInputEditText(procedure_dateTime_layout.getContext());
//        procedure_date.setLayoutParams(new LinearLayout.LayoutParams(udiEditText.getWidth(), ViewGroup.LayoutParams.WRAP_CONTENT));
//        procedure_dateTime_layout.addView(procedure_date);
//        procedure_dateTime_layout.setEndIconMode(TextInputLayout.END_ICON_CUSTOM);
//        procedure_dateTime_layout.setEndIconDrawable(R.drawable.calendar);
//        procedure_dateTime_layout.setEndIconTintList(ColorStateList.valueOf(ContextCompat
//                .getColor(rootView.getContext(), R.color.colorPrimary)));
//        linearLayout.addView(procedure_dateTime_layout, 3 + linearLayout.indexOfChild(itemUsed));

        //TODO not done
        TextInputLayout procedureDateTimeLayout = rootView.findViewById(R.id.textinputlayout_proceduredatetime);
        final TextInputEditText procedureDate = rootView.findViewById(R.id.edittext_procedure_date);
        final DatePickerDialog.OnDateSetListener date_proc = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
                myCalendar.set(Calendar.YEAR, i);
                myCalendar.set(Calendar.MONTH, i1);
                myCalendar.set(Calendar.DAY_OF_MONTH, i2);
                String myFormat = "yyyy/MM/dd";
                SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
                procedureDate.setText(sdf.format(myCalendar.getTime()));
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


//        TextInputLayout patient_id_layout = new TextInputLayout(rootView.getContext(), null,
//                R.style.Widget_MaterialComponents_TextInputLayout_OutlinedBox);
//        patient_id_layout.setHint("Enter patient ID");
//        patient_id_layout.setBoxBackgroundMode(TextInputLayout.BOX_BACKGROUND_OUTLINE);
//        patient_id = new TextInputEditText(patient_id_layout.getContext());
//        patient_id.setLayoutParams(new LinearLayout.LayoutParams(udiEditText.getWidth(), ViewGroup.LayoutParams.WRAP_CONTENT));
//        patient_id_layout.addView(patient_id);
//        linearLayout.addView(patient_id_layout, 4 + linearLayout.indexOfChild(itemUsed));
//
//        TextInputLayout numbersUsed_layout = new TextInputLayout(rootView.getContext(), null,
//                R.style.Widget_MaterialComponents_TextInputLayout_OutlinedBox);
//        numbersUsed_layout.setHint("Enter numbers used");
//        numbersUsed_layout.setBoxBackgroundMode(TextInputLayout.BOX_BACKGROUND_OUTLINE);
//        numberUsed = new TextInputEditText(numbersUsed_layout.getContext());
//        numberUsed.setLayoutParams(new LinearLayout.LayoutParams(udiEditText.getWidth(), ViewGroup.LayoutParams.WRAP_CONTENT));
//        numbersUsed_layout.addView(numberUsed);
//        linearLayout.addView(numbersUsed_layout, 5 + linearLayout.indexOfChild(itemUsed));


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
                expiration.setText(sdf.format(myCalendar.getTime()));
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
        String equipment_type_str = equipmentType.getText().toString();
        String manufacturer_str = equipmentType.getText().toString();
//        String procedure_used_str = procedure_used.getText().toString();
//        String procedure_date_str = procedure_date.getText().toString();
//        String patient_id_str = patient_id.getText().toString();
        String expiration_str = expiration.getText().toString();
        String quantity_str = "2"; // temporarily
        String current_date_time = Calendar.getInstance().getTime().toString();
        String hospital_name_str = hospitalName.getText().toString();
        String physical_location_str = physicalLocation.getText().toString();
        String notes_str = notes.getText().toString();

        in = new InventoryTemplate(barcode_str, name_str, equipment_type_str,
                manufacturer_str, "", "", "", expiration_str,
                quantity_str, hospital_name_str, current_date_time, physical_location_str, notes_str);

        DocumentReference equipRef = db.document("Inventory/" + barcode_str);

        //saving data of InventoryTemplate to database
        equipRef.set(in)
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
                        Toast.makeText(getActivity(), "Error!", Toast.LENGTH_SHORT).show();
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
                            productId.setText(udi.getString("di"));
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