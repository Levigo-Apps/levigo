package org.getcarebase.carebase.views;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;

import com.google.mlkit.md.barcodedetection.BarcodeField;
import com.google.mlkit.md.barcodedetection.BarcodeResultFragment;

import org.getcarebase.carebase.R;
import org.getcarebase.carebase.activities.Main.ScanningActivity;

import java.util.ArrayList;

public class CarebaseBarcodeResultFragment extends BarcodeResultFragment {

    private static final String TAG = "CarebaseBarcodeResultFragment";
    private static final String ARG_BARCODE_FIELD_LIST = "arg_barcode_field_list";

    private ScanningActivity.ContinueCallback continueCallback;

    @NonNull
    @Override
    public View onCreateView(@NonNull LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
      View view = super.onCreateView(layoutInflater, viewGroup, bundle);
      View continueButton = view.findViewById(com.google.mlkit.md.R.id.continue_button);
      continueButton.setOnClickListener(v -> continueCallback.onContinueClicked());
      return view;
    }

    public void setContinueCallback(ScanningActivity.ContinueCallback continueCallback) {
        this.continueCallback = continueCallback;
    }

    public static void show(FragmentManager fragmentManager, ArrayList<BarcodeField> barcodeFields,
                            ScanningActivity.ContinueCallback continueCallback) {
        CarebaseBarcodeResultFragment fragment = new CarebaseBarcodeResultFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList(ARG_BARCODE_FIELD_LIST,barcodeFields);
        fragment.setArguments(bundle);
        fragment.setContinueCallback(continueCallback);
        fragment.show(fragmentManager, TAG);
    }

    public static void dismiss(FragmentManager fragmentManager) {
        CarebaseBarcodeResultFragment fragment = ((CarebaseBarcodeResultFragment) fragmentManager.findFragmentByTag(TAG));
        if (fragment != null) {
            fragment.dismiss();
        }
    }
}
