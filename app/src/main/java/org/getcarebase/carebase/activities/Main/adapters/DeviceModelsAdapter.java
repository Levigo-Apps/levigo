package org.getcarebase.carebase.activities.Main.adapters;

import android.bluetooth.BluetoothClass;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.lifecycle.LiveData;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.getcarebase.carebase.R;
import org.getcarebase.carebase.activities.Main.MainActivity;
import org.getcarebase.carebase.activities.Main.fragments.InventoryFragment;
import org.getcarebase.carebase.activities.Main.fragments.ModelListFragment;
import org.getcarebase.carebase.models.DeviceModel;
import org.getcarebase.carebase.utils.Resource;

import java.util.ArrayList;
import java.util.List;

public class DeviceModelsAdapter extends RecyclerView.Adapter<DeviceModelsAdapter.DeviceModelHolder> {
    private final ModelListFragment modelListFragment;
    private List<DeviceModel> deviceModels;
    private List<DeviceModel> deviceModelsCopy = new ArrayList<>();
    private List<DeviceModel> filtered;

    public static class DeviceModelHolder extends RecyclerView.ViewHolder {
        public RecyclerView itemUDIs;
        public TextView itemName, itemQuantity, itemDI, itemSubcategory;
        public ConstraintLayout itemType;


        public DeviceModelHolder(View view){
            super(view);
            itemUDIs = view.findViewById(R.id.dis_udis);
            itemDI = view.findViewById(R.id.dis_di);
            itemName = view.findViewById(R.id.dis_name);
            itemQuantity = view.findViewById(R.id.dis_quantity);
            itemSubcategory = view.findViewById(R.id.dis_subcategory);

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

    public DeviceModelsAdapter(ModelListFragment modelListFragment) {
        this.modelListFragment = modelListFragment;
    }

    public void setDeviceModels(List<DeviceModel> deviceModels) {
        this.deviceModels = deviceModels;
        deviceModelsCopy.addAll(this.deviceModels);
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
        holder.itemQuantity.setText(modelListFragment.getString(R.string.unit_quantity_value,deviceModel.getQuantity()));
        holder.itemDI.setText(deviceModel.getDeviceIdentifier());
        // Display subcategory only if nonnull
        if (deviceModel.getSubType() != null) {
            holder.itemSubcategory.setVisibility(View.VISIBLE);
            holder.itemSubcategory.setText(deviceModel.getSubType());
        } else {
            holder.itemSubcategory.setVisibility(View.GONE);
        }


        DeviceProductionsAdapter deviceProductionsAdapter = new DeviceProductionsAdapter(modelListFragment, deviceModel.getDeviceIdentifier(), deviceModel.getProductions());

        LinearLayoutManager layoutManager = new LinearLayoutManager(modelListFragment.getContext());
        holder.itemUDIs.setLayoutManager(layoutManager);
        holder.itemUDIs.setAdapter(deviceProductionsAdapter);
    }

    @Override
    public int getItemCount(){
        return deviceModels == null ? 0 : deviceModels.size();
    }

    public void filterSubtype(String subtype) {
        deviceModels.clear();
        if (subtype.equals("All")) {
            deviceModels.addAll(deviceModelsCopy);
        } else {
            for (DeviceModel model: deviceModelsCopy) {
                if (model.getSubType() != null && model.getSubType().equals(subtype)) {
                    deviceModels.add(model);
                }
            }
        }
        notifyDataSetChanged();
    }
}
