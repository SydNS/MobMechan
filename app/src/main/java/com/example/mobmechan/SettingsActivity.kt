package com.example.mobmechan

import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.StorageTask
import com.squareup.picasso.Picasso
import com.theartofdev.edmodo.cropper.CropImage
import de.hdodenhof.circleimageview.CircleImageView
import java.util.*

class SettingsActivity constructor() : AppCompatActivity() {
    private var getType: String? = null
    private var profileImageView: CircleImageView? = null
    private var nameEditText: EditText? = null
    private var phoneEditText: EditText? = null
    private var driverCarName: EditText? = null
    private var closeButton: ImageView? = null
    private var saveButton: ImageView? = null
    private var profileChangeBtn: TextView? = null
    private var databaseReference: DatabaseReference? = null
    private var mAuth: FirebaseAuth? = null
    private var checker: String = ""
    private var imageUri: Uri? = null
    private var myUrl: String = ""
    private var uploadTask: StorageTask<*>? = null
    private var storageProfilePicsRef: StorageReference? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        getType = intent.getStringExtra("type")
        Toast.makeText(this, getType, Toast.LENGTH_SHORT).show()
        mAuth = FirebaseAuth.getInstance()
        databaseReference =
            FirebaseDatabase.getInstance().reference.child("Users").child((getType)!!)
        storageProfilePicsRef =
            FirebaseStorage.getInstance().reference.child("Profile Pictures")
        profileImageView = findViewById(R.id.profile_image)
        nameEditText = findViewById(R.id.name)
        phoneEditText = findViewById(R.id.phone_number)
        driverCarName = findViewById(R.id.driver_car_name)
        driverCarName?.visibility = View.VISIBLE

//        if ((getType == "Drivers")) {
//            driverCarName?.hint="Enter Your Details"
//        }else{
//            driverCarName?.hint="Enter Your Details"
//
//        }
        when(getType){
            "Customers"->{
                driverCarName?.hint="Enter Your Situation Details"
            }
            "Drivers"->{
                driverCarName?.hint="Enter Your Mechanic Details"
            }
            else->{
                driverCarName?.hint="Enter Your Details"
            }
        }
        closeButton = findViewById(R.id.close_button)
        saveButton = findViewById(R.id.save_button)
        profileChangeBtn = findViewById(R.id.change_picture_btn)
        closeButton?.setOnClickListener {
            when (getType) {
                "Drivers" -> {
                    startActivity(Intent(this@SettingsActivity, MechanicMapUi::class.java))
                }
                else -> {
                    startActivity(Intent(this@SettingsActivity, UserMapUi::class.java))
                }
            }
        }
        saveButton?.setOnClickListener {
            if ((checker == "clicked")) {
                validateControllers()
            } else {
                validateAndSaveOnlyInformation()
            }
        }
        profileChangeBtn?.setOnClickListener(View.OnClickListener {
            checker = "clicked"
            CropImage.activity()
                .setAspectRatio(1, 1)
                .start(this@SettingsActivity)
        })
        userInformation
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if ((requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) && (resultCode == RESULT_OK) && (data != null)) {
            val result: CropImage.ActivityResult = CropImage.getActivityResult(data)
            imageUri = result.getUri()
            profileImageView!!.setImageURI(imageUri)
        } else {
            if ((getType == "Drivers")) {
                startActivity(Intent(this@SettingsActivity, MechanicMapUi::class.java))
            } else {
                startActivity(Intent(this@SettingsActivity, UserMapUi::class.java))
            }
            Toast.makeText(this, "Error, Try Again.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun validateControllers() {
        if (TextUtils.isEmpty(nameEditText!!.getText().toString())) {
            Toast.makeText(this, "Please provide your name.", Toast.LENGTH_SHORT).show()
        } else if (TextUtils.isEmpty(phoneEditText!!.getText().toString())) {
            Toast.makeText(this, "Please provide your phone number.", Toast.LENGTH_SHORT).show()
        } else if ((getType == "Drivers") && TextUtils.isEmpty(
                driverCarName!!.getText().toString()
            )
        ) {
            Toast.makeText(this, "Please provide your car Name.", Toast.LENGTH_SHORT).show()
        } else if ((checker == "clicked")) {
            uploadProfilePicture()
        }
    }

    private fun uploadProfilePicture() {
        val progressDialog: ProgressDialog = ProgressDialog(this)
        progressDialog.setTitle("Settings Account Information")
        progressDialog.setMessage("Please wait, while we are settings your account information")
        progressDialog.show()
        if (imageUri != null) {
            val fileRef: StorageReference = storageProfilePicsRef!!.child((mAuth?.currentUser?.uid ?: String() ) + ".jpg")
            uploadTask  = fileRef.putFile(imageUri!!)
            uploadTask!!.continueWithTask { task ->
                if (!task.isSuccessful) {
                    task.exception?.let {
                        throw it
                    }
                }
                fileRef.downloadUrl
            }.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val downloadUrl: Uri = task.result
                    myUrl = downloadUrl.toString()
                    val userMap: HashMap<String, Any> = HashMap()
                    userMap.put("uid", mAuth!!.getCurrentUser().uid)
                    userMap.put("name", nameEditText!!.text.toString())
                    userMap.put("phone", phoneEditText!!.text.toString())
                    userMap.put("image", myUrl)
                    if ((getType == "Drivers")) {
                        userMap.put("car", driverCarName!!.getText().toString())
                    }
                    databaseReference!!.child(mAuth!!.getCurrentUser().getUid())
                        .updateChildren(userMap)
                    progressDialog.dismiss()
                    if ((getType == "Drivers")) {
                        startActivity(
                            Intent(
                                this@SettingsActivity,
                                MechanicMapUi::class.java
                            )
                        )
                    } else {
                        startActivity(
                            Intent(
                                this@SettingsActivity,
                                UserMapUi::class.java
                            )
                        )
                    }
                }
            }
        } else {
            Toast.makeText(this, "Image is not selected.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun validateAndSaveOnlyInformation() {
        if (TextUtils.isEmpty(nameEditText!!.getText().toString())) {
            Toast.makeText(this, "Please provide your name.", Toast.LENGTH_SHORT).show()
        } else if (TextUtils.isEmpty(phoneEditText!!.getText().toString())) {
            Toast.makeText(this, "Please provide your phone number.", Toast.LENGTH_SHORT).show()
        } else if ((getType == "Drivers") && TextUtils.isEmpty(
                driverCarName!!.getText().toString()
            )
        ) {
            Toast.makeText(this, "Please provide your car Name.", Toast.LENGTH_SHORT).show()
        } else {
            val userMap: HashMap<String, Any> = HashMap()
            userMap.put("uid", mAuth!!.getCurrentUser().getUid())
            userMap.put("name", nameEditText!!.getText().toString())
            userMap.put("phone", phoneEditText!!.getText().toString())
            if ((getType == "Drivers")) {
                userMap.put("car", driverCarName!!.text.toString())
            }
            databaseReference!!.child(mAuth!!.getCurrentUser().getUid()).updateChildren(userMap)
            if ((getType == "Drivers")) {
                startActivity(Intent(this@SettingsActivity, MechanicMapUi::class.java))
            } else {
                startActivity(Intent(this@SettingsActivity, UserMapUi::class.java))
            }
        }
    }

    private val userInformation: Unit
        private get() {
            databaseReference!!.child(mAuth!!.getCurrentUser().getUid())
                .addValueEventListener(object : ValueEventListener {
                    public override fun onDataChange(dataSnapshot: DataSnapshot) {
                        if (dataSnapshot.exists() && dataSnapshot.childrenCount > 0) {
                            val name: String = dataSnapshot.child("name").value.toString()
                            val phone: String = dataSnapshot.child("phone").value.toString()
                            nameEditText!!.setText(name)
                            phoneEditText!!.setText(phone)
                            if ((getType == "Drivers")) {
                                val car: String = dataSnapshot.child("car").value.toString()
                                driverCarName!!.setText(car)
                            }
                            if (dataSnapshot.hasChild("image")) {
                                val image: String =
                                    dataSnapshot.child("image").getValue().toString()
                                Picasso.get().load(image).into(profileImageView)
                            }
                        }
                    }

                    public override fun onCancelled(databaseError: DatabaseError) {}
                })
        }
}