package com.example.testarmflow

import android.app.AlarmManager
import android.app.AlertDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.ColorStateList
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI.setupWithNavController
import com.example.testarmflow.CompClassesCalendar.AlarmReceiver
import com.example.testarmflow.databinding.ActivityMainHomeBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.Firebase
import com.google.firebase.appcheck.appCheck
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.initialize
import java.time.LocalDate
import java.time.LocalTime
import java.util.Calendar
import java.util.Date

class MainHomeActivity : AppCompatActivity() {
    private lateinit var navController: NavController
    private lateinit var binding: ActivityMainHomeBinding
    private var auth: FirebaseUser? = null
    private lateinit var db: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val sharedPreferences1: SharedPreferences = applicationContext.getSharedPreferences("moje_pref", Context.MODE_PRIVATE)
        val mt = sharedPreferences1.getBoolean("motyw", true)

        val motyw = if(mt){
            R.color.trans_black
        }else{
            R.color.trans_white
        }

        Firebase.initialize(context = this)
        Firebase.appCheck.installAppCheckProviderFactory(
            PlayIntegrityAppCheckProviderFactory.getInstance(),
        )

        binding = ActivityMainHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = FirebaseAuth.getInstance().currentUser
        val nav = findViewById<BottomNavigationView>(R.id.bottomNavView)
        nav.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(applicationContext, motyw))


        val navHostFragment = supportFragmentManager.findFragmentById(R.id.main_home_fragment) as? NavHostFragment
        navHostFragment?.let { fragment ->
            navController = fragment.navController
            val bottomNavigationView = nav
            setupWithNavController(bottomNavigationView, navController)
        }

    }

    private fun updateStatus(status: String){
        val ref = FirebaseDatabase.getInstance().reference
            .child("users").child(auth!!.uid)

        val hashMap = HashMap<String, Any>()
        hashMap["status"] = status
        ref.updateChildren(hashMap)
    }

    override fun onResume() {
        super.onResume()
        updateStatus("online")
    }

    override fun onPause() {
        super.onPause()

        updateStatus("offline")
    }

}