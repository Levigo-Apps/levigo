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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.LinearLayout;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.getcarebase.carebase.R;
import org.getcarebase.carebase.activities.Main.MainActivity;
import org.getcarebase.carebase.activities.Main.adapters.DeviceModelsAdapter;
import org.getcarebase.carebase.activities.Main.adapters.TypesAdapter;
import org.getcarebase.carebase.models.DeviceModel;
import org.getcarebase.carebase.repositories.DeviceRepository;
import org.getcarebase.carebase.utils.Request;
import org.getcarebase.carebase.viewmodels.DeviceViewModel;
import org.getcarebase.carebase.viewmodels.InventoryViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ModelListFragment extends Fragment {
    public static final String TAG = ModelListFragment.class.getName();

    private View rootView;
    private RecyclerView modelListRecyclerView;
    private TextInputLayout filterLayout;
    private SwipeRefreshLayout swipeRefreshLayout;
    private DeviceModelsAdapter deviceModelsAdapter;

    private Activity parent;

    private String type;

    private InventoryViewModel inventoryViewModel;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_model_list, container, false);
        filterLayout = rootView.findViewById(R.id.filter_layout);
        modelListRecyclerView = rootView.findViewById(R.id.types_dis);
        swipeRefreshLayout = rootView.findViewById(R.id.swipe_refresh_model_list);
        parent = getActivity();
        MaterialToolbar topToolBar = rootView.findViewById(R.id.type_title);

        Bundle bundle = this.getArguments();

        if (bundle != null) {
            type = bundle.getString("type");
        }

        topToolBar.setTitle(type);

        topToolBar.setNavigationOnClickListener(view -> {
            if (requireActivity().getSupportFragmentManager().getBackStackEntryCount() > 0) {
                requireActivity().getSupportFragmentManager().popBackStack();
            } else {
                if (parent != null) {
                    parent.onBackPressed();
                }
            }
        });

        inventoryViewModel = new ViewModelProvider(requireActivity()).get(InventoryViewModel.class);
        initModelList();

        // on refresh update inventory
        swipeRefreshLayout.setOnRefreshListener(() -> swipeRefresh());

        inventoryViewModel.loadDeviceModel(type);

        return rootView;
    }

    private void swipeRefresh() {
        inventoryViewModel.loadDeviceModel(type);
    }

    private void initModelList() {
        RecyclerView.LayoutManager modelListLayoutManager = new LinearLayoutManager(getContext());
        modelListRecyclerView.setLayoutManager(modelListLayoutManager);

        deviceModelsAdapter = new DeviceModelsAdapter(this);
        modelListRecyclerView.setAdapter(deviceModelsAdapter);

        inventoryViewModel.getDeviceModelListWithTypeLiveData(type).observe(getViewLifecycleOwner(), mapResource -> {
            if (mapResource.getRequest().getStatus() == Request.Status.LOADING) {
                ((MainActivity) getActivity()).showLoadingScreen();
            } else {
                ((MainActivity) getActivity()).removeLoadingScreen();
                if (mapResource.getRequest().getStatus() == Request.Status.SUCCESS) {
                    deviceModelsAdapter.setDeviceModels(mapResource.getData());
                    deviceModelsAdapter.notifyDataSetChanged();
                    swipeRefreshLayout.setRefreshing(false);
                } else if (mapResource.getRequest().getStatus() == Request.Status.ERROR) {
                    ((MainActivity) getActivity()).showErrorScreen();
                    Snackbar.make(rootView.findViewById(R.id.activity_main), mapResource.getRequest().getResourceString(), Snackbar.LENGTH_LONG).show();
                }
            }
        });
    }

}