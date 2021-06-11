package com.wency.petmanager.dialog.mission

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.wency.petmanager.data.MissionGroup
import com.wency.petmanager.databinding.ItemMissionListHolderBinding

class MissionAdapter: ListAdapter<MissionGroup, MissionAdapter.MissionViewHolder >(DiffCallback) {
    class MissionViewHolder(val binding: ItemMissionListHolderBinding): RecyclerView.ViewHolder(binding.root){

    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MissionViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return MissionViewHolder(ItemMissionListHolderBinding.inflate(layoutInflater, parent, false))
    }

    override fun onBindViewHolder(holder: MissionViewHolder, position: Int) {
        TODO("Not yet implemented")
    }
    companion object DiffCallback : DiffUtil.ItemCallback<MissionGroup>() {
        override fun areItemsTheSame(oldItem: MissionGroup, newItem: MissionGroup): Boolean {
            return oldItem === newItem
        }

        override fun areContentsTheSame(oldItem: MissionGroup, newItem: MissionGroup): Boolean {
            return oldItem.missionId == newItem.missionId
        }
    }


}