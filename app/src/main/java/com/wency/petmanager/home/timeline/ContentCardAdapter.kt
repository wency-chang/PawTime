package com.wency.petmanager.home.timeline

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.wency.petmanager.data.Event
import com.wency.petmanager.databinding.SubItemTimelinePhotoBinding
import com.wency.petmanager.home.HomeViewModel

class ContentCardAdapter(private val eventToday: MutableList<Event>, val viewModel: HomeViewModel): RecyclerView.Adapter<ContentCardAdapter.PhotoCardViewHolder>() {



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoCardViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return PhotoCardViewHolder(SubItemTimelinePhotoBinding.inflate(layoutInflater, parent, false))
    }

    override fun onBindViewHolder(holder: PhotoCardViewHolder, position: Int) {
        val event = eventToday[position]
        holder.bind(event)
        holder.itemView.setOnClickListener {
            viewModel.navigateToDetail(event)
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

    override fun getItemCount(): Int {
        return eventToday.size
    }
}
