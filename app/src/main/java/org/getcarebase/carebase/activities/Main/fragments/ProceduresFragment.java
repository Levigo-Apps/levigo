package org.getcarebase.carebase.activities.Main.fragments;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.snackbar.Snackbar;

import org.getcarebase.carebase.R;
import org.getcarebase.carebase.activities.Main.MainActivity;
import org.getcarebase.carebase.activities.Main.adapters.ProceduresAdapter;
import org.getcarebase.carebase.utils.Request;
import org.getcarebase.carebase.viewmodels.ProceduresViewModel;

public class ProceduresFragment extends FloatingActionButtonManagerFragment {

    private View rootView;
    private RecyclerView proceduresRecyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;

    private ProceduresViewModel proceduresViewModel;

    public interface ProcedureClickCallback {
        void showProcedureDetail(final String procedureId);
    }

    private final ProceduresFragment.ProcedureClickCallback procedureClickCallback = this::showProcedureDetail;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.generic_swipe_refresh_list_layout, container, false);
        RecyclerView proceduresRecyclerView = rootView.findViewById(R.id.recycler_view);

        // set up list divider
        Drawable divider = getContext().getDrawable(R.drawable.divider);
        DividerItemDecoration itemDecoration = new DividerItemDecoration(getContext(),DividerItemDecoration.VERTICAL);
        itemDecoration.setDrawable(divider);
        proceduresRecyclerView.addItemDecoration(itemDecoration);

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

        ProceduresAdapter proceduresAdapter = new ProceduresAdapter(procedureClickCallback);
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

        // On scroll of procedures recycler view will load more procedures
        proceduresRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                proceduresViewModel.loadProcedures();
            }
        });
        swipeRefreshLayout.setOnRefreshListener(() -> proceduresViewModel.loadProcedures());

        return rootView;
    }

    @Override
    public void onMainFloatingActionButtonClicked(View view) {
        // start add procedure screen
        ((MainActivity) requireActivity()).startProcedureInfo();
    }

    public void showProcedureDetail(final String procedureId) {
        Fragment fragment = new ProcedureDetailFragment();
        Bundle bundle = new Bundle();
        bundle.putString("procedure_id",procedureId);
        fragment.setArguments(bundle);
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.activity_main,fragment,ProcedureDetailFragment.TAG)
                .addToBackStack(null)
                .commit();
    }
}
