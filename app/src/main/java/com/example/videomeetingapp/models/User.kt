package com.example.videomeetingapp.models

import java.io.Serializable

/* TODO: Create a User data class only save data*/
data class User(val firstName:String?, val lastName:String?, val email:String?, val token:String?) : Serializable