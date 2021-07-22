package org.getcarebase.carebase.activities.Main.fragments;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.ImageSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.TimePicker;

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
import com.google.android.material.chip.ChipDrawable;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.zxing.integration.android.IntentIntegrator;
import com.journeyapps.barcodescanner.CaptureActivity;

import org.getcarebase.carebase.R;;
import org.getcarebase.carebase.activities.Main.MainActivity;
import org.getcarebase.carebase.models.Cost;
import org.getcarebase.carebase.models.DeviceModel;
import org.getcarebase.carebase.models.DeviceProduction;
import org.getcarebase.carebase.models.PendingDevice;
import org.getcarebase.carebase.utils.Request;
import org.getcarebase.carebase.viewmodels.DeviceViewModel;
import org.getcarebase.carebase.viewmodels.ProcedureViewModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.stream.Collectors;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

public class ItemDetailFragment extends Fragment {
    private static final String TAG = ItemDetailFragment.class.getSimpleName();
    private MainActivity parent;
    private Calendar myCalendar;

    // USER INPUT VALUES
    private TextInputEditText udiEditText;
    private TextInputEditText nameEditText;
    private TextInputEditText equipmentType;
    private TextInputEditText equipmentTags;
    private TextInputEditText company;
    private TextInputEditText deviceIdentifier;
    private TextInputEditText deviceDescription;
    private TextInputEditText expiration;
    private TextInputEditText quantity;
    private TextInputEditText lotNumber;
    private TextInputEditText referenceNumber;
    private TextInputEditText numberAdded;
    private TextView specsTextView;
    private LinearLayout linearLayout;
//    private TextInputEditText costEditText;

    private Button saveButton;
    private MaterialButton removeSizeButton;
    private Button addSizeButton;

    private int emptySizeFieldCounter = 0;
    private boolean isAddSizeButtonClicked;
    private List<TextInputEditText> allSizeOptions;

    private float dp;

    private DeviceViewModel deviceViewModel;
    private View rootView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        dp = requireActivity().getResources().getDisplayMetrics().density;
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_itemdetail, container, false);
        myCalendar = Calendar.getInstance();
        parent = (MainActivity) requireActivity();
        linearLayout = rootView.findViewById(R.id.itemdetail_linearlayout);
        udiEditText = rootView.findViewById(R.id.detail_udi);
        nameEditText = rootView.findViewById(R.id.detail_name);
        equipmentType = rootView.findViewById(R.id.detail_type);
        equipmentTags = rootView.findViewById(R.id.detail_tags);
        company = rootView.findViewById(R.id.detail_company);
        expiration = rootView.findViewById(R.id.detail_expiration_date);
        lotNumber = rootView.findViewById(R.id.detail_lot_number);
        referenceNumber = rootView.findViewById(R.id.detail_reference_number);
        numberAdded = rootView.findViewById(R.id.detail_number_added);
        deviceIdentifier = rootView.findViewById(R.id.detail_di);
        deviceDescription = rootView.findViewById(R.id.detail_description);
        quantity = rootView.findViewById(R.id.detail_quantity);
        TextInputLayout expirationTextLayout = rootView.findViewById(R.id.expiration_date_string);
        saveButton = rootView.findViewById(R.id.detail_save_button);
        Button rescanButton = rootView.findViewById(R.id.detail_rescan_button);
        final Button autoPopulateButton = rootView.findViewById(R.id.detail_autopop_button);
        TextInputLayout numberAddedLayout = rootView.findViewById(R.id.numberAddedLayout);
        MaterialToolbar topToolBar = rootView.findViewById(R.id.topAppBar);
//        costEditText = rootView.findViewById(R.id.detail_equipment_cost);
        isAddSizeButtonClicked = true;
        addSizeButton = rootView.findViewById(R.id.button_addsize);
        specsTextView = rootView.findViewById(R.id.detail_specs_textview);
        allSizeOptions = new ArrayList<>();

        deviceViewModel = new ViewModelProvider(this).get(DeviceViewModel.class);
        // if it is viable, find a way to get the user from inventoryViewModel from main activity
        // instead of waiting again to get the user again
        parent.showLoadingScreen();
        deviceViewModel.getUserLiveData().observe(getViewLifecycleOwner(), userResource -> {
            deviceViewModel.setupDeviceRepository();
            deviceViewModel.setupEntityRepository();
            deviceViewModel.getEntityType().observe(getViewLifecycleOwner(), entityTypeResource -> {
                setupAutoPopulate();
                setupSaveDevice();
                parent.removeLoadingScreen();
                handleArguments();
            });
        });

        // NumberPicker Dialog for NumberAdded field
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

        autoPopulateButton.setOnClickListener(view -> {
            String barcode = Objects.requireNonNull(udiEditText.getText()).toString().trim();
            if (!barcode.isEmpty()) {
                deviceViewModel.autoPopulatedScannedBarcode(barcode);
                hideKeyboard();
            }
        });

        addSizeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addEmptySizeOption(view);
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

        //going back to inventory view
        topToolBar.setNavigationOnClickListener(v -> requireActivity().getSupportFragmentManager().popBackStack());

        // date picker for expiration date if entered manually
        final DatePickerDialog.OnDateSetListener date_exp = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
                myCalendar.set(Calendar.YEAR, i);
                myCalendar.set(Calendar.MONTH, i1);
                myCalendar.set(Calendar.DAY_OF_MONTH, i2);
                String myFormat = "yyyy/MM/dd";
                SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
                expiration.setText(String.format("%s", sdf.format(myCalendar.getTime())));
            }
        };

        expirationTextLayout.setEndIconOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new DatePickerDialog(view.getContext(), date_exp, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        // saves data into database
        saveButton.setOnClickListener(v -> saveData());
        return rootView;
    }

    private void hideKeyboard() {
        try {
            InputMethodManager imm = (InputMethodManager) requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), 0);
        } catch (Exception e) {
            Log.d(TAG,"Keyboard not open");
        }
    }

    private void setupSaveDevice() {
        deviceViewModel.getSaveDeviceRequestLiveData().observe(getViewLifecycleOwner(),event -> {
            Request request = event.getContentIfNotHandled();
            if (request == null) {
                return;
            }
            if (request.getStatus() == org.getcarebase.carebase.utils.Request.Status.SUCCESS) {
                FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
                // if addEquipmentFragment is in the backstack tell the fragment to retry getting the
                // device from the database
                Fragment addEquipmentFragment = fragmentManager.findFragmentByTag(AddEquipmentFragment.TAG);
                View nextView = requireActivity().findViewById(R.id.activity_main);
                if (addEquipmentFragment != null) {
                    ProcedureViewModel procedureViewModel = new ViewModelProvider(requireActivity()).get(ProcedureViewModel.class);
                    procedureViewModel.retryDeviceUsed();
                    nextView = addEquipmentFragment.getView();
                }

                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.remove(this).commit();
                Snackbar.make(nextView, "Device saved to inventory", Snackbar.LENGTH_LONG).show();
            } else {
                Log.d(TAG,"error while saving device");
                Snackbar.make(rootView, R.string.error_something_wrong, Snackbar.LENGTH_LONG).show();
            }
        });
    }

    private void setupAutoPopulate() {
        boolean isDistributor = deviceViewModel.getEntityType().getValue().getData().equals("distributor");
        deviceViewModel.getAutoPopulatedDeviceLiveData().observe(getViewLifecycleOwner(), deviceModelResource -> {
            if (deviceModelResource.getRequest().getStatus() == org.getcarebase.carebase.utils.Request.Status.SUCCESS) {
                parent.removeLoadingScreen();
                DeviceModel deviceModel = deviceModelResource.getData();
                if (deviceModel.getProductions().size() != 0) {
                    DeviceProduction deviceProduction = deviceModel.getProductions().get(0);
                    expiration.setText(deviceProduction.getExpirationDate());
                    expiration.setEnabled(deviceProduction.getExpirationDate() == null || isDistributor);
                    lotNumber.setText(deviceProduction.getLotNumber());
                    lotNumber.setEnabled(deviceProduction.getLotNumber() == null || isDistributor);
                    referenceNumber.setText(deviceProduction.getReferenceNumber());
                    referenceNumber.setEnabled(deviceProduction.getReferenceNumber() == null || isDistributor);
                }

                deviceIdentifier.setText(deviceModel.getDeviceIdentifier());
                deviceIdentifier.setEnabled(deviceModel.getDeviceIdentifier() == null || isDistributor);
                nameEditText.setText(deviceModel.getName());
                nameEditText.setEnabled(deviceModel.getName() == null || isDistributor);
                quantity.setText(Integer.toString(deviceModel.getQuantity()));
                quantity.setEnabled(false);
                equipmentType.setText(deviceModel.getEquipmentType());
                equipmentType.setEnabled(false);
                setEquipmentTags(deviceModel.getTags());
                deviceDescription.setText(deviceModel.getDescription());
                deviceDescription.setEnabled(deviceModel.getDescription() == null || isDistributor);
                company.setText(deviceModel.getCompany());
                company.setEnabled(deviceModel.getCompany() == null || isDistributor);
                if (numberAdded.getText() == null || numberAdded.getText().toString().trim().isEmpty()) {
                    numberAdded.setText("1");
                }
                for (Map.Entry<String,Object> specification : deviceModel.getSpecificationList()) {
                    addItemSpecs(specification.getKey(),specification.getValue().toString(),rootView);
                }
            } else if (deviceModelResource.getRequest().getStatus() == org.getcarebase.carebase.utils.Request.Status.ERROR) {
                parent.removeLoadingScreen();
                Snackbar.make(rootView, deviceModelResource.getRequest().getResourceString(), Snackbar.LENGTH_LONG).show();
            }
        });
    }

    public void handleArguments() {
        if (getArguments() != null) {
            String barcode;
            String pendingDeviceId;
            if ((barcode = getArguments().getString("barcode")) != null) {
                parent.showLoadingScreen();
                udiEditText.setText(barcode);
                deviceViewModel.autoPopulatedScannedBarcode(barcode);
            // this device is a pending device
            } else if ((pendingDeviceId = getArguments().getString("pending_device_id")) != null) {
                deviceViewModel.getPendingDeviceLiveData().observe(getViewLifecycleOwner(),pendingDeviceResource -> {
                    if (pendingDeviceResource.getRequest().getStatus() == Request.Status.SUCCESS) {
                        setPendingDataFields(pendingDeviceResource.getData());
                    } else if (pendingDeviceResource.getRequest().getStatus() == Request.Status.ERROR) {
                        FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
                        fragmentManager.popBackStack();
                        Fragment fragment = fragmentManager.findFragmentByTag(PendingUdiFragment.TAG);
                        Snackbar.make(Objects.requireNonNull(fragment).requireView(),pendingDeviceResource.getRequest().getResourceString(),Snackbar.LENGTH_LONG).show();
                    }
                });
                deviceViewModel.setPendingDeviceIdLiveData(pendingDeviceId);
            }

        }
    }

    private void setEquipmentTags(List<String> tags) {
        int spannedLength = 0;
        for (String tag : tags) {
            equipmentTags.append(tag);
            ChipDrawable chip = ChipDrawable.createFromResource(requireContext(), R.xml.standalone_chip);
            chip.setText(tag);
            chip.setBounds(0, 0, chip.getIntrinsicWidth(), chip.getIntrinsicHeight());
            ImageSpan span = new ImageSpan(chip);
            Editable text = equipmentTags.getText();
            text.setSpan(span, spannedLength, text.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
            spannedLength += tag.length();
        }
        equipmentTags.setEnabled(false);
    }

    private void setPendingDataFields(PendingDevice pendingDevice) {
        udiEditText.setText(pendingDevice.getUniqueDeviceIdentifier());
        numberAdded.setText(pendingDevice.getQuantity());
    }

    // not in mvvm style - need to use data bindings
    // packages all the fields into a DeviceModel object
    private DeviceModel isFieldsValid() {
        boolean isValid = true;

        List<EditText> requiredEditTexts = new ArrayList<>(allSizeOptions);
        requiredEditTexts.addAll(Arrays.asList(udiEditText, deviceIdentifier, nameEditText, expiration, equipmentType, company, numberAdded));
        for (EditText editText : requiredEditTexts) {
            if (editText.getText().toString().trim().isEmpty()) {
                isValid = false;
            }
        }

        if (isValid) {
            DeviceModel deviceModel = new DeviceModel();
            deviceModel.setDeviceIdentifier(Objects.requireNonNull(deviceIdentifier.getText()).toString().trim());
            deviceModel.setName(Objects.requireNonNull(nameEditText.getText()).toString().trim());
            deviceModel.setCompany(Objects.requireNonNull(company.getText()).toString().trim());
            deviceModel.setDescription(Objects.requireNonNull(deviceDescription.getText()).toString().trim());
            deviceModel.setEquipmentType(equipmentType.getText().toString().trim());
            int currentQuantity = Integer.parseInt(Objects.requireNonNull(quantity.getText()).toString());
            int amount = Integer.parseInt(Objects.requireNonNull(numberAdded.getText()).toString());
            deviceModel.setQuantity(currentQuantity + amount);

            if (allSizeOptions.size() > 0) {
                int i = 0;
                while (i < allSizeOptions.size()) {
                    String key = Objects.requireNonNull(allSizeOptions.get(i++).getText()).toString().trim();
                    String value = Objects.requireNonNull(allSizeOptions.get(i++).getText()).toString().trim();
                    deviceModel.addSpecification(key,value);
                }
            }

            DeviceProduction deviceProduction = new DeviceProduction();
            deviceProduction.setUniqueDeviceIdentifier(Objects.requireNonNull(udiEditText.getText()).toString().trim());
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd", Locale.US);
            deviceProduction.setDateAdded(dateFormat.format(new Date()));
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss", Locale.US);
            deviceProduction.setTimeAdded(timeFormat.format((new Date())));
            deviceProduction.setExpirationDate(Objects.requireNonNull(expiration.getText()).toString().trim());
            deviceProduction.setLotNumber(Objects.requireNonNull(lotNumber.getText()).toString().trim());
            deviceProduction.setQuantity(amount);
            deviceModel.addDeviceProduction(deviceProduction);

//            if(!Objects.requireNonNull(costEditText.getText()).toString().trim().isEmpty()){
//                String cleanString = costEditText.getText().toString().replaceAll("[$,.]", "");
//                double packagePrice = Double.parseDouble(cleanString) / 100;
//                Cost cost = new Cost(Objects.requireNonNull(dateIn.getText()).toString(),amount,packagePrice);
//                deviceProduction.addCost(cost);
//            }

            return deviceModel;
        }
        return null;
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
        b1.setOnClickListener(v -> {
            //set the value to textview
            editTextAdded.setText(String.valueOf(np.getValue()));
            d.dismiss();
        });
        b2.setOnClickListener(v -> d.dismiss());
        d.show();

    }

    // adds new row of size text views if users clicks on a button
    int rowIndex = 1;
    int rowLoc = 1;

    private void addEmptySizeOption(View view) {
        Log.d(TAG, "Adding empty size option!");
        emptySizeFieldCounter++;
        LinearLayout layoutSize = new LinearLayout(getContext());
        layoutSize.setOrientation(LinearLayout.HORIZONTAL);
        layoutSize.setLayoutParams(new LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT));

        TextInputLayout sizeKeyLayout = new TextInputLayout(view.getContext());
        sizeKeyLayout.setHint("Key");
        LinearLayout.LayoutParams klp = new LinearLayout.LayoutParams(0, MATCH_PARENT, 1f);
        klp.setMargins((int) (4 * dp), (int) (4 * dp), (int) (4 * dp), (int) (4 * dp));
        sizeKeyLayout.setLayoutParams(klp);
        TextInputEditText sizeKey = new TextInputEditText(sizeKeyLayout.getContext());
        sizeKey.setSingleLine();
        sizeKey.setEllipsize(TextUtils.TruncateAt.END);

        TextInputLayout sizeValueLayout = new TextInputLayout(view.getContext());
        sizeValueLayout.setHint("Value");
        LinearLayout.LayoutParams vlp = new LinearLayout.LayoutParams(0, MATCH_PARENT, 1f);
        vlp.setMargins((int) (4 * dp), (int) (4 * dp), (int) (4 * dp), (int) (4 * dp));
        sizeValueLayout.setLayoutParams(vlp);
        TextInputEditText sizeValue = new TextInputEditText(sizeKeyLayout.getContext());
        sizeValue.setSingleLine();
        sizeValue.setEllipsize(TextUtils.TruncateAt.END);

        sizeKeyLayout.addView(sizeKey);
        sizeValueLayout.addView(sizeValue);
        layoutSize.addView(sizeKeyLayout);
        layoutSize.addView(sizeValueLayout);

        allSizeOptions.add(sizeKey);
        allSizeOptions.add(sizeValue);
        linearLayout.addView(layoutSize, (rowLoc++) + linearLayout.indexOfChild(specsTextView));
        rowIndex++;

        if (isAddSizeButtonClicked) {
            removeSizeButton = new MaterialButton(view.getContext(),
                    null, R.attr.materialButtonOutlinedStyle);
            removeSizeButton.setText(R.string.removeSize_label);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT);
            lp.setMargins((int) (4 * dp), 0, (int) (4 * dp), 0);
            removeSizeButton.setLayoutParams(lp);
            linearLayout.addView(removeSizeButton, linearLayout.indexOfChild(addSizeButton));
        }

        removeSizeButton.setOnClickListener(buttonView -> removeEmptySizeOption());
        isAddSizeButtonClicked = false;
    }

    //removes one row of size text entry
    private void removeEmptySizeOption() {
        if (emptySizeFieldCounter > 0) {
            linearLayout.removeViewAt(linearLayout.indexOfChild(specsTextView) + --rowLoc);
            emptySizeFieldCounter--;

        }
        if (emptySizeFieldCounter == 0) {
            linearLayout.removeViewAt(linearLayout.indexOfChild(removeSizeButton));
            isAddSizeButtonClicked = true;
        }
        allSizeOptions.remove(allSizeOptions.size() - 1);
        allSizeOptions.remove(allSizeOptions.size() - 1);
    }

    private void addItemSpecs(String key, String value, View view) {
        Log.d(TAG, "Adding item specs!");

        LinearLayout layoutSize = new LinearLayout(view.getContext());
        layoutSize.setOrientation(LinearLayout.HORIZONTAL);
        layoutSize.setLayoutParams(new LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT));

        TextInputLayout sizeKeyLayout = new TextInputLayout(view.getContext());
        sizeKeyLayout.setHint("Key");
        LinearLayout.LayoutParams klp = new LinearLayout.LayoutParams(0, MATCH_PARENT, 1f);
        klp.setMargins((int) (4 * dp), (int) (4 * dp), (int) (4 * dp), (int) (4 * dp));
        sizeKeyLayout.setLayoutParams(klp);
        TextInputEditText sizeKey = new TextInputEditText(sizeKeyLayout.getContext());
        sizeKey.setSingleLine();
        sizeKey.setEllipsize(TextUtils.TruncateAt.END);
        sizeKey.setEnabled(false);
        sizeKey.setText(key);

        TextInputLayout sizeValueLayout = new TextInputLayout(view.getContext());
        sizeValueLayout.setHint("Value");
        LinearLayout.LayoutParams vlp = new LinearLayout.LayoutParams(0, MATCH_PARENT, 1f);
        vlp.setMargins((int) (4 * dp), (int) (4 * dp), (int) (4 * dp), (int) (4 * dp));
        sizeValueLayout.setLayoutParams(vlp);
        TextInputEditText sizeValue = new TextInputEditText(sizeKeyLayout.getContext());
        sizeValue.setSingleLine();
        sizeValue.setEllipsize(TextUtils.TruncateAt.END);
        sizeValue.setEnabled(false);
        sizeValue.setText(value);

        sizeKeyLayout.addView(sizeKey);
        sizeValueLayout.addView(sizeValue);

        layoutSize.addView(sizeKeyLayout);
        layoutSize.addView(sizeValueLayout);

        allSizeOptions.add(sizeKey);
        allSizeOptions.add(sizeValue);
        linearLayout.addView(layoutSize, (rowLoc++) + linearLayout.indexOfChild(specsTextView));
        rowIndex++;
    }

    private void saveData() {
        DeviceModel deviceModel = isFieldsValid();
        if (deviceModel != null) {
            deviceViewModel.saveDevice(deviceModel);
        } else {
            Snackbar.make(rootView, "Please fill all required fields", Snackbar.LENGTH_LONG).show();
        }
    }
}