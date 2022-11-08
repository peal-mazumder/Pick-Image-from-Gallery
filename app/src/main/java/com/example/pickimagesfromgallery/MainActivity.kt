package com.example.pickimagesfromgallery

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.pickimagesfromgallery.databinding.ActivityMainBinding
import com.github.drjacky.imagepicker.ImagePicker
import com.github.drjacky.imagepicker.constant.ImageProvider
import java.io.File


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val REQUEST_CODE = 1

    private val viewModel: MainViewModel by viewModels()

    private val adapter = ImageAdapter()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        askForPermission()
        binding.recyclerViewGalleryImage.adapter = adapter

        val launcher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                Log.d("Peal", "Error")
                if (it.resultCode == Activity.RESULT_OK) {
                    Log.d("Peal", "${it.data?.clipData?.itemCount}")
                    if (it.data?.clipData != null) {

                        val count = it.data?.clipData?.itemCount
                        Log.d("Peal", "$count")
                        for (i in 0 until count!!) {
                            val imageUri: Uri = it.data?.clipData?.getItemAt(i)!!.uri
                            viewModel.images.add(imageUri)
                        }


                    } else if (it.data?.data != null) {
                        val imageUri: Uri = it.data?.data!!
                        viewModel.images.add(imageUri)
                    }
                    adapter.submitList(viewModel.images)
                    adapter.notifyDataSetChanged()
                }
            }

        binding.btnAddPhotos.setOnClickListener {
            ImagePicker.with(this)
                .setMultipleAllowed(true)
                .provider(ImageProvider.BOTH)
                .createIntentFromDialog { launcher.launch(it) }
        }

    }

    private fun askForPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.CAMERA
                ),
                REQUEST_CODE
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        val errMsg = "Cannot start without required permissions"
        if (requestCode == REQUEST_CODE) {
            grantResults.forEach {
                if (it == PackageManager.PERMISSION_DENIED) {
                    Toast.makeText(this, errMsg, Toast.LENGTH_LONG).show()
                    finish()
                    return
                }
            }
            recreate()
        }
    }
}
