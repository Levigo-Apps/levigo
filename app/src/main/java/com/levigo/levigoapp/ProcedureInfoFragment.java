package com.levigo.levigoapp;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

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

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

public class ProcedureInfoFragment extends Fragment {

    private static final String TAG = ItemDetailOfflineFragment.class.getSimpleName();
    private Activity parent;

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final CollectionReference usersRef = db.collection("users");
    private LinearLayout linearLayout;

    private String mNetworkId;
    private String mHospitalId;
    private String mHospitalName;

    private TextInputLayout dateLayout;
    private TextInputLayout timeInLayout;
    private TextInputLayout timeoutLayout;
    private TextInputLayout accessionNumber;

    private TextInputEditText procedureNameEditText;
    private TextInputEditText procedureDateEditText;
    private TextInputEditText timeInEditText;
    private TextInputEditText timeOutEditText;
    private TextInputEditText fluoroTimeEditText;
    private TextInputEditText accessionNumberEditText;

    private MaterialButton addUdisButton;
    private MaterialButton cancelButton;

    @Override
    public View onCreateView(LayoutInflater inflater,ViewGroup container,Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_procedureinfo, container, false);
        parent = getActivity();
        final Calendar myCalendar = Calendar.getInstance();
        dateLayout = rootView.findViewById(R.id.procedureinfo_date_layout);
        timeInLayout = rootView.findViewById(R.id.procedureinfo_timeIn_layout);
        timeoutLayout = rootView.findViewById(R.id.procedureinfo_timeOut_layout);
        procedureNameEditText = rootView.findViewById(R.id.procedure_name);
        procedureDateEditText = rootView.findViewById(R.id.procedure_date);
        timeInEditText = rootView.findViewById(R.id.procedure_timeIn);
        timeOutEditText = rootView.findViewById(R.id.procedure_timeOut);
        fluoroTimeEditText = rootView.findViewById(R.id.procedure_fluoroTime);
        accessionNumberEditText = rootView.findViewById(R.id.procedure_accessionNumber);
        accessionNumber = rootView.findViewById(R.id.procedureinfo_accessionNumber_layout);
        addUdisButton = rootView.findViewById(R.id.procedure_next_button);
        cancelButton = rootView.findViewById(R.id.procedure_cancel_button);
        MaterialToolbar topToolBar = rootView.findViewById(R.id.topAppBar);

        if (getArguments() != null) {
            HashMap<String, Object> procedureInfo;
            Bundle procedureInfoBundle = this.getArguments();
            if(procedureInfoBundle.get("procedureMap") != null){
                procedureInfo = (HashMap<String, Object>) procedureInfoBundle.getSerializable("procedureMap");
                if(procedureInfo != null) {
                    procedureDateEditText.setText((String) procedureInfo.get("procedure_date"));
                    procedureNameEditText.setText((String) procedureInfo.get("procedure_used"));
                    timeInEditText.setText((String) procedureInfo.get("time_in"));
                    timeOutEditText.setText((String) procedureInfo.get("time_out"));
                    fluoroTimeEditText.setText((String) procedureInfo.get("fluoro_time"));
                    accessionNumberEditText.setText((String) procedureInfo.get("accession_number"));
                }
            }
        }

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
                if (parent != null)
                    parent.onBackPressed();
            }
        });


        //TimePicker dialog pops up when clicked on the icon (Room time in)
        timeInLayout.setEndIconOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                timeLayoutPicker(view, timeInEditText);
            }
        });

        //TimePicker dialog pops up when clicked on the icon (Room time out)
        timeoutLayout.setEndIconOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                timeLayoutPicker(view, timeOutEditText);
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
                procedureDateEditText.setText(String.format("%s", sdf.format(myCalendar.getTime())));
            }
        };

        dateLayout.setEndIconOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new DatePickerDialog(view.getContext(), dateInListener, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        accessionNumberEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                generateNewNumber(rootView,accessionNumberEditText);
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (parent != null)
                    parent.onBackPressed();
            }
        });

        TextWatcher timeTextWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }
            @Override
            public void afterTextChanged(Editable editable) {
                boolean checkProcedureTime = validateFields(new TextInputEditText[]{timeInEditText
                        ,timeOutEditText});
                
                if(checkProcedureTime){
                    calculateFluoroTime(timeInEditText.getText().toString(),
                            timeOutEditText.getText().toString());
                }
            }
        };

        timeInEditText.addTextChangedListener(timeTextWatcher);
        timeOutEditText.addTextChangedListener(timeTextWatcher);

        addUdisButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveAndSend(view);
            }
        });



        return rootView;
    }

    private void saveAndSend(View view){
        AddEquipmentFragment fragment = new AddEquipmentFragment();
        Bundle bundle = new Bundle();
        HashMap<String, Object> procedureInfo = new HashMap<>();
        procedureInfo.put("procedure_used", procedureNameEditText.getText().toString());
        procedureInfo.put("procedure_date", procedureDateEditText.getText().toString());
        procedureInfo.put("time_in", timeInEditText.getText().toString());
        procedureInfo.put("time_out", timeOutEditText.getText().toString());
        procedureInfo.put("fluoro_time", fluoroTimeEditText.getText().toString());
        procedureInfo.put("accession_number", accessionNumberEditText.getText().toString());

        bundle.putSerializable("procedureMap", (Serializable) procedureInfo);
        fragment.setArguments(bundle);

        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        //clears other fragments
        fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.setCustomAnimations(R.anim.fui_slide_in_right, R.anim.fui_slide_out_left);
        fragmentTransaction.add(R.id.activity_main, fragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();

    }

    private void calculateFluoroTime(String timeIn, String timeOut){
        final SimpleDateFormat format = new SimpleDateFormat("HH:mm", Locale.US);
        try {
            Date timeInFormat = format.parse(timeIn);
            long millsIn = Objects.requireNonNull(timeInFormat).getTime();

            Date timeOutFormat = format.parse(timeOut);
            long millsOut = Objects.requireNonNull(timeOutFormat).getTime();

            long millsDif = millsOut - millsIn;
            int hours = (int) millsDif / (1000 * 60 * 60);
            if (hours < 0) {
                hours = hours + 24;
            }
            int mins = (int) (millsDif / (1000 * 60)) % 60;
            String totalTime = (hours * 60 + mins) + " minutes";
            fluoroTimeEditText.setText(totalTime);
        }catch(ParseException e){
            e.printStackTrace();
        }
    }

    private boolean validateFields(TextInputEditText[] fields) {
        for (TextInputEditText currentField : fields) {
            if (Objects.requireNonNull(currentField.getText()).toString().length() <= 0) {
                return false;
            }
        }
        return true;
    }



    private void timeLayoutPicker(View view, final TextInputEditText editText) {
        Calendar mcurrentTime = Calendar.getInstance();
        int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
        int minute = mcurrentTime.get(Calendar.MINUTE);
        TimePickerDialog mTimePicker;
        mTimePicker = new TimePickerDialog(view.getContext(), new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                editText.setText(String.format(Locale.US, "%02d:%02d", selectedHour, selectedMinute));
            }
        }, hour, minute, true);
        mTimePicker.setTitle("Select Time");
        mTimePicker.show();
    }


    // generates new number
    public void generateNewNumber(View view, TextInputEditText accessionNumberEditText) {
        Random rand = new Random();
        String randomAccessionNum = null;
        String randomAccession = String.valueOf(1 + rand.nextInt(999999));
        if (randomAccession.length() == 1) {
            randomAccessionNum = "TZ00000" + randomAccession;
        }
        if (randomAccession.length() == 2) {
            randomAccessionNum = "TZ0000" + randomAccession;
        }
        if (randomAccession.length() == 3) {
            randomAccessionNum = "TZ000" + randomAccession;
        }
        if (randomAccession.length() == 4) {
            randomAccessionNum = "TZ00" + randomAccession;
        }
        if (randomAccession.length() == 5) {
            randomAccessionNum = "TZ0" + randomAccession;
        }
        if (randomAccession.length() == 6) {
            randomAccessionNum = "TZ" + randomAccession;
        }
        checkAccessionNumber(view, randomAccessionNum, accessionNumberEditText);
    }

    //checks whether or not the accession number is unique
    private void checkAccessionNumber(final View view, final String accessionNum, final TextInputEditText accessionNumberEditText) {
        final DocumentReference accessionNumberRef = db.collection("networks")
                .document(mNetworkId)
                .collection("hospitals").document(mHospitalId)
                .collection("accession_numbers").document(accessionNum);


        accessionNumberRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (Objects.requireNonNull(document).exists()) {
                        generateNewNumber(view, accessionNumberEditText);
                    } else {
                        setAccessionNumber(accessionNumberEditText, accessionNum);
                    }
                } else {
                    Log.d(TAG, "Failed with: ", task.getException());
                }
            }
        });
    }

    //sets accession number and save it to the database
    public void setAccessionNumber(TextInputEditText accessionNumberEditText, String accessionNumberStr) {
        accessionNumberEditText.setText((accessionNumberStr));
        accessionNumber.setHint("Accession number");
    }
}
