package com.example.igecuser.Activities;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.example.igecuser.fireBase.Employee;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class LauncherActivity extends Activity {
    private boolean logged = false;
    public static final String IGEC = "IGEC";
    public static final String ID = "ID";
    public static final String LOGGED = "LOGGED";
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
                        if (employee.getManagerID().equals("adminID"))
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
}
