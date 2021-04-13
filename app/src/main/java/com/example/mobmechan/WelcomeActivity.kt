package com.example.mobmechan

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.mobmechan.WelcomeActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuth.AuthStateListener
import com.google.firebase.auth.FirebaseUser

class WelcomeActivity constructor() : AppCompatActivity() {
    private var DriverWelcomeButton: Button? = null
    private var CustomerWelcomeButton: Button? = null
    private val mAuth: FirebaseAuth? = null
    private val firebaseAuthListner: AuthStateListener? = null
    private val currentUser: FirebaseUser? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)



        DriverWelcomeButton = findViewById<View>(R.id.driver_welcome_btn) as Button?
        CustomerWelcomeButton = findViewById<View>(R.id.customer_welcome_btn) as Button?
        DriverWelcomeButton!!.setOnClickListener {
            val DriverIntent: Intent =
                Intent(this@WelcomeActivity, DriverLoginRegisterActivity::class.java)
            startActivity(DriverIntent)
        }
        CustomerWelcomeButton!!.setOnClickListener(object : View.OnClickListener {
            override fun onClick(view: View) {
                val CustomerIntent: Intent =
                    Intent(this@WelcomeActivity, CustomerLoginRegisterActivity::class.java)
                startActivity(CustomerIntent)
            }
        })
    }
}