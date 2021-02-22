package org.getcarebase.carebase.activities.Main.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.getcarebase.carebase.R;
import org.getcarebase.carebase.activities.Main.MainActivity;
import org.getcarebase.carebase.activities.Main.fragments.InventoryFragment;
import org.getcarebase.carebase.models.DeviceModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TypesAdapter extends RecyclerView.Adapter<TypesAdapter.TypesHolder> {
    private final InventoryFragment inventoryFragment;
    private Map<String, List<DeviceModel>> categoricalInventory;

    public static class TypesHolder extends RecyclerView.ViewHolder {
        public TextView itemType;

        public TypesHolder(View view){
            super(view);
            itemType = view.findViewById(R.id.types_name);
        }
    }

    public TypesAdapter(InventoryFragment inventoryFragment) {
        this.inventoryFragment = inventoryFragment;
    }

    public void setCategoricalInventory(Map<String, List<DeviceModel>> categoricalInventory) {
        this.categoricalInventory = categoricalInventory;
    }

    @NonNull
    @Override
    public TypesAdapter.TypesHolder onCreateViewHolder (ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.types_item, parent, false);
        return new TypesHolder(view);
    }

    @Override
    public void onBindViewHolder(TypesHolder holder, int position){
        List<Map.Entry<String, List<DeviceModel>>> categories = new ArrayList<>(categoricalInventory.entrySet());
        Map.Entry<String, List<DeviceModel>> category = categories.get(position);

        holder.itemType.setText(category.getKey());

        holder.itemView.setOnClickListener(view -> inventoryFragment.showModelList(category.getKey(), category.getValue()));
    }

    @Override
    public int getItemCount(){
        return categoricalInventory == null ? 0 : categoricalInventory.size();
    }
}
