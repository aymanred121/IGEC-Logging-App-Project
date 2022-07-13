package com.example.igec_admin.Activities;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.igec_admin.R;
import com.google.android.material.button.MaterialButton;

public class SplashScreen_InternetConnection extends AppCompatActivity {

    private MaterialButton vRetry;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen_internet_connection);
        vRetry = findViewById(R.id.Open_Wifi);
        vRetry.setOnClickListener(view -> {
            isNetworkAvailable();
        });

    }


    @Override
    protected void onResume() {
        super.onResume();
        isNetworkAvailable();
    }
    private void isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        boolean connected = activeNetworkInfo != null && activeNetworkInfo.isConnected();
        if (connected) {
            Intent intent = new Intent(SplashScreen_InternetConnection.this, MainActivity.class);
            startActivity(intent);
            finish();
        }
    }

}