package com.levigo.levigoapp;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class AddEquipmentFragment extends Fragment {
    private static final String TAG = AddEquipmentFragment.class.getSimpleName();
    private Activity parent;

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final CollectionReference usersRef = db.collection("users");
    private LinearLayout linearLayout;
    private LinearLayout buttonsLayout;
    private List<HashMap<String, Object>> procedureInfo;
    HashMap<String, Object> procedureInfoHashMap;

    private TextView procedureInfoName;
    private TextView procedureInfoDate;
    private TextView procedureAccessionNumber;

    private String mNetworkId;
    private String mHospitalId;
    private String procedureDate;
    private String procedureName;
    private String procedureTimeIn;
    private String procedureTimeOut;
    private String roomTime;
    private String fluoroTime;
    private String accessionNumber;
    private String di;
    private String udiStr;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_addequipment, container, false);
        parent = getActivity();
        MaterialButton cancelButton = rootView.findViewById(R.id.equipment_cancel_button);
        MaterialButton saveButton = rootView.findViewById(R.id.equipment_save_button);
        FloatingActionButton addBarcode = rootView.findViewById(R.id.fragment_addScan);
        procedureInfoName = rootView.findViewById(R.id.procedureinfoName_edittext);
        procedureInfoDate = rootView.findViewById(R.id.procedureinfoDate_edittext);
        procedureAccessionNumber = rootView.findViewById(R.id.procedureinfoAccessionNumber_edittext);
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
                HashMap<String, Object> procedureInfoMap = new HashMap<>();
                procedureInfoMap.put("procedure_used", procedureName);
                procedureInfoMap.put("procedure_date", procedureDate);
                procedureInfoMap.put("time_in", procedureTimeIn);
                procedureInfoMap.put("time_out", procedureTimeOut);
                procedureInfoMap.put("room_time", roomTime);
                procedureInfoMap.put("fluoro_time", fluoroTime);
                procedureInfoMap.put("accession_number", accessionNumber);
                bundle.putSerializable("procedureMap", procedureInfoMap);
                fragment.setArguments(bundle);

                FragmentManager fragmentManager = Objects.requireNonNull(getActivity()).getSupportFragmentManager();
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

            Bundle procedureInfoBundle = this.getArguments();
            if(procedureInfoBundle.getBoolean("added")){
                HashMap<String, Object> returnedProcedureInfo = (HashMap<String, Object>) procedureInfoBundle.getSerializable("procedure_info");
                checkUdi(Objects.requireNonNull(procedureInfoBundle.getString("barcode")));
                procedureDate = (String) Objects.requireNonNull(returnedProcedureInfo).get("procedure_date");
                procedureName = (String) returnedProcedureInfo.get("procedure_used");
                procedureTimeIn = (String) returnedProcedureInfo.get("time_in");
                procedureTimeOut = (String) returnedProcedureInfo.get("time_out");
                roomTime = (String) returnedProcedureInfo.get("room_time");
                fluoroTime = (String) returnedProcedureInfo.get("fluoro_time");
                accessionNumber= (String) returnedProcedureInfo.get("accession_number");
                setProcedureSummary(returnedProcedureInfo);

            }
            if(procedureInfoBundle.get("procedureMap") != null){
                procedureInfoHashMap = (HashMap<String, Object>) procedureInfoBundle.getSerializable("procedureMap");
                setProcedureSummary(procedureInfoHashMap);
                procedureDate = (String) Objects.requireNonNull(procedureInfoHashMap).get("procedure_date");
                procedureName = (String) procedureInfoHashMap.get("procedure_used");
                procedureTimeIn = (String) procedureInfoHashMap.get("time_in");
                procedureTimeOut = (String) procedureInfoHashMap.get("time_out");
                roomTime = (String) procedureInfoHashMap.get("room_time");
                fluoroTime = (String) procedureInfoHashMap.get("fluoro_time");
                accessionNumber= (String) procedureInfoHashMap.get("accession_number");
            }

        }

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (parent != null)
                    parent.onBackPressed();
            }
        });


        final boolean[] notclicked = {true};

        addBarcode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               // startScanner();

                if(notclicked[0]){
                    checkUdi("(01)00886333006052(17)22313");
                    notclicked[0] = false;
                }else if(!notclicked[0]){
                    checkUdi("(01)00389701007632(17)22063");
                }

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

    private void setProcedureSummary(HashMap<String, Object> procedureInformation){
        procedureInfoName.setText(procedureInformation.get("procedure_used").toString());
        procedureInfoDate.setText(procedureInformation.get("procedure_date").toString());
        procedureAccessionNumber.setText(procedureInformation.get("accession_number").toString());
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
                            JSONObject udi = responseJson.getJSONObject("udi");

                            di = udi.getString("di");
                            udiStr = contents;

                            DocumentReference docRef = db.collection("networks").document(mNetworkId)
                                    .collection("hospitals").document(mHospitalId).collection("departments")
                                    .document("default_department").collection("dis").document(di)
                                    .collection("udis").document(contents);
                            docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                    if (task.isSuccessful()) {
                                        DocumentSnapshot document = task.getResult();
                                        if (Objects.requireNonNull(document).exists()) {
                                            System.out.println(contents);
                                            addUdi(contents, getView());
                                        }else{
                                            offerEquipmentScan(Objects.requireNonNull(view), contents);
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
                parseBarcodeError(Objects.requireNonNull(view));
            }
        });
        // Add the request to the RequestQueue.
        queue.add(stringRequest);
    }

    public void offerEquipmentScan(View view, final String udi){
        new MaterialAlertDialogBuilder(view.getContext())
                .setTitle("Scan equipment")
                .setMessage("The equipment could not be found in inventory.\nWould you like to scan it now?")

        .setNegativeButton("Scan", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                ItemDetailFragment fragment = new ItemDetailFragment();
                Bundle bundle = new Bundle();
                bundle.putString("barcode", udi);
                bundle.putSerializable("procedure_info",procedureInfoHashMap);
                fragment.setArguments(bundle);
                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();

                //clears other fragments
               // fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.setCustomAnimations(R.anim.fui_slide_in_right, R.anim.fui_slide_out_left);
                fragmentTransaction.add(R.id.activity_main, fragment);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();

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

    private void addUdi(final String barcode, View view){
        final View textView = View.inflate(view.getContext(),R.layout.procedure_item,null);
        textView.setId(View.generateViewId());

        TextView udi = textView.findViewById(R.id.scanned_udi);
        final TextView quantity = textView.findViewById(R.id.udi_quantity);
        final ImageView plusIcon = textView.findViewById(R.id.increment_icon);
        ImageView deleteIcon = textView.findViewById(R.id.delete_icon);
        deleteIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteUdiView(view,textView.getId(),barcode);
            }
        });
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

    private void deleteUdiView(View view, int elementId, String barcode){
        linearLayout.removeView(linearLayout.findViewById(elementId));
        for(int i = 0; i < procedureInfo.size(); i++){
            if(procedureInfo.get(i).get("udi").toString().equals(barcode)){
                procedureInfo.remove(i);
            }
        }
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
                int quantityInt = np.getValue();
                if(quantityInt > 1){
                    quantity.setText(String.format(Locale.US,"%d units have been used", quantityInt));
                }else{
                    quantity.setText(String.format(Locale.US,"%d unit has been used", quantityInt));
                }

                quantity.setVisibility(View.VISIBLE);
                d.dismiss();
                HashMap<String, Object> eachUdi = new HashMap<>();
                eachUdi.put("udi",barcode);
                eachUdi.put("amount_used",String.valueOf(np.getValue()));
                procedureInfo.add(eachUdi);
                System.out.println(procedureInfo);
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
            procedureInfo.get(i).put("room_time",roomTime);
            procedureInfo.get(i).put("procedure_date",procedureDate);
            procedureInfo.get(i).put("time_in",procedureTimeIn);
            procedureInfo.get(i).put("time_out",procedureTimeOut);
            procedureInfo.get(i).put("procedure_used",procedureName);
            procedureInfo.get(i).put("fluoro_time",fluoroTime);
        }

        for(int i = 0; i < procedureInfo.size(); i++){
            DocumentReference procedureCounterRef = db.collection("networks").document(mNetworkId)
                    .collection("hospitals").document(mHospitalId).collection("departments")
                    .document("default_department").collection("dis").document(di)
                    .collection("udis").document(Objects.requireNonNull(procedureInfo.get(i).get("udi")).toString());


            final int finalI = i;
            procedureCounterRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (Objects.requireNonNull(document).exists()) {
                            if(document.get("procedure_number") != null){
                                String procedureCounter = document.getString("procedure_number");
                                saveData(procedureInfo.get(finalI),di,procedureCounter );
                            }else{
                                saveData(procedureInfo.get(finalI),di,"0");
                            }
                        } else {
                            Toast.makeText(Objects.requireNonNull(getView()).getContext(), "Procedure information for " + procedureInfo.get(finalI).get("udi") +
                                    " has not been saved", Toast.LENGTH_SHORT).show();
                            Log.d(TAG, "No such document");
                        }
                    } else {
                        Log.d(TAG, "get failed with ", task.getException());
                    }
                }
            });
        }
        if(parent != null){
            Toast.makeText(getActivity(), "Procedure information saved", Toast.LENGTH_SHORT).show();
            parent.onBackPressed();
        }


    }

    private void saveData(HashMap<String, Object> procedureInfo, String di,String procedureCounter){

        String udi = Objects.requireNonNull(procedureInfo.get("udi")).toString();
        procedureInfo.remove("udi");

        DocumentReference procedureDocRef = db.collection("networks").document(mNetworkId)
                .collection("hospitals").document(mHospitalId).collection("departments")
                .document("default_department").collection("dis").document(di)
                .collection("udis").document(udi);


        // saves procedure information for each udi
        procedureDocRef.collection("procedures")
                .document("procedure_" + (Integer.parseInt(procedureCounter) + 1)).set(procedureInfo)
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

        // saves accession number in a collection of accession numbers
        HashMap<String, Object> accessionNumberMap = new HashMap<>();
        accessionNumberMap.put("accession_number", accessionNumber);
        db.collection("networks").document(mNetworkId).collection("hospitals")
                .document(mHospitalId).collection("accession_numbers").document(accessionNumber)
                .set(accessionNumberMap)
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

        // updates number of procedures saved for each udi
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
