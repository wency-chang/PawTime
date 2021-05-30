package com.wency.petmanager.friend

import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.wency.petmanager.create.events.adapter.UserListAdapter
import com.wency.petmanager.data.UserInfo
import com.wency.petmanager.databinding.ItemFriendListChooseHolderBinding
import com.wency.petmanager.detail.UserHeaderAdapter

class FriendChooseAdapter(val viewModel: ChooseFriendViewModel): ListAdapter<UserInfo, FriendChooseAdapter.UserChooseViewHolder >(UserListAdapter.DiffCallback) {

    class UserChooseViewHolder(val binding: ItemFriendListChooseHolderBinding): RecyclerView.ViewHolder(binding.root){
        val petListRecycler = binding.friendPetList
        fun bind(user: UserInfo){
            binding.user = user
            binding.executePendingBindings()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserChooseViewHolder {
        TODO("Not yet implemented")
    }

    override fun onBindViewHolder(holder: UserChooseViewHolder, position: Int) {
        TODO("Not yet implemented")
    }


}