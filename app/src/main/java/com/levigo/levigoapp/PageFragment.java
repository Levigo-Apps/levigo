package com.levigo.levigoapp;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

//import android.app.Fragment;

import androidx.fragment.app.Fragment;

import com.google.zxing.integration.android.IntentIntegrator;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;

public class PageFragment extends Fragment {
    public static final String ARG_PAGE = "ARG_PAGE";
    private static final String TAG = PageFragment.class.getName();

    private int mPage;

    DecoratedBarcodeView barcodeView;

    public static PageFragment newInstance(int page) {
        Bundle args = new Bundle();
        args.putInt(ARG_PAGE, page);
        PageFragment fragment = new PageFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPage = getArguments().getInt(ARG_PAGE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView;
        Log.d(TAG, "Page: " + mPage);
        switch (mPage){
//            case 1:
//                Log.d(TAG, "SCANNER");
//                rootView = inflater.inflate(R.layout.fragment_tabbed_scanning, container, false);
//                barcodeView = rootView.findViewById(R.id.barcode_scanner);
////                new IntentIntegrator(getActivity()).initiateScan();
////                IntentIntegrator.forFragment((Fragment) PageFragment.this).initiateScan();
////                IntentIntegrator scanIntegrator = new IntentIntegrator(TabbedScanning.this);
////                scanIntegrator.initiateScan();
//                break;
            case 2:
                Log.d(TAG, "MANUAL");
                rootView = inflater.inflate(R.layout.fragment_tabbed_manual, container, false);
                break;
            default:
                Log.d(TAG, "DEFAULT");
                rootView = inflater.inflate(R.layout.fragment_tabbed_manual, container, false);
                break;
        }
//        View view = inflater.inflate(R.layout.fragment_page, container, false);
//        TextView textView = (TextView) view;
//        textView.setText("Fragment #" + mPage);
//        return view;
//        View rootView = inflater.inflate(R.layout.fragment_tabbed_scanning, container, false);
//        barcodeView = rootView.findViewById(R.id.barcode_view);
        return rootView;
    }
}