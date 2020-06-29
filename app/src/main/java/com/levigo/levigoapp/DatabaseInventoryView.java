package com.levigo.levigoapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class DatabaseInventoryView extends Activity {
    private FirebaseDatabase levigoDatabase = FirebaseDatabase.getInstance() ;
    private DatabaseReference levigoRef = levigoDatabase.getReference("Inventory/") ;


   @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_inventoryview);

   }

   public void dataWatch() {
       levigoRef.addValueEventListener(new ValueEventListener() {
           @Override
           public void onDataChange(@NonNull DataSnapshot dataSnapshot) {


           }

           @Override
           public void onCancelled(@NonNull DatabaseError databaseError) {
               Log.w("TAG", "Failed to add item.", databaseError.toException());
           }
       });
       levigoRef.addChildEventListener(new ChildEventListener() {
           @Override
           public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

           }

           @Override
           public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

           }

           @Override
           public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

           }

           @Override
           public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

           }

           @Override
           public void onCancelled(@NonNull DatabaseError databaseError) {
               Log.w("TAG", "Failed to add item.", databaseError.toException());
           }
       });
   }

    public void goToManualAdd(View view) {
       Intent manualAddPage = new Intent(this, Firebase.class);
       startActivity(manualAddPage);
    }

    public void goToAutoFill(View view) {
        Intent autoFillPage = new Intent(this, MainActivity.class);
        startActivity(autoFillPage);
    }
}
