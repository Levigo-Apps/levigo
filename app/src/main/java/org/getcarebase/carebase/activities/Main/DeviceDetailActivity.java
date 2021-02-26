package org.getcarebase.carebase.activities.Main;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import org.getcarebase.carebase.R;
import org.getcarebase.carebase.activities.Main.fragments.ItemDetailViewFragment;
import org.getcarebase.carebase.viewmodels.DeviceViewModel;

public class DeviceDetailActivity extends AppCompatActivity {

    private DeviceViewModel deviceViewModel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        deviceViewModel = new ViewModelProvider(this).get(DeviceViewModel.class);
        deviceViewModel.getUserLiveData().observe(this,userResource -> {
            deviceViewModel.setupDeviceRepository();
            String di = getIntent().getStringExtra("di");
            String udi = getIntent().getStringExtra("udi");
            // start at Device Detail Fragment
            showDeviceDetail(di,udi);
        });
        setContentView(R.layout.activity_generic);


    }

    public void showDeviceDetail(final String di, final String udi) {
        Fragment fragment = new ItemDetailViewFragment();
        Bundle bundle = new Bundle();
        bundle.putString("udi", udi);
        bundle.putString("di", di);
        fragment.setArguments(bundle);
        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.frame_layout,fragment,ItemDetailViewFragment.TAG)
                .addToBackStack(null)
                .commit();
    }
}
