@file:Suppress("DEPRECATION")

package com.example.mobmechan

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.os.PersistableBundle
import android.view.View
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.fragment.app.FragmentActivity
import com.firebase.geofire.GeoFire
import com.firebase.geofire.GeoLocation
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.LocationListener
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

class MechanicMapUi : FragmentActivity(), OnMapReadyCallback,
    GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
    LocationListener {
    private var mMap: GoogleMap? = null
    var googleApiClient: GoogleApiClient? = null
    var LastLocation: Location? = null
    var locationRequest: LocationRequest? = null
    private var LogoutDriverBtn: Button? = null
    private var SettingsDriverButton: Button? = null

    //    private lateinit var callingbtn: Button
    private var mAuth: FirebaseAuth? = null
    private var currentUser: FirebaseUser? = null
    private var currentLogOutUserStatus: Boolean = false

    //getting request customer's id
    private var customerID: String = ""
    private var driverID: String? = null
    private var AssignedCustomerRef: DatabaseReference? = null
    private var AssignedCustomerPickUpRef: DatabaseReference? = null
    var PickUpMarker: Marker? = null
    private var AssignedCustomerPickUpRefListner: ValueEventListener? = null
    private var txtName: TextView? = null
    private var txtPhone: TextView? = null
    private var profilePic: CircleImageView? = null
    private var relativeLayout: RelativeLayout? = null

    //    lateinit var phone: String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //notice
        setContentView(R.layout.activity_driver_map)
        mAuth = FirebaseAuth.getInstance()
        currentUser = mAuth!!.currentUser
        driverID = mAuth!!.currentUser.uid
        LogoutDriverBtn = findViewById<View>(R.id.logout_driv_btn) as Button?
        SettingsDriverButton = findViewById<View>(R.id.settings_driver_btn) as Button?

        val callingbtn: Button = findViewById(R.id.callingbtn)
        txtName = findViewById(R.id.name_customer)
        txtPhone = findViewById(R.id.phone_customer)
        profilePic = findViewById(R.id.profile_image_customer)
        relativeLayout = findViewById(R.id.rel2)
        val mapFragment: SupportMapFragment? = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment!!.getMapAsync(this@MechanicMapUi)
        SettingsDriverButton!!.setOnClickListener {
            val intent: Intent = Intent(this@MechanicMapUi, SettingsActivity::class.java)
            intent.putExtra("type", "Drivers")
            startActivity(intent)
        }
        LogoutDriverBtn!!.setOnClickListener {
            currentLogOutUserStatus = true
            DisconnectDriver()
            mAuth!!.signOut()
            LogOutUser()
        }

        callingbtn!!.setOnClickListener {
            val intent = Intent(Intent.ACTION_CALL, Uri.parse("tel:0780134747"))
            startActivity(intent)

        }

        assignedCustomersRequest
    }

    //getting assigned customer location
    private val assignedCustomersRequest: Unit
        get() {
            AssignedCustomerRef = FirebaseDatabase.getInstance().reference.child("Users")
                .child("Drivers").child((driverID)!!).child("CustomerRideID")
            AssignedCustomerRef!!.addValueEventListener(object : ValueEventListener {
                public override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        customerID = dataSnapshot.value.toString()
                        //getting assigned customer location
                        GetAssignedCustomerPickupLocation()
                        relativeLayout!!.visibility = View.VISIBLE
                        assignedCustomerInformation
                    } else {
                        customerID = ""
                        if (PickUpMarker != null) {
                            PickUpMarker!!.remove()
                        }
                        if (AssignedCustomerPickUpRefListner != null) {
                            AssignedCustomerPickUpRef!!.removeEventListener(
                                AssignedCustomerPickUpRefListner!!
                            )
                        }
                        relativeLayout!!.setVisibility(View.GONE)
                    }
                }

                public override fun onCancelled(databaseError: DatabaseError) {}
            })
        }

    private fun GetAssignedCustomerPickupLocation() {
        AssignedCustomerPickUpRef =
            FirebaseDatabase.getInstance().reference.child("Customer Requests")
                .child(customerID).child("l")
        AssignedCustomerPickUpRefListner =
            AssignedCustomerPickUpRef!!.addValueEventListener(object : ValueEventListener {
                public override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        val customerLocationMap: List<Any?>? =
                            dataSnapshot.value as List<Any?>?
                        var LocationLat: Double = 0.0
                        var LocationLng: Double = 0.0
                        if (customerLocationMap!![0] != null) {
                            LocationLat = customerLocationMap[0].toString().toDouble()
                        }
                        if (customerLocationMap[1] != null) {
                            LocationLng = customerLocationMap[1].toString().toDouble()
                        }
                        val DriverLatLng: LatLng = LatLng(LocationLat, LocationLng)
                        PickUpMarker = mMap!!.addMarker(
                            MarkerOptions().position(DriverLatLng)
                                .title("Customer BreakDown Location")
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.user))
                        )
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {}
            })
    }

    public override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // now let set user location enable
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        buildGoogleApiClient()
        mMap!!.isMyLocationEnabled = true
    }

    public override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onCreate(savedInstanceState, persistentState)
    }

    public override fun onLocationChanged(location: Location) {
        if (applicationContext != null) {
            //getting the updated location
            LastLocation = location
            val latLng: LatLng = LatLng(location.latitude, location.longitude)
            mMap!!.moveCamera(CameraUpdateFactory.newLatLng(latLng))
            mMap!!.animateCamera(CameraUpdateFactory.zoomTo(12f))
            val userID: String = FirebaseAuth.getInstance().getCurrentUser().getUid()
            val DriversAvailabilityRef: DatabaseReference =
                FirebaseDatabase.getInstance().getReference().child("Drivers Available")
            val geoFireAvailability: GeoFire = GeoFire(DriversAvailabilityRef)
            val DriversWorkingRef: DatabaseReference =
                FirebaseDatabase.getInstance().getReference().child("Drivers Working")
            val geoFireWorking: GeoFire = GeoFire(DriversWorkingRef)
            when (customerID) {
                "" -> {
                    geoFireWorking.removeLocation(userID)
                    geoFireAvailability.setLocation(
                        userID,
                        GeoLocation(location.getLatitude(), location.getLongitude())
                    )
                }
                else -> {
                    geoFireAvailability.removeLocation(userID)
                    geoFireWorking.setLocation(
                        userID,
                        GeoLocation(location.getLatitude(), location.getLongitude())
                    )
                }
            }
        }
    }

    public override fun onConnected(bundle: Bundle?) {
        locationRequest = LocationRequest()
        locationRequest!!.interval = 1000
        locationRequest!!.fastestInterval = 1000
        locationRequest!!.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            //
            return
        }
        //it will handle the refreshment of the location
        //if we dont call it we will get location only once
        LocationServices.FusedLocationApi.requestLocationUpdates(
            googleApiClient,
            locationRequest,
            this
        )
    }

    public override fun onConnectionSuspended(i: Int) {}
    public override fun onConnectionFailed(connectionResult: ConnectionResult) {}

    //create this method -- for useing apis
    @Synchronized
    protected fun buildGoogleApiClient() {
        googleApiClient = GoogleApiClient.Builder(this)
            .addConnectionCallbacks(this)
            .addOnConnectionFailedListener(this)
            .addApi(LocationServices.API)
            .build()
        googleApiClient?.connect()
    }

    override fun onStop() {
        super.onStop()
        if (!currentLogOutUserStatus) {
            DisconnectDriver()
        }
    }

    private fun DisconnectDriver() {
        val userID: String = FirebaseAuth.getInstance().getCurrentUser().getUid()
        val DriversAvailabiltyRef: DatabaseReference =
            FirebaseDatabase.getInstance().getReference().child("Drivers Available")
        val geoFire: GeoFire = GeoFire(DriversAvailabiltyRef)
        geoFire.removeLocation(userID)
    }

    fun LogOutUser() {
        val startPageIntent: Intent = Intent(this@MechanicMapUi, WelcomeActivity::class.java)
        startPageIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(startPageIntent)
        finish()
    }

    private val assignedCustomerInformation: Unit
        private get() {
            val reference: DatabaseReference = FirebaseDatabase.getInstance().getReference()
                .child("Users").child("Customers").child(customerID)
            reference.addValueEventListener(object : ValueEventListener {
                public override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists() && dataSnapshot.childrenCount > 0) {
                        val name: String = dataSnapshot.child("name").value.toString()
                        val phone = dataSnapshot.child("phone").value.toString()
                        txtName!!.text = name
                        txtPhone!!.text = phone
                        if (dataSnapshot.hasChild("image")) {
                            val image: String = dataSnapshot.child("image").value.toString()
                            Picasso.get().load(image).into(profilePic)
                        }
                    }
                }

                public override fun onCancelled(databaseError: DatabaseError) {}
            })
        }
}