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
import androidx.fragment.app.Fragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.getcarebase.carebase.R;

/**
 * Manages Floating Action Button of the home screen
 * Any fragment of the home must extend this fragment if fragment has floating action button actions
 */
public class MiniFloatingActionButtonManagerFragment extends FloatingActionButtonManagerFragment {
    private ViewGroup container;
    protected FloatingActionButton[] miniFABs;
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
        for (FloatingActionButton fab : miniFABs) {
            container.addView(fab,0);
        }
        super.onResume();
    }

    @Override
    public void onPause() {
        // remove mini fab buttons
        for (FloatingActionButton fab : miniFABs) {
            container.removeView(fab);
        }
        super.onPause();
    }


    public void onMainFloatingActionButtonClicked(final View view) {
        setFABVisibilities(isOptionsShown);
        setFABAnimations(isOptionsShown);
        isOptionsShown = !isOptionsShown;
    }

    public void setFABVisibilities(final boolean isOptionsShown) {
        if (isOptionsShown) {
            for (FloatingActionButton fab : miniFABs) {
                fab.setVisibility(View.INVISIBLE);
            }
        } else {
            for (FloatingActionButton fab : miniFABs) {
                fab.setVisibility(View.VISIBLE);
            }
        }
    }

    public void setFABAnimations(final boolean isOptionsShown) {
        if (isOptionsShown) {
            mainFAB.startAnimation(rotateCloseAnimation);
            for (FloatingActionButton fab : miniFABs) {
                fab.startAnimation(fadeOutDownAnimation);
            }
        } else {
            mainFAB.startAnimation(rotateOpenAnimation);
            for (FloatingActionButton fab : miniFABs) {
                fab.startAnimation(fadeInUpAnimation);
            }
        }
    }
}
