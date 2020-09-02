/*
 * Copyright 2020 Carebase Solutions
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

package org.getcarebase.carebase.activities.Main;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.journeyapps.barcodescanner.CaptureActivity;

import org.getcarebase.carebase.R;
import org.getcarebase.carebase.activities.Login.LoginActivity;
import org.getcarebase.carebase.activities.Main.adapters.InventoryViewAdapter;
import org.getcarebase.carebase.activities.Main.fragments.ItemDetailFragment;
import org.getcarebase.carebase.activities.Main.fragments.ItemDetailOfflineFragment;
import org.getcarebase.carebase.activities.Main.fragments.ItemDetailViewFragment;
import org.getcarebase.carebase.activities.Main.fragments.PendingUdiFragment;
import org.getcarebase.carebase.activities.Main.fragments.ProcedureInfoFragment;

import java.util.HashMap;
import java.util.Map;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int RC_HANDLE_CAMERA_PERM = 1;


    private FirebaseFirestore levigoDb = FirebaseFirestore.getInstance();
    private CollectionReference inventoryRef;

    private RecyclerView inventoryScroll;
    private RecyclerView.Adapter iAdapter;
    private RecyclerView.LayoutManager iLayoutManager;
    private Map<String, Object> entries = new HashMap<String, Object>();

    private Query query;
    private String key;
    private String value;

    // authorized hospital based on user
    private FirebaseAuth mAuth;
    private CollectionReference usersRef = levigoDb.collection("users");
    private String mNetworkId;
    private String mHospitalId;
    private String mHospitalName;
    private String mUserEmail;
    private Toolbar mToolbar;
    private TextView mHospitalNameTextView;
    private TextView mUserEmailTextView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Bundle bundle = this.getIntent().getExtras();
        if (bundle != null) {
            if (bundle.getString("key") != null && (bundle.getString("key")).equals("equipment_type")) {
                key = bundle.getString("key");
                value = bundle.getString("value");
            }
        } else {
            value = null;
            key = null;
        }

        mAuth = FirebaseAuth.getInstance();
        String userId = mAuth.getCurrentUser().getUid();

        mToolbar = findViewById(R.id.main_toolbar);
        mHospitalNameTextView = findViewById(R.id.main_hospital_textview);
        mUserEmailTextView = findViewById(R.id.main_user_email_textview);

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
                            mHospitalId = document.get("hospital_id").toString();
                            mHospitalName = document.get("hospital_name").toString();
                            mUserEmail = document.get("email").toString();
                            String inventoryRefUrl = "networks/" + mNetworkId + "/hospitals/" + mHospitalId + "/departments/default_department/dis";

//                            Toolbar mToolbar = findViewById(R.id.main_toolbar);
                            setSupportActionBar(mToolbar);
                            // TODO only display first word from hospital name to prevent being cut off
//                            mToolbar.setTitle(mHospitalName.split(" ", 2)[0]);
                            mHospitalNameTextView.setText(mHospitalName);
                            mUserEmailTextView.setText(mUserEmail);


                            inventoryRef = levigoDb.collection(inventoryRefUrl);
                            initInventory(value, key);

                            subscribeUser(mHospitalId);
                            getToken(currentUserRef);

                        } catch (NullPointerException e) {
                            FirebaseCrashlytics.getInstance().recordException(e);
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

        getPermissions();
        createNotificationChannel();
    }

    private void subscribeUser(final String hospitalId) {
        FirebaseMessaging.getInstance().subscribeToTopic(hospitalId)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        String msg = "Successfully subscribed to " + hospitalId;
                        if (!task.isSuccessful()) {
                            msg = "Error subscribing to " + hospitalId;
                        }
                        Log.d("TAG", msg);
                        Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void getToken(final DocumentReference userRef) {
        //Get Token for Current User
        FirebaseInstanceId.getInstance().getInstanceId().addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
            @Override
            public void onComplete(@NonNull Task<InstanceIdResult> task) {
                if (!task.isSuccessful()) {
                    Log.w(TAG, "getInstanceId failed", task.getException());
                    return;
                }

                // Get new Instance ID token
                String token = task.getResult().getToken();

                // Log and toast
                String msg = getString(R.string.msg_token_fmt, token);
                Log.d("Token: ", msg);
                Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();


                userRef.update("registration_token", token).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "DocumentSnapshot successfully updated!");
                    }
                })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.w(TAG, "Error updating document", e);
                            }
                        });


            }
        });
    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("levigo", name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager manager =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();
        boolean isAvailable = false;
        if (networkInfo != null && networkInfo.isConnected()) {
            // Network is present and connected
            isAvailable = true;
        }
        return isAvailable;
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

    private void initInventory(String key, String value) {
        iLayoutManager = new LinearLayoutManager(this);
        inventoryScroll.setLayoutManager(iLayoutManager);

        iAdapter = new InventoryViewAdapter(MainActivity.this, entries);
        inventoryScroll.setAdapter(iAdapter);

        if (value != null) {
            if (value.equals("equipment_type")) {
                Log.d(TAG, "IT GOT TO THE IF STATEMENT");
                query = inventoryRef.whereEqualTo(value, key);
                Log.d(TAG, "key and value returned in main areeeeee: " + key + " and " + value);
            } else if (key.equals("expiration")) {
                Log.d(TAG, "IT GOT TO THE ELSE IF STATEMENT");
                if (key == null) {
                    Log.d(TAG, "IT GOT TO THE ELSE IF IF STATEMENT");
                    query = inventoryRef.orderBy("expiration");
                } else {
                    Log.d(TAG, "IT GOT TO THE ELSE IF IF IF STATEMENT");
                    query = inventoryRef.orderBy("expiration", Query.Direction.DESCENDING);
                }
            }
        } else {
            query = inventoryRef;
        }

        query.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) throws NullPointerException {
                if (e != null) {
                    System.err.println("Listen failed: " + e);
                    return;
                }

                if (queryDocumentSnapshots == null) return;
                for (DocumentChange dc : queryDocumentSnapshots.getDocumentChanges()) {
                    try {
                        final Map<String, Object> di = dc.getDocument().getData();
                        final String type = di.get("equipment_type").toString();
                        final String diString = di.get("di").toString();

                        //TODO: add cases
                        Map<String, Object> types, dis, productid;
                        switch (dc.getType()) {
                            case ADDED:
                                Log.d(TAG, "Added");
                                dc.getDocument().getReference().collection("udis").addSnapshotListener(new EventListener<QuerySnapshot>() {
                                    @Override
                                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                                        if (e != null) {
                                            System.err.println("Listen failed: " + e);
                                            return;
                                        }

                                        if (!entries.containsKey("Category1")) {
                                            entries.put("Category1", new HashMap<>());
                                        }
                                        Map<String, Object> types = (HashMap<String, Object>) entries.get("Category1");
                                        if (!types.containsKey(type)) {
                                            types.put(type, new HashMap<>());
                                        }
                                        Map<String, Object> dis = (HashMap<String, Object>) types.get(type);
                                        if (!dis.containsKey(diString)) {
                                            dis.put(diString, new HashMap<>());
                                        }
                                        Map<String, Object> productid = (HashMap<String, Object>) dis.get(diString);
                                        if (!productid.containsKey("udis")) {
                                            productid.put("udis", new HashMap<>());
                                        }
                                        Map<String, Object> udis = (HashMap<String, Object>) productid.get("udis");

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
                                        iAdapter.notifyDataSetChanged();
                                    }
                                });
                            case MODIFIED:
                                Log.d(TAG, "Modified");
                                if (!entries.containsKey("Category1")) {
                                    entries.put("Category1", new HashMap<>());
                                }
                                types = (HashMap<String, Object>) entries.get("Category1");
                                if (!types.containsKey(type)) {
                                    types.put(type, new HashMap<>());
                                }
                                dis = (HashMap<String, Object>) types.get(type);
                                if (!dis.containsKey(diString)) {
                                    dis.put(diString, new HashMap<>());
                                }
                                productid = (HashMap<String, Object>) dis.get(diString);
                                productid.put("di", di);
                                break;
                            case REMOVED:
                                Log.d(TAG, "Removed");
                                if (!entries.containsKey("Category1")) {
                                    entries.put("Category1", new HashMap<>());
                                }
                                types = (HashMap<String, Object>) entries.get("Category1");
                                if (!types.containsKey(type)) {
                                    types.put(type, new HashMap<>());
                                }
                                dis = (HashMap<String, Object>) types.get(type);
                                if (!dis.containsKey(diString)) {
                                    dis.put(diString, new HashMap<>());
                                }
                                productid = (HashMap<String, Object>) dis.get(diString);
                                productid.remove("di");
                                break;
                        }
                        iAdapter.notifyDataSetChanged();
                    } catch (NullPointerException npe) {
                        FirebaseCrashlytics.getInstance().recordException(npe);
                        String toastMessage = "Error 0001: Failed to retrieve inventory information; Please report to support if possible";
                        Toast.makeText(getApplicationContext(), toastMessage, Toast.LENGTH_LONG).show();
                    }
                }
            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            String contents = result.getContents();
            if (contents != null) {
                if (isNetworkAvailable())
                    startItemView(contents);
                else
                    startItemOffline(contents);
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

        //clears other fragments
        fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);


        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.setCustomAnimations(R.anim.fui_slide_in_right, R.anim.fui_slide_out_left);
        fragmentTransaction.add(R.id.activity_main, fragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    public void startItemViewOnly(String barcode, String di) {
        ItemDetailViewFragment fragment = new ItemDetailViewFragment();
        Bundle bundle = new Bundle();
        bundle.putString("barcode", barcode);
        bundle.putString("di", di);
        fragment.setArguments(bundle);

        FragmentManager fragmentManager = getSupportFragmentManager();
        //clears other fragments
        fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);


        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.setCustomAnimations(R.anim.fui_slide_in_right, R.anim.fui_slide_out_left);
        fragmentTransaction.add(R.id.activity_main, fragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    public void startItemOffline(String barcode) {
        ItemDetailOfflineFragment fragment = new ItemDetailOfflineFragment();
        Bundle bundle = new Bundle();
        bundle.putString("barcode", barcode);
        bundle.putBoolean("editingExisting", false);
        fragment.setArguments(bundle);

        FragmentManager fragmentManager = getSupportFragmentManager();
        //clears other fragments
        fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);


        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.setCustomAnimations(R.anim.fui_slide_in_right, R.anim.fui_slide_out_left);
        fragmentTransaction.add(R.id.activity_main, fragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();

    }

    public void startPendingEquipment(String barcode) {
        PendingUdiFragment fragment = new PendingUdiFragment();
        Bundle bundle = new Bundle();
        bundle.putString("barcode", barcode);
        fragment.setArguments(bundle);

        FragmentManager fragmentManager = getSupportFragmentManager();
        //clears other fragments
        fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);


        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.setCustomAnimations(R.anim.fui_slide_in_right, R.anim.fui_slide_out_left);
        fragmentTransaction.add(R.id.activity_main, fragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();

    }

    public void startProcedureInfo(String barcode) {
        ProcedureInfoFragment fragment = new ProcedureInfoFragment();
        Bundle bundle = new Bundle();
        bundle.putString("barcode", barcode);
        fragment.setArguments(bundle);

        FragmentManager fragmentManager = getSupportFragmentManager();
        //clears other fragments
        fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.setCustomAnimations(R.anim.fui_slide_in_right, R.anim.fui_slide_out_left);
        fragmentTransaction.add(R.id.activity_main, fragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
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
            case android.R.id.home:
                startScanner();
                return true;
            case R.id.manual_entry:
                // if device has an access to the network regular manual entry opens
                if (isNetworkAvailable()) {
                    startItemView("");
                    // if device does not have an access to the network, offline manual entry opens
                } else {
                    startItemOffline("");
                }
                return true;
            case R.id.logout:
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
                return true;
            case R.id.pendingUdiFragment:
                startPendingEquipment("");
                return true;

            case R.id.procedureInfo:
                startProcedureInfo("");
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }
}