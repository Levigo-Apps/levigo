package org.getcarebase.carebase.activities.Main.fragments;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.snackbar.Snackbar;

import org.getcarebase.carebase.R;
import org.getcarebase.carebase.activities.Main.adapters.PendingDevicesAdapter;
import org.getcarebase.carebase.utils.Request;
import org.getcarebase.carebase.viewmodels.PendingDeviceViewModel;

import java.util.Objects;

public class PendingUdiFragment extends Fragment {
    public static final String TAG = PendingUdiFragment.class.getSimpleName();

    public interface ItemClickFunction {
        void openEditView(String id);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,ViewGroup container,Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_pendingudis, container, false);
        MaterialToolbar topToolBar = rootView.findViewById(R.id.topAppBar);
        topToolBar.setNavigationOnClickListener(view -> requireActivity().onBackPressed());
        RecyclerView recyclerView = rootView.findViewById(R.id.pending_devices_recycler_view);
        PendingDeviceViewModel pendingDeviceViewModel = new ViewModelProvider(this).get(PendingDeviceViewModel.class);
        ItemClickFunction clickFunction = id -> {
            ConnectivityManager manager = (ConnectivityManager) requireActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = manager.getActiveNetworkInfo();
            if (networkInfo != null && networkInfo.isConnected()) {
                ItemDetailFragment fragment = new ItemDetailFragment();
                Bundle bundle = new Bundle();
                bundle.putString("pending_device_id", id);
                fragment.setArguments(bundle);
                FragmentManager fragmentManager = Objects.requireNonNull(getActivity()).getSupportFragmentManager();

                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.setCustomAnimations(R.anim.fui_slide_in_right, R.anim.fui_slide_out_left);
                fragmentTransaction.add(R.id.activity_main, fragment);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
            } else {
                Toast.makeText(requireActivity(), "Your device is offline", Toast.LENGTH_SHORT).show();
            }
        };
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        pendingDeviceViewModel.getUserLiveData().observe(getViewLifecycleOwner(),userResource -> {
            pendingDeviceViewModel.setupPendingDeviceRepository();

            PendingDevicesAdapter pendingDevicesAdapter = new PendingDevicesAdapter(Objects.requireNonNull(pendingDeviceViewModel.getPendingDevicesLiveData().getValue()).getData(),clickFunction);
            recyclerView.setAdapter(pendingDevicesAdapter);

            pendingDeviceViewModel.getPendingDevicesLiveData().observe(getViewLifecycleOwner(),listResource -> {
                if (listResource.getRequest().getStatus() == Request.Status.SUCCESS) {
                    pendingDevicesAdapter.notifyDataSetChanged();
                } else if (listResource.getRequest().getStatus() == Request.Status.ERROR) {
                    Snackbar.make(requireView(),listResource.getRequest().getResourceString(),Snackbar.LENGTH_LONG);
                }
            });
        });

        return rootView;
    }
}
