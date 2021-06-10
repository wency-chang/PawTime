package com.wency.petmanager.home.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.wency.petmanager.create.events.adapter.TagListAdapter
import com.wency.petmanager.databinding.ItemHomeTagHolderBinding
import com.wency.petmanager.home.HomeViewModel

class TagQueryAdapter(val viewModel: HomeViewModel): ListAdapter<String, TagQueryAdapter.HomeTagViewHolder>(TagListAdapter.DiffCallback) {

    class HomeTagViewHolder(val binding: ItemHomeTagHolderBinding): RecyclerView.ViewHolder(binding.root){

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeTagViewHolder {
        TODO("Not yet implemented")
    }

    override fun onBindViewHolder(holder: HomeTagViewHolder, position: Int) {
        TODO("Not yet implemented")
    }
}