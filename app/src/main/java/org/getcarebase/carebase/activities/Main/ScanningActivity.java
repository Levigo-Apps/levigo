package org.getcarebase.carebase.activities.Main;

import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.gms.common.internal.Objects;
import com.google.android.material.chip.Chip;
import com.google.android.material.snackbar.Snackbar;
import com.google.mlkit.md.barcodedetection.BarcodeField;
import com.google.mlkit.md.barcodedetection.BarcodeProcessor;
import com.google.mlkit.md.barcodedetection.BarcodeResultFragment;
import com.google.mlkit.md.camera.CameraSource;
import com.google.mlkit.md.camera.CameraSourcePreview;
import com.google.mlkit.md.camera.GraphicOverlay;
import com.google.mlkit.md.camera.WorkflowModel;
import com.google.mlkit.vision.barcode.Barcode;

import org.getcarebase.carebase.R;
import org.getcarebase.carebase.models.DeviceModel;
import org.getcarebase.carebase.models.DeviceProduction;
import org.getcarebase.carebase.utils.Request;
import org.getcarebase.carebase.viewmodels.ScanningViewModel;
import org.getcarebase.carebase.views.CarebaseBarcodeResultFragment;

import java.io.IOException;
import java.util.ArrayList;

public class ScanningActivity extends AppCompatActivity {

    private final static String TAG = ScanningActivity.class.getName();

    private CameraSource cameraSource;
    private CameraSourcePreview preview;
    private GraphicOverlay graphicOverlay;
    private Chip promptChip;
    private AnimatorSet promptChipAnimator;

    private ScanningViewModel scanningViewModel;

    public interface ContinueCallback {
        void onContinueClicked();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(com.google.mlkit.md.R.layout.activity_live_barcode);
        preview = findViewById(com.google.mlkit.md.R.id.camera_preview);
        graphicOverlay = findViewById(com.google.mlkit.md.R.id.camera_preview_graphic_overlay);
        cameraSource = new CameraSource(graphicOverlay);
        graphicOverlay.setCameraInfo(cameraSource);

        promptChip = findViewById(com.google.mlkit.md.R.id.bottom_prompt_chip);
        promptChipAnimator = (AnimatorSet) AnimatorInflater.loadAnimator(this, com.google.mlkit.md.R.animator.bottom_prompt_chip_enter);
        promptChipAnimator.setTarget(promptChip);

        View closeButton = findViewById(com.google.mlkit.md.R.id.close_button);
        closeButton.setOnClickListener(view -> onBackPressed());

        scanningViewModel = new ViewModelProvider(this).get(ScanningViewModel.class);
        scanningViewModel.getUserLiveData().observe(this, userResource -> {
            if (userResource.getRequest().getStatus() == Request.Status.SUCCESS) {
                scanningViewModel.setDeviceRepository(userResource.getData().getNetworkId(),userResource.getData().getHospitalId());
            } else if (userResource.getRequest().getStatus() == Request.Status.ERROR){
                Snackbar.make(preview.getRootView(), R.string.error_something_wrong, Snackbar.LENGTH_LONG).show();
            }
        });

        setUpWorkflowModel();
    }

    @Override
    protected void onResume() {
        super.onResume();
        WorkflowModel workflowModel = scanningViewModel.getWorkflowModel();
        if (workflowModel != null) {
            workflowModel.markCameraFrozen();
        }
        scanningViewModel.setCurrentWorkflowState(WorkflowModel.WorkflowState.NOT_STARTED);
        if (cameraSource != null) {
            cameraSource.setFrameProcessor(new BarcodeProcessor(graphicOverlay,workflowModel));
        }
        if (workflowModel != null) {
            workflowModel.setWorkflowState(WorkflowModel.WorkflowState.DETECTING);
        }
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        CarebaseBarcodeResultFragment.dismiss(getSupportFragmentManager());
    }

    @Override
    protected void onPause() {
        super.onPause();
        scanningViewModel.setCurrentWorkflowState(WorkflowModel.WorkflowState.NOT_STARTED);
        stopCameraPreview();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (cameraSource != null) {
            cameraSource.release();
            cameraSource = null;
        }

    }

    private void startCameraPreview() {
        WorkflowModel workflowModel = scanningViewModel.getWorkflowModel();
        if (workflowModel != null && !workflowModel.isCameraLive() && cameraSource != null) {
            try {
                workflowModel.markCameraLive();
                if (preview != null) {
                    preview.start(cameraSource);
                }
            } catch (IOException e) {
                Log.e(TAG, "Failed to start camera preview!", e);
                cameraSource.release();
                this.cameraSource = null;
            }
        }
    }

    private void stopCameraPreview() {
        WorkflowModel workflowModel = scanningViewModel.getWorkflowModel();
        if (workflowModel != null && workflowModel.isCameraLive()) {
            workflowModel.markCameraFrozen();
            if (preview != null) {
                preview.stop();
            }
        }
    }

    private void setUpWorkflowModel() {
        WorkflowModel workflowModel = new ViewModelProvider(this).get(WorkflowModel.class);
        scanningViewModel.setWorkflowModel(workflowModel);

        // Observes the workflow state changes, if happens, update the overlay view indicators and
        // camera preview state.
        workflowModel.getWorkflowState().observe(this,  workflowState -> {
            if (workflowState == null || Objects.equal(scanningViewModel.getCurrentWorkflowState(), workflowState)) {
                return;
            }

            scanningViewModel.setCurrentWorkflowState(workflowState);
            Log.d(TAG, "Current workflow state: " + scanningViewModel.getCurrentWorkflowState().name());


            if (promptChip != null) {
                boolean wasPromptChipGone = promptChip.getVisibility() == View.GONE;

                switch (workflowState) {
                    case DETECTING:
                        promptChip.setVisibility(View.VISIBLE);
                        promptChip.setText(com.google.mlkit.md.R.string.prompt_point_at_a_barcode);
                        startCameraPreview();
                        break;
                    case CONFIRMING:
                        promptChip.setVisibility(View.VISIBLE);
                        promptChip.setText(com.google.mlkit.md.R.string.prompt_move_camera_closer);
                        startCameraPreview();
                        break;
                    case SEARCHING:
                        promptChip.setVisibility(View.VISIBLE);
                        promptChip.setText(com.google.mlkit.md.R.string.prompt_searching);
                        stopCameraPreview();
                        break;
                    case DETECTED:
                    case SEARCHED:
                        promptChip.setVisibility(View.GONE);
                        stopCameraPreview();
                        break;
                    default:
                        promptChip.setVisibility(View.GONE);
                }

                if (promptChipAnimator != null) {
                    boolean shouldPlayPromptChipEnteringAnimation = wasPromptChipGone && promptChip.getVisibility() == View.VISIBLE;
                    if (shouldPlayPromptChipEnteringAnimation && !promptChipAnimator.isRunning()) {
                        promptChipAnimator.start();
                    }
                }
            }
        });

        scanningViewModel.getAutoPopulatedLiveData().observe(this,deviceModelResource -> {
            if (deviceModelResource.getRequest().getStatus() == Request.Status.SUCCESS) {
                ArrayList<BarcodeField> barcodeFieldList = new ArrayList<>();

                DeviceModel deviceModel = deviceModelResource.getData();
                DeviceProduction deviceProduction = null;
                if (deviceModel.getProductions().size() > 0) {
                    deviceProduction = deviceModel.getProductions().get(0);
                }
                barcodeFieldList.add(new BarcodeField("Name", deviceModel.getName()));
                if (deviceProduction != null) {
                    barcodeFieldList.add(new BarcodeField("Unique Device Identifier",deviceProduction.getUniqueDeviceIdentifier()));
                }
                barcodeFieldList.add(new BarcodeField("Device Identifier", deviceModel.getDeviceIdentifier()));
                if (deviceModel.getDescription() != null) {
                    barcodeFieldList.add(new BarcodeField("Description", deviceModel.getDescription()));
                }

                ContinueCallback callback = this::onContinueButtonClicked;
                CarebaseBarcodeResultFragment.show(getSupportFragmentManager(),barcodeFieldList,callback);
            } else if (deviceModelResource.getRequest().getStatus() == Request.Status.ERROR) {
                Snackbar.make(preview.getRootView(),R.string.error_something_wrong,Snackbar.LENGTH_LONG).show();
            }
        });
    }

    public void onContinueButtonClicked() {
        Log.d(TAG,"Clicked");
    }
}
