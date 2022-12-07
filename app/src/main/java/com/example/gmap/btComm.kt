package com.example.gmap

import BluetoothService
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.example.gmap.databinding.BtActivityLayoutBinding
import com.example.gmap.databinding.BtcomLayoutBinding
import java.io.InputStream
import java.io.OutputStream
import java.util.*

class btComm :AppCompatActivity() {
        var ans : String = "b"
        val bluetoothSocket: BluetoothSocket? = null
        lateinit var btAdapter :BluetoothAdapter
        lateinit var btManager : BluetoothManager
        var isConnected = true
        lateinit var btsocket : BluetoothSocket
    inner class ResThread(socket : BluetoothSocket) : Thread() {
        
        val soc = socket
        lateinit var inputStream : InputStream
        init{
            inputStream = socket.inputStream
        }
        lateinit public var str : String
        var buffer: ByteArray = ByteArray(100)
        override fun run() {
            if(inputStream!=null)
            {
                var bytes = inputStream.available()
                if(bytes!=0) {
                    var tempBuffer = ByteArray(bytes)
                    inputStream.read(tempBuffer)
                    str = tempBuffer.toString()
                    ans = tempBuffer.toString()
                    ans = str.toString()
                }
            }
        }
    }
    @SuppressLint("MissingPermission")
    inner class ConnectThread(device : BluetoothDevice) : Thread() {
        lateinit var socket : BluetoothSocket
        val serviceID : String = "00001101-0000-1000-8000-00805f9b34fb"
        var dev :BluetoothDevice = device
        init {
            try {
                socket = dev.createRfcommSocketToServiceRecord(UUID.fromString(serviceID))
            }
            catch (e:java.lang.Exception) {
                Log.d("ERROR", "ERRORRRRRRRRRRRRR")
            }
            btsocket = socket
        }
        override fun  run() {
            btAdapter.cancelDiscovery()
            try {
                socket.connect()
                Log.d("BTConn", "Connectedd")
            }
            catch(e : Exception) {
                socket.close()
                Log.d("Exception", e.toString())
                return
            }
            btsocket = socket

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
    private lateinit var binding: BtcomLayoutBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = BtcomLayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)
        btManager = this.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        btAdapter = btManager.adapter
        var btdev: BluetoothDevice = btAdapter.getRemoteDevice(intent.getStringExtra("DEVICE_ADDRESS").toString())
        val intent = intent
        binding.txtReceivedMessage.text = "HEHE"
        val btAddress = intent.getStringExtra("DEVICE_ADDRESS").toString()
        var dev = intent.getStringExtra("DEVICE_NAME").toString()
        dev +="  "
        dev += intent.getStringExtra("DEVICE_ADDRESS").toString()
        binding.btNameTextView.text = dev
        binding.txtStatus.text= "Connecting"
        var connectThread = ConnectThread(btdev)
        connectThread.start()
        binding.btSendBtn.setOnClickListener {
            if(btsocket!=null)
            {
                binding.txtStatus.text = "Connected"
                try {
                    var outputStream: OutputStream = btsocket.outputStream
                    outputStream.write(binding.btInputText.text.toString().toByteArray())
                    //outputStream.write("How Much can you really seeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeee".toByteArray())
                    outputStream.flush()

                }
                catch (e:java.lang.Exception)
                {
                    Log.d("HEHEH error","FAIL")
                }
            }
        }

        binding.btRecieve.setOnClickListener{
            var startBytes: ByteArray = "<".toByteArray()
            var untilBytes: ByteArray = ">".toByteArray()
            var ser = BluetoothService
            val buffer = ser.listenData(startBytes,untilBytes,btsocket.inputStream)
            binding.txtReceivedMessage.text = String(buffer)
//            if(btsocket!=null)
//            {
//                binding.txtStatus.text = "Receiving"
//
//                try {
//                    val res
//                            = ResThread(btsocket)
//                    res.start()
//
//                    binding.txtReceivedMessage.text = ans
//                }
//                catch(e: java.lang.Exception)
//                {
//                    Log.d("error:::" , e.toString())
//                }
//            }
        }
    }
}