package com.wency.petmanager.memory.apater

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.wency.petmanager.data.Gallery
import com.wency.petmanager.databinding.ItemMemoryGalleryHolderBinding

class GalleryAdapter(private val memoryList: List<Gallery>) : RecyclerView.Adapter<GalleryAdapter.GalleryViewHolder>() {
    class GalleryViewHolder(val binding: ItemMemoryGalleryHolderBinding): RecyclerView.ViewHolder(binding.root){
        fun bind(gallery: Gallery){
            binding.gallery = gallery
            binding.executePendingBindings()
        }

    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GalleryViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return GalleryViewHolder(ItemMemoryGalleryHolderBinding.inflate(layoutInflater, parent, false))
    }

    override fun onBindViewHolder(holder: GalleryViewHolder, position: Int) {
        Log.d("Debug","Adapter: $position")
        val gallery = memoryList[position]
        holder.bind(gallery)
    }

    override fun getItemCount(): Int {
        return memoryList.size
    }
}