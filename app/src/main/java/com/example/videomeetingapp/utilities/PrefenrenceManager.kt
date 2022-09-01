package com.example.videomeetingapp.utilities

import android.content.Context
import android.content.SharedPreferences

/* TODO: Create SharePreference to save information */
class PreferenceManager() {
    private lateinit var sharePreference : SharedPreferences
    private val constants : Constants = Constants()

    constructor(context: Context) : this() {
        sharePreference = context.getSharedPreferences(constants.KEY_PREFERENCE_NAME, Context.MODE_PRIVATE)
    }

    // TODO: Save And Stored Boolean Value
    fun putBoolean(key:String, value:Boolean) {
        val editor : SharedPreferences.Editor = sharePreference.edit()
        editor.putBoolean(key, value)
        editor.apply()
    }

    fun getBoolean(key: String) : Boolean {
        return sharePreference.getBoolean(key, false)
    }

    // TODO: Save And Stored String Value
    fun putString(key:String, value:String) {
        val editor : SharedPreferences.Editor = sharePreference.edit()
        editor.putString(key, value)
        editor.apply()
    }

    fun getString(key:String) : String {
        return sharePreference.getString(key, null).toString()
    }

    // TODO: Reset SharedPreferences
    fun clearPreferences(){
        val editor : SharedPreferences.Editor = sharePreference.edit()
        editor.clear()
        editor.apply()
    }
}