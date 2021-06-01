package com.wency.petmanager.home

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Timestamp
import com.wency.petmanager.R
import com.wency.petmanager.data.MissionGroup
import com.wency.petmanager.data.MissionToday
import com.wency.petmanager.databinding.SubItemTimelineMissionBinding
import com.wency.petmanager.profile.UserManager
import java.util.*

class MissionAdapter(private val mission: List<MissionToday>, val viewModel: HomeViewModel): RecyclerView.Adapter<MissionAdapter.MissionViewHolder>() {
    class MissionViewHolder(val binding: SubItemTimelineMissionBinding):RecyclerView.ViewHolder(binding.root){
        val completeCheckBox = binding.checkBox
        val completeUserPhoto = binding.completeUserImage

        fun bind(mission: MissionToday){
            binding.missionToday = mission
            binding.executePendingBindings()
        }
        fun bindEdit(editable: Boolean){
            binding.editable = editable
        }
        fun bindUserPhoto(photo: String){
            binding.userPhoto = photo
        }

    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MissionViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return MissionViewHolder(SubItemTimelineMissionBinding.inflate(layoutInflater, parent, false))
    }

    override fun onBindViewHolder(holder: MissionViewHolder, position: Int) {
        val mission = mission[position]
        if (mission.complete){
            holder.bindUserPhoto(mission.completeUserPhoto)
            if (mission.completeUserId == viewModel.userInfoProfile.userId){
                holder.bindEdit(true)
            } else {
                holder.bindEdit(false)
            }
        } else{
            holder.bindEdit(true)
            if (viewModel.userInfoProfile.userPhoto.isNullOrEmpty()){
                holder.bindUserPhoto(UserManager.userDefaultPhoto)
            } else {
                holder.bindUserPhoto(viewModel.userInfoProfile.userPhoto!!)
            }

        }

        holder.bind(mission)
        holder.completeCheckBox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked){
                viewModel.changeMissionStatus(mission.petID, mission.missionId, isChecked)
                holder.completeUserPhoto.apply {
                    visibility = View.VISIBLE
                    startAnimation(AnimationUtils.loadAnimation(this.context,R.anim.image_show_up))
                }

            } else {
                Log.d("checked button clicked", "isChecked $isChecked")
                holder.completeUserPhoto.apply {
                    visibility = View.GONE
                    startAnimation(AnimationUtils.loadAnimation(this.context,R.anim.image_gone))
                }

            }
        }
        holder.completeUserPhoto.setOnClickListener {
            if (mission.completeUserId == viewModel.userInfoProfile.userId) {

                viewModel.changeMissionStatus(mission.petID, mission.missionId, false)
                holder.completeUserPhoto.apply {
                    visibility = View.GONE
                    startAnimation(AnimationUtils.loadAnimation(this.context, R.anim.image_gone))
                }
                holder.completeCheckBox.isChecked = false
            }

        }

    }

    override fun getItemCount(): Int {
        return mission.size
    }

}