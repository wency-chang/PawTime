package com.wency.petmanager.create.events.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.wency.petmanager.create.events.MissionCreateViewModel
import com.wency.petmanager.databinding.ItemAddContentHolderBinding
import com.wency.petmanager.databinding.ItemMemoHolderBinding

class MemoListAdapter(private val onClickListener: OnClickListener): ListAdapter<String, RecyclerView.ViewHolder >(
    DiffCallback
) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return when(viewType){
            TYPE_MEMO_HOLDER -> MemoHolder(ItemMemoHolderBinding.inflate(layoutInflater, parent, false))
            TYPE_ADD_HOLDER -> NeedAddHolder(ItemAddContentHolderBinding.inflate(layoutInflater, parent, false))
            else -> throw ClassCastException("Unknown viewType $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder){
            is MemoHolder -> {
                holder.bind(getItem(position))
            }
            is NeedAddHolder -> {
                holder.itemView.setOnClickListener {
                    onClickListener.onClick()
                }
                holder.bind()
            }
        }
    }


    class MemoHolder(val binding: ItemMemoHolderBinding): RecyclerView.ViewHolder(binding.root){
        fun bind(memo: String){
            binding.memo = memo
            binding.executePendingBindings()
        }

    }

    class NeedAddHolder(val binding: ItemAddContentHolderBinding): RecyclerView.ViewHolder(binding.root){
        fun bind(){
            binding.executePendingBindings()
        }

    }
    class OnClickListener(val clickListener: ()-> Unit){
        fun onClick() = clickListener()
    }


    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)){
            MissionCreateViewModel.NEED_ADD_HOLDER -> TYPE_ADD_HOLDER
            else -> TYPE_MEMO_HOLDER
        }

    }


    companion object DiffCallback : DiffUtil.ItemCallback<String>() {
        override fun areItemsTheSame(oldItem: String, newItem: String): Boolean {
            return oldItem === newItem
        }
        override fun areContentsTheSame(oldItem: String, newItem: String): Boolean {
            return oldItem == newItem
        }
        const val TYPE_ADD_HOLDER = 0x00
        const val TYPE_MEMO_HOLDER = 0x01
    }


}