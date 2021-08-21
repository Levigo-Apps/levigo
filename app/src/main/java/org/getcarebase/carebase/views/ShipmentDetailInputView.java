package org.getcarebase.carebase.views;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;

import com.google.android.material.snackbar.Snackbar;

import org.getcarebase.carebase.R;
import org.getcarebase.carebase.databinding.ViewShipmentDetailBinding;
import org.getcarebase.carebase.utils.Request;
import org.getcarebase.carebase.utils.Resource;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

/**
 * A view that encapsulates the behavior between the tracking number input and the destination
 * input
 */
public class ShipmentDetailInputView extends LinearLayout {
    public static final String TAG = ShipmentDetailInputView.class.getSimpleName();

    private ViewShipmentDetailBinding binding;

    private OnTrackingNumberSelection onTrackingNumberSelection;
    private OnDestinationSelection onDestinationSelection;

    private ArrayAdapter<String> trackingNumbersAdapter;
    private ArrayAdapter<String> destinationsAdapter;

    private Map<String,String> trackingNumbersToEntityIdMap;
    private Map<String,String> entityIdToEntityNameMap;

    public ShipmentDetailInputView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        binding = DataBindingUtil.inflate(LayoutInflater.from(getContext()),R.layout.view_shipment_detail,(ViewGroup) getParent(),true);
        trackingNumbersAdapter = new ArrayAdapter<>(getContext(), R.layout.dropdown_menu_popup_item, new ArrayList<>());
        destinationsAdapter = new ArrayAdapter<>(getContext(), R.layout.dropdown_menu_popup_item, new ArrayList<>());

        binding.trackingNumberTextView.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String trackingNumber = trackingNumbersAdapter.getItem(position);
                onTrackingNumberSelection.update(trackingNumber);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                onTrackingNumberSelection.update(null);
            }
        });

        binding.destinationTextView.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String destinationName = destinationsAdapter.getItem(position);
                String destinationId = null;
                // destination ID reverse lookup
                for (Map.Entry<String,String> entry : entityIdToEntityNameMap.entrySet()) {
                    if (entry.getValue().equals(destinationName)) {
                        destinationId = entry.getKey();
                    }
                }
                if (destinationId == null) {
                    Log.e(TAG,"destination name is not recognized");
                }
                onDestinationSelection.update(destinationId,destinationName);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                onDestinationSelection.update(null,null);
            }
        });
    }

    public void setTrackingNumbersOptions(Resource<Map<String,String>> trackingNumbersResource) {
        if (trackingNumbersResource.getRequest().getStatus() == Request.Status.SUCCESS) {
            trackingNumbersToEntityIdMap = trackingNumbersResource.getData();
            trackingNumbersAdapter.clear();
            trackingNumbersAdapter.add("Get New Tracking Number");
            trackingNumbersAdapter.addAll(trackingNumbersToEntityIdMap.keySet());

            binding.trackingNumberLayout.setEnabled(true);

            binding.trackingNumberTextView.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    if (charSequence.toString().contentEquals("Get New Tracking Number")) {
                        binding.destinationLayout.setEnabled(true);
                    } else {
                        String entityId = trackingNumbersToEntityIdMap.get(charSequence.toString());
                        Log.d(TAG,"Matching Entity Id: " + entityId);
                        String entityName = entityIdToEntityNameMap.get(entityId);
                        Log.d(TAG, "Matching Entity Name: " + entityName);
                        binding.destinationTextView.setText(entityName);
                    }
                    binding.destinationLayout.setVisibility(View.VISIBLE);
                }

                @Override
                public void afterTextChanged(Editable editable) {}
            });
            binding.trackingNumberTextView.setAdapter(trackingNumbersAdapter);
        } else if (trackingNumbersResource.getRequest().getStatus() == Request.Status.ERROR) {
            Log.d(TAG,"Unable to fetch shipment tracking numbers");
            Snackbar.make(binding.getRoot(), R.string.error_something_wrong, Snackbar.LENGTH_LONG).show();
        }
    }

    public void setOnTrackingNumberSelection(OnTrackingNumberSelection onTrackingNumberSelection) {
        this.onTrackingNumberSelection = onTrackingNumberSelection;
    }

    public interface OnTrackingNumberSelection {
        void update(String trackingNumber);
    }

    public void setDestinationOptions(Resource<Map<String,String>> destinationsResource) {
        if (destinationsResource.getRequest().getStatus() == Request.Status.SUCCESS) {
            entityIdToEntityNameMap = destinationsResource.getData();
            destinationsAdapter.clear();
            Collection<String> siteOptions = entityIdToEntityNameMap.values();
            destinationsAdapter.addAll(siteOptions);
            binding.destinationTextView.setAdapter(destinationsAdapter);
            binding.destinationLayout.setEnabled(true);
        } else if (destinationsResource.getRequest().getStatus() == Request.Status.ERROR) {
            Log.d(TAG,"Unable to fetch sites");
            Snackbar.make(binding.getRoot(), R.string.error_something_wrong, Snackbar.LENGTH_LONG).show();
        }
    }

    public void setOnDestinationSelection(OnDestinationSelection onDestinationSelection) {
        this.onDestinationSelection = onDestinationSelection;
    }

    public interface OnDestinationSelection {
        void update(String destinationId, String destinationName);
    }

}
