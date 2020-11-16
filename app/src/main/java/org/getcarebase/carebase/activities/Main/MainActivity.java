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
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.snackbar.Snackbar;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.journeyapps.barcodescanner.CaptureActivity;

import org.getcarebase.carebase.R;
import org.getcarebase.carebase.activities.Login.LoginActivity;
import org.getcarebase.carebase.activities.Main.adapters.TypesAdapter;
import org.getcarebase.carebase.activities.Main.fragments.ItemDetailFragment;
import org.getcarebase.carebase.activities.Main.fragments.ItemDetailOfflineFragment;
import org.getcarebase.carebase.activities.Main.fragments.ItemDetailViewFragment;
import org.getcarebase.carebase.activities.Main.fragments.PendingUdiFragment;
import org.getcarebase.carebase.activities.Main.fragments.ProcedureInfoFragment;
import org.getcarebase.carebase.models.User;
import org.getcarebase.carebase.utils.Request;
import org.getcarebase.carebase.viewmodels.InventoryViewModel;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int RC_HANDLE_CAMERA_PERM = 1;

    private RecyclerView inventoryScroll;
    private SwipeRefreshLayout swipeRefreshLayout;
    private TypesAdapter typesAdapter;
    private TextView hospitalNameTextView;
    private TextView userEmailTextView;

    private InventoryViewModel inventoryViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);
        hospitalNameTextView = findViewById(R.id.main_hospital_textview);
        userEmailTextView = findViewById(R.id.main_user_email_textview);
        inventoryScroll = findViewById(R.id.main_categories);
        swipeRefreshLayout = findViewById(R.id.main_swipe_refresh_container);

        // get user info
        inventoryViewModel = new ViewModelProvider(this).get(InventoryViewModel.class);
        inventoryViewModel.getUserLiveData().observe(this, userResource -> {
            if (userResource.getRequest().getStatus() == Request.Status.SUCCESS) {
                User currentUser = userResource.getData();
                hospitalNameTextView.setText(currentUser.getHospitalName());
                userEmailTextView.setText(currentUser.getEmail());
                // update inventory once we get user
                initInventory();
            } else if (userResource.getRequest().getStatus() == Request.Status.ERROR) {
                Snackbar.make(findViewById(R.id.activity_main), userResource.getRequest().getResourceString(), Snackbar.LENGTH_LONG).show();
            }
        });

        // on refresh update inventory
        swipeRefreshLayout.setOnRefreshListener(() -> inventoryViewModel.loadInventory());

        getPermissions();
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

    private void initInventory() {
        RecyclerView.LayoutManager inventoryLayoutManager = new LinearLayoutManager(this);
        inventoryScroll.setLayoutManager(inventoryLayoutManager);

        typesAdapter = new TypesAdapter(MainActivity.this);
        inventoryScroll.setAdapter(typesAdapter);

        inventoryViewModel.getCategoricalInventoryLiveData().observe(this, mapResource -> {
            if (mapResource.getRequest().getStatus() == Request.Status.SUCCESS) {
                typesAdapter.setCategoricalInventory(mapResource.getData());
                typesAdapter.notifyDataSetChanged();
                swipeRefreshLayout.setRefreshing(false);
            } else if (mapResource.getRequest().getStatus() == Request.Status.ERROR) {
                Snackbar.make(findViewById(R.id.activity_main), mapResource.getRequest().getResourceString(), Snackbar.LENGTH_LONG).show();
            }
        });

        inventoryViewModel.loadInventory();
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
        // clears other fragments
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
    
    public void signOut() {
        inventoryViewModel.signOut();
        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
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
                signOut();
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
