package com.wency.petmanager.dialog.mission

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.wency.petmanager.data.MissionGroup
import com.wency.petmanager.databinding.ItemMissionListHolderBinding

class MissionAdapter(val clickListener: OnClickListener):
    ListAdapter<MissionGroup, MissionAdapter.MissionViewHolder >(DiffCallback) {
    class MissionViewHolder(val binding: ItemMissionListHolderBinding): RecyclerView.ViewHolder(binding.root){
        val deleteButton = binding.missionDeleteButton
        fun bind(mission: MissionGroup){
            binding.mission = mission
            binding.executePendingBindings()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MissionViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return MissionViewHolder(
            ItemMissionListHolderBinding.inflate(layoutInflater, parent, false)
        )
    }

    override fun onBindViewHolder(holder: MissionViewHolder, position: Int) {
        val mission = getItem(position)
        holder.bind(mission)
        holder.deleteButton.setOnClickListener {
            clickListener.onClick(mission)
        }
    }
    class OnClickListener(val clickListener: (mission: MissionGroup)-> Unit){
        fun onClick(mission: MissionGroup) = clickListener(mission)
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