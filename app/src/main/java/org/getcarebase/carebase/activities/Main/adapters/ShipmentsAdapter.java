package org.getcarebase.carebase.activities.Main.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import org.getcarebase.carebase.R;
import org.getcarebase.carebase.activities.Main.fragments.ShipmentFragment;
import org.getcarebase.carebase.models.DeviceUsage;
import org.getcarebase.carebase.models.Shipment;
import org.getcarebase.carebase.views.LabeledTextView;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class ShipmentsAdapter extends RecyclerView.Adapter<ShipmentsAdapter.ViewHolder> {
    public static final String TAG = ShipmentsAdapter.class.getName();

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public RecyclerView deviceView;
        public LabeledTextView trackerView;
        public LabeledTextView dateView;
        public LabeledTextView destinationView;
        public LabeledTextView sourceView;
        public ConstraintLayout shipmentLayout;

        public ViewHolder(View view) {
            super(view);

            trackerView = view.findViewById(R.id.tracker_text_view);
            dateView = view.findViewById(R.id.date_text_view);
            destinationView = view.findViewById(R.id.destination_text_view);
            sourceView = view.findViewById(R.id.source_text_view);
            deviceView = view.findViewById(R.id.devices_view);

            shipmentLayout = view.findViewById(R.id.buttons_layout);
            shipmentLayout.setOnClickListener(view1 -> {
                shipmentLayout.setVisibility(View.VISIBLE);
                if(deviceView.getVisibility() == View.GONE){
                    deviceView.setVisibility(View.VISIBLE);
                }
                else {
                    deviceView.setVisibility(View.GONE);
                }
            });
        }
    }

    private final ShipmentFragment shipmentFragment;
    private final List<Shipment> shipmentList = new ArrayList<>();
    private ShipmentFragment.OnBottomReachedCallback onBottomReachedCallback;

    public ShipmentsAdapter(ShipmentFragment shipmentFragment) {
        this.shipmentFragment = shipmentFragment;
    }

    public void setOnBottomReachedCallback(ShipmentFragment.OnBottomReachedCallback onBottomReachedCallback) {
        this.onBottomReachedCallback = onBottomReachedCallback;
    }

    public void setShipments(List<Shipment> shipmentList) {
        this.shipmentList.clear();
        this.shipmentList.addAll(shipmentList);
    }

    @NonNull
    @Override
    public ShipmentsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View shipmentsView = inflater.inflate(R.layout.shipment_item,parent,false);
        return new ShipmentsAdapter.ViewHolder(shipmentsView);
    }

    @Override
    public void onBindViewHolder(ShipmentsAdapter.ViewHolder holder, int position){
        Shipment shipment = shipmentList.get(position);

        holder.trackerView.setValue(shipment.getTrackingNumber());
        holder.dateView.setValue(shipment.getShippedTime());
        holder.destinationView.setValue(shipment.getDestinationEntityId());
        holder.sourceView.setValue(shipment.getSourceEntityId());

        DevicesUsedAdapter devicesUsedAdapter = new DevicesUsedAdapter();
        LinearLayoutManager layoutManager = new LinearLayoutManager(shipmentFragment.getContext());
        holder.deviceView.setLayoutManager(layoutManager);
        holder.deviceView.setAdapter(devicesUsedAdapter);
        devicesUsedAdapter.setDeviceUsages(shipment.getItems().stream().map(item -> {
            DeviceUsage deviceUsage = new DeviceUsage();
            deviceUsage.setUniqueDeviceIdentifier(item.get("udi"));
            deviceUsage.setDeviceIdentifier(item.get("di"));
            deviceUsage.setName(item.get("name"));
            deviceUsage.setAmountUsed(Integer.parseInt(item.get("quantity")));
            return deviceUsage;
        }).collect(Collectors.toList()));

        if ((position >= getItemCount() - 1)){
            onBottomReachedCallback.onBottomReached();
        }
    }

    @Override
    public int getItemCount(){
        return shipmentList.size();
    }
}
