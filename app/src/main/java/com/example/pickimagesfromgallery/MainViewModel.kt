package com.example.pickimagesfromgallery

import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.ViewModel

class MainViewModel : ViewModel() {
    val images: MutableList<Bitmap> = mutableListOf()
}