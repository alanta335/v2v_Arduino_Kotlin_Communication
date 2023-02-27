package com.example.gmap

import android.annotation.SuppressLint
import android.location.Location
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.JsonObjectRequest
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


class MapsActivity : AppCompatActivity(), OnMapReadyCallback,
    GoogleMap.OnMarkerClickListener {
   // var markersArray: ArrayList<MarkerOptions> = ArrayList()
  //  var locationArray : ArrayList<LatLng> =  ArrayList()
    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private lateinit var lastLocation: Location
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var queue : RequestQueue


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


    }


    @SuppressLint("MissingPermission")
    private fun setUpMap() {
        queue = Volley.newRequestQueue(this)
        mMap.isMyLocationEnabled = true
        fusedLocationClient.lastLocation.addOnSuccessListener(this){location ->
            if(location != null){
                lastLocation = location
                val currentLatLong = LatLng(location.latitude,location.longitude)
                val ud = intent.getStringExtra("accountid").toString()
                val obj = JSONObject()
                obj.put("latitude",location.latitude)
                obj.put("longitude",location.longitude)

                obj.put("accident",false)
                obj.put("block",false)
                obj.put("id",ud)
                val url = resources.getString(R.string.nodeJSBackend_IP)
                val jsonObjectRequest = JsonObjectRequest(
                    Request.Method.POST, url, obj,
                    Response.Listener { response ->
                        Log.d("response",response.toString())
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
                    val url = resources.getString(R.string.nodeJSBackend_IP)
                    var currentLatLong = LatLng(location.latitude, location.longitude)

                    CoroutineScope(Dispatchers.Main).launch {

                    while (true) {
                        currentLatLong = LatLng(location.latitude, location.longitude)
                        val ud = intent.getStringExtra("accountid").toString()
                        val obj = JSONObject()
                        obj.put("latitude",location.latitude)
                        obj.put("longitude",location.longitude)
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
                        //delay(5000)
                        updateOtherVehicleLocation()
                        delay(3000)

                    }

                    }
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
        val url = resources.getString(R.string.nodeJSBackend_IP)
        val jsonArrayRequest = JsonArrayRequest(
            Request.Method.GET, url, null,
            Response.Listener { response ->
                Log.d("read all data", response.toString())
                markLocations(response)
            },
            Response.ErrorListener { error ->
                Log.d("error read all data", error.localizedMessage)
            }
        )
        queue.add(jsonArrayRequest)
    }
    override fun onMarkerClick(p0: Marker) = false
}
