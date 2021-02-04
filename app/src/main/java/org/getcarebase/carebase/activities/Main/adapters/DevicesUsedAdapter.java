package org.getcarebase.carebase.activities.Main.adapters;

import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.getcarebase.carebase.R;
import org.getcarebase.carebase.models.DeviceUsage;

import java.util.ArrayList;
import java.util.List;

public class DevicesUsedAdapter extends RecyclerView.Adapter<DevicesUsedAdapter.ViewHolder> {
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView nameView;
        TextView udiView;
        TextView countView;

        public ViewHolder(@NonNull View view) {
            super(view);
            nameView = view.findViewById(R.id.name_text_view);
            udiView = view.findViewById(R.id.udi_text_view);
            countView = view.findViewById(R.id.count_text_view);
        }
    }

    private final List<DeviceUsage> deviceUsages = new ArrayList<>();
    private Resources resources;

    public void setDeviceUsages(List<DeviceUsage> deviceUsages) {
        this.deviceUsages.clear();
        this.deviceUsages.addAll(deviceUsages);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        this.resources = context.getResources();
        LayoutInflater inflater = LayoutInflater.from(context);
        View procedureView = inflater.inflate(R.layout.device_used_item,parent,false);
        return new DevicesUsedAdapter.ViewHolder(procedureView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DeviceUsage deviceUsage = deviceUsages.get(position);
        holder.nameView.setText(deviceUsage.getName());
        holder.udiView.setText(deviceUsage.getUniqueDeviceIdentifier());
        holder.countView.setText(resources.getQuantityString(R.plurals.number_of_units,deviceUsage.getAmountUsed(),deviceUsage.getAmountUsed()));
    }

    @Override
    public int getItemCount() {
        return deviceUsages.size();
    }


}
