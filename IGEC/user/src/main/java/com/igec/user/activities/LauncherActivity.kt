package com.igec.user.activities;

import static com.igec.common.CONSTANTS.ADMIN;
import static com.igec.common.CONSTANTS.EMPLOYEE_COL;
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

import com.igec.common.firebase.Employee;

public class LauncherActivity extends Activity {
    private boolean logged = false;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // for splash screen
        new Handler().postDelayed(() -> {
            SharedPreferences preferences = getSharedPreferences(IGEC, MODE_PRIVATE);
            logged = preferences.getBoolean(LOGGED, false);


            /*
             *
             * not logged before -> Open  login Activity [x]
             * logged:
             *      not assigned to a project -> closes the app [x]
             *      assigned to a project:
             *          the account isn't locked => Open login Activity [x]
             *          the account is locked => open suitable dashboard [x]
             * */

            if (!logged) {
                Intent intent = new Intent(LauncherActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            } else {
                String savedID = preferences.getString(ID, "");
                EMPLOYEE_COL.document(savedID).get().addOnSuccessListener(documentSnapshot -> {
                    if (!documentSnapshot.exists())
                        return;
                    Employee employee = documentSnapshot.toObject(Employee.class);
                    Intent intent;
                    // not assigned to any project -> can't login
                    assert employee != null;
                    if (employee.getManagerID() == null) {
                        Toast.makeText(LauncherActivity.this, "You're not assigned to any project yet", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                    // not used by another device but still logged here
                    // meaning that the account has been unlocked while the account is still open in a device
                    if (!employee.isLocked()) {
                        intent = new Intent(LauncherActivity.this, LoginActivity.class);
                        Toast.makeText(LauncherActivity.this, "Account is unlocked, login is required", Toast.LENGTH_SHORT).show();
                    } else {
                        // not used -> Open suitable dashboard
                        if (employee.getManagerID().equals(ADMIN))
                            intent = new Intent(LauncherActivity.this, MDashboard.class);
                        else
                            intent = new Intent(LauncherActivity.this, EDashboard.class);
                    }
                    intent.putExtra("user", employee);
                    startActivity(intent);
                    finish();
                });
            }
        }, 2000);


    }
}
