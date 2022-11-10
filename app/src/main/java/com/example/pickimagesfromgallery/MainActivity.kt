package com.example.pickimagesfromgallery

import android.Manifest
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.example.pickimagesfromgallery.databinding.ActivityFullScreenBinding
import com.example.pickimagesfromgallery.databinding.ActivityMainBinding
import com.example.pickimagesfromgallery.databinding.FragmentBottomDialogBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


class MainActivity : AppCompatActivity(), ImageAdapter.Callback {
    private lateinit var binding: ActivityMainBinding
    private lateinit var bindingBottomSheetDialog: FragmentBottomDialogBinding
    private lateinit var dialog: BottomSheetDialog
    private var photoURI: Uri = Uri.EMPTY

    lateinit var currentPhotoPath: String

    private val REQUEST_CODE = 1

    private val viewModel: MainViewModel by viewModels()

    private val adapter = ImageAdapter(this)
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
            val imageUri = photoURI
            viewModel.images.add(imageUri)
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
                    viewModel.images.add(imageUri)
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

        val photoFile: File? = try {
            createImageFile()
        } catch (ex: IOException) {
            null
        }

        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        photoFile?.also {
            photoURI = FileProvider.getUriForFile(
                this,
                this.applicationContext.packageName + ".provider",
                it
            )
            intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
            getImageFromCamera.launch(intent)
        }
    }


    private fun createImageFile(): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${timeStamp}_",
            ".jpg",
            storageDir
        ).apply {
            currentPhotoPath = absolutePath
        }
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
            ) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
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

    override fun onImageClicked(imagePath: Uri) {
        val dialog = Dialog(this)
        val dialogBinding: ActivityFullScreenBinding =
            ActivityFullScreenBinding.inflate(layoutInflater)
        dialog.setContentView(dialogBinding.root)
        dialog.setCancelable(true)
        dialogBinding.ivFullScreen.setOnClickListener {
            dialog.dismiss()
        }
        dialogBinding.ivFullScreen.setImageURI(imagePath)
        dialog.show()
    }
}
