package org.getcarebase.carebase.activities.Main.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.getcarebase.carebase.R;
import org.getcarebase.carebase.models.DeviceUsage;

import java.util.List;

public class NewUsedDevicesAdapter extends RecyclerView.Adapter<NewUsedDevicesAdapter.ViewHolder>{

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView udiTextView;
        public TextView quantityTextView;
        public ImageView incrementQuantityButton;
        public ImageView removeDeviceButton;

        public ViewHolder(View view) {
            super(view);
            udiTextView = view.findViewById(R.id.used_device_udi);
            quantityTextView = view.findViewById(R.id.used_device_quantity);
            incrementQuantityButton = view.findViewById(R.id.increment_used_device_quantity_button);
            removeDeviceButton = view.findViewById(R.id.remove_used_device_button);
        }
    }

    private List<DeviceUsage> devices;

    public NewUsedDevicesAdapter(List<DeviceUsage> devices) {
        this.devices = devices;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View deviceView = inflater.inflate(R.layout.procedure_item, parent, false);
        return new ViewHolder(deviceView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DeviceUsage deviceUsage = devices.get(position);
        holder.udiTextView.setText(deviceUsage.getUniqueDeviceIdentifier());
        holder.quantityTextView.setText(Integer.toString(deviceUsage.getAmountUsed()));
        holder.incrementQuantityButton.setOnClickListener(view -> {
            deviceUsage.incrementAmountUsed();
            notifyItemChanged(position);
        });
        holder.removeDeviceButton.setOnClickListener(view -> {
            devices.remove(position);
            notifyItemRemoved(position);
        });
    }

    @Override
    public int getItemCount() {
        return devices.size();
    }

}
