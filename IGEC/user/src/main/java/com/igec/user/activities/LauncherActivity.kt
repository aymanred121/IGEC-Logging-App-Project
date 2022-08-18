package com.igec.user.activities

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.provider.Settings
import android.widget.Toast
import com.igec.common.CONSTANTS
import com.google.firebase.firestore.DocumentSnapshot
import com.igec.common.firebase.Employee

class LauncherActivity : Activity() {
    private var logged = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        validateDate(this)
        // for splash screen
        Handler().postDelayed({
            val preferences = getSharedPreferences(CONSTANTS.IGEC, MODE_PRIVATE)
            logged = preferences.getBoolean(CONSTANTS.LOGGED, false)


            /*
             *
             * not logged before -> Open  login Activity [x]
             * logged:
             *      not assigned to a project -> closes the app [x]
             *      assigned to a project:
             *          the account isn't locked => Open login Activity [x]
             *          the account is locked => open suitable dashboard [x]
             * */if (!logged) {
            val intent = Intent(this@LauncherActivity, LoginActivity::class.java)
            startActivity(intent)
            finish()
        } else {
            val savedID = preferences.getString(CONSTANTS.ID, "")
            CONSTANTS.EMPLOYEE_COL.document(savedID!!).get()
                .addOnSuccessListener { documentSnapshot: DocumentSnapshot ->
                    if (!documentSnapshot.exists()) return@addOnSuccessListener
                    val employee = documentSnapshot.toObject(Employee::class.java)
                    val intent: Intent
                    assert(employee != null)
                    if (employee!!.managerID == null) {
                        Toast.makeText(
                            this@LauncherActivity,
                            "You're not assigned to any project yet",
                            Toast.LENGTH_SHORT
                        ).show()
                        finish()
                    }
                    // not used by another device but still logged here
                    // meaning that the account has been unlocked while the account is still open in a device
                    if (!employee.isLocked) {
                        intent = Intent(this@LauncherActivity, LoginActivity::class.java)
                        Toast.makeText(
                            this@LauncherActivity,
                            "Account is unlocked, login is required",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        // not used -> Open suitable dashboard
                        intent = if (employee.managerID == CONSTANTS.ADMIN) Intent(
                            this@LauncherActivity,
                            MDashboard::class.java
                        ) else Intent(this@LauncherActivity, EDashboard::class.java)
                    }
                    intent.putExtra("user", employee)
                    startActivity(intent)
                    finish()
                }
        }
        }, 2000)
    }

    override fun onResume() {
        super.onResume()
        validateDate(this)
    }
    private fun validateDate(c: Context) {
        if (Settings.Global.getInt(c.contentResolver, Settings.Global.AUTO_TIME, 0) != 1) {
            val intent = Intent(this@LauncherActivity, DateInaccurate::class.java)
            startActivity(intent)
            finish()
        }
    }
}