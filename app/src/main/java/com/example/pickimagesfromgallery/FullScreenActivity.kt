package com.example.pickimagesfromgallery

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.pickimagesfromgallery.databinding.ActivityFullScreenBinding

class FullScreenActivity : AppCompatActivity() {
    private lateinit var binding: ActivityFullScreenBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFullScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.hide()
        supportActionBar?.title = "Full Screen Image"

        val imagePath = intent.getStringExtra("path")

        Glide.with(this)
            .load(imagePath)
            .into(binding.ivFullScreen)

    }
}