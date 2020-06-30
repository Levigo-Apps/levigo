package com.levigo.levigoapp;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class InventoryViewAdapter extends RecyclerView.Adapter<InventoryViewAdapter.InventoryViewHolder> {
    private List<String> iDataset;

    public static class InventoryViewHolder extends RecyclerView.ViewHolder {
        public TextView itemTextView;

        public InventoryViewHolder(TextView t){
            super(t);
            itemTextView = t;
        }
    }

    public InventoryViewAdapter(List<String> invNameDataSet) {
        iDataset = invNameDataSet;
    }

    @NonNull
    @Override
    public InventoryViewAdapter.InventoryViewHolder onCreateViewHolder (ViewGroup parent, int viewType) {
        TextView t = (TextView) LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_inventoryview,parent,false);

        InventoryViewHolder vh = new InventoryViewHolder(t);
        return vh;
    }

    @Override
    public void onBindViewHolder(InventoryViewHolder holder, int position){
        holder.itemTextView.setText(iDataset.get(position));
    }

    @Override
    public int getItemCount(){
        return iDataset.size();
    }
}
