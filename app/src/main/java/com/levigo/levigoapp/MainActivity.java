package com.levigo.levigoapp;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.Gson;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.journeyapps.barcodescanner.CaptureActivity;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int RC_HANDLE_CAMERA_PERM = 1;


    private FirebaseFirestore levigoDb = FirebaseFirestore.getInstance() ;
    private CollectionReference inventoryRef = levigoDb.collection("Inventory") ;


    private RecyclerView inventoryScroll ;
    private RecyclerView.Adapter iAdapter ;
    private RecyclerView.LayoutManager iLayoutManager ;
    private List<String> names = new LinkedList<>();

//    private TextView mTextView;
//    private ImageView mImageView;
    private FloatingActionButton mAdd;
    private FloatingActionButton mAdd2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        inventoryScroll = findViewById(R.id.inventoryScroll);
        mAdd = findViewById(R.id.main_add);
//        mTextView = findViewById(R.id.textView);
//        mImageView = findViewById(R.id.imageView);

        inventoryScroll.setHasFixedSize(true);


        mAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startScanner();
            }
        });

//        mAdd2 = findViewById(R.id.main_add3);
//        mAdd2.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Log.d(TAG, "there");
////                new ItemDetailFragment();
//                FragmentManager fragmentManager = getSupportFragmentManager();
//                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
//                ItemDetailFragment fragment = new ItemDetailFragment();
//                fragmentTransaction.add(R.id.fragment_container, fragment);
//                fragmentTransaction.commit();
//            }
//        });


        Toolbar mToolbar = findViewById(R.id.main_toolbar);
        setSupportActionBar(mToolbar);

        getPermissions();
        initInventory();
    }

    private void startScanner() {
        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.setCaptureActivity(CaptureActivity.class);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES);
        integrator.setBarcodeImageEnabled(true);
        integrator.initiateScan();
    }

    private void getPermissions() {
        final String[] permissions = new String[]{Manifest.permission.CAMERA};
        if (checkSelfPermission(permissions[0]) != PackageManager.PERMISSION_GRANTED){
            requestPermissions(permissions, RC_HANDLE_CAMERA_PERM);
        }
    }

    private void initInventory() {
        iLayoutManager = new LinearLayoutManager(this);
        inventoryScroll.setLayoutManager(iLayoutManager);
        iAdapter = new InventoryViewAdapter(names);
        inventoryScroll.setAdapter(iAdapter);
        inventoryRef.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) throws NullPointerException {
                if (e != null) {
                    System.err.println("Listen failed: " + e);
                    return;
                }

                assert queryDocumentSnapshots != null;
                for(DocumentChange dc : queryDocumentSnapshots.getDocumentChanges()) {
                    String di = dc.getDocument().getString("di");
                    switch (dc.getType()) {
                        case ADDED:
                            Log.d(TAG, "add");
                            names.add(di);
                            break;
                        case REMOVED:
                            Log.d(TAG, "remove");
                            for (int i = 0; i < names.size(); ++i) {
                                if (names.get(i).equals(di)) {
                                    names.remove(i);
                                    break;
                                }
                            }
                            break;
                        case MODIFIED:
                            Log.d(TAG, "modify");
                            for (int i = 0; i < names.size(); ++i) {
                                if (names.get(i).equals(di)) {
                                    names.set(i, di);
                                }
                            }
                            break;
                    }
                }
                iAdapter.notifyDataSetChanged();
            }
        });
    }
    private void addItem(String json) {
        HashMap<String, String> data = new Gson().fromJson(json, HashMap.class);
        String di = data.get("di");
        if(di == null) di = "UNKNOWN DI";
        inventoryRef.document(di).set(data).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.d(TAG, "Added Successfully");
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if(result != null){
            String contents = result.getContents();
            if(contents != null) {
                Toast.makeText(this, "Success: Scanned " + result.getFormatName(), Toast.LENGTH_LONG).show();

                RequestQueue queue = Volley.newRequestQueue(this);

                String getInfo = null;
                try {
                    getInfo = UDI.URL + URLEncoder.encode(contents, "UTF-8");
                }
                catch(UnsupportedEncodingException e){
                    e.printStackTrace();
                }
                if(getInfo == null) return;

                StringRequest stringRequest = new StringRequest(Request.Method.GET, getInfo,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                Log.d(TAG,response);
                                addItem(response);
                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                    }
                });

                queue.add(stringRequest);
            }
            if(result.getBarcodeImagePath() != null) {
                Log.d(TAG, "" + result.getBarcodeImagePath());
//                mImageView.setImageBitmap(BitmapFactory.decodeFile(result.getBarcodeImagePath()));
                //maybe add image to firebase storage
            }
        }
        else{
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode != RC_HANDLE_CAMERA_PERM) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
        else if(!(grantResults.length != 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
            finish();
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_toolbar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch(item.getItemId()) {
            case R.id.manual_entry:
//                Intent myIntent = new Intent(this, FirebaseActivity.class);
//                startActivityForResult(myIntent, 0);
                FragmentManager fragmentManager = getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                ItemDetailFragment fragment = new ItemDetailFragment();
                fragmentTransaction.add(R.id.fragment_container, fragment);
                fragmentTransaction.commit();
                return true;
            case R.id.logout:
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
                return true;
            case R.id.settings:
                Toast.makeText(this, "Not implemented yet", Toast.LENGTH_SHORT).show();
                return true;
            default:
            return super.onOptionsItemSelected(item);
        }
    }

    public static class ItemDetailFragment extends Fragment {

        // Firebase database
        private FirebaseFirestore db = FirebaseFirestore.getInstance();
        InventoryTemplate in;

        private static final String TAG = ItemDetailFragment.class.getSimpleName();

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
        private Button mSave;



        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            // Inflate the layout for this fragment

            final View rootView = inflater.inflate(R.layout.fragment_itemdetail, container, false);
            final Calendar myCalendar = Calendar.getInstance();
            barcode = rootView.findViewById(R.id.barcode_editText);
            name = rootView.findViewById(R.id.name_editText);
            equipment_type = rootView.findViewById(R.id.equipment_editText);
            manufacturer = rootView.findViewById(R.id.manufacturer_editText);
            procedure_used =  rootView.findViewById(R.id.procedure_editText);
            procedure_date = rootView.findViewById(R.id.procedureDate_editText);
            patient_id = rootView.findViewById(R.id.patientID_editText);
            expiration = rootView.findViewById(R.id.expiration_editText);
            hospital_name = rootView.findViewById(R.id.hospitalName_editText);
            physical_location = rootView.findViewById(R.id.physicalLocation_editText);
            notes = rootView.findViewById(R.id.notes_editText);
            mSave = rootView.findViewById(R.id.save_button);
            mSave.setEnabled(false);

            // datepicker for Expiration field
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

            // datepicker for Procedure date field
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

            procedure_date.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    new DatePickerDialog(view.getContext(), date_proc, myCalendar
                            .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                            myCalendar.get(Calendar.DAY_OF_MONTH)).show();

                }
            });

            expiration.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    new DatePickerDialog(view.getContext(), date_exp, myCalendar
                            .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                            myCalendar.get(Calendar.DAY_OF_MONTH)).show();
                }
            });

            // disabling save button if required fields are empty
            TextWatcher textWatcher = new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    for (EditText et  : new EditText[] {barcode,name,equipment_type,manufacturer,hospital_name,
                        physical_location}) {
                            if(et.getText().toString().isEmpty()){
                                mSave.setEnabled(false);
                                et.setError("Please enter " + et.getHint().toString().toLowerCase());
                                return;
                        }
                    }
                    mSave.setEnabled(true);

                }

                @Override
                public void afterTextChanged(Editable editable) {

                }
            };
            barcode.addTextChangedListener(textWatcher);
            name.addTextChangedListener(textWatcher);
            equipment_type.addTextChangedListener(textWatcher);
            manufacturer.addTextChangedListener(textWatcher);


            mSave.setOnClickListener( new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    saveData(rootView);
                }
            });
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
            String barcode_str = barcode.getText().toString();
            DocumentReference equipRef = db.document("Inventory/" + barcode_str);
            String name_str = name.getText().toString();
            String equipment_type_str = equipment_type.getText().toString();
            String manufacturer_str = equipment_type.getText().toString();
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
    }

}
