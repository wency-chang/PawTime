package com.wency.petmanager.home.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.wency.petmanager.data.Pet
import com.wency.petmanager.databinding.ItemPetHeaderBinding
import com.wency.petmanager.home.HomeFragment
import com.wency.petmanager.home.HomeViewModel

class PetHeaderAdapter(val viewModel: HomeViewModel, private val homeFragment: HomeFragment): ListAdapter<Pet, PetHeaderAdapter.PetOptionViewHolder>(
    DiffCallback
) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PetOptionViewHolder {


        val layoutInflater = LayoutInflater.from(parent.context)

        return PetOptionViewHolder(ItemPetHeaderBinding.inflate(layoutInflater, parent, false))
    }


    override fun onBindViewHolder(holder: PetOptionViewHolder, position: Int) {
            homeFragment.registerForContextMenu(holder.petHeader)
            holder.addPetButton.setOnClickListener {
                viewModel.clickForCreate(HomeViewModel.PAGE_PET_CREATE)
            }
            holder.bind(getItem(position))
            holder.petHeader.setOnLongClickListener{
                it.showContextMenu()
                viewModel.navigateToPetProfile(getItem(position))
                return@setOnLongClickListener true
            }
            holder.petHeader.setOnClickListener {
                viewModel.queryByPet(position, viewModel.petQueryPosition.value != position)
                viewModel.closeTagQuery()
//                viewModel.clearTagQuery(true)
                notifyDataSetChanged()
            }
            if (position == viewModel.petQueryPosition.value){
                holder.bindBorder(true)
            } else {
                holder.bindBorder(false)
            }
    }

    class PetOptionViewHolder(val binding: ItemPetHeaderBinding): RecyclerView.ViewHolder(binding.root){
        val addPetButton = binding.petAddImage
        val petHeader = binding.petOptionImage


//        init {
//            val fragment = HomeFragment()
//            fragment.registerForContextMenu(binding.petOptionImage)
//            petHeader.setOnLongClickListener {
//                it.showContextMenu()
//                return@setOnLongClickListener true
//            }
//        }




        fun bind(pet: Pet?){

            binding.pet = pet
            binding.executePendingBindings()
        }
        fun bindBorder(select: Boolean){
            binding.select = select
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