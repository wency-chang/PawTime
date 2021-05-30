package com.wency.petmanager.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.wency.petmanager.MainViewModel
import com.wency.petmanager.create.events.DiaryCreateViewModel
import com.wency.petmanager.create.events.MissionCreateViewModel
import com.wency.petmanager.create.events.ScheduleCreateViewModel
import com.wency.petmanager.data.source.Repository
import com.wency.petmanager.friend.FriendListViewModel

@Suppress("UNCHECKED_CAST")
class ViewModelFactory constructor(
    private val repository: Repository
) : ViewModelProvider.NewInstanceFactory() {

    override fun <T : ViewModel> create(modelClass: Class<T>) =
        with(modelClass) {
            when {
                isAssignableFrom(MainViewModel::class.java) ->
                    MainViewModel(repository)

                isAssignableFrom(DiaryCreateViewModel::class.java) ->
                    DiaryCreateViewModel(repository)

                isAssignableFrom(ScheduleCreateViewModel::class.java) ->
                    ScheduleCreateViewModel(repository)

                isAssignableFrom(MissionCreateViewModel::class.java) ->
                    MissionCreateViewModel(repository)

                isAssignableFrom(FriendListViewModel::class.java) ->
                    FriendListViewModel(repository)

                else ->
                    throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
            }
        } as T
}
