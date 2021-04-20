package org.getcarebase.carebase.activities.Main;
;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;

import com.carebase.carebasescanner.ScanningActivity;

import org.getcarebase.carebase.activities.Main.fragments.AutoPopulatedBarcodeResultFragment;

public class CarebaseScanningActivity extends ScanningActivity {
    private static final String TAG = CarebaseScanningActivity.class.getSimpleName();
    public static final String ARG_UDI_RESULT = "arg_udi_result";
    private int resultCode;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.resultCode = getIntent().getIntExtra("result_code",0);

    }

    // TODO build custom bottom sheet
    @Override
    public void showBottomSheet(String udi) {
        AutoPopulatedBarcodeResultFragment.show(getSupportFragmentManager(),udi,this::restartUseCases,this::onContinueClicked);
        scanningViewModel.clearUseCases();
    }

    public void onContinueClicked(String udi) {
        // cleans up state
        AutoPopulatedBarcodeResultFragment.dismiss(getSupportFragmentManager());
        // returns scanned result
        Intent data = new Intent();
        data.putExtra(CarebaseScanningActivity.ARG_UDI_RESULT,udi);
        setResult(resultCode,data);
        finish();
    }
}
