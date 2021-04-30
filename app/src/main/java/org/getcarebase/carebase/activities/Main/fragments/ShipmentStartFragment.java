package org.getcarebase.carebase.activities.Main.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.getcarebase.carebase.R;

public class ShipmentStartFragment extends Fragment {
    public static final String TAG = InventoryStartFragment.class.getName();
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View rootView = inflater.inflate(R.layout.generic_message_screen, container, false);

        ImageView image = rootView.findViewById(R.id.imageView);
        image.setImageDrawable(getResources().getDrawable(R.drawable.ic_baseline_inventory_24, getContext().getTheme()));
        image.setColorFilter(getResources().getColor(R.color.colorPrimary, getContext().getTheme()));

        TextView text = rootView.findViewById(R.id.messageText);
        text.setText("Start creating shipments!");

        TextView text2 = rootView.findViewById(R.id.messageText2);
        text2.setText("Click on a device in the inventory tab to get started");

        return rootView;
    }

}