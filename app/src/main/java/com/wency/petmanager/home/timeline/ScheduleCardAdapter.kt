package com.wency.petmanager.home.timeline

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.wency.petmanager.MainViewModel
import com.wency.petmanager.data.Event
import com.wency.petmanager.databinding.SubItemTimelineScheduleBinding
import com.wency.petmanager.home.HomeViewModel
import com.wency.petmanager.home.schedule.MemoAdapter
import com.wency.petmanager.home.schedule.ParticipantHeaderAdapter
import com.wency.petmanager.profile.TimeFormat

class ScheduleCardAdapter(
    private val events: List<Event>,
    private val viewModel: HomeViewModel,
    private val mainViewModel: MainViewModel):
    RecyclerView.Adapter<ScheduleCardAdapter.ScheduleCardViewHolder>() {

    private val expendList = mutableListFalse(events.size)

    private fun mutableListFalse(size: Int): MutableList<Boolean> {
        val list = mutableListOf<Boolean>()
        for (i in 0 until size){
            list.add(false)
        }
        return list
    }

    class ScheduleCardViewHolder(val binding: SubItemTimelineScheduleBinding):
        RecyclerView.ViewHolder(binding.root){
        val expendButton = binding.expendButton
        fun bind(event: Event){
            binding.date = TimeFormat.dateFormat.format(event.date.toDate()).toString()
            event.time?.let {
                binding.time = TimeFormat.timeFormat12.format(it.toDate()).toString()
            }
            if (!event.memoList.isNullOrEmpty()) {
                binding.scheduleMemoRecycler.adapter = MemoAdapter(event.memoList)
            }
            binding.event = event
            binding.executePendingBindings()
        }


        fun bindUserPhoto(images: List<String>){
            binding.participantHeaderRecycler.adapter = ParticipantHeaderAdapter(images)
            binding.executePendingBindings()
        }

        fun bindPetPhoto(images: List<String>){
            binding.participantPetHeaderRecycler.adapter = ParticipantHeaderAdapter(images)
            binding.executePendingBindings()
        }

        fun bindExpend(expend: Boolean){
            binding.expand = expend
            binding.needExpand = !expend
            binding.executePendingBindings()
        }



    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScheduleCardViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return ScheduleCardViewHolder(SubItemTimelineScheduleBinding.inflate(layoutInflater, parent, false))
    }

    override fun onBindViewHolder(holder: ScheduleCardViewHolder, position: Int) {
        val event = events[position]
        holder.bind(event)

        event.userParticipantList?.let { userParticipant->
            val photoList = mutableListOf<String>()
            for (userId in userParticipant) {
                if (viewModel.friendList.isNotEmpty()) {
                    val photo = viewModel.friendList.filter {
                        it.userId == userId
                    }
                    if (photo.isNotEmpty()) {
                        photo[0].userPhoto?.let {
                            photoList.add(it)
                        }
                    }
                }
                holder.bindUserPhoto(photoList)
            }
        }

        if (event.petParticipantList.isNotEmpty()) {
            val photoList = mutableListOf<String>()
            for (petId in event.petParticipantList){
                val photo = mainViewModel.petDataForAll.filter {
                    it.id == petId
                }
                if (photo.isNotEmpty()){
                    photo[0].profilePhoto.let {
                        photoList.add(it)
                    }
                }
            }
            holder.bindPetPhoto(photoList)
        }

        holder.bindExpend(expendList[position])



        holder.expendButton.setOnClickListener {
            expendList[position] = true
            notifyDataSetChanged()
        }

        holder.itemView.setOnClickListener {
            viewModel.navigateToDetail(events[position])
        }

    }

    override fun getItemCount(): Int {
        return events.size
    }


}


