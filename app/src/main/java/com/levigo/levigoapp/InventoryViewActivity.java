package com.levigo.levigoapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.LinkedList;
import java.util.List;


public class InventoryViewActivity extends Activity {

    private static final String TAG = "InventoryViewActivity";
    private FirebaseFirestore levigoDb = FirebaseFirestore.getInstance() ;
    private CollectionReference inventoryRef = levigoDb.collection("Inventory") ;


    private RecyclerView inventoryScroll ;
    @SuppressWarnings("rawtypes")
    private RecyclerView.Adapter iAdapter ;
    @SuppressWarnings("FieldCanBeLocal")
    private RecyclerView.LayoutManager iLayoutManager ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventoryview);

        final List<String> names = new LinkedList<>();
        inventoryScroll = findViewById(R.id.inventoryScroll);
        inventoryScroll.setHasFixedSize(true);
        iLayoutManager = new LinearLayoutManager(this);
        inventoryScroll.setLayoutManager(iLayoutManager);
        iAdapter = new InventoryViewAdapter(names);
        inventoryScroll.setAdapter(iAdapter);


        inventoryRef.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) throws NullPointerException{
                if (e != null) {
                    System.err.println("Listen failed: " + e);
                    return;
                }

                inventoryRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()) {
                            if(task.getResult() != null) {
                                for (QueryDocumentSnapshot inventoryItem : task.getResult()) {
                                    names.add(inventoryItem.getString("name"));
                                    Log.d(TAG, "Success");
                                    iAdapter.notifyDataSetChanged();
                                }
                            } else {
                                throw new NullPointerException("Error getting list of inventory items");
                            }
                        } else {
                            Log.d(TAG, "Error getting list of inventory items: ", task.getException());
                        }
                    }
                });
            }
        });

   }



    public void goToManualAdd(View view) {
//       Intent manualAddPage = new Intent(this, FirebaseActivity.class);
//       startActivity(manualAddPage);
        // Let me know when you need to implement this --Elliot
    }

    public void goToScan(View view) {
//        Intent scanPage = new Intent(this, MainActivity.class);
//        startActivity(scanPage);
    }
}
