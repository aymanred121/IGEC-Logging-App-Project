package com.igec.user.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;

import com.igec.user.R;
import com.google.android.material.button.MaterialButton;

public class SplashScreen_DateInaccurate extends AppCompatActivity {

    private MaterialButton vAdjustTime;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen_date_incorrect);
        vAdjustTime = findViewById(R.id.Adjust_Date);
        vAdjustTime.setOnClickListener(view -> {
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
            Intent intent = new Intent(SplashScreen_DateInaccurate.this, MainActivity.class);
            startActivity(intent);
            finish();
        }
    }

}