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

import java.util.HashMap;
import java.util.Map;

public class TypesAdapter extends RecyclerView.Adapter<TypesAdapter.TypesHolder> {

    private static final String TAG = "typesadapter";
    private Activity activity;
    private Map<String,Object> iDataset;

    public static class TypesHolder extends RecyclerView.ViewHolder {
        public RecyclerView itemDIs;
        public TextView itemType;


        public TypesHolder(View view){
            super(view);
            itemDIs = view.findViewById(R.id.types_dis);
            itemType = view.findViewById(R.id.types_type);
            itemType.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(itemDIs.getVisibility() == View.GONE){
                        itemDIs.setVisibility(View.VISIBLE);
                    }
                    else {
                        itemDIs.setVisibility(View.GONE);
                    }
                }
            });
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
     //   Map<String,Object> udi = tDataset.get(position);
     //   if(udi.containsKey("equipment_type")) {
      //      String typeString = udi.get("equipment_type").toString();
      //      holder.itemType.setText(typeString);
       // }

         Object[] types = iDataset.values().toArray();

         Object object = types[position];

         Map<String,Object> dis;
            if(object instanceof Map) {
                dis = (Map<String, Object>) object;
            }
         else {
            Log.d(TAG, "ERROR");
            return;
         }


        Object[] types1 = dis.values().toArray();
        Object object1 = types1[0];
        Map<String,Object> productType;
        if(object1 instanceof Map) {
            productType = (Map<String,Object>) object1;
        }
        else {
            Log.d(TAG, "ERROR");
            return;
        }

        Map<String,Object> type = (HashMap<String,Object>) productType.get("di");
        //TODO make safe
        if(type.containsKey("equipment_type")) {
            String type_item = type.get("equipment_type").toString();
            holder.itemType.setText(type_item);
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
