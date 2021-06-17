package com.wency.petmanager.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.wency.petmanager.create.CreateEventViewModel
import com.wency.petmanager.data.Pet
import com.wency.petmanager.data.source.Repository

class CreateEventViewModelFactory(private val repository: Repository
                                  , val petList: Array<Pet>
                                  , private val selectedList: Array<String>
): ViewModelProvider.Factory {

    override fun <T : ViewModel?> create(modelClass: Class<T>)=
        with(modelClass) {
            when {
                isAssignableFrom(CreateEventViewModel::class.java) ->
                    CreateEventViewModel(repository, petList, selectedList)

                else ->
                    throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
            }
        } as T

}