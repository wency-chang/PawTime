package com.wency.petmanager.detail

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.wency.petmanager.data.Pet
import com.wency.petmanager.databinding.ItemPetHeaderBinding

class PetHeaderAdapter(val petList: List<Pet>): RecyclerView.Adapter<PetHeaderAdapter.PetHeaderViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PetHeaderViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return PetHeaderViewHolder(ItemPetHeaderBinding.inflate(
            layoutInflater, parent, false
        ))
    }

    override fun onBindViewHolder(holder: PetHeaderViewHolder, position: Int) {
        holder.bind(petList[position])
    }

    class PetHeaderViewHolder(val binding: ItemPetHeaderBinding): RecyclerView.ViewHolder(binding.root){
        fun bind(pet: Pet){
            binding.pet = pet
            binding.petOptionName.visibility = View.GONE
            binding.executePendingBindings()
        }
    }

    override fun getItemCount(): Int {
        return petList.size
    }


}