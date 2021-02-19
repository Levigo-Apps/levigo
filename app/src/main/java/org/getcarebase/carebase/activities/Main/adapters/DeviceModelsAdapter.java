package org.getcarebase.carebase.activities.Main.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.getcarebase.carebase.R;
import org.getcarebase.carebase.activities.Main.MainActivity;
import org.getcarebase.carebase.activities.Main.fragments.InventoryFragment;
import org.getcarebase.carebase.models.DeviceModel;

import java.util.List;

public class DeviceModelsAdapter extends RecyclerView.Adapter<DeviceModelsAdapter.DeviceModelHolder> {
    private final InventoryFragment inventoryFragment;
    private final List<DeviceModel> deviceModels;

    public static class DeviceModelHolder extends RecyclerView.ViewHolder {
        public RecyclerView itemUDIs;
        public TextView itemName, itemQuantity, itemDI;
        public ConstraintLayout itemType;


        public DeviceModelHolder(View view){
            super(view);
            itemUDIs = view.findViewById(R.id.dis_udis);
            itemDI = view.findViewById(R.id.dis_di);
            itemName = view.findViewById(R.id.dis_name);
            itemQuantity = view.findViewById(R.id.dis_quantity);

            itemType = view.findViewById(R.id.dis_type);
            itemType.setOnClickListener(view1 -> {
                if(itemUDIs.getVisibility() == View.GONE){
                    itemUDIs.setVisibility(View.VISIBLE);
                }
                else {
                    itemUDIs.setVisibility(View.GONE);
                }
            });
        }
    }

    public DeviceModelsAdapter(InventoryFragment inventoryFragment, List<DeviceModel> deviceModels) {
        this.inventoryFragment = inventoryFragment;
        this.deviceModels = deviceModels;
    }

    @NonNull
    @Override
    public DeviceModelsAdapter.DeviceModelHolder onCreateViewHolder (ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.dis_item, parent, false);
        return new DeviceModelHolder(view);
    }

    @Override
    public void onBindViewHolder(DeviceModelHolder holder, int position) {
        DeviceModel deviceModel = deviceModels.get(position);
        holder.itemName.setText(deviceModel.getName());
        holder.itemQuantity.setText(inventoryFragment.getString(R.string.unit_quantity_value,deviceModel.getQuantity()));
        holder.itemDI.setText(deviceModel.getDeviceIdentifier());

        DeviceProductionsAdapter deviceProductionsAdapter = new DeviceProductionsAdapter(inventoryFragment, deviceModel.getDeviceIdentifier(), deviceModel.getProductions());

        LinearLayoutManager layoutManager = new LinearLayoutManager(inventoryFragment.getContext());
        holder.itemUDIs.setLayoutManager(layoutManager);
        holder.itemUDIs.setAdapter(deviceProductionsAdapter);
    }

    @Override
    public int getItemCount(){
        return deviceModels.size();
    }
}
