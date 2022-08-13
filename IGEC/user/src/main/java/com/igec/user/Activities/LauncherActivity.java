package com.igec.user.Activities;

import static com.igec.common.CONSTANTS.ADMIN;
import static com.igec.common.CONSTANTS.ID;
import static com.igec.common.CONSTANTS.IGEC;
import static com.igec.common.CONSTANTS.LOGGED;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.igec.user.R;
import com.igec.common.firebase.Employee;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class LauncherActivity extends Activity {
    private boolean logged = false;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                SharedPreferences preferences = getSharedPreferences(IGEC, MODE_PRIVATE);
                logged = preferences.getBoolean(LOGGED, false);
                if (!logged) {
                    Intent intent = new Intent(LauncherActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    String savedID = preferences.getString(ID, "");
                    db.collection("employees").document(savedID).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            if (!documentSnapshot.exists())
                                return;
                            Employee employee = documentSnapshot.toObject(Employee.class);
                            Intent intent;
                            if (employee.getManagerID() == null) {
                                Toast.makeText(LauncherActivity.this, "You're not assigned to any project yet", Toast.LENGTH_SHORT).show();
                                finishAffinity();
                                System.exit(0);
                            }
                            if (!employee.isLocked()) {
                                intent = new Intent(LauncherActivity.this, MainActivity.class);
                                Toast.makeText(LauncherActivity.this, "Account is unlocked, login is required", Toast.LENGTH_SHORT).show();
                            } else {
                                if (employee.getManagerID().equals(ADMIN))
                                    intent = new Intent(LauncherActivity.this, MDashboard.class);
                                else
                                    intent = new Intent(LauncherActivity.this, EDashboard.class);
                            }
                            intent.putExtra("emp", employee);
                            startActivity(intent);
                            finish();
                        }
                    });
                }
            }
        }, 2000);


    }
}
