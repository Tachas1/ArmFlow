package com.example.testarmflow.chatFrag

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import com.example.testarmflow.R
import com.example.testarmflow.databinding.ActivityViewImageBinding
import com.squareup.picasso.Picasso

class ViewImageActivity : AppCompatActivity() {
    private lateinit var binding: ActivityViewImageBinding
    private lateinit var imageViewer: ImageView
    private lateinit var imageUrl: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityViewImageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        imageUrl = intent.getStringExtra("url").toString()
        imageViewer = binding.imageViewer

        Picasso.get().load(imageUrl).into(imageViewer)

        binding.imageViewBack.setOnClickListener {
            finish()
        }
    }
}