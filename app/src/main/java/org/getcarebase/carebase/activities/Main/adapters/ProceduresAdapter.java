package org.getcarebase.carebase.activities.Main.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.getcarebase.carebase.R;
import org.getcarebase.carebase.models.Procedure;

import java.util.ArrayList;
import java.util.List;

public class ProceduresAdapter extends RecyclerView.Adapter<ProceduresAdapter.ViewHolder> {
    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView nameView;
        public TextView dateView;

        public ViewHolder(View view) {
            super(view);
            nameView = view.findViewById(R.id.name_text_view);
            dateView = view.findViewById(R.id.date_text_view);
        }
    }

    private final List<Procedure> procedures = new ArrayList<>();

    public void setProcedures(List<Procedure> procedures) {
        this.procedures.clear();
        this.procedures.addAll(procedures);
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
        holder.dateView.setText(procedure.getDate());
    }

    @Override
    public int getItemCount() {
        return procedures.size();
    }
}
