package org.getcarebase.carebase.activities.Main.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.getcarebase.carebase.R;


/**
 * Manages Floating Action Button of the home screen
 * Any fragment of the home must extend this fragment if fragment has floating action button actions
 */
public class MiniFloatingActionButtonManagerFragment extends FloatingActionButtonManagerFragment {
    private ViewGroup container;
    protected View[] miniFABs;
    private Animation rotateOpenAnimation;
    private Animation rotateCloseAnimation;
    private Animation fadeInUpAnimation;
    private Animation fadeOutDownAnimation;
    private boolean isOptionsShown;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, @Nullable Bundle savedInstanceState) {
        this.container = container;
        rotateOpenAnimation = AnimationUtils.loadAnimation(getContext(),R.anim.rotate_open);
        rotateCloseAnimation = AnimationUtils.loadAnimation(getContext(),R.anim.rotate_close);
        fadeInUpAnimation = AnimationUtils.loadAnimation(getContext(),R.anim.fade_in_up);
        fadeOutDownAnimation = AnimationUtils.loadAnimation(getContext(),R.anim.fade_out_down);
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onResume() {
        // add mini fab buttons
        for (View fab : miniFABs) {
            container.addView(fab,0);
        }
        super.onResume();
    }

    @Override
    public void onPause() {
        // remove mini fab buttons
        for (View fab : miniFABs) {
            container.removeView(fab);
        }
        super.onPause();
    }


    public void onMainFloatingActionButtonClicked(final View view) {
        setFABVisibilities(isOptionsShown);
        setFABAnimations(isOptionsShown);
        isOptionsShown = !isOptionsShown;
        FloatingActionButton tmp = (FloatingActionButton) view.findViewById(R.id.toggle_options_fab);
        if (isOptionsShown) {
            tmp.setBackgroundTintList(ContextCompat.getColorStateList(getActivity(),R.color.colorPrimaryDark));
        } else {
            tmp.setBackgroundTintList(ContextCompat.getColorStateList(getActivity(),R.color.colorPrimary));
        }

    }

    public void setFABVisibilities(final boolean isOptionsShown) {
        if (isOptionsShown) {
            for (View fab : miniFABs) {
                fab.setVisibility(View.INVISIBLE);
            }
        } else {
            for (View fab : miniFABs) {
                fab.setVisibility(View.VISIBLE);
            }
        }
    }

    public void setFABAnimations(final boolean isOptionsShown) {
        if (isOptionsShown) {
            mainFAB.startAnimation(rotateCloseAnimation);
            for (View fab : miniFABs) {
                fab.startAnimation(fadeOutDownAnimation);
            }
        } else {
            mainFAB.startAnimation(rotateOpenAnimation);
            for (View fab : miniFABs) {
                fab.startAnimation(fadeInUpAnimation);
            }
        }
    }
}
