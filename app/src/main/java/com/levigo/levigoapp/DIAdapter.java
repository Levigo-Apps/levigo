package com.levigo.levigoapp;

import android.app.Activity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import org.w3c.dom.Text;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class DIAdapter extends RecyclerView.Adapter<DIAdapter.DIHolder> {

    private static final String TAG = "diadapter";
    private Activity activity;
    private Map<String,Object> iDataset;

    public static class DIHolder extends RecyclerView.ViewHolder {
        public RecyclerView itemUDIs;
        public TextView itemName, itemQuantity, itemDI;


        public DIHolder(View view){
            super(view);
            itemUDIs = view.findViewById(R.id.dis_udis);
            itemDI = view.findViewById(R.id.dis_di);
            itemName = view.findViewById(R.id.dis_name);
            itemQuantity = view.findViewById(R.id.dis_quantity);
        }
    }

    public DIAdapter(Activity activity, Map<String,Object> iDataset) {
        this.activity = activity;
        this.iDataset = iDataset;
    }

    @NonNull
    @Override
    public DIAdapter.DIHolder onCreateViewHolder (ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.dis_item, parent, false);

        DIHolder vh = new DIHolder(view);
        return vh;
    }

    @Override
    public void onBindViewHolder(DIHolder holder, int position){


        Object[] dis = iDataset.values().toArray();
        Object object = dis[position];
        Map<String,Object> productid;
        if(object instanceof Map) {
            productid = (Map<String,Object>) object;
        }
        else {
            Log.d(TAG, "ERROR");
            return;
        }

        Map<String,Object> di = (HashMap<String,Object>) productid.get("di");
        //TODO make safe
        if(di.containsKey("name")) {
            String name = di.get("name").toString();
            holder.itemName.setText(name);
        }
        //TODO make plural
        if(di.containsKey("quantity")) {
            String quantity = di.get("quantity").toString() + " Units";
            holder.itemQuantity.setText(quantity);
        }
        if(di.containsKey("di")) {
            String diString = di.get("di").toString();
            holder.itemDI.setText(diString);
        }





        List<Map<String,Object>> udis = (LinkedList<Map<String, Object>>)productid.get("udis");

        UDIAdapter udiAdapter = new UDIAdapter(activity, udis);

        LinearLayoutManager layoutManager = new LinearLayoutManager(activity);
        holder.itemUDIs.setLayoutManager(layoutManager);
        holder.itemUDIs.setAdapter(udiAdapter);

    }

    @Override
    public int getItemCount(){
        return iDataset.size();
    }
}