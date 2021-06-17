package com.wency.petmanager.detail

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.wency.petmanager.create.events.adapter.TagListAdapter
import com.wency.petmanager.databinding.ItemDetailPhotoHolderBinding

class PhotoListAdapter(val editable: LiveData<Boolean>,
                       val lifecycleOwner: LifecycleOwner,
                       private val onClickListener: OnClickListener):
    ListAdapter<String, PhotoListAdapter.PhotoViewHolder>(TagListAdapter.DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return PhotoViewHolder(
            ItemDetailPhotoHolderBinding.inflate(layoutInflater, parent, false)
        )
    }

    override fun onBindViewHolder(holder: PhotoViewHolder, position: Int) {
                holder.bind(getItem(position), lifecycleOwner, this)
                holder.cancelButton.setOnClickListener {
                    onClickListener.onClick(false, position)
                    notifyDataSetChanged()
                }
    }

    class PhotoViewHolder(val binding: ItemDetailPhotoHolderBinding) : RecyclerView.ViewHolder(binding.root){
        val cancelButton = binding.photoCancelButton

        fun bind(photo: String, lifecycleOwner: LifecycleOwner, adapter: PhotoListAdapter){
            binding.lifecycleOwner = lifecycleOwner
            binding.adapter = adapter
            binding.photo = photo
            binding.executePendingBindings()
        }
    }

    class OnClickListener(val clickListener:(add: Boolean, position: Int)-> Unit){
        fun onClick(add: Boolean, position: Int) = clickListener(add, position)
    }
}