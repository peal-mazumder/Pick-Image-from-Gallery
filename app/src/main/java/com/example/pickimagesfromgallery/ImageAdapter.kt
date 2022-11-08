package com.example.pickimagesfromgallery

import android.graphics.Bitmap
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.pickimagesfromgallery.databinding.ImageViewBinding

class ImageAdapter : ListAdapter<Bitmap, ImageAdapter.ImageViewHolder>(DiffCallback) {
    class ImageViewHolder(var binding: ImageViewBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(data: Bitmap) {
            binding.ivImage.setImageBitmap(data)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val view = ImageViewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ImageViewHolder(view)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        val data = getItem(position)
        return holder.bind((data))
    }

    companion object DiffCallback : DiffUtil.ItemCallback<Bitmap>() {
        override fun areItemsTheSame(oldItem: Bitmap, newItem: Bitmap): Boolean {
            return (oldItem.sameAs(newItem))
        }

        override fun areContentsTheSame(oldItem: Bitmap, newItem: Bitmap): Boolean {
            return (oldItem.sameAs(newItem))
        }
    }
}