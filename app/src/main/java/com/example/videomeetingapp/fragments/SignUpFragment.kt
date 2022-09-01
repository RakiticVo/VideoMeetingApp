package com.example.videomeetingapp.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.videomeetingapp.activities.MainActivity
import com.example.videomeetingapp.databinding.FragmentSignUpBinding
import com.example.videomeetingapp.utilities.Constants
import com.example.videomeetingapp.utilities.PreferenceManager
import com.google.firebase.firestore.FirebaseFirestore

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/* TODO: Create a Fragment to Sign Up for User */
class SignUpFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private var _binding: FragmentSignUpBinding? = null
    private val binding get() = _binding!!
    private lateinit var preferenceManager : PreferenceManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // TODO: Create binding for Fragment
        _binding = FragmentSignUpBinding.inflate(inflater, container, false)
        val view = binding.root

        // TODO: Declare preferenceManager to save data
        preferenceManager = PreferenceManager(context!!)

        // TODO: Set up View in Fragment
        // TODO: Text View SignIn
        binding.tvSignIn.setOnClickListener {
            activity!!.supportFragmentManager.popBackStack()
        }

        // TODO: Button SignUp
        binding.btnSignUp.setOnClickListener {
            if (binding.edtFirstNameSignUp.text.toString().isEmpty()){
                Toast.makeText(context!!, "Enter your First name", Toast.LENGTH_SHORT).show()
            }else if (binding.edtLastNameSignUp.text.toString().isEmpty()){
                Toast.makeText(context!!, "Enter your Last name", Toast.LENGTH_SHORT).show()
            }else if (binding.edtEmailSignUp.text.toString().isEmpty()){
                Toast.makeText(context!!, "Enter your Email", Toast.LENGTH_SHORT).show()
            }else if (!Patterns.EMAIL_ADDRESS.matcher(binding.edtEmailSignUp.text.toString()).matches()){
                Toast.makeText(context!!, "Invalid Email", Toast.LENGTH_SHORT).show()
            }else if (binding.edtPasswordSignUp.text.toString().isEmpty()){
                Toast.makeText(context!!, "Enter your Password", Toast.LENGTH_SHORT).show()
            }else if (binding.edtConfirmPasswordSignUp.text.toString().isEmpty()){
                Toast.makeText(context!!, "Please confirm your Password", Toast.LENGTH_SHORT).show()
            }else if (binding.edtConfirmPasswordSignUp.text.toString() != binding.edtPasswordSignUp.text.toString()){
                Toast.makeText(context!!, "Password & Confirm Password must be same", Toast.LENGTH_SHORT).show()
            }else {
                signUp()
            }
        }

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment SignUpFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            SignUpFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    // TODO: Create function to Register User from Firebase and SignIn
    private fun signUp() {
        binding.btnSignUp.visibility = View.INVISIBLE
        binding.progressSignUp.visibility = View.VISIBLE

        // TODO: Set up val
        val database : FirebaseFirestore = FirebaseFirestore.getInstance()
        val constants = Constants()
        val user : HashMap<String, Any> = HashMap()

        val firstName = binding.edtFirstNameSignUp.text.toString()
        val lastName = binding.edtLastNameSignUp.text.toString()
        val email = binding.edtEmailSignUp.text.toString()
        val password = binding.edtPasswordSignUp.text.toString()

        // TODO: Save data and POST to FirebaseFirestore
        user[constants.KEY_FIRST_NAME] = firstName
        user[constants.KEY_LAST_NAME] = lastName
        user[constants.KEY_EMAIL] = email
        user[constants.KEY_PASSWORD] = password
        database.collection(constants.KEY_COLLECTION_USERS)
            .add(user)
            .addOnSuccessListener {
                Toast.makeText(context!!, "Register User Success", Toast.LENGTH_SHORT).show()
                preferenceManager.putBoolean(constants.KEY_IS_SIGNED_IN, true)
                preferenceManager.putString(constants.KEY_USER_ID, it.id)
                preferenceManager.putString(constants.KEY_FIRST_NAME, firstName)
                preferenceManager.putString(constants.KEY_LAST_NAME, lastName)
                preferenceManager.putString(constants.KEY_EMAIL, email)
                val intent = Intent(context!!, MainActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                activity!!.finish()
            }
            .addOnFailureListener {
                binding.btnSignUp.visibility = View.VISIBLE
                binding.progressSignUp.visibility = View.INVISIBLE
                Toast.makeText(context!!, "Register User Failed" + it.message, Toast.LENGTH_SHORT).show()
            }
    }
}