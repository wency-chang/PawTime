package com.wency.petmanager.detail

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.wency.petmanager.create.events.adapter.UserListAdapter
import com.wency.petmanager.data.UserInfo
import com.wency.petmanager.databinding.ItemDetailUserHeaderBinding

class UserHeaderAdapter(
    private var selectedIdList: MutableList<String>,
    private val viewModel: ScheduleDetailViewModel,
    private val onClickListener: OnClickListener
) : ListAdapter<UserInfo, UserHeaderAdapter.UserHeaderViewHolder>(UserListAdapter.DiffCallback) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserHeaderViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return UserHeaderViewHolder(
            ItemDetailUserHeaderBinding.inflate(
                layoutInflater, parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: UserHeaderViewHolder, position: Int) {
        val userInfo = getItem(position)

        holder.bind(userInfo, selectedIdList.contains(userInfo.userId))
        holder.itemView.setOnClickListener {

            if (viewModel.editable.value== true){

                if (selectedIdList.contains(userInfo.userId)) {
                    if (selectedIdList.size> 1){
                        onClickListener.onClick(false, userInfo)
                        selectedIdList.remove(userInfo.userId)
                    }

                } else {
                    onClickListener.onClick(true, userInfo)
                    selectedIdList.add(userInfo.userId)
                }
                notifyDataSetChanged()
            }
        }
    }

    class UserHeaderViewHolder(val binding: ItemDetailUserHeaderBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(user: UserInfo, selected: Boolean) {
            binding.userInfo = user
            binding.selected = selected
            binding.user = true
            binding.executePendingBindings()
        }
    }


    class OnClickListener(val clickListener: (add: Boolean, user: UserInfo) -> Unit) {
        fun onClick(add: Boolean, user: UserInfo) = clickListener(add, user)
    }

}