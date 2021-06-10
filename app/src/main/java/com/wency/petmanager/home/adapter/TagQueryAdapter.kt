package com.wency.petmanager.home.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.CompoundButton
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.wency.petmanager.create.events.adapter.TagListAdapter
import com.wency.petmanager.databinding.ItemHomeTagHolderBinding
import com.wency.petmanager.home.HomeViewModel

class TagQueryAdapter(val viewModel: HomeViewModel): ListAdapter<String, TagQueryAdapter.HomeTagViewHolder>(TagListAdapter.DiffCallback) {

    class HomeTagViewHolder(val binding: ItemHomeTagHolderBinding): RecyclerView.ViewHolder(binding.root){
        val tagChip = binding.homeTagQueryChip

        fun bind(tag: String, check: Boolean){
            binding.tag = tag
            binding.checked = check
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeTagViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return HomeTagViewHolder(ItemHomeTagHolderBinding.inflate(layoutInflater, parent, false))
    }

    override fun onBindViewHolder(holder: HomeTagViewHolder, position: Int) {
        val tag = getItem(position)
        if (viewModel.tagQueryList.value.isNullOrEmpty()){
            holder.bind(tag, false)
        } else {
            viewModel.tagQueryList.value?.contains(tag)?.let { holder.bind(tag, it) }
        }
        holder.tagChip.setOnCheckedChangeListener { _, checked: Boolean ->
            Log.d("Debug", "Chip click $checked, tag: $tag")
            viewModel.clickQuery(tag, checked)
        }
    }
}