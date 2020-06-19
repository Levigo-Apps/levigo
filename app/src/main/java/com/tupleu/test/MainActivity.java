package com.tupleu.test;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresPermission;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final int RC_HANDLE_CAMERA_PERM = 1;

    private CameraSource mCameraSource;
    private SurfaceView mCameraView;
    private TextView mTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mCameraView = findViewById(R.id.cameraView);
        mTextView = findViewById(R.id.textView);


        mCameraView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                getPermissions();
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                if(mCameraSource != null) {
                    mCameraSource.stop();
                }
            }
        });

        getPermissions();

    }

    @RequiresPermission(Manifest.permission.CAMERA)
    private void initCamera() throws IOException, SecurityException {
        Context context = getApplicationContext();

        BarcodeDetector barcodeDetector = new BarcodeDetector.Builder(context).build();
        barcodeDetector.setProcessor(new Detector.Processor<Barcode>() {
            @Override
            public void release() {

            }

            @Override
            public void receiveDetections(Detector.Detections<Barcode> detections) {
                final SparseArray<Barcode> barcodes = detections.getDetectedItems();
                if(barcodes.size() > 0) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mTextView.setText(barcodes.valueAt(0).rawValue);
                        }
                    });
                }
            }
        });
        if(!barcodeDetector.isOperational()) {
            Log.e(TAG, "Barcode detector is not operational");
            return;
        }

        DisplayMetrics displayMetrics = new DisplayMetrics();
                getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        CameraSource.Builder builder = new CameraSource.Builder(context, barcodeDetector)
                .setFacing(CameraSource.CAMERA_FACING_BACK)
                .setRequestedPreviewSize(displayMetrics.heightPixels,displayMetrics.widthPixels)
                .setRequestedFps(15.0f)
                .setAutoFocusEnabled(true);

        mCameraSource = builder.build();
        mCameraSource.start(mCameraView.getHolder());
    }



    private void getPermissions() {
        final String[] permissions = new String[]{Manifest.permission.CAMERA};
        if (checkSelfPermission(permissions[0]) != PackageManager.PERMISSION_GRANTED){
            requestPermissions(permissions, RC_HANDLE_CAMERA_PERM);
        }
        else {
            try {
                initCamera();
            }
            catch (SecurityException se) {
                Log.e(TAG, "Camera permission not granted", se);
                Toast.makeText(getApplicationContext(), "Camera permission not granted", Toast.LENGTH_SHORT).show();
            }
            catch (IOException ie) {
                Log.e(TAG, "Error in starting camera", ie);
                Toast.makeText(getApplicationContext(), "Error in starting camera", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        getPermissions();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(mCameraSource != null) {
            mCameraSource.stop();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mCameraSource != null) {
            mCameraSource.release();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode != RC_HANDLE_CAMERA_PERM) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
        else if(grantResults.length != 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            try {
                initCamera();
            }
            catch (SecurityException se) {
                Log.e(TAG, "Camera permission not granted", se);
            }
            catch (IOException ie) {
                Log.e(TAG, "Error in starting camera", ie);
            }
        }
        else {
            finish();
        }

    }
}
