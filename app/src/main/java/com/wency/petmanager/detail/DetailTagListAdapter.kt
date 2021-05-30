package com.wency.petmanager.detail

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.wency.petmanager.create.events.DiaryCreateViewModel
import com.wency.petmanager.create.events.adapter.TagListAdapter
import com.wency.petmanager.databinding.ItemTagViewBinding

class DetailTagListAdapter(private val editable: Boolean):
    ListAdapter<String, RecyclerView.ViewHolder>(TagListAdapter.DiffCallback) {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return when (viewType){
            TagListAdapter.ITEM_TYPE_TAG -> {
                TagHolder(ItemTagViewBinding.inflate(layoutInflater, parent, false))
            }
            else -> throw ClassCastException("Unknown viewType $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when(holder){
            is TagHolder -> {
                holder.bind(getItem(position))
                holder.bindClickable(editable)

            }
        }
    }

    class TagHolder(val binding: ItemTagViewBinding): RecyclerView.ViewHolder(binding.root){

        fun bind(tag: String){
            binding.tag = tag
            binding.executePendingBindings()
        }
        fun bindClickable(clickable: Boolean){
            binding.chipView.isClickable = clickable
            binding.executePendingBindings()
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)){

            else ->{
                TagListAdapter.ITEM_TYPE_TAG
            }

        }

    }
}