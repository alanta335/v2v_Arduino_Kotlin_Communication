package com.example.gmap

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.app.ActivityCompat
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.example.gmap.databinding.ActivityMapsBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.Marker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONObject


class MapsActivity : AppCompatActivity(), OnMapReadyCallback,
    GoogleMap.OnMarkerClickListener {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private lateinit var lastLocation: Location
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var marker: Marker


    companion object {
        private const val LOCATION_REQUEST_CODE = 1
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
    }

    /**
     * Manipulates the map once available.
     */

    override fun onMapReady(googleMap: GoogleMap) {

        mMap = googleMap
        mMap.uiSettings.isZoomControlsEnabled = true
        mMap.setOnMarkerClickListener(this)
        setUpMap()

        updateDataL()
        //Log.d("hi","comp")

    }


    @SuppressLint("MissingPermission")
    private fun setUpMap() {

        mMap.isMyLocationEnabled = true
        fusedLocationClient.lastLocation.addOnSuccessListener(this){location ->
            if(location != null){
                lastLocation = location
                var currentLatLong = LatLng(location.latitude,location.longitude)
                var ud = intent.getStringExtra("accountid").toString();
                val obj = JSONObject()
                obj.put("location",currentLatLong.toString())
                obj.put("accident",false)
                obj.put("block",false)
                obj.put("id",ud)

                val url = "https://4d0c-111-92-117-51.in.ngrok.io/addfirstdata"
                val queue = Volley.newRequestQueue(this)
                val jsonObjectRequest = JsonObjectRequest(
                    Request.Method.POST, url, obj,
                    Response.Listener { response ->
                        Log.d("response",response.toString());
                    },
                    Response.ErrorListener { error ->
                        Log.d("error",error.localizedMessage)
                    }
                )
                queue.add(jsonObjectRequest)

                placeMarkerOnMap(currentLatLong)
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLong, 22f))



            }
        }

    }
    @SuppressLint("MissingPermission")
    private fun updateDataL(){

            fusedLocationClient.lastLocation.addOnSuccessListener(this) { location ->
                if (location != null) {
                    lastLocation = location
                    val url = "https://4d0c-111-92-117-51.in.ngrok.io/updatedataL"
                    val queue = Volley.newRequestQueue(this)
                    var currentLatLong = LatLng(location.latitude, location.longitude)

                    CoroutineScope(Dispatchers.Main).launch {

                    while (true) {
                        currentLatLong = LatLng(location.latitude, location.longitude)
                        var ud = intent.getStringExtra("accountid").toString();
                        val obj = JSONObject()
                        obj.put("location", currentLatLong.toString())
                        obj.put("id", ud)


                        val jsonObjectRequest = JsonObjectRequest(
                            Request.Method.POST, url, obj,
                            Response.Listener { response ->
                                Log.d("response updated from coro", response.toString());
                            },
                            Response.ErrorListener { error ->
                                Log.d("error coro", error.localizedMessage)
                            }
                        )
                        queue.add(jsonObjectRequest)
                        //marker.remove()
                        mMap.clear()

                        delay(5000)

                        placeMarkerOnMap(currentLatLong)
                        delay(3000)

                    }

                    }
                }
            }
    }
    private fun placeMarkerOnMap(currentLatLong: LatLng) {
        val markerOptions = MarkerOptions().position(currentLatLong);
        markerOptions.title("$currentLatLong")

        marker = mMap.addMarker(markerOptions)!!
    }
    override fun onMarkerClick(p0: Marker) = false
}
