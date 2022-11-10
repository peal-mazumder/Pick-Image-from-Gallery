package com.example.pickimagesfromgallery

import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.pickimagesfromgallery.databinding.ActivityFullScreenBinding


class FullScreenActivity : AppCompatActivity(), ImageAdapter.Callback {
    private lateinit var binding: ActivityFullScreenBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFullScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.hide()
        supportActionBar?.title = "Full Screen Image"

        ImageAdapter(this)
    }

    override fun onImageClicked(imagePath: Uri) {
        Toast.makeText(this, "Hello", Toast.LENGTH_LONG).show()
        Glide.with(this)
            .load(imagePath)
            .into(binding.ivFullScreen)
    }
}