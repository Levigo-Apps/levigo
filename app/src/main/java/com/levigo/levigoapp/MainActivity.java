package com.levigo.levigoapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.journeyapps.barcodescanner.CaptureActivity;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final int RC_HANDLE_CAMERA_PERM = 1;

    private TextView mTextView;
    private Button mButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mButton = findViewById(R.id.button);
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startScanner();
            }
        });
        mTextView = findViewById(R.id.textView);

        getPermissions();
    }

    private void startScanner() {
        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.setCaptureActivity(CaptureActivity.class);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES);

        integrator.initiateScan();
    }



    private void getPermissions() {
        final String[] permissions = new String[]{Manifest.permission.CAMERA};
        if (checkSelfPermission(permissions[0]) != PackageManager.PERMISSION_GRANTED){
            requestPermissions(permissions, RC_HANDLE_CAMERA_PERM);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode,resultCode,data);
        if(result != null){
            String contents = result.getContents();
            if(contents != null) {
                Toast.makeText(this, "Success", Toast.LENGTH_LONG).show();
                mTextView.setText(contents);
            }
        }
        else{
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode != RC_HANDLE_CAMERA_PERM) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
        else if(!(grantResults.length != 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
            finish();
        }

    }
}
