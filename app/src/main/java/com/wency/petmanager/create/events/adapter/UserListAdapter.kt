package com.wency.petmanager.create.events.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.wency.petmanager.create.events.ScheduleCreateViewModel
import com.wency.petmanager.data.Pet
import com.wency.petmanager.data.UserInfo
import com.wency.petmanager.databinding.ItemPetSelectorBinding
import com.wency.petmanager.databinding.ItemUserChooseButtonBinding
import com.wency.petmanager.databinding.ItemUserSelectorBinding

class UserListAdapter (private val onClickListener: OnClickListener, val viewModel: ScheduleCreateViewModel):
    ListAdapter<UserInfo, RecyclerView.ViewHolder>(DiffCallback)  {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return when(viewType){
            TYPE_USER -> UserSelectorViewHolder(ItemUserSelectorBinding.inflate(layoutInflater, parent, false))
            TYPE_ADDER -> UserAddViewHolder(ItemUserChooseButtonBinding.inflate(layoutInflater, parent, false))
            else -> throw ClassCastException("Unknown viewType $viewType")
        }

    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val user: UserInfo = getItem(position)

        when (holder){

            is UserSelectorViewHolder -> {
                holder.itemView.setOnClickListener {
                    onClickListener.onClick(user.userId, !viewModel.participantUser.contains(user.userId))
                    notifyDataSetChanged()
                }
                holder.bind(user, viewModel.participantUser.contains(user.userId))

            }

            is UserAddViewHolder -> {
                holder.itemView.setOnClickListener {
                    onClickListener.onClick("TYPE_ADDER", false)
                }
            }
        }



    }

    class UserSelectorViewHolder(val binding: ItemUserSelectorBinding): RecyclerView.ViewHolder(binding.root){

        fun bind(user: UserInfo, selectedStatus: Boolean){
            binding.user = user
            binding.isSelected = selectedStatus
            binding.executePendingBindings()
        }
    }
    class UserAddViewHolder(val binding: ItemUserChooseButtonBinding): RecyclerView.ViewHolder(binding.root){

    }

    override fun getItemViewType(position: Int): Int {

        return when (getItem(position)){
            UserInfo() -> TYPE_ADDER
            else -> TYPE_USER
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

        const val TYPE_USER = 0x00
        const val TYPE_ADDER = 0x01
    }
}