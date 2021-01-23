package org.getcarebase.carebase.activities.Main.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.snackbar.Snackbar;

import org.getcarebase.carebase.R;
import org.getcarebase.carebase.activities.Main.adapters.ProceduresAdapter;
import org.getcarebase.carebase.utils.Request;
import org.getcarebase.carebase.viewmodels.ProceduresViewModel;

public class ProceduresFragment extends MiniFloatingActionButtonManagerFragment {

    private View rootView;
    private RecyclerView proceduresRecyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;

    private ProceduresViewModel proceduresViewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.generic_swipe_refresh_list_layout, container, false);
        proceduresRecyclerView = rootView.findViewById(R.id.recycler_view);
        swipeRefreshLayout = rootView.findViewById(R.id.swipe_refresh_layout);

        proceduresViewModel = new ViewModelProvider(requireActivity()).get(ProceduresViewModel.class);
        proceduresViewModel.getUserLiveData().observe(getViewLifecycleOwner(),userResource -> {
            if (userResource.getRequest().getStatus() == Request.Status.SUCCESS) {
                proceduresViewModel.setupRepository();
                proceduresViewModel.loadProcedures();
            } else {
                Snackbar.make(rootView.findViewById(R.id.activity_main), userResource.getRequest().getResourceString(), Snackbar.LENGTH_LONG).show();
            }
        });

        // setup procedures recycler view
        RecyclerView.LayoutManager inventoryLayoutManager = new LinearLayoutManager(getContext());
        proceduresRecyclerView.setLayoutManager(inventoryLayoutManager);

        ProceduresAdapter proceduresAdapter = new ProceduresAdapter();
        proceduresRecyclerView.setAdapter(proceduresAdapter);

        proceduresViewModel.getProceduresLiveData().observe(getViewLifecycleOwner(), proceduresResource -> {
            if (proceduresResource.getRequest().getStatus() == Request.Status.SUCCESS) {
                proceduresAdapter.setProcedures(proceduresResource.getData());
                proceduresAdapter.notifyDataSetChanged();
                swipeRefreshLayout.setRefreshing(false);
            } else if (proceduresResource.getRequest().getStatus() == Request.Status.ERROR) {
                Snackbar.make(rootView,proceduresResource.getRequest().getResourceString(),Snackbar.LENGTH_LONG).show();
            }
        });

        swipeRefreshLayout.setOnRefreshListener(() -> proceduresViewModel.loadProcedures());

        return rootView;
    }

}
