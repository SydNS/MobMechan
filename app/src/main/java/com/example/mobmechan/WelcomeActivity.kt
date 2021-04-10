package com.example.mobmechan

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuth.AuthStateListener
import com.google.firebase.auth.FirebaseUser


class WelcomeActivity : AppCompatActivity() {
    private var DriverWelcomeButton: Button? = null
    private var CustomerWelcomeButton: Button? = null
    private val mAuth: FirebaseAuth? = null
    private val firebaseAuthListner: AuthStateListener? = null
    private val currentUser: FirebaseUser? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)


//        mAuth = FirebaseAuth.getInstance();
//
//        firebaseAuthListner = new FirebaseAuth.AuthStateListener() {
//            @Override
//            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth)
//            {
//                currentUser = FirebaseAuth.getInstance().getCurrentUser();
//
//                if(currentUser != null)
//                {
//                    Intent intent = new Intent(WelcomeActivity.this, WelcomeActivity.class);
//                    startActivity(intent);
//                }
//            }
//        };
        DriverWelcomeButton = findViewById(R.id.driver_welcome_btn) as Button
        CustomerWelcomeButton = findViewById(R.id.customer_welcome_btn) as Button
        DriverWelcomeButton!!.setOnClickListener {
            val DriverIntent = Intent(
                this@WelcomeActivity,
                DriverLoginRegisterActivity::class.java
            )
            startActivity(DriverIntent)
        }
        CustomerWelcomeButton!!.setOnClickListener {
            val CustomerIntent = Intent(
                this@WelcomeActivity,
                CustomerLoginRegisterActivity::class.java
            )
            startActivity(CustomerIntent)
        }
    }

//    @Override
    //    protected void onStart()
    //    {
    //        super.onStart();
    //
    //        mAuth.addAuthStateListener(firebaseAuthListner);
    //    }
    //
    //
    //    @Override
    //    protected void onStop()
    //    {
    //        super.onStop();
    //
    //        mAuth.removeAuthStateListener(firebaseAuthListner);
    //    }
}
