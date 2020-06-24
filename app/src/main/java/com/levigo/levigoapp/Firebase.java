package com.levigo.levigoapp;
import java.util.Calendar;
import android.app.Activity;
import android.widget.EditText;
import android.os.Bundle;

import androidx.annotation.Nullable;

import com.google.firebase.firestore.FirebaseFirestore;

public class Firebase extends Activity {
    // Firebase database
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    private static final String TAG = "MainActivity";
    // TAGS FOR THE FIELD NAMES
    private String barcode_field = "BARCODE";
    private String name_field = "NAME";
    private String equipment_type_field = "EQUIPMENT_TYPE";
    private String manufacturer_field = "MANUFACTURER";
    private String procedure_used_field = "PROCEDURE";
    private String procedure_date_field = "PROCEDURE_DATE";
    private String patiend_id_field = "PATIENT_ID";
    private String expiration_field = "EXPIRATION";
    private String quantity_field = "QUANTITY";
    private String hospital_name_field = "HOSPITAL_NAME";
    private String physical_location_field = "PHYSICAL_LOCATION";
    private String notes_field = "NOTES";
    private String current_date_time_field = "CURRENT_TIME";

    // USER INPUT VALUES
    private EditText barcode;
    private EditText name;
    private EditText equipment_type;
    private EditText manufacturer;
    private EditText procedure_used;
    private EditText procedure_date;
    private EditText patient_id;
    private EditText expiration;
    private EditText quantity;
    private String current_date_time;
    private EditText hospital_name;
    private EditText physical_location;
    private EditText notes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entry);
    }
}
