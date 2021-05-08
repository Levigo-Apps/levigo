package org.getcarebase.carebase.activities.Main.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import org.getcarebase.carebase.R;

public class AddShipmentFragment extends Fragment {
    public static final String TAG = AddShipmentFragment.class.getSimpleName();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_add_shipment,container,false);
        TextView shipmentIdTextView = rootView.findViewById(R.id.shipment_id_text_view);

        String shipment_id = requireArguments().getString("shipment_id");
        shipmentIdTextView.setText(shipment_id);
        return rootView;
    }
}
