package com.wency.petmanager.home

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.wency.petmanager.data.Event
import com.wency.petmanager.databinding.SubItemTimelineScheduleBinding
import com.wency.petmanager.home.schedule.MemoAdapter
import com.wency.petmanager.home.schedule.ParticipantHeaderAdapter
import com.wency.petmanager.profile.Today

class ScheduleCardAdapter(val events: List<Event>, val viewModel: HomeViewModel): RecyclerView.Adapter<ScheduleCardAdapter.ScheduleCardViewHolder>() {

    class ScheduleCardViewHolder(val binding: SubItemTimelineScheduleBinding): RecyclerView.ViewHolder(binding.root){
        val expendButton = binding.expendButton
        var expend: Boolean = false
        var needExpend: Boolean = true
        fun bind(event: Event){
            binding.date = Today.dateFormat.format(event.date.toDate()).toString()
            event.time?.let {
                binding.time = Today.timeFormat.format(it.toDate()).toString()
            }
            if (!event.memoList.isNullOrEmpty()) {
                binding.scheduleMemoRecycler.adapter = MemoAdapter(event.memoList)
            }
            binding.expand = expend
            binding.needExpand = needExpend
            binding.event = event

            binding.executePendingBindings()
        }
        fun clickExpend(){
            expend = expend == false
            needExpend = false
        }

        fun bindUserPhoto(images: List<String>){
            binding.participantHeaderRecycler.adapter = ParticipantHeaderAdapter(images)
            binding.executePendingBindings()
        }

        fun bindPetPhoto(images: List<String>){
            binding.participantPetHeaderRecycler.adapter = ParticipantHeaderAdapter(images)
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
                    Log.d("get user photo","$photo")
                    photo[0].userPhoto?.let {
                        photoList.add(it)
                    }
                }
                holder.bindUserPhoto(photoList)
            }
        }

        event.petParticipantList?.let {petParticipant->
            val photoList = mutableListOf<String>()
            for (petId in petParticipant){
                val photo = viewModel.userPetList?.filter {
                    it.id == petId
                }
                photo?.get(0)?.profilePhoto?.let {
                    photoList.add(it)
                }
            }
            holder.bindPetPhoto(photoList)
        }



        holder.expendButton.setOnClickListener {
            holder.clickExpend()
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


