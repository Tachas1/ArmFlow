package com.example.testarmflow.mapFrag

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.testarmflow.CompClassesMap.EventAdapter
import com.example.testarmflow.CompClassesMap.Events
import com.example.testarmflow.R
import com.example.testarmflow.databinding.FragmentMyEventsBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class MyEventsFragment : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var eventAdapter: EventAdapter
    private lateinit var mEvents: MutableList<Events>

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
            R.layout.fragment_my_events_light
        }else{
            R.layout.fragment_my_events
        }

        val view = inflater.inflate(motyw, container, false)

        recyclerView = view.findViewById(R.id.myEventsList)
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(context)

        view.findViewById<ImageView>(R.id.myEventsGoBack).setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack("fragments_map", FragmentManager.POP_BACK_STACK_INCLUSIVE)
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mEvents = mutableListOf()
        retrieveAllMyEvents()
    }

    private fun retrieveAllMyEvents() {
        val auth = FirebaseAuth.getInstance().currentUser
        val refEvents = FirebaseDatabase.getInstance().reference.child("events")

        refEvents.orderByChild("uid").equalTo(auth!!.uid).addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if (isAdded) {
                    mEvents.clear()
                    for (dataSnapshot in snapshot.children) {
                        val event = dataSnapshot.getValue(Events::class.java)
                        mEvents.add(event!!)
                    }
                    eventAdapter = EventAdapter(requireContext(), mEvents)
                    recyclerView.adapter = eventAdapter
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }
}
