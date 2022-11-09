package com.example.pickimagesfromgallery

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.pickimagesfromgallery.databinding.ActivityMainBinding
import com.example.pickimagesfromgallery.databinding.FragmentBottomDialogBinding
import com.google.android.material.bottomsheet.BottomSheetDialog


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var bindingBottomSheetDialog: FragmentBottomDialogBinding
    private lateinit var dialog: BottomSheetDialog

    private val REQUEST_CODE = 1

    private val viewModel: MainViewModel by viewModels()

    private val adapter = ImageAdapter()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        askForPermission()
        binding.recyclerViewGalleryImage.adapter = adapter

        binding.btnAddPhotos.setOnClickListener {
            showBottomSheetDialog()
        }
    }

    private val getImageFromCamera =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            val bitmap = it.data?.extras!!.get("data") as Bitmap
            viewModel.images.add(bitmap)
            adapter.submitList(viewModel.images)
            adapter.notifyItemInserted(viewModel.images.size - 1)
        }

    private val getImageFromGallery =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.data?.clipData != null) {
                val count = it.data?.clipData?.itemCount ?: 0
                val sizeBeforeItemsInserted = viewModel.images.size

                for (i in 0 until count) {
                    val imageUri = it.data?.clipData?.getItemAt(i)?.uri ?: Uri.EMPTY
                    val bitmap = when {
                        Build.VERSION.SDK_INT >= Build.VERSION_CODES.P -> {
                            val source = ImageDecoder.createSource(this.contentResolver, imageUri)
                            ImageDecoder.decodeBitmap(source)
                        }
                        else -> {
                            MediaStore.Images.Media.getBitmap(this.contentResolver, imageUri)
                        }
                    }
                    viewModel.images.add(bitmap)
                }
                adapter.submitList(viewModel.images)
                adapter.notifyItemRangeChanged(sizeBeforeItemsInserted - 1, count)
            }
        }


    private fun showBottomSheetDialog() {
        dialog = BottomSheetDialog(this)
        bindingBottomSheetDialog = FragmentBottomDialogBinding.inflate(layoutInflater)

        setClickListener()

        dialog.setCancelable(true)
        dialog.setContentView(bindingBottomSheetDialog.root)
        dialog.show()
    }

    private fun setClickListener() {
        bindingBottomSheetDialog.tvPhoto.setOnClickListener {
            takePhotoFromCamera()
            dialog.dismiss()
        }

        bindingBottomSheetDialog.tvGallery.setOnClickListener {
            choosePhotosFromGallery()
            dialog.dismiss()
        }
    }

    private fun takePhotoFromCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        getImageFromCamera.launch(intent)
    }

    private fun choosePhotosFromGallery() {
        val intent = Intent()
        intent.type = "image/*"
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        intent.action = Intent.ACTION_PICK
        getImageFromGallery.launch(intent)
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
