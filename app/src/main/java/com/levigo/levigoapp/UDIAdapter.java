package com.levigo.levigoapp;

import android.app.Activity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Map;

public class UDIAdapter extends RecyclerView.Adapter<UDIAdapter.UDIHolder> {

    private static final String TAG = "udiadapter";
    private MainActivity activity;
    private Map<String,Object> iDataset;

    public static class UDIHolder extends RecyclerView.ViewHolder {
        public TextView itemExpiration;
        public TextView itemQuantity;


        public UDIHolder(View view){
            super(view);
            itemExpiration = view.findViewById(R.id.udis_expirationdate);
            itemQuantity = view.findViewById(R.id.udis_quantity);
        }
    }

    public UDIAdapter(MainActivity activity, Map<String,Object> iDataset) {
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
        Object[] types = iDataset.values().toArray();

        Object object = types[position];


        if(object instanceof Map) {
            final Map<String,Object> udi = (Map<String, Object>) object;
            if(udi.containsKey("expiration")) {
                String expiration = "EXP " + udi.get("expiration").toString();
                holder.itemExpiration.setText(expiration);
            }
            if(udi.containsKey("quantity")) {
                //TODO PLURAL
                String quantity = udi.get("quantity").toString() + " Units";
                holder.itemQuantity.setText(quantity);
            }
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(udi.containsKey("udi")) {
                        String udiString = udi.get("udi").toString();
                        //activity.startItemView(udiString);
                        activity.startItemViewOnly(udiString);
                    }
                }
            });
        }
        else {
            Log.d(TAG, "ERROR");
        }


    }

    @Override
    public int getItemCount(){
        return iDataset.size();
    }
}
