package org.getcarebase.carebase.activities.Main.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.getcarebase.carebase.R;
import org.getcarebase.carebase.models.DeviceUsage;
import org.getcarebase.carebase.models.Procedure;

import java.util.List;
import java.util.Optional;


public class DeviceProceduresAdapter extends RecyclerView.Adapter<DeviceProceduresAdapter.ViewHolder> {

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public LinearLayout dropdown;
        public ImageView toggleButton;
        public LinearLayout procedureLayout;
        public TextView procedureDateView;
        public TextView accessionNumberView;
        public TextView procedureNameView;
        public TextView amountUsedView;
        public TextView roomTimeInView;
        public TextView roomTimeOutView;
        public TextView roomTimeView;
        public TextView fluoroTimeView;

        public ViewHolder(View view) {
            super(view);
            dropdown = view.findViewById(R.id.procedure_dropdown);
            toggleButton = view.findViewById(R.id.procedure_toggle_button);
            procedureLayout = view.findViewById(R.id.procedure_layout);
            procedureDateView = view.findViewById(R.id.procedure_date_text_view);
            accessionNumberView = view.findViewById(R.id.accession_number_text_view);
            procedureNameView = view.findViewById(R.id.procedure_name_text_view);
            amountUsedView = view.findViewById(R.id.amount_used_text_view);
            roomTimeInView = view.findViewById(R.id.room_time_in_text_view);
            roomTimeOutView = view.findViewById(R.id.room_time_out_text_view);
            roomTimeView = view.findViewById(R.id.room_time_text_view);
            fluoroTimeView = view.findViewById(R.id.fluoro_time_text_view);
        }
    }

    private final List<Procedure> procedures;
    private final String uniqueDeviceIdentifier;

    public DeviceProceduresAdapter(List<Procedure> procedures, final String uniqueDeviceIdentifier) {
        this.procedures = procedures;
        this.uniqueDeviceIdentifier = uniqueDeviceIdentifier;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View procedureView = inflater.inflate(R.layout.device_procedure_item, parent, false);
        ViewHolder viewHolder = new ViewHolder(procedureView);
        viewHolder.dropdown.setOnClickListener(view -> {
            if (viewHolder.procedureLayout.getVisibility() == View.VISIBLE) {
                viewHolder.procedureLayout.setVisibility(View.GONE);
                viewHolder.toggleButton.setImageResource(R.drawable.ic_baseline_plus);
            } else {
                viewHolder.procedureLayout.setVisibility(View.VISIBLE);
                viewHolder.toggleButton.setImageResource(R.drawable.icon_minimize);
            }
        });
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Procedure procedure = procedures.get(position);
        holder.procedureDateView.setText(procedure.getDate());
        holder.accessionNumberView.setText(procedure.getAccessionNumber());
        holder.procedureNameView.setText(procedure.getName());
        Optional<DeviceUsage> usage = procedure.getDeviceUsages().stream().filter(deviceUsage -> deviceUsage.getUniqueDeviceIdentifier().equals(uniqueDeviceIdentifier)).findFirst();
        if (usage.isPresent()) {
            int amountUsed = usage.get().getAmountUsed();
            holder.amountUsedView.setText(holder.amountUsedView.getContext().getResources().getQuantityString(R.plurals.number_of_units,amountUsed,amountUsed));
        }
        holder.roomTimeInView.setText(procedure.getTimeIn());
        holder.roomTimeOutView.setText(procedure.getTimeOut());
        holder.roomTimeView.setText(procedure.getRoomTime());
        holder.fluoroTimeView.setText(procedure.getFluoroTime());
    }

    @Override
    public int getItemCount() {
        return procedures.size();
    }
}
