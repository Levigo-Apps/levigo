package org.getcarebase.carebase.activities.Main.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.getcarebase.carebase.R;
import org.getcarebase.carebase.activities.Main.fragments.ShipDeviceFragment;
import org.getcarebase.carebase.models.Shipment;
import org.getcarebase.carebase.views.LabeledTextView;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class ShipmentsAdapter extends RecyclerView.Adapter<ShipmentsAdapter.ViewHolder> {
    public static final String TAG = ShipmentsAdapter.class.getName();

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public LabeledTextView trackerView;
        public LabeledTextView dateView;
        public LabeledTextView destinationView;
        public LabeledTextView sourceView;
        public LabeledTextView diView;
        public LabeledTextView udiView;
        public LabeledTextView quantityView;

        public ViewHolder(View view) {
            super(view);
            // Replace ids with correct shipment ids once shipment item layout is created: done
            trackerView = view.findViewById(R.id.tracker_text_view);
            dateView = view.findViewById(R.id.date_text_view);
            destinationView = view.findViewById(R.id.destination_text_view);
            sourceView = view.findViewById(R.id.source_text_view);

            // Device values, eventually should go into separate fragment
            diView = view.findViewById(R.id.di_text_view);
            udiView = view.findViewById(R.id.udi_text_view);
            quantityView = view.findViewById(R.id.quantity_text_view);
        }
    }

    private final List<Shipment> shipmentList = new ArrayList<>();

    public void setShipments(List<Shipment> shipmentList) {
        this.shipmentList.clear();
        this.shipmentList.addAll(shipmentList);
    }

    @NonNull
    @Override
    public ShipmentsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        // Replace with shipment item (create in layout): done
        View procedureView = inflater.inflate(R.layout.shipment_item,parent,false);
        return new ShipmentsAdapter.ViewHolder(procedureView);
    }

    @Override
    public void onBindViewHolder(ShipmentsAdapter.ViewHolder holder, int position){
        Shipment shipment = shipmentList.get(position);

        holder.trackerView.setValue(shipment.getTrackingNumber());
        holder.dateView.setValue(shipment.getShippedTime());
        holder.destinationView.setValue(shipment.getDestinationEntityId());
        holder.sourceView.setValue(shipment.getSourceEntityId());

        holder.diView.setValue(shipment.getItems().get(0).get("di"));
        holder.udiView.setValue(shipment.getItems().get(0).get("udi"));
        holder.quantityView.setValue(shipment.getItems().get(0).get("quantity"));
    }

    @Override
    public int getItemCount(){
        return shipmentList.size();
    }
}
