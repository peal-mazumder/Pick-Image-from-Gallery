package com.example.pickimagesfromgallery

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
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
    private val IMAGE_PICK_CODE = 2
    private val CAMERA_REQUEST_CODE = 3

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
        startActivityForResult(intent, CAMERA_REQUEST_CODE)
    }

    private fun choosePhotosFromGallery() {
        val intent = Intent()
        intent.type = "image/*"
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        intent.action = Intent.ACTION_PICK
        startActivityForResult(intent, IMAGE_PICK_CODE)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == IMAGE_PICK_CODE) {
                if (data?.clipData != null) {
                    val count = data.clipData?.itemCount

                    for (i in 0 until count!!) {
                        val imageUri: Uri = data.clipData?.getItemAt(i)!!.uri
                        val bitmap =
                            MediaStore.Images.Media.getBitmap(this.contentResolver, imageUri)
                        viewModel.images.add(bitmap)
                    }


                } else if (data?.data != null) {
                    val imageUri: Uri = data.data!!
                    val bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, imageUri)
                    viewModel.images.add(bitmap)
                }
            } else if (requestCode == CAMERA_REQUEST_CODE) {
                val bitmap = data!!.extras!!.get("data") as Bitmap
                viewModel.images.add(bitmap)
            }
            adapter.submitList(viewModel.images)
            adapter.notifyDataSetChanged()
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
