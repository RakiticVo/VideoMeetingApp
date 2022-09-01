package com.example.videomeetingapp.firebase

import android.content.Intent
import androidx.annotation.NonNull
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.videomeetingapp.activities.IncomingInvitationActivity
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

/* TODO: Initiate FirebaseMessagingService */
class MessagingService : FirebaseMessagingService() {
    private val constants = com.example.videomeetingapp.utilities.Constants()

    override fun onNewToken(@NonNull token: String) {
        super.onNewToken(token)
    }

    // TODO: Get Invitation From Other Users
    override fun onMessageReceived(@NonNull message: RemoteMessage) {
        super.onMessageReceived(message)
        val type = message.data[constants.REMOTE_MSG_TYPE]

        // TODO: Check Inivitation and Initiate
        if (type != null || type != "null"){
            if (type.equals(constants.REMOTE_MSG_INVITATION)){
                val intent = Intent(applicationContext, IncomingInvitationActivity::class.java)
                intent.putExtra(
                    constants.REMOTE_MSG_MEETING_TYPE,
                    message.data[constants.REMOTE_MSG_MEETING_TYPE]
                )
                intent.putExtra(
                    constants.KEY_FIRST_NAME,
                    message.data[constants.KEY_FIRST_NAME]
                )
                intent.putExtra(
                    constants.KEY_LAST_NAME,
                    message.data[constants.KEY_LAST_NAME]
                )
                intent.putExtra(
                    constants.KEY_EMAIL,
                    message.data[constants.KEY_EMAIL]
                )
                intent.putExtra(
                    constants.REMOTE_MSG_INVITER_TOKEN,
                    message.data[constants.REMOTE_MSG_INVITER_TOKEN]
                )
                intent.putExtra(
                    constants.REMOTE_MSG_INVITER_TOKEN,
                    message.data[constants.REMOTE_MSG_INVITER_TOKEN]
                )
                intent.putExtra(
                    constants.REMOTE_MSG_MEETING_ROOM,
                    message.data[constants.REMOTE_MSG_MEETING_ROOM]
                )
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }else if (type == constants.REMOTE_MSG_INVITATION_RESPONSE){
                val intent = Intent(constants.REMOTE_MSG_INVITATION_RESPONSE)
                intent.putExtra(
                    constants.REMOTE_MSG_INVITATION_RESPONSE,
                    message.data[constants.REMOTE_MSG_INVITATION_RESPONSE]
                )
                LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intent)
            }
        }
    }
}