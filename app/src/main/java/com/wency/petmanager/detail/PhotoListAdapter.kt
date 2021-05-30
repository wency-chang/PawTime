package com.wency.petmanager.detail

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.ViewModel
import androidx.recyclerview.widget.RecyclerView
import com.wency.petmanager.databinding.ItemDetailPhotoHolderBinding

class PhotoListAdapter(val photoList: List<String>): RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return when(viewType) {
            TYPE_PHOTO -> PhotoViewHolder(ItemDetailPhotoHolderBinding.inflate(layoutInflater, parent, false))
            else -> throw ClassCastException("Unknown viewType $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder){
            is PhotoViewHolder -> {
                holder.bind(photoList[position])
            }

        }
    }

    override fun getItemCount(): Int {
        return photoList.size
    }

    class PhotoViewHolder(val binding: ItemDetailPhotoHolderBinding) : RecyclerView.ViewHolder(binding.root){
        fun bind(photo: String){
            binding.photo = photo
            binding.executePendingBindings()
        }

    }

    override fun getItemViewType(position: Int): Int {
        return when (photoList[position]){
            else -> {
                TYPE_PHOTO
            }
        }
    }

    companion object{
        const val TYPE_PHOTO = 0x00
    }
}