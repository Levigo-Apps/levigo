package org.getcarebase.carebase.activities.Main;

import android.content.Context;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.snackbar.Snackbar;

import org.getcarebase.carebase.R;
import org.getcarebase.carebase.activities.Main.fragments.DeviceDataFormFragment;
import org.getcarebase.carebase.activities.Main.fragments.DeviceUDIFormFragment;
import org.getcarebase.carebase.models.User;
import org.getcarebase.carebase.utils.Request;
import org.getcarebase.carebase.viewmodels.AddDeviceViewModel;
import org.getcarebase.carebase.viewmodels.AuthViewModel;

public class AddDeviceActivity extends AppCompatActivity {

    private AddDeviceViewModel viewModel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(AddDeviceViewModel.class);
        setContentView(R.layout.activity_generic);
        goToAddDeviceUDIFragment();

        View view = findViewById(R.id.frame_layout);

        viewModel.getUserLiveData().observe(this,resource -> {
            if (resource.getRequest().getStatus() == Request.Status.SUCCESS) {
                User user = resource.getData();
                setup(user);
            } else if (resource.getRequest().getStatus() == Request.Status.ERROR) {
                Snackbar.make(view,resource.getRequest().getResourceString(),Snackbar.LENGTH_LONG);
            }
        });

        viewModel.getDeviceModelLiveData().observe(this,resource -> {
            if (resource.getRequest().getStatus() == Request.Status.SUCCESS) {
                goToAddDeviceDataFragment();
            } else if (resource.getRequest().getStatus() == Request.Status.ERROR) {
                Snackbar.make(view,resource.getRequest().getResourceString(),Snackbar.LENGTH_LONG);
            }
        });
    }
    private void setup(User user) {
        viewModel.setupDeviceRepository(user.getNetworkId(),user.getEntityId());

        String barcode = getIntent().getStringExtra("barcode");
        viewModel.uniqueDeviceIdentifierLiveData.setValue(barcode);
        if (!barcode.isEmpty()) {
            viewModel.onAutoPopulate();
        }
    }

    private void goToAddDeviceUDIFragment() {
        Fragment fragment = new DeviceUDIFormFragment();
        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.frame_layout,fragment,DeviceUDIFormFragment.TAG)
                .commit();
    }

    private void goToAddDeviceDataFragment() {
        Fragment fragment = new DeviceDataFormFragment();
        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.frame_layout,fragment,DeviceDataFormFragment.TAG)
                .commit();
    }
}
