package com.example.gmap

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.auth.api.signin.GoogleSignInAccount

class GViewModel : ViewModel() {
    val account = MutableLiveData<GoogleSignInAccount>()
}