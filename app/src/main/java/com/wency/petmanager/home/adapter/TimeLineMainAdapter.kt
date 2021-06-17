package com.wency.petmanager.home.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.CompositePageTransformer
import androidx.viewpager2.widget.MarginPageTransformer
import androidx.viewpager2.widget.ViewPager2
import com.wency.petmanager.MainViewModel
import com.wency.petmanager.data.DayEvent
import com.wency.petmanager.data.DayMission
import com.wency.petmanager.data.TimelineItem
import com.wency.petmanager.databinding.ItemHomeScheduleCardBinding
import com.wency.petmanager.databinding.ItemHomeTimelineEventBinding
import com.wency.petmanager.databinding.ItemTimelineTodayMissionBinding
import com.wency.petmanager.home.HomeViewModel
import com.wency.petmanager.home.timeline.ContentCardAdapter
import com.wency.petmanager.home.timeline.MissionAdapter
import com.wency.petmanager.home.timeline.ScheduleCardAdapter
import com.wency.petmanager.profile.TimeFormat
import java.util.*

class TimeLineMainAdapter(private val viewModel: HomeViewModel, private val mainViewModel: MainViewModel) :
    ListAdapter<TimelineItem, RecyclerView.ViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            ITEM_VIEW_TYPE_TODAY -> {
                TimelineTodayViewHolder(
                    ItemTimelineTodayMissionBinding.inflate(
                        layoutInflater,
                        parent,
                        false
                    )
                )
            }
            ITEM_VIEW_TYPE_PHOTO -> {
                TimeLinePhotoViewHolder(
                    ItemHomeTimelineEventBinding.inflate(
                        layoutInflater,
                        parent,
                        false
                    )
                )
            }
            ITEM_VIEW_TYPE_SCHEDULE_CARD -> {
                TimelineCardViewHolder(
                    ItemHomeScheduleCardBinding.inflate(
                        layoutInflater,
                        parent,
                        false
                    )
                )
            }
            else -> throw ClassCastException("Unknown viewType $viewType")
        }

    }

    private val viewPool = RecyclerView.RecycledViewPool()

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = getItem(position)
        when (holder) {
            is TimeLinePhotoViewHolder -> {
                holder.bind((item as TimelineItem.TimelineEvent).event, viewModel)
            }

            is TimelineTodayViewHolder -> {
                holder.bind((item as TimelineItem.Today).missionToday)
                holder.missionAdapter.adapter =
                    item.missionToday.missionList?.let { MissionAdapter(it, viewModel) }

            }
            is TimelineCardViewHolder -> {
                holder.bind((item as TimelineItem.TimelineSchedule).event, viewModel, mainViewModel)
                holder.recyclerScheduleCard.apply {
                    setRecycledViewPool(viewPool)
                }
            }
        }

    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is TimelineItem.Today -> ITEM_VIEW_TYPE_TODAY
            is TimelineItem.TimelineEvent -> ITEM_VIEW_TYPE_PHOTO
            is TimelineItem.TimelineSchedule -> ITEM_VIEW_TYPE_SCHEDULE_CARD
            else ->  ITEM_VIEW_TYPE_SCHEDULE_CARD
        }
    }

    class TimeLinePhotoViewHolder(val binding: ItemHomeTimelineEventBinding) :
        RecyclerView.ViewHolder(binding.root) {

        private val viewPager2 = binding.contentCardPager
        fun bind(events: DayEvent, viewModel: HomeViewModel) {
            val compositePageTransformations = CompositePageTransformer()
            compositePageTransformations.apply {
                addTransformer(MarginPageTransformer(10))
                addTransformer(ScaleInTransformer())
            }

            viewPager2.apply {
                offscreenPageLimit = 1
                val recyclerView = getChildAt(0) as RecyclerView
                recyclerView.apply {
                    setPadding(100, 0, 200, 0)
                    clipToPadding = false
                }
                setPageTransformer(compositePageTransformations)
                adapter = ContentCardAdapter(events.eventList, viewModel)
            }

            binding.year = TimeFormat.yearOnlyFormat.format(events.date)
            binding.date = TimeFormat.dateOnlyFormat.format(events.date)
            binding.dayOfWeek = TimeFormat.dayOfWeekFormat.format(events.date)
            binding.executePendingBindings()
        }
    }

    class TimelineTodayViewHolder(val binding: ItemTimelineTodayMissionBinding) :
        RecyclerView.ViewHolder(binding.root) {
        val missionAdapter = binding.missionRecyclerView

        fun bind(mission: DayMission) {
            binding.year = TimeFormat.yearOnlyFormat.format(Date())
            binding.date = TimeFormat.dateOnlyFormat.format(Date())
            binding.dayOfWeek = TimeFormat.dayOfWeekFormat.format(Date())
            binding.missionVisibility = mission.missionList?.isNotEmpty()
            binding.executePendingBindings()
        }
    }

    class TimelineCardViewHolder(val binding: ItemHomeScheduleCardBinding) :
        RecyclerView.ViewHolder(binding.root) {
        val recyclerScheduleCard = binding.contentCardRecycler

        fun bind(events: DayEvent, viewModel: HomeViewModel, mainViewModel: MainViewModel) {
            binding.year = TimeFormat.yearOnlyFormat.format(events.date)
            binding.date = TimeFormat.dateOnlyFormat.format(events.date)
            binding.dayOfWeek = TimeFormat.dayOfWeekFormat.format(events.date)
            binding.contentCardRecycler.adapter =
                ScheduleCardAdapter(events.eventList.sortedBy { it.time }, viewModel, mainViewModel)
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
        private const val ITEM_VIEW_TYPE_PHOTO = 0x01
        private const val ITEM_VIEW_TYPE_SCHEDULE_CARD = 0x02

    }

    class ScaleInTransformer : ViewPager2.PageTransformer {
        override fun transformPage(view: View, position: Float) {
            val r = 1 - kotlin.math.abs(position)
            view.scaleY = (0.8f + (r * 0.2f))
            view.scaleX = (0.8f + (r * 0.2f))
        }
    }
}