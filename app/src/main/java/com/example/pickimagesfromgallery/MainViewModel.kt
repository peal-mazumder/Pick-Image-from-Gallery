package com.example.pickimagesfromgallery

import android.net.Uri
import androidx.lifecycle.ViewModel

class MainViewModel : ViewModel() {
    val images: MutableList<Uri> = mutableListOf()
}