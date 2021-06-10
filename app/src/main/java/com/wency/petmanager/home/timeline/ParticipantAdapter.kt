package com.wency.petmanager.home.timeline

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.wency.petmanager.data.Pet
import com.wency.petmanager.databinding.SubItemTimelinePetParticipantBinding

class ParticipantAdapter(private val photo: List<String>): RecyclerView.Adapter<ParticipantAdapter.ItemPetViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemPetViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return ItemPetViewHolder(SubItemTimelinePetParticipantBinding.inflate(layoutInflater,parent,false))
    }

    override fun onBindViewHolder(holder: ItemPetViewHolder, position: Int) {
        Log.d("PetAdapter","onBindViewHolder $photo")
        holder.bind(photo[position])
    }

    class ItemPetViewHolder(val binding: SubItemTimelinePetParticipantBinding): RecyclerView.ViewHolder(binding.root){
        fun bind(photo: String){
            binding.petPhoto = photo
            binding.executePendingBindings()
        }
    }
    companion object DiffCallback : DiffUtil.ItemCallback<Pet>() {
        override fun areItemsTheSame(oldItem: Pet, newItem: Pet): Boolean {
            return oldItem === newItem
        }
        override fun areContentsTheSame(oldItem: Pet, newItem: Pet): Boolean {
            return oldItem == newItem
        }

    }

    override fun getItemCount(): Int {
        return photo.size
    }


}