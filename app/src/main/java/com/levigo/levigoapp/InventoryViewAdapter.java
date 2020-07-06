package com.levigo.levigoapp;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.List;
import java.util.Map;

public class InventoryViewAdapter extends RecyclerView.Adapter<InventoryViewAdapter.InventoryViewHolder> {
//    private List<String> iDataset;
    private List<Map<String, Object>> iDataset;
    CollectionReference inventoryRef;
    private final String TAG = this.getClass().getSimpleName();

    public static class InventoryViewHolder extends RecyclerView.ViewHolder {
        // TODO add elements
        public TextView itemTitle;
        public TextView itemExpirationDate;

        public InventoryViewHolder(View view){
            super(view);
            itemTitle = view.findViewById(R.id.item_udi);
            itemExpirationDate = view.findViewById(R.id.item_expiration_date);
        }
    }

    public InventoryViewAdapter(List<Map<String, Object>> invNameDataSet, CollectionReference inventoryRef) {
        iDataset = invNameDataSet;
        inventoryRef = inventoryRef;
    }

    @NonNull
    @Override
    public InventoryViewAdapter.InventoryViewHolder onCreateViewHolder (ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.inventory_item, null, false);

        InventoryViewHolder vh = new InventoryViewHolder(view);
        return vh;
    }

    @Override
    public void onBindViewHolder(InventoryViewHolder holder, int position){
        Log.d(TAG, "DATASET: " + iDataset);
        // TODO better way of checking if key exists
        if (iDataset.get(position).containsKey("udi")) {
            Log.d(TAG, "ITEM UDI: " + iDataset.get(position).get("udi").toString());
            holder.itemTitle.setText(iDataset.get(position).get("udi").toString());
        }
        if (iDataset.get(position).containsKey("expiration")){
            Log.d(TAG, "ITEM EXPIRATION: " + iDataset.get(position).get("expiration").toString());
            holder.itemExpirationDate.setText(iDataset.get(position).get("expiration").toString());
        }


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
