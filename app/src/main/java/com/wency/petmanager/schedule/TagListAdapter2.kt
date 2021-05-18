package com.wency.petmanager.diary

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.flexbox.FlexboxLayoutManager
import com.wency.petmanager.databinding.ItemTagAddBinding
import com.wency.petmanager.databinding.ItemTagViewBinding
import com.wency.petmanager.schedule.ScheduleCreateViewModel


class TagListAdapter2(val viewModel: ScheduleCreateViewModel): ListAdapter<String, RecyclerView.ViewHolder>(
    DiffCallback
) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return when(viewType){
            ITEM_TYPE_TAG -> TagViewHolder(
                ItemTagViewBinding.inflate(
                    layoutInflater,
                    parent,
                    false
                )
            )
            ITEM_TYPE_ADD -> AddTagViewHolder(
                ItemTagAddBinding.inflate(
                    layoutInflater,
                    parent,
                    false
                )
            )
            else -> throw ClassCastException("Unknown viewType $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        val layoutParams = holder.itemView.layoutParams

        if (layoutParams is FlexboxLayoutManager.LayoutParams) {
            val flexboxLp: FlexboxLayoutManager.LayoutParams = FlexboxLayoutManager.LayoutParams(holder.itemView.layoutParams)
            flexboxLp.flexGrow = 1.0f
        }

        when (holder){
            is TagViewHolder -> holder.bind(getItem(position))
            is AddTagViewHolder -> holder.bind(getItem(position))
        }
    }

    class TagViewHolder(val binding: ItemTagViewBinding):RecyclerView.ViewHolder(binding.root){

        val chipView = binding.chipView
        fun bind(tag: String?){
            tag?.let {
                binding.tag = tag
                val layoutPrams: ViewGroup.LayoutParams = chipView.layoutParams
                binding.executePendingBindings()
            }
        }
    }
    class AddTagViewHolder(val binding: ItemTagAddBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(addString: String) {
            binding.executePendingBindings()
        }
    }


    override fun getItemViewType(position: Int): Int {
        return when(getItem(position)){
            DiaryCreateViewModel.ADD_TAG_STRING -> ITEM_TYPE_ADD
            else -> ITEM_TYPE_TAG
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<String>() {
        override fun areItemsTheSame(oldItem: String, newItem: String): Boolean {
            return oldItem === newItem
        }
        override fun areContentsTheSame(oldItem: String, newItem: String): Boolean {
            return oldItem == newItem
        }
        private const val ITEM_TYPE_ADD = 0x00
        private const val ITEM_TYPE_TAG = 0x01
    }

}
