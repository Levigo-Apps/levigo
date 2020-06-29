package com.levigo.levigoapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.journeyapps.barcodescanner.CaptureActivity;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final int RC_HANDLE_CAMERA_PERM = 1;

    private TextView mTextView;
    private ImageView mImageView;
    private FloatingActionButton mAdd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAdd = findViewById(R.id.main_add);
        mAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startScanner();
            }
        });
        mTextView = findViewById(R.id.textView);
        mImageView = findViewById(R.id.imageView);

        Toolbar mToolbar = findViewById(R.id.main_toolbar);
        setSupportActionBar(mToolbar);

        getPermissions();

    }
    // changes layout from main to manual entry view
//    public void change_layout(View view){
//        Intent myIntent = new Intent(view.getContext(), FirebaseActivity.class);
//        startActivityForResult(myIntent, 0);
//    }

    private void startScanner() {
        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.setCaptureActivity(CaptureActivity.class);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES);
        integrator.setBarcodeImageEnabled(true);
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
                Toast.makeText(this, "Success: Scanned " + result.getFormatName(), Toast.LENGTH_LONG).show();
                mTextView.setText(contents);
            }
            if(result.getBarcodeImagePath() != null) {
                Log.d(TAG, "" + result.getBarcodeImagePath());
                mImageView.setImageBitmap(BitmapFactory.decodeFile(result.getBarcodeImagePath()));
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_toolbar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch(item.getItemId()) {
            case R.id.manual_entry:
                Intent myIntent = new Intent(this, FirebaseActivity.class);
                startActivityForResult(myIntent, 0);
                return true;
            case R.id.logout:
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
                return true;
            case R.id.settings:
                Toast.makeText(this, "Not implemented yet", Toast.LENGTH_SHORT).show();
                return true;
            default:
            return super.onOptionsItemSelected(item);
        }
    }
}
