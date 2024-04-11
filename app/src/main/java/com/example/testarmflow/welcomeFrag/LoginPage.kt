package com.example.testarmflow.welcomeFrag

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.example.testarmflow.MainHomeActivity
import com.example.testarmflow.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import java.util.regex.Pattern


class LoginPage : Fragment() {
    private lateinit var editTextEmail: EditText
    private lateinit var editTextPassword: EditText
    private lateinit var buttonLogin: Button
    private lateinit var regtext: TextView
    private lateinit var auth: FirebaseAuth


    private fun showDialog(s: String){
        Toast.makeText(activity, s, Toast.LENGTH_SHORT).show()
    }


    private fun isValidEmail(email: EditText): String {
        val pattern = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}\$")
        val matcher = pattern.matcher(email.text.toString())
        return when {
            email.text.toString().trim().isEmpty() -> {
                "E-mail field is empty!"
            }
            !matcher.matches() -> {
                "Invalid e-mail!"
            }
            else -> {
                "true"
            }
        }
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreate(savedInstanceState)
        val sharedPreferences1: SharedPreferences = requireContext().getSharedPreferences("moje_pref", Context.MODE_PRIVATE)
        val mt = sharedPreferences1.getBoolean("motyw", false)

        val motyw = if(mt){
            R.layout.fragment_login_page_light
        }else{
            R.layout.fragment_login_page
        }

        val view = inflater.inflate(motyw, container, false)

        editTextEmail = view.findViewById(R.id.username)
        editTextPassword = view.findViewById(R.id.password)
        buttonLogin = view.findViewById(R.id.login_button)
        regtext = view.findViewById(R.id.reg_text)

        auth = Firebase.auth

        regtext.setOnClickListener{
            findNavController().navigate(R.id.action_loginPage_to_registerPage)
        }

        buttonLogin.setOnClickListener {
            val v = isValidEmail(editTextEmail)
            if(v =="true") {
                auth.signInWithEmailAndPassword(editTextEmail.text.toString().trim(), editTextPassword.text.toString().trim())
                    .addOnCompleteListener {
                        if (it.isSuccessful) {
                            Log.d("myTag", "signInWithEmail:success")
                            val realTime = FirebaseDatabase.getInstance().reference
                            realTime.child("users").child(auth.currentUser!!.uid).child("status").setValue("online")

                            val intent = Intent(activity, MainHomeActivity::class.java)
                            startActivity(intent)
                        } else {
                            Log.w("myTag", "signInWithEmail:failure", it.exception)
                            Toast.makeText(activity, "Authentication failed.", Toast.LENGTH_SHORT,).show()
                        }
                    }
            } else {
                showDialog(v)
            }
        }
        return view
    }

}