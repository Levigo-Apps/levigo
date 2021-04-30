package org.getcarebase.carebase.activities.Main.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.google.android.material.snackbar.Snackbar;

import org.getcarebase.carebase.R;
import org.getcarebase.carebase.activities.Main.MainActivity;
import org.getcarebase.carebase.activities.Main.adapters.ShipmentsAdapter;
import org.getcarebase.carebase.utils.Request;
import org.getcarebase.carebase.viewmodels.DeviceViewModel;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

public class ShipmentFragment extends Fragment {
    public static final String TAG = ShipmentFragment.class.getName();

    private View rootView;
    private RecyclerView shipmentRecyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ShipmentsAdapter shipmentsAdapter;

    private DeviceViewModel deviceViewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.generic_swipe_refresh_list_layout, container, false);
        shipmentRecyclerView = rootView.findViewById(R.id.recycler_view);
        swipeRefreshLayout = rootView.findViewById(R.id.swipe_refresh_layout);

        deviceViewModel = new ViewModelProvider(requireActivity()).get(DeviceViewModel.class);

        //init shipments
        initShipments();

        // on refresh update shipments
//        swipeRefreshLayout.setOnRefreshListener(() -> inventoryViewModel.loadTypeList());
//
//        inventoryViewModel.loadTypeList();


        return rootView;
    }

    private void initShipments() {
        RecyclerView.LayoutManager inventoryLayoutManager = new LinearLayoutManager(getContext());
        shipmentRecyclerView.setLayoutManager(inventoryLayoutManager);

        shipmentsAdapter = new ShipmentsAdapter();
        shipmentRecyclerView.setAdapter(shipmentsAdapter);

        deviceViewModel.getUserLiveData().observe(getViewLifecycleOwner(), userResource -> {
            deviceViewModel.setupEntityRepository();
            deviceViewModel.getShipmentsLiveData().observe(getViewLifecycleOwner(), mapResource -> {
                if (mapResource.getRequest().getStatus() == Request.Status.LOADING) {
                    ((MainActivity) getActivity()).showLoadingScreen();
                } else {
                    ((MainActivity) getActivity()).removeLoadingScreen();
                    if (mapResource.getRequest().getStatus() == Request.Status.SUCCESS) {
                        if (mapResource.getData().size() == 0) {
                            ((MainActivity) getActivity()).showShipmentEmptyScreen();
                        } else {
                            ((MainActivity) getActivity()).removeInventoryEmptyScreen();
                            shipmentsAdapter.setShipments(mapResource.getData());
                            shipmentsAdapter.notifyDataSetChanged();
                        }
                        swipeRefreshLayout.setRefreshing(false);
                    } else if (mapResource.getRequest().getStatus() == Request.Status.ERROR) {
                        ((MainActivity) getActivity()).showErrorScreen();
                        Snackbar.make(rootView, mapResource.getRequest().getResourceString(), Snackbar.LENGTH_LONG).show();
                    }
                }
            });
        });
    }

    @Override
    public void onPause() {
        ((MainActivity) getActivity()).removeErrorScreen();
        ((MainActivity) getActivity()).removeShipmentEmptyScreen();
        ((MainActivity) getActivity()).removeLoadingScreen();
        super.onPause();
    }
}
