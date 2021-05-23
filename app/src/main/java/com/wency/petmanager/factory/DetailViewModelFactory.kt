package com.wency.petmanager.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.wency.petmanager.data.Event
import com.wency.petmanager.data.source.Repository
import com.wency.petmanager.detail.DiaryDetailViewModel
import com.wency.petmanager.detail.ScheduleDetailViewModel

@Suppress("UNCHECKED_CAST")
class DetailViewModelFactory(
    private val repository: Repository,
    private val eventDetail: Event
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>) =
        with(modelClass) {
            when {
                isAssignableFrom(DiaryDetailViewModel::class.java) ->
                    DiaryDetailViewModel(repository, eventDetail)

                isAssignableFrom(ScheduleDetailViewModel::class.java) ->
                    ScheduleDetailViewModel(repository, eventDetail)

                else ->
                    throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
            }
        } as T
}

