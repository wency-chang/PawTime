package com.wency.petmanager.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.wency.petmanager.data.MissionGroup
import com.wency.petmanager.data.Pet
import com.wency.petmanager.data.source.Repository
import com.wency.petmanager.dialog.MissionListViewModel
import com.wency.petmanager.profile.PetProfileViewModel

class MissionListViewModelFactory(private val firebaseRepository: Repository, private val missionList: Array<MissionGroup>?,
                                  private val petProfile: Pet
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>) =
        with(modelClass) {
            when {
                isAssignableFrom(MissionListViewModel::class.java) ->
                    MissionListViewModel(firebaseRepository, missionList, petProfile)

                else ->
                    throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
            }
        } as T
}
