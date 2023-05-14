package com.example.gmap

import BluetoothService
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.Context
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
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import kotlinx.coroutines.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.OutputStream
import java.sql.Timestamp
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class MapsActivity : AppCompatActivity(), OnMapReadyCallback ,
    GoogleMap.OnMarkerClickListener {
    private lateinit var crowdDetection : ExtendedFloatingActionButton
    private lateinit var accidentDetection : ExtendedFloatingActionButton
    private lateinit var nearbyDetection : ExtendedFloatingActionButton
    private var crowdDetected = 0
    private var nearbyDetected = 0
    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    lateinit var btAdapter : BluetoothAdapter
            //lateinit var bluetoothAdapter :BluetoothAdapter
        lateinit var bluetoothManager :BluetoothManager
    lateinit var btsocket : BluetoothSocket
    private lateinit var lastLocation: Location
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var accident = false
    private var block = false
    private lateinit var connectThread : ConnectThread
    private var markerMap : HashMap<String , Marker> = HashMap()
    private var nearbyMap: HashMap<String, Marker> = HashMap()
    private lateinit var faba: View
    private var x : Job? = null
    private var k : Job? = null
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
    @SuppressLint("MissingPermission")
    inner class ConnectThread(device : BluetoothDevice) : Thread() {
        fun receive()
        {
            var startBytes: ByteArray = "<".toByteArray()
            var untilBytes: ByteArray = ">".toByteArray()
            var ser = BluetoothService
            val buffer = ser.listenData(startBytes,untilBytes,btsocket.inputStream)
            Log.d("BTRecieved: ", buffer.toString())
            if(!buffer.isEmpty()) {
                val answ = String(buffer);
                Log.d("BTRecieved answ: ", answ);
                volleyRequest.volleyPostRequest("/updateaccofother", JSONObject(answ), listener)
            }

        }
        fun send(string: String)
        {
            if(btsocket!=null)
            {

                try {
                    var outputStream: OutputStream = btsocket.outputStream
                    outputStream.write(string.toByteArray())
                    outputStream.flush()

                }
                catch (e:java.lang.Exception)
                {
                    Log.d("BTError","Socket error")
                }
            }
        }

        lateinit var socket : BluetoothSocket
        private val serviceID : String = "00001101-0000-1000-8000-00805f9b34fb"
        var dev :BluetoothDevice = device
        init {
            try {
                socket = dev.createRfcommSocketToServiceRecord(UUID.fromString(serviceID))
            }
            catch (e:java.lang.Exception) {
                Log.d("ERROR", "Creating socket error")
            }
            btsocket = socket
        }
        override fun  run() {
            btAdapter.cancelDiscovery()
            try {
                socket.connect()
                Log.d("BTConn", "Connected")
            }
            catch(e : Exception) {
                socket.close()
                Log.d("Exception", e.toString())
                return
            }
            btsocket = socket
//            run_continously()

        }
        public fun cancel()
        {
            try{
                socket.close()

            }
            catch(e:java.lang.Exception)
            {
                Log.d("HEHEHEH", "Error")
            }
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
        @SuppressLint("MissingPermission")
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
                    bluetoothManager = this.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        btAdapter = bluetoothManager.adapter
            connectThread = ConnectThread(btAdapter.getRemoteDevice("00:22:04:00:71:36"))
            connectThread.start()
            Log.d("Connected the connect thread","");
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
                if(!accident)
                {
                    accident = true
                    x = CoroutineScope(Dispatchers.IO).launch{
                        while(!::connectThread.isInitialized) {}
                        while(true) {

                            yield()
                            Log.d("Trying to send", "3")
                            fusedLocationClient.lastLocation.addOnSuccessListener { loc: Location? ->
                                if (loc != null) {
                                    Log.d("latitude", loc.latitude.toString())
                                    Log.d("Longitude ", loc.longitude.toString());
                                    var obj = JSONObject()
                                    obj.put("lat",  loc.latitude.toString())
                                    obj.put("long" , loc.longitude.toString())
                                    val pid = intent.getStringExtra("accountid").toString()
                                    obj.put("did" , pid.substring(14,21))
                                    Log.d("JSON Object" , obj.toString())
                                    connectThread!!.send(("<$obj>"))

                                }
                                else Log.d("Location Pinger", "Turn on Location")
                            }
                            Thread.sleep(3000)
                        }

                    }
                }
                else
                {
                    accident = false
                    x?.cancel()
                    //connectThread.stop()
                   // x?.join()
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


    @SuppressLint("MissingPermission")
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.uiSettings.isZoomControlsEnabled = true
        mMap.setOnMarkerClickListener(this)
        setUpMap()

            k = CoroutineScope(Dispatchers.IO).launch{
                while(!::connectThread.isInitialized) {}
                while(true) {
                    yield()
                    Log.d("Trying to resive", "3")
                    if (connectThread.socket.isConnected)
                    {
                        connectThread.receive()
                    }
                    Thread.sleep(3000)
                }

            }


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
