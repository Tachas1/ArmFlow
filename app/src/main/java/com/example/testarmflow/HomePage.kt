package com.example.testarmflow

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.testarmflow.CompClassesCalendar.EventDecorator
import com.example.testarmflow.CompClassesHome.CalendarDayAdapter
import com.example.testarmflow.CompClassesHome.CalendarDays
import com.example.testarmflow.CompClassesHome.TrainingAdapter
import com.example.testarmflow.databinding.FragmentHomePageBinding
import com.example.testarmflow.homeFrag.CreateCalenEventFragment
import com.example.testarmflow.homeFrag.TrainingPlanFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.MaterialCalendarView
import java.time.LocalDate


class HomePage : Fragment(), TrainingAdapter.OnItemClickListener {
    private lateinit var view: View
    private lateinit var ref: FirebaseFirestore
    private lateinit var calendar: MaterialCalendarView
    private lateinit var recyclerView: RecyclerView
    private lateinit var recyclerViewTrain: RecyclerView
    private lateinit var bundle: Bundle
    private lateinit var auth: FirebaseUser
    private lateinit var refTrainings: FirebaseDatabase
    private lateinit var mTrainings: List<String>
    private lateinit var mDescription: List<String>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        ref = FirebaseFirestore.getInstance()
        refTrainings = FirebaseDatabase.getInstance()
        auth = FirebaseAuth.getInstance().currentUser!!

        mTrainings = ArrayList()
        mDescription = ArrayList()

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val sharedPreferences1: SharedPreferences = requireContext().getSharedPreferences("moje_pref", Context.MODE_PRIVATE)
        val mt = sharedPreferences1.getBoolean("motyw", true)

        val motyw = if(mt){
            R.layout.fragment_home_page_light
        }else{
            R.layout.fragment_home_page
        }

        // Inflate the layout for this fragment
        view = inflater.inflate(motyw, container, false)
        val refCalendar = FirebaseFirestore.getInstance().collection("calendar")
        calendar = view.findViewById(R.id.calendarView)
        val buttonToDoAdd:ImageView = view.findViewById(R.id.toDoAdd)

        recyclerView = view.findViewById(R.id.toDoRecycler)
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(context)

        recyclerViewTrain = view.findViewById(R.id.trainingsRecycler)
        recyclerViewTrain.setHasFixedSize(true)
        recyclerViewTrain.layoutManager = LinearLayoutManager(context)

        bundle = Bundle()
        val localDate = LocalDate.now()
        val currDay = localDate.dayOfMonth
        val currMonth = localDate.monthValue
        val currYear = localDate.year
        bundle.apply{
            putString("day", currDay.toString())
            putString("month", currMonth.toString())
            putString("year", currYear.toString())
        }

        val query = refCalendar
            .whereEqualTo("uid", auth.uid)

        query.addSnapshotListener { days, error ->
            if (error != null) {
                Log.e("FirestoreError", "Error fetching documents: ", error)
                return@addSnapshotListener
            }
            calendar.removeDecorators()
            val mCalendarDays = mutableListOf<CalendarDays>()
            for (day in days!!) {
                val calendarDay = day.toObject(CalendarDays::class.java)
                val date = calendarDay.getDate()!!.split(".")
                val day = CalendarDay.from(date[0].toInt(), date[1].toInt(), date[2].toInt())
                if(calendarDay.getDescription() !="> Odpoczynek") {
                    calendar.addDecorator(EventDecorator(day, Color.RED, ContextCompat.getColor(requireContext(), R.color.light_text_less)))
                }
                else{
                    calendar.addDecorator(EventDecorator(day, Color.GREEN, ContextCompat.getColor(requireContext(), R.color.light_text_less)))
                }
                if (calendarDay.getDate()=="$currYear.$currMonth.$currDay"){
                    mCalendarDays.add(calendarDay)
                }
            }
            recyclerView.adapter = CalendarDayAdapter(requireContext(), mCalendarDays)
            calendar.selectedDate = CalendarDay.from(currYear, currMonth, currDay)
        }

        calendar.setOnDateChangedListener { _, date, _ ->
            val day = date.day
            val month = date.month
            val year = date.year
            bundle.apply{
                putString("day", day.toString())
                putString("month", month.toString())
                putString("year", year.toString())
            }
            val query = refCalendar
                .whereEqualTo("uid", auth.uid)
                .whereEqualTo("date", "$year.$month.$day")

            query.addSnapshotListener { days, error ->
                if (error != null) {
                    Log.e("FirestoreError", "Error fetching documents: ", error)
                    return@addSnapshotListener
                }

                val mCalendarDays = mutableListOf<CalendarDays>()
                for (day in days!!) {
                    val calendarDay = day.toObject(CalendarDays::class.java)
                    mCalendarDays.add(calendarDay)
                }
                recyclerView.adapter = CalendarDayAdapter(requireContext(), mCalendarDays)
                }
        }

        buttonToDoAdd.setOnClickListener {
            val options = arrayOf<CharSequence>(
                "Tak",
                "Anuluj"
            )
            val builder: AlertDialog.Builder = AlertDialog.Builder(requireContext())
            builder.setTitle("Czy chcesz utworzyć wydarzenie w kalendarzu?")
            builder.setItems(options) { _, which ->
                if (which == 0) {
                    val fragment = CreateCalenEventFragment()
                    fragment.arguments = bundle
                    val transaction = requireActivity().supportFragmentManager.beginTransaction()
                    transaction.replace(view.id, fragment)
                        .setReorderingAllowed(true)
                        .addToBackStack("fragments_calendar")
                        .commit()
                }
            }
            builder.show()
        }

        refTrainings.reference.child("trainings").addValueEventListener(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                (mTrainings as ArrayList<String>).clear()
                for(dataSnapshot in snapshot.children){
                    val training = dataSnapshot.child("name").getValue(String::class.java)
                    val description = dataSnapshot.child("description").getValue(String::class.java)
                    (mTrainings as ArrayList<String>).add(training!!)
                    (mDescription as ArrayList<String>).add(description!!)
                }
                recyclerViewTrain.adapter = TrainingAdapter(requireContext(), mTrainings, mDescription,this@HomePage)
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })

        return view
    }

    override fun onItemClick(position: Int) {
        bundle.apply{
            putString("trainingName", "${mTrainings[position]}")
            putString("trainingDesc", "${mDescription[position]}")
        }
        val fragment = TrainingPlanFragment()
        fragment.arguments = bundle
        val transaction = requireActivity().supportFragmentManager.beginTransaction()
        transaction.replace(view.id, fragment)
        transaction.setReorderingAllowed(true)
        transaction.addToBackStack("fragments_training")
        transaction.commit()
    }

}