package com.example.testarmflow.exercFrag

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.testarmflow.CompClassesExerc.ExerciseAdapter
import com.example.testarmflow.CompClassesExerc.Exercises
import com.example.testarmflow.R
import com.example.testarmflow.databinding.FragmentExercisesPageBinding
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class ExercisesPage : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var exerciseAdapter: ExerciseAdapter
    private lateinit var mExercises: MutableList<Exercises>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val sharedPreferences1: SharedPreferences = requireContext().getSharedPreferences("moje_pref", Context.MODE_PRIVATE)
        val mt = sharedPreferences1.getBoolean("motyw", true)

        val motyw = if(mt){
            R.layout.fragment_exercises_page_light
        }else{
            R.layout.fragment_exercises_page
        }

        val view = inflater.inflate(motyw, container, false)

        recyclerView = view.findViewById(R.id.exercise_list)
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(context)

        mExercises = mutableListOf()
        retrieveAllExercises()

        return view
    }

    private fun retrieveAllExercises() {
        val refUsers = Firebase.firestore.collection("exercises")

        refUsers.get().addOnSuccessListener { documents ->
            val exercisesList = mutableListOf<Exercises>()

            for (document in documents) {
                val exercise = document.toObject(Exercises::class.java)
                exercisesList.add(exercise)
            }

            // Sprawdzenie, czy RecyclerView istnieje i czy jest widoczny
            if (recyclerView != null && recyclerView.isVisible) {
                mExercises.clear()
                mExercises.addAll(exercisesList)

                // Sprawdzenie, czy RecyclerView ma przypisany adapter
                if (recyclerView.adapter == null) {
                    exerciseAdapter = ExerciseAdapter(requireContext(), mExercises)
                    recyclerView.adapter = exerciseAdapter
                } else {
                    exerciseAdapter.notifyDataSetChanged() // Odświeżenie danych
                }
            }
        }
    }

}
