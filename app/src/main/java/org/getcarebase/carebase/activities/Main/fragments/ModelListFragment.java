package org.getcarebase.carebase.activities.Main.fragments;

import android.app.Activity;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.snackbar.Snackbar;

import org.getcarebase.carebase.R;
import org.getcarebase.carebase.activities.Main.MainActivity;
import org.getcarebase.carebase.activities.Main.adapters.DeviceModelsAdapter;
import org.getcarebase.carebase.activities.Main.adapters.TypesAdapter;
import org.getcarebase.carebase.models.DeviceModel;
import org.getcarebase.carebase.utils.Request;
import org.getcarebase.carebase.viewmodels.InventoryViewModel;

import java.util.List;

public class ModelListFragment extends Fragment {
    public static final String TAG = ModelListFragment.class.getName();

    private View rootView;
    private RecyclerView modelListRecyclerView;
//    private SwipeRefreshLayout swipeRefreshLayout;
    private DeviceModelsAdapter deviceModelsAdapter;

    private Activity parent;

    private String type;

    private InventoryViewModel inventoryViewModel;

    public interface DeviceClickCallback {
        void showDeviceDetail(final String di, final String udi);
    }

    private DeviceClickCallback deviceClickCallback = this::showDeviceDetail;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_model_list, container, false);
        modelListRecyclerView = rootView.findViewById(R.id.types_dis);
        parent = getActivity();
        MaterialToolbar topToolBar = rootView.findViewById(R.id.type_title);

        Bundle bundle = this.getArguments();

        if (bundle != null) {
            type = bundle.getString("type");
        }

        topToolBar.setTitle(type);

        inventoryViewModel = new ViewModelProvider(requireActivity()).get(InventoryViewModel.class);
        initModelList();

        topToolBar.setNavigationOnClickListener(view -> {
            if (parent != null)
                parent.onBackPressed();
        });

        return rootView;
    }

    private void initModelList() {
        RecyclerView.LayoutManager modelListLayoutManager = new LinearLayoutManager(getContext());
        modelListRecyclerView.setLayoutManager(modelListLayoutManager);

        deviceModelsAdapter = new DeviceModelsAdapter(this);
        modelListRecyclerView.setAdapter(deviceModelsAdapter);

        inventoryViewModel.getDeviceModelListWithTypeLiveData(type).observe(getViewLifecycleOwner(), mapResource -> {
            if (mapResource.getRequest().getStatus() == Request.Status.SUCCESS) {
                deviceModelsAdapter.setDeviceModels(mapResource.getData());
                deviceModelsAdapter.notifyDataSetChanged();
//                swipeRefreshLayout.setRefreshing(false);
            } else if (mapResource.getRequest().getStatus() == Request.Status.ERROR) {
                Snackbar.make(rootView.findViewById(R.id.activity_main), mapResource.getRequest().getResourceString(), Snackbar.LENGTH_LONG).show();
            }
        });

    }

    public void showDeviceDetail(final String di, final String udi) {
        Fragment fragment = new ItemDetailViewFragment();
        Bundle bundle = new Bundle();
        // TODO rename parameters
        bundle.putString("barcode", udi);
        bundle.putString("di", di);
        fragment.setArguments(bundle);
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.activity_main,fragment,ItemDetailViewFragment.TAG)
                .addToBackStack(null)
                .commit();
    }
}