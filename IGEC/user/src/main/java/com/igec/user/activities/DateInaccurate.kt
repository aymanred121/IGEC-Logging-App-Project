package com.igec.user.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import com.igec.user.databinding.ActivitySplashScreenDateIncorrectBinding

class DateInaccurate : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivitySplashScreenDateIncorrectBinding.inflate(
            layoutInflater
        )
        setContentView(binding.root)
        binding.adjustDateButton.setOnClickListener { startActivity(Intent(Settings.ACTION_DATE_SETTINGS)) }
    }

    override fun onResume() {
        super.onResume()
        validateTime(this)
    }

    private fun validateTime(c: Context) {
        if (Settings.Global.getInt(c.contentResolver, Settings.Global.AUTO_TIME, 0) == 1) {
            val intent = Intent(this@DateInaccurate, LauncherActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}