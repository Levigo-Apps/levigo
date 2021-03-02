package org.getcarebase.carebase.activities.Main.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.getcarebase.carebase.R;

public abstract class FloatingActionButtonManagerFragment extends Fragment {
    private static final String TAG = FloatingActionButtonManagerFragment.class.getName();
    protected FloatingActionButton mainFAB;
    private Animation fadeInAnimation;
    private Animation fadeOutAnimation;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        fadeInAnimation = AnimationUtils.loadAnimation(getContext(),R.anim.fade_in);
        fadeOutAnimation = AnimationUtils.loadAnimation(getContext(),R.anim.fade_out);
//        setChild();
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onResume() {
        setChild();
        super.onResume();
    }

    private void setChild() {
        mainFAB = requireActivity().findViewById(R.id.toggle_options_fab);
        // set on click back to this fragment's on click
        mainFAB.setOnClickListener(this::onMainFloatingActionButtonClicked);
        // start fade in animation
        mainFAB.startAnimation(fadeInAnimation);
    }

    @Override
    public void onPause() {
        // start fade out animation
        mainFAB.startAnimation(fadeOutAnimation);
        super.onPause();
    }

    public abstract void onMainFloatingActionButtonClicked(final View view);
}
