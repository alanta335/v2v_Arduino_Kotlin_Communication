package com.example.gmap

import android.annotation.SuppressLint
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.gmap.databinding.ActivityMapsBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

class MapsActivity : AppCompatActivity(), OnMapReadyCallback ,
    GoogleMap.OnMarkerClickListener {
    private lateinit var crowdDetection : FloatingActionButton
    private lateinit var accidentDetection : FloatingActionButton
    private lateinit var nearbyDetection : FloatingActionButton
    private var crowdDetected = 0;
    private var accidentDetected = 0;
    private var nearbyDetected = 0;
    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private lateinit var lastLocation: Location
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var accident = false
    private var block = false
    private var markerMap : HashMap<String , Marker> = HashMap()
    private var nearbyMap: HashMap<String, Marker> = HashMap()
    private lateinit var faba: View

    private lateinit var volleyRequest : volleyRequestHandler
    private var circle : ArrayList<Circle> = ArrayList()
    var listener: VolleyResponseListener = object : VolleyResponseListener {

        override fun onSuccess(url: String, json: JSONObject) {
            Log.d("VolleyResponseListener", "got message from $url")
            Log.d("VolleyResponseListener", json.toString())
        }
        override fun onSuccess(url: String, jsonArray: JSONArray) {
            Log.d("VolleyResponseListener", "got message from $url")
            //Log.d("VolleyResponseListener", jsonArray.toString())

            if(url == "/read") markLocations(jsonArray, markerMap)
            if(url == "/readNearby") markLocations(jsonArray, nearbyMap)
            if(url == "/cluster"){
                (0 until jsonArray.length()).forEach {
                    val jsonObj = jsonArray.getJSONObject(it)
                    val centroidx = jsonObj.getDouble("centroidx")
                    val centroidy = jsonObj.getDouble("centroidy")
                    val radius = jsonObj.getDouble("radius")
                    drawCircle(centroidx, centroidy, radius)
                }
            }
        }
        override fun onFail(url: String, error : String) {
            Log.d("VolleyResponseListener Error", url + error)
        }
    }
    private fun drawCircle(latitude: Double, longitude: Double, radius: Double) {
        val rdx :Double?
        if(radius != 0.00) {
            rdx = radius * 130000
        }
        else rdx = 0.00


        Log.d("radius = ", rdx.toString())

        val circleOptions = CircleOptions()
            .center(LatLng(latitude, longitude))
            .radius(rdx)
            .strokeWidth(1.0f)
            .strokeColor(ContextCompat.getColor(this, R.color.purple_500))
            .fillColor(ContextCompat.getColor(this, R.color.teal_200))
        val ncircle = mMap.addCircle(circleOptions)
        circle.add(ncircle);

    }
    private fun clearCircle(){
        circle.forEach {
            it.remove()
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
        faba = findViewById(R.id.fabaccident)
        faba.setOnClickListener {
            accident = true
        }

        crowdDetection = findViewById(R.id.check_block)
            accidentDetection = findViewById(R.id.fabaccident)
            nearbyDetection = findViewById(R.id.nearby)

        crowdDetection.setOnClickListener {
            if (crowdDetected == 0){
                volleyRequest.volleyGetRequest("/cluster", null, listener)
                crowdDetected = 1
            }else{
                clearCircle()
                crowdDetected = 0;

            }
        }
            accidentDetection.setOnClickListener {
                if (accidentDetected == 0){
                    accident = true
                    accidentDetected = 1
                }else{
                    accident = false
                    accidentDetected = 0;
                }
            }

            nearbyDetection.setOnClickListener {
                if (nearbyDetected == 0){
                    volleyRequest.volleyGetRequest("/readNearby", null  ,listener)
                    nearbyDetected = 1
                    Log.d("Nearby" , nearbyDetected.toString())
                }
                else{
                    nearbyMap.forEach(){
                            it.value.remove()
                    }
                    nearbyDetected = 0;
                    Log.d("Nearby", nearbyDetected.toString())
                }
            }
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
//                            mMap.clear()
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
                markerMap.put(intent.getStringExtra("accountid")!!,placeMarkerOnMap(currentLatLong,ambulance = false,acc=false,type = null))
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLong, 22f))

            }
        }

    }
    @SuppressLint("MissingPermission")
    private fun updateDataL(){
        fusedLocationClient.lastLocation.addOnSuccessListener(this) { location ->
                if (location != null) {
                    lastLocation = location
                }
            }
    }

    private fun placeMarkerOnMap(currentLatLong: LatLng,ambulance:Boolean?,acc:Boolean?, type: String?) : Marker {
        val markerOptions = MarkerOptions().position(currentLatLong)
        if (ambulance == true){
            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(
                BitmapDescriptorFactory.HUE_CYAN
            )).alpha(1f)
        }

        if (acc == true){
            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(
                BitmapDescriptorFactory.HUE_YELLOW
            )).alpha(1f)
        }
        if(type == "hospital")
        {
            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE
            )).alpha(1f)
        }
        if(type == "police")
        {
            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET
            )).alpha(1f)
        }
        if(type == "repairshop")
        {
            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ROSE
            )).alpha(1f)
        }
        if(type!=null) {
            markerOptions.title(type)
        }
        else
            markerOptions.title("$currentLatLong")
        return  mMap.addMarker(markerOptions)!!
        //Log.d("Marker" , marker.isVisible.toString())

    }
    private fun markLocations(jArray : JSONArray, map : HashMap<String, Marker>)
    {
        (0 until jArray.length()).forEach {
            val res = jArray.getJSONObject(it)
            val account = res.getString("id")

            if(map.containsKey(account))
            {
                map[account]?.remove()
            }
            try {map[account] = placeMarkerOnMap(LatLng(res.get("latitude") as Double, res.get("longitude") as Double),
                null,
                null,
                res.get("type") as String?)
                //Log.d("RESULT FOR NEARBY", res.toString())
            }
            catch (e : Exception) {
                 map[account] = placeMarkerOnMap(LatLng(res.get("latitude") as Double, res.get("longitude") as Double),
                    res.get("ambulance") as Boolean?,
                    res.get("accident") as Boolean?,
                    null)
            }
        }

    }
    private fun updateOtherVehicleLocation() {
        volleyRequest.volleyGetRequest("/read" , null , listener)
    }
    override fun onMarkerClick(p0: Marker) = false



}
