package com.wency.petmanager.home.schedule

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.auth.User
import com.wency.petmanager.databinding.SubItemCircleHeaderParticipantBinding
import com.wency.petmanager.databinding.SubItemMemoTextBinding

class ParticipantHeaderAdapter(val photoList: List<String>): RecyclerView.Adapter<ParticipantHeaderAdapter.UserListHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserListHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return UserListHolder(SubItemCircleHeaderParticipantBinding.inflate(layoutInflater, parent, false))
    }

    override fun onBindViewHolder(holder: UserListHolder, position: Int) {
        holder.bind(photoList[position])
    }

    override fun getItemCount(): Int {
        return photoList.size
    }

    class UserListHolder(val binding: SubItemCircleHeaderParticipantBinding): RecyclerView.ViewHolder(binding.root){

        fun bind(photo: String){
            binding.image = photo
            binding.executePendingBindings()
        }
    }
}