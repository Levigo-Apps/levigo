package org.getcarebase.carebase.activities.Main.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.getcarebase.carebase.R;

public class ErrorFragment extends Fragment {
    public static final String TAG = ErrorFragment.class.getName();
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View rootView = inflater.inflate(R.layout.generic_message_screen, container, false);

        ImageView image = rootView.findViewById(R.id.imageView);
        image.setImageDrawable(getResources().getDrawable(R.drawable.ic_baseline_error_outline_24, getContext().getTheme()));
        image.setColorFilter(getResources().getColor(R.color.colorGrey, getContext().getTheme()));

        TextView text = rootView.findViewById(R.id.messageText);
        text.setText("Uh-oh page not found");

        TextView text2 = rootView.findViewById(R.id.messageText2);
        text2.setText("Try reloading the app");

        return rootView;
    }
}