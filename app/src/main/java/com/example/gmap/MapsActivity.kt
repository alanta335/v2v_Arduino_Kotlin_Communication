package com.example.gmap

import android.annotation.SuppressLint
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.RequestQueue
import com.android.volley.toolbox.Volley
import com.example.gmap.databinding.ActivityMapsBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

class MapsActivity : AppCompatActivity(), OnMapReadyCallback ,
    GoogleMap.OnMarkerClickListener {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private lateinit var lastLocation: Location
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var queue: RequestQueue
    private var accident = false
    private var block = false
    private lateinit var faba: View
    private lateinit var fabb: View
    private lateinit var volleyRequest : volleyRequestHandler

    var listener: VolleyResponseListener = object : VolleyResponseListener {

        override fun onSuccess(url: String, json: JSONObject) {
            Log.d("VolleyResponseListener", "got message from $url")
            Log.d("VolleyResponseListener", json.toString())
        }
        override fun onSuccess(url: String, jsonArray: JSONArray) {
            Log.d("VolleyResponseListener", "got message from $url")
            //Log.d("VolleyResponseListener", jsonArray.toString())

            if(url == "/read") markLocations(jsonArray)
        }
        override fun onFail(url: String, error : String) {
            Log.d("VolleyResponseListener Error", url + error)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        volleyRequest = volleyRequestHandler(this)
    }

    /**
     * Manipulates the map once available.
     */

    @SuppressLint("MissingPermission")
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.uiSettings.isZoomControlsEnabled = true
        mMap.setOnMarkerClickListener(this)
        setUpMap()
        CoroutineScope(Dispatchers.Default).launch {
            while (true) {
                var updatePos = false
                var currentPos : LatLng
                fusedLocationClient.lastLocation
                    .addOnSuccessListener { location : Location? ->
                        if (location != null) {
                            currentPos = LatLng(location.latitude, location.longitude)
                            updatePos = true
                            val ud = intent.getStringExtra("accountid").toString()
                            val obj = JSONObject()
                            obj.put("latitude", currentPos.latitude)
                            obj.put("longitude",currentPos.longitude)
                            obj.put("accident",accident)
                            obj.put("block",block)
                            obj.put("id", ud)
                            volleyRequest.volleyPostRequest("/updatedataL", obj, listener)
                            mMap.clear()
                            //delay(5000)
                            updateOtherVehicleLocation()
                        }
                    }
                delay(1000)

            }
        }
        updateDataL()


    }

    @SuppressLint("MissingPermission")
    private fun setUpMap() {

       // queue = Volley.newRequestQueue(this)
        mMap.isMyLocationEnabled = true
        fusedLocationClient.lastLocation.addOnSuccessListener(this){location ->
            if(location != null){
                lastLocation = location
                val currentLatLong = LatLng(location.latitude,location.longitude)
                val ud = intent.getStringExtra("accountid").toString()
                val obj = JSONObject()
                obj.put("latitude",location.latitude)
                obj.put("longitude",location.longitude)
                obj.put("accident",accident)
                obj.put("block",block)
                obj.put("id",ud)

                volleyRequest.volleyPostRequest("/addfirstdata" , obj, listener)
                placeMarkerOnMap(currentLatLong)
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLong, 22f))

            }
        }

    }
    @SuppressLint("MissingPermission")
    private fun updateDataL(){
        faba = findViewById(R.id.fabaccident)
        faba.setOnClickListener {
            accident = true
            }
        fabb = findViewById(R.id.fabblock)
        fabb.setOnClickListener {
                block = true
        }


        fusedLocationClient.lastLocation.addOnSuccessListener(this) { location ->
                if (location != null) {
                    lastLocation = location


                }
            }
    }

    private fun placeMarkerOnMap(currentLatLong: LatLng) {
        val markerOptions = MarkerOptions().position(currentLatLong)
        markerOptions.title("$currentLatLong")
        mMap.addMarker(markerOptions)
    }
    private fun markLocations(jArray : JSONArray)
    {
        (0 until jArray.length()).forEach {
            val res = jArray.getJSONObject(it)
            placeMarkerOnMap(LatLng(res.get("latitude") as Double, res.get("longitude") as Double))

        }

    }
    private fun updateOtherVehicleLocation() {
        volleyRequest.volleyGetRequest("/read" , null , listener)
    }
    override fun onMarkerClick(p0: Marker) = false



}
