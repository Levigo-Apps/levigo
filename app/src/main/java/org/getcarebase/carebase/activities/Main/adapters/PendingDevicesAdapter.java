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
import org.getcarebase.carebase.activities.Main.fragments.PendingUdiFragment;
import org.getcarebase.carebase.models.PendingDevice;

import java.util.List;

public class PendingDevicesAdapter extends RecyclerView.Adapter<PendingDevicesAdapter.ViewHolder> {
    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView udiTextView;
        public ImageView saveIcon;

        public ViewHolder(View view) {
            super(view);
            udiTextView = view.findViewById(R.id.udi_text_view);
            saveIcon = view.findViewById(R.id.save_icon);
        }
    }

    private final List<PendingDevice> pendingDevices;
    private final PendingUdiFragment.ItemClickFunction clickFunction;

    public PendingDevicesAdapter(List<PendingDevice> pendingDevices, PendingUdiFragment.ItemClickFunction clickFunction) {
        this.pendingDevices = pendingDevices;
        this.clickFunction = clickFunction;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View pendingDeviceView = inflater.inflate(R.layout.pending_device_item,parent,false);
        return new ViewHolder(pendingDeviceView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PendingDevice pendingDevice = pendingDevices.get(position);
        holder.udiTextView.setText(pendingDevice.getUniqueDeviceIdentifier());
        holder.saveIcon.setOnClickListener(view -> clickFunction.openEditView(pendingDevice.getId()));
    }

    @Override
    public int getItemCount() {
        return pendingDevices.size();
    }

}
