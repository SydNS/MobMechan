@file:Suppress("DEPRECATION")

package com.example.mobmechan

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.fragment.app.FragmentActivity
import com.firebase.geofire.GeoFire
import com.firebase.geofire.GeoLocation
import com.firebase.geofire.GeoQuery
import com.firebase.geofire.GeoQueryEventListener
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
import java.util.*

class UserMapUi  : FragmentActivity(), OnMapReadyCallback,
    GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
    LocationListener {
    private var mMap: GoogleMap? = null
    var googleApiClient: GoogleApiClient? = null
    var LastLocation: Location? = null
    var locationRequest: LocationRequest? = null
    private var Logout: Button? = null
    private var SettingsButton: Button? = null
    private var CallCabCarButton: Button? = null
    private var callingbtn: ImageView? = null
    private var mAuth: FirebaseAuth? = null
    private var currentUser: FirebaseUser? = null
    private var CustomerDatabaseRef: DatabaseReference? = null
    private var CustomerPickUpLocation: LatLng? = null
    private var DriverAvailableRef: DatabaseReference? = null
    private var DriverLocationRef: DatabaseReference? = null
    private var DriversRef: DatabaseReference? = null
    private var radius: Int = 1
    private var driverFound: Boolean? = false
    private var requestType: Boolean = false
    private var driverFoundID: String? = null
    private var customerID: String? = null
    var DriverMarker: Marker? = null
    var PickUpMarker: Marker? = null
    var geoQuery: GeoQuery? = null
    private var DriverLocationRefListner: ValueEventListener? = null
    private var txtName: TextView? = null
    private var txtPhone: TextView? = null
    private var txtCarName: TextView? = null
    private var profilePic: CircleImageView? = null
    private var relativeLayout: RelativeLayout? = null
//    lateinit var phone: String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_customers_map)
        mAuth = FirebaseAuth.getInstance()
        currentUser = mAuth!!.getCurrentUser()
        customerID = FirebaseAuth.getInstance().getCurrentUser().getUid()
        CustomerDatabaseRef =
            FirebaseDatabase.getInstance().getReference().child("Customer Requests")
        DriverAvailableRef =
            FirebaseDatabase.getInstance().getReference().child("Drivers Available")
        DriverLocationRef = FirebaseDatabase.getInstance().getReference().child("Drivers Working")
        Logout = findViewById<View>(R.id.logout_customer_btn) as Button?
        SettingsButton = findViewById<View>(R.id.settings_customer_btn) as Button?
        CallCabCarButton = findViewById<View>(R.id.call_a_car_button) as Button?
        txtName = findViewById(R.id.name_driver)
        txtPhone = findViewById(R.id.phone_driver)
        txtCarName = findViewById(R.id.car_name_driver)
        profilePic = findViewById(R.id.profile_image_driver)
        relativeLayout = findViewById(R.id.rel1)
        callingbtn = findViewById(R.id.callingbtn)



        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment: SupportMapFragment? = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment!!.getMapAsync(this)
        SettingsButton!!.setOnClickListener {
            val intent: Intent = Intent(this@UserMapUi, SettingsActivity::class.java)
            intent.putExtra("type", "Customers")
            startActivity(intent)
        }
        Logout!!.setOnClickListener {
            mAuth!!.signOut()
            LogOutUser()
        }
        CallCabCarButton!!.setOnClickListener {
            if (requestType) {
                requestType = false
                geoQuery!!.removeAllListeners()
                DriverLocationRef!!.removeEventListener((DriverLocationRefListner)!!)
                if (driverFound != null) {
                    DriversRef = FirebaseDatabase.getInstance().reference
                        .child("Users").child("Drivers").child((driverFoundID)!!)
                        .child("CustomerRideID")
                    DriversRef!!.removeValue()
                    driverFoundID = null
                }
                driverFound = false
                radius = 1
                val customerId: String = FirebaseAuth.getInstance().getCurrentUser().uid
                val geoFire: GeoFire = GeoFire(CustomerDatabaseRef)
                geoFire.removeLocation(customerId)
                if (PickUpMarker != null) {
                    PickUpMarker!!.remove()
                }
                if (DriverMarker != null) {
                    DriverMarker!!.remove()
                }
                CallCabCarButton!!.setText("Call a Cab")
                relativeLayout?.setVisibility(View.GONE)
            } else {
                requestType = true
                val customerId: String = FirebaseAuth.getInstance().getCurrentUser().getUid()
                val geoFire: GeoFire = GeoFire(CustomerDatabaseRef)
                geoFire.setLocation(
                    customerId,
                    GeoLocation(LastLocation!!.latitude, LastLocation!!.longitude)
                )
                CustomerPickUpLocation =
                    LatLng(LastLocation!!.latitude, LastLocation!!.longitude)
                PickUpMarker = mMap!!.addMarker(
                    MarkerOptions().position(CustomerPickUpLocation!!).title("Your Location")
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.user))
                )
                CallCabCarButton!!.text = "Kindly Wait as We get you a Mechanic..."
                closetDriverCab
            }
        }

        callingbtn!!.setOnClickListener {
            val intent = Intent(Intent.ACTION_CALL, Uri.parse("tel:0701315149"))
            startActivity(intent)
        }
    }//we tell driver which customer he is going to have

    //Show driver location on customerMapActivity
    //anytime the driver is called this method will be called
    //key=driverID and the location
    private val closetDriverCab: Unit
        private get() {
            val geoFire: GeoFire = GeoFire(DriverAvailableRef)
            geoQuery = geoFire.queryAtLocation(
                GeoLocation(
                    CustomerPickUpLocation!!.latitude,
                    CustomerPickUpLocation!!.longitude
                ), radius.toDouble()
            )
            geoQuery?.removeAllListeners()
            geoQuery?.addGeoQueryEventListener(object : GeoQueryEventListener {
                public override fun onKeyEntered(key: String, location: GeoLocation) {
                    //anytime the driver is called this method will be called
                    //key=driverID and the location
                    if (!driverFound!! && requestType) {
                        driverFound = true
                        driverFoundID = key


                        //we tell driver which customer he is going to have
                        DriversRef = FirebaseDatabase.getInstance().getReference().child("Users")
                            .child("Drivers").child(
                                driverFoundID!!
                            )
                        val driversMap = HashMap<String?, String?>()
                        driversMap["CustomerRideID"] = customerID
                        DriversRef!!.updateChildren(driversMap as Map<String, Any>)

                        //Show driver location on customerMapActivity
                        GettingDriverLocation()
                        CallCabCarButton!!.setText("Looking for Mechanic Location...")
                    }
                }

                public override fun onKeyExited(key: String) {}
                public override fun onKeyMoved(key: String, location: GeoLocation) {}
                public override fun onGeoQueryReady() {
                    if (!driverFound!!) {
                        radius += 1
                        closetDriverCab
                    }
                }

                override fun onGeoQueryError(error: DatabaseError) {}
            })
        }

    //and then we get to the driver location - to tell customer where is the driver
    private fun GettingDriverLocation() {
        DriverLocationRefListner = DriverLocationRef!!.child((driverFoundID)!!).child("l")
            .addValueEventListener(object : ValueEventListener {
                public override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists() && requestType) {
                        val driverLocationMap: List<Any?>? = dataSnapshot.value as List<Any?>?
                        var LocationLat: Double = 0.0
                        var LocationLng: Double = 0.0
                        CallCabCarButton!!.text = "Mechanic Found"
                        relativeLayout!!.visibility = View.VISIBLE
                        assignedDriverInformation
                        if (driverLocationMap!!.get(0) != null) {
                            LocationLat = driverLocationMap[0].toString().toDouble()
                        }
                        if (driverLocationMap.get(1) != null) {
                            LocationLng = driverLocationMap[1].toString().toDouble()
                        }

                        //adding marker - to pointing where driver is - using this lat lng
                        val DriverLatLng: LatLng = LatLng(LocationLat, LocationLng)
                        if (DriverMarker != null) {
                            DriverMarker!!.remove()
                        }
                        val location1: Location = Location("")
                        location1.setLatitude(CustomerPickUpLocation!!.latitude)
                        location1.setLongitude(CustomerPickUpLocation!!.longitude)
                        val location2: Location = Location("")
                        location2.setLatitude(DriverLatLng.latitude)
                        location2.setLongitude(DriverLatLng.longitude)
                        val Distance: Float = location1.distanceTo(location2)
                        if (Distance < 90) {
                            CallCabCarButton!!.setText("Mechanic has Reached")
                        } else {
                            CallCabCarButton!!.text = "Mechanic is  $Distance.toString() away"
                        }
                        DriverMarker = mMap!!.addMarker(
                            MarkerOptions().position(DriverLatLng).title("your Mechanic is here")
                                .icon(BitmapDescriptorFactory.defaultMarker())
                        )
                    }
                }

                public override fun onCancelled(databaseError: DatabaseError) {}
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
        mMap!!.setMyLocationEnabled(true)
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
    public override fun onLocationChanged(location: Location) {
        //getting the updated location
        LastLocation = location
        val latLng: LatLng = LatLng(location.latitude, location.longitude)
        mMap!!.moveCamera(CameraUpdateFactory.newLatLng(latLng))
        mMap!!.animateCamera(CameraUpdateFactory.zoomTo(12f))
    }

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
    }

    fun LogOutUser() {
        val startPageIntent: Intent = Intent(this@UserMapUi, WelcomeActivity::class.java)
        startPageIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(startPageIntent)
        finish()
    }

    private val assignedDriverInformation: Unit
        private get() {
            val reference: DatabaseReference = FirebaseDatabase.getInstance().reference
                .child("Users").child("Drivers").child((driverFoundID)!!)
            reference.addValueEventListener(object : ValueEventListener {
                public override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists() && dataSnapshot.childrenCount > 0) {
                        val name: String = dataSnapshot.child("name").value.toString()
                        val phone = dataSnapshot.child("phone").value.toString()
                        val car: String = dataSnapshot.child("car").value.toString()
                        txtName!!.text = name
                        txtPhone!!.text = phone
                        txtCarName!!.text = car
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