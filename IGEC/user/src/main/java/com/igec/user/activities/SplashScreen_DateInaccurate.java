package com.igec.user.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;

import com.igec.user.databinding.ActivitySplashScreenDateIncorrectBinding;

public class SplashScreen_DateInaccurate extends AppCompatActivity {

    private ActivitySplashScreenDateIncorrectBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySplashScreenDateIncorrectBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.adjustDateButton.setOnClickListener(view -> {
            startActivity(new Intent(android.provider.Settings.ACTION_DATE_SETTINGS));
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        validateTime(this);
    }
    public void validateTime(Context c) {
        if (Settings.Global.getInt(c.getContentResolver(), Settings.Global.AUTO_TIME, 0) == 1) {
            Intent intent = new Intent(SplashScreen_DateInaccurate.this, LoginActivity.class);
            startActivity(intent);
            finish();
        }
    }

}