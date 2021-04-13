package com.example.mobmechan

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuth.AuthStateListener
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class DriverLoginRegisterActivity constructor() : AppCompatActivity() {
    private var CreateDriverAccount: TextView? = null
    private var TitleDriver: TextView? = null
    private var LoginDriverButton: Button? = null
    private var RegisterDriverButton: Button? = null
    private var DriverEmail: EditText? = null
    private var DriverPassword: EditText? = null
    private var driversDatabaseRef: DatabaseReference? = null
    private var mAuth: FirebaseAuth? = null
    private val firebaseAuthListner: AuthStateListener? = null
    private var loadingBar: ProgressDialog? = null
    private val currentUser: FirebaseUser? = null
    var currentUserId: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_driver_login_register)
        mAuth = FirebaseAuth.getInstance()

//        firebaseAuthListner = new FirebaseAuth.AuthStateListener() {
//            @Override
//            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth)
//            {
//                currentUser = FirebaseAuth.getInstance().getCurrentUser();
//
//                if(currentUser != null)
//                {
//                    Intent intent = new Intent(DriverLoginRegisterActivity.this, DriverMapActivity.class);
//                    startActivity(intent);
//                }
//            }
//        };
        CreateDriverAccount = findViewById<View>(R.id.create_driver_account) as TextView?
        TitleDriver = findViewById<View>(R.id.titlr_driver) as TextView?
        LoginDriverButton = findViewById<View>(R.id.login_driver_btn) as Button?
        RegisterDriverButton = findViewById<View>(R.id.register_driver_btn) as Button?
        DriverEmail = findViewById<View>(R.id.driver_email) as EditText?
        DriverPassword = findViewById<View>(R.id.driver_password) as EditText?
        loadingBar = ProgressDialog(this)
        RegisterDriverButton!!.setVisibility(View.INVISIBLE)
        RegisterDriverButton!!.setEnabled(false)
        CreateDriverAccount!!.setOnClickListener(object : View.OnClickListener {
            public override fun onClick(view: View) {
                CreateDriverAccount!!.setVisibility(View.INVISIBLE)
                LoginDriverButton!!.setVisibility(View.INVISIBLE)
                TitleDriver!!.setText("Driver Registration")
                RegisterDriverButton!!.setVisibility(View.VISIBLE)
                RegisterDriverButton!!.setEnabled(true)
            }
        })
        RegisterDriverButton!!.setOnClickListener(object : View.OnClickListener {
            public override fun onClick(view: View) {
                val email: String = DriverEmail!!.getText().toString()
                val password: String = DriverPassword!!.getText().toString()
                if (TextUtils.isEmpty(email)) {
                    Toast.makeText(
                        this@DriverLoginRegisterActivity,
                        "Please write your Email...",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                if (TextUtils.isEmpty(password)) {
                    Toast.makeText(
                        this@DriverLoginRegisterActivity,
                        "Please write your Password...",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    loadingBar!!.setTitle("Please wait :")
                    loadingBar!!.setMessage("While system is performing processing on your data...")
                    loadingBar!!.show()
                    mAuth!!.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(object : OnCompleteListener<AuthResult?> {
                            public override fun onComplete(task: Task<AuthResult?>) {
                                if (task.isSuccessful()) {
                                    currentUserId = mAuth!!.getCurrentUser().getUid()
                                    driversDatabaseRef =
                                        FirebaseDatabase.getInstance().getReference().child("Users")
                                            .child("Drivers").child(
                                                currentUserId!!
                                            )
                                    driversDatabaseRef!!.setValue(true)
                                    val intent: Intent = Intent(
                                        this@DriverLoginRegisterActivity,
                                        MechanicMapUi::class.java
                                    )
                                    startActivity(intent)
                                    loadingBar!!.dismiss()
                                } else {
                                    Toast.makeText(
                                        this@DriverLoginRegisterActivity,
                                        "Please Try Again. Error Occurred, while registering... ",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    loadingBar!!.dismiss()
                                }
                            }
                        })
                }
            }
        })
        LoginDriverButton!!.setOnClickListener(object : View.OnClickListener {
            public override fun onClick(view: View) {
                val email: String = DriverEmail!!.getText().toString()
                val password: String = DriverPassword!!.getText().toString()
                if (TextUtils.isEmpty(email)) {
                    Toast.makeText(
                        this@DriverLoginRegisterActivity,
                        "Please write your Email...",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                if (TextUtils.isEmpty(password)) {
                    Toast.makeText(
                        this@DriverLoginRegisterActivity,
                        "Please write your Password...",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    loadingBar!!.setTitle("Please wait :")
                    loadingBar!!.setMessage("While system is performing processing on your data...")
                    loadingBar!!.show()
                    mAuth!!.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(object : OnCompleteListener<AuthResult?> {
                            public override fun onComplete(task: Task<AuthResult?>) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(
                                        this@DriverLoginRegisterActivity,
                                        "Sign In , Successful...",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    val intent: Intent = Intent(
                                        this@DriverLoginRegisterActivity,
                                        MechanicMapUi::class.java
                                    )
                                    startActivity(intent)
                                } else {
                                    Toast.makeText(
                                        this@DriverLoginRegisterActivity,
                                        "Error Occurred, while Signing In... ",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        })
                }
            }
        })
    } //    @Override
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