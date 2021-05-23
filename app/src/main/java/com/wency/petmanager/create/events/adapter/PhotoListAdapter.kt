package com.wency.petmanager.create.events.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.wency.petmanager.create.events.MissionCreateViewModel
import com.wency.petmanager.databinding.ItemAddContentHolderBinding
import com.wency.petmanager.databinding.ItemMemoHolderBinding
import com.wency.petmanager.databinding.ItemPhotoHolderBinding

class PhotoListAdapter(private val onClickListener: MemoListAdapter.OnClickListener
            , val onCancelClickListener: OnCancelClickListener): ListAdapter<String, RecyclerView.ViewHolder>(
    DiffCallback
) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return when(viewType){
            TYPE_PHOTO_HOLDER -> PhotoHolder(ItemPhotoHolderBinding.inflate(layoutInflater, parent, false))
            TYPE_ADD_HOLDER -> NeedAddHolder(ItemAddContentHolderBinding.inflate(layoutInflater, parent, false))
            else -> throw ClassCastException("Unknown viewType $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder){
            is PhotoHolder -> {
                holder.bind(getItem(position))
                holder.cancelButton.setOnClickListener {
                    onCancelClickListener.onClick(position)
                    notifyDataSetChanged()
                }

            }
            is NeedAddHolder -> {
                holder.itemView.setOnClickListener {
                    onClickListener.onClick()
                }
                holder.bind()
            }
        }
    }


    class PhotoHolder(val binding: ItemPhotoHolderBinding): RecyclerView.ViewHolder(binding.root){
        val cancelButton = binding.photoSelectCancelButton
        fun bind(photoURI: String){
            Log.d("Adapter","photo URI $photoURI")
            binding.photoSelectorImage.setImageURI(photoURI.toUri())
            binding.executePendingBindings()
        }

    }

    class NeedAddHolder(val binding: ItemAddContentHolderBinding): RecyclerView.ViewHolder(binding.root){
        fun bind(){
            binding.executePendingBindings()
        }

    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)){
            MissionCreateViewModel.NEED_ADD_HOLDER -> TYPE_ADD_HOLDER
            else -> TYPE_PHOTO_HOLDER
        }

    }
    class OnCancelClickListener(val cancelClickListener: (position: Int)-> Unit){
        fun onClick(position: Int) = cancelClickListener(position)
    }


    companion object DiffCallback : DiffUtil.ItemCallback<String>() {
        override fun areItemsTheSame(oldItem: String, newItem: String): Boolean {
            return oldItem === newItem
        }
        override fun areContentsTheSame(oldItem: String, newItem: String): Boolean {
            return oldItem == newItem
        }
        const val TYPE_ADD_HOLDER = 0x00
        const val TYPE_PHOTO_HOLDER = 0x01
    }

}