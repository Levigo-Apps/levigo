package com.levigo.levigoapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class InventoryViewAdapter extends RecyclerView.Adapter<InventoryViewAdapter.InventoryViewHolder> {
    private List<String> iDataset;

    public static class InventoryViewHolder extends RecyclerView.ViewHolder {
        public TextView itemTitle;

        public InventoryViewHolder(View view){
            super(view);
            itemTitle = view.findViewById(R.id.itemTitle);
        }
    }

    public InventoryViewAdapter(List<String> invNameDataSet) {
        iDataset = invNameDataSet;
    }

    @NonNull
    @Override
    public InventoryViewAdapter.InventoryViewHolder onCreateViewHolder (ViewGroup parent, int viewType) {
//        TextView t = (TextView) LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_inventoryview,parent,false);
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.inventory_item, null, false);

        InventoryViewHolder vh = new InventoryViewHolder(view);
        return vh;
    }

    @Override
    public void onBindViewHolder(InventoryViewHolder holder, int position){
        holder.itemTitle.setText(iDataset.get(position));
    }

    @Override
    public int getItemCount(){
        return iDataset.size();
    }
}
