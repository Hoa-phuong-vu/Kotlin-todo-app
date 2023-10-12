package com.example.customproject

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.WindowInsets
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        //Hide the system bars -> enter full screen
        val windowInsetsController =
            WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())

        //Automatically goes onto log in page
        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this, LogInActivity::class.java))
            finish()
        }, 2000)


    }
}