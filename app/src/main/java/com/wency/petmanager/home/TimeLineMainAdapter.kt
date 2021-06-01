package com.wency.petmanager.home

import android.annotation.SuppressLint
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
import com.wency.petmanager.data.*
import com.wency.petmanager.databinding.ItemHomeScheduleCardBinding
import com.wency.petmanager.databinding.ItemHomeTimelineEventBinding
import com.wency.petmanager.databinding.ItemTimelineTodayMissionBinding
import com.wency.petmanager.profile.Today
import kotlinx.android.synthetic.main.fragment_diary_create.view.*
import kotlinx.android.synthetic.main.item_home_timeline_event.view.*
import java.lang.Math.abs
import java.text.SimpleDateFormat
import java.util.*

class TimeLineMainAdapter(val viewModel: HomeViewModel, val mainViewModel: MainViewModel) :
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

//                holder.recyclerPetHeader.apply {
//                    setRecycledViewPool(viewPool)
//                }
//                val compositePageTransformations = CompositePageTransformer()
//                compositePageTransformations.apply {
//                    addTransformer(MarginPageTransformer(10))
//                    addTransformer(ScaleInTransformer())
//                }

//                holder.pagerContent.apply {
//                    offscreenPageLimit = 3
//                    getChildAt(0).overScrollMode = RecyclerView.OVER_SCROLL_NEVER
//                    getChildAt(0).apply {
//                        val padding = resources.getDimensionPixelOffset(0)+ resources.getDimensionPixelSize(0)
//                        setPadding(padding, 0, padding, 0)
//                        clipToPadding = false
//                    }
//                    setPadding(20,0,20,0)
//                    clipChildren = false
//                    clipToPadding = false
//                    setPageTransformer(compositePageTransformations)
//                }

            }

            is TimelineTodayViewHolder -> {
                holder.bind((item as TimelineItem.Today).missionToday)
                holder.missionAdapter.adapter = item.missionToday.missionList?.let { MissionAdapter(it, viewModel) }

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
        //            val recyclerPetHeader = binding.petParticipantRecycler
        private val viewPager2 = binding.contentCardPager
        fun bind(events: DayEvent, viewModel: HomeViewModel) {
            val petParticipantPhoto = mutableListOf<String>()

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



//            for(item in events.eventList){
//                item.petHeaderList.let { petParticipantPhoto.addAll(it) }
//            }
//            binding.petParticipantRecycler.adapter = PetParticipantAdapter(petParticipantPhoto)
//            binding.contentCardPager
//            binding.contentCardPager.apply {
//                adapter = ContentCardAdapter(events.eventList, viewModel)
//                clipToPadding = false
//                clipChildren = false
//                offscreenPageLimit = 1
//                setPageTransformer(compositePageTransformations)

//                getChildAt(0).apply {
//                    clipToPadding = false
//                    setPadding(150,10,150,10)
//
//                    clipChildren = false
//                }
//                setPadding(120,0,120,0)

//                getChildAt(0).overScrollMode = RecyclerView.OVER_SCROLL_NEVER

//            }
            binding.year = Today.yearOnlyFormat.format(events.date)
            binding.date = Today.dateOnlyFormat.format(events.date)
            binding.dayOfWeek = Today.dayOfWeekFormat.format(events.date)
            binding.executePendingBindings()
        }
    }

    class TimelineTodayViewHolder(val binding: ItemTimelineTodayMissionBinding) :
        RecyclerView.ViewHolder(binding.root) {
        val missionAdapter = binding.missionRecyclerView
        fun bind(mission: DayMission) {
            binding.year = Today.yearOnlyFormat.format(Date())
            binding.date = Today.dateOnlyFormat.format(Date())
            binding.dayOfWeek = Today.dayOfWeekFormat.format(Date())
//            binding.missionRecyclerView.adapter = mission.missionList?.let { MissionAdapter(it) }
            binding.missionVisibility = mission.missionList?.isNotEmpty()
            binding.executePendingBindings()
        }
    }

    class TimelineCardViewHolder(val binding: ItemHomeScheduleCardBinding) :
        RecyclerView.ViewHolder(binding.root) {
        val recyclerScheduleCard = binding.contentCardRecycler
        fun bind(events: DayEvent, viewModel: HomeViewModel, mainViewModel: MainViewModel) {
            binding.year = Today.yearOnlyFormat.format(events.date)
            binding.date = Today.dateOnlyFormat.format(events.date)
            binding.dayOfWeek = Today.dayOfWeekFormat.format(events.date)
            binding.contentCardRecycler.adapter = ScheduleCardAdapter(events.eventList.sortedBy { it.time }, viewModel, mainViewModel)
            binding.executePendingBindings()
        }
    }
//    class OnClickListenerToDetail (val clickListener: (event: Event)-> Unit){
//        fun onClick(event: Event) = clickListener(event)
//    }


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

        @SuppressLint("SimpleDateFormat")
        val timeFormat = SimpleDateFormat("yyyy.MM.dd E")
    }

    class ScaleInTransformer : ViewPager2.PageTransformer {
        //        private val mMinScale = DEFAULT_MIN_SCALE
        override fun transformPage(view: View, position: Float) {
//                view.elevation = -abs(position)
//                val pageWidth = view.width
//                val pageHeight = view.height
//                view.pivotY = (pageHeight / 2).toFloat()
//                view.pivotX = (pageWidth / 2).toFloat()
//                if (position < -1) {
//                    view.scaleX = mMinScale
//                    view.scaleY = mMinScale
//                    view.pivotX = pageWidth.toFloat()
//                } else if (position <= 1) {
//                    if (position < 0) {
//                        val scaleFactor = (1 + position) * (1 - mMinScale) + mMinScale
//                        view.scaleX = scaleFactor
//                        view.scaleY = scaleFactor
//                        view.pivotX = pageWidth * (DEFAULT_CENTER + DEFAULT_CENTER * -position)
//                    } else {
//                        val scaleFactor = (1 - position) * (1 - mMinScale) + mMinScale
//                        view.scaleX = scaleFactor
//                        view.scaleY = scaleFactor
//                        view.pivotX = pageWidth * ((1 - position) * DEFAULT_CENTER)
//                    }
//                } else {
//                    view.pivotX = 0f
//                    view.scaleX = mMinScale
//                    view.scaleY = mMinScale
//                }
            val r = 1 - abs(position)
            view.scaleY = (0.8f + (r * 0.2f))
            view.scaleX = (0.8f + (r * 0.2f))
        }


//        companion object {
//            const val DEFAULT_MIN_SCALE = 0.85f
//            const val DEFAULT_CENTER = 0.5f
//        }

    }
}