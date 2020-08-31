package org.getcarebase.carebase;

import android.app.Activity;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
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
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ItemDetailViewFragment extends Fragment {

    private static final String TAG = ItemDetailViewFragment.class.getSimpleName();
    private Activity parent;

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final CollectionReference usersRef = db.collection("users");

    private String mNetworkId;
    private String mHospitalId;
    private String itemQuantity;
    private String currentDate;
    private String currentTime;
    private String returnedDi;
    private int procedureCount;
    private final String TYPE_KEY = "equipment_type";
    private final String SITE_KEY = "site_name";
    private final String USAGE_KEY = "usage";
    private final String PHYSICALLOC_KEY = "physical_location";
    private final String QUANTITY_KEY = "quantity";


    private LinearLayout linearLayout;
    private LinearLayout usageLinearLayout;
    private LinearLayout itemSpecsLinearLayout;
    private LinearLayout costLayout;


    private ImageView specificationLayout;
    private ImageView usageLayout;
    private ImageView costIcon;
    private TextView itemName;
    private TextView udi;
    private TextView deviceIdentifier;
    private TextView quantity;
    private TextView expiration;
    private TextView hospitalName;
    private TextView physicalLocation;
    private TextView type;
    private TextView usage;
    private TextView medicalSpecialty;
    private TextView referenceNumber;
    private TextView lotNumber;
    private TextView manufacturer;
    private TextView lastUpdate;
    private TextView notes;
    private TextView deviceDescription;
    private TextView usageHeader;
    private List<Map> procedureDoc;
    private List<Map> costDoc;

    private float dp;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        dp = Objects.requireNonNull(getContext()).getResources().getDisplayMetrics().density;
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
        specificationLayout = rootView.findViewById(R.id.specifications_plus);
        usageLayout = rootView.findViewById(R.id.usage_plus);
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
        linearLayout.addView(itemSpecsLinearLayout, linearLayout.indexOfChild(specsLinearLayout) + 1);
        costDoc = new ArrayList<>();
        costLayout = rootView.findViewById(R.id.cost_linearlayout);
        costIcon = rootView.findViewById(R.id.cost_plus);
        ImageView itemNameEdit = rootView.findViewById(R.id.itemname_edit);


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

                            if (getArguments() != null) {
                                String barcode = getArguments().getString("barcode");
                                udi.setText(barcode);
                                returnedDi = getArguments().getString("di");
                                Log.d(TAG, "DI: " + returnedDi + "| UDI: " + barcode);
                                autoPopulateFromDatabase(returnedDi, barcode, rootView);
                                getItemSpecs(rootView,barcode);
                            }
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
                if (parent != null)
                    parent.onBackPressed();
            }
        });

        final boolean[] isSpecsMaximized = {false};
        specificationLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isSpecsMaximized[0]) {
                    isSpecsMaximized[0] = false;
                    itemSpecsLinearLayout.setVisibility(View.GONE);
                    specificationLayout.setImageResource(R.drawable.ic_baseline_plus);

                } else {
                    itemSpecsLinearLayout.setVisibility(View.VISIBLE);
                    specificationLayout.setImageResource(R.drawable.icon_minimize);
                    isSpecsMaximized[0] = true;

                }
            }
        });

        itemNameEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ItemDetailFragment fragment = new ItemDetailFragment();
                Bundle bundle = new Bundle();
                bundle.putString("barcode", Objects.requireNonNull(udi.getText()).toString());
                bundle.putBoolean("editingExisting", true);
                bundle.putString("di", deviceIdentifier.getText().toString());
                fragment.setArguments(bundle);

                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
//            //clears other fragments
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.setCustomAnimations(R.anim.fui_slide_in_right, R.anim.fui_slide_out_left);
                fragmentTransaction.add(R.id.activity_main, fragment);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
            }
        });



        return rootView;
    }


    private void getItemSpecs(final View view, String udi) {
        String udiStr = udi;
        if (udiStr.equals("")) {
            return;
            // Some UDI starts with '+'; needs to strip plus sign and last letter in order to be recognized
        } else if (udiStr.charAt(0) == '+') {
            udiStr = udiStr.replaceFirst("[+]", "01");
        }
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
                            if (responseJson.has("gudid") && responseJson.getJSONObject("gudid").has("device")) {
                                JSONObject deviceInfo = responseJson.getJSONObject("gudid").getJSONObject("device");

                                if (deviceInfo.has("deviceSizes") && deviceInfo.getJSONObject("deviceSizes").has("deviceSize")) {
                                    JSONArray deviceSizeArray = deviceInfo.getJSONObject("deviceSizes").getJSONArray("deviceSize");
                                    for (int i = 0; i < deviceSizeArray.length(); ++i) {
                                        int colonIndex;
                                        String k;
                                        String v;
                                        JSONObject currentSizeObject = deviceSizeArray.getJSONObject(i);
                                        k = currentSizeObject.getString("sizeType");
                                        if (k.equals("Device Size Text, specify")) {
                                            String customSizeText = currentSizeObject.getString("sizeText");
                                            // Key, Value usually separated by colon
                                            colonIndex = customSizeText.indexOf(":");
                                            if (colonIndex == -1) {
                                                // If no colon, save whole field as "value"
                                                k = "Custom Key";
                                                v = customSizeText;
                                            } else {
                                                k = customSizeText.substring(0, colonIndex);
                                                v = customSizeText.substring(colonIndex + 1).trim();
                                            }
                                        } else {
                                            v = currentSizeObject.getJSONObject("size").getString("value")
                                                    + " "
                                                    + currentSizeObject.getJSONObject("size").getString("unit");
                                        }
                                        addItemSpecs(k, v, view);
                                    }
                                }
                            }

                        } catch (JSONException e) {
                            FirebaseCrashlytics.getInstance().recordException(e);
                            e.printStackTrace();
//                            Log.d(TAG, "ERROR: "+ e.getMessage());
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                FirebaseCrashlytics.getInstance().recordException(error);
//                Log.d(TAG, "Error in parsing barcode");
            }
        });
        // Add the request to the RequestQueue.
        queue.add(stringRequest);

    }

    private void addItemSpecs(String key, String value, View view) {

        LinearLayout eachItemSpecsLayout = new LinearLayout(view.getContext());
        eachItemSpecsLayout.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        eachItemSpecsLayout.setOrientation(LinearLayout.HORIZONTAL);
        eachItemSpecsLayout.setBackgroundColor(Color.WHITE);

        LinearLayout.LayoutParams itemSpecsParams = new LinearLayout.LayoutParams(0,
                LinearLayout.LayoutParams.MATCH_PARENT);
        itemSpecsParams.weight = (float) 1.0;
        itemSpecsParams.setMargins(0, (int) (1 * dp), 0, (int) (1 * dp));


        TextView headerKey = new TextView(view.getContext());
        headerKey.setLayoutParams(itemSpecsParams);
        headerKey.setPadding((int) (8 * dp), (int) (8 * dp), (int) (8 * dp), (int) (8 * dp));
        headerKey.setText(key);
        headerKey.setFocusable(false);
        headerKey.setTypeface(headerKey.getTypeface(), Typeface.BOLD);
        headerKey.setTextSize(14);
        headerKey.setTextColor(Color.BLACK);


        LinearLayout.LayoutParams specValueParams = new LinearLayout.LayoutParams(0,
                LinearLayout.LayoutParams.MATCH_PARENT);
        specValueParams.weight = (float) 1.0;
        specValueParams.setMargins(0, (int) (1 * dp), 0, (int) (1 * dp));

        TextView specsValue = new TextView(view.getContext());
        specsValue.setLayoutParams(specValueParams);
        specsValue.setPadding((int) (8 * dp), (int) (8 * dp), (int) (8 * dp), (int) (8 * dp));
        specsValue.setText(value);
        specsValue.setTextSize(14);
        specsValue.setTextColor(Color.BLACK);

        eachItemSpecsLayout.addView(headerKey);
        eachItemSpecsLayout.addView(specsValue);

        itemSpecsLinearLayout.addView(eachItemSpecsLayout);
    }

    private void autoPopulateFromDatabase(final String di, final String udiStr, final View view) {
        final DocumentReference udiDocRef;
        DocumentReference diDocRef;

        udiDocRef = db.collection("networks").document(mNetworkId)
                .collection("hospitals").document(mHospitalId).collection("departments")
                .document("default_department").collection("dis").document(di)
                .collection("udis").document(udiStr);


        diDocRef = db.collection("networks").document(mNetworkId)
                .collection("hospitals").document(mHospitalId).collection("departments")
                .document("default_department").collection("dis").document(di);


        diDocRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (Objects.requireNonNull(document).exists()) {
                        if (document.get(TYPE_KEY) != null) {
                            type.setText(document.getString(TYPE_KEY));
                        }
                        if (document.get(SITE_KEY) != null) {
                            hospitalName.setText(document.getString(SITE_KEY));
                        }
                        if (document.get(USAGE_KEY) != null) {
                            String usageStr = document.getString(USAGE_KEY);
                            usage.setText(usageStr);
                        }
                        if (document.get("device_description") != null && deviceDescription.getText().toString().length() <= 0) {
                            deviceDescription.setText(document.getString("device_description"));
                        }
                        if (document.get("di") != null && deviceIdentifier.getText().toString().length() <= 0) {
                            deviceIdentifier.setText(document.getString("di"));
                        }
                        if (document.get("medical_specialty") != null && medicalSpecialty.getText().toString().length() <= 0) {
                            medicalSpecialty.setText(document.getString("medical_specialty"));
                        }
                        if (document.get("name") != null && itemName.getText().toString().length() <= 0) {
                            itemName.setText(document.getString("name"));
                        }
                        if (document.get("company") != null && manufacturer.getText().toString().length() <= 0) {
                            manufacturer.setText(document.getString("company"));
                        }

                    } else {
                        Log.d(TAG, "Document does not exist!");
                    }
                } else {
                    Log.d(TAG, "Failed with: ", task.getException());
                }
            }
        });

        getCostInfo(udiDocRef,view);

        udiDocRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (Objects.requireNonNull(document).exists()) {
                        if (document.get("procedure_number") != null) {
                            procedureCount = Integer.parseInt(
                                    Objects.requireNonNull(document.getString("procedure_number")));
                            getProcedureInfo(procedureCount, di, udiStr, view);
                        } else {
                            procedureCount = 0;
                        }
                        if (document.get("expiration") != null) {
                            expiration.setText(document.getString("expiration"));
                        }
                        if (document.get("lot_number") != null && lotNumber.getText().toString().length() <= 0) {
                            lotNumber.setText(document.getString("lot_number"));
                        }
                        if (document.get("notes") != null) {
                            notes.setText(document.getString("notes"));
                        }
                        if (document.get("physical_location") != null) {
                            physicalLocation.setText(document.getString("physical_location"));
                        }
                        if (document.get("quantity") != null) {
                            itemQuantity = document.getString(QUANTITY_KEY);
                            quantity.setText(itemQuantity);
                        } else {
                            itemQuantity = "0";
                            quantity.setText("0");
                        }
                        if (document.get(PHYSICALLOC_KEY) != null) {
                            physicalLocation.setText(document.getString(PHYSICALLOC_KEY));
                        }
                        if (document.get("current_date") != null) {
                            currentDate = document.getString("current_date");
                        }
                        if (document.get("current_time") != null) {
                            currentTime = document.getString("current_time");
                            lastUpdate.setText(String.format("%s\n%s", currentDate, currentTime));
                        }
                        if (document.get("notes") != null) {
                            notes.setText(document.getString("notes"));
                        }
                        if (document.get("reference_number") != null && referenceNumber.getText().toString().length() <= 0) {
                            referenceNumber.setText(document.getString("reference_number"));
                        }
                    } else {
                        itemQuantity = "0";
                        quantity.setText("0");
                        procedureCount = 0;
                        Log.d(TAG, "Document does not exist!");
                    }
                    quantity.setText(document.getString(QUANTITY_KEY));
                    quantity.setFocusable(false);
                } else {
                    procedureCount = 0;
                    Log.d(TAG, "Failed with: ", task.getException());
                }
            }
        });

    }

    private void getProcedureInfo(final int procedureCount, String di,
                                  String udiStr, final View view) {
        final int[] check = {0};
        DocumentReference procedureRef;
        for (int i = 0; i < procedureCount; i++) {
            procedureRef = db.collection("networks").document(mNetworkId)
                    .collection("hospitals").document(mHospitalId).collection("departments")
                    .document("default_department").collection("dis").document(di)
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
                                procedureDoc.add(map);
                            }
                        }
                        final boolean[] isUsageMaximized = {false};
                        final LinearLayout isItemUsedLinearLayout = view.findViewById(R.id.isitemused_linear);
                        if (check[0] == procedureCount) {
                            usageLayout.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    if (isUsageMaximized[0]) {
                                        usageLayout.setImageResource(R.drawable.ic_baseline_plus);
                                        isItemUsedLinearLayout.setVisibility(View.GONE);
                                        linearLayout.getChildAt(linearLayout.indexOfChild(usageLinearLayout) + 1)
                                                .setVisibility(View.GONE);
                                        linearLayout.getChildAt(linearLayout.indexOfChild(usageLinearLayout) + 2)
                                                .setVisibility(View.GONE);
                                        isUsageMaximized[0] = false;


                                    } else {
                                        usageLayout.setImageResource(R.drawable.icon_minimize);
                                        isItemUsedLinearLayout.setVisibility(View.VISIBLE);
                                        addProcedureInfoFields(procedureDoc, view);
                                        isUsageMaximized[0] = true;

                                    }
                                }
                            });
                        }
                    }
                }
            });
        }
    }


    private void addProcedureInfoFields(final List<Map> procedureDoc, View view) {
        int i;
        final LinearLayout procedureInfoLayout = new LinearLayout(view.getContext());
        procedureInfoLayout.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        procedureInfoLayout.setOrientation(LinearLayout.VERTICAL);


        for (i = 0; i < procedureDoc.size(); i++) {

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
            dateKey.setBackgroundColor(Color.WHITE);
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
            Object dateObject = procedureDoc.get(i).get("procedure_date");
            dateText.setBackgroundColor(Color.WHITE);
            dateText.setText(dateObject.toString());
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
            addProcedureSubFields(procedureInfoLayout, view, procedureDoc, i, eachProcedureLayout);
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
                        procedureInfoLayout.getChildAt((procedureInfoLayout.indexOfChild(eachProcedureLayout)) + 1).setVisibility(View.VISIBLE);
                        procedureDateText.setEndIconDrawable(R.drawable.icon_minimize);
                        procedureDateText.setEndIconTintList(ColorStateList.valueOf(getResources().
                                getColor(R.color.colorPrimary, Objects.requireNonNull(getActivity()).getTheme())));
                        isMaximized[0] = true;

                    }
                }
            });
        }

        linearLayout.addView(procedureInfoLayout, linearLayout.indexOfChild(usageLinearLayout) + 2);
    }

    private void addProcedureSubFields(LinearLayout procedureInfoLayout, View view,
                                       List<Map> procedureDoc, int item, LinearLayout procedureInfo) {
        LinearLayout subFieldsLayout = new LinearLayout(view.getContext());
        subFieldsLayout.setOrientation(LinearLayout.VERTICAL);

        GridLayout procedureName = new GridLayout(view.getContext());
        procedureName.setColumnCount(2);
        procedureName.setRowCount(1);
        GridLayout.LayoutParams procedureNameHeaderParams = new GridLayout.LayoutParams();
        procedureNameHeaderParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        procedureNameHeaderParams.width = usageLinearLayout.getWidth() / 2;
        procedureNameHeaderParams.rowSpec = GridLayout.spec(0);
        procedureNameHeaderParams.columnSpec = GridLayout.spec(0);
        procedureNameHeaderParams.setMargins(0, 0, 0, 5);
        TextInputLayout procedureNameHeaderLayout = (TextInputLayout) View.inflate(view.getContext(),
                R.layout.activity_itemdetail_materialcomponent, null);
        procedureNameHeaderLayout.setLayoutParams(procedureNameHeaderParams);
        TextInputEditText procedureNameHeaderEditText = new TextInputEditText(procedureNameHeaderLayout.getContext());
        procedureNameHeaderEditText.setBackgroundColor(Color.WHITE);
        procedureNameHeaderEditText.setText(R.string.procedureName_lbl);
        procedureNameHeaderEditText.setTypeface(procedureNameHeaderEditText.getTypeface(), Typeface.BOLD);
        procedureNameHeaderLayout.addView(procedureNameHeaderEditText);
        procedureNameHeaderEditText.setFocusable(false);


        GridLayout.LayoutParams procedureNameParams = new GridLayout.LayoutParams();
        procedureNameParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        procedureNameParams.width = usageLinearLayout.getWidth() / 2;
        procedureNameParams.rowSpec = GridLayout.spec(0);
        procedureNameParams.columnSpec = GridLayout.spec(1);
        procedureNameParams.setMargins(0, 0, 0, 5);
        TextInputLayout procedureNameLayout = (TextInputLayout) View.inflate(view.getContext(),
                R.layout.activity_itemdetail_materialcomponent, null);
        procedureNameLayout.setLayoutParams(procedureNameParams);
        TextInputEditText procedureNameEditText = new TextInputEditText(procedureNameLayout.getContext());
        procedureNameEditText.setBackgroundColor(Color.WHITE);
        procedureNameEditText.setText(Objects.requireNonNull(procedureDoc.get(item).get("procedure_used")).toString());
        procedureNameLayout.addView(procedureNameEditText);
        procedureNameEditText.setFocusable(false);
        procedureName.addView(procedureNameHeaderLayout);
        procedureName.addView(procedureNameLayout);


        GridLayout procedureTimeIn = new GridLayout(view.getContext());
        procedureTimeIn.setColumnCount(2);
        procedureTimeIn.setRowCount(1);
        GridLayout.LayoutParams procedureTimeInHeaderParams = new GridLayout.LayoutParams();
        procedureTimeInHeaderParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        procedureTimeInHeaderParams.width = linearLayout.getWidth() / 2;
        procedureTimeInHeaderParams.rowSpec = GridLayout.spec(0);
        procedureTimeInHeaderParams.columnSpec = GridLayout.spec(0);
        procedureTimeInHeaderParams.setMargins(0, 0, 0, 5);
        TextInputLayout procedureTimeHeaderLayout = (TextInputLayout) View.inflate(view.getContext(),
                R.layout.activity_itemdetail_materialcomponent, null);
        procedureTimeHeaderLayout.setLayoutParams(procedureTimeInHeaderParams);
        TextInputEditText procedureTimeHeaderEditText = new TextInputEditText(procedureTimeHeaderLayout.getContext());
        procedureTimeHeaderEditText.setText(R.string.procedureTimeIn_label);
        procedureTimeHeaderEditText.setBackgroundColor(Color.WHITE);
        procedureTimeHeaderEditText.setTypeface(procedureTimeHeaderEditText.getTypeface(), Typeface.BOLD);
        procedureTimeHeaderLayout.addView(procedureTimeHeaderEditText);
        procedureTimeHeaderEditText.setFocusable(false);

        GridLayout.LayoutParams procedureTimeParams = new GridLayout.LayoutParams();
        procedureTimeParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        procedureTimeParams.width = linearLayout.getWidth() / 2;
        procedureTimeParams.rowSpec = GridLayout.spec(0);
        procedureTimeParams.columnSpec = GridLayout.spec(1);
        procedureTimeParams.setMargins(0, 0, 0, 5);

        TextInputLayout procedureTimeInLayout = (TextInputLayout) View.inflate(view.getContext(),
                R.layout.activity_itemdetail_materialcomponent, null);
        procedureTimeInLayout.setLayoutParams(procedureTimeParams);
        TextInputEditText procedureTimeEditText = new TextInputEditText(procedureTimeInLayout.getContext());
        procedureTimeEditText.setBackgroundColor(Color.WHITE);
        procedureTimeEditText.setText(Objects.requireNonNull(procedureDoc.get(item).get("time_in")).toString());

        procedureTimeInLayout.addView(procedureTimeEditText);
        procedureTimeEditText.setFocusable(false);
        procedureTimeIn.addView(procedureTimeHeaderLayout);
        procedureTimeIn.addView(procedureTimeInLayout);


        GridLayout procedureTimeOut = new GridLayout(view.getContext());
        procedureTimeOut.setColumnCount(2);
        procedureTimeOut.setRowCount(1);
        GridLayout.LayoutParams procedureTimeOutHeaderParams = new GridLayout.LayoutParams();
        procedureTimeOutHeaderParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        procedureTimeOutHeaderParams.width = linearLayout.getWidth() / 2;
        procedureTimeOutHeaderParams.rowSpec = GridLayout.spec(0);
        procedureTimeOutHeaderParams.columnSpec = GridLayout.spec(0);
        procedureTimeOutHeaderParams.setMargins(0, 0, 0, 5);
        TextInputLayout procedureTimeOutHeaderLayout = (TextInputLayout) View.inflate(view.getContext(),
                R.layout.activity_itemdetail_materialcomponent, null);
        procedureTimeOutHeaderLayout.setLayoutParams(procedureTimeOutHeaderParams);
        TextInputEditText procedureTimeOutHeaderEditText = new TextInputEditText(procedureTimeOutHeaderLayout.getContext());
        procedureTimeOutHeaderEditText.setText(R.string.procedureTimeOut_label);
        procedureTimeOutHeaderEditText.setBackgroundColor(Color.WHITE);
        procedureTimeOutHeaderEditText.setTypeface(procedureTimeOutHeaderEditText.getTypeface(), Typeface.BOLD);
        procedureTimeOutHeaderEditText.setFocusable(false);
        procedureTimeOutHeaderLayout.addView(procedureTimeOutHeaderEditText);

        GridLayout.LayoutParams procedureTimeOutParams = new GridLayout.LayoutParams();
        procedureTimeOutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        procedureTimeOutParams.width = linearLayout.getWidth() / 2;
        procedureTimeOutParams.rowSpec = GridLayout.spec(0);
        procedureTimeOutParams.columnSpec = GridLayout.spec(1);
        procedureTimeOutParams.setMargins(0, 0, 0, 5);

        TextInputLayout procedureTimeOutLayout = (TextInputLayout) View.inflate(view.getContext(),
                R.layout.activity_itemdetail_materialcomponent, null);
        procedureTimeOutLayout.setLayoutParams(procedureTimeParams);
        TextInputEditText procedureTimeOutEditText = new TextInputEditText(procedureTimeOutLayout.getContext());
        procedureTimeOutEditText.setBackgroundColor(Color.WHITE);
        procedureTimeOutEditText.setText(Objects.requireNonNull(procedureDoc.get(item).get("time_out")).toString());
        procedureTimeOutEditText.setFocusable(false);
        procedureTimeOutLayout.addView(procedureTimeOutEditText);
        procedureTimeOut.addView(procedureTimeOutHeaderLayout);
        procedureTimeOut.addView(procedureTimeOutLayout);

        GridLayout procedureFluoroTime = new GridLayout(view.getContext());
        procedureFluoroTime.setColumnCount(2);
        procedureFluoroTime.setRowCount(1);
        GridLayout.LayoutParams procedureFluoroTimeHeaderParams = new GridLayout.LayoutParams();
        procedureFluoroTimeHeaderParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        procedureFluoroTimeHeaderParams.width = linearLayout.getWidth() / 2;
        procedureFluoroTimeHeaderParams.rowSpec = GridLayout.spec(0);
        procedureFluoroTimeHeaderParams.columnSpec = GridLayout.spec(0);
        procedureFluoroTimeHeaderParams.setMargins(0, 0, 0, 5);
        TextInputLayout procedureFluoroTimeHeaderLayout = (TextInputLayout) View.inflate(view.getContext(),
                R.layout.activity_itemdetail_materialcomponent, null);
        procedureFluoroTimeHeaderLayout.setLayoutParams(procedureFluoroTimeHeaderParams);
        TextInputEditText procedureFluoroTimeHeaderEditText = new TextInputEditText(procedureFluoroTimeHeaderLayout.getContext());
        procedureFluoroTimeHeaderEditText.setText("Fluoro time");
        procedureFluoroTimeHeaderEditText.setBackgroundColor(Color.WHITE);
        procedureFluoroTimeHeaderEditText.setTypeface(procedureFluoroTimeHeaderEditText.getTypeface(), Typeface.BOLD);
        procedureFluoroTimeHeaderEditText.setFocusable(false);
        procedureFluoroTimeHeaderLayout.addView(procedureFluoroTimeHeaderEditText);


        GridLayout.LayoutParams procedureFluoroTimeParams = new GridLayout.LayoutParams();
        procedureFluoroTimeParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        procedureFluoroTimeParams.width = linearLayout.getWidth() / 2;
        procedureFluoroTimeParams.rowSpec = GridLayout.spec(0);
        procedureFluoroTimeParams.columnSpec = GridLayout.spec(1);
        procedureFluoroTimeParams.setMargins(0, 0, 0, 5);
        TextInputLayout procedureFluoroTimeLayout = (TextInputLayout) View.inflate(view.getContext(),
                R.layout.activity_itemdetail_materialcomponent, null);
        procedureFluoroTimeLayout.setLayoutParams(procedureFluoroTimeParams);
        TextInputEditText procedureFluoroTimeEditText = new TextInputEditText(procedureFluoroTimeLayout.getContext());
        procedureFluoroTimeEditText.setBackgroundColor(Color.WHITE);
        procedureFluoroTimeEditText.setText(Objects.requireNonNull(procedureDoc.get(item).get("fluoro_time")).toString());
        procedureFluoroTimeEditText.setFocusable(false);
        procedureFluoroTimeLayout.addView(procedureFluoroTimeEditText);
        procedureFluoroTime.addView(procedureFluoroTimeHeaderLayout);
        procedureFluoroTime.addView(procedureFluoroTimeLayout);


        GridLayout procedureRoomTime = new GridLayout(view.getContext());
        procedureRoomTime.setColumnCount(2);
        procedureRoomTime.setRowCount(1);
        GridLayout.LayoutParams procedureRoomTimeHeaderParams = new GridLayout.LayoutParams();
        procedureRoomTimeHeaderParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        procedureRoomTimeHeaderParams.width = linearLayout.getWidth() / 2;
        procedureRoomTimeHeaderParams.rowSpec = GridLayout.spec(0);
        procedureRoomTimeHeaderParams.columnSpec = GridLayout.spec(0);
        procedureRoomTimeHeaderParams.setMargins(0, 0, 0, 5);
        TextInputLayout procedureRoomTimeHeaderLayout = (TextInputLayout) View.inflate(view.getContext(),
                R.layout.activity_itemdetail_materialcomponent, null);
        procedureRoomTimeHeaderLayout.setLayoutParams(procedureRoomTimeHeaderParams);
        TextInputEditText procedureRoomTimeHeaderEditText = new TextInputEditText(procedureRoomTimeHeaderLayout.getContext());
        procedureRoomTimeHeaderEditText.setText("Room time");
        procedureRoomTimeHeaderEditText.setBackgroundColor(Color.WHITE);
        procedureRoomTimeHeaderEditText.setTypeface(procedureRoomTimeHeaderEditText.getTypeface(), Typeface.BOLD);
        procedureRoomTimeHeaderEditText.setFocusable(false);
        procedureRoomTimeHeaderLayout.addView(procedureRoomTimeHeaderEditText);


        GridLayout.LayoutParams procedureRoomTimeParams = new GridLayout.LayoutParams();
        procedureRoomTimeParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        procedureRoomTimeParams.width = linearLayout.getWidth() / 2;
        procedureRoomTimeParams.rowSpec = GridLayout.spec(0);
        procedureRoomTimeParams.columnSpec = GridLayout.spec(1);
        procedureRoomTimeParams.setMargins(0, 0, 0, 5);
        TextInputLayout procedureRoomTimeLayout = (TextInputLayout) View.inflate(view.getContext(),
                R.layout.activity_itemdetail_materialcomponent, null);
        procedureRoomTimeLayout.setLayoutParams(procedureRoomTimeParams);
        TextInputEditText procedureRoomTimeEditText = new TextInputEditText(procedureRoomTimeLayout.getContext());
        procedureRoomTimeEditText.setBackgroundColor(Color.WHITE);
        procedureRoomTimeEditText.setText(Objects.requireNonNull(procedureDoc.get(item).get("room_time")).toString());
        procedureRoomTimeEditText.setFocusable(false);
        procedureRoomTimeLayout.addView(procedureRoomTimeEditText);
        procedureRoomTime.addView(procedureRoomTimeHeaderLayout);
        procedureRoomTime.addView(procedureRoomTimeLayout);


        GridLayout procedureAccession = new GridLayout(view.getContext());
        procedureAccession.setColumnCount(2);
        procedureAccession.setRowCount(1);
        GridLayout.LayoutParams procedureAccessionHeaderParams = new GridLayout.LayoutParams();
        procedureAccessionHeaderParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        procedureAccessionHeaderParams.width = linearLayout.getWidth() / 2;
        procedureAccessionHeaderParams.rowSpec = GridLayout.spec(0);
        procedureAccessionHeaderParams.columnSpec = GridLayout.spec(0);
        procedureAccessionHeaderParams.setMargins(0, 0, 0, 5);
        TextInputLayout accessionHeaderLayout = (TextInputLayout) View.inflate(view.getContext(),
                R.layout.activity_itemdetail_materialcomponent, null);
        accessionHeaderLayout.setLayoutParams(procedureAccessionHeaderParams);
        TextInputEditText accessionHeaderEditText = new TextInputEditText(accessionHeaderLayout.getContext());
        accessionHeaderEditText.setBackgroundColor(Color.WHITE);
        accessionHeaderEditText.setText(R.string.AccessionNumber_lbl);
        accessionHeaderEditText.setTypeface(accessionHeaderEditText.getTypeface(), Typeface.BOLD);
        accessionHeaderLayout.addView(accessionHeaderEditText);
        accessionHeaderEditText.setFocusable(false);


        GridLayout.LayoutParams procedureAccessionParams = new GridLayout.LayoutParams();
        procedureAccessionParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        procedureAccessionParams.width = linearLayout.getWidth() / 2;
        procedureAccessionParams.rowSpec = GridLayout.spec(0);
        procedureAccessionParams.columnSpec = GridLayout.spec(1);
        procedureAccessionParams.setMargins(0, 0, 0, 5);
        TextInputLayout accessionLayout = (TextInputLayout) View.inflate(view.getContext(),
                R.layout.activity_itemdetail_materialcomponent, null);
        accessionLayout.setLayoutParams(procedureAccessionParams);
        TextInputEditText accessionEditText = new TextInputEditText(accessionLayout.getContext());
        accessionEditText.setBackgroundColor(Color.WHITE);
        accessionEditText.setText(Objects.requireNonNull(procedureDoc.get(item).get("accession_number")).toString());
        accessionLayout.addView(accessionEditText);
        accessionEditText.setFocusable(false);
        procedureAccession.addView(accessionHeaderLayout);
        procedureAccession.addView(accessionLayout);

        GridLayout procedureItemUsed = new GridLayout(view.getContext());
        procedureItemUsed.setColumnCount(2);
        procedureItemUsed.setRowCount(1);

        GridLayout.LayoutParams procedureItemUsedHeader = new GridLayout.LayoutParams();
        procedureItemUsedHeader.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        procedureItemUsedHeader.width = linearLayout.getWidth() / 2;
        procedureItemUsedHeader.rowSpec = GridLayout.spec(0);
        procedureItemUsedHeader.columnSpec = GridLayout.spec(0);
        procedureItemUsedHeader.setMargins(0, 0, 0, 5);
        TextInputLayout itemUsedHeaderLayout = (TextInputLayout) View.inflate(view.getContext(),
                R.layout.activity_itemdetail_materialcomponent, null);
        itemUsedHeaderLayout.setLayoutParams(procedureItemUsedHeader);
        TextInputEditText itemUsedHeaderEditText = new TextInputEditText(itemUsedHeaderLayout.getContext());
        itemUsedHeaderEditText.setBackgroundColor(Color.WHITE);
        itemUsedHeaderEditText.setText(R.string.itemsUsed_lbl);
        itemUsedHeaderEditText.setTypeface(itemUsedHeaderEditText.getTypeface(), Typeface.BOLD);
        itemUsedHeaderLayout.addView(itemUsedHeaderEditText);
        itemUsedHeaderEditText.setFocusable(false);

        GridLayout.LayoutParams procedureItemUsedLayout = new GridLayout.LayoutParams();
        procedureItemUsedLayout.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        procedureItemUsedLayout.width = usageLinearLayout.getWidth() / 2;
        procedureItemUsedLayout.rowSpec = GridLayout.spec(0);
        procedureItemUsedLayout.columnSpec = GridLayout.spec(1);
        procedureItemUsedLayout.setMargins(0, 0, 0, 5);
        TextInputLayout itemUsedLayout = (TextInputLayout) View.inflate(view.getContext(),
                R.layout.activity_itemdetail_materialcomponent, null);
        TextInputEditText itemUsedEditText = new TextInputEditText(itemUsedLayout.getContext());
        itemUsedLayout.setLayoutParams(procedureItemUsedLayout);
        itemUsedEditText.setBackgroundColor(Color.WHITE);
        itemUsedEditText.setText(Objects.requireNonNull(procedureDoc.get(item).get("amount_used")).toString());
        itemUsedLayout.addView(itemUsedEditText);
        itemUsedEditText.setFocusable(false);

        procedureItemUsed.addView(itemUsedHeaderLayout);
        procedureItemUsed.addView(itemUsedLayout);

        subFieldsLayout.addView(procedureName);
        subFieldsLayout.addView(procedureTimeIn);
        subFieldsLayout.addView(procedureTimeOut);
        subFieldsLayout.addView(procedureRoomTime);
        subFieldsLayout.addView(procedureFluoroTime);
        subFieldsLayout.addView(procedureAccession);
        subFieldsLayout.addView(procedureItemUsed);
        procedureInfoLayout.addView(subFieldsLayout, (procedureInfoLayout.indexOfChild(procedureInfo)) + 1);
        procedureInfoLayout.getChildAt(procedureInfoLayout.indexOfChild(procedureInfo) + 1).setVisibility(View.GONE);

    }

    private void getCostInfo(DocumentReference udiRef, final View view){
        udiRef.collection("equipment_cost")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                costDoc.add(document.getData());
                            }
                            addCostInfo(costDoc,view);
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });

    }

    private void addCostInfo(List<Map> costDoc,View view){
        if(costDoc.size() > 0) {
            int i;
            final LinearLayout costInfoLayout = new LinearLayout(view.getContext());
            costInfoLayout.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));
            costInfoLayout.setOrientation(LinearLayout.VERTICAL);
            costInfoLayout.setVisibility(View.GONE);

            for (i = 0; i < costDoc.size(); i++) {

                final LinearLayout eachEquipmentLayout = new LinearLayout(view.getContext());
                eachEquipmentLayout.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT));
                eachEquipmentLayout.setOrientation(LinearLayout.HORIZONTAL);
                eachEquipmentLayout.setBaselineAligned(false);

                final TextInputLayout costDateHeader = new TextInputLayout(view.getContext());
                LinearLayout.LayoutParams costHeaderParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
                costHeaderParams.weight = (float) 1.0;
                costDateHeader.setLayoutParams(costHeaderParams);
                TextInputEditText dateKey = new TextInputEditText(costDateHeader.getContext());
                dateKey.setBackgroundColor(Color.WHITE);
                dateKey.setText("Purchase date");
                dateKey.setTypeface(dateKey.getTypeface(), Typeface.BOLD);
                dateKey.setFocusable(false);
                costDateHeader.addView(dateKey);


                final TextInputLayout costDateText = new TextInputLayout(view.getContext());
                LinearLayout.LayoutParams costParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
                costParams.weight = (float) 1.0;
                costDateText.setLayoutParams(costParams);
                TextInputEditText dateText = new TextInputEditText(costDateText.getContext());
                dateText.setText(costDoc.get(i).get("cost_date").toString());
                dateText.setBackgroundColor(Color.WHITE);
                dateText.setFocusable(false);
                costDateText.addView(dateText);
                costDateText.setEndIconMode(TextInputLayout.END_ICON_CUSTOM);
                costDateText.setEndIconDrawable(R.drawable.ic_baseline_plus);
                costDateText.setEndIconTintList(ColorStateList.valueOf(getResources().
                        getColor(R.color.colorPrimary, Objects.requireNonNull(getActivity()).getTheme())));

                eachEquipmentLayout.addView(costDateHeader);
                eachEquipmentLayout.addView(costDateText);
                costInfoLayout.addView(eachEquipmentLayout);


                final LinearLayout costInfoDetails = new LinearLayout(view.getContext());
                costInfoDetails.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT));
                costInfoDetails.setOrientation(LinearLayout.VERTICAL);
                costInfoDetails.setVisibility(View.GONE);

                final LinearLayout packagePriceLinearLayout = new LinearLayout(view.getContext());
                packagePriceLinearLayout.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT));
                packagePriceLinearLayout.setOrientation(LinearLayout.HORIZONTAL);
                packagePriceLinearLayout.setBaselineAligned(false);

                final TextInputLayout packagePriceHeader = new TextInputLayout(view.getContext());
                LinearLayout.LayoutParams packageHeaderParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
                packageHeaderParams.weight = (float) 1.0;
                packagePriceHeader.setLayoutParams(packageHeaderParams);
                TextInputEditText packageKey = new TextInputEditText(packagePriceHeader.getContext());
                packageKey.setBackgroundColor(Color.WHITE);
                packageKey.setText("Package cost");
                packageKey.setTypeface(packageKey.getTypeface(), Typeface.BOLD);
                packageKey.setFocusable(false);
                packagePriceHeader.addView(packageKey);

                final TextInputLayout packagePriceText = new TextInputLayout(view.getContext());
                LinearLayout.LayoutParams packageParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
                packageParams.weight = (float) 1.0;
                packagePriceText.setLayoutParams(packageParams);
                TextInputEditText packagePriceEditText = new TextInputEditText(packagePriceText.getContext());
                packagePriceEditText.setText(String.format("$ %s", costDoc.get(i).get("package_price").toString()));
                packagePriceEditText.setBackgroundColor(Color.WHITE);
                packagePriceEditText.setFocusable(false);
                packagePriceText.addView(packagePriceEditText);

                packagePriceLinearLayout.addView(packagePriceHeader);
                packagePriceLinearLayout.addView(packagePriceText);

                final LinearLayout numberAddedLinearLayout = new LinearLayout(view.getContext());
                numberAddedLinearLayout .setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT));
                numberAddedLinearLayout.setOrientation(LinearLayout.HORIZONTAL);
                numberAddedLinearLayout.setBaselineAligned(false);


                final TextInputLayout numberAddedHeader = new TextInputLayout(view.getContext());
                LinearLayout.LayoutParams numberAddedHeaderParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
                numberAddedHeaderParams.weight = (float) 1.0;
                numberAddedHeader.setLayoutParams(numberAddedHeaderParams);
                TextInputEditText numberAddedKey = new TextInputEditText(numberAddedHeader.getContext());
                numberAddedKey.setBackgroundColor(Color.WHITE);
                numberAddedKey.setText("Number of units");
                numberAddedKey.setTypeface(numberAddedKey.getTypeface(), Typeface.BOLD);
                numberAddedKey.setFocusable(false);
                numberAddedHeader.addView(numberAddedKey);

                final TextInputLayout numberAddedText = new TextInputLayout(view.getContext());
                LinearLayout.LayoutParams numberAddedParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
                numberAddedParams.weight = (float) 1.0;
                numberAddedText.setLayoutParams(numberAddedParams);
                TextInputEditText numberAddedEditText = new TextInputEditText(numberAddedText.getContext());
                numberAddedEditText.setText(costDoc.get(i).get("number_added").toString());
                numberAddedEditText.setBackgroundColor(Color.WHITE);
                numberAddedEditText.setFocusable(false);
                numberAddedText.addView(numberAddedEditText);

                numberAddedLinearLayout.addView(numberAddedHeader);
                numberAddedLinearLayout.addView(numberAddedText);

                final LinearLayout unitCostLinearLayout = new LinearLayout(view.getContext());
                unitCostLinearLayout.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT));
                unitCostLinearLayout.setOrientation(LinearLayout.HORIZONTAL);
                unitCostLinearLayout.setBaselineAligned(false);


                final TextInputLayout unitCostHeader = new TextInputLayout(view.getContext());
                LinearLayout.LayoutParams unitCostParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
                unitCostParams.weight = (float) 1.0;
                unitCostHeader.setLayoutParams(unitCostParams);
                TextInputEditText unitCostKey = new TextInputEditText(unitCostHeader.getContext());
                unitCostKey.setBackgroundColor(Color.WHITE);
                unitCostKey.setText("Cost per unit");
                unitCostKey.setTypeface(dateKey.getTypeface(), Typeface.BOLD);
                unitCostKey.setFocusable(false);
                unitCostHeader.addView(unitCostKey);

                final TextInputLayout unitCostText = new TextInputLayout(view.getContext());
                LinearLayout.LayoutParams unitCostTextParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
                unitCostTextParams.weight = (float) 1.0;
                unitCostText.setLayoutParams(unitCostTextParams);
                TextInputEditText unitCostEditText = new TextInputEditText(unitCostText.getContext());
                unitCostEditText.setText(String.format("$ %s", costDoc.get(i).get("unit_price").toString()));
                unitCostEditText.setBackgroundColor(Color.WHITE);
                unitCostEditText.setFocusable(false);
                unitCostText.addView(unitCostEditText);

                unitCostLinearLayout.addView(unitCostHeader);
                unitCostLinearLayout.addView(unitCostText);

                costInfoDetails.addView(packagePriceLinearLayout);
                costInfoDetails.addView(numberAddedLinearLayout);
                costInfoDetails.addView(unitCostLinearLayout);

                costInfoLayout.addView(costInfoDetails);



                final boolean[] isDetailMaximized = {false};
                costDateText.setEndIconOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(isDetailMaximized[0]){
                            costInfoDetails.setVisibility(View.GONE);
                            isDetailMaximized[0] = false;
                            costDateText.setEndIconDrawable(R.drawable.ic_baseline_plus);

                        }else{
                            costInfoDetails.setVisibility(View.VISIBLE);
                            isDetailMaximized[0] = true;
                            costDateText.setEndIconDrawable(R.drawable.icon_minimize);
                        }
                    }
                });

            }
            linearLayout.addView(costInfoLayout, linearLayout.indexOfChild(costLayout) + 1);


            final boolean[] isMaximized = {false};
            costIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(isMaximized[0]){
                        costInfoLayout.setVisibility(View.GONE);
                        isMaximized[0] = false;
                        costIcon.setImageResource(R.drawable.ic_baseline_plus);

                    }else{
                        costInfoLayout.setVisibility(View.VISIBLE);
                        isMaximized[0] = true;
                        costIcon.setImageResource(R.drawable.icon_minimize);
                    }
                }
            });
        }
    }

}
