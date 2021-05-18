package com.wency.petmanager.home

import android.util.EventLog
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.wency.petmanager.data.Event
import com.wency.petmanager.databinding.SubItemTimelineScheduleBinding

class ScheduleCardAdapter(val events: List<Event>, val viewModel: HomeViewModel): RecyclerView.Adapter<ScheduleCardAdapter.ScheduleCardViewHolder>() {

    class ScheduleCardViewHolder(val binding: SubItemTimelineScheduleBinding): RecyclerView.ViewHolder(binding.root){
        val expendButton = binding.expendButton
        var expend: Boolean = false
        var needExpend: Boolean = true
        fun bind(event: Event){
                binding.expand = expend
                binding.needExpand = needExpend
                binding.event = event
                binding.executePendingBindings()
        }
        fun clickExpend(){
            expend = expend == false
            needExpend = false
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScheduleCardViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return ScheduleCardViewHolder(SubItemTimelineScheduleBinding.inflate(layoutInflater, parent, false))
    }

    override fun onBindViewHolder(holder: ScheduleCardViewHolder, position: Int) {
        holder.bind(events[position])
        holder.expendButton.setOnClickListener {
            holder.clickExpend()
            notifyDataSetChanged()
        }
    }

    override fun getItemCount(): Int {
        return events.size
    }


}


