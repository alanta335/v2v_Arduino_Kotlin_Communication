package com.example.gmap
import android.content.Context
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.android.volley.Request
import com.android.volley.toolbox.JsonArrayRequest
import org.json.JSONArray

import org.json.JSONObject
lateinit var baseURL : String

class volleyRequestHandler(context: Context) {
    var queue: RequestQueue

    init {
        queue = Volley.newRequestQueue(context)
        baseURL = context.getString(R.string.baseURL)
    }
    fun volleyPostRequest(url : String, jsonObject: JSONObject, callback : VolleyResponseListener)
    {
        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.POST, baseURL + url, jsonObject,
            { response ->
                callback.onSuccess(url , response)
            },
            { error ->
                callback.onFail(url, error.toString())
                //Log.d("error",error.localizedMessage)
            }
        )
        queue.add(jsonObjectRequest)
    }
    fun volleyGetRequest(url: String ,jsonArray: JSONArray? ,callback: VolleyResponseListener){
        val jsonArrayRequest = JsonArrayRequest(
            Request.Method.GET, baseURL+url, jsonArray,
            { response ->
                callback.onSuccess(url, response)
                //markLocations(response)
            },
            { error ->
                callback.onFail(url,error.toString())
            }
        )
        queue.add(jsonArrayRequest)

    }
}

interface VolleyResponseListener{
    fun onSuccess(url : String , json : JSONObject)
    fun onSuccess(url: String , jsonArray : JSONArray)
    fun onFail(url : String  , error: String)
}