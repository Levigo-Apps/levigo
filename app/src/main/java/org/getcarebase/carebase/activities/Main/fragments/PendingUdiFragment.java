package org.getcarebase.carebase.activities.Main.fragments;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.getcarebase.carebase.R;
import org.getcarebase.carebase.models.PendingDevice;
import org.getcarebase.carebase.utils.Request;
import org.getcarebase.carebase.viewmodels.PendingDeviceViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class PendingUdiFragment extends Fragment {
    public static final String TAG = PendingUdiFragment.class.getSimpleName();
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final CollectionReference usersRef = db.collection("users");
    private View rootView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,ViewGroup container,Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_pendingudis, container, false);
        MaterialToolbar topToolBar = rootView.findViewById(R.id.topAppBar);
        topToolBar.setNavigationOnClickListener(view -> requireActivity().onBackPressed());

        PendingDeviceViewModel pendingDeviceViewModel = new ViewModelProvider(this).get(PendingDeviceViewModel.class);
        pendingDeviceViewModel.getUserLiveData().observe(getViewLifecycleOwner(),userResource -> {
            pendingDeviceViewModel.setupPendingDeviceRepository();
            pendingDeviceViewModel.loadPendingDevices();
        });

        pendingDeviceViewModel.getPendingDevicesLiveData().observe(getViewLifecycleOwner(),listResource -> {
            if (listResource.getRequest().getStatus() == Request.Status.SUCCESS) {
                addUdis(listResource.getData());
            } else if (listResource.getRequest().getStatus() == Request.Status.ERROR) {
                Snackbar.make(requireView(),listResource.getRequest().getResourceString(),Snackbar.LENGTH_LONG);
            }
        });

        return rootView;
    }

    private void addUdis(final List<PendingDevice> list) {
        LinearLayout linearLayout = rootView.findViewById(R.id.pendingudi_linearlayout);
        for(int i = 0; i < list.size(); i++){
            View view = View.inflate(rootView.getContext(),R.layout.pending_udis,null);
            TextView udi = view.findViewById(R.id.pending_udi);
            udi.setText(list.get(i).getUniqueDeviceIdentifier());
            linearLayout.addView(view);
            ImageView resaveIcon = view.findViewById(R.id.resave_icon);
            final int finalI = i;
            resaveIcon.setOnClickListener(icon -> {
                ConnectivityManager manager =
                        (ConnectivityManager) Objects.requireNonNull(getActivity()).getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo networkInfo = manager.getActiveNetworkInfo();
                if (networkInfo != null && networkInfo.isConnected()) {
                    // Network is present and connected
                    openEditView(list.get(finalI).getId());
                } else {
                    Toast.makeText(requireActivity(), "Your device is offline", Toast.LENGTH_SHORT).show();
                }
            });

        }
    }

    private void openEditView(String id){
        ItemDetailFragment fragment = new ItemDetailFragment();
        Bundle bundle = new Bundle();
        bundle.putString("pending_device_id", id);
        fragment.setArguments(bundle);
        FragmentManager fragmentManager = Objects.requireNonNull(getActivity()).getSupportFragmentManager();

        //clears other fragments
      //  fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.setCustomAnimations(R.anim.fui_slide_in_right, R.anim.fui_slide_out_left);
        fragmentTransaction.add(R.id.activity_main, fragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }
}
