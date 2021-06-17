package com.wency.petmanager.friend

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.wency.petmanager.create.events.adapter.UserListAdapter
import com.wency.petmanager.data.UserInfo
import com.wency.petmanager.databinding.ItemFriendListHolderBinding

class FriendGridListAdapter: ListAdapter<UserInfo,
        FriendGridListAdapter.GridFriendListViewHolder >(UserListAdapter.DiffCallback) {

    class GridFriendListViewHolder(val binding: ItemFriendListHolderBinding):
        RecyclerView.ViewHolder(binding.root){
        fun bind(user: UserInfo){
            binding.user = user
            binding.executePendingBindings()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GridFriendListViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return GridFriendListViewHolder(
            ItemFriendListHolderBinding.inflate(layoutInflater, parent, false))
    }

    override fun onBindViewHolder(holder: GridFriendListViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}