package org.getcarebase.carebase.activities.Main.adapters;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import org.getcarebase.carebase.activities.Main.fragments.InventoryFragment;
import org.getcarebase.carebase.activities.Main.fragments.ProceduresFragment;

public class HomePagerAdapter extends FragmentStateAdapter {

    public HomePagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0: return new InventoryFragment();
            // TODO change to ProcedureFragment
            case 1: return new ProceduresFragment();
            default: return new Fragment();
        }
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}
