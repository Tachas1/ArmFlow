@file:Suppress("DEPRECATION")

package com.example.testarmflow

import android.app.Activity
import android.app.AlarmManager
import android.app.PendingIntent
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.ColorStateList
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService
import com.example.testarmflow.CompClassesCalendar.AlarmReceiver
import com.example.testarmflow.welcomeFrag.MainActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import com.squareup.picasso.Picasso
import java.lang.Exception
import java.util.Calendar


class UserProfilePage : Fragment() {
    private lateinit var nickNameText: TextView
    private lateinit var emailText: TextView
    private lateinit var logoutButton: Button
    private lateinit var avatarImage: ImageView
    private lateinit var storage: FirebaseStorage
    private lateinit var auth: FirebaseUser
    private var imageUri: Uri? = null
    private val RequestCode = 438

    private fun showDialog(s: String){
        Toast.makeText(activity, s, Toast.LENGTH_SHORT).show()
    }

    private fun pickImage(){
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(intent, RequestCode)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == RequestCode && resultCode == Activity.RESULT_OK && data!!.data!=null){
            imageUri = data.data
            uploadImageToDatabase()
        }
    }

    private fun uploadImageToDatabase(){
        val progressBar = ProgressDialog(context)
        progressBar.setMessage("Wgrywanie...")
        progressBar.show()
        val database= Firebase.firestore
        val docRef = database!!.collection("users").document(auth.uid)
        imageUri?.let {
            storage.reference.child(auth.uid).putFile(it).addOnCompleteListener{ task ->
                if(task.isSuccessful){
                    storage.reference.child(auth.uid).downloadUrl.addOnSuccessListener{ uri ->
                        docRef.update("avatar_src", uri).addOnCompleteListener{ finish ->
                            if (finish.isSuccessful){
                                showDialog("Zdjecie zaktualizowane pomyslnie!")
                            }
                        }
                        val realTime = FirebaseDatabase.getInstance().reference
                        realTime.child("users").child(auth.uid).child("avatarSrc").setValue(uri.toString())
                    }
                    progressBar.dismiss()
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val sharedPreferences1: SharedPreferences = requireContext().getSharedPreferences("moje_pref", Context.MODE_PRIVATE)
        val mt = sharedPreferences1.getBoolean("motyw", true)

        val motyw = if(mt){
            R.layout.fragment_user_profile_page_light
        }else{
            R.layout.fragment_user_profile_page
        }

        val theme = if(mt){
            "Zmień motyw: jasny"
        }else{
            "Zmień motyw: ciemny"
        }

        val color = if(mt){
            R.color.trans_white
        }else{
            R.color.trans_black
        }

        val view = inflater.inflate(motyw, container, false)
        // Inflate the layout for this fragment
        val database= Firebase.firestore
        nickNameText = view.findViewById(R.id.ProfileNickname)
        emailText = view.findViewById(R.id.ProfileEmail)
        avatarImage = view.findViewById(R.id.ProfileImage)
        logoutButton = view.findViewById(R.id.logoutButton)
        storage = Firebase.storage
        auth = FirebaseAuth.getInstance().currentUser!!

        val docRef = database!!.collection("users").document(auth.uid)


        val setAlert: Button = view.findViewById(R.id.setAlertTime)
        val setTheme: Button = view.findViewById(R.id.changeYourTheme)
        val closeAlert: ImageView = view.findViewById(R.id.setAlarmClose)
        val alertContainer: RelativeLayout = view.findViewById(R.id.setAlarmContainer)
        val alertSubmit: Button = view.findViewById(R.id.setAlertSubmit)
        val alertTime: EditText = view.findViewById(R.id.setAlertEditText)
        setTheme.text = theme
        setTheme.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), color))

        setAlert.setOnClickListener{
                alertContainer.visibility = View.VISIBLE
                alertContainer.isClickable = true

        }
        closeAlert.setOnClickListener {
            alertContainer.visibility = View.GONE
            alertContainer.isClickable = false
        }

        alertSubmit.setOnClickListener{
            val realTime = FirebaseDatabase.getInstance().reference
            realTime.child("users").child(auth!!.uid).child("notifyTime").setValue(alertTime.text.toString().trim())
            alertContainer.visibility = View.GONE
            alertContainer.isClickable = false
            setAlarm(alertTime.text.toString().trim())
        }

        setTheme.setOnClickListener {
            val sharedPreferences: SharedPreferences = requireContext().getSharedPreferences("moje_pref", Context.MODE_PRIVATE)
            val editor: SharedPreferences.Editor = sharedPreferences.edit()
            // Sprawdź aktualny motyw
            val currentTheme = sharedPreferences.getBoolean("motyw", false)
            // Zmiana motywu i tekstu przycisku
            if (currentTheme) {
                // Jeśli aktualny motyw to czarny, zmień na ciemny
                editor.putBoolean("motyw", false)
                editor.apply()
                setTheme.text = "Zmień motyw: ciemny"
                setTheme.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.trans_black))
                showDialog("Motyw ustawi się po ponownym uruchomieniu aplikacji!")
            } else if(!currentTheme) {
                // Jeśli aktualny motyw to jasny, zmień na jasny
                editor.putBoolean("motyw", true)
                editor.apply()
                setTheme.text = "Zmień motyw: jasny"
                setTheme.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.trans_white))
                showDialog("Motyw ustawi się po ponownym uruchomieniu aplikacji!")
            }
        }

        docRef
            .get()
            .addOnSuccessListener{ result ->
                Log.d("UID", auth.uid)
                nickNameText.text = result.data?.get("nickname").toString()
                emailText.text = result.data?.get("email").toString()
                val gsReference = storage.getReferenceFromUrl(result.data?.get("avatar_src").toString())

                gsReference.downloadUrl.addOnSuccessListener {
                    Picasso
                        .get()
                        .load(it)
                        .fit()
                        .centerCrop()
                        .into(avatarImage)
                }.addOnFailureListener { e ->
                    Log.e("ImageError", "Image loading failure! [$e]")
                }
            }
            .addOnFailureListener{ e ->
                showDialog("Failed to retrieve data from the database. [$e]")
            }
        docRef.addSnapshotListener{value, error ->
            if(error != null){
                Log.i("SnapShot", "Listen failed!")
            }
            nickNameText.text = value!!.data?.get("nickname").toString()
            emailText.text = value!!.data?.get("email").toString()
            val gsReference = storage.getReferenceFromUrl(value.data?.get("avatar_src").toString())
            gsReference.downloadUrl.addOnSuccessListener { urlImage ->
                Picasso
                    .get()
                    .load(urlImage)
                    .fit()
                    .centerCrop()
                    .into(avatarImage)
            }.addOnFailureListener { e ->
                Log.e("ImageError", "Image loading failure! [$e]")
            }
        }

        avatarImage.setOnClickListener {
            pickImage()
        }

        logoutButton.setOnClickListener {
            val user = FirebaseAuth.getInstance()
            try{
                val realTime = FirebaseDatabase.getInstance().reference
                realTime.child("users").child(user.currentUser!!.uid).child("status").setValue("offline")

                user.signOut()
                showDialog("User Sign Out!")
                Log.i("Signout", "successful")
                requireActivity().finish()
            }
            catch (e: Exception){
                Log.e("Signout", "Exception: " + e.message, e)
            }
        }
        return view
    }

    private fun setAlarm(time: String) {
        Log.d("Timer1", "$time")
        val alarmManager = requireActivity().getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val splitTime = time.split(":")
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, splitTime[0].toInt())
            set(Calendar.MINUTE, splitTime[1].toInt())
            set(Calendar.SECOND, 0)
        }
        val intent = Intent(requireContext(), AlarmReceiver::class.java)
            .apply { putExtra("UID", auth!!.uid) }
        val pendingIntent = PendingIntent.getBroadcast(
            requireContext(),
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.setInexactRepeating(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            AlarmManager.INTERVAL_DAY,
            pendingIntent
        )
        showDialog("Ustawiono godzinę alarmu na ${splitTime[0].toInt()}:${splitTime[1].toInt()}")
    }
}