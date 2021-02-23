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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.journeyapps.barcodescanner.CaptureActivity;

import org.getcarebase.carebase.R;
import org.getcarebase.carebase.activities.Login.LoginActivity;
import org.getcarebase.carebase.activities.Main.adapters.HomePagerAdapter;
import org.getcarebase.carebase.activities.Main.fragments.ItemDetailFragment;
import org.getcarebase.carebase.activities.Main.fragments.ItemDetailOfflineFragment;
import org.getcarebase.carebase.activities.Main.fragments.ModelListFragment;
import org.getcarebase.carebase.activities.Main.fragments.PendingUdiFragment;
import org.getcarebase.carebase.models.User;
import org.getcarebase.carebase.utils.Request;
import org.getcarebase.carebase.viewmodels.InventoryViewModel;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int RC_HANDLE_CAMERA_PERM = 1;
    private static final int RC_ADD_PROCEDURE = 2;
    private static final int RC_EDIT_DEVICE_DETAILS = 3;

    private Toolbar toolbar;

    private InventoryViewModel inventoryViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_host_layout);
        toolbar = findViewById(R.id.toolbar);
        toolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.logout) {
                signOut();
                return true;
            }
            return false;
        });

        // get user info
        inventoryViewModel = new ViewModelProvider(this).get(InventoryViewModel.class);
        inventoryViewModel.getUserLiveData().observe(this, userResource -> {
            if (userResource.getRequest().getStatus() == Request.Status.SUCCESS) {
                User currentUser = userResource.getData();
                toolbar.setTitle(currentUser.getHospitalName());
                toolbar.inflateMenu(R.menu.main_toolbar);
                setUpViewPager();
            } else if (userResource.getRequest().getStatus() == Request.Status.ERROR) {
                Snackbar.make(findViewById(R.id.activity_main), userResource.getRequest().getResourceString(), Snackbar.LENGTH_LONG).show();
            }
        });

        getPermissions();
    }

    private void setUpViewPager() {
        HomePagerAdapter adapter = new HomePagerAdapter(this);
        ViewPager2 viewPager = findViewById(R.id.home_view_pager);
        viewPager.setAdapter(adapter);
        TabLayout tabLayout = findViewById(R.id.home_tab_layout);
        new TabLayoutMediator(tabLayout,viewPager,(tab, position) -> {
            switch (position) {
                case 0: tab.setText("Inventory");
                    break;
                case 1: tab.setText("Procedures");
                    break;
            }
        }).attach();
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

    public void startScanner() {
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null && result.getContents() != null) {
            String contents = result.getContents().trim();
            if (!contents.equals("")) {
                startItemForm(contents);
            }

            if (result.getBarcodeImagePath() != null) {
                Log.d(TAG, "" + result.getBarcodeImagePath());
            }
        } else if (requestCode == RC_ADD_PROCEDURE) {
            if (resultCode == RESULT_OK) {
                Snackbar.make(findViewById(R.id.activity_main),"Procedure Saved", Snackbar.LENGTH_LONG).show();
            }
        } else if (requestCode == RC_EDIT_DEVICE_DETAILS) {
            if (resultCode == RESULT_OK) {
                boolean edited = Objects.requireNonNull(data).getBooleanExtra("edit", false);
                if (edited) {
                    // reload model list fragment if edited
                    Fragment fragment = getSupportFragmentManager().findFragmentByTag(ModelListFragment.TAG);
                    final FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                    ft.detach(fragment);
                    ft.attach(fragment);
                    ft.commit();
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    public void startItemForm(String barcode) {
        if (isNetworkAvailable())
            startItemFormOnline(barcode);
        else
            startItemFormOffline(barcode);
    }

    public void startItemFormOnline(String barcode) {
        ItemDetailFragment fragment = new ItemDetailFragment();
        if (!barcode.trim().isEmpty()) {
            Bundle bundle = new Bundle();
            bundle.putString("barcode", barcode);
            fragment.setArguments(bundle);
        }

        FragmentManager fragmentManager = getSupportFragmentManager();

        //clears other fragments
        fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);


        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left);
        fragmentTransaction.add(R.id.activity_main, fragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    public void startDeviceDetail(final String di, final String udi) {
        Intent intent = new Intent(this,DeviceDetailActivity.class);
        intent.putExtra("di",di);
        intent.putExtra("udi",udi);
        startActivityForResult(intent, RC_EDIT_DEVICE_DETAILS);
    }

    public void startItemFormOffline(String barcode) {
        ItemDetailOfflineFragment fragment = new ItemDetailOfflineFragment();
        Bundle bundle = new Bundle();
        bundle.putString("barcode", barcode);
        bundle.putBoolean("editingExisting", false);
        fragment.setArguments(bundle);

        FragmentManager fragmentManager = getSupportFragmentManager();
        // clears other fragments
        fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);

        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_in_left);
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
        fragmentTransaction.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left);
        fragmentTransaction.add(R.id.activity_main, fragment, PendingUdiFragment.TAG);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    public void startProcedureInfo() {
        startActivityForResult(new Intent(this, AddProcedureActivity.class),RC_ADD_PROCEDURE);
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
}
