package org.getcarebase.carebase.activities.Main.fragments;

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
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.TimePicker;
import android.widget.Toast;
import android.widget.Toolbar;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.zxing.integration.android.IntentIntegrator;
import com.journeyapps.barcodescanner.CaptureActivity;

import org.getcarebase.carebase.R;
import org.getcarebase.carebase.models.PendingDevice;
import org.getcarebase.carebase.utils.Request;
import org.getcarebase.carebase.viewmodels.PendingDeviceViewModel;
import org.getcarebase.carebase.viewmodels.ProcedureViewModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

public class ItemDetailOfflineFragment extends Fragment {
    public static final String TAG = ItemDetailOfflineFragment.class.getSimpleName();

    private TextInputEditText udi;
    private TextInputEditText siteLocation;
    private TextInputEditText physicalLocation;
    private TextInputEditText numberAdded;
    private TextInputEditText notes;
    private TextInputEditText dateIn;
    private TextInputEditText timeIn;

    MaterialButton rescanButton;
    MaterialButton saveButton;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,ViewGroup container,Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_itemdetailoffline, container, false);
        final Calendar myCalendar = Calendar.getInstance();
        final MaterialToolbar toolbar = rootView.findViewById(R.id.topAppBar);
        udi = rootView.findViewById(R.id.detail_udi);
        siteLocation = rootView.findViewById(R.id.detail_site_location);
        physicalLocation = rootView.findViewById(R.id.detail_physical_location);
        numberAdded = rootView.findViewById(R.id.detail_number_added);
        notes = rootView.findViewById(R.id.detail_notes);
        dateIn = rootView.findViewById(R.id.detail_in_date);
        timeIn = rootView.findViewById(R.id.detail_in_time);
        TextInputLayout numberAddedLayout = rootView.findViewById(R.id.numberAddedLayout);
        TextInputLayout dateInLayout = rootView.findViewById(R.id.in_date_layout);
        TextInputLayout timeInLayout = rootView.findViewById(R.id.in_time_layout);
        rescanButton = rootView.findViewById(R.id.detail_rescan_button);
        saveButton = rootView.findViewById(R.id.detail_save_button);

        PendingDeviceViewModel pendingDeviceViewModel = new ViewModelProvider(this).get(PendingDeviceViewModel.class);

        pendingDeviceViewModel.getUserLiveData().observe(getViewLifecycleOwner(),userResource -> {
            siteLocation.setText(userResource.getData().getEntityName());
            pendingDeviceViewModel.setupPendingDeviceRepository();
        });

        numberAdded.setOnClickListener(view -> showNumberPicker(rootView, numberAdded));

        // incrementing number by 1 when clicked on the end icon
        numberAddedLayout.setEndIconOnClickListener(view -> incrementNumberAdded());

        //TimePicker dialog pops up when clicked on the icon
        timeInLayout.setEndIconOnClickListener(this::timeInLayoutPicker);

        // going back to the scanner view
        rescanButton.setOnClickListener(view -> {
            IntentIntegrator integrator = new IntentIntegrator(getActivity());
            integrator.setCaptureActivity(CaptureActivity.class);
            integrator.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES);
            integrator.setBarcodeImageEnabled(true);
            integrator.initiateScan();
        });

        // date picker for date in if entered manually
        final DatePickerDialog.OnDateSetListener dateInListener = (datePicker, i, i1, i2) -> {
            myCalendar.set(Calendar.YEAR, i);
            myCalendar.set(Calendar.MONTH, i1);
            myCalendar.set(Calendar.DAY_OF_MONTH, i2);
            String myFormat = "yyyy/MM/dd";
            SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
            dateIn.setText(String.format("%s", sdf.format(myCalendar.getTime())));
        };

        dateInLayout.setEndIconOnClickListener(view -> new DatePickerDialog(view.getContext(), dateInListener, myCalendar
                .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                myCalendar.get(Calendar.DAY_OF_MONTH)).show());


        // saves data into database
        saveButton.setOnClickListener(v -> {
            PendingDevice pendingDevice = validateFields();
            if (pendingDevice != null) {
                pendingDeviceViewModel.savePendingDevice(pendingDevice);
                FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.remove(this).commit();
                Snackbar.make(requireActivity().findViewById(R.id.activity_main), "Pending device saved for later approval", Snackbar.LENGTH_LONG).show();
            } else {
                changeBoxStroke(new TextInputEditText[]{udi,numberAdded, dateIn, timeIn, siteLocation, physicalLocation});
                Snackbar.make(requireView(), R.string.error_missing_required_fields, Snackbar.LENGTH_LONG).show();
            }
        });

        toolbar.setNavigationOnClickListener(view -> requireActivity().onBackPressed());

        if (getArguments() != null) {
            String barcode = getArguments().getString("barcode");
            udi.setText(barcode);
        }

        return rootView;
    }

    private PendingDevice validateFields() {
        for (EditText currentField : new EditText[]{udi,siteLocation,physicalLocation,numberAdded,dateIn,timeIn}) {
            if (Objects.requireNonNull(currentField.getText()).toString().trim().length() <= 0) {
                return null;
            }
        }
        PendingDevice pendingDevice = new PendingDevice();
        pendingDevice.setUniqueDeviceIdentifier(Objects.requireNonNull(udi.getText()).toString().trim());
        pendingDevice.setSiteName(Objects.requireNonNull(siteLocation.getText()).toString().trim());
        pendingDevice.setPhysicalLocation(Objects.requireNonNull(physicalLocation.getText()).toString().trim());
        pendingDevice.setQuantity(Objects.requireNonNull(numberAdded.getText()).toString().trim());
        pendingDevice.setNotes(Objects.requireNonNull(notes.getText()).toString().trim());
        pendingDevice.setDateAdded(Objects.requireNonNull(dateIn.getText()).toString().trim());
        pendingDevice.setTimeAdded(Objects.requireNonNull(timeIn.getText()).toString().trim());
        return pendingDevice;
    }

    private void changeBoxStroke(TextInputEditText[] fields) {
        for (TextInputEditText currentField : fields) {
            if (Objects.requireNonNull(currentField.getText()).toString().length() <= 0) {
                TextInputLayout textInputLayout = (TextInputLayout) currentField.getParent().getParent();
                textInputLayout.setBoxStrokeColor(getResources().getColor(R.color.design_default_color_error,requireActivity().getTheme()));
                textInputLayout.setHelperText(getText(R.string.fui_required_field));
                textInputLayout.setHelperTextTextAppearance(R.style.error_appearance);
            }
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
