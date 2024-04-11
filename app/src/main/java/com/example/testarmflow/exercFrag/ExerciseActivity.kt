package com.example.testarmflow.exercFrag

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.example.testarmflow.R
import com.example.testarmflow.databinding.ActivityExerciseBinding
import com.google.android.material.imageview.ShapeableImageView
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView
import com.squareup.picasso.Picasso

class ExerciseActivity : AppCompatActivity() {
    private lateinit var exerciseImage: ShapeableImageView
    private lateinit var exerciseImageTwo: ShapeableImageView
    private lateinit var exerciseName: TextView
    private lateinit var exerciseSource: TextView
    private lateinit var exerciseDescription: TextView
    private lateinit var exerciseTips: TextView
    private lateinit var exerciseGoBack: ImageView
    private lateinit var exerciseVideoContainer: LinearLayout
    private lateinit var exerciseVideo: YouTubePlayerView

    private lateinit var exerciseID: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val sharedPreferences1: SharedPreferences = applicationContext.getSharedPreferences("moje_pref", Context.MODE_PRIVATE)
        val mt = sharedPreferences1.getBoolean("motyw", true)

        val motyw = if(mt){
            R.layout.activity_exercise_light
        }else{
            R.layout.activity_exercise
        }

        setContentView(motyw)

        exerciseImage = findViewById(R.id.exercise_image)
        exerciseImageTwo = findViewById(R.id.exercise_image_two)
        exerciseName = findViewById(R.id.exercise_name)
        exerciseSource = findViewById(R.id.exercise_source)
        exerciseDescription = findViewById(R.id.exercise_description)
        exerciseTips = findViewById(R.id.exercise_tips)
        exerciseGoBack = findViewById(R.id.exerciseGoBack)
        exerciseVideoContainer = findViewById(R.id.exercise_video_container)
        exerciseVideo = findViewById(R.id.exercise_video)
        lifecycle.addObserver(exerciseVideo)

        exerciseID = intent.getStringExtra("eid")!!
        val database = Firebase.firestore
        val reference = database.collection("exercises")

        reference
            .whereEqualTo("id", exerciseID)
            .get()
            .addOnSuccessListener { results ->
                for (result in results) {
                    if (!result.data?.get("name").toString().contains("Youtube")) {
                        val gsReference =
                            Firebase.storage.getReferenceFromUrl(
                                result.data?.get("media1").toString()
                            )
                        gsReference.downloadUrl.addOnSuccessListener {
                            Picasso
                                .get()
                                .load(it)
                                .fit()
                                .centerCrop()
                                .into(exerciseImage)
                        }
                        if (result.data?.get("media2").toString() != "") {
                            exerciseImageTwo.visibility = View.VISIBLE
                            Firebase.storage.getReferenceFromUrl(
                                result.data?.get("media2").toString()
                            )
                                .downloadUrl.addOnSuccessListener {
                                    Picasso
                                        .get()
                                        .load(it)
                                        .fit()
                                        .centerCrop()
                                        .into(exerciseImageTwo)
                                }
                        }
                        exerciseName.text = result.data?.get("name").toString()
                        exerciseSource.text = result.data?.get("source").toString()
                        exerciseDescription.text = result.data?.get("description").toString()
                        exerciseTips.text = result.data?.get("tips").toString().replace("_b", "\n")
                    } else {
                        exerciseImage.visibility = View.GONE
                        exerciseVideoContainer.visibility = View.VISIBLE
                        exerciseName.text = result.data?.get("name").toString()
                        exerciseSource.text = result.data?.get("source").toString()
                        exerciseDescription.text = result.data?.get("description").toString()
                        exerciseTips.text = result.data?.get("tips").toString().replace("_b", "\n")
                        exerciseVideo.addYouTubePlayerListener(object : AbstractYouTubePlayerListener(){
                            override fun onReady(youTubePlayer: YouTubePlayer) {
                                val videoId = result.data?.get("media2")
                                youTubePlayer.loadVideo(videoId.toString(), 0F)
                            }
                        })
                    }
                }
            }

        exerciseGoBack.setOnClickListener {
            finish()
        }
    }
}
