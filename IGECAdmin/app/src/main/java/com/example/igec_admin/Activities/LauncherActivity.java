package com.example.igec_admin.Activities;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;

import androidx.annotation.Nullable;

import com.google.firebase.firestore.FirebaseFirestore;

public class LauncherActivity extends Activity {
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    public static final String IGEC = "IGEC";
    public static final String ID = "ID";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                SharedPreferences preferences = getSharedPreferences(IGEC, MODE_PRIVATE);
                if (preferences.getString(ID, "").equals("")) {
                    db.collection("employees").whereEqualTo("admin", true).limit(1).get().addOnSuccessListener(docs -> {
                        if (docs.size() != 0) {
                            Intent intent = new Intent(LauncherActivity.this, LoginActivity.class);
                            startActivity(intent);
                        } else {
                            Intent intent = new Intent(LauncherActivity.this, MainActivity.class);
                            startActivity(intent);
                        }
                        finish();
                    });
                } else {
                    Intent intent = new Intent(LauncherActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                }
            }

        }, 2000);


    }
}