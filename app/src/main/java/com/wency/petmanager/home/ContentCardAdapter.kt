package com.wency.petmanager.home

import android.telephony.ims.ImsMmTelManager
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.wency.petmanager.data.Event
import com.wency.petmanager.databinding.SubItemTimelinePhotoBinding
import com.wency.petmanager.databinding.SubItemTimelineScheduleBinding

class ContentCardAdapter(private val eventToday: List<Event>, val viewModel: HomeViewModel): RecyclerView.Adapter<RecyclerView.ViewHolder>() {



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return when(viewType){
            ITEM_TYPE_PHOTO -> {
                PhotoCardViewHolder(SubItemTimelinePhotoBinding.inflate(layoutInflater, parent, false))
            }
            ITEM_TYPE_SCHEDULE -> {
                ScheduleCardViewHolder(SubItemTimelineScheduleBinding.inflate(layoutInflater, parent, false))
            }
            else -> throw ClassCastException("Unknown viewType $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val event = eventToday[position]
        when (holder){
            is PhotoCardViewHolder -> {
                holder.bind(event)
                Log.d("Debug","Photo")
            }
            is ScheduleCardViewHolder -> {
                holder.bind(event)
                Log.d("Debug","Schedule")
            }
        }

    }

    class PhotoCardViewHolder(val binding: SubItemTimelinePhotoBinding): RecyclerView.ViewHolder(binding.root){
        fun bind(event: Event){
            event.photoList?.let {
                binding.image = it[0]
                binding.title = event.title
                binding.executePendingBindings()
            }
        }
    }
    class ScheduleCardViewHolder(val binding: SubItemTimelineScheduleBinding): RecyclerView.ViewHolder(binding.root){
        fun bind(event: Event){
            binding.event = event
        }
    }

    override fun getItemCount(): Int {
        Log.d("Debug","eventToday ${eventToday}")
        return eventToday.size

    }

    override fun getItemViewType(position: Int): Int {
        val list = eventToday[position]
        Log.d("Debug","${list.complete}")
        return if (list.type == "diary"){
            ITEM_TYPE_PHOTO
        } else if (list.complete && !list.photoList.isNullOrEmpty()){

            ITEM_TYPE_PHOTO
        } else {
            ITEM_TYPE_SCHEDULE
        }
    }

    companion object {
        private const val ITEM_TYPE_SCHEDULE = 0x00
        private const val ITEM_TYPE_PHOTO = 0x01
    }


}