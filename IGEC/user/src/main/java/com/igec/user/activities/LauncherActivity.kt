package com.igec.user.activities

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.provider.Settings
import android.widget.Toast
import com.igec.common.CONSTANTS
import com.google.firebase.firestore.DocumentSnapshot
import com.igec.common.firebase.Employee
import com.igec.user.AlarmReceiver
import com.igec.user.R
import java.util.*

class LauncherActivity : Activity() {
    private var logged = false
    private var alarmManager: AlarmManager? = null
    private var pendingIntent: PendingIntent? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = System.currentTimeMillis()
        // set to 4:30 pm
        calendar.set(Calendar.HOUR_OF_DAY, 16)
        calendar.set(Calendar.MINUTE, 30)
        calendar.set(Calendar.SECOND, 0)
        if (calendar.get(Calendar.DAY_OF_WEEK) != Calendar.FRIDAY) {
            createNotificationChannel(
                "shift-notification",
                R.string.shift_notification,
                R.string.shift_notification_channel_description
            )
            alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(this, AlarmReceiver::class.java)
            pendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_MUTABLE)
            }else{
                PendingIntent.getBroadcast(this, 0, intent, 0)
            }
            alarmManager?.setInexactRepeating(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                AlarmManager.INTERVAL_DAY,
                pendingIntent
            )
        }
        //validateDate(this)
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
                    // not used by another device but still logged here
                    // meaning that the account has been unlocked while the account is still open in a device
                    if (!employee!!.isLocked) {
                        intent = Intent(this@LauncherActivity, LoginActivity::class.java)
                        Toast.makeText(
                            this@LauncherActivity,
                            "Account is unlocked, login is required",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        // not used -> Open suitable dashboard
                        intent = if (employee.isManager) Intent(
                            this@LauncherActivity,
                            MDashboard::class.java
                        ) else Intent(this@LauncherActivity, EDashboard::class.java)
                    }
                    intent.putExtra("user", employee)
                    startActivity(intent)
                    finish()
                }
        }
        }, 0)
    }

    override fun onResume() {
        super.onResume()
        //validateDate(this)
    }

    private fun validateDate(c: Context) {
        if (Settings.Global.getInt(c.contentResolver, Settings.Global.AUTO_TIME, 0) != 1) {
            val intent = Intent(this@LauncherActivity, DateInaccurate::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun createNotificationChannel(CHANNEL_ID: String, channelName: Int, channelDesc: Int) {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        val name = getString(channelName)
        val descriptionText = getString(channelDesc)
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val alarmSound: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
            description = descriptionText
            enableLights(true)
            lightColor = Color.GREEN
            setSound(alarmSound, null)
        }
        // Register the channel with the system
        val notificationManager: NotificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }
}