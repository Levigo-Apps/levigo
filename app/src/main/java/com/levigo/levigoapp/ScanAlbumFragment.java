//package com.levigo.levigoapp;
//
//import android.os.Bundle;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//
//import androidx.fragment.app.Fragment;
//
//import com.journeyapps.barcodescanner.DecoratedBarcodeView;
//
//
//public class ScanAlbumFragment extends Fragment {
//    private static final String TAG = ScanAlbumFragment.class.getName();
//
//
//    DecoratedBarcodeView barcodeView;
//
//
//    public static ScanAlbumFragment newInstance() {
//        Bundle args = new Bundle();
//
//        ScanAlbumFragment fragment = new ScanAlbumFragment();
//        fragment.setArguments(args);
//        return fragment;
//    }
//
//    @Override
//    public void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//
//    }
//
//    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container,
//                             Bundle savedInstanceState) {
//        View rootView = inflater.inflate(R.layout.fragment_album, container, false);
//
//        return rootView;
//    }
//}