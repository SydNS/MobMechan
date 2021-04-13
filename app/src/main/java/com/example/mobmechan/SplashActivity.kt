package com.example.mobmechan

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class SplashActivity constructor() : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        val thread: Thread = object : Thread() {
            public override fun run() {
                try {
                    sleep(1000)
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    val mainIntent: Intent =
                        Intent(this@SplashActivity, WelcomeActivity::class.java)
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