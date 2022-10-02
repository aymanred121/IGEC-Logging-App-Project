package com.igec.admin.activities

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Network
import android.os.Bundle
import android.os.Handler
import com.igec.common.CONSTANTS
import com.google.firebase.firestore.QuerySnapshot

class LauncherActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val connectivityManager = applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        connectivityManager.let {
            it.registerDefaultNetworkCallback(object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    //take action when network connection is gained
                    Handler().postDelayed({
                        val preferences = getSharedPreferences(CONSTANTS.IGEC, MODE_PRIVATE)
                        if (preferences.getString(CONSTANTS.ID, "") == "") {
                            CONSTANTS.EMPLOYEE_COL.whereEqualTo("admin", true).limit(1).get()
                                .addOnSuccessListener { docs: QuerySnapshot ->
                                    if (docs.size() != 0) {
                                        val intent = Intent(this@LauncherActivity, LoginActivity::class.java)
                                        startActivity(intent)
                                    } else {
                                        val intent = Intent(this@LauncherActivity, MainActivity::class.java)
                                        startActivity(intent)
                                    }
                                    finish()
                                }
                        } else {
                            val intent = Intent(this@LauncherActivity, MainActivity::class.java)
                            startActivity(intent)
                            finish()
                        }
                    }, 0)
                }

                override fun onLost(network: Network) {
                    //take action when network connection is lost
                    val intent = Intent(this@LauncherActivity, InternetConnection::class.java)
                    startActivity(intent)
                    finish()
                }
            })
        }
    }
}