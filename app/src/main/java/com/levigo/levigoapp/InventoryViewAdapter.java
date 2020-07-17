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

public class InventoryViewAdapter extends RecyclerView.Adapter<InventoryViewAdapter.InventoryViewHolder> {

    private static final String TAG = "ivadapter";
    private Activity activity;
    private Map<String,Object> iDataset;

    public static class InventoryViewHolder extends RecyclerView.ViewHolder {
//        public TextView itemTitle;
//        public TextView itemDI;
//    //    public TextView itemExpirationDate;
//        public TextView itemType;
//        public TextView itemName;
//        public TextView itemQuantity;
        public RecyclerView itemTypes;


        public InventoryViewHolder(View view){
            super(view);
//            itemDI = view.findViewById(R.id.item_di);
//            itemType = view.findViewById(R.id.item_type);
//            itemName = view.findViewById(R.id.item_name);
//            itemQuantity = view.findViewById(R.id.item_quantity);
            itemTypes = view.findViewById(R.id.categories_types);
        }
    }

    public InventoryViewAdapter(Activity activity, Map<String,Object> iDataset) {
        this.activity = activity;
        this.iDataset = iDataset;
    }

    @NonNull
    @Override
    public InventoryViewAdapter.InventoryViewHolder onCreateViewHolder (ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.categories_item, parent, false);
        Log.d(TAG, "onCreate");
        InventoryViewHolder vh = new InventoryViewHolder(view);
        return vh;
    }

    @Override
    public void onBindViewHolder(InventoryViewHolder holder, int position){
//        holder.itemTitle.setText(iDataset.get(position));
        Log.d(TAG, "bind");
        Object[] categories = iDataset.values().toArray();
        Object object = categories[position];
        Map<String, Object> types;
        if(object instanceof Map) {
            types = (Map<String,Object>) object;
        }
        else {
            Log.d(TAG, "ERROR");
            return;
        }

        TypesAdapter typesAdapter = new TypesAdapter(activity, types);
        LinearLayoutManager layoutManager = new LinearLayoutManager(activity);
        holder.itemTypes.setLayoutManager(layoutManager);
        holder.itemTypes.setAdapter(typesAdapter);
        Log.d(TAG, "here");


//        Log.d(TAG, "DATASET: " + iDataset);
//        // TODO better way of checking if key exists
//        if (iDataset.get(position).containsKey("di")) {
//            Log.d(TAG, "ITEM DI: " + iDataset.get(position).get("di").toString());
//            String di = "DI: " + iDataset.get(position).get("di").toString();
//            holder.itemDI.setText(di);
//        }
//        if (iDataset.get(position).containsKey("equipment_type")) {
//            Log.d(TAG, "ITEM TYPE: " + iDataset.get(position).get("equipment_type").toString());
//            holder.itemType.setText(iDataset.get(position).get("equipment_type").toString());
//        }
//        if (iDataset.get(position).containsKey("quantity")) {
//            Log.d(TAG, "ITEM QUANTITY: " + iDataset.get(position).get("quantity").toString());
//            //TODO plural of unit
//            String qty = iDataset.get(position).get("quantity").toString() + " Units";
//            holder.itemQuantity.setText(qty);
//        }
//        if (iDataset.get(position).containsKey("name")) {
//            Log.d(TAG, "ITEM NAME: " + iDataset.get(position).get("name").toString());
//            holder.itemName.setText(iDataset.get(position).get("name").toString());
//        }



  //      if (iDataset.get(position).containsKey("expiration")){
  //          Log.d(TAG, "ITEM EXPIRATION: " + iDataset.get(position).get("expiration").toString());
   //         holder.itemExpirationDate.setText(iDataset.get(position).get("expiration").toString());
   //     }


//        inventoryRef.whereEqualTo("udi", "0100886333006052172204101011174028")
//                .get()
//                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
//                    @Override
//                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
//                        if (task.isSuccessful()) {
//                            for (QueryDocumentSnapshot document : task.getResult()) {
//                                Log.d(TAG, document.getId() + " => " + document.getData());
//                            }
//                        } else {
//                            Log.d(TAG, "Error getting documents: ", task.getException());
//                        }
//                    }
//                });
    }

    @Override
    public int getItemCount(){
        return iDataset.size();
    }
}
