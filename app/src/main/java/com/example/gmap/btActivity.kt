//package com.example.gmap
//
//import android.annotation.SuppressLint
//import android.app.Activity
//import android.bluetooth.*
//import android.content.Context
//import android.content.Intent
//import android.os.Bundle
//import android.util.Log
//import android.widget.AdapterView
//import android.widget.ArrayAdapter
//import android.widget.Button
//import android.widget.Toast
//import androidx.activity.result.contract.ActivityResultContracts
//import androidx.appcompat.app.AppCompatActivity
//import com.example.gmap.databinding.BtActivityLayoutBinding
//
//
//class btActivity  : AppCompatActivity(){
//
//    val REQUEST_BLUETOOTH_ENABLE = 1
//    companion object{
//        lateinit var bluetoothAdapter :BluetoothAdapter
//        lateinit var bluetoothManager :BluetoothManager
//        private lateinit var binding : BtActivityLayoutBinding
//        private lateinit var btDevices : Set<BluetoothDevice>
//        val DEVICE_ADDRESS: String = "deviceID"
//    }
//
//    @SuppressLint("MissingPermission")
//    private fun showDevices(){
//        btDevices = bluetoothAdapter.bondedDevices
//        var list: ArrayList<BluetoothDevice> = ArrayList()
//        if(btDevices.isNotEmpty()) {
//            for (device:BluetoothDevice in btDevices) {
//                list.add(device)
//                Log.d("device",device.name)
//            }
//
//        }
//        else
//            Toast.makeText(this, "No bluetooth device found", Toast.LENGTH_SHORT).show()
//        val adapter = ArrayAdapter(this,android.R.layout.simple_list_item_1,list)
//        binding.btActivityList.adapter = adapter
//        binding.btActivityList.onItemClickListener = AdapterView.OnItemClickListener{_, _, position , _ ->
//            val selDev = list[position]
//            val macAddress = selDev.address
//            val intent =  Intent(this, btComm::class.java)
//            intent.putExtra("DEVICE_ADDRESS",macAddress.toString())
//            intent.putExtra("DEVICE_NAME", selDev.name.toString())
//            startActivity(intent)
//        }
//    }
//    @SuppressLint("MissingPermission")
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        binding = BtActivityLayoutBinding.inflate(layoutInflater)
//        setContentView(binding.root)
//        bluetoothManager = this.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
//        bluetoothAdapter = bluetoothManager.adapter
//        val resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
//            if (result.resultCode == Activity.RESULT_OK) {
//                // There are no request codes
//                val data: Intent? = result.data
//                Log.d("debug", result.data.toString())
//                if(bluetoothAdapter.isEnabled)
//                    Toast.makeText(this, "BT enable success", Toast.LENGTH_SHORT).show()
//                else
//                    Toast.makeText(this, "Bluetooth disabled", Toast.LENGTH_SHORT).show()
//            }
//            else
//                Toast.makeText(this, "Error in enabling bluetooth", Toast.LENGTH_SHORT).show()
//        }
//        if(bluetoothAdapter == null)
//        {
//            Toast.makeText(this, "No bluetooth Support", Toast.LENGTH_SHORT).show()
//            return
//        }
//        else
//            Toast.makeText(this, "Bluetooth found", Toast.LENGTH_SHORT).show()
//        if(!bluetoothAdapter.isEnabled){
//            val enableBTIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
//            resultLauncher.launch(enableBTIntent)
//        }
//
//
//        val refresh  = binding.btActivityRefresh.setOnClickListener() {
//            Toast.makeText(
//                this,
//                "Button pressed",
//                Toast.LENGTH_SHORT
//            ).show()
//            showDevices()
//        }
//
//    }
//}