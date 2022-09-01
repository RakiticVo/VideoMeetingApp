package com.example.videomeetingapp.activities

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.example.videomeetingapp.databinding.ActivitySignInBinding
import com.example.videomeetingapp.fragments.SignUpFragment
import com.example.videomeetingapp.utilities.Constants
import com.example.videomeetingapp.utilities.PreferenceManager
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore

/* TODO: Create a Activity to Sign In for User */
class SignInActivity : AppCompatActivity() {
    private lateinit var binding : ActivitySignInBinding
    private lateinit var preferenceManager: PreferenceManager
    private val constants = Constants()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // TODO: Create binding for Activity
        binding = ActivitySignInBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // TODO: Declare preferenceManager to save data
        preferenceManager = PreferenceManager(this@SignInActivity)

        // TODO: Check User is signed in previous
        if (preferenceManager.getBoolean(constants.KEY_IS_SIGNED_IN)){
            startActivity(Intent(this@SignInActivity, MainActivity::class.java))
            finish()
        }

        // TODO: Check TextView SignUp to move to SignUpFragment
        binding.tvSignUp.setOnClickListener {
            val fragmentManager: FragmentManager = supportFragmentManager
            val fragmentTransaction: FragmentTransaction = fragmentManager.beginTransaction()
            binding.frameLayout.visibility = View.VISIBLE
            fragmentTransaction.add(binding.frameLayout.id, SignUpFragment())
                .addToBackStack("SignInActivity")
                .commit()
        }

        // TODO: Check data input and SignIn
        binding.btnSignIn.setOnClickListener {
            if (binding.edtEmailSignIn.text.toString().isEmpty()){
                Toast.makeText(this@SignInActivity, "Enter your Email", Toast.LENGTH_SHORT).show()
            }else if (!Patterns.EMAIL_ADDRESS.matcher(binding.edtEmailSignIn.text.toString()).matches()){
                Toast.makeText(this@SignInActivity, "Invalid Email", Toast.LENGTH_SHORT).show()
            }else if (binding.edtPasswordSignIn.text.toString().isEmpty()){
                Toast.makeText(this@SignInActivity, "Enter your Password", Toast.LENGTH_SHORT).show()
            }else {
                signIn()
            }
        }
    }

    // TODO: Create function to check User already in FirebaseFirestore and SignIn
    private fun signIn() {
        binding.btnSignIn.visibility = View.INVISIBLE
        binding.progressSignIn.visibility = View.VISIBLE

        val database : FirebaseFirestore = FirebaseFirestore.getInstance()

        val email = binding.edtEmailSignIn.text.toString()
        val password = binding.edtPasswordSignIn.text.toString()

        // TODO: Check User already in FirebaseFirestore
        database.collection(constants.KEY_COLLECTION_USERS)
            .whereEqualTo(constants.KEY_EMAIL, email)
            .whereEqualTo(constants.KEY_PASSWORD, password)
            .get()
            .addOnCompleteListener {
                if (it.isSuccessful && it.result != null && it.result.documents.size > 0){
//                    Toast.makeText(this@SignInActivity, "Sign in success", Toast.LENGTH_SHORT).show()
                    val documentSnapshot : DocumentSnapshot = it.result.documents[0]
                    if (binding.cbRemember.isChecked){
                        preferenceManager.putBoolean(constants.KEY_IS_SIGNED_IN, true)
                    }
                    preferenceManager.putString(constants.KEY_USER_ID, documentSnapshot.id)
                    preferenceManager.putString(constants.KEY_FIRST_NAME, documentSnapshot.getString(constants.KEY_FIRST_NAME)!!)
                    preferenceManager.putString(constants.KEY_LAST_NAME, documentSnapshot.getString(constants.KEY_LAST_NAME)!!)
                    preferenceManager.putString(constants.KEY_EMAIL, documentSnapshot.getString(constants.KEY_EMAIL)!!)
                    val intent = Intent(this@SignInActivity, MainActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                }else{
                    binding.btnSignIn.visibility = View.VISIBLE
                    binding.progressSignIn.visibility = View.INVISIBLE
                    Toast.makeText(this@SignInActivity, "Sign in failed", Toast.LENGTH_SHORT).show()
                }
            }
    }
}