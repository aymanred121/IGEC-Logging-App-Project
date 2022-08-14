package com.igec.admin.Activities;


import static com.igec.common.CONSTANTS.EMPLOYEE_COL;
import static com.igec.common.CONSTANTS.ID;
import static com.igec.common.CONSTANTS.IGEC;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;

import androidx.annotation.Nullable;

import com.google.firebase.firestore.FirebaseFirestore;

public class LauncherActivity extends Activity {
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                SharedPreferences preferences = getSharedPreferences(IGEC, MODE_PRIVATE);
                if (preferences.getString(ID, "").equals("")) {
                    EMPLOYEE_COL.whereEqualTo("admin", true).limit(1).get().addOnSuccessListener(docs -> {
                        if (docs.size() != 0) {
                            Intent intent = new Intent(LauncherActivity.this, com.igec.admin.Activities.LoginActivity.class);
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