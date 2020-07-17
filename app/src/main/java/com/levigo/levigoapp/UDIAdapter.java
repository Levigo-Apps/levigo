package com.levigo.levigoapp;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.google.android.gms.common.util.MapUtils;

import org.w3c.dom.Text;

import java.util.List;
import java.util.Map;

public class UDIAdapter extends RecyclerView.Adapter<UDIAdapter.UDIHolder> {

    private static final String TAG = "udiadapter";
    private Activity activity;
    private List<Map<String,Object>> iDataset;

    public static class UDIHolder extends RecyclerView.ViewHolder {
        public TextView itemUDI;
        public TextView itemQuantity;


        public UDIHolder(View view){
            super(view);
            itemUDI = view.findViewById(R.id.udis_udi);
            itemQuantity = view.findViewById(R.id.udis_quantity);
        }
    }

    public UDIAdapter(Activity activity, List<Map<String,Object>> iDataset) {
        this.activity = activity;
        this.iDataset = iDataset;
    }

    @NonNull
    @Override
    public UDIAdapter.UDIHolder onCreateViewHolder (ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.udis_item, parent, false);

        UDIHolder vh = new UDIHolder(view);
        return vh;
    }

    @Override
    public void onBindViewHolder(UDIHolder holder, int position){
        Map<String,Object> udi = iDataset.get(position);
        if(udi.containsKey("udi")) {
            String udiString = udi.get("udi").toString();
            holder.itemUDI.setText(udiString);
        }
        if(udi.containsKey("quantity")) {
            //TODO PLURAL
            String quantity = udi.get("quantity").toString() + " Units";
            holder.itemQuantity.setText(quantity);
        }
    }

    @Override
    public int getItemCount(){
        return iDataset.size();
    }
}
