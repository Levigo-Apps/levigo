package com.levigo.levigoapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

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
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.journeyapps.barcodescanner.CaptureActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class AddEquipmentFragment extends Fragment {
    private static final String TAG = AddEquipmentFragment.class.getSimpleName();
    private Activity parent;

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final CollectionReference usersRef = db.collection("users");
    private LinearLayout linearLayout;
    private LinearLayout buttonsLayout;
    private List<HashMap<String, Object>> procedureInfo;

    private String mNetworkId;
    private String mHospitalId;
    private String mHospitalName;
    private String procedureDate;
    private String procedureName;
    private String procedureTimeIn;
    private String procedureTimeOut;
    private String fluoroTime;
    private String accessionNumber;
    private String udiQuantity;
    private String di;
    

    private MaterialButton cancelButton;
    private MaterialButton saveButton;
    private FloatingActionButton addBarcode;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_addequipment, container, false);
        parent = getActivity();
        cancelButton = rootView.findViewById(R.id.equipment_cancel_button);
        saveButton = rootView.findViewById(R.id.equipment_save_button);
        addBarcode = rootView.findViewById(R.id.fragment_addScan);
        linearLayout = rootView.findViewById(R.id.equipment_linearlayout);
        buttonsLayout = rootView.findViewById(R.id.item_bottom);
        procedureInfo = new ArrayList<>();
        di = "";
        MaterialToolbar topToolBar = rootView.findViewById(R.id.topAppBar);


        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        String userId = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();

        final DocumentReference currentUserRef = usersRef.document(userId);
        currentUserRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                String toastMessage;
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (Objects.requireNonNull(document).exists()) {
                        try {
                            mNetworkId = Objects.requireNonNull(document.get("network_id")).toString();
                            mHospitalId = Objects.requireNonNull(document.get("hospital_id")).toString();
                            mHospitalName = Objects.requireNonNull(document.get("hospital_name")).toString();
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

        topToolBar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ProcedureInfoFragment fragment = new ProcedureInfoFragment();
                Bundle bundle = new Bundle();
                HashMap<String, Object> procedureInfo = new HashMap<>();
                procedureInfo.put("procedure_used", procedureName);
                procedureInfo.put("procedure_date", procedureDate);
                procedureInfo.put("time_in", procedureTimeIn);
                procedureInfo.put("time_out", procedureTimeOut);
                procedureInfo.put("fluoro_time", fluoroTime);
                procedureInfo.put("accession_number", accessionNumber);
                bundle.putSerializable("procedureMap", (Serializable) procedureInfo);
                fragment.setArguments(bundle);

                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                //clears other fragments
                fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.setCustomAnimations(R.anim.enter_left, R.anim.fui_slide_out_left);
                fragmentTransaction.add(R.id.activity_main, fragment);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();

            }
        });

        if (getArguments() != null) {
            HashMap<String, Object> procedureInfo;
            Bundle procedureInfoBundle = this.getArguments();
            if(procedureInfoBundle.get("procedureMap") != null){
                procedureInfo = (HashMap<String, Object>) procedureInfoBundle.getSerializable("procedureMap");
                procedureDate = (String) procedureInfo.get("procedure_date");
                procedureName = (String) procedureInfo.get("procedure_used");
                procedureTimeIn = (String) procedureInfo.get("time_in");
                procedureTimeOut = (String) procedureInfo.get("time_out");
                fluoroTime = (String) procedureInfo.get("fluoro_time");
                accessionNumber= (String) procedureInfo.get("accession_number");
            }

        }

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (parent != null)
                    parent.onBackPressed();
            }
        });



        addBarcode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startScanner();
            }
        });

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveProcedureInfo(procedureInfo,di);
            }
        });

        return rootView;
    }

    private void startScanner() {
        IntentIntegrator.forSupportFragment(AddEquipmentFragment.this)
                .setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES)
                .setBarcodeImageEnabled(true)
                .setCaptureActivity(CaptureActivity.class)
                .initiateScan();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            String contents = result.getContents();
            if (contents != null) {
                checkUdi(contents);
            }
            if (result.getBarcodeImagePath() != null) {
                Log.d(TAG, "" + result.getBarcodeImagePath());
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void checkUdi(final String contents){
        if (contents.equals("")) {
            return;
        }

        final View view = getView();
        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(parent);
        String url = "https://accessgudid.nlm.nih.gov/api/v2/devices/lookup.json?udi=";
        url = url + contents;

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

                            di = udi.getString("di");

                            DocumentReference docRef = db.collection("networks").document(mNetworkId)
                                    .collection("hospitals").document(mHospitalId).collection("departments")
                                    .document("default_department").collection("dis").document(di)
                                    .collection("udis").document(contents);
                            docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                    if (task.isSuccessful()) {
                                        DocumentSnapshot document = task.getResult();
                                        if (document.exists()) {
                                            addUdi(contents, view);
                                        }else{
                                            offerEquipmentScan(contents, view);
                                        }
                                    }else{
                                        Log.d(TAG, "Failed with: ", task.getException());
                                    }
                                }
                            });

                        } catch (JSONException e) {
                            FirebaseCrashlytics.getInstance().recordException(e);
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                FirebaseCrashlytics.getInstance().recordException(error);
                Log.d(TAG, "Error in parsing barcode");
               // parseBarcodeError(view);
                addUdi(contents,view);
            }
        });
        // Add the request to the RequestQueue.
        queue.add(stringRequest);
    }

    public void offerEquipmentScan(String content, View view){
        new MaterialAlertDialogBuilder(view.getContext())
                .setTitle("Scan equipment")
                .setMessage("The equipment could not be found in inventory.\nWould you like to scan it now?")

        .setNegativeButton("Scan", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        })
        .setPositiveButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        })
        .show();
    }

    public void parseBarcodeError(View view){
        new MaterialAlertDialogBuilder(view.getContext())
                .setTitle("Error reading barcode")
                .setMessage("The barcode could not be read/parsed.\n" +
                        "Would you like to add a new equipment to the procedure?")

                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        startScanner();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (parent != null)
                            parent.onBackPressed();
                    }
                })
                .show();

    }

    public void addUdi(final String barcode, View view){
        LayoutInflater viewInflater = (LayoutInflater) view.getContext().getSystemService
                (Context.LAYOUT_INFLATER_SERVICE);
        View textView = viewInflater.inflate(R.layout.procedures_item,null);
        TextView udi = textView.findViewById(R.id.scanned_udi);
        final TextView quantity = textView.findViewById(R.id.udi_quantity);
        final ImageView plusIcon = textView.findViewById(R.id.increment_icon);
        plusIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addNumberPicker(view, quantity,barcode,plusIcon);
            }
        });

        addNumberPicker(view,quantity, barcode, plusIcon);
        udi.setText(barcode);
        buttonsLayout.setVisibility(View.VISIBLE);
        linearLayout.addView(textView,linearLayout.indexOfChild(buttonsLayout));

    }

    private void addNumberPicker(View view, final TextView quantity, final String barcode, final ImageView plusIcon){
        final Dialog d = new Dialog(view.getContext());
        //d.setTitle("Enter quantity");
        d.setContentView(R.layout.dialog);
        Button b1 =  d.findViewById(R.id.button1);
        Button b2 =  d.findViewById(R.id.button2);
        final NumberPicker np =  d.findViewById(R.id.numberPicker1);
        np.setMaxValue(1000);
        np.setMinValue(0);
        np.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker numberPicker, int i, int i1) {
            }
        });
        b1.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                quantity.setText(String.format(Locale.US,"%d units", np.getValue()));
                plusIcon.setVisibility(View.GONE);
                quantity.setVisibility(View.VISIBLE);

                d.dismiss();
                HashMap<String, Object> eachUdi = new HashMap<>();
                eachUdi.put("udi",barcode);
                eachUdi.put("amount_used",String.valueOf(np.getValue()));
                procedureInfo.add(eachUdi);
            }
        });
        b2.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                d.dismiss();
            }
        });
        d.show();
    }

    private void saveProcedureInfo(final List<HashMap<String, Object>> procedureInfo, final String di){
        for(int i = 0; i < procedureInfo.size(); i++){
            procedureInfo.get(i).put("accession_number",accessionNumber);
            procedureInfo.get(i).put("fluoro_time",fluoroTime);
            procedureInfo.get(i).put("procedure_date",procedureDate);
            procedureInfo.get(i).put("time_in",procedureTimeIn);
            procedureInfo.get(i).put("time_out",procedureTimeOut);
            procedureInfo.get(i).put("procedure_used",procedureName);
        }

        for(int i = 0; i < procedureInfo.size(); i++){
            DocumentReference procedureCounterRef = db.collection("networks").document(mNetworkId)
                    .collection("hospitals").document(mHospitalId).collection("departments")
                    .document("default_department").collection("dis").document("100")
                    .collection("udis").document(procedureInfo.get(i).get("udi").toString());
            System.out.println(procedureInfo.get(i).get("udi"));

            final int finalI = i;
            procedureCounterRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            if(document.get("procedure_number") != null){
                                String procedureCounter = document.getString("procedure_number");
                                saveData(procedureInfo.get(finalI),di,procedureCounter );
                            }else{
                                saveData(procedureInfo.get(finalI),di,"0");
                            }
                        } else {
                            Toast.makeText(getView().getContext(), "Procedure information for " + procedureInfo.get(finalI).get("udi") +
                                    " has not been saved", Toast.LENGTH_SHORT).show();
                            Log.d(TAG, "No such document");
                        }
                    } else {
                        Log.d(TAG, "get failed with ", task.getException());
                    }
                }
            });
        }

        Toast.makeText(parent,"Procedure information saved",Toast.LENGTH_LONG).show();

    }

    private void saveData(HashMap<String, Object> procedureInfo, String di,String procedureCounter){

        String udi = procedureInfo.get("udi").toString();
        procedureInfo.remove("udi");

        DocumentReference procedureDocRef = db.collection("networks").document(mNetworkId)
                .collection("hospitals").document(mHospitalId).collection("departments")
                .document("default_department").collection("dis").document("100")
                .collection("udis").document(udi);


        procedureDocRef.collection("procedures")
                .document("procedure_0" + (Integer.parseInt(procedureCounter) + 1)).set(procedureInfo)
                //in case of success
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "procedure info saved");
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

        HashMap<String, Object> procedureCounterMap = new HashMap<>();
        int procedureCount = Integer.parseInt(procedureCounter);
        procedureCount++;
        procedureCounterMap.put("procedure_number",String.valueOf(procedureCount));
        procedureDocRef.set(procedureCounterMap, SetOptions.merge())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG,"Procedure counter has been saved");
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
