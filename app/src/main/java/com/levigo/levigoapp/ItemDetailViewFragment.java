package com.levigo.levigoapp;

import android.app.Activity;
import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ItemDetailViewFragment extends Fragment {

    private static final String TAG = ItemDetailFragment.class.getSimpleName();
    private Activity parent;

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final CollectionReference usersRef = db.collection("users");

    private String mNetworkId;
    private String mHospitalId;
    private String itemQuantity;
    private String currentDate;
    private String currentTime;
    private int procedureCount;
    private final String TYPE_KEY = "equipment_type";
    private final String SITE_KEY = "site_name";
    private final String USAGE_KEY = "usage";
    private final String PHYSICALLOC_KEY = "physical_location";
    private final String QUANTITY_KEY = "quantity";


    private LinearLayout linearLayout;
    private LinearLayout usageLinearLayout;
    private LinearLayout itemSpecsLinearLayout;

    private TextInputLayout specificationLayout;
    private TextInputLayout usageLayout;
    private TextInputEditText itemName;
    private TextInputEditText udi;
    private TextInputEditText deviceIdentifier;
    private TextInputEditText quantity;
    private TextInputEditText expiration;
    private TextInputEditText hospitalName;
    private TextInputEditText physicalLocation;
    private TextInputEditText type;
    private TextInputEditText usage;
    private TextInputEditText medicalSpecialty;
    private TextInputEditText referenceNumber;
    private TextInputEditText lotNumber;
    private TextInputEditText manufacturer;
    private TextInputEditText lastUpdate;
    private TextInputEditText notes;
    private TextInputEditText deviceDescription;
    private TextInputLayout usageHeader;

    private List<String> procedureDocuments;
    private List<List<String>> procedureDoc;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        final View rootView = inflater.inflate(R.layout.fragment_viewonlyitemdetail, container, false);
        parent = getActivity();
        MaterialToolbar topToolBar = rootView.findViewById(R.id.topAppBar);
        itemName = rootView.findViewById(R.id.itemname_text);
        udi = rootView.findViewById(R.id.barcode_edittext);
        deviceIdentifier = rootView.findViewById(R.id.di_edittext);
        quantity = rootView.findViewById(R.id.quantity_edittext);
        expiration = rootView.findViewById(R.id.expiration_edittext);
        hospitalName = rootView.findViewById(R.id.site_edittext);
        physicalLocation = rootView.findViewById(R.id.physicallocation_edittext);
        type = rootView.findViewById(R.id.type_edittext);
        usage = rootView.findViewById(R.id.usage_edittext);
        medicalSpecialty = rootView.findViewById(R.id.medicalspecialty_edittext);
        referenceNumber = rootView.findViewById(R.id.referencenumber_edittext);
        lotNumber = rootView.findViewById(R.id.lotnumber_edittext);
        manufacturer = rootView.findViewById(R.id.company_edittext);
        lastUpdate = rootView.findViewById(R.id.lasteupdate_edittext);
        notes = rootView.findViewById(R.id.notes_edittext);
        deviceDescription = rootView.findViewById(R.id.devicedescription_edittext);
        specificationLayout = rootView.findViewById(R.id.specifications_header);
        usageLayout = rootView.findViewById(R.id.usageicon_header);
        procedureDoc = new ArrayList<>();
        usageHeader = rootView.findViewById(R.id.usage_header);
        linearLayout = rootView.findViewById(R.id.itemdetailviewonly_linearlayout);
        LinearLayout specsLinearLayout = rootView.findViewById(R.id.specs_linearlayout);
        usageLinearLayout = rootView.findViewById(R.id.usage_linearlayout);
        itemSpecsLinearLayout = new LinearLayout(rootView.getContext());
        itemSpecsLinearLayout.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        itemSpecsLinearLayout.setOrientation(LinearLayout.VERTICAL);
        itemSpecsLinearLayout.setVisibility(View.GONE);
        linearLayout.addView(itemSpecsLinearLayout,linearLayout.indexOfChild(specsLinearLayout) + 1);


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


        if (getArguments() != null) {
            String barcode = getArguments().getString("barcode");
            udi.setText(barcode);
            autoPopulate(rootView);
        }

        topToolBar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (parent != null)
                    parent.onBackPressed();
            }
        });

        final boolean[] isSpecsMaximized = {false};
        specificationLayout.setEndIconOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isSpecsMaximized[0]){
                    isSpecsMaximized[0] = false;
                    itemSpecsLinearLayout.setVisibility(View.GONE);
                    specificationLayout.setEndIconDrawable(R.drawable.ic_baseline_plus);

                }else{
                    itemSpecsLinearLayout.setVisibility(View.VISIBLE);
                    specificationLayout.setEndIconDrawable(R.drawable.ic_remove_minimize);
                    isSpecsMaximized[0] = true;

                }
            }
        });

        return rootView;
    }

    String di = "";
    private void autoPopulate(final View view) {


        final String udiStr = Objects.requireNonNull(udi.getText()).toString();
        udi.setFocusable(false);
        Log.d(TAG, udiStr);

        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(parent);
        String url = "https://accessgudid.nlm.nih.gov/api/v2/devices/lookup.json?udi=";

        url = url + udiStr;

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
                            JSONArray productCodes = responseJson.getJSONArray("productCodes");
                            StringBuilder medicalSpecialties = new StringBuilder();
                            for (int i = 0; i < productCodes.length(); i++) {
                                medicalSpecialties.append(productCodes.getJSONObject(i).getString("medicalSpecialty"));
                                medicalSpecialties.append("; ");
                            }
                            medicalSpecialties = new StringBuilder(medicalSpecialties.substring(0, medicalSpecialties.length() - 2));

                            lotNumber.setText(udi.getString("lotNumber"));
                            lotNumber.setFocusable(false);

                            manufacturer.setText(deviceInfo.getString("companyName"));
                            manufacturer.setFocusable(false);

                            expiration.setText(udi.getString("expirationDate"));
                            expiration.setFocusable(false);

                            di = udi.getString("di");
                            deviceIdentifier.setText(udi.getString("di"));
                            deviceIdentifier.setFocusable(false);

                            itemName.setText(deviceInfo.getJSONObject("gmdnTerms").getJSONArray("gmdn").getJSONObject(0).getString("gmdnPTName"));
                            itemName.setFocusable(false);

                            deviceDescription.setText(deviceInfo.getString("deviceDescription"));
                            deviceDescription.setFocusable(false);

                            referenceNumber.setText(deviceInfo.getString("catalogNumber"));
                            referenceNumber.setFocusable(false);

                            medicalSpecialty.setText(medicalSpecialties.toString());
                            medicalSpecialty.setFocusable(false);

                            autoPopulateFromDatabase(udi, udiStr, view);

                            JSONArray deviceSizeArray = deviceInfo.getJSONObject("deviceSizes").getJSONArray("deviceSize");
                            for (int i = 0; i < deviceSizeArray.length(); ++i) {
                                String k;
                                String v;

                                JSONObject currentSizeObject = deviceSizeArray.getJSONObject(i);
                                k = currentSizeObject.getString("sizeType");
                                Log.d(TAG, "KEYS: " + k);
                                if (k.equals("Device Size Text, specify")) {
                                    String customSizeText = currentSizeObject.getString("sizeText");
                                    // Key is usually substring before first number (e.g. "Co-Axial Introducer Needle: 17ga x 14.9cm")
                                    k = customSizeText.split("[0-9]+")[0];

                                    // needs remember the cutoff to retrieve the rest of the string
                                    int cutoff = k.length();
                                    // take off trailing whitespace
                                    try {
                                        k = k.substring(0, k.length() - 2);
                                    } catch (StringIndexOutOfBoundsException e) { // if sizeText starts with number
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
                                addItemSpecs(k, v, view);
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
    }

    private void addItemSpecs(String key, String value, View view){

        LinearLayout eachItemSpecsLayout = new LinearLayout(view.getContext());
        eachItemSpecsLayout.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        eachItemSpecsLayout.setOrientation(LinearLayout.HORIZONTAL);
        eachItemSpecsLayout.setBaselineAligned(false);

        final TextInputLayout itemSpecsHeader = new TextInputLayout(view.getContext());
        LinearLayout.LayoutParams itemSpecsParams = new LinearLayout.LayoutParams(0,
                LinearLayout.LayoutParams.MATCH_PARENT);
        itemSpecsParams.weight = (float) 1.0;
        itemSpecsHeader.setLayoutParams(itemSpecsParams);

        TextInputEditText headerKey = new TextInputEditText(itemSpecsHeader.getContext());
        headerKey.setText(key);
        headerKey.setFocusable(false);
        headerKey.setTypeface(headerKey.getTypeface(), Typeface.BOLD);
        itemSpecsHeader.addView(headerKey);


        final TextInputLayout itemSpecsValue = new TextInputLayout(view.getContext());
        LinearLayout.LayoutParams specValueParams = new LinearLayout.LayoutParams(0,
                LinearLayout.LayoutParams.MATCH_PARENT);
        specValueParams.weight = (float) 1.0;
        itemSpecsValue.setLayoutParams(specValueParams);

        TextInputEditText specsValue = new TextInputEditText(itemSpecsValue.getContext());
        specsValue.setText(value);
        specsValue.setFocusable(false);
        itemSpecsValue.addView(specsValue);

        eachItemSpecsLayout.addView(itemSpecsHeader);
        eachItemSpecsLayout.addView(itemSpecsValue);

        itemSpecsLinearLayout.addView(eachItemSpecsLayout);
    }


    private void autoPopulateFromDatabase(final JSONObject udi, final String udiStr, final View view) {
        DocumentReference udiDocRef;
        DocumentReference diDocRef;

        udiDocRef = db.collection("networks").document(mNetworkId)
                .collection("hospitals").document(mHospitalId).collection("departments")
                .document("default_department").collection("dis").document(di)
                .collection("udis").document(udiStr);

        diDocRef = db.collection("networks").document(mNetworkId)
                .collection("hospitals").document(mHospitalId).collection("departments")
                .document("default_department").collection("dis").document(di);


        udiDocRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (Objects.requireNonNull(document).exists()) {
                        if(document.get("procedure_number") != null){
                            procedureCount = Integer.parseInt(
                                    Objects.requireNonNull(document.getString("procedure_number")));

                            getProcedureInfo(procedureCount, udi, udiStr, view);
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
                    if (Objects.requireNonNull(document).exists()) {
                        if(document.get(TYPE_KEY) != null){
                            type.setText(document.getString(TYPE_KEY));
                            type.setFocusable(false);

                        }if(document.get(SITE_KEY) != null){
                            hospitalName.setText(document.getString(SITE_KEY));
                            hospitalName.setFocusable(false);
                        }if(document.get(USAGE_KEY) != null){
                            String usageStr = document.getString(USAGE_KEY);
                            usage.setText(usageStr);
                        }
                    } else {

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
                    if (Objects.requireNonNull(document).exists()) {
                        if(document.get("quantity") != null) {
                            itemQuantity = document.getString(QUANTITY_KEY);
                            quantity.setText(itemQuantity);
                        }else{
                            itemQuantity = "0";
                            quantity.setText("0");
                        }if(document.get(PHYSICALLOC_KEY) != null){
                            physicalLocation.setText(document.getString(PHYSICALLOC_KEY));
                        }if(document.get("current_date") != null){
                            currentDate = document.getString("current_date");
                        }if(document.get("current_date_time") != null){
                            currentTime = document.getString("current_date_time");
                            lastUpdate.setText(String.format("%s\n%s", currentDate, currentTime));
                        }if(document.get("isUsed") != null){
                            String isUsed = String.valueOf(document.getBoolean("isUsed"));
                            String isUsedStr = isUsed.substring(0, 1).toUpperCase() + isUsed.substring(1);
                           TextInputEditText isUsedEditText = view.findViewById(R.id.isitemused_edittext);
                           isUsedEditText.setText(isUsedStr);
                        }
                        if(document.get("notes") != null){
                            notes.setText(document.getString("notes"));
                        }
                    } else {
                        itemQuantity = "0";
                        quantity.setText("0");
                        Log.d(TAG, "Document does not exist!");
                    }
                    quantity.setText(document.getString(QUANTITY_KEY));
                    quantity.setFocusable(false);
                } else {
                    Log.d(TAG, "Failed with: ", task.getException());
                }
            }
        });

    }

    private void getProcedureInfo(final int procedureCount, JSONObject udi,
                                  String udiStr, final View view){
        final int[] check = {0};
        DocumentReference procedureRef;

        try {
            for ( int i = 0; i < procedureCount; i++) {
                procedureRef = db.collection("networks").document(mNetworkId)
                        .collection("hospitals").document(mHospitalId).collection("departments")
                        .document("default_department").collection("dis").document(udi.getString("di"))
                        .collection("udis").document(udiStr).collection("procedures")
                        .document("procedure_" + (i + 1));

                procedureRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (Objects.requireNonNull(document).exists()) {
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
                            final boolean[] isUsageMaximized = {false};
                            final LinearLayout isItemUsedLinearLayout = view.findViewById(R.id.isitemused_linear);
                            if(check[0] == procedureCount) {
                                usageLayout.setEndIconOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        if(isUsageMaximized[0]){
                                            usageLayout.setEndIconDrawable(R.drawable.ic_baseline_plus);
                                            isItemUsedLinearLayout.setVisibility(View.GONE);
                                            linearLayout.getChildAt(linearLayout.indexOfChild(usageLinearLayout)+ 1)
                                                    .setVisibility(View.GONE);
                                            linearLayout.getChildAt(linearLayout.indexOfChild(usageLinearLayout)+ 2)
                                                    .setVisibility(View.GONE);
                                            isUsageMaximized[0] = false;
                                            usageHeader.setEndIconDrawable(R.drawable.ic_baseline_plus);

                                        }else{
                                            usageLayout.setEndIconDrawable(R.drawable.ic_remove_minimize);
                                            isItemUsedLinearLayout.setVisibility(View.VISIBLE);
                                            addProcedureInfoFields(procedureDoc,view);
                                            isUsageMaximized[0] = true;
                                            usageHeader.setEndIconDrawable(R.drawable.ic_remove_minimize);

                                        }
                                    }
                                });
                            }
                        }
                    }
                });
            }
        }catch(JSONException e){
            Log.d(TAG, e.toString());
        }

    }


    private void addProcedureInfoFields(final List<List<String>> procedureDoc, View view){
        int i;
        final LinearLayout procedureInfoLayout = new LinearLayout(view.getContext());
        procedureInfoLayout.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        procedureInfoLayout.setOrientation(LinearLayout.VERTICAL);

        for(i = 0; i < procedureDoc.size(); i++) {

            final LinearLayout eachProcedureLayout = new LinearLayout(view.getContext());
            eachProcedureLayout.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));
            eachProcedureLayout.setOrientation(LinearLayout.HORIZONTAL);
            eachProcedureLayout.setBaselineAligned(false);

            final TextInputLayout procedureDateHeader = new TextInputLayout(view.getContext());
            LinearLayout.LayoutParams procedureHeaderParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            procedureHeaderParams.weight = (float) 1.0;
            procedureDateHeader.setLayoutParams(procedureHeaderParams);
            TextInputEditText dateKey = new TextInputEditText(procedureDateHeader.getContext());
            dateKey.setText(R.string.procedureDate_lbl);
            dateKey.setTypeface(dateKey.getTypeface(), Typeface.BOLD);
            dateKey.setFocusable(false);
            procedureDateHeader.addView(dateKey);


            final TextInputLayout procedureDateText = new TextInputLayout(view.getContext());
            LinearLayout.LayoutParams procedureParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            procedureParams.weight = (float) 1.0;
            procedureDateText.setLayoutParams(procedureParams);

            TextInputEditText dateText = new TextInputEditText(procedureDateText.getContext());
            dateText.setText(procedureDoc.get(i).get(3));
            dateText.setFocusable(false);
            procedureDateText.addView(dateText);
            procedureDateText.setEndIconMode(TextInputLayout.END_ICON_CUSTOM);
            procedureDateText.setEndIconDrawable(R.drawable.ic_baseline_plus);
            procedureDateText.setEndIconTintList(ColorStateList.valueOf(getResources().
                    getColor(R.color.colorPrimary, Objects.requireNonNull(getActivity()).getTheme())));

            eachProcedureLayout.addView(procedureDateHeader);
            eachProcedureLayout.addView(procedureDateText);
            procedureInfoLayout.addView(eachProcedureLayout);


            final boolean[] isMaximized = {false};
            final int finalI = i;
            procedureDateText.setEndIconOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (isMaximized[0]) {
                        procedureInfoLayout.getChildAt((procedureInfoLayout.indexOfChild(eachProcedureLayout)) + 1).setVisibility(View.GONE);
                        procedureDateText.setEndIconDrawable(R.drawable.ic_baseline_plus);
                        procedureDateText.setEndIconTintList(ColorStateList.valueOf(getResources().
                                getColor(R.color.colorPrimary, Objects.requireNonNull(getActivity()).getTheme())));
                        isMaximized[0] = false;


                    } else {
                        addProcedureSubFields(procedureInfoLayout,view,procedureDoc, finalI,eachProcedureLayout);
                        procedureDateText.setEndIconDrawable(R.drawable.ic_remove_minimize);
                        procedureDateText.setEndIconTintList(ColorStateList.valueOf(getResources().
                                getColor(R.color.colorPrimary, Objects.requireNonNull(getActivity()).getTheme())));
                        isMaximized[0] = true;

                    }
                }
            });
        }

        linearLayout.addView(procedureInfoLayout,linearLayout.indexOfChild(usageLinearLayout) +   2);
    }

    private void addProcedureSubFields(LinearLayout procedureInfoLayout, View view,
                                       List<List<String>> procedureDoc, int item, LinearLayout procedureInfo){
        LinearLayout subFieldsLayout = new LinearLayout(view.getContext());
        subFieldsLayout.setOrientation(LinearLayout.VERTICAL);

        GridLayout procedureName = new GridLayout(view.getContext());
        procedureName.setColumnCount(2);
        procedureName.setRowCount(1);
        GridLayout.LayoutParams procedureNameHeaderParams = new GridLayout.LayoutParams();
        procedureNameHeaderParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        procedureNameHeaderParams.width = usageLinearLayout.getWidth()/2;
        procedureNameHeaderParams.rowSpec = GridLayout.spec(0);
        procedureNameHeaderParams.columnSpec = GridLayout.spec(0);
        procedureNameHeaderParams.setMargins(0, 0, 0, 5);
        TextInputLayout procedureNameHeaderLayout = (TextInputLayout) View.inflate(view.getContext(),
                R.layout.activity_itemdetail_materialcomponent, null);
        procedureNameHeaderLayout.setLayoutParams(procedureNameHeaderParams);
        TextInputEditText procedureNameHeaderEditText = new TextInputEditText(procedureNameHeaderLayout.getContext());
        procedureNameHeaderEditText.setText(R.string.procedureName_lbl);
        procedureNameHeaderEditText.setTypeface(procedureNameHeaderEditText.getTypeface(), Typeface.BOLD);
        procedureNameHeaderLayout.addView(procedureNameHeaderEditText);
        procedureNameHeaderEditText.setFocusable(false);


        GridLayout.LayoutParams procedureNameParams = new GridLayout.LayoutParams();
        procedureNameParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        procedureNameParams.width = usageLinearLayout.getWidth()/2;
        procedureNameParams.rowSpec = GridLayout.spec(0);
        procedureNameParams.columnSpec = GridLayout.spec(1);
        procedureNameParams.setMargins(0, 0, 0, 5);
        TextInputLayout procedureNameLayout = (TextInputLayout) View.inflate(view.getContext(),
                R.layout.activity_itemdetail_materialcomponent, null);
        procedureNameLayout.setLayoutParams(procedureNameParams);
        TextInputEditText procedureNameEditText = new TextInputEditText(procedureNameLayout.getContext());
        procedureNameEditText.setText(procedureDoc.get(item).get(1));
        procedureNameLayout.addView(procedureNameEditText);
        procedureNameEditText.setFocusable(false);
        procedureName.addView(procedureNameHeaderLayout);
        procedureName.addView(procedureNameLayout);


        GridLayout procedureTime = new GridLayout(view.getContext());
        procedureTime.setColumnCount(2);
        procedureTime.setRowCount(1);
        GridLayout.LayoutParams procedureTimeHeaderParams = new GridLayout.LayoutParams();
        procedureTimeHeaderParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        procedureTimeHeaderParams.width = linearLayout.getWidth()/2;
        procedureTimeHeaderParams.rowSpec = GridLayout.spec(0);
        procedureTimeHeaderParams.columnSpec = GridLayout.spec(0);
        procedureTimeHeaderParams.setMargins(0, 0, 0, 5);
        TextInputLayout procedureTimeHeaderLayout = (TextInputLayout) View.inflate(view.getContext(),
                R.layout.activity_itemdetail_materialcomponent, null);
        procedureTimeHeaderLayout.setLayoutParams(procedureTimeHeaderParams);
        TextInputEditText procedureTimeHeaderEditText = new TextInputEditText(procedureTimeHeaderLayout.getContext());
        procedureTimeHeaderEditText.setText(R.string.procedureTime_lbl);
        procedureTimeHeaderEditText.setTypeface(procedureNameHeaderEditText.getTypeface(), Typeface.BOLD);
        procedureTimeHeaderLayout.addView(procedureTimeHeaderEditText);
        procedureTimeHeaderEditText.setFocusable(false);

        GridLayout.LayoutParams procedureTimeParams = new GridLayout.LayoutParams();
        procedureTimeParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        procedureTimeParams.width = linearLayout.getWidth()/2;
        procedureTimeParams.rowSpec = GridLayout.spec(0);
        procedureTimeParams.columnSpec = GridLayout.spec(1);
        procedureTimeParams.setMargins(0, 0, 0, 5);

        TextInputLayout procedureTimeLayout = (TextInputLayout) View.inflate(view.getContext(),
                R.layout.activity_itemdetail_materialcomponent, null);
        procedureTimeLayout.setLayoutParams(procedureTimeParams);
        TextInputEditText procedureTimeEditText = new TextInputEditText(procedureTimeLayout.getContext());
        procedureTimeEditText.setText(procedureDoc.get(item).get(4));
        procedureTimeLayout.addView(procedureTimeEditText);
        procedureTimeEditText.setFocusable(false);
        procedureTime.addView(procedureTimeHeaderLayout);
        procedureTime.addView(procedureTimeLayout);



        GridLayout procedureAccession = new GridLayout(view.getContext());
        procedureAccession.setColumnCount(2);
        procedureAccession.setRowCount(1);
        GridLayout.LayoutParams procedureAccessionHeaderParams = new GridLayout.LayoutParams();
        procedureAccessionHeaderParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        procedureAccessionHeaderParams.width = linearLayout.getWidth()/2;
        procedureAccessionHeaderParams.rowSpec = GridLayout.spec(0);
        procedureAccessionHeaderParams.columnSpec = GridLayout.spec(0);
        procedureAccessionHeaderParams.setMargins(0, 0, 0, 5);
        TextInputLayout accessionHeaderLayout = (TextInputLayout) View.inflate(view.getContext(),
                R.layout.activity_itemdetail_materialcomponent, null);
        accessionHeaderLayout.setLayoutParams(procedureAccessionHeaderParams);
        TextInputEditText accessionHeaderEditText = new TextInputEditText(accessionHeaderLayout.getContext());
        accessionHeaderEditText.setText(R.string.AccessionNumber_lbl);
        accessionHeaderEditText.setTypeface(procedureNameHeaderEditText.getTypeface(), Typeface.BOLD);
        accessionHeaderLayout.addView(accessionHeaderEditText);
        accessionHeaderEditText.setFocusable(false);


        GridLayout.LayoutParams procedureAccessionParams = new GridLayout.LayoutParams();
        procedureAccessionParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        procedureAccessionParams.width = linearLayout.getWidth()/2;
        procedureAccessionParams.rowSpec = GridLayout.spec(0);
        procedureAccessionParams.columnSpec = GridLayout.spec(1);
        procedureAccessionParams.setMargins(0, 0, 0, 5);
        TextInputLayout accessionLayout = (TextInputLayout) View.inflate(view.getContext(),
                R.layout.activity_itemdetail_materialcomponent, null);
        accessionLayout.setLayoutParams(procedureAccessionParams);
        TextInputEditText accessionEditText = new TextInputEditText(accessionLayout.getContext());
        accessionEditText.setText(procedureDoc.get(item).get(0));
        accessionLayout.addView(accessionEditText);
        accessionEditText.setFocusable(false);
        procedureAccession.addView(accessionHeaderLayout);
        procedureAccession.addView(accessionLayout);

        GridLayout procedureItemUsed = new GridLayout(view.getContext());
        procedureItemUsed.setColumnCount(2);
        procedureItemUsed.setRowCount(1);

        GridLayout.LayoutParams procedureItemUsedHeader = new GridLayout.LayoutParams();
        procedureItemUsedHeader.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        procedureItemUsedHeader.width = linearLayout.getWidth()/2;
        procedureItemUsedHeader.rowSpec = GridLayout.spec(0);
        procedureItemUsedHeader.columnSpec = GridLayout.spec(0);
        procedureItemUsedHeader.setMargins(0, 0, 0, 5);
        TextInputLayout itemUsedHeaderLayout = (TextInputLayout) View.inflate(view.getContext(),
                R.layout.activity_itemdetail_materialcomponent, null);
        itemUsedHeaderLayout.setLayoutParams(procedureItemUsedHeader);
        TextInputEditText itemUsedHeaderEditText = new TextInputEditText(itemUsedHeaderLayout.getContext());
        itemUsedHeaderEditText.setText(R.string.itemsUsed_lbl);
        itemUsedHeaderEditText.setTypeface(itemUsedHeaderEditText.getTypeface(), Typeface.BOLD);
        itemUsedHeaderLayout.addView(itemUsedHeaderEditText);
        itemUsedHeaderEditText.setFocusable(false);

        GridLayout.LayoutParams procedureItemUsedLayout = new GridLayout.LayoutParams();
        procedureItemUsedLayout.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        procedureItemUsedLayout.width = usageLinearLayout.getWidth()/2;
        procedureItemUsedLayout.rowSpec = GridLayout.spec(0);
        procedureItemUsedLayout.columnSpec = GridLayout.spec(1);
        procedureItemUsedLayout.setMargins(0, 0, 0, 5);
        TextInputLayout itemUsedLayout = (TextInputLayout) View.inflate(view.getContext(),
                R.layout.activity_itemdetail_materialcomponent, null);
        TextInputEditText itemUsedEditText = new TextInputEditText(itemUsedLayout.getContext());
        itemUsedLayout.setLayoutParams(procedureItemUsedLayout);
        itemUsedEditText.setText(procedureDoc.get(item).get(2));
        itemUsedLayout.addView(itemUsedEditText);
        itemUsedEditText.setFocusable(false);

        procedureItemUsed.addView(itemUsedHeaderLayout);
        procedureItemUsed.addView(itemUsedLayout);

        subFieldsLayout.addView(procedureName);
        subFieldsLayout.addView(procedureTime);
        subFieldsLayout.addView(procedureAccession);
        subFieldsLayout.addView(procedureItemUsed);
        procedureInfoLayout.addView(subFieldsLayout,(procedureInfoLayout.indexOfChild(procedureInfo))+1);

    }
}
