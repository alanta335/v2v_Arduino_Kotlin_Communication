package com.example.gmap

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.app.Activity
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task


class MainActivity : AppCompatActivity() {
    val model: GViewModel by viewModels()
    val getResult =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()) {
            if(it.resultCode == Activity.RESULT_OK){
                val task: Task<GoogleSignInAccount> =
                    GoogleSignIn.getSignedInAccountFromIntent(
                        it?.data
                    )
                val account: GoogleSignInAccount =
                    task.getResult(ApiException::class.java)
                model.account.value = account
                Log.v("CHRCKING",account.displayName.toString())
                val intent = Intent(this,MapsActivity::class.java)
                startActivity(intent)
            }
        }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build()
        val mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        val account = GoogleSignIn.getLastSignedInAccount(this)
        val signinButton = findViewById<Button>(R.id.google_sign_in)
        val btButton = findViewById<Button>(R.id.main_bluetooth_dev).setOnClickListener(){
            val intent = Intent(this,btActivity::class.java)
            startActivity(intent)
        }
        signin(signinButton,mGoogleSignInClient)
        val gObserver = Observer<GoogleSignInAccount> { account ->
            // Update the UI, in this case, a TextView.
            if(account != null){
                signout(signinButton,mGoogleSignInClient)
            }else{
                signin(signinButton,mGoogleSignInClient)

            }

            Log.v("GoogleSignIn","Account Changed")
        }

        model.account.observe(this,gObserver )


    }

    fun signin(signinButton : Button,client:GoogleSignInClient){
        signinButton.text = "Signin"
        signinButton.setOnClickListener {
            val signInIntent = client.signInIntent
            getResult.launch(signInIntent)

        }
    }
    fun signout(signinButton:Button,client:GoogleSignInClient){
        signinButton.text = "Logout"
        signinButton.setOnClickListener {
            client.signOut()
                .addOnCompleteListener(this) {
                    model.account.value = null
                    Toast.makeText(this@MainActivity,"You clicked me",Toast.LENGTH_SHORT)
                }
        }
    }
    override fun onStart() {
        super.onStart()
        val account = GoogleSignIn.getLastSignedInAccount(this)
        model.account.value = account
    if(account != null) {
        val intent = Intent(this,MapsActivity::class.java)
        intent.putExtra("accountid",model.account.value?.id)
        startActivity(intent)
    }
    }
}