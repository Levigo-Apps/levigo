package org.getcarebase.carebase.activities.Main.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.getcarebase.carebase.R;
import org.getcarebase.carebase.activities.Main.MainActivity;
import org.getcarebase.carebase.activities.Main.fragments.InventoryFragment;
import org.getcarebase.carebase.activities.Main.fragments.ModelListFragment;
import org.getcarebase.carebase.models.DeviceProduction;

import java.util.List;

public class DeviceProductionsAdapter extends RecyclerView.Adapter<DeviceProductionsAdapter.DeviceProductionHolder> {
    private final ModelListFragment modelListFragment;
    private final List<DeviceProduction> deviceProductions;
    private final String deviceIdentifier;

    public static class DeviceProductionHolder extends RecyclerView.ViewHolder {
        public TextView itemExpiration;
        public TextView itemQuantity;

        public DeviceProductionHolder(View view){
            super(view);
            itemExpiration = view.findViewById(R.id.udis_expirationdate);
            itemQuantity = view.findViewById(R.id.udis_quantity);
        }
    }

    public DeviceProductionsAdapter(ModelListFragment modelListFragment, String deviceIdentifier, List<DeviceProduction> deviceProductions) {
        this.modelListFragment = modelListFragment;
        this.deviceProductions = deviceProductions;
        this.deviceIdentifier = deviceIdentifier;
    }

    @NonNull
    @Override
    public DeviceProductionsAdapter.DeviceProductionHolder onCreateViewHolder (ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.udis_item, parent, false);
        return new DeviceProductionHolder(view);
    }

    @Override
    public void onBindViewHolder(DeviceProductionHolder holder, int position) {
        DeviceProduction deviceProduction = deviceProductions.get(position);

        String expiration = "EXP " + deviceProduction.getExpirationDate();
        holder.itemExpiration.setText(expiration);

        //TODO PLURAL
        holder.itemQuantity.setText(modelListFragment.getString(R.string.unit_quantity_value,deviceProduction.getQuantity()));

        holder.itemView.setOnClickListener(view -> modelListFragment.showDeviceDetail(deviceIdentifier,deviceProduction.getUniqueDeviceIdentifier()));
    }

    @Override
    public int getItemCount() {
        return deviceProductions.size();
    }
}
