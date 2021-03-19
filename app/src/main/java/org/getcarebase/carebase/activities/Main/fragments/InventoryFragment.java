package org.getcarebase.carebase.activities.Main.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.viewpager2.widget.ViewPager2;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import org.getcarebase.carebase.R;
import org.getcarebase.carebase.activities.Main.MainActivity;
import org.getcarebase.carebase.activities.Main.adapters.TypesAdapter;
import org.getcarebase.carebase.models.DeviceModel;
import org.getcarebase.carebase.utils.Request;
import org.getcarebase.carebase.viewmodels.InventoryViewModel;

import java.io.Serializable;
import java.util.List;

public class InventoryFragment extends MiniFloatingActionButtonManagerFragment {

    private View rootView;
    private RecyclerView inventoryRecyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private TypesAdapter typesAdapter;

    private InventoryViewModel inventoryViewModel;

    private boolean resume = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.generic_swipe_refresh_list_layout, container, false);
        inventoryRecyclerView = rootView.findViewById(R.id.recycler_view);
        swipeRefreshLayout = rootView.findViewById(R.id.swipe_refresh_layout);

        inventoryViewModel = new ViewModelProvider(requireActivity()).get(InventoryViewModel.class);
        LinearLayout fabLayout = requireActivity().findViewById(R.id.fab_layout);
        View scanDeviceFAB = inflater.inflate(R.layout.mini_scan_device_fab,fabLayout, false);
        scanDeviceFAB.setOnClickListener(view -> ((MainActivity) requireActivity()).startScanner());
        View manualAddDeviceFAB = inflater.inflate(R.layout.mini_manual_add_device_fab,fabLayout,false);
        manualAddDeviceFAB.setOnClickListener(view -> ((MainActivity) requireActivity()).startItemForm(""));
        miniFABs = new View[] {scanDeviceFAB,manualAddDeviceFAB};
        super.onCreateView(inflater,fabLayout,savedInstanceState);

        initInventory();

        // on refresh update inventory
        swipeRefreshLayout.setOnRefreshListener(() -> inventoryViewModel.loadTypeList());

        inventoryViewModel.loadTypeList();

        return rootView;
    }

    private void initInventory() {
        RecyclerView.LayoutManager inventoryLayoutManager = new LinearLayoutManager(getContext());
        inventoryRecyclerView.setLayoutManager(inventoryLayoutManager);

        typesAdapter = new TypesAdapter(this);
        inventoryRecyclerView.setAdapter(typesAdapter);

        inventoryViewModel.getTypeListLiveData().observe(getViewLifecycleOwner(), mapResource -> {
            if (mapResource.getRequest().getStatus() == Request.Status.LOADING) {
                ((MainActivity) getActivity()).showLoadingScreen();
            } else {
                ((MainActivity) getActivity()).removeLoadingScreen();
                if (mapResource.getRequest().getStatus() == Request.Status.SUCCESS) {
                    if (mapResource.getData().size() == 0) {
                        ((MainActivity) getActivity()).showInventoryEmptyScreen();
                    } else {
                        ((MainActivity) getActivity()).removeInventoryEmptyScreen();
                        typesAdapter.setTypeList(mapResource.getData());
                        typesAdapter.notifyDataSetChanged();
                    }
                    swipeRefreshLayout.setRefreshing(false);
                } else if (mapResource.getRequest().getStatus() == Request.Status.ERROR) {
                    ((MainActivity) getActivity()).showErrorScreen();
                    Snackbar.make(rootView, mapResource.getRequest().getResourceString(), Snackbar.LENGTH_LONG).show();
                }
            }
        });
    }

    @Override
    public void onResume() {
        if (resume) {
            inventoryViewModel.loadTypeList();
        } else {
            resume = true;
        }
        super.onResume();
    }

    @Override
    public void onPause() {
        ((MainActivity) getActivity()).removeErrorScreen();
        ((MainActivity) getActivity()).removeInventoryEmptyScreen();
        ((MainActivity) getActivity()).removeLoadingScreen();
        super.onPause();
    }

    public void showModelList(final String type) {
        Fragment fragment = new ModelListFragment();
        Bundle bundle = new Bundle();
        bundle.putString("type", type);
        fragment.setArguments(bundle);

        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .setCustomAnimations(R.anim.fragment_fade_enter, R.anim.fragment_fade_exit)
                .add(R.id.activity_main, fragment, ModelListFragment.TAG)
                .addToBackStack(null)
                .commit();
    }

}