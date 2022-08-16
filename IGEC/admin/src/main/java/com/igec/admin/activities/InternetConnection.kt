package com.igec.admin.activities

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Network
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.igec.admin.databinding.ActivityInternetConnectionBinding

class InternetConnection : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val connectivityManager = application.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        connectivityManager.let {
            it.registerDefaultNetworkCallback(object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    //take action when network connection is gained
                    val intent = Intent(this@InternetConnection, LauncherActivity::class.java)
                    startActivity(intent)
                    finish()
                }
                override fun onLost(network: Network) {
                    //take action when network connection is lost
                }
            })
        }
        val binding = ActivityInternetConnectionBinding.inflate(
            layoutInflater
        )
        setContentView(binding.root)
    }


}