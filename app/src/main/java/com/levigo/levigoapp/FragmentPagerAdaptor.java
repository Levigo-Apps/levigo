//package com.levigo.levigoapp;
//
//
//import android.content.Context;
//
//import androidx.fragment.app.Fragment;
//import androidx.fragment.app.FragmentManager;
//import androidx.fragment.app.FragmentPagerAdapter;
//
//class ScanningFragmentPagerAdapter extends FragmentPagerAdapter {
//    final int PAGE_COUNT = 3;
//    private String tabTitles[] = new String[]{"Camera", "Album", "Manual"};
//    private Context context;
//
//    public ScanningFragmentPagerAdapter(FragmentManager fm, Context context) {
//        super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
//        this.context = context;
//    }
//
//    @Override
//    public int getCount() {
//        return PAGE_COUNT;
//    }
//
//    @Override
//    public Fragment getItem(int position) {
//        if (position == 0) {
////            return ScanCameraFragment.newInstance();
////            return new ScanFragment();
//            return new ScanCameraFragment();
//        } else if (position == 1) {
//            return ScanAlbumFragment.newInstance();
//        } else {
//            return ScanManualFragment.newInstance();
//        }
//    }
//
//    @Override
//    public CharSequence getPageTitle(int position) {
//        // Generate title based on item position
//        return tabTitles[position];
//    }
//}