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

import java.text.DateFormat;
import java.time.format.DateTimeFormatter;
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
        public LabeledTextView shippedDateView;
        public LabeledTextView receivedDateView;
        public LabeledTextView destinationView;
        public LabeledTextView sourceView;
        public ImageButton dropdownButton;

        public ViewHolder(View view) {
            super(view);

            trackerView = view.findViewById(R.id.tracker_text_view);
            shippedDateView = view.findViewById(R.id.shipped_date_text_view);
            receivedDateView = view.findViewById(R.id.recieved_date_text_view);
            destinationView = view.findViewById(R.id.destination_text_view);
            sourceView = view.findViewById(R.id.source_text_view);
            deviceView = view.findViewById(R.id.devices_view);
            dropdownButton = view.findViewById(R.id.dropdown);
        }
    }

    private final ShipmentFragment shipmentFragment;
    private final List<Shipment> shipmentList = new ArrayList<>();
    private final List<Boolean> shipmentVisibilities = new ArrayList<>();
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
        this.shipmentVisibilities.clear();
        this.shipmentVisibilities.addAll(this.shipmentList.stream().map(i -> false).collect(Collectors.toList()));
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
        String formattedShippingTime = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(shipment.getShippedTime());
        holder.shippedDateView.setValue(formattedShippingTime);
        if (shipment.getReceivedTime() != null) {
            String formattedReceivedTime = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(shipment.getReceivedTime());
            holder.receivedDateView.setValue(formattedReceivedTime);
        } else {
            holder.receivedDateView.setValue("PENDING");
        }

        holder.destinationView.setValue(shipment.getDestinationEntityName());
        holder.sourceView.setValue(shipment.getSourceEntityName());

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

        holder.dropdownButton.setOnClickListener(view -> {
            if (!shipmentVisibilities.get(position)) {
                holder.deviceView.setVisibility(View.VISIBLE);
                shipmentVisibilities.set(position,true);
            } else {
                holder.deviceView.setVisibility(View.GONE);
                shipmentVisibilities.set(position,false);
            }
        });
        // check if position is last and override the on bottom reached callback function
        if ((position >= getItemCount() - 1)){
            onBottomReachedCallback.onBottomReached();
        }
    }

    @Override
    public int getItemCount(){
        return shipmentList.size();
    }
}
