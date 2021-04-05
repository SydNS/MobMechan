package com.example.mobmechan

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity


class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val thread: Thread = object : Thread() {
            override fun run() {
                try {
                    sleep(1000)
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    val mainIntent = Intent(this@SplashActivity, WelcomeActivity::class.java)
                    startActivity(mainIntent)
                }
            }
        }
        thread.start()
    }

    override fun onPause() {
        super.onPause()
        finish()
    }
}
