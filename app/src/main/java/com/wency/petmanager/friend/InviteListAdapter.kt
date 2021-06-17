package com.wency.petmanager.friend

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.wency.petmanager.create.events.adapter.UserListAdapter
import com.wency.petmanager.data.UserInfo
import com.wency.petmanager.databinding.ItemInviteListBinding

class InviteListAdapter(val viewModel: FriendListViewModel): ListAdapter<UserInfo,
        InviteListAdapter.InviteRequestViewHolder>(UserListAdapter.DiffCallback) {

    class InviteRequestViewHolder(val binding: ItemInviteListBinding):
        RecyclerView.ViewHolder(binding.root){
        val acceptButton = binding.inviteConfirmButton
        val rejectButton = binding.inviteRejectButton
        val clickToDetail = binding.clickToDialog

        fun bind(user: UserInfo){
            binding.user = user
            binding.executePendingBindings()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InviteRequestViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return InviteRequestViewHolder(
            ItemInviteListBinding.inflate(layoutInflater, parent, false))
    }

    override fun onBindViewHolder(holder: InviteRequestViewHolder, position: Int) {
        val user = getItem(position)
        holder.acceptButton.setOnClickListener {
            viewModel.acceptFriend(user.userId)
        }
        holder.rejectButton.setOnClickListener {
            viewModel.rejectInvite(user.userId)
        }
        holder.clickToDetail.setOnClickListener {
            viewModel._userDetailDialogData.value = user
        }
        holder.bind(user)
    }
}