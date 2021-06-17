package com.wency.petmanager.profile

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import com.wency.petmanager.create.events.adapter.TagListAdapter
import com.wency.petmanager.databinding.ItemDetailPhotoPagerBinding
import com.wency.petmanager.detail.PhotoPagerAdapter

class CoverPhotoAdapter: ListAdapter<String, PhotoPagerAdapter.PhotoPagerViewHolder>(TagListAdapter.DiffCallback) {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): PhotoPagerAdapter.PhotoPagerViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return PhotoPagerAdapter.PhotoPagerViewHolder(ItemDetailPhotoPagerBinding.inflate(
            layoutInflater,
                parent,
                false))

    }

    override fun onBindViewHolder(holder: PhotoPagerAdapter.PhotoPagerViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}