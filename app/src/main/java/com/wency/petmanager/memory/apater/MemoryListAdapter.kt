package com.wency.petmanager.memory.apater

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.wency.petmanager.data.Pet
import com.wency.petmanager.databinding.ItemMemoryListHolderBinding
import com.wency.petmanager.home.adapter.PetHeaderAdapter
import com.wency.petmanager.memory.MemoryListViewModel
import com.wency.petmanager.profile.Today

class MemoryListAdapter(val viewModel: MemoryListViewModel): ListAdapter<Pet, MemoryListAdapter.MemoryPetListViewHolder> (PetHeaderAdapter.DiffCallback) {

    class MemoryPetListViewHolder(val binding: ItemMemoryListHolderBinding): RecyclerView.ViewHolder(binding.root){

        fun bind(pet: Pet, year: String){
            binding.pet = pet
            binding.memoryDates = year
            binding.executePendingBindings()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MemoryPetListViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return MemoryPetListViewHolder(ItemMemoryListHolderBinding.inflate(layoutInflater, parent, false))
    }

    override fun onBindViewHolder(holder: MemoryPetListViewHolder, position: Int) {
        val pet = getItem(position)
        val birthDayString = if (pet.birth == null){
            ""
        } else {
            Today.birthFormat.format(pet.birth!!.toDate())
        }
        val memoryDate = if (pet.memoryDate == null){
            ""
        } else {
            Today.dateFormat.format(pet.memoryDate!!.toDate())
        }
        holder.bind(pet, "$birthDayString - $memoryDate")
        holder.itemView.setOnClickListener {
            viewModel.selectPet(pet)
        }
    }
}