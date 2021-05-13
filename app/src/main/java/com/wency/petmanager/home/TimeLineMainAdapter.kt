package com.wency.petmanager.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.wency.petmanager.data.Diary
import com.wency.petmanager.databinding.ItemHomeTimelineEventBinding

class TimeLineMainAdapter(val viewModel: HomeViewModel): ListAdapter<Diary, TimeLineMainAdapter.TimeLineViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TimeLineViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return TimeLineViewHolder(
            ItemHomeTimelineEventBinding.inflate(
                layoutInflater,
                parent,
                false
            )
        )

    }
    private val viewPool = RecyclerView.RecycledViewPool()

    override fun onBindViewHolder(holder: TimeLineViewHolder, position: Int) {

        holder.recyclerViewCard.apply {
            viewModel.petPhotoMock?.let {
                this.adapter = ContentCardAdapter(it as List<String>)
                this.hasFixedSize()
                this.setRecycledViewPool(viewPool)
            }
        }

        holder.recyclerViewPet.apply {
            viewModel.petPhotoMock?.let {
                this.adapter = PetParticipantAdapter(it)
                this.hasFixedSize()
                this.setRecycledViewPool(viewPool)
            }
        }
        holder.bind(getItem(position), viewModel)


    }

    override fun getItemViewType(position: Int): Int {
        return super.getItemViewType(position)
    }



    class TimeLineViewHolder(val binding: ItemHomeTimelineEventBinding): RecyclerView.ViewHolder(binding.root){
        val recyclerViewPet = binding.petParticipantRecycler
        val recyclerViewCard = binding.contentCardRecycler
        fun bind(diary: Diary, viewModel: HomeViewModel){
            binding.diary = diary
            binding.viewModel = viewModel
//            binding.executePendingBindings()
        }
    }
    companion object DiffCallback : DiffUtil.ItemCallback<Diary>() {
        override fun areItemsTheSame(oldItem: Diary, newItem: Diary): Boolean {
            return oldItem === newItem
        }
        override fun areContentsTheSame(oldItem: Diary, newItem: Diary): Boolean {
            return oldItem.date == newItem.date
        }

        private const val ITEM_VIEW_TYPE_TODAY = 0x00
        private const val ITEM_VIEW_TYPE_EVENT = 0x01
    }


}