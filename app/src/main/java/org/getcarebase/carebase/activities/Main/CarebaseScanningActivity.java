package org.getcarebase.carebase.activities.Main;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;

import com.carebase.carebasescanner.BarcodeAnalyzer;
import com.carebase.carebasescanner.ScanningActivity;
import com.carebase.carebasescanner.ScanningViewModel;

import org.getcarebase.carebase.activities.Main.fragments.AutoPopulatedBarcodeResultFragment;

public class CarebaseScanningActivity extends ScanningActivity {
    private static final String TAG = CarebaseScanningActivity.class.getSimpleName();
    public static final String ARG_UDI_RESULT = "arg_udi_result";
    private int deviceResultCode;
    private int shipmentResultCode;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.deviceResultCode = getIntent().getIntExtra("device_result_code",0);
        this.shipmentResultCode = getIntent().getIntExtra("shipment_result_code",-1);

    }

    // TODO build custom bottom sheet
    @Override
    public void showBottomSheet(String udi, String type) {
        AutoPopulatedBarcodeResultFragment.show(getSupportFragmentManager(),udi,type,this::restartUseCases,this::onContinueClicked);
        scanningViewModel.clearUseCases();
    }

    public void onContinueClicked(String udi, String type) {
        // cleans up state
        AutoPopulatedBarcodeResultFragment.dismiss(getSupportFragmentManager());

        ScanningViewModel.BarcodeType barcodeType = ScanningViewModel.BarcodeType.valueOf(type);
        // returns scanned result
        Intent data = new Intent();
        data.putExtra(CarebaseScanningActivity.ARG_UDI_RESULT,udi);
        if (barcodeType == ScanningViewModel.BarcodeType.DEVICE) {
            setResult(deviceResultCode,data);
        } else if (barcodeType == ScanningViewModel.BarcodeType.SHIPMENT) {
            setResult(shipmentResultCode,data);
        }

        finish();
    }
}
