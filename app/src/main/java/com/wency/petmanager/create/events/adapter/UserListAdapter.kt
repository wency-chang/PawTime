package com.wency.petmanager.create.events.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.wency.petmanager.data.Pet
import com.wency.petmanager.data.UserInfo
import com.wency.petmanager.databinding.ItemPetSelectorBinding
import com.wency.petmanager.databinding.ItemUserSelectorBinding

class UserListAdapter (private val onClickListener: OnClickListener):
    ListAdapter<UserInfo, UserListAdapter.UserSelectorViewHolder>(DiffCallback)  {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserSelectorViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return UserSelectorViewHolder(ItemUserSelectorBinding.inflate(layoutInflater, parent, false))
    }

    override fun onBindViewHolder(holder: UserSelectorViewHolder, position: Int) {

        val user: UserInfo = getItem(position)

        holder.itemView.setOnClickListener {
            holder.selectedStatus = holder.selectedStatus != true
            notifyDataSetChanged()
            onClickListener.onClick(user.userId, holder.selectedStatus)
        }
        holder.bind(user)

    }

    class UserSelectorViewHolder(val binding: ItemUserSelectorBinding): RecyclerView.ViewHolder(binding.root){
        var selectedStatus = false
        fun bind(user: UserInfo){
            binding.user = user
            binding.isSelected = selectedStatus
            binding.executePendingBindings()
        }
    }
    class OnClickListener(val clickListener: (petID: String, checkedStatus: Boolean)-> Unit){
        fun onClick(petID: String, checkedStatus: Boolean) = clickListener(petID, checkedStatus)
    }

    companion object DiffCallback : DiffUtil.ItemCallback<UserInfo>() {
        override fun areItemsTheSame(oldItem: UserInfo, newItem: UserInfo): Boolean {
            return oldItem === newItem
        }
        override fun areContentsTheSame(oldItem: UserInfo, newItem: UserInfo): Boolean {
            return oldItem.userId == newItem.userId
        }
    }
}