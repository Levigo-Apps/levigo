package org.getcarebase.carebase.activities.Main.fragments;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
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

    private boolean resume = false;

    public interface ProcedureClickCallback {
        void showProcedureDetail(final String procedureId);
    }

    // interface for reaching the bottom of a procedures list
    public interface OnBottomReachedCallback {
        void onBottomReached();
    }

    private final ProceduresFragment.ProcedureClickCallback procedureClickCallback = this::showProcedureDetail;
    // method will call load procedures
    private final OnBottomReachedCallback onBottomReachedCallback = () -> proceduresViewModel.loadProcedures(false);

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.generic_swipe_refresh_list_layout, container, false);
        proceduresRecyclerView = rootView.findViewById(R.id.recycler_view);
        swipeRefreshLayout = rootView.findViewById(R.id.swipe_refresh_layout);

        setUpListDivider();

        proceduresViewModel = new ViewModelProvider(requireActivity()).get(ProceduresViewModel.class);
        proceduresViewModel.getUserLiveData().observe(getViewLifecycleOwner(),userResource -> {
            if (userResource.getRequest().getStatus() == Request.Status.SUCCESS) {
                proceduresViewModel.setupRepository();
                proceduresViewModel.loadProcedures(false);
            } else {
                Snackbar.make(rootView.findViewById(R.id.activity_main), userResource.getRequest().getResourceString(), Snackbar.LENGTH_LONG).show();
            }
        });

        initProcedures();

        return rootView;
    }

    private void setUpListDivider() {
        Drawable divider = getContext().getDrawable(R.drawable.divider);
        DividerItemDecoration itemDecoration = new DividerItemDecoration(getContext(),DividerItemDecoration.VERTICAL);
        itemDecoration.setDrawable(divider);
        proceduresRecyclerView.addItemDecoration(itemDecoration);
    }

    private void initProcedures() {
        // setup procedures recycler view
        RecyclerView.LayoutManager inventoryLayoutManager = new LinearLayoutManager(getContext());
        proceduresRecyclerView.setLayoutManager(inventoryLayoutManager);

        ProceduresAdapter proceduresAdapter = new ProceduresAdapter(procedureClickCallback);
        proceduresRecyclerView.setAdapter(proceduresAdapter);
        proceduresAdapter.setOnBottmReachedCallback(onBottomReachedCallback);

        proceduresViewModel.getProceduresLiveData().observe(getViewLifecycleOwner(), proceduresResource -> {
            if (proceduresResource.getRequest().getStatus() == Request.Status.LOADING) {
                ((MainActivity) getActivity()).showLoadingScreen();
            } else {
                ((MainActivity) getActivity()).removeLoadingScreen();
                if (proceduresResource.getRequest().getStatus() == Request.Status.SUCCESS) {
                    if (proceduresResource.getData().size() == 0) {
                        ((MainActivity) getActivity()).showProcedureEmptyScreen();
                    } else {
                        ((MainActivity) getActivity()).removeProcedureEmptyScreen();
                        proceduresAdapter.setProcedures(proceduresResource.getData());
                        proceduresAdapter.notifyDataSetChanged();
                    }
                    swipeRefreshLayout.setRefreshing(false);
                } else if (proceduresResource.getRequest().getStatus() == Request.Status.ERROR) {
                    ((MainActivity) getActivity()).showErrorScreen();
                    Snackbar.make(rootView,proceduresResource.getRequest().getResourceString(),Snackbar.LENGTH_LONG).show();
                }
            }
        });
        swipeRefreshLayout.setOnRefreshListener(() -> proceduresViewModel.loadProcedures(true));
    }

    @Override
    public void onResume() {
        if (resume) {
            proceduresViewModel.loadProcedures(true);
        } else {
            resume = true;
        }
        super.onResume();
    }

    @Override
    public void onPause() {
        ((MainActivity) getActivity()).removeErrorScreen();
        ((MainActivity) getActivity()).removeProcedureEmptyScreen();
        ((MainActivity) getActivity()).removeLoadingScreen();
        super.onPause();
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
