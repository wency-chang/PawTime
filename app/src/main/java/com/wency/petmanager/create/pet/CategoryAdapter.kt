package com.wency.petmanager.create.pet

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.wency.petmanager.ManagerApplication
import com.wency.petmanager.create.events.adapter.PhotoListAdapter
import com.wency.petmanager.databinding.ItemPetCategoryAddHolderBinding
import com.wency.petmanager.databinding.ItemPetCategoryCreateBinding

class CategoryAdapter(val onCancelClickListener: PhotoListAdapter.OnCancelClickListener) : ListAdapter<String, RecyclerView.ViewHolder> (PhotoListAdapter.DiffCallback){



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return when (viewType){
            PhotoListAdapter.TYPE_ADD_HOLDER -> AddPhotoViewHolder(ItemPetCategoryAddHolderBinding
                .inflate(layoutInflater, parent, false))
            PhotoListAdapter.TYPE_PHOTO_HOLDER -> PhotoViewHolder(ItemPetCategoryCreateBinding.inflate(
                layoutInflater, parent, false
            ))
            else -> throw ClassCastException("Unknown viewType $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when(holder){
            is AddPhotoViewHolder -> {
                holder.itemView.setOnClickListener {
                    onCancelClickListener.onClick(position)

                }

            }

            is PhotoViewHolder -> {
                holder.bind(Uri.parse(getItem(position)))
                holder.cancelButton.setOnClickListener {
                    onCancelClickListener.onClick(position)
                    notifyDataSetChanged()
                }
            }

        }
    }

    override fun getItemViewType(position: Int): Int {
        return when(getItem(position)){
            PetCreateViewModel.ADD_HOLDER_STRING -> PhotoListAdapter.TYPE_ADD_HOLDER
            else -> PhotoListAdapter.TYPE_PHOTO_HOLDER
        }
    }

    class AddPhotoViewHolder(val binding: ItemPetCategoryAddHolderBinding): RecyclerView.ViewHolder(binding.root){

    }
    class PhotoViewHolder(val binding: ItemPetCategoryCreateBinding): RecyclerView.ViewHolder(binding.root){
        val cancelButton = binding.petCategoryCancelButton
        fun bind(uri: Uri){
            Glide.with(ManagerApplication.instance)
                .load(uri)
                .centerCrop()
                .into(binding.photoHolder)
            binding.executePendingBindings()
        }

    }


}