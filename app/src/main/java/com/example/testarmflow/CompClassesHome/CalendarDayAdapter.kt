package com.example.testarmflow.CompClassesHome

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.testarmflow.CompClassesMap.EventAdapter
import com.example.testarmflow.CompClassesMap.Events
import com.example.testarmflow.R

class CalendarDayAdapter(mContext: Context, mCalendarDays: List<CalendarDays>) : RecyclerView.Adapter<CalendarDayAdapter.ViewHolder?>() {
    private val mContext: Context
    private val mCalendarDays: List<CalendarDays>

    init {
        this.mContext = mContext
        this.mCalendarDays = mCalendarDays
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CalendarDayAdapter.ViewHolder {
        val view: View = LayoutInflater.from(mContext).inflate(R.layout.calendar_day_todo_item, parent, false)
        return CalendarDayAdapter.ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val date: CalendarDays = mCalendarDays[position]
        holder.title.text = date.getTitle()
        holder.description.text = date.getDescription()
        if (date.getByUser()=="true") {
            holder.byUser.visibility = View.VISIBLE
        }
        else{
            holder.byUser.setImageResource(R.drawable.dumbbell_1)
        }
        if (date.getDescription()==""){
            holder.description.visibility = View.GONE
        }
        holder.itemView.bringToFront()
    }

    override fun getItemCount(): Int {
        return mCalendarDays.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        var title: TextView
        var byUser: ImageView
        var description: TextView

        init {
            title = itemView.findViewById(R.id.toDoTitle)
            byUser = itemView.findViewById(R.id.toDoByUser)
            description = itemView.findViewById(R.id.toDoDescription)
        }
    }

}