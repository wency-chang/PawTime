package com.wency.petmanager.friend

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.wency.petmanager.data.Pet
import com.wency.petmanager.databinding.SubItemFriendPetListBinding
import com.wency.petmanager.home.adapter.PetHeaderAdapter

class FriendPetListAdapter: ListAdapter<Pet, FriendPetListAdapter.PetListViewHolder >(
    PetHeaderAdapter.DiffCallback) {


    class PetListViewHolder(val binding: SubItemFriendPetListBinding): RecyclerView.ViewHolder(binding.root){
        fun bind(pet: Pet){
            binding.pet = pet
            binding.executePendingBindings()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PetListViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return PetListViewHolder(SubItemFriendPetListBinding.inflate(layoutInflater, parent, false))
    }

    override fun onBindViewHolder(holder: PetListViewHolder, position: Int) {
        holder.bind(getItem(position))
    }


}