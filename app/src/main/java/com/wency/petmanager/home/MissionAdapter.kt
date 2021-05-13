package com.wency.petmanager.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.wency.petmanager.data.MissionToday
import com.wency.petmanager.databinding.SubItemTimelineMissionBinding

class MissionAdapter(private val mission: List<MissionToday>): RecyclerView.Adapter<MissionAdapter.MissionViewHolder>() {
    class MissionViewHolder(val binding: SubItemTimelineMissionBinding):RecyclerView.ViewHolder(binding.root){
        fun bind(mission: MissionToday){
            binding.missionToday = mission
            binding.executePendingBindings()
        }
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MissionViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return MissionViewHolder(SubItemTimelineMissionBinding.inflate(layoutInflater, parent, false))
    }

    override fun onBindViewHolder(holder: MissionViewHolder, position: Int) {
        holder.bind(mission[position])
    }

    override fun getItemCount(): Int {
        return mission.size
    }

}