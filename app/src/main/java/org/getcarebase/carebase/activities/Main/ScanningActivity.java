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
import com.google.mlkit.md.barcodedetection.BarcodeField;
import com.google.mlkit.md.barcodedetection.BarcodeProcessor;
import com.google.mlkit.md.barcodedetection.BarcodeResultFragment;
import com.google.mlkit.md.camera.CameraSource;
import com.google.mlkit.md.camera.CameraSourcePreview;
import com.google.mlkit.md.camera.GraphicOverlay;
import com.google.mlkit.md.camera.WorkflowModel;

import java.io.IOException;
import java.util.ArrayList;

public class ScanningActivity extends AppCompatActivity {

    private final static String TAG = ScanningActivity.class.getName();

    private CameraSource cameraSource;
    private CameraSourcePreview preview;
    private GraphicOverlay graphicOverlay;
    private Chip promptChip;
    private AnimatorSet promptChipAnimator;
    private WorkflowModel workflowModel;
    private WorkflowModel.WorkflowState currentWorkflowState;

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

        setUpWorkflowModel();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (workflowModel != null) {
            workflowModel.markCameraFrozen();
        }
        currentWorkflowState = WorkflowModel.WorkflowState.NOT_STARTED;
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
        BarcodeResultFragment.Companion.dismiss(getSupportFragmentManager());
    }

    @Override
    protected void onPause() {
        super.onPause();
        currentWorkflowState = WorkflowModel.WorkflowState.NOT_STARTED;
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
        if (workflowModel != null && workflowModel.isCameraLive()) {
            workflowModel.markCameraFrozen();
            if (preview != null) {
                preview.stop();
            }
        }
    }

    private void setUpWorkflowModel() {
        workflowModel = new ViewModelProvider(this).get(WorkflowModel.class);

        // Observes the workflow state changes, if happens, update the overlay view indicators and
        // camera preview state.
        workflowModel.getWorkflowState().observe(this,  workflowState -> {
            if (workflowState == null || Objects.equal(currentWorkflowState, workflowState)) {
                return;
            }

            currentWorkflowState = workflowState;
            Log.d(TAG, "Current workflow state: " + currentWorkflowState.name());


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

        if (workflowModel != null && workflowModel.getDetectedBarcode() != null) {
            workflowModel.getDetectedBarcode().observe(this, barcode -> {
                if (barcode != null) {
                    ArrayList<BarcodeField> barcodeFieldList = new ArrayList<>();
                    if (barcode.getRawValue() != null) {
                        barcodeFieldList.add(new BarcodeField("Raw Value", barcode.getRawValue()));
                    }
                    if (barcode.getDisplayValue() != null) {
                        barcodeFieldList.add(new BarcodeField("Display Value", barcode.getDisplayValue()));
                    }
                    BarcodeResultFragment.Companion.show(getSupportFragmentManager(),barcodeFieldList);
                }
            });
        }

    }
}
