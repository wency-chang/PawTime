package com.wency.petmanager.detail

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.wency.petmanager.databinding.ItemDetailPhotoPagerBinding

class PhotoPagerAdapter(val photoList: List<String>): RecyclerView.Adapter<PhotoPagerAdapter.PhotoPagerViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoPagerViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return PhotoPagerViewHolder(ItemDetailPhotoPagerBinding.inflate(layoutInflater, parent, false))
    }

    override fun onBindViewHolder(holder: PhotoPagerViewHolder, position: Int) {
        holder.bind(photoList[position])
    }

    class PhotoPagerViewHolder(val binding: ItemDetailPhotoPagerBinding): RecyclerView.ViewHolder(binding.root){
        fun bind(photo: String){
            binding.photo = photo
            binding.executePendingBindings()
        }
    }

    override fun getItemCount(): Int {
        return photoList.size
    }
}