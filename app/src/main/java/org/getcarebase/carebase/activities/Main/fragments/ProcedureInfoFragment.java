package org.getcarebase.carebase.activities.Main.fragments;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.text.style.BackgroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TimePicker;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.getcarebase.carebase.R;
import org.getcarebase.carebase.models.Procedure;
import org.getcarebase.carebase.viewmodels.ProcedureViewModel;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Random;

public class ProcedureInfoFragment extends Fragment {

    private View rootView;

    public static final String TAG = ProcedureInfoFragment.class.getSimpleName();

    private AutoCompleteTextView procedureNameEditText;
    private TextInputEditText procedureDateEditText;
    private TextInputEditText timeInEditText;
    private TextInputEditText timeOutEditText;
    private TextInputEditText roomTimeEditText;
    private EditText fluoroTimeEditText;
    private TextInputEditText accessionNumberEditText;

    Calendar myCalendar;

    private ProcedureViewModel procedureViewModel;

    @Override
    public View onCreateView(LayoutInflater inflater,ViewGroup container,Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_procedureinfo, container, false);
        this.rootView = rootView;
        myCalendar = Calendar.getInstance();
        TextInputLayout dateLayout = rootView.findViewById(R.id.procedureinfo_date_layout);
        TextInputLayout timeInLayout = rootView.findViewById(R.id.procedureinfo_timeIn_layout);
        TextInputLayout timeoutLayout = rootView.findViewById(R.id.procedureinfo_timeOut_layout);
        procedureNameEditText = rootView.findViewById(R.id.procedure_name);
        procedureDateEditText = rootView.findViewById(R.id.procedure_date);
        timeInEditText = rootView.findViewById(R.id.procedure_timeIn);
        timeOutEditText = rootView.findViewById(R.id.procedure_timeOut);
        roomTimeEditText = rootView.findViewById(R.id.procedure_roomTime);
        fluoroTimeEditText = rootView.findViewById(R.id.procedure_fluoroTime);
        accessionNumberEditText = rootView.findViewById(R.id.procedure_accessionNumber);
        List<String> procedureNames = new ArrayList<>();
        MaterialToolbar topToolBar = rootView.findViewById(R.id.topAppBar);
        Button continueButton = rootView.findViewById(R.id.procedure_continue_button);

        procedureViewModel = new ViewModelProvider(requireActivity()).get(ProcedureViewModel.class);

        topToolBar.setNavigationOnClickListener(view -> procedureViewModel.goToInventory());

        continueButton.setOnClickListener(view -> {
            if (validateFields()) {
                // Go to add devices used screen
                procedureViewModel.goToDeviceUsed();
            }
        });

        //TimePicker dialog pops up when clicked on the icon (Room time in)
        timeInLayout.setEndIconOnClickListener(view -> timeLayoutPicker(view, timeInEditText));

        //TimePicker dialog pops up when clicked on the icon (Room time out)
        timeoutLayout.setEndIconOnClickListener(view -> timeLayoutPicker(view, timeOutEditText));

        // date picker for date in if entered manually
        final DatePickerDialog.OnDateSetListener dateInListener = (datePicker, i, i1, i2) -> {
            myCalendar.set(Calendar.YEAR, i);
            myCalendar.set(Calendar.MONTH, i1);
            myCalendar.set(Calendar.DAY_OF_MONTH, i2);
            String myFormat = "yyyy/MM/dd";
            SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
            procedureDateEditText.setText(String.format("%s", sdf.format(myCalendar.getTime())));
        };

        dateLayout.setEndIconOnClickListener(view -> new DatePickerDialog(view.getContext(), dateInListener, myCalendar
                .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                myCalendar.get(Calendar.DAY_OF_MONTH)).show());

        accessionNumberEditText.setOnClickListener(view -> {
            accessionNumberEditText.setText(generateNewNumber());
        });

        ArrayAdapter<String> procedureNamesAdapter = new ArrayAdapter<>(
                rootView.getContext(),
                R.layout.dropdown_menu_popup_item,
                procedureNames);
        List<String> namesList = Arrays.asList
                (
                        "Angioplasty and Stent Insertion",
                        "Ascitic Tap",
                        "Biliary Drainage",
                        "Bursal Injection",
                        "Carotid Stenting",
                        "Carpal Tunnel Ultrasound and Injection",
                        "Image Guided Cervical Nerve Root Sleeve Corticosteroid Injection",
                        "Image Guided Liver Biopsy",
                        "Image Guided Lumbar Epidural Corticosteroid Injection",
                        "Image guided lumbar nerve root sleeve injection",
                        "Inferior Vena Cava Filters",
                        "Joint Injection",
                        "Nephrostomy",
                        "Pleural Aspiration",
                        "Radiofrequency Ablation",
                        "SAH Vasospasm Endovascular Treatment",
                        "Selective Internal Radiation Therapy [SIRT]: SIR-Spheres®",
                        "Spinal Cord Embolisation (AVM/DAVF)",
                        "Thyroid fine needle aspiration (FNA)",
                        "Transarterial Chemoembolisation (TACE)",
                        "Uterine Fibroid Embolisation",
                        "Varicose Vein Ablation",
                        "Vascular Closure Devices",
                        "Venous Access",
                        "Vertebroplasty",
                        "Other"
                );
        procedureNames.addAll(namesList);
        Collections.sort(procedureNames);
        procedureNameEditText.setAdapter(procedureNamesAdapter);

        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            @Override
            public void afterTextChanged(Editable editable) {
                calculateRoomTime(Objects.requireNonNull(timeInEditText.getText()).toString(),
                            Objects.requireNonNull(timeOutEditText.getText()).toString(),roomTimeEditText);
            }
        };

        timeInEditText.addTextChangedListener(textWatcher);
        timeOutEditText.addTextChangedListener(textWatcher);

        return rootView;
    }

    private void calculateRoomTime(String timeIn, String timeOut, TextInputEditText roomTime){
        final SimpleDateFormat format = new SimpleDateFormat("HH:mm", Locale.US);
        try {
            Date timeInFormat = format.parse(timeIn);
            long millsIn = Objects.requireNonNull(timeInFormat).getTime();

            Date timeOutFormat = format.parse(timeOut);
            long millsOut = Objects.requireNonNull(timeOutFormat).getTime();

            long millsDif = millsOut - millsIn;
            int mins = (int) millsDif / (1000 * 60);
            String totalTime = (mins < 0 ? mins + 24*60 : mins) + "";

            roomTime.setText(totalTime);
        }catch(ParseException e){
            e.printStackTrace();
        }
    }

    private boolean validateFields() {
        for (EditText currentField : new EditText[]{procedureNameEditText,procedureDateEditText,timeInEditText,timeOutEditText,
                roomTimeEditText,fluoroTimeEditText,accessionNumberEditText}) {
            if (Objects.requireNonNull(currentField.getText()).toString().trim().length() <= 0) {
                Snackbar.make(rootView, R.string.error_missing_required_fields, Snackbar.LENGTH_LONG).show();
                return false;
            }
        }
        String fluoro = checkFluoroTimeFormat();
        if (fluoro == null) {
            Snackbar.make(rootView, "Please enter valid fluoro time with format shown", Snackbar.LENGTH_LONG).show();
            return false;
        }

        Procedure procedure = new Procedure();
        procedure.setName(procedureNameEditText.getText().toString().trim());
        procedure.setDate(procedureDateEditText.getText().toString().trim());
        procedure.setTimeIn(timeInEditText.getText().toString().trim());
        procedure.setTimeOut(timeOutEditText.getText().toString().trim());
        procedure.setRoomTime(roomTimeEditText.getText().toString().trim() + " minutes");
        procedure.setFluoroTime(fluoro);
        procedure.setAccessionNumber(accessionNumberEditText.getText().toString().trim());

        procedureViewModel.setProcedureDetails(procedure);
        return true;
    }

    private String checkFluoroTimeFormat() {
        // must in format m:ss or mm:ss
        String fluoro = fluoroTimeEditText.getText().toString().trim();
        if (fluoro.length() < 4 || fluoro.length() > 5) {
            return null;
        }
        String[] time = fluoro.split(":", -2); // do not discard empty strings
        // contain wrong number of ":"
        if (time.length != 2) {
            return null;
        }
        String min = time[0];
        String sec = time[1];
        // only allow 1 or 2 digits mm and 2 digits ss
        if (min.length() == 0 || min.length() > 2 ||
                sec.length() != 2 || Integer.valueOf(sec) >= 60) {
            return null;
        }
        if (Integer.valueOf(min) == 0) {
            min = "0";
        }
        return min+" min "+sec+" sec";
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
    public String generateNewNumber() {
        Random rand = new Random();
        String randomAccessionNum = "";
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
        return randomAccessionNum;
    }

//    //checks whether or not the accession number is unique
//    private void checkAccessionNumber(final View view, final String accessionNum, final TextInputEditText accessionNumberEditText) {
//        final DocumentReference accessionNumberRef = db.collection("networks")
//                .document(mNetworkId)
//                .collection("hospitals").document(mHospitalId)
//                .collection("accession_numbers").document(accessionNum);
//
//
//        accessionNumberRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
//            @Override
//            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
//                if (task.isSuccessful()) {
//                    DocumentSnapshot document = task.getResult();
//                    if (Objects.requireNonNull(document).exists()) {
//                        generateNewNumber(view, accessionNumberEditText);
//                    } else {
//                        setAccessionNumber(accessionNumberEditText, accessionNum);
//                    }
//                } else {
//                    Log.d(TAG, "Failed with: ", task.getException());
//                }
//            }
//        });
//    }
//
//    //sets accession number
//    public void setAccessionNumber(TextInputEditText accessionNumberEditText, String accessionNumberStr) {
//        accessionNumberEditText.setText((accessionNumberStr));
//        accessionNumber.setHint("Accession number");
//    }
}
