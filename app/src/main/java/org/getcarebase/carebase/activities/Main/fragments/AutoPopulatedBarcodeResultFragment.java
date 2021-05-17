package org.getcarebase.carebase.activities.Main.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;

import com.carebase.carebasescanner.BarcodeAnalyzer;
import com.carebase.carebasescanner.BarcodeResultFragment;
import com.carebase.carebasescanner.OnDismissCallback;
import com.carebase.carebasescanner.ScanningViewModel;
import com.google.android.material.button.MaterialButton;

import org.getcarebase.carebase.R;
import org.w3c.dom.Text;

import java.util.Objects;

public class AutoPopulatedBarcodeResultFragment extends BarcodeResultFragment {

    private final OnContinueCallback onContinueCallback;
    private String udi;
    private String type;

    public interface OnContinueCallback {
        void onContinue(String udi, String type);
    }

    public AutoPopulatedBarcodeResultFragment(OnDismissCallback onDismissCallback, OnContinueCallback onContinueCallback) {
        super(onDismissCallback);
        this.onContinueCallback = onContinueCallback;
    }

    @NonNull
    @Override
    public View onCreateView(@NonNull LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        View view = layoutInflater.inflate(R.layout.auto_populate_bottom_sheet,viewGroup,false);
        LinearLayout contentLayout = view.findViewById(R.id.content);
        TextView udiTextView = view.findViewById(R.id.udi_field_value);
        TextView typeTextView = view.findViewById(R.id.type_field_value);
        MaterialButton continueButton = view.findViewById(R.id.button_continue);
        ProgressBar progressBar = view.findViewById(R.id.progress_indicator);
        continueButton.setOnClickListener((v) -> onContinueCallback.onContinue(udi,type));

        ScanningViewModel scanningViewModel = new ViewModelProvider(requireActivity()).get(ScanningViewModel.class);
        Bundle arguments = getArguments();
        udi = Objects.requireNonNull(arguments).getString(ARG_UDI_FIELD);
        type = Objects.requireNonNull(arguments).getString(ARG_TYPE_FIELD);
        udiTextView.setText(udi);
        typeTextView.setText(type);

        contentLayout.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.GONE);

        scanningViewModel.setState(ScanningViewModel.ScanningState.DETECTED);
        return view;
    }

    private final static String TAG = "BarcodeResultFragment";
    private final static String ARG_UDI_FIELD = "arg_udi_field";
    private final static String ARG_TYPE_FIELD = "arg_type_field";

    public static void show(FragmentManager fragmentManager, String udi, String type, OnDismissCallback onDismissCallback, OnContinueCallback onContinueCallback) {
        AutoPopulatedBarcodeResultFragment barcodeResultFragment = new AutoPopulatedBarcodeResultFragment(onDismissCallback, onContinueCallback);
        Bundle bundle = new Bundle();
        bundle.putString(ARG_UDI_FIELD, udi);
        bundle.putString(ARG_TYPE_FIELD, type);
        barcodeResultFragment.setArguments(bundle);
        barcodeResultFragment.show(fragmentManager, TAG);
    }

    public static void dismiss(FragmentManager fragmentManager) {
        BarcodeResultFragment fragment = ((BarcodeResultFragment) fragmentManager.findFragmentByTag(TAG));
        if (fragment != null) {
            fragment.dismiss();
        }
    }

}
