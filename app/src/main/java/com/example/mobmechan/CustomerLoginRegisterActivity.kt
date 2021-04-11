package com.example.mobmechan

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuth.AuthStateListener
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class CustomerLoginRegisterActivity : AppCompatActivity() {
    private var CreateCustomerAccount: TextView? = null
    private var TitleCustomer: TextView? = null
    private var LoginCustomerButton: Button? = null
    private var RegisterCustomerButton: Button? = null
    private var CustomerEmail: EditText? = null
    private var CustomerPassword: EditText? = null
    private var customersDatabaseRef: DatabaseReference? = null
    private var mAuth: FirebaseAuth? = null
    private var firebaseAuthListner: AuthStateListener? = null
    private var loadingBar: ProgressDialog? = null
    private var currentUser: FirebaseUser? = null
    var currentUserId: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_customer_login_register)
        mAuth = FirebaseAuth.getInstance()
        firebaseAuthListner = AuthStateListener {
            currentUser = FirebaseAuth.getInstance().currentUser
            if (currentUser != null) {
                val intent = Intent(
                    this@CustomerLoginRegisterActivity,
                    CustomersMapActivity::class.java
                )
                startActivity(intent)
            }
        }
        CreateCustomerAccount = findViewById(R.id.customer_register_link) as TextView
        TitleCustomer = findViewById(R.id.customer_status) as TextView
        LoginCustomerButton = findViewById(R.id.customer_login_btn) as Button
        RegisterCustomerButton = findViewById(R.id.customer_register_btn) as Button
        CustomerEmail = findViewById(R.id.customer_email) as EditText
        CustomerPassword = findViewById(R.id.customer_password) as EditText
        loadingBar = ProgressDialog(this)
        RegisterCustomerButton!!.visibility = View.INVISIBLE
        RegisterCustomerButton!!.isEnabled = false
        CreateCustomerAccount!!.setOnClickListener {
            CreateCustomerAccount!!.visibility = View.INVISIBLE
            LoginCustomerButton!!.visibility = View.INVISIBLE
            TitleCustomer!!.text = "Driver Registration"
            RegisterCustomerButton!!.visibility = View.VISIBLE
            RegisterCustomerButton!!.isEnabled = true
        }
        RegisterCustomerButton!!.setOnClickListener {
            val email = CustomerEmail!!.text.toString()
            val password = CustomerPassword!!.text.toString()
            if (TextUtils.isEmpty(email)) {
                Toast.makeText(
                    this@CustomerLoginRegisterActivity,
                    "Please write your Email...",
                    Toast.LENGTH_SHORT
                ).show()
            }
            if (TextUtils.isEmpty(password)) {
                Toast.makeText(
                    this@CustomerLoginRegisterActivity,
                    "Please write your Password...",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                loadingBar!!.setTitle("Please wait :")
                loadingBar!!.setMessage("While system is performing processing on your data...")
                loadingBar!!.show()
                mAuth!!.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            currentUserId = mAuth!!.currentUser.uid
                            customersDatabaseRef =
                                FirebaseDatabase.getInstance("https://mobilemechan-default-rtdb.firebaseio.com/").reference.child("Users")
                                    .child("Clients").child(currentUserId!!)
                            customersDatabaseRef!!.setValue(true)
                            val intent = Intent(
                                this@CustomerLoginRegisterActivity,
                                CustomersMapActivity::class.java
                            )
                            startActivity(intent)
                            loadingBar!!.dismiss()
                        } else {
                            Toast.makeText(
                                this@CustomerLoginRegisterActivity,
                                "Please Try Again. Error Occurred, while registering... ",
                                Toast.LENGTH_SHORT
                            ).show()
                            loadingBar!!.dismiss()
                        }
                    }
            }
        }
        LoginCustomerButton!!.setOnClickListener {
            val email = CustomerEmail!!.text.toString()
            val password = CustomerPassword!!.text.toString()
            if (TextUtils.isEmpty(email)) {
                Toast.makeText(
                    this@CustomerLoginRegisterActivity,
                    "Please write your Email...",
                    Toast.LENGTH_SHORT
                ).show()
            }
            if (TextUtils.isEmpty(password)) {
                Toast.makeText(
                    this@CustomerLoginRegisterActivity,
                    "Please write your Password...",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                loadingBar!!.setTitle("Please wait :")
                loadingBar!!.setMessage("While system is performing processing on your data...")
                loadingBar!!.show()
                mAuth!!.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(
                            this@CustomerLoginRegisterActivity,
                            "Sign In , Successful...",
                            Toast.LENGTH_SHORT
                        ).show()
                        val intent = Intent(
                            this@CustomerLoginRegisterActivity,
                            CustomersMapActivity::class.java
                        )
                        startActivity(intent)
                        loadingBar!!.dismiss()
                    } else {
                        Toast.makeText(
                            this@CustomerLoginRegisterActivity,
                            "Error Occurred, while Signing In... ",
                            Toast.LENGTH_SHORT
                        ).show()
                        loadingBar!!.dismiss()
                    }
                }
            }
        }
    }
}
