package com.wency.petmanager.friend

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.wency.petmanager.create.events.adapter.UserListAdapter
import com.wency.petmanager.data.UserInfo
import com.wency.petmanager.databinding.ItemFriendListChooseHolderBinding

class FriendChooseAdapter(val viewModel: ChooseFriendViewModel): ListAdapter<UserInfo, FriendChooseAdapter.UserChooseViewHolder >(UserListAdapter.DiffCallback) {

    class UserChooseViewHolder(val binding: ItemFriendListChooseHolderBinding): RecyclerView.ViewHolder(binding.root){
        val petListRecycler = binding.friendPetList
        fun bind(user: UserInfo, selected:Boolean){
            binding.user = user
            binding.selected = selected
            binding.executePendingBindings()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserChooseViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return UserChooseViewHolder(ItemFriendListChooseHolderBinding.inflate(layoutInflater, parent, false))
    }

    override fun onBindViewHolder(holder: UserChooseViewHolder, position: Int) {

        val user = getItem(position)
        user.petList?.let {petList->
            holder.petListRecycler.adapter = PetListAdapter(petList, viewModel)
        }
        holder.bind(user, viewModel.selectedIdList.contains(user.userId))
        holder.itemView.setOnClickListener {
            if (viewModel.selectedIdList.contains(user.userId)){
                viewModel.selectedIdList.remove(user.userId)
            } else {
                viewModel.selectedIdList.add(user.userId)
            }
            notifyDataSetChanged()
        }
    }


}