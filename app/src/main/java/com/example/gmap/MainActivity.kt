package com.example.gmap

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        var button_sign : Button = findViewById(R.id.google_sign_in)
        button_sign.setOnClickListener {
            // Handler code here.
            val intent = Intent(this,MapsActivity::class.java)
            startActivity(intent)
        }
    }
}