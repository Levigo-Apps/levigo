//package com.levigo.levigoapp;
//
//import android.content.Intent;
//import android.os.Bundle;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.Button;
//import android.widget.Toast;
//
//import androidx.fragment.app.Fragment;
//
//import com.google.zxing.integration.android.IntentIntegrator;
//import com.google.zxing.integration.android.IntentResult;
//
//public class ScanFragment extends Fragment {
//    private String toast;
//
//    public ScanFragment() {
//    }
//
//    @Override
//    public void onActivityCreated(Bundle savedInstanceState) {
//        super.onActivityCreated(savedInstanceState);
//
//        displayToast();
//    }
//
//    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container,
//                             Bundle savedInstanceState) {
//        View view = inflater.inflate(R.layout.fragment_scan, container, false);
//        Button scan = view.findViewById(R.id.scan_from_fragment);
//        scan.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                ScanFragment.this.scanFromFragment();
//            }
//        });
//        return view;
//    }
//
//    public void scanFromFragment() {
//        IntentIntegrator.forSupportFragment(this).initiateScan();
//    }
//
//    private void displayToast() {
//        if(getActivity() != null && toast != null) {
//            Toast.makeText(getActivity(), toast, Toast.LENGTH_LONG).show();
//            toast = null;
//        }
//    }
//
//    @Override
//    public void onActivityResult(int requestCode, int resultCode, Intent data) {
//        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
//        if(result != null) {
//            if(result.getContents() == null) {
//                toast = "Cancelled from fragment";
//            } else {
//                toast = "Scanned from fragment: " + result.getContents();
//            }
//
//            // At this point we may or may not have a reference to the activity
//            displayToast();
//        }
//    }
//}
