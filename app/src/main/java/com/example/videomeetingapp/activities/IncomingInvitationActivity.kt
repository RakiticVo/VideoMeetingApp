package com.example.videomeetingapp.activities

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.annotation.NonNull
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.videomeetingapp.R
import com.example.videomeetingapp.databinding.ActivityIncomingInvitationBinding
import com.example.videomeetingapp.network.ApiClient
import com.example.videomeetingapp.network.ApiServices
import org.jitsi.meet.sdk.JitsiMeetActivity
import org.jitsi.meet.sdk.JitsiMeetConferenceOptions
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.net.URL
import java.util.*

class IncomingInvitationActivity : AppCompatActivity() {
    private lateinit var binding : ActivityIncomingInvitationBinding
    private val constants = com.example.videomeetingapp.utilities.Constants()
    private var meetingType: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityIncomingInvitationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // TODO: Check Meeting Type
        meetingType = intent.getStringExtra(constants.REMOTE_MSG_MEETING_TYPE)
        if (meetingType != null || meetingType != "null"){
            if (meetingType!!.lowercase(Locale.getDefault()) == "audio"){
                binding.imgMeetingTypeIncoming.setImageResource(R.drawable.ic_call)
            }else{
                binding.imgMeetingTypeIncoming.setImageResource(R.drawable.ic_video)
            }
        }

        // TODO: Set Up User Information
        val firstName = intent.getStringExtra(constants.KEY_FIRST_NAME)
        val lastName = intent.getStringExtra(constants.KEY_LAST_NAME)
        val email = intent.getStringExtra(constants.KEY_EMAIL)
        if (firstName != null || firstName != "null"){
            val random = Random()
            val color = Color.argb(255, random.nextInt(256), random.nextInt(256), random.nextInt(256))
            val draw = GradientDrawable()
            val constants = com.example.videomeetingapp.utilities.Constants()
            draw.shape = GradientDrawable.OVAL
            draw.setColor(color)
            if (constants.isColorDark(color)){
                binding.tvFirstCharIncoming.setTextColor(Color.WHITE)
            }else{
                binding.tvFirstCharIncoming.setTextColor(Color.BLACK)
            }
            binding.tvFirstCharIncoming.background = draw
            binding.tvFirstCharIncoming.text = firstName!!.substring(0,1)

            if (lastName != null || lastName != "null"){
                binding.tvUserNameIncomingInvitation.text = String.format("%s %s", firstName, lastName)
            }
        }
        if (email != null || email != "null"){
            binding.tvEmailIncomingInvitation.text = email
        }

        /* TODO: Set Up Accept and Decline */
        val draw = GradientDrawable()
        draw.shape = GradientDrawable.OVAL
        draw.setColor(Color.RED)

        val draw2 = GradientDrawable()
        draw2.shape = GradientDrawable.OVAL
        draw2.setColor(ContextCompat.getColor(this@IncomingInvitationActivity, R.color.green_light))

        binding.imgDeclineIncoming.background = draw
        binding.imgDeclineIncoming.setOnClickListener {
            sendInvitationResponse(
                constants.REMOTE_MSG_INVITATION_DECLINED,
                intent.getStringExtra(constants.REMOTE_MSG_INVITER_TOKEN)!!
            )
        }

        binding.imgAcceptIncoming.background = draw2
        binding.imgAcceptIncoming.setOnClickListener {
//            Toast.makeText(this@IncomingInvitationActivity, "Success", Toast.LENGTH_SHORT).show()
            sendInvitationResponse(
                constants.REMOTE_MSG_INVITATION_ACCEPTED,
                intent.getStringExtra(constants.REMOTE_MSG_INVITER_TOKEN)!!
            )
        }
    }

    private fun sendInvitationResponse(type: String, receiverToken: String){
        try {
            val tokens = JSONArray()
            tokens.put(receiverToken)

            val body = JSONObject()
            val data = JSONObject()

            data.put(constants.REMOTE_MSG_TYPE, constants.REMOTE_MSG_INVITATION_RESPONSE)
            data.put(constants.REMOTE_MSG_INVITATION_RESPONSE, type)

            body.put(constants.REMOTE_MSG_DATA, data)
            body.put(constants.REMOTE_MSG_REGISTRATION_IDS, tokens)

            sendRemoteMessage(body.toString(), type)
        }catch (e: Exception){
            Toast.makeText(this@IncomingInvitationActivity, e.message, Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun sendRemoteMessage(remoteMessageBody: String, type: String){
        val api = ApiClient()
        api.getClient()!!.create(ApiServices::class.java).sendRemoteMessage(
            constants.getRemoteMessageHeaders(),
            remoteMessageBody
        ).enqueue(object: Callback<String> {
            override fun onResponse(@NonNull call: Call<String>, @NonNull response: Response<String>) {
                if (response.isSuccessful){
                    if (type == constants.REMOTE_MSG_INVITATION_ACCEPTED){
//                        Toast.makeText(this@IncomingInvitationActivity, "Invitation Accepted", Toast.LENGTH_SHORT).show()
                        try {
                            val serverURL = URL("https://meet.jit.si")
                            val builder = JitsiMeetConferenceOptions.Builder()
                            builder.setServerURL(serverURL)
                            builder.setFeatureFlag("welcomepage.enabled", false)
                            builder.setRoom(intent.getStringExtra(constants.REMOTE_MSG_MEETING_ROOM))
                            if (meetingType == "audio"){
                                builder.setVideoMuted(true)
                            }
                            JitsiMeetActivity.launch(this@IncomingInvitationActivity, builder.build())
                            finish()
                        }catch (e: Exception){
                            Toast.makeText(this@IncomingInvitationActivity, e.message, Toast.LENGTH_SHORT).show()
                            finish()
                        }
                    }else{
                        Toast.makeText(this@IncomingInvitationActivity, "Invitation Declined", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                }else{
                    Toast.makeText(this@IncomingInvitationActivity, response.message(), Toast.LENGTH_SHORT).show()
                    finish()
                }
            }

            override fun onFailure(@NonNull call: Call<String>, @NonNull t: Throwable) {
                Toast.makeText(this@IncomingInvitationActivity, t.message, Toast.LENGTH_SHORT).show()
                finish()
            }

        })
    }

    private var invitationResponseBRIcoming = object: BroadcastReceiver(){
        override fun onReceive(p0: Context?, p1: Intent?) {
            val type = p1?.getStringExtra(constants.REMOTE_MSG_INVITATION_RESPONSE)
            if (type != null){
                if (type == constants.REMOTE_MSG_INVITATION_CANCELLED){
                    Toast.makeText(this@IncomingInvitationActivity, "Invitation cancelled", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        LocalBroadcastManager.getInstance(applicationContext).registerReceiver(
            invitationResponseBRIcoming,
            IntentFilter(constants.REMOTE_MSG_INVITATION_RESPONSE)
        )
    }

    override fun onStop() {
        super.onStop()
        LocalBroadcastManager.getInstance(applicationContext).unregisterReceiver(
            invitationResponseBRIcoming
        )
    }
}