package com.wency.petmanager.diary

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.wency.petmanager.data.Pet
import com.wency.petmanager.databinding.ItemPetSelectorBinding
import com.wency.petmanager.schedule.ScheduleCreateViewModel

class PetSelectorAdapter2(val viewModel: ScheduleCreateViewModel):ListAdapter<Pet, PetSelectorAdapter2.PetSelectorViewHolder >(DiffCallback)  {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PetSelectorViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return PetSelectorViewHolder(ItemPetSelectorBinding.inflate(layoutInflater, parent, false))
    }

    override fun onBindViewHolder(holder: PetSelectorViewHolder, position: Int) {
        holder.itemView.setOnClickListener {
            holder.selectedStatus = holder.selectedStatus != true
            notifyDataSetChanged()
        }
        holder.bind(getItem(position))
    }

    class PetSelectorViewHolder(val binding: ItemPetSelectorBinding): RecyclerView.ViewHolder(binding.root){
        var selectedStatus = false
        fun bind(pet: Pet){
            binding.isSelected = selectedStatus
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