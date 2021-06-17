package com.wency.petmanager.friend

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.wency.petmanager.data.Pet
import com.wency.petmanager.databinding.SubItemFriendPetListBinding

class PetListAdapter(private val petIdList: List<String>, val viewModel: ChooseFriendViewModel):
    RecyclerView.Adapter<PetListAdapter.PetListViewHolder>() {


    class PetListViewHolder(val binding: SubItemFriendPetListBinding):
        RecyclerView.ViewHolder(binding.root){

        fun bind(pet: Pet){
            binding.pet = pet
            binding.executePendingBindings()
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PetListViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return PetListViewHolder(
            SubItemFriendPetListBinding.inflate(layoutInflater, parent, false))
    }

    override fun onBindViewHolder(holder: PetListViewHolder, position: Int) {
        val petId = petIdList[position]
        val pet = viewModel.petInfoList.value?.filter {
            it.id == petId
        }
        pet?.get(0)?.let {
            holder.bind(it)
        }
    }

    override fun getItemCount(): Int {
        return petIdList.size
    }

}