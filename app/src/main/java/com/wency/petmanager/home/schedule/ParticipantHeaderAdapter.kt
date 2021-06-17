package com.wency.petmanager.home.schedule

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.wency.petmanager.databinding.SubItemCircleHeaderParticipantBinding

class ParticipantHeaderAdapter(val photoList: List<String>):
    RecyclerView.Adapter<ParticipantHeaderAdapter.UserListHolder>() {
    private val maxNumber = 6

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserListHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return UserListHolder(SubItemCircleHeaderParticipantBinding.inflate(layoutInflater, parent, false))
    }

    override fun onBindViewHolder(holder: UserListHolder, position: Int) {
        if (position == 0){
            setMargin(holder.layout, 0,0,0,0)
        }
        holder.bind(photoList[position])
    }

    override fun getItemCount(): Int {
        return if (photoList.size < maxNumber){
            photoList.size
        } else maxNumber
    }

    class UserListHolder(val binding: SubItemCircleHeaderParticipantBinding): RecyclerView.ViewHolder(binding.root){
        val layout = binding.linearLayoutForMargin
        fun bind(photo: String){
            binding.image = photo
            binding.executePendingBindings()
        }
    }

    private fun setMargin(view: View, left: Int, right: Int, top: Int, bottom: Int){
        if (view.layoutParams is ViewGroup.MarginLayoutParams){
            val p = view.layoutParams as ViewGroup.MarginLayoutParams
            p.setMargins(left, top, right, bottom)
            view.requestLayout()
        }
    }



}