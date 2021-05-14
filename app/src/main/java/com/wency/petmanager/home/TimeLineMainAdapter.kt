package com.wency.petmanager.home

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.wency.petmanager.data.*
import com.wency.petmanager.databinding.ItemHomeTimelineEventBinding
import com.wency.petmanager.databinding.ItemTimelineTodayMissionBinding
import java.text.SimpleDateFormat
import java.util.*

class TimeLineMainAdapter(val viewModel: HomeViewModel): ListAdapter<TimelineItem, RecyclerView.ViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return when(viewType){
            ITEM_VIEW_TYPE_TODAY -> {
                TimelineTodayViewHolder(ItemTimelineTodayMissionBinding.inflate(layoutInflater, parent,false))
            }
            ITEM_VIEW_TYPE_EVENT -> {
                TimeLineEventViewHolder(ItemHomeTimelineEventBinding.inflate(layoutInflater, parent, false))
            }
            else -> throw ClassCastException("Unknown viewType $viewType")
        }

    }
    private val viewPool = RecyclerView.RecycledViewPool()


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = getItem(position)
        when (holder){
            is TimeLineEventViewHolder -> {



                holder.bind((item as TimelineItem.TimelineEvents).event , viewModel)
                holder.recyclerContent.apply {
                    setRecycledViewPool(viewPool)
                }
                holder.recyclerPetHeader.apply {
                    setRecycledViewPool(viewPool)
                }
            }
            is TimelineTodayViewHolder -> {
                holder.bind((item as TimelineItem.Today).missionToday)

            }
        }

    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)){
            is TimelineItem.Today -> ITEM_VIEW_TYPE_TODAY
            is TimelineItem.TimelineEvents -> ITEM_VIEW_TYPE_EVENT
        }
    }

    class TimeLineEventViewHolder(val binding: ItemHomeTimelineEventBinding): RecyclerView.ViewHolder(binding.root){
            val recyclerContent = binding.contentCardRecycler
            val recyclerPetHeader = binding.petParticipantRecycler
        @SuppressLint("SimpleDateFormat")
        fun bind(events: DayEvent, viewModel: HomeViewModel){
            val timeFormat = SimpleDateFormat("yyyy.MM.dd E")


            val petParticipantPhoto = mutableListOf<String>()

            for(item in events.eventList){
                item.petHeaderList.let { petParticipantPhoto.addAll(it) }
            }
            Log.d("Debug","PetHeader $petParticipantPhoto")
            binding.petParticipantRecycler.adapter = PetParticipantAdapter(petParticipantPhoto)
            binding.contentCardRecycler.adapter = ContentCardAdapter(events.eventList, viewModel)
            binding.date = timeFormat.format(events.date)
            binding.executePendingBindings()
        }
    }

    class TimelineTodayViewHolder(val binding: ItemTimelineTodayMissionBinding): RecyclerView.ViewHolder(binding.root){
        val timeFormat = SimpleDateFormat("yyyy.MM.dd E")

        fun bind( mission: DayMission){

            binding.today = timeFormat.format(Date())
            binding.missionRecyclerView.adapter = MissionAdapter(mission.missionList)
            binding.executePendingBindings()
        }
    }



    companion object DiffCallback : DiffUtil.ItemCallback<TimelineItem>() {
        override fun areItemsTheSame(oldItem: TimelineItem, newItem: TimelineItem): Boolean {
            return oldItem === newItem
        }
        override fun areContentsTheSame(oldItem: TimelineItem, newItem: TimelineItem): Boolean {
            return oldItem == newItem
        }
        private const val ITEM_VIEW_TYPE_TODAY = 0x00
        private const val ITEM_VIEW_TYPE_EVENT = 0x01
    }
}