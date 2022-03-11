package com.example.igecuser.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.budiyev.android.codescanner.CodeScanner;
import com.budiyev.android.codescanner.CodeScannerView;
import com.example.igecuser.R;

public class qrCameraActivity extends AppCompatActivity {

    //Views
    private CodeScanner mCodeScanner;

    //Vars
    //Overrides
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr_camera);
        CodeScannerView scannerView = findViewById(R.id.scanner_view);
        mCodeScanner = new CodeScanner(getApplicationContext(), scannerView);
        mCodeScanner.setDecodeCallback(result -> runOnUiThread(() -> {
            Intent resultIntent = new Intent();
            resultIntent.putExtra("qrCamera", result.getText());
            setResult(Activity.RESULT_OK, resultIntent);
            finish();
        }));
        scannerView.setOnClickListener(view -> mCodeScanner.startPreview());
    }

    @Override
    protected void onResume() {
        super.onResume();
        mCodeScanner.startPreview();
    }

    @Override
    protected void onPause() {
        mCodeScanner.releaseResources();
        super.onPause();
    }
    //Functions
    //Listeners


}