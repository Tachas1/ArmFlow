package com.example.testarmflow.CompClassesHome

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.testarmflow.R

class TrainingPlanAdapter(private val mContext: Context,
                          private val mTrainingDays: List<String>,
                          private val mDayDescription: List<String>)
    : RecyclerView.Adapter<TrainingPlanAdapter.ViewHolder?>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrainingPlanAdapter.ViewHolder {
        val view: View = LayoutInflater.from(mContext).inflate(R.layout.training_plan_layout, parent, false)
        return TrainingPlanAdapter.ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val day = mTrainingDays[position]
        val desc = mDayDescription[position]

        holder.day.text = day
        holder.desc.text = desc
        if (holder.desc.text == "> Odpoczynek"){
            holder.icon.setImageResource(R.drawable.baseline_self_improvement_24)
            holder.container.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(mContext, R.color.trans_gray))
        }
        else {
            holder.icon.setImageResource(R.drawable.baseline_fitness_center_24)
            holder.container.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(mContext, R.color.trans_dark_blue_less))
        }
    }

    override fun getItemCount(): Int {
        return mTrainingDays.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val day: TextView = itemView.findViewById(R.id.trainingDay)
        val desc: TextView = itemView.findViewById(R.id.trainingDayDesc)
        val icon: ImageView = itemView.findViewById(R.id.trainingDayIcon)
        val container: RelativeLayout = itemView.findViewById(R.id.trainingDayContainer)
    }

}