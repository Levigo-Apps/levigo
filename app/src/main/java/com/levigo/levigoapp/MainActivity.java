/*
 * Copyright 2020 Levigo Apps
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.levigo.levigoapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Filter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.journeyapps.barcodescanner.CaptureActivity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class MainActivity extends AppCompatActivity implements Serializable {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int RC_HANDLE_CAMERA_PERM = 1;


    private FirebaseFirestore levigoDb = FirebaseFirestore.getInstance();
    private CollectionReference inventoryRef;

    private RecyclerView inventoryScroll;
    private RecyclerView.Adapter iAdapter;
    private RecyclerView.LayoutManager iLayoutManager;
    private Map<String, Object> entries = new HashMap<String, Object>();

    private FloatingActionButton mAdd;

    private Query query;

    // authorized hospital based on user
    private FirebaseAuth mAuth;
    private CollectionReference usersRef = levigoDb.collection("users");
    private String mNetworkId;
    private String mNetworkName;
    private String mHospitalId;
    private String mHospitalName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        String userId = mAuth.getCurrentUser().getUid();

        // Get user information in "users" collection
        final DocumentReference currentUserRef = usersRef.document(userId);
        currentUserRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                String toastMessage;
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        try {
                            mNetworkId = document.get("network_id").toString();
                            mNetworkName = document.get("network_name").toString();
                            mHospitalId = document.get("hospital_id").toString();
                            mHospitalName = document.get("hospital_name").toString();
                            String inventoryRefUrl = "networks/network1/sites/n1_hospital3/n1_h3_departments/department1/n1_h1_d1 productids";
                            //TODO update to the following
                            //String inventoryRefUrl = "networks/" + mNetworkId + "/hospitals/" + mHospitalId + "/departments/default_department/dis";

                            Toolbar mToolbar = findViewById(R.id.main_toolbar);
                            setSupportActionBar(mToolbar);
                            mToolbar.setTitle(mHospitalName);

                            inventoryRef = levigoDb.collection(inventoryRefUrl);
                            //query = inventoryRef.whereEqualTo("equipment_type", "Scalpel");

                            initInventory();
                        } catch (NullPointerException e) {
                            toastMessage = "Error retrieving user information; Please contact support";
                            Toast.makeText(getApplicationContext(), toastMessage, Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        // document for user doesn't exist
                        toastMessage = "User not found; Please contact support";
                        Toast.makeText(getApplicationContext(), toastMessage, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    toastMessage = "User lookup failed; Please try again and contact support if issue persists";
                    Toast.makeText(getApplicationContext(), toastMessage, Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });

        inventoryScroll = findViewById(R.id.main_categories);
        mAdd = findViewById(R.id.main_add);
        mAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startScanner();
            }
        });

        getPermissions();
    }

    private void startScanner() {
        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.setCaptureActivity(CaptureActivity.class);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES);
        integrator.setBarcodeImageEnabled(true);
        integrator.initiateScan();
    }

    private void getPermissions() {
        final String[] permissions = new String[]{Manifest.permission.CAMERA};
        if (checkSelfPermission(permissions[0]) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(permissions, RC_HANDLE_CAMERA_PERM);
        }
    }

    private void initInventory() {
        iLayoutManager = new LinearLayoutManager(this);
        inventoryScroll.setLayoutManager(iLayoutManager);

        iAdapter = new InventoryViewAdapter(MainActivity.this, entries);
        inventoryScroll.setAdapter(iAdapter);

        inventoryRef.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) throws NullPointerException {
                if (e != null) {
                    System.err.println("Listen failed: " + e);
                    return;
                }

                if(queryDocumentSnapshots == null) return;
                for (DocumentChange dc : queryDocumentSnapshots.getDocumentChanges()) {
                    final Map<String, Object> di = dc.getDocument().getData();
                    final String type = di.get("equipment_type").toString();
                    final String diString = di.get("di").toString();
//                    Log.d(TAG, "Data di: " + di.toString());
//                    Log.d(TAG, "UDIs: " + dc.getDocument().getReference().collection("UDIs"));
                    //TODO: add cases
                    Map<String, Object> types, dis, productid;
                    switch (dc.getType()) {
                        case ADDED:
                            Log.d(TAG, "Added");
                            dc.getDocument().getReference().collection("UDIs").addSnapshotListener(new EventListener<QuerySnapshot>() {
                                @Override
                                public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                                    if (e != null) {
                                        System.err.println("Listen failed: " + e);
                                        return;
                                    }
                                    if(queryDocumentSnapshots == null) return;

                                    if (!entries.containsKey("Category1")) {
                                        entries.put("Category1", new HashMap<>());
                                    }
                                    Map<String, Object> types = (HashMap<String, Object>) entries.get("Category1");
                                    if(types == null) return;
                                    if (!types.containsKey(type)) {
                                        types.put(type, new HashMap<>());
                                    }
                                    Map<String, Object> dis = (HashMap<String, Object>) types.get(type);
                                    if(dis == null) return;
                                    if (!dis.containsKey(diString)) {
                                        dis.put(diString, new HashMap<>());
                                    }
                                    Map<String, Object> productid = (HashMap<String, Object>) dis.get(diString);
                                    if(productid == null) return;
                                    if (!productid.containsKey("udis")) {
                                        productid.put("udis", new HashMap<>());
                                    }
                                    Map<String, Object> udis = (HashMap<String, Object>) productid.get("udis");
                                    if(udis == null) return;

                                    for (DocumentChange dc : queryDocumentSnapshots.getDocumentChanges()) {
                                        Map<String, Object> data = dc.getDocument().getData();
                                        //TODO: make safe
                                        String udi = data.get("udi").toString();
                                        switch (dc.getType()) {
                                            case ADDED:
                                            case MODIFIED:
                                                udis.put(udi, data);
                                                break;
                                            case REMOVED:
                                                udis.remove(udi);
                                        }
                                    }

//                                    productid.put("udis", udis);
//                                    Log.d(TAG, "ENTRIES: " + entries);
                                    iAdapter.notifyDataSetChanged();
                                }
                            });
                        case MODIFIED:
                            Log.d(TAG, "Modified");
                            if (!entries.containsKey("Category1")) {
                                entries.put("Category1", new HashMap<>());
                            }
                            types = (HashMap<String, Object>) entries.get("Category1");
                            if(types == null) return;
                            if (!types.containsKey(type)) {
                                types.put(type, new HashMap<>());
                            }
                            dis = (HashMap<String, Object>) types.get(type);
                            if(dis == null) return;
                            if (!dis.containsKey(diString)) {
                                dis.put(diString, new HashMap<>());
                            }
                            productid = (HashMap<String, Object>) dis.get(diString);
                            if(productid == null) return;
                            productid.put("di", di);
                            break;
                        case REMOVED:
                            Log.d(TAG, "Removed");
                            if (!entries.containsKey("Category1")) {
                                entries.put("Category1", new HashMap<>());
                            }
                            types = (HashMap<String, Object>) entries.get("Category1");
                            if(types == null) return;
                            if (!types.containsKey(type)) {
                                types.put(type, new HashMap<>());
                            }
                            dis = (HashMap<String, Object>) types.get(type);
                            if(dis == null) return;
                            if (!dis.containsKey(diString)) {
                                dis.put(diString, new HashMap<>());
                            }
                            productid = (HashMap<String, Object>) dis.get(diString);
                            if(productid == null) return;
                            productid.remove("di");
                            break;
                    }
                    iAdapter.notifyDataSetChanged();
//                    switch(dc.getType()) {
//                        case ADDED:
//                            Log.d(TAG, "added");
//                            entries.add(entry);
//                            break;
//                        case REMOVED:
//                            Log.d(TAG, "remove");
//                            for(int i = 0; i < entries.size(); ++i) {
//                                if(entries.get(i).get("di").equals(entry.get("di"))) {
//                                    Log.d(TAG, "remove2");
//                                    entries.remove(i);
//                                    break;
//                                }
//                            }
//                            break;
//                        case MODIFIED:
//                            Log.d(TAG, "modify");
//                            for(int i = 0; i < entries.size(); ++i) {
//                                if(entries.get(i).get("di").equals(entry.get("di"))) {
//                                    Log.d(TAG, "modify2");
//                                    entries.set(i,entry);
//                                    break;
//                                }
//                            }
//                            break;
//                    }
                }
//                iAdapter.notifyDataSetChanged();
            }
        });
    }
//    private void addItem(String json) {
//        HashMap<String, String> data = new Gson().fromJson(json, HashMap.class);
//        String udi = data.get("udi");
//        if(udi == null) udi = "UNKNOWN UDI";
//        inventoryRef.document(udi).set(data).addOnSuccessListener(new OnSuccessListener<Void>() {
//            @Override
//            public void onSuccess(Void aVoid) {
//                Log.d(TAG, "Added Successfully");
//            }
//        });
//    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            String contents = result.getContents();
            if (contents != null) {
                startItemView(contents);

            }
            if (result.getBarcodeImagePath() != null) {
                Log.d(TAG, "" + result.getBarcodeImagePath());
//                mImageView.setImageBitmap(BitmapFactory.decodeFile(result.getBarcodeImagePath()));
                //maybe add image to firebase storage
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    public void startItemView(String barcode) {
        ItemDetailFragment fragment = new ItemDetailFragment();
        Bundle bundle = new Bundle();
        bundle.putString("barcode", barcode);
        fragment.setArguments(bundle);

        FragmentManager fragmentManager = getSupportFragmentManager();
//        List<Fragment> fragments = fragmentManager.getFragments();
//        //prevents creating more than one ItemDetailFragment
//        for(Fragment f : fragments) {
//            if(f instanceof ItemDetailFragment){
//                return;
//            }
//        }

        //clears other fragments
        fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);


        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.setCustomAnimations(R.anim.fui_slide_in_right, R.anim.fui_slide_out_left);
        fragmentTransaction.add(R.id.activity_main, fragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();

//                mAdd.setVisibility(View.GONE);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode != RC_HANDLE_CAMERA_PERM) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        } else if (!(grantResults.length != 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
            finish();
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_toolbar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.manual_entry:
                startItemView("");
                return true;
            case R.id.logout:
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
                return true;
            case R.id.network:
                Intent intent_network = new Intent(getApplicationContext(), NetworkActivity.class);
                startActivity(intent_network);
                finish();
                return true;
            case R.id.settings:
                //TODO next step
//                Toast.makeText(this, "Not implemented yet", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.filter:
                Log.d(TAG, "reached case filter");
                Intent intent_filter = new Intent(getApplicationContext(), FilterActivity.class);
                startActivity(intent_filter);
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}
