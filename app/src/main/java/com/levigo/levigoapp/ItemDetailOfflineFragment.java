package com.levigo.levigoapp;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.zxing.integration.android.IntentIntegrator;
import com.journeyapps.barcodescanner.CaptureActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

public class ItemDetailOfflineFragment extends Fragment {

    private static final String TAG = ItemDetailFragment.class.getSimpleName();
    private Activity parent;

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final CollectionReference usersRef = db.collection("users");

    private LinearLayout linearLayout;

    private String mNetworkId;
    private String mHospitalId;
    private String mHospitalName;
    private TextInputEditText udi;
    private AutoCompleteTextView siteLocation;
    private TextInputEditText physicalLocation;
    private TextInputEditText numberAdded;
    private TextInputEditText notes;
    private TextInputEditText dateIn;
    private TextInputEditText timeIn;
    private TextInputEditText otherSite_text;

    private TextInputLayout siteLocationLayout;
    private TextInputLayout numberAddedLayout;
    private TextInputLayout dateInLayout;
    private TextInputLayout timeInLayout;
    private ArrayList<String> SITELOC;

    private boolean chosenSite;
    MaterialButton rescanButton;
    MaterialButton saveButton;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,ViewGroup container,Bundle savedInstanceState) {

        final View rootView = inflater.inflate(R.layout.fragment_itemdetailoffline, container, false);
        parent = getActivity();
        final Calendar myCalendar = Calendar.getInstance();
        linearLayout = rootView.findViewById(R.id.itemdetailoffline_linearlayout);
        udi = rootView.findViewById(R.id.detail_udi);
        siteLocation = rootView.findViewById(R.id.detail_site_location);
        physicalLocation = rootView.findViewById(R.id.detail_physical_location);
        numberAdded = rootView.findViewById(R.id.detail_number_added);
        notes = rootView.findViewById(R.id.detail_notes);
        dateIn = rootView.findViewById(R.id.detail_in_date);
        timeIn = rootView.findViewById(R.id.detail_in_time);
        siteLocationLayout = rootView.findViewById(R.id.siteLocationLayout);
        numberAddedLayout = rootView.findViewById(R.id.numberAddedLayout);
        dateInLayout= rootView.findViewById(R.id.in_date_layout);
        timeInLayout = rootView.findViewById(R.id.in_time_layout);
        rescanButton = rootView.findViewById(R.id.detail_rescan_button);
        saveButton = rootView.findViewById(R.id.detail_save_button);
        SITELOC = new ArrayList<>();
        chosenSite = false;
        MaterialToolbar topToolBar = rootView.findViewById(R.id.topAppBar);

        topToolBar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (parent != null)
                    parent.onBackPressed();
            }
        });


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
                            setAdapter(rootView);
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

        numberAdded.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showNumberPicker(rootView, numberAdded);
            }
        });

        // incrementing number by 1 when clicked on the end icon
        numberAddedLayout.setEndIconOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                incrementNumberAdded();
            }
        });

        //TimePicker dialog pops up when clicked on the icon
        timeInLayout.setEndIconOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                timeInLayoutPicker(view);
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
            }
        });

        // date picker for date in if entered manually
        final DatePickerDialog.OnDateSetListener dateInListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
                myCalendar.set(Calendar.YEAR, i);
                myCalendar.set(Calendar.MONTH, i1);
                myCalendar.set(Calendar.DAY_OF_MONTH, i2);
                String myFormat = "yyyy/MM/dd";
                SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
                dateIn.setText(String.format("%s", sdf.format(myCalendar.getTime())));
            }
        };

        dateInLayout.setEndIconOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new DatePickerDialog(view.getContext(), dateInListener, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        return rootView;
    }

    // Dropdown menu for Site Location field
    public void setAdapter(View rootView){
        final ArrayAdapter<String> adapterSite =
                new ArrayAdapter<>(
                        rootView.getContext(),
                        R.layout.dropdown_menu_popup_item,
                        SITELOC);
        adapterSite.add("Other");
        System.out.println(mHospitalName);
        adapterSite.add(mHospitalName);
        siteLocation.setAdapter(adapterSite);
        siteLocation.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                addNewSite(adapterView, view, i);
            }
        });

    }

    private void addNewSite(final AdapterView<?> adapterView, View view, int i) {
        String selected = (String) adapterView.getItemAtPosition(i);
        TextInputLayout other_site_layout;
        if (selected.equals("Other")) {
            chosenSite = true;
            other_site_layout = (TextInputLayout) View.inflate(view.getContext(),
                    R.layout.activity_itemdetail_materialcomponent, null);
            other_site_layout.setHint("Enter site");
            other_site_layout.setId(View.generateViewId());
            other_site_layout.setGravity(Gravity.END);
            otherSite_text = new TextInputEditText(other_site_layout.getContext());
            TextWatcher siteTextWatcher = new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void afterTextChanged(Editable editable) {
                    if (!(otherSite_text.toString().trim().isEmpty())) {
                     //   saveButton.setEnabled(true);
                    }

                }
            };
            otherSite_text.addTextChangedListener(siteTextWatcher);
            otherSite_text.setLayoutParams(new LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT));
            other_site_layout.addView(otherSite_text);
            linearLayout.addView(other_site_layout, 1 + linearLayout.indexOfChild(siteLocationLayout));

            MaterialButton submitOtherSite = new MaterialButton(view.getContext(),
                    null, R.attr.materialButtonOutlinedStyle);
            submitOtherSite.setText(R.string.submitSite_lbl);
            submitOtherSite.setLayoutParams(new LinearLayout.LayoutParams(MATCH_PARENT,
                    WRAP_CONTENT));
            other_site_layout.addView(submitOtherSite);
        } else if (chosenSite) {

            chosenSite = false;
            linearLayout.removeViewAt(1 + linearLayout.indexOfChild(siteLocationLayout));
        }
    }

    private void timeInLayoutPicker(View view) {
        Calendar mcurrentTime = Calendar.getInstance();
        int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
        int minute = mcurrentTime.get(Calendar.MINUTE);
        TimePickerDialog mTimePicker;
        mTimePicker = new TimePickerDialog(view.getContext(), new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                timeIn.setText(String.format(Locale.US, "%02d:%02d", selectedHour, selectedMinute));
            }
        }, hour, minute, true);
        mTimePicker.setTitle("Select Time");
        mTimePicker.show();
    }

    private void incrementNumberAdded() {
        int newNumber = 0;
        try {
            newNumber = Integer.parseInt(Objects.requireNonNull(numberAdded.getText()).toString());
        } catch (NumberFormatException e) {
            Log.d(TAG, e.toString());
        } finally {
            numberAdded.setText(String.valueOf(++newNumber));
        }
    }

    private void showNumberPicker(View view, final TextInputEditText editTextAdded) {

        final Dialog d = new Dialog(view.getContext());
        d.setTitle("NumberPicker");
        d.setContentView(R.layout.dialog);
        Button b1 = d.findViewById(R.id.button1);
        Button b2 = d.findViewById(R.id.button2);
        final NumberPicker np = d.findViewById(R.id.numberPicker1);
        np.setMaxValue(1000); // max value 100
        np.setMinValue(0);   // min value 0

        np.setWrapSelectorWheel(true);
        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editTextAdded.setText(String.valueOf(np.getValue())); //set the value to textview
                d.dismiss();
            }
        });
        b2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                d.dismiss(); // dismiss the dialog
            }
        });
        d.show();

    }
}
