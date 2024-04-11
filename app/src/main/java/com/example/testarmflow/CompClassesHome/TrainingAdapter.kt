package com.example.testarmflow.CompClassesHome

import android.content.Context
import android.content.SharedPreferences
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.testarmflow.R
import com.example.testarmflow.mapFrag.MyEventsFragment

class TrainingAdapter(private val mContext: Context,
                      private val mTrainings: List<String>,
                      private val mDescription: List<String>,
                      private val onItemClickListener: OnItemClickListener)
    : RecyclerView.Adapter<TrainingAdapter.ViewHolder>() {

    interface OnItemClickListener {
        fun onItemClick(position: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val sharedPreferences1: SharedPreferences = mContext.getSharedPreferences("moje_pref", Context.MODE_PRIVATE)
        val mt = sharedPreferences1.getBoolean("motyw", true)

        val motyw = if(mt){
            R.layout.training_item_layout_light
        }else{
            R.layout.training_item_layout
        }

        val view: View = LayoutInflater.from(mContext).inflate(motyw, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val name: String = mTrainings[position]
        val desc: String = mDescription[position]
        holder.name.text = name
        holder.desc.text = desc

        holder.itemView.setOnClickListener {
            onItemClickListener.onItemClick(position)
        }
    }

    override fun getItemCount(): Int {
        return mTrainings.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val name: TextView = itemView.findViewById(R.id.trainingHeadLine)
        val desc: TextView = itemView.findViewById(R.id.trainingDescription)
    }
}
