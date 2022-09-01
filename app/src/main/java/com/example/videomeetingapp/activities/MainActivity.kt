package com.example.videomeetingapp.activities

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.videomeetingapp.adapters.UsersAdapter
import com.example.videomeetingapp.databinding.ActivityMainBinding
import com.example.videomeetingapp.listeners.UsersListener
import com.example.videomeetingapp.models.User
import com.example.videomeetingapp.utilities.Constants
import com.example.videomeetingapp.utilities.PreferenceManager
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.google.firebase.messaging.FirebaseMessaging
import com.google.gson.Gson

class MainActivity : AppCompatActivity(), UsersListener{
    private lateinit var binding : ActivityMainBinding
    private lateinit var preferenceManager : PreferenceManager
    private val constants : Constants = Constants()
    private lateinit var users : ArrayList<User>
    private lateinit var usersAdapter : UsersAdapter
    private val database : FirebaseFirestore = FirebaseFirestore.getInstance()

    private val REQUEST_CODE_BATTERY_OPTIMIZATIONS = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // TODO: Create binding for Activity
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // TODO: Declare preferenceManager to save data
        preferenceManager = PreferenceManager(this@MainActivity)

        // TODO: Send FCMToken to Firebase
        FirebaseMessaging.getInstance().token.addOnCompleteListener {
            if (it.isSuccessful && it.result != null){
                sendFCMTokenToDatabase(it.result)
            }
        }

        // TODO: Set up View in Activity

        binding.tvTitle.text = String.format(
            "Hello %s %s",
            preferenceManager.getString(constants.KEY_FIRST_NAME),
            preferenceManager.getString(constants.KEY_LAST_NAME)
        )

        binding.tvSignOut.setOnClickListener {
            signOut()
        }

        users = ArrayList()
        usersAdapter = UsersAdapter(this,this@MainActivity, users)
        val layoutManager: RecyclerView.LayoutManager = LinearLayoutManager(this)
        binding.recyclerViewUsers.layoutManager = layoutManager
        binding.recyclerViewUsers.adapter = usersAdapter

        binding.swipeRefreshLayout.setOnRefreshListener(this::getUsers)
        binding.fabConference.visibility = View.GONE
        getUsers()
        checkForBatteryOptimizations()
    }

    // TODO: Create a function to send FCMToken to update Token in Firebase
    private fun sendFCMTokenToDatabase(token : String){
        val documentReference : DocumentReference = database.collection(constants.KEY_COLLECTION_USERS).document(
            preferenceManager.getString(constants.KEY_USER_ID)
        )
        documentReference.update(constants.KEY_FCM_TOKEN, token)
            .addOnFailureListener {
                Toast.makeText(this@MainActivity, "Token updated failed" + it.message, Toast.LENGTH_SHORT).show()
                Log.d("FCM", "sendFCMTokenToDatabase: " + it.message)
            }
    }

    // TODO: Create a function to Sign Out
    private fun signOut(){
        Toast.makeText(this@MainActivity, "Signing out...", Toast.LENGTH_SHORT).show()
        val documentReference : DocumentReference = database.collection(constants.KEY_COLLECTION_USERS).document(
            preferenceManager.getString(constants.KEY_USER_ID)
        )
        val updates : HashMap<String, Any> = HashMap()
        updates[constants.KEY_FCM_TOKEN] = FieldValue.delete()
        documentReference.update(updates)
            .addOnSuccessListener {
                preferenceManager.clearPreferences()
                startActivity(Intent(this@MainActivity, SignInActivity::class.java))
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this@MainActivity, "Unable to Sign out" + it.message, Toast.LENGTH_SHORT).show()
            }
    }

    // TODO: Create a function to get all Users in FirebaseFirestore
    private fun getUsers(){
        binding.swipeRefreshLayout.isRefreshing = true
        database.collection(constants.KEY_COLLECTION_USERS)
            .get()
            .addOnCompleteListener {
                binding.swipeRefreshLayout.isRefreshing = false
                val myUserID = preferenceManager.getString(constants.KEY_USER_ID)
                if (it.isSuccessful && it.result != null){
                    users.clear()
                    for (snapshot : QueryDocumentSnapshot in it.result){
                        if (myUserID == snapshot.id){
                            continue
                        }
                        val firstName = snapshot.getString(constants.KEY_FIRST_NAME).toString()
                        val lastName = snapshot.getString(constants.KEY_LAST_NAME).toString()
                        val email = snapshot.getString(constants.KEY_EMAIL).toString()
                        val token = snapshot.getString(constants.KEY_FCM_TOKEN).toString()
                        val user = User(firstName, lastName, email, token)
                        users.add(user)

                    }
                    if (users.isNotEmpty()) {
                        usersAdapter.notifyDataSetChanged()
                    }else{
                        binding.tvErrorMessageUserLoad.text = String.format("%s", "No users available")
                        binding.tvErrorMessageUserLoad.visibility = View.VISIBLE
                    }
                }else{
                    binding.tvErrorMessageUserLoad.text = String.format("%s", "No users available")
                    binding.tvErrorMessageUserLoad.visibility = View.VISIBLE
                }
            }
    }

    // TODO: Create a function to Initiate VideoMeeting Invitation
    override fun initiateVideoMeeting(user: User) {
        if (user.token == "" || user.token == "null"){
            Toast.makeText(this@MainActivity, user.firstName + " " + user.lastName + " is not available for meeting", Toast.LENGTH_SHORT).show()
        }else{
//            Toast.makeText(this@MainActivity, "Video Meeting with " + user.firstName + " " + user.lastName, Toast.LENGTH_SHORT).show()
            val intent = Intent(this@MainActivity, OutgoingInvitationActivity::class.java)
            intent.putExtra("user", user)
            intent.putExtra("type", "video")
            startActivity(intent)
        }
    }

    // TODO: Create a function to Initiate AudioMeeting Invitation
    override fun initiateAudioMeeting(user: User) {
        if (user.token == "" || user.token == "null"){
            Toast.makeText(this@MainActivity, user.firstName + " " + user.lastName + " is not available for meeting", Toast.LENGTH_SHORT).show()
        }else{
//            Toast.makeText(this@MainActivity, "Audio Meeting with " + user.firstName + " " + user.lastName, Toast.LENGTH_SHORT).show()
            val intent = Intent(this@MainActivity, OutgoingInvitationActivity::class.java)
            intent.putExtra("user", user)
            intent.putExtra("type", "audio")
            startActivity(intent)
        }
    }

    // TODO: Create a function to Initiate MultipleMeeting Invitation
    override fun onMultipleUsersAction(isMultipleUsersSelected: Boolean) {
        if (isMultipleUsersSelected){
            binding.fabConference.visibility = View.VISIBLE
            binding.fabConference.setOnClickListener {
                val intent = Intent(this@MainActivity, OutgoingInvitationActivity::class.java)
                intent.putExtra("selectedUsers", Gson().toJson(usersAdapter.getSelectedUsers()))
                intent.putExtra("type", "video")
                intent.putExtra("isMultiple", true)
                startActivity(intent)
            }
        }else{
            binding.fabConference.visibility = View.GONE
        }
    }

    // TODO: Create a function to Check Battery Optimizations
    private fun checkForBatteryOptimizations(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            val powerManager = getSystemService(POWER_SERVICE) as PowerManager
            if (!powerManager.isIgnoringBatteryOptimizations(packageName)){
                val builder = AlertDialog.Builder(this@MainActivity)
                builder.setTitle("Warning")
                builder.setMessage("Battery optimization is enable. It can interrupt running background services.")
                builder.setPositiveButton("Disable") { _, _ ->
                    val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
                    startActivityForResult(intent, REQUEST_CODE_BATTERY_OPTIMIZATIONS)
                }
                builder.setNegativeButton("Cancel") { p0, _ ->
                    p0.dismiss()
                }
                builder.create().show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == REQUEST_CODE_BATTERY_OPTIMIZATIONS){
            checkForBatteryOptimizations()
        }
    }
}