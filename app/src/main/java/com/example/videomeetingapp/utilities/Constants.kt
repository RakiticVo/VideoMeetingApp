package com.example.videomeetingapp.utilities

import android.graphics.Color

/* TODO: Create constants to use */
class Constants {
    // TODO: User Information
    var KEY_COLLECTION_USERS = "users"
    var KEY_FIRST_NAME = "first_name"
    var KEY_LAST_NAME = "last_name"
    var KEY_EMAIL = "email"
    var KEY_PASSWORD = "password"
    var KEY_USER_ID = "user_id"

    // TODO: SharedPreference Information
    var KEY_PREFERENCE_NAME = "videoMeetingPreference"
    var KEY_IS_SIGNED_IN = "isSignedIn"

    // TODO: FCM And Invitation Information
    var KEY_FCM_TOKEN = "fcm_token"
    var REMOTE_MSG_AUTHORIZATION = "Authorization"
    var REMOTE_MSG_CONTENT_TYPE = "Content-Type"

    var REMOTE_MSG_TYPE = "type"
    var REMOTE_MSG_INVITATION = "invitation"
    var REMOTE_MSG_MEETING_TYPE = "meetingType"
    var REMOTE_MSG_INVITER_TOKEN = "inviterToken"
    var REMOTE_MSG_DATA = "data"
    var REMOTE_MSG_REGISTRATION_IDS = "registration_ids"

    var REMOTE_MSG_INVITATION_RESPONSE = "invitationResponse"

    var REMOTE_MSG_INVITATION_ACCEPTED = "accepted"
    var REMOTE_MSG_INVITATION_DECLINED = "declined"
    var REMOTE_MSG_INVITATION_CANCELLED = "cancelled"

    var REMOTE_MSG_MEETING_ROOM = "meetingRoom"

    fun isColorDark(color: Int) : Boolean{
        val darkness: Double = 1-(0.299* Color.red(color) + 0.587*Color.green(color) + 0.114*Color.blue(color))/255
        return darkness >= 0.5
    }

    fun getRemoteMessageHeaders(): HashMap<String, String>{
        val headers = HashMap<String, String>()
        headers[REMOTE_MSG_AUTHORIZATION] = "key=AAAAinV4VkY:APA91bF36wZtMjIXkFJoRVhnM_ywN2NTVruK1STmOrrPhWDJjRy1EAqG_DxQ8hD1950jfVpTXyqXxGqc5l7t-zKW17Tn77roB_udIH1D_MEeWWnhiOExUQ0xArmXM2037SeSrK7xsN2k"
        headers[REMOTE_MSG_CONTENT_TYPE] = "application/json"

        return headers
    }
}