package org.getcarebase.carebase.activities.Main.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.getcarebase.carebase.R;
import org.getcarebase.carebase.activities.Main.fragments.AddShipmentFragment;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AddShipmentAdapter extends RecyclerView.Adapter<AddShipmentAdapter.ViewHolder> {
    public static final String TAG = AddShipmentFragment.class.getSimpleName();

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView nameView;
        TextView udiView;
        TextView countView;
        AutoCompleteTextView physicalLocationTextView;

        public ViewHolder(@NonNull View view) {
            super(view);
            nameView = view.findViewById(R.id.name_text_view);
            udiView = view.findViewById(R.id.udi_text_view);
            countView = view.findViewById(R.id.count_text_view);
            physicalLocationTextView = view.findViewById(R.id.detail_physical_location);
        }
    }

    private List<Map<String,String>> items;
    ArrayAdapter<String> physicalLocationAdapter;

    public void setItems(List<Map<String,String>> items) {
        this.items = items;
    }

    public void setPhysicalLocationAdapter(ArrayAdapter<String> physicalLocationAdapter) {
        this.physicalLocationAdapter = physicalLocationAdapter;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View addShipmentItemView = inflater.inflate(R.layout.add_shipment_list_item,parent,false);
        return new ViewHolder(addShipmentItemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Map<String,String> item = items.get(position);
        holder.nameView.setText(item.get("name"));
        holder.udiView.setText(item.get("udi"));
        holder.countView.setText(item.get("quantity"));
        holder.physicalLocationTextView.setAdapter(physicalLocationAdapter);
    }

    @Override
    public int getItemCount() {
        return (items == null || physicalLocationAdapter == null) ? 0 : items.size();
    }
}
