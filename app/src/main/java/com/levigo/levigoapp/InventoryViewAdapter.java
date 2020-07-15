package com.levigo.levigoapp;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.google.firebase.firestore.CollectionReference;

import java.util.List;
import java.util.Map;

public class InventoryViewAdapter extends RecyclerView.Adapter<InventoryViewAdapter.InventoryViewHolder> {
    private List<Map<String,Object>> iDataset;
    private CollectionReference inventoryRef;
    private static final String TAG = "ivadapter";

    public static class InventoryViewHolder extends RecyclerView.ViewHolder {
        public TextView itemTitle;
        public TextView itemDI;
    //    public TextView itemExpirationDate;
        public TextView itemType;
        public TextView itemName;
        public TextView itemQuantity;


        public InventoryViewHolder(View view){
            super(view);
            itemDI = view.findViewById(R.id.item_di);
            itemType = view.findViewById(R.id.item_type);
            itemName = view.findViewById(R.id.item_name);
            itemQuantity = view.findViewById(R.id.item_quantity);
        }
    }

    public InventoryViewAdapter(List<Map<String,Object>> invNameDataSet, CollectionReference inventoryRef) {
        iDataset = invNameDataSet;
        this.inventoryRef = inventoryRef;
    }

    @NonNull
    @Override
    public InventoryViewAdapter.InventoryViewHolder onCreateViewHolder (ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.inventory_item, parent, false);

        InventoryViewHolder vh = new InventoryViewHolder(view);
        return vh;
    }

    @Override
    public void onBindViewHolder(InventoryViewHolder holder, int position){
//        holder.itemTitle.setText(iDataset.get(position));

        Log.d(TAG, "DATASET: " + iDataset);
        // TODO better way of checking if key exists
        if (iDataset.get(position).containsKey("di")) {
            Log.d(TAG, "ITEM DI: " + iDataset.get(position).get("di").toString());
            String di = "DI: " + iDataset.get(position).get("di").toString();
            holder.itemDI.setText(di);
        }
        if (iDataset.get(position).containsKey("equipment_type")) {
            Log.d(TAG, "ITEM TYPE: " + iDataset.get(position).get("equipment_type").toString());
            holder.itemType.setText(iDataset.get(position).get("equipment_type").toString());
        }
        if (iDataset.get(position).containsKey("quantity")) {
            Log.d(TAG, "ITEM QUANTITY: " + iDataset.get(position).get("quantity").toString());
            //TODO plural of unit
            String qty = iDataset.get(position).get("quantity").toString() + " Units";
            holder.itemQuantity.setText(qty);
        }
        if (iDataset.get(position).containsKey("name")) {
            Log.d(TAG, "ITEM NAME: " + iDataset.get(position).get("name").toString());
            holder.itemName.setText(iDataset.get(position).get("name").toString());
        }

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
