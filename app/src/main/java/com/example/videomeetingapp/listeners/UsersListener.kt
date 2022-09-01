package com.example.videomeetingapp.listeners

import com.example.videomeetingapp.models.User

/* TODO: Create an Interface to initiate screen for Invitation */
interface UsersListener {
    fun initiateVideoMeeting(user: User)

    fun initiateAudioMeeting(user: User)

    fun onMultipleUsersAction(isMultipleUsersSelected: Boolean)
}