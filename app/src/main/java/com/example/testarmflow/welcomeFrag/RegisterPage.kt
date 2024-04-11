package com.example.testarmflow.welcomeFrag

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.example.testarmflow.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.regex.Pattern


class RegisterPage : Fragment() {
    private lateinit var regEdittextemail: EditText
    private lateinit var regEdittextnickname: EditText
    private lateinit var regEdittextpassword: EditText
    private lateinit var regButton: Button
    private lateinit var goBackButton: ImageView
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseFirestore

    // function for Toasts
    private fun showDialog(s: String){
        Toast.makeText(activity, s, Toast.LENGTH_SHORT).show()
    }

    // Text formatting function
    private fun stringFormat(str: EditText): String{
        return str.text.toString().trim()
    }

    // Email validation
    private fun isValidEmail(email: EditText): String {
        val pattern = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}\$")
        val matcher = pattern.matcher(stringFormat(email))
        return when {
            stringFormat(email).isEmpty() -> {
                "Pole e-mail jest puste!"
            }
            !matcher.matches() -> {
                "Nieprawidłowy e-mail!"
            }
            else -> {
                "true"
            }
        }
    }

    // account creation
    private fun createAccount(email: String, password: String, avatar: String, nickname: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d("myTag", "createUserWithEmail:success")
                    val uid = auth.currentUser!!.uid
                    Log.d("myTagUID", uid)
                    val user = hashMapOf(
                        "avatar_src" to avatar,
                        "email" to email,
                        "nickname" to nickname,
                        "uid" to uid
                    )
                    val realUser = hashMapOf(
                        "avatarSrc" to avatar,
                        "nickname" to nickname,
                        "nicknameLC" to nickname.lowercase(),
                        "status" to "offline",
                        "uid" to uid
                    )
                    val realTime = FirebaseDatabase.getInstance().reference

                    database.collection("users").document(uid).set(user)
                    realTime.child("users").child(uid).setValue(realUser)

                    findNavController().navigate(R.id.action_registerPage_to_loginPage)
                    showDialog("Konto zostało utworzone!.")
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w("myTag", "createUserWithEmail:failure", task.exception)
                    showDialog("Autentykacja nie powiodła się.")
                }
            }
    }

    // main function
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreate(savedInstanceState)
        val view = inflater.inflate(R.layout.fragment_register_page_light, container, false)

        regEdittextemail = view.findViewById(R.id.reg_email)
        regEdittextnickname = view.findViewById(R.id.reg_username)
        regEdittextpassword = view.findViewById(R.id.reg_password)
        regButton = view.findViewById(R.id.register_button)
        goBackButton = view.findViewById(R.id.goBackButton)

        auth = Firebase.auth
        database = Firebase.firestore

        goBackButton.setOnClickListener{
            findNavController().navigate(R.id.action_registerPage_to_loginPage)
        }

        regButton.setOnClickListener {
                val valid = isValidEmail(regEdittextemail)
                if(valid == "true"){
                    database.collection("users").whereEqualTo("nickname", stringFormat(regEdittextnickname))
                        .get()
                        .addOnSuccessListener { querySnapshot ->
                            if (!querySnapshot.isEmpty) {
                                for (document in querySnapshot) {
                                    val userData = document.data
                                    Log.d("UserData", "$userData")
                                    showDialog("Nazwa użytkownika jest już zajęta!!")
                                }
                            } else {
                                Log.d("UserData", "Nazwa użytkownika jest wolna")
                                createAccount(stringFormat(regEdittextemail),
                                    stringFormat(regEdittextpassword),
                                    "gs://dyplom-cb250.appspot.com/avatar.png",
                                    stringFormat(regEdittextnickname))
                            }
                        }
                        .addOnFailureListener { e ->
                            Log.e("FireBaseError","$e")
                        }
                } else {
                    showDialog(valid)
                }
        }
        return view
    }
}