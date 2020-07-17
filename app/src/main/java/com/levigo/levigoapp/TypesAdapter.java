package com.levigo.levigoapp;

import android.app.Activity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import java.util.Map;

public class TypesAdapter extends RecyclerView.Adapter<TypesAdapter.TypesHolder> {

    private static final String TAG = "typesadapter";
    private Activity activity;
    private Map<String,Object> iDataset;

    public static class TypesHolder extends RecyclerView.ViewHolder {
        public RecyclerView itemDIs;


        public TypesHolder(View view){
            super(view);
            itemDIs = view.findViewById(R.id.types_dis);
        }
    }

    public TypesAdapter(Activity activity, Map<String,Object> iDataset) {
        this.activity = activity;
        this.iDataset = iDataset;
    }

    @NonNull
    @Override
    public TypesAdapter.TypesHolder onCreateViewHolder (ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.types_item, parent, false);

        TypesHolder vh = new TypesHolder(view);
        return vh;
    }

    @Override
    public void onBindViewHolder(TypesHolder holder, int position){

        Object[] types = iDataset.values().toArray();
        Object object = types[position];
        Map<String,Object> dis;
        if(object instanceof Map) {
            dis = (Map<String,Object>) object;
        }
        else {
            Log.d(TAG, "ERROR");
            return;
        }

        DIAdapter diAdapter = new DIAdapter(activity, dis);

        LinearLayoutManager layoutManager = new LinearLayoutManager(activity);
        holder.itemDIs.setLayoutManager(layoutManager);
        holder.itemDIs.setAdapter(diAdapter);

    }

    @Override
    public int getItemCount(){
        return iDataset.size();
    }
}
