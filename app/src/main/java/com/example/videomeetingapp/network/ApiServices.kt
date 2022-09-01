package com.example.videomeetingapp.network

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.HeaderMap
import retrofit2.http.POST

/* TODO: Create a Services to send data(User Information) to Firebase*/
interface ApiServices {
    @POST("send")
    fun sendRemoteMessage(
        @HeaderMap headers : HashMap<String, String>,
        @Body remoteBody : String
    ) : Call<String>
}