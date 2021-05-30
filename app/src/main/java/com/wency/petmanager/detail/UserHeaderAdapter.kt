package com.wency.petmanager.detail

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.wency.petmanager.data.UserInfo
import com.wency.petmanager.databinding.ItemDetailUserHeaderBinding

class UserHeaderAdapter(private val userList: List<UserInfo>): RecyclerView.Adapter<UserHeaderAdapter.UserHeaderViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserHeaderViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return UserHeaderViewHolder(
            ItemDetailUserHeaderBinding.inflate(
            layoutInflater, parent, false
        ))
    }

    override fun onBindViewHolder(holder: UserHeaderViewHolder, position: Int) {
        holder.bind(userList[position])
    }

    class UserHeaderViewHolder(val binding: ItemDetailUserHeaderBinding): RecyclerView.ViewHolder(binding.root){
        fun bind(user: UserInfo){
            binding.userInfo = user
            binding.user = true
            binding.executePendingBindings()
        }
    }

    override fun getItemCount(): Int {
        return userList.size
    }


}