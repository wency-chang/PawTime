package com.wency.petmanager.detail

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.LiveData
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.wency.petmanager.data.Pet
import com.wency.petmanager.databinding.ItemParticipantHeaderBinding

class PetHeaderAdapter(private val selectedIdList: MutableList<String>,
                       private val editable: LiveData<Boolean>,
                       private val onClickListener: OnClickListener): ListAdapter<Pet, PetHeaderAdapter.PetHeaderViewHolder>(
    com.wency.petmanager.home.adapter.PetHeaderAdapter.DiffCallback) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PetHeaderViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return PetHeaderViewHolder(ItemParticipantHeaderBinding.inflate(
            layoutInflater, parent, false
        ))
    }

    override fun onBindViewHolder(holder: PetHeaderViewHolder, position: Int) {

        val pet = getItem(position)
        holder.bind(pet, selectedIdList.contains(pet.id))
        holder.itemView.setOnClickListener {
            
            if (editable.value == true){
                if (selectedIdList.contains(pet.id)) {
                    if (selectedIdList.size > 1) {
                        onClickListener.onClick(false, pet)
                        selectedIdList.remove(pet.id)
                    }

                } else {
                    onClickListener.onClick(true, pet)
                    selectedIdList.add(pet.id)
                }

                notifyDataSetChanged()
            }
        }

    }

    class PetHeaderViewHolder(val binding: ItemParticipantHeaderBinding): RecyclerView.ViewHolder(binding.root){
        fun bind(pet: Pet, selected: Boolean){
            binding.pet = pet
            binding.selected = selected
            binding.executePendingBindings()
        }
    }

    class OnClickListener(val clickListener: (add: Boolean, pet: Pet) -> Unit) {
        fun onClick(add: Boolean, pet: Pet) = clickListener(add, pet)
    }


}