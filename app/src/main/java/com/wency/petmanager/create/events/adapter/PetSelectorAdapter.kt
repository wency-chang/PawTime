package com.wency.petmanager.create.events.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.wency.petmanager.R
import com.wency.petmanager.data.Pet
import com.wency.petmanager.data.PetSelector
import com.wency.petmanager.databinding.ItemPetSelectorBinding

class PetSelectorAdapter(private val onClickListener: OnClickListener):ListAdapter<PetSelector, PetSelectorAdapter.PetSelectorViewHolder>(DiffCallback)  {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PetSelectorViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return PetSelectorViewHolder(ItemPetSelectorBinding.inflate(layoutInflater, parent, false))
    }


    override fun onBindViewHolder(holder: PetSelectorViewHolder, position: Int) {
        val petSelector = getItem(position)


        holder.itemView.setOnClickListener {
            petSelector.selectedStatus = petSelector.selectedStatus != true
            onClickListener.onClick(petSelector.pet.id, petSelector.selectedStatus)
            notifyDataSetChanged()
        }
        holder.bind(petSelector)

    }

    class PetSelectorViewHolder(val binding: ItemPetSelectorBinding): RecyclerView.ViewHolder(binding.root){
        val selectedColor = R.color.blue_sunday
        val unSelectedColor = R.color.white
        fun bind(petSelector: PetSelector){
//            if (petSelector.selectedStatus){
//                binding.petSelectCardView.setCardBackgroundColor(selectedColor)
//            } else {
//                binding.petSelectCardView.setCardBackgroundColor(unSelectedColor)
//            }
            binding.isSelected = petSelector.selectedStatus
            binding.pet = petSelector.pet
            binding.executePendingBindings()
        }


    }
    class OnClickListener(val clickListener: (petID: String, checkedStatus: Boolean)-> Unit){
        fun onClick(petID: String, checkedStatus: Boolean) = clickListener(petID, checkedStatus)
    }

    companion object DiffCallback : DiffUtil.ItemCallback<PetSelector>() {
        override fun areItemsTheSame(oldItem: PetSelector, newItem: PetSelector): Boolean {
            return oldItem === newItem
        }
        override fun areContentsTheSame(oldItem: PetSelector, newItem: PetSelector): Boolean {
            return oldItem.pet == newItem.pet
        }
    }
}