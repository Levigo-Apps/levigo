package org.getcarebase.carebase.activities.Main.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.material.appbar.MaterialToolbar;

import org.getcarebase.carebase.R;

import androidx.fragment.app.Fragment;

public class ShipDeviceFragment extends Fragment {
    private Activity parent;
    TextView deviceName;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        parent = getActivity();
        final View rootView = inflater.inflate(R.layout.ship_device, container, false);
        MaterialToolbar topToolBar = rootView.findViewById(R.id.topAppBar);

        topToolBar.setNavigationOnClickListener(view -> {
            if (parent != null)
                parent.onBackPressed();
        });

        return rootView;
    }
}