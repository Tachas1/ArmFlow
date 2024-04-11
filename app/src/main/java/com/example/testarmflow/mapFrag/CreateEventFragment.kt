package com.example.testarmflow.mapFrag

import android.app.ProgressDialog
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.FragmentManager
import com.example.testarmflow.CompClassesMap.Events
import com.example.testarmflow.R
import com.example.testarmflow.databinding.FragmentCreateEventBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class CreateEventFragment : Fragment() {
    private lateinit var database: FirebaseDatabase
    private lateinit var auth: FirebaseAuth
    private lateinit var progressBar: ProgressDialog

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val sharedPreferences1: SharedPreferences = requireContext().getSharedPreferences("moje_pref", Context.MODE_PRIVATE)
        val mt = sharedPreferences1.getBoolean("motyw", true)

        val motyw = if(mt){
            R.layout.fragment_create_event_light
        }else{
            R.layout.fragment_create_event
        }

        val view = inflater.inflate(motyw, container, false)
        progressBar = ProgressDialog(requireContext())
        progressBar.setProgressStyle(ProgressDialog.STYLE_SPINNER)
        progressBar.setTitle("Dodawanie...")

        val latitudeTxt: TextView = view.findViewById(R.id.latitudeTxt)
        val longitudeTxt: TextView = view.findViewById(R.id.longitudeTxt)
        val createSubmit: Button = view.findViewById(R.id.createSubmit)
        val addressEditText: EditText = view.findViewById(R.id.addressEditText)
        val addressEditText2: EditText = view.findViewById(R.id.addressEditText2)
        val headLineEditText: EditText = view.findViewById(R.id.headLineEditText)
        val descriptionEditText: EditText = view.findViewById(R.id.descriptionEditText)
        val createEventBack: ImageView = view.findViewById(R.id.createEventBack)

        val bundle = this.arguments
        val latVal = bundle!!.getString("latitude")
        val longVal = bundle!!.getString("longitude")

        latitudeTxt.text = ("$latVal")
        longitudeTxt.text = ("$longVal")

        database = FirebaseDatabase.getInstance()
        auth = FirebaseAuth.getInstance()

        createSubmit.setOnClickListener {

            progressBar.show()
            val event = Events()
            event.setUID(auth.currentUser!!.uid)
            event.setLatitude(latVal!!.toDouble())
            event.setLongitude(longVal!!.toDouble())
            event.setAddress(addressEditText.text.toString().trim())
            event.setAddressTwo(addressEditText2.text.toString().trim())
            event.setHeadLine(headLineEditText.text.toString().trim())
            event.setDescription(descriptionEditText.text.toString().trim())
            event.setEventID(database.reference.push().key)

            database.reference.child("events").child(event.getEventID()!!).setValue(event).addOnSuccessListener{
                Toast.makeText(requireContext(), "Wydarzenie utworzone!", Toast.LENGTH_SHORT).show()

                progressBar.dismiss()
                requireActivity().supportFragmentManager.popBackStack("fragments_map", FragmentManager.POP_BACK_STACK_INCLUSIVE)
            }
        }

        createEventBack.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack("fragments_map", FragmentManager.POP_BACK_STACK_INCLUSIVE)
        }

        return view
    }
}
