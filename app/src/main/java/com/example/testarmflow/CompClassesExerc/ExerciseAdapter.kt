package com.example.testarmflow.CompClassesExerc

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.testarmflow.R
import com.example.testarmflow.exercFrag.ExerciseActivity
import com.google.android.material.imageview.ShapeableImageView
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.squareup.picasso.Picasso


class ExerciseAdapter(mContext: Context, mExercises: List<Exercises>) : RecyclerView.Adapter<ExerciseAdapter.ViewHolder?>()
{
    private val mContext: Context
    private val mExercises: List<Exercises>

    init {
        this.mContext = mContext
        this.mExercises = mExercises
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExerciseAdapter.ViewHolder {
        val view: View = LayoutInflater.from(mContext).inflate(R.layout.exercise_item_layout, parent, false)
        return ExerciseAdapter.ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return mExercises.size
    }

    override fun onBindViewHolder(holder: ExerciseAdapter.ViewHolder, position: Int) {
        val exercise: Exercises = mExercises[position]
        holder.exerciseName.text = exercise!!.getName()
        val storage = Firebase.storage.getReferenceFromUrl(exercise.getMedia1().toString())
        storage.downloadUrl
            .addOnSuccessListener{ uri ->
                Picasso
                    .get()
                    .load(uri.toString())
                    .fit()
                    .centerCrop()
                    .into(holder.exerciseImage)
            }

        holder.itemView.bringToFront()
        holder.itemView.setOnClickListener{
            val intent = Intent(mContext, ExerciseActivity::class.java)
            intent.putExtra("eid", exercise.getID())
            mContext.startActivity(intent)
        }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        var exerciseImage: ShapeableImageView
        var exerciseName: TextView

        init {
            exerciseImage = itemView.findViewById(R.id.exercise_image)
            exerciseName = itemView.findViewById(R.id.exercise_name)
        }
    }

    private fun showDialog(s: String){
        Toast.makeText(mContext, s, Toast.LENGTH_SHORT).show()
    }
}