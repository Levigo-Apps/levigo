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
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class TypesAdapter extends RecyclerView.Adapter<TypesAdapter.TypesHolder> {
    private final InventoryFragment inventoryFragment;
    private List<String> typeList;

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

    public void setTypeList(List<String> typeList) {
        this.typeList = typeList;
        Collections.sort(this.typeList);
    }

    @NonNull
    @Override
    public TypesAdapter.TypesHolder onCreateViewHolder (ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.types_item, parent, false);
        return new TypesHolder(view);
    }

    @Override
    public void onBindViewHolder(TypesHolder holder, int position){
        String type = typeList.get(position);

        holder.itemType.setText(type);

        holder.itemView.setOnClickListener(view -> inventoryFragment.showModelList(type));
    }

    @Override
    public int getItemCount(){
        return typeList == null ? 0 : typeList.size();
    }
}
