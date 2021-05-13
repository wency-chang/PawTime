package com.wency.petmanager.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.wency.petmanager.databinding.SubItemTimelineDiaryBinding

class ContentCardAdapter(private val photoList: List<String>): RecyclerView.Adapter<ContentCardAdapter.DiaryCardViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DiaryCardViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return DiaryCardViewHolder(SubItemTimelineDiaryBinding.inflate(layoutInflater, parent, false))
    }

    override fun onBindViewHolder(holder: DiaryCardViewHolder, position: Int) {
        holder.bind(photoList[position])
    }

    class DiaryCardViewHolder(val binding: SubItemTimelineDiaryBinding): RecyclerView.ViewHolder(binding.root){
        fun bind(image: String){
            binding.image = image
            binding.executePendingBindings()
        }
    }

    override fun getItemCount(): Int {
        return photoList.size
    }


}