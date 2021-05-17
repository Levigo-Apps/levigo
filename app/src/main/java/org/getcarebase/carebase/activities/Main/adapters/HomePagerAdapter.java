package org.getcarebase.carebase.activities.Main.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import org.getcarebase.carebase.activities.Main.fragments.InventoryFragment;
import org.getcarebase.carebase.activities.Main.fragments.ProceduresFragment;
import org.getcarebase.carebase.activities.Main.fragments.ShipmentFragment;
import org.getcarebase.carebase.models.TabType;

import java.util.List;

public class HomePagerAdapter extends FragmentStateAdapter {

    private final List<TabType> tabTypeList;

    public HomePagerAdapter(@NonNull FragmentActivity fragmentActivity, List<TabType> tabTypeList) {
        super(fragmentActivity);
        this.tabTypeList = tabTypeList;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        TabType type = tabTypeList.get(position);
        switch (type) {
            case INVENTORY: return new InventoryFragment();
            case PROCEDURES: return new ProceduresFragment();
            case SHIPMENTS: return new ShipmentFragment();
            default: return new Fragment();
        }
    }

    @Override
    public int getItemCount() {
        return tabTypeList.size();
    }
}
