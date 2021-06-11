package com.wency.petmanager.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.wency.petmanager.data.Event
import com.wency.petmanager.data.Pet
import com.wency.petmanager.memory.MemoryViewModel

class MemoryViewModelFactory(private val petData: Pet,
                             private val eventList: Array<Event>?,
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>) =
        with(modelClass) {
            when {

                isAssignableFrom(MemoryViewModel::class.java) ->
                    MemoryViewModel(petData, eventList)

                else ->
                    throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
            }
        } as T
}