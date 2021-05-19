package org.getcarebase.carebase.activities.Main.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.getcarebase.carebase.R;
import org.getcarebase.carebase.activities.Main.fragments.ProceduresFragment;
import org.getcarebase.carebase.models.Procedure;
import org.getcarebase.carebase.views.LabeledTextView;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ProceduresAdapter extends RecyclerView.Adapter<ProceduresAdapter.ViewHolder> {
    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView nameView;
        public LabeledTextView dateView;
        public LabeledTextView timeView;
        public LabeledTextView roomTimeView;
        public LabeledTextView accessionNumberView;
        public LabeledTextView deviceUsageCountView;
        public ImageButton dropdownToggleButton;
        public Button viewButton;

        public ViewHolder(View view) {
            super(view);
            nameView = view.findViewById(R.id.name_text_view);
            dateView = view.findViewById(R.id.date_text_view);
            timeView = view.findViewById(R.id.time_text_view);
            roomTimeView = view.findViewById(R.id.room_time_text_view);
            accessionNumberView = view.findViewById(R.id.accession_number_text_view);
            deviceUsageCountView = view.findViewById(R.id.device_usage_count_text_view);
            dropdownToggleButton = view.findViewById(R.id.dropdown);
            viewButton = view.findViewById(R.id.view_button);
        }
    }

    private final List<Procedure> procedures = new ArrayList<>();
    private final List<Boolean> procedureVisibilities = new ArrayList<>();
    private final ProceduresFragment.ProcedureClickCallback procedureClickCallback;
    private ProceduresFragment.OnBottomReachedCallback onBottomReachedCallback;

    public ProceduresAdapter(ProceduresFragment.ProcedureClickCallback procedureClickCallback) {
        this.procedureClickCallback = procedureClickCallback;
    }

    public void setOnBottmReachedCallback(ProceduresFragment.OnBottomReachedCallback onBottomReachedCallback) {
        this.onBottomReachedCallback = onBottomReachedCallback;
    }

    public void setProcedures(List<Procedure> procedures) {
        this.procedures.clear();
        this.procedures.addAll(procedures);
        this.procedureVisibilities.clear();
        this.procedureVisibilities.addAll(this.procedures.stream().map(i -> false).collect(Collectors.toList()));
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View procedureView = inflater.inflate(R.layout.procedure_item,parent,false);
        return new ViewHolder(procedureView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Procedure procedure = procedures.get(position);
        holder.nameView.setText(procedure.getName());
        holder.dateView.setValue(procedure.getDate());
        holder.timeView.setValue(procedure.getTimeIn());
        holder.roomTimeView.setValue(procedure.getRoomTime());
        holder.accessionNumberView.setValue(procedure.getAccessionNumber());
        holder.deviceUsageCountView.setValue(Integer.toString(procedure.getDeviceUsages().size()));
        holder.viewButton.setOnClickListener(view -> procedureClickCallback.showProcedureDetail(procedure.getProcedureId()));

        holder.dropdownToggleButton.setOnClickListener(view -> {
            if (!procedureVisibilities.get(position)) {
                holder.dateView.setShowLabel(true);
                holder.timeView.setVisibility(View.VISIBLE);
                holder.roomTimeView.setVisibility(View.VISIBLE);
                holder.accessionNumberView.setVisibility(View.VISIBLE);
                holder.deviceUsageCountView.setVisibility(View.VISIBLE);
                holder.viewButton.setVisibility(View.VISIBLE);
                procedureVisibilities.set(position,true);
            } else {
                holder.dateView.setShowLabel(false);
                holder.timeView.setVisibility(View.GONE);
                holder.roomTimeView.setVisibility(View.GONE);
                holder.accessionNumberView.setVisibility(View.GONE);
                holder.deviceUsageCountView.setVisibility(View.GONE);
                holder.viewButton.setVisibility(View.GONE);
                procedureVisibilities.set(position,false);
            }
        });
        // check if position is last and override the on bottom reached callback function
        if ((position >= getItemCount() - 1)) {
            if (onBottomReachedCallback != null) {
                onBottomReachedCallback.onBottomReached();
                onBottomReachedCallback = null;
            }

        }
    }

    @Override
    public int getItemCount() {
        return procedures.size();
    }
}
