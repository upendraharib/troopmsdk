package com.tvisha.trooponprime.ui.recent.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.*
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.provider.Settings
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsStatusCodes
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.net.PlacesClient
import com.tvisha.trooponprime.R
import com.tvisha.trooponprime.constants.Constants
import com.tvisha.trooponprime.databinding.ActivityAddressPickerBinding
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLConnection
import java.util.Locale
import javax.net.ssl.HttpsURLConnection

class LocationPickerActivity:AppCompatActivity() , OnMapReadyCallback,
    GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
        var mLocationManager: LocationManager? = null
        private var mFusedLocationClient: FusedLocationProviderClient? = null
        var latitude = 0.0
        var longitude:kotlin.Double? = 0.0
        private val LOCATION_RESULT = 200
        var mLastLocation: Location? = null
        var mGoogleMap: GoogleMap? = null
        var placesClient : PlacesClient? = null

    lateinit var dataBinding:ActivityAddressPickerBinding
        protected var mGoogleApiClient: GoogleApiClient? = null
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            dataBinding = DataBindingUtil.setContentView(this,R.layout.activity_address_picker)
            setUpTheMap()

        }

        var previousWidths = 0f
        var previousHeights = 0f

        fun setUpTheMap(){
            mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
            mLocationManager = getSystemService(LOCATION_SERVICE) as LocationManager
            buildGoogleApiClient()
            createLocationRequest()
            isEnableLocationService()
            val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
            mapFragment.getMapAsync(this)

        }
        private fun getDataFromUrl(demoIdUrl: String): String? {
            var error:String? = null
            var result: String? = null
            val resCode: Int
            val `in`: InputStream
            try {
                val url = URL(demoIdUrl)
                val urlConn: URLConnection = url.openConnection()
                val httpsConn: HttpsURLConnection = urlConn as HttpsURLConnection
                httpsConn.setAllowUserInteraction(false)
                httpsConn.setInstanceFollowRedirects(true)
                httpsConn.setRequestMethod("GET")
                httpsConn.connect()
                resCode = httpsConn.getResponseCode()
                if (resCode == HttpURLConnection.HTTP_OK) {
                    `in` = httpsConn.getInputStream()
                    val reader = BufferedReader(
                        InputStreamReader(
                            `in`, "iso-8859-1"
                        ), 8
                    )
                    val sb = java.lang.StringBuilder()
                    var line: String?
                    while (reader.readLine().also { line = it } != null) {
                        sb.append(line).append("\n")
                    }
                    `in`.close()
                    result = sb.toString()
                } else {
                    error += resCode
                }
            } catch (e: IOException) {
                //Helper.printExceptions(e)
            }
            return result
        }
        fun apiCall(search:String){
            var data = getDataFromUrl(search);
            /*val queue = Volley.newRequestQueue(this)
            val url = "https://google.com/maps/search/$search"
            val request = JsonObjectRequest(url, null,
                object : Response.Listener<String?> {
                    override fun onResponse(response: String) {
                        if (null != response) {
                            try {
                                //handle your response
                            } catch (e: JSONException) {
                                Helper.printExceptions(e)
                            }
                        }
                    }
                }, object : Response.ErrorListener {
                   override fun onErrorResponse(error: VolleyError?) {
                   }
                })
            queue.add(request)*/
        }


        override fun onResume() {
            super.onResume()
        }
        private fun createLocationRequest() {
            val locationRequest = com.google.android.gms.location.LocationRequest.create()
            locationRequest.interval = 10000
            locationRequest.fastestInterval = 5000
            locationRequest.priority = com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        private fun buildGoogleApiClient() {
            mGoogleApiClient = GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build()
        }
        private fun isEnableLocationService() {
            val lm = applicationContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            var gps_enabled = false
            var network_enabled = false
            mGoogleApiClient = GoogleApiClient.Builder(this)
                .addApi(LocationServices.API).build()
            mGoogleApiClient!!.connect()
            try {
                assert(lm != null)
                gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER)
                network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
            } catch (ex: Exception) {
                //Helper.printExceptions(ex)
            }
            if (!gps_enabled && !network_enabled) {
                val locationRequest = com.google.android.gms.location.LocationRequest.create()
                locationRequest.priority = com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY
                locationRequest.interval = (30 * 1000).toLong()
                locationRequest.fastestInterval = (5 * 1000).toLong()
                val builder = LocationSettingsRequest.Builder()
                    .addLocationRequest(locationRequest)

                //**************************
                builder.setAlwaysShow(true) //this is the key ingredient
                //**************************
                val result = LocationServices.SettingsApi.checkLocationSettings(
                    mGoogleApiClient!!,
                    builder.build()
                )
                result.setResultCallback { result ->
                    val status = result.status
                    val state = result.locationSettingsStates
                    when (status.statusCode) {
                        LocationSettingsStatusCodes.SUCCESS -> {}
                        LocationSettingsStatusCodes.RESOLUTION_REQUIRED ->                             // Location settings are not satisfied. But could be fixed by showing the user
                            // a dialog.
                            try {
                                // Show the dialog by calling startResolutionForResult(),
                                // and check the result in onActivityResult().
                                status.startResolutionForResult(
                                    this,
                                    LOCATION_RESULT
                                )
                            } catch (e: IntentSender.SendIntentException) {
                                // Ignore the error.
                            }
                        LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> {}
                    }
                }
            } else {
                if (Constants.checkLocationPermissions(this)) {
                    currentLocation()
                } else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        requestPermissions(
                            Constants.LOCATION_PERMISSION,
                            Constants.LOCATION_PERMISSION_CALLBACK
                        )
                    }
                }
            }
        }
        override fun onMapReady(googleMap: GoogleMap) {
            mGoogleMap = googleMap

            mGoogleMap!!.setOnCameraMoveStartedListener(GoogleMap.OnCameraMoveStartedListener { })
            mGoogleMap!!.setOnCameraIdleListener(GoogleMap.OnCameraIdleListener {
                val position: LatLng = googleMap.getCameraPosition().target
                latitude = position.latitude
                longitude = position.longitude
                setTheAddress()
            })
            setTheMap()
        }
        private fun currentLocation() {
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
            try {
                mLocationManager!!.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    10000,
                    0f,
                    mLocationListener
                )
                mFusedLocationClient!!.lastLocation.addOnSuccessListener(
                    this
                ) { location ->
                    var location = location
                    if (location != null) {
                        try {
                            latitude = location.latitude
                            longitude = location.longitude
                            setTheMap()
                        } catch (ignored: Exception) {
                        }
                    } else {
                        location = getLastKnownLocation()
                        if (location != null) {
                            latitude = location.latitude
                            longitude = location.longitude
                            setTheMap()
                        }else{
                            Thread{
                                Thread.sleep(2000)
                                runOnUiThread {
                                    currentLocation()
                                }
                            }.start()
                        }
                    }
                }
            } catch (e: Exception) {
            }
        }
        private fun getLastKnownLocation(): Location? {
            val providers = mLocationManager!!.getProviders(true)
            var bestLocation: Location? = null
            for (provider in providers) {
                if (ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    return null
                }
                val l = mLocationManager!!.getLastKnownLocation(provider) ?: continue
                if (bestLocation == null
                    || l.accuracy < bestLocation.accuracy
                ) {
                    bestLocation = l
                }
            }
            return bestLocation
        }
        var ischeckTheNeverAskAgain = false



        private val mLocationListener: LocationListener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                try {
                    longitude = location.longitude
                    longitude = location.longitude
                    setTheMap()
                } catch (e: Exception) {
                }
            }

            override fun onStatusChanged(s: String, i: Int, bundle: Bundle) {}
            override fun onProviderEnabled(s: String) {}
            override fun onProviderDisabled(s: String) {}
        }
        private fun setTheMap() {
            val latLng = LatLng(latitude, longitude!!)
            val center = CameraUpdateFactory.newLatLng(latLng)
            val zoom = CameraUpdateFactory.zoomTo(10.0f)
            mGoogleMap!!.moveCamera(zoom)
            mGoogleMap!!.moveCamera(center)
            setTheAddress()
        }

        var mAddress: Address? = null
        private fun setTheAddress() {
            runOnUiThread {
                try {
                    val geo = Geocoder(
                        this,
                        Locale.getDefault()
                    )
                    dataBinding.selectedCordinates.text = StringBuilder().append(latitude).append(",").append(longitude)
                    val addresses: List<*> = /*if (Build.VERSION.SDK_INT >= 33) {
                    listOf(geo.getFromLocation(latitude, longitude!!, 1, Geocoder.GeocodeListener {

                    }))!!
                } else {

                }*/
                        geo.getFromLocation(latitude, longitude!!, 1)!!
                    if (addresses.isEmpty()) {
                        dataBinding.selectedAddress.text = "Waiting for Location" as CharSequence
                    } else if (addresses.isNotEmpty()) {
                        mAddress = addresses[0] as Address?
                        dataBinding.selectedAddress.text = mAddress!!.getAddressLine(0) as CharSequence
                    }
                } catch (e: IOException) {
                   // Helper.printExceptions(e)
                }
            }
        }

        @SuppressLint("MissingPermission")
        override fun onConnected(p0: Bundle?) {
            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient!!)
            if (mLastLocation != null) {
                latitude = mLastLocation!!.latitude
                longitude = mLastLocation!!.longitude
                setTheMap()
            }
        }

        override fun onConnectionSuspended(p0: Int) {
        }
        override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
            super.onActivityResult(requestCode, resultCode, data)
            when (requestCode) {
                Constants.LOCATION_PERMISSION_CALLBACK -> if (Constants.checkLocationPermissions(this)) {
                    currentLocation()
                } else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        requestPermissions(
                            Constants.LOCATION_PERMISSION,
                            Constants.LOCATION_PERMISSION_CALLBACK
                        )
                    }
                }
                LOCATION_RESULT -> if (Constants.checkLocationPermissions(this)) {
                    currentLocation()
                } else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        requestPermissions(
                            Constants.LOCATION_PERMISSION,
                            Constants.LOCATION_PERMISSION_CALLBACK
                        )
                    }
                }
            }
        }

        override fun onConnectionFailed(p0: ConnectionResult) {

        }

        fun fetchCurrentLocation(view: View) {
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
            try {
                mLocationManager!!.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    10000,
                    0f,
                    mLocationListener
                )
                mFusedLocationClient!!.lastLocation.addOnSuccessListener(
                    this
                ) { location ->
                    var location = location
                    if (location != null) {
                        try {
                            latitude = location.latitude
                            longitude = location.longitude
                            setTheMap()
                        } catch (ignored: java.lang.Exception) {
                        }
                    } else {
                        location = getLastKnownLocation()
                        if (location != null) {
                            latitude = location.latitude
                            longitude = location.longitude
                            setTheMap()
                        }
                    }
                }
            } catch (e: java.lang.Exception) {
                //Helper.printExceptions(e)
            }
        }

        override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<out String>,
            grantResults: IntArray
        ) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
            if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                currentLocation()
            }
        }

        fun sendTheLocation(view: View) {
            val intent = Intent()
            intent.putExtra("address", mAddress as Parcelable?)
            setResult(-1, intent)
            finish()
        }


}