package com.example.videomeetingapp.activities

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.annotation.NonNull
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.videomeetingapp.R
import com.example.videomeetingapp.databinding.ActivityOutgoingInvitationBinding
import com.example.videomeetingapp.models.User
import com.example.videomeetingapp.network.ApiClient
import com.example.videomeetingapp.network.ApiServices
import com.example.videomeetingapp.utilities.Constants
import com.example.videomeetingapp.utilities.PreferenceManager
import com.google.firebase.messaging.FirebaseMessaging
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.jitsi.meet.sdk.JitsiMeetActivity
import org.jitsi.meet.sdk.JitsiMeetConferenceOptions
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.Exception
import java.net.URL
import java.util.*
import kotlin.collections.ArrayList
import kotlin.text.StringBuilder

class OutgoingInvitationActivity : AppCompatActivity() {
    private lateinit var binding : ActivityOutgoingInvitationBinding
    private lateinit var preferences: PreferenceManager
    private var inviterToken: String = null.toString()
    private val constants = Constants()
    private var meetingRoom: String? = null
    private var meetingType: String? = null
    private var rejectionCount = 0
    private var totalReceivers = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // TODO: Create binding for Activity
        binding = ActivityOutgoingInvitationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // TODO: Declare preferenceManager to save data
        preferences = PreferenceManager(this@OutgoingInvitationActivity)

        // TODO: Check Meeting Type
        meetingType = intent.getStringExtra("type").toString()

        if (meetingType != null){
            if (meetingType!!.lowercase(Locale.getDefault()) == "audio"){
                binding.imgMeetingTypeOutgoing.setImageResource(R.drawable.ic_call)
            }else{
                binding.imgMeetingTypeOutgoing.setImageResource(R.drawable.ic_video)
            }
        }
//        else{
//            Toast.makeText(this@OutgoingInvitationActivity, "Don't know Meeting Type", Toast.LENGTH_SHORT).show()
//        }

        // TODO: Set Up User Information
        val user: User? = intent.getSerializableExtra("user") as User?
        if (user != null){
            val random = Random()
            val color = Color.argb(255, random.nextInt(256), random.nextInt(256), random.nextInt(256))
            val draw = GradientDrawable()
            val constants = Constants()
            draw.shape = GradientDrawable.OVAL
            draw.setColor(color)
            if (constants.isColorDark(color)){
                binding.tvFirstCharOutgoing.setTextColor(Color.WHITE)
            }else{
                binding.tvFirstCharOutgoing.setTextColor(Color.BLACK)
            }
            binding.tvFirstCharOutgoing.background = draw
            binding.tvFirstCharOutgoing.text = user.firstName!!.substring(0,1)
            binding.tvUserNameOutgoingInvitation.text = String.format("%s %s", user.firstName, user.lastName)
            binding.tvEmailOutgoingInvitation.text = user.email
        }

        val type = object : TypeToken<ArrayList<User>>() {}.type
        val receivers = Gson().fromJson<ArrayList<User>>(intent.getStringExtra("selectedUsers"), type)

        // TODO: Set Up Cancel
        val draw = GradientDrawable()
        draw.shape = GradientDrawable.OVAL
        draw.setColor(Color.RED)
        binding.imgDecline.background = draw
        binding.imgDecline.setOnClickListener {
            if (intent.getBooleanExtra("isMultiple", false)){
                cancelInvitation( null, receivers)
            }else if (user != null){
                cancelInvitation(user.token!!, null)
            }
        }

        // TODO: Get Firebase Messaging Token
        FirebaseMessaging.getInstance().token.addOnCompleteListener {
            if (it.isSuccessful && it.result != null){
                inviterToken = it.result
                if (meetingType != "null"){
                    if (intent.getBooleanExtra("isMultiple", false)){
                        if (receivers != null){
                            totalReceivers = receivers.size
                        }
                        initiateMeeting(meetingType!!, null, receivers)
                    }else if (user != null){
                        totalReceivers = 1
                        initiateMeeting(meetingType!!, user.token!!, null)
                    }
                }
            }
        }
    }

    // TODO: Create a function to Initiate Invitation
    private fun initiateMeeting(meetingType: String, receiverToken: String?, receivers: ArrayList<User>?){
        try {
            // TODO: Get Token of receivers
            val tokens = JSONArray()
            if (receiverToken != null || receiverToken != "null"){
                tokens.put(receiverToken)
            }

            // TODO: Get list of receivers for multiple call
            if (receivers != null && receivers.size > 0){
               val userNames = StringBuilder()
                for (user in receivers){
                    tokens.put(user.token)
                    userNames.append(user.firstName).append(" ").append(user.lastName).append("\n")
                }
                binding.tvFirstCharOutgoing.visibility = View.GONE
                binding.tvEmailOutgoingInvitation.visibility = View.GONE
                binding.tvUserNameOutgoingInvitation.text = userNames.toString()
            }

            // TODO: Create data and POST in Firebase
            val body = JSONObject()
            val data = JSONObject()

            data.put(constants.REMOTE_MSG_TYPE, constants.REMOTE_MSG_INVITATION)
            data.put(constants.REMOTE_MSG_MEETING_TYPE, meetingType)
            data.put(constants.KEY_FIRST_NAME, preferences.getString(constants.KEY_FIRST_NAME))
            data.put(constants.KEY_LAST_NAME, preferences.getString(constants.KEY_LAST_NAME))
            data.put(constants.KEY_EMAIL, preferences.getString(constants.KEY_EMAIL))
            data.put(constants.REMOTE_MSG_INVITER_TOKEN, inviterToken)

            meetingRoom = preferences.getString(constants.KEY_USER_ID) + "_" + UUID.randomUUID().toString().substring(0, 5)
            data.put(constants.REMOTE_MSG_MEETING_ROOM, meetingRoom)

            body.put(constants.REMOTE_MSG_DATA, data)
            body.put(constants.REMOTE_MSG_REGISTRATION_IDS, tokens)

            sendRemoteMessage(body.toString(), constants.REMOTE_MSG_INVITATION)

        }catch (e: Exception){
            Toast.makeText(this@OutgoingInvitationActivity, e.message, Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    // TODO: Send an Invitation for other Users
    private fun sendRemoteMessage(remoteMessageBody: String, type: String){
        val api = ApiClient()
        api.getClient()!!.create(ApiServices::class.java).sendRemoteMessage(
            constants.getRemoteMessageHeaders(),
            remoteMessageBody
        ).enqueue(object: Callback<String> {
            override fun onResponse(@NonNull call: Call<String>,@NonNull response: Response<String>) {
                if (response.isSuccessful){
                    if (type == constants.REMOTE_MSG_INVITATION){
//                        Toast.makeText(this@OutgoingInvitationActivity, "Invitation sent successfully", Toast.LENGTH_SHORT).show()
                    }else if(type == constants.REMOTE_MSG_INVITATION_RESPONSE){
                        Toast.makeText(this@OutgoingInvitationActivity, "Invitation cancelled", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                }else{
                    Toast.makeText(this@OutgoingInvitationActivity, response.message(), Toast.LENGTH_SHORT).show()
                    finish()
                }
            }

            override fun onFailure(@NonNull call: Call<String>,@NonNull t: Throwable) {
                Toast.makeText(this@OutgoingInvitationActivity, t.message, Toast.LENGTH_SHORT).show()
                finish()
            }

        })
    }

    // TODO: cancel an Invitation
    private fun cancelInvitation(receiverToken: String?, receivers: ArrayList<User>?){
        try {
            val tokens = JSONArray()
            if (receiverToken != null || receiverToken != "null"){
                tokens.put(receiverToken)
            }

            if (receivers != null && receivers.size > 0){
                for (user in receivers){
                    tokens.put(user.token)
                }
            }

            val body = JSONObject()
            val data = JSONObject()

            data.put(constants.REMOTE_MSG_TYPE, constants.REMOTE_MSG_INVITATION_RESPONSE)
            data.put(constants.REMOTE_MSG_INVITATION_RESPONSE, constants.REMOTE_MSG_INVITATION_CANCELLED)

            body.put(constants.REMOTE_MSG_DATA, data)
            body.put(constants.REMOTE_MSG_REGISTRATION_IDS, tokens)

            sendRemoteMessage(body.toString(), constants.REMOTE_MSG_INVITATION_RESPONSE)
        }catch (e: Exception){
            Toast.makeText(this@OutgoingInvitationActivity, e.message, Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    // TODO: Create an Invitation Response Broadcast and start call
    private var invitationResponseBROutgoing = object: BroadcastReceiver(){
        override fun onReceive(p0: Context?, p1: Intent?) {
            val type = p1?.getStringExtra(constants.REMOTE_MSG_INVITATION_RESPONSE)
            if (type != null){
                if (type == constants.REMOTE_MSG_INVITATION_ACCEPTED){
//                    Toast.makeText(this@OutgoingInvitationActivity, "Invitation Accepted", Toast.LENGTH_SHORT).show()
                    try {
                        val serverURL = URL("https://meet.jit.si")
                        val builder = JitsiMeetConferenceOptions.Builder()
                        builder.setServerURL(serverURL)
                        builder.setFeatureFlag("welcomepage.enabled", false)
                        builder.setRoom(meetingRoom)
                        if (meetingType == "audio"){
                            builder.setVideoMuted(true)
                        }
                        JitsiMeetActivity.launch(this@OutgoingInvitationActivity, builder.build())
                        finish()
                    }catch (e: Exception){
                        Toast.makeText(this@OutgoingInvitationActivity, e.message, Toast.LENGTH_SHORT).show()
                        finish()
                    }
                }else if (type == constants.REMOTE_MSG_INVITATION_DECLINED){
                    rejectionCount += 1;
                    if (rejectionCount == totalReceivers){
                        Toast.makeText(this@OutgoingInvitationActivity, "Invitation Declined", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                }
            }
        }
    }

    // TODO: Register LocalBroadcast
    override fun onStart() {
        super.onStart()
        LocalBroadcastManager.getInstance(applicationContext).registerReceiver(
            invitationResponseBROutgoing,
            IntentFilter(constants.REMOTE_MSG_INVITATION_RESPONSE)
        )
    }

    // TODO: Unregister LocalBroadcast
    override fun onStop() {
        super.onStop()
        LocalBroadcastManager.getInstance(applicationContext).unregisterReceiver(
            invitationResponseBROutgoing
        )
    }
}