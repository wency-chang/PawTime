package com.wency.petmanager.home

import android.telephony.ims.ImsMmTelManager
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.wency.petmanager.data.Event
import com.wency.petmanager.databinding.SubItemTimelinePhotoBinding
import com.wency.petmanager.databinding.SubItemTimelineScheduleBinding

class ContentCardAdapter(private val eventToday: List<Event>, val viewModel: HomeViewModel): RecyclerView.Adapter<ContentCardAdapter.PhotoCardViewHolder>() {



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoCardViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return PhotoCardViewHolder(SubItemTimelinePhotoBinding.inflate(layoutInflater, parent, false))
    }

    override fun onBindViewHolder(holder: PhotoCardViewHolder, position: Int) {
        val event = eventToday[position]
        holder.bind(event)
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

    override fun getItemCount(): Int {
        return eventToday.size
    }
}
