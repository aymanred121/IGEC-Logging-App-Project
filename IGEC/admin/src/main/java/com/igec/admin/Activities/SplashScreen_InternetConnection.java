package com.igec.admin.Activities;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.igec.admin.R;
import com.google.android.material.button.MaterialButton;
import com.igec.admin.databinding.ActivitySplashScreenInternetConnectionBinding;

public class SplashScreen_InternetConnection extends AppCompatActivity {

    private ActivitySplashScreenInternetConnectionBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySplashScreenInternetConnectionBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.retryButton.setOnClickListener(view -> {
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