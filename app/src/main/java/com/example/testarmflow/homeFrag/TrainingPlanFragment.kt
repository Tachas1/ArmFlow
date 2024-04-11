package com.example.testarmflow.homeFrag

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.testarmflow.CompClassesHome.CalendarDays
import com.example.testarmflow.CompClassesHome.TrainingPlanAdapter
import com.example.testarmflow.CompClassesMap.EventAdapter
import com.example.testarmflow.CompClassesMap.Events
import com.example.testarmflow.R
import com.example.testarmflow.databinding.FragmentTrainingPlanBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class TrainingPlanFragment : Fragment() {
    private lateinit var view: View
    private lateinit var recyclerView: RecyclerView
    private lateinit var trainingPlanAdapter: TrainingPlanAdapter
    private lateinit var mTrainingDays: MutableList<String>
    private lateinit var mDayDescription: MutableList<String>
    private lateinit var mAddDays: MutableList<CalendarDays>
    private lateinit var mAddDayTitle: MutableList<String>
    private lateinit var mAddDayIndex: MutableList<Int>
    private lateinit var mAddDayDescription: MutableList<String>
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mTrainingDays = mutableListOf()
        mDayDescription = mutableListOf()
        mAddDays = mutableListOf()

        mAddDayTitle = mutableListOf()
        mAddDayIndex = mutableListOf()
        mAddDayDescription = mutableListOf()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        view = inflater.inflate(R.layout.fragment_training_plan, container, false)

        val bundle = this.arguments
        val trainingName = bundle?.getString("trainingName")
        val trainingDesc = bundle?.getString("trainingDesc")

        recyclerView = view.findViewById(R.id.trainingPlanRecyclerView)
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(context)

        val trainingPlanTitle = view.findViewById<TextView>(R.id.trainingPlanTitle)
        val trainingPlanDesc = view.findViewById<TextView>(R.id.trainingPlanDesc)
        trainingPlanTitle.text = trainingName
        trainingPlanDesc.text = trainingDesc

        auth = FirebaseAuth.getInstance()
        database = FirebaseFirestore.getInstance()
        val collection = database.collection("calendar")

        view.apply {
            val trainingPlanAddToCalendar = findViewById<ImageView>(R.id.trainingPlanAddToCalendar)
            val trainingPlanAddClose = findViewById<ImageView>(R.id.trainingPlanAddClose)
            val trainingPlanReturn = findViewById<ImageView>(R.id.trainingPlanReturn)
            val trainingPlanSubmit = findViewById<Button>(R.id.trainingPlanSubmit)
            val trainingPlanStartDate = findViewById<TextView>(R.id.trainingPlanStartDate)

            trainingPlanAddToCalendar.setOnClickListener {
                findViewById<RelativeLayout>(R.id.trainingPlanAddContainer).visibility = View.VISIBLE
                findViewById<RelativeLayout>(R.id.trainingPlanAddContainer).isClickable = true
            }
            trainingPlanAddClose.setOnClickListener {
                findViewById<RelativeLayout>(R.id.trainingPlanAddContainer).visibility = View.GONE
                findViewById<RelativeLayout>(R.id.trainingPlanAddContainer).isClickable = false
            }
            trainingPlanReturn.setOnClickListener {
                requireActivity().supportFragmentManager.popBackStack("fragments_training", FragmentManager.POP_BACK_STACK_INCLUSIVE)
            }
            trainingPlanSubmit.setOnClickListener {
                (mAddDays as ArrayList<CalendarDays>).clear()
                for (i in mTrainingDays.size - 1 downTo 0) {
                    val title = mTrainingDays[i]
                    val description = mDayDescription[i]
                    val date = addDaysToDate(
                        trainingPlanStartDate.text.toString().trim(),
                        i
                    )
                        .split(".")
                    val singleExercise = CalendarDays(
                        "${date[0].toInt()}.${date[1].toInt()}.${date[2].toInt()}",
                        title!!,
                        description!!,
                        auth.currentUser!!.uid,
                        "false"
                    )
                    (mAddDays as ArrayList<CalendarDays>).add(singleExercise)
                }
                lifecycleScope.launch {
                    try {
                        for (exercise in mAddDays) {
                            collection.add(exercise).await()
                        }
                        Toast.makeText(
                            requireContext(),
                            "Dodano trening do kalendarza!",
                            Toast.LENGTH_SHORT
                        ).show()
                        requireActivity().supportFragmentManager.popBackStack(
                            "fragments_training",
                            FragmentManager.POP_BACK_STACK_INCLUSIVE
                        )
                    } catch (e: Exception) {
                        findViewById<RelativeLayout>(R.id.trainingPlanAddContainer).visibility = View.GONE
                        findViewById<RelativeLayout>(R.id.trainingPlanAddContainer).isClickable = false
                        Toast.makeText(
                            requireContext(),
                            "NIEPOWODZENIE!",
                            Toast.LENGTH_SHORT
                        ).show()
                        Log.e("TrainingError", "Exception: $e")
                    }
                }
            }
        }

        val refTrainings = FirebaseDatabase.getInstance().reference.child("trainings")
        var index = 0
        refTrainings.orderByChild("name").equalTo(trainingName).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (isAdded) {
                    (mTrainingDays as ArrayList<String>).clear()
                    (mDayDescription as ArrayList<String>).clear()
                    for (trainingSnapshot in snapshot.children) {
                        for (daysSnapshot in trainingSnapshot.child("days").children) {
                            val stringBuilder = StringBuilder()
                            for (exerciseSnapshot in daysSnapshot.children) {
                                val title = exerciseSnapshot.child("title").getValue(String::class.java)
                                val description = exerciseSnapshot.child("description").getValue(String::class.java)
                                if (description != "") {
                                    stringBuilder.append("> $title ($description)\n")
                                }
                                else{
                                    stringBuilder.append("> $title")
                                }
                            }
                            index++
                            (mTrainingDays as ArrayList<String>).add("Dzień $index")
                            (mDayDescription as ArrayList<String>).add(stringBuilder.toString())
                        }
                    }
                    val trainingPlanAdapter = TrainingPlanAdapter(requireContext(), mTrainingDays, mDayDescription)
                    recyclerView.adapter = trainingPlanAdapter
                }
            }
            override fun onCancelled(error: DatabaseError) {
                // Obsługa błędu
            }
        })

        return view
    }

    private fun addDaysToDate(startDate: String, amount: Int): String {
        val formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd")
        val data = LocalDate.parse(startDate, formatter)

        val dataPoDodaniu = data.plusDays(amount.toLong())

        return dataPoDodaniu.format(formatter)
    }
}
