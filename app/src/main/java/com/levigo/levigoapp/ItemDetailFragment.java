package com.levigo.levigoapp;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.res.ColorStateList;
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
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
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
import java.util.Date;
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
    private AutoCompleteTextView equipment_type;
    private TextInputEditText company;
    private TextInputEditText procedure_used;
    private TextInputEditText otherType_text;
    private TextInputEditText procedure_date;
    private TextInputEditText patient_id;
    private TextInputEditText product_id;
    private TextInputEditText expiration;
    private TextInputEditText quantity;
    private TextInputEditText lotNumber;
    private TextInputEditText hospital_name;
    private TextInputEditText physical_location;
    private TextInputEditText notes;
    private TextInputEditText numberUsed;
    private TextInputEditText currentDateTime;
    private TextInputEditText number_added;
    private TextInputLayout expiration_textLayout;
    private TextInputLayout time_layout;


    private Button mSave;
    private MaterialButton addPatient;
    private MaterialButton removePatient;
    private MaterialButton submit_otherType;
    private SwitchMaterial item_used;
    private ImageButton back_button;
    private Button rescan_button;
    private int patientidAdded  = 0;

    // TODO: delete later
    private Button autoPopulateButton;



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        final View rootView = inflater.inflate(R.layout.fragment_itemdetail, container, false);
        final Calendar myCalendar = Calendar.getInstance();

        parent = getActivity();

        // TODO: refactor, convert to camel case
        linearLayout = rootView.findViewById(R.id.linear_layout);
        udiEditText = (TextInputEditText) rootView.findViewById(R.id.detail_udi);
        nameEditText = (TextInputEditText)rootView.findViewById(R.id.detail_name);
        equipment_type = (AutoCompleteTextView) rootView.findViewById(R.id.detail_type);
        company = (TextInputEditText)rootView.findViewById(R.id.detail_company);
        expiration = (TextInputEditText)rootView.findViewById(R.id.detail_expiration_date);
        hospital_name = (TextInputEditText)rootView.findViewById(R.id.detail_site_location);
        physical_location = (TextInputEditText)rootView.findViewById(R.id.detail_physical_location);
        notes = (TextInputEditText)rootView.findViewById(R.id.detail_notes);
        lotNumber =  (TextInputEditText)rootView.findViewById(R.id.detail_lot_number);
        number_added =  (TextInputEditText)rootView.findViewById(R.id.detail_number_added);
        product_id = (TextInputEditText)rootView.findViewById(R.id.detail_di);
        lotNumber = (TextInputEditText)rootView.findViewById(R.id.detail_lot_number);
        currentDateTime = (TextInputEditText)rootView.findViewById(R.id.detail_date_time);
        expiration_textLayout = (TextInputLayout) rootView.findViewById(R.id.expiration_date_string2);
        time_layout = (TextInputLayout) rootView.findViewById(R.id.time_layout);
        item_used = rootView.findViewById(R.id.detail_used_switch);
        mSave = rootView.findViewById(R.id.detail_save_button);
        back_button = rootView.findViewById(R.id.detail_back_button);
        rescan_button = rootView.findViewById(R.id.detail_rescan_button);
        autoPopulateButton = rootView.findViewById(R.id.detail_autopop_button);

        item_used.setChecked(false);


        // Dropdown menu for Type field
        final ArrayList<String> TYPES = new ArrayList<>(Arrays.asList("Balloon", "Catheter", "Needle", "Dilator", "Drainage Bag",
                "Biliary Stent", "Wire", "Imaging/US","Mask","Picc Line","Scalpel","Sheath","Snare Kit",
                "Stent","Sterile Tray","Stopcock","Tube","Other"));
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
                if(selected.equals("Other")){
                    other_type_layout = new TextInputLayout(rootView.getContext(), null,
                            R.style.Widget_MaterialComponents_TextInputLayout_OutlinedBox);
                    other_type_layout.setHint("Enter type");
                    other_type_layout.setId(View.generateViewId());
                    other_type_layout.setBoxBackgroundMode(TextInputLayout.BOX_BACKGROUND_OUTLINE);
                    otherType_text =  new TextInputEditText(other_type_layout.getContext());
                    otherType_text.setLayoutParams(new LinearLayout.LayoutParams(udiEditText.getWidth(), ViewGroup.LayoutParams.WRAP_CONTENT));
                    other_type_layout.addView(otherType_text);
                    linearLayout.addView(other_type_layout,1 + linearLayout.indexOfChild(rootView.findViewById(R.id.typeInputLayout)));

                    submit_otherType = new MaterialButton(rootView.getContext(),
                            null, R.attr.materialButtonOutlinedStyle);
                    submit_otherType.setText(R.string.otherType_lbl);
                    submit_otherType.setLayoutParams(new LinearLayout.LayoutParams(udiEditText.getWidth(),
                            ViewGroup.LayoutParams.WRAP_CONTENT));
                    linearLayout.addView(submit_otherType,2 + linearLayout.indexOfChild(rootView.findViewById(R.id.typeInputLayout)));

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


                }else {
                    linearLayout.removeViewAt(1 + linearLayout.indexOfChild(rootView.findViewById(R.id.typeInputLayout)));
                    linearLayout.removeViewAt(1 + linearLayout.indexOfChild(rootView.findViewById(R.id.typeInputLayout)));
                }




            }
        });



        autoPopulateButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                autoPopulate();
            }
        });

        //TimePicker dialog pops up when clicked on the icon
        time_layout.setEndIconOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar mcurrentTime = Calendar.getInstance();
                int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
                int minute = mcurrentTime.get(Calendar.MINUTE);
                TimePickerDialog mTimePicker;
                mTimePicker = new TimePickerDialog(rootView.getContext(), new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                        currentDateTime.setText(String.format(Locale.US,"%d:%d: 00 %s", selectedHour,
                                selectedMinute, TimeZone.getDefault().getID()));
                    }
                }, hour, minute,true);
                mTimePicker.setTitle("Select Time");
                mTimePicker.show();
            }
        });

        // going back to the scanner view
        rescan_button.setOnClickListener(new View.OnClickListener() {
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
        back_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            if(parent != null)
                parent.onBackPressed();
            }
        });


        // checking switch icon

        item_used.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b){
                    RadioGroup item_usable = new RadioGroup(rootView.getContext());
                    item_usable.setOrientation(RadioGroup.HORIZONTAL);

                    RadioButton singleUse  = new RadioButton(rootView.getContext());
                    singleUse.setText(R.string.SingleUse_lbl);
                    singleUse.setId(View.generateViewId());
                    item_usable.addView(singleUse);

                    RadioButton multiUse  = new RadioButton(rootView.getContext());
                    multiUse.setText(R.string.reusable_lbl);
                    multiUse.setId(View.generateViewId());
                    item_usable.addView(multiUse);

                    linearLayout.addView(item_usable,1 + linearLayout.indexOfChild(item_used));


                    // Checks which buttons is chosen
                    multiUse.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                            // if reusable button is chosen, gives an user option to add multiple patient IDs
                            if(b){
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
                                        patient_id =  new TextInputEditText(patient_id_layout.getContext());
                                        patient_id.setLayoutParams(new LinearLayout.LayoutParams(udiEditText.getWidth(), ViewGroup.LayoutParams.WRAP_CONTENT));
                                        patient_id_layout.addView(patient_id);
                                        linearLayout.addView(patient_id_layout,4 + linearLayout.indexOfChild(item_used));
                                    }
                                });

                                // when clicked remove one added field "Patient ID"
                                removePatient.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        if(patientidAdded > 0){
                                            linearLayout.removeViewAt(5 + linearLayout.indexOfChild(item_used));
                                            patientidAdded--;
                                        }
                                    }
                                });
                                linearLayout.addView(addPatient,5 + linearLayout.indexOfChild(item_used));
                                linearLayout.addView(removePatient,6 + linearLayout.indexOfChild(item_used));

                                // if users changes from reusable to single us removes all unnecessary fields.
                            }else{
                                linearLayout.removeViewAt(linearLayout.indexOfChild(addPatient));
                                linearLayout.removeViewAt(linearLayout.indexOfChild(removePatient));
                                while(patientidAdded > 0) {
                                    linearLayout.removeViewAt(5 + linearLayout.indexOfChild(item_used));
                                    patientidAdded--;
                                }

                            }
                        }
                    });

                    // programmatically creates four additional fields if item is used
                    TextInputLayout procedure_layout = new TextInputLayout(rootView.getContext(), null,
                            R.style.Widget_MaterialComponents_TextInputLayout_OutlinedBox);
                    procedure_layout.setHint("Enter procedure");
                    procedure_layout.setBoxBackgroundMode(TextInputLayout.BOX_BACKGROUND_OUTLINE);
                    procedure_used =  new TextInputEditText(procedure_layout.getContext());
                    procedure_used.setLayoutParams(new LinearLayout.LayoutParams(udiEditText.getWidth(), ViewGroup.LayoutParams.WRAP_CONTENT));
                    procedure_layout.addView(procedure_used);
                    linearLayout.addView(procedure_layout,2 + linearLayout.indexOfChild(item_used));

                    TextInputLayout procedure_dateTime_layout = new TextInputLayout(rootView.getContext(),null,
                            R.style.Widget_MaterialComponents_TextInputLayout_OutlinedBox);
                    procedure_dateTime_layout.setHint("Enter procedure date");
                    procedure_dateTime_layout.setBoxBackgroundMode(TextInputLayout.BOX_BACKGROUND_OUTLINE);
                    procedure_date =  new TextInputEditText(procedure_dateTime_layout.getContext());
                    procedure_date.setLayoutParams(new LinearLayout.LayoutParams(udiEditText.getWidth(), ViewGroup.LayoutParams.WRAP_CONTENT));
                    procedure_dateTime_layout.addView(procedure_date);
                    procedure_dateTime_layout.setEndIconMode(TextInputLayout.END_ICON_CUSTOM);
                    procedure_dateTime_layout.setEndIconDrawable(R.drawable.calendar);
                    procedure_dateTime_layout.setEndIconTintList(ColorStateList.valueOf(ContextCompat
                            .getColor(rootView.getContext(),R.color.colorPrimary)));
                    linearLayout.addView(procedure_dateTime_layout,3 + linearLayout.indexOfChild(item_used));
                    final DatePickerDialog.OnDateSetListener date_proc = new DatePickerDialog.OnDateSetListener() {
                        @Override
                        public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
                            myCalendar.set(Calendar.YEAR, i);
                            myCalendar.set(Calendar.MONTH, i1);
                            myCalendar.set(Calendar.DAY_OF_MONTH, i2);
                            String myFormat = "yyyy/MM/dd";
                            SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
                            procedure_date.setText(sdf.format(myCalendar.getTime()));
                        }
                    };
                    procedure_dateTime_layout.setEndIconOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            new DatePickerDialog(view.getContext(), date_proc, myCalendar
                                    .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                                    myCalendar.get(Calendar.DAY_OF_MONTH)).show();
                        }
                    });

                    TextInputLayout patient_id_layout = new TextInputLayout(rootView.getContext(), null,
                            R.style.Widget_MaterialComponents_TextInputLayout_OutlinedBox);
                    patient_id_layout.setHint("Enter patient ID");
                    patient_id_layout.setBoxBackgroundMode(TextInputLayout.BOX_BACKGROUND_OUTLINE);
                    patient_id =  new TextInputEditText(patient_id_layout.getContext());
                    patient_id.setLayoutParams(new LinearLayout.LayoutParams(udiEditText.getWidth(), ViewGroup.LayoutParams.WRAP_CONTENT));
                    patient_id_layout.addView(patient_id);
                    linearLayout.addView(patient_id_layout,4 + linearLayout.indexOfChild(item_used));

                    TextInputLayout numbersUsed_layout = new TextInputLayout(rootView.getContext(), null,
                            R.style.Widget_MaterialComponents_TextInputLayout_OutlinedBox);
                    numbersUsed_layout.setHint("Enter numbers used");
                    numbersUsed_layout.setBoxBackgroundMode(TextInputLayout.BOX_BACKGROUND_OUTLINE);
                    numberUsed =  new TextInputEditText(numbersUsed_layout.getContext());
                    numberUsed.setLayoutParams(new LinearLayout.LayoutParams(udiEditText.getWidth(), ViewGroup.LayoutParams.WRAP_CONTENT));
                    numbersUsed_layout.addView(numberUsed);
                    linearLayout.addView(numbersUsed_layout,5 + linearLayout.indexOfChild(item_used));

                    //programmatically removes four additional fields if users switches from reusable to single use
                }else{
                    linearLayout.removeViewAt(1 + linearLayout.indexOfChild(item_used));
                    linearLayout.removeViewAt(1 + linearLayout.indexOfChild(item_used));
                    linearLayout.removeViewAt(1 + linearLayout.indexOfChild(item_used));
                    linearLayout.removeViewAt(1 + linearLayout.indexOfChild(item_used));
                    linearLayout.removeViewAt(1 + linearLayout.indexOfChild(item_used));
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
//
//            @Override
//            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
//                for (TextInputEditText et  : new TextInputEditText[] {barcode,name,equipment_type,company,hospital_name,
//                        physical_location}) {
//                    if(et.getText().toString().isEmpty()){
//                        mSave.setEnabled(false);
//                        et.setError("Please enter " + et.getHint().toString().toLowerCase());
//                        return;
//                    }
//
//                    String barcode_str = Objects.requireNonNull(barcode.getText()).toString();
//                    for(int j = 0; j < barcode_str.length(); j++) {
//                        if(!(Character.isDigit(barcode_str.charAt(j)) || Character.isLetter(barcode_str.charAt(j))
//                                || barcode_str.charAt(j) == '(' || barcode_str.charAt(j) == ')')){
//                            mSave.setEnabled(false);
//                            // todo: cannot find symbol "et"
//                            et.setError("Barcode entry does not match format. Please check");
//                            return;
//                        }
//                    }
//                    if(barcode_str.charAt(0) == '(' && Character.isDigit(barcode_str.charAt(1))
//                            && Character.isDigit(barcode_str.charAt(2)) && barcode_str.charAt(3) == ')' &&
//                            barcode_str.charAt(18) == '(' && Character.isDigit(barcode_str.charAt(19))
//                            && Character.isDigit(barcode_str.charAt(20)) && barcode_str.charAt(21) == ')'){
//                        for(int j = 4; j < 18; j++){
//                            if(!(Character.isDigit(barcode_str.charAt(j)))) {
//                                mSave.setEnabled(false);
//                                et.setError("Barcode entry does not match format. Please check");
//                                return;
//                            }
//                        }
//                    } else {
//                        mSave.setEnabled(false);
//                        et.setError("Barcode entry does not match format. Please check");
//                        return;
//                    }
//
//                    product_id.setText(barcode_str.substring(0,18));
//                    mSave.setEnabled(true);
//                }
//            }
//
//            @Override
//            public void afterTextChanged(Editable editable) {
//                for (TextInputEditText et  : new TextInputEditText[] {barcode,name,equipment_type,company,hospital_name,
//                        physical_location}) {
//                    if(et.getText().toString().isEmpty()){
//                        mSave.setEnabled(false);
//                        et.setError("Please enter " + et.getHint().toString().toLowerCase());
//                        return;
//                    }
//                }
//                String barcode_str = Objects.requireNonNull(barcode.getText()).toString();
//                for(int i = 0; i < barcode_str.length(); i++) {
//                    if(!(Character.isDigit(barcode_str.charAt(i)) || Character.isLetter(barcode_str.charAt(i))
//                            || barcode_str.charAt(i) == '(' || barcode_str.charAt(i) == ')')){
//                        mSave.setEnabled(false);
////                            et.setError("Barcode entry does not match format. Please check");
//                        return;
//                    }
//                }
//                if(barcode_str.charAt(0) == '(' && Character.isDigit(barcode_str.charAt(1))
//                        && Character.isDigit(barcode_str.charAt(2)) && barcode_str.charAt(3) == ')' &&
//                        barcode_str.charAt(18) == '(' && Character.isDigit(barcode_str.charAt(19))
//                        && Character.isDigit(barcode_str.charAt(20)) && barcode_str.charAt(21) == ')'){
//                    for(int j = 4; j < 18; j++){
//                        if(!(Character.isDigit(barcode_str.charAt(j)))) {
//                            mSave.setEnabled(false);
////                                et.setError("Barcode entry does not match format. Please check");
//                            return;
//                        }
//                    }
//                } else {
//                    mSave.setEnabled(false);
////                        et.setError("Barcode entry does not match format. Please check");
//                    return;
//                }
//
//                product_id.setText(barcode_str.substring(0,18));
//                mSave.setEnabled(true);
//            }
//        };


//        barcode.addTextChangedListener(textWatcher);
//        name.addTextChangedListener(textWatcher);
//        equipment_type.addTextChangedListener(textWatcher);
//        company.addTextChangedListener(textWatcher);


        mSave.setOnClickListener( new View.OnClickListener() {
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
    public void saveData(View view){

        //assigning user input values to string parameters
        // naming each document with barcode number of the equipment
        /* each document needs (which will include an equipment) a document number. I am assigning
        barcode number of the equipment to a document number. it is possible to have random id as
        a document number as well
         */

        Log.d(TAG, "SAVING");
        String barcode_str = udiEditText.getText().toString();
        String name_str = nameEditText.getText().toString();
        String equipment_type_str = equipment_type.getText().toString();
        String manufacturer_str = equipment_type.getText().toString();
//        String procedure_used_str = procedure_used.getText().toString();
//        String procedure_date_str = procedure_date.getText().toString();
//        String patient_id_str = patient_id.getText().toString();
        String expiration_str = expiration.getText().toString();
        String quantity_str = "2"; // temporarily
        String current_date_time = Calendar.getInstance().getTime().toString();
        String hospital_name_str = hospital_name.getText().toString();
        String physical_location_str = physical_location.getText().toString();
        String notes_str  = notes.getText().toString();

        in = new InventoryTemplate(barcode_str,name_str, equipment_type_str,
            manufacturer_str, "", "", "",expiration_str,
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
//        String barcode = "(01)00885672101114(17)180401(10)DP02149";
        String udi = udiEditText.getText().toString();
        Log.d(TAG,udi);

        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(parent);
        String url = "https://accessgudid.nlm.nih.gov/api/v2/devices/lookup.json?udi=";

        url = url + udi;
        Log.d(TAG, "URL: " + url);

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
            new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    JSONObject responseJson = null;
                    try {
                        responseJson = new JSONObject(response);

                        Log.d(TAG, "Response: " + response);
//                                Log.d(TAG, responseJson.toString());

                        JSONObject deviceInfo = responseJson.getJSONObject("gudid").getJSONObject("device");
                        JSONObject udi =  responseJson.getJSONObject("udi");
//                                Log.d(TAG, "DEVICE INFO: " + deviceInfo);
//                                String lotNumberText = responseJson.getJSONObject("udi").getString("lotNumber");
                        lotNumber.setText(udi.getString("lotNumber"));
                        company.setText(deviceInfo.getString("companyName"));
                        expiration.setText(udi.getString("expirationDate"));
                        product_id.setText(udi.getString("di"));
//                                name.setText(responseJson.getJSONObject("gudid").get);
//                                Log.d(TAG, "uDI: " + udi);
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