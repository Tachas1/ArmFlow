package com.example.testarmflow.homeFrag

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
import com.example.testarmflow.CompClassesHome.CalendarDays
import com.example.testarmflow.CompClassesMap.Events
import com.example.testarmflow.R
import com.example.testarmflow.databinding.FragmentCreateCalenEventBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore

class CreateCalenEventFragment : Fragment() {
    private lateinit var view: View
    private lateinit var database: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val sharedPreferences1: SharedPreferences = requireContext().getSharedPreferences("moje_pref", Context.MODE_PRIVATE)
        val mt = sharedPreferences1.getBoolean("motyw", true)

        val motyw = if(mt){
            R.layout.fragment_create_calen_event_light
        }else{
            R.layout.fragment_create_calen_event
        }

        view = inflater.inflate(motyw, container, false)

        val bundle = this.arguments
        val day = bundle!!.getString("day")
        val month = bundle!!.getString("month")
        val year = bundle!!.getString("year")

        val createCalenEventDate = view.findViewById<TextView>(R.id.createCalenEventDate)
        createCalenEventDate.text = "Data: $day.$month.$year"

        database = FirebaseFirestore.getInstance()
        val collection = database.collection("calendar")
        auth = FirebaseAuth.getInstance()

        view.apply {
            val calenCreateTitleTxt = findViewById<EditText>(R.id.calenCreateTitleTxt)
            val calenCreateDescTxt = findViewById<EditText>(R.id.calenCreateDescTxt)
            val calenCreateSubmit = findViewById<Button>(R.id.calenCreateSubmit)
            val createCalenEventBack = findViewById<ImageView>(R.id.createCalenEventBack)

            calenCreateSubmit.setOnClickListener {

                val calendarDay = CalendarDays(
                    "$year.$month.$day",
                    calenCreateTitleTxt.text.toString().trim(),
                    calenCreateDescTxt.text.toString().trim(),
                    auth.currentUser!!.uid,
                    "true"
                )

                collection.add(calendarDay).addOnSuccessListener {
                    Toast.makeText(requireContext(), "Wydarzenie utworzone!", Toast.LENGTH_SHORT).show()

                    requireActivity().supportFragmentManager.popBackStack("fragments_calendar", FragmentManager.POP_BACK_STACK_INCLUSIVE)
                }
            }
            createCalenEventBack.setOnClickListener {
                requireActivity().supportFragmentManager.popBackStack("fragments_calendar", FragmentManager.POP_BACK_STACK_INCLUSIVE)
            }
        }

        return view
    }
}
