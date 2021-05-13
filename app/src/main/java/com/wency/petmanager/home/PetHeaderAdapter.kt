package com.wency.petmanager.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.wency.petmanager.data.Pet
import com.wency.petmanager.databinding.ItemPetHeaderBinding

class PetHeaderAdapter: ListAdapter<Pet, PetHeaderAdapter.PetOptionViewHolder>(DiffCallback) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PetHeaderAdapter.PetOptionViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return PetOptionViewHolder(ItemPetHeaderBinding.inflate(layoutInflater, parent, false))
    }

    override fun onBindViewHolder(holder: PetHeaderAdapter.PetOptionViewHolder, position: Int) {
            holder.bind(getItem(position))
    }

    class PetOptionViewHolder(val binding: ItemPetHeaderBinding): RecyclerView.ViewHolder(binding.root){

        fun bind(pet: Pet?){
            binding.pet = pet
            binding.executePendingBindings()
        }

    }



    companion object DiffCallback : DiffUtil.ItemCallback<Pet>() {
        override fun areItemsTheSame(oldItem: Pet, newItem: Pet): Boolean {
            return oldItem === newItem
        }
        override fun areContentsTheSame(oldItem: Pet, newItem: Pet): Boolean {
            return oldItem.id == newItem.id
        }
    }
}