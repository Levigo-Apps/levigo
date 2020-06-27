package com.levigo.levigoapp;
import java.util.Calendar;
import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.os.Bundle;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

public class Firebase extends Activity {
    // Firebase database
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private DocumentReference equipRef;
    InventoryTemplate in;

    private static final String TAG = "Firebasectivity";

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
    private EditText hospital_name;
    private EditText physical_location;
    private EditText notes;

    // method for saving data to firebase cloud firestore
    public void save_data(View view){

        //assigning user input values to string parameters
        // naming each document with barcode number of the equipment
        /* each document needs (which will include an equipment) a document number. I am assigning
        barcode number of the equipment to a document number. it is possible to have random id as
        a document number as well
         */
        String barcode_str = barcode.getText().toString();
        equipRef = db.document("Inventory/" + barcode_str);

        String name_str = name.getText().toString();
        String equipment_type_str = equipment_type.getText().toString();
        String manufacturer_str = manufacturer.getText().toString();
        String procedure_used_str = procedure_used.getText().toString();
        String procedure_date_str = procedure_date.getText().toString();
        String patient_id_str = patient_id.getText().toString();
        String expiration_str = expiration.getText().toString();
        String quantity_str = "2"; // temporarily
        String current_date_time = Calendar.getInstance().getTime().toString();
        String hospital_name_str = hospital_name.getText().toString();
        String physical_location_str = physical_location.getText().toString();
        String notes_str  = notes.getText().toString();
        in = new InventoryTemplate(barcode_str,name_str, equipment_type_str,
                manufacturer_str,procedure_used_str, procedure_date_str,patient_id_str,expiration_str,
                quantity_str, hospital_name_str, current_date_time, physical_location_str, notes_str);

        //saving data of InventoryTemplate to database
        equipRef.set(in)
                //in case of success
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(Firebase.this, "equipment saved", Toast.LENGTH_SHORT).show();
                    }
                })
                // in case of failure
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(Firebase.this, "Error!", Toast.LENGTH_SHORT).show();
                        Log.d(TAG, e.toString());
                    }
                });
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_entry);
        barcode = findViewById(R.id.barcode_editText);
        name = findViewById(R.id.name_editText);
        equipment_type = findViewById(R.id.equipment_editText);
        manufacturer = findViewById(R.id.manufacturer_editText);
        procedure_used =  findViewById(R.id.procedure_editText);
        procedure_date = findViewById(R.id.procedureDate_editText);
        patient_id = findViewById(R.id.patientID_editText);
        expiration = findViewById(R.id.expiration_editText);
        hospital_name = findViewById(R.id.hospitalName_editText);
        physical_location = findViewById(R.id.physicalLocation_editText);
        notes = findViewById(R.id.notes_editText);
    }
}
